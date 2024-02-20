package org.example.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.example.dto.training.TrainingCreateDTO;
import org.example.dto.workload.TrainingRequestDTO;
import org.example.enums.ActionType;
import org.example.exception.date.IllegalDateArgumentException;
import org.example.exception.notfound.TraineeNotFoundException;
import org.example.exception.notfound.TrainerNotFoundException;
import org.example.exception.notfound.TrainingNotFoundException;
import org.example.exception.notfound.TrainingTypeNotFoundException;
import org.example.feign.FitnessWorkloadServiceClient;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.example.repository.TraineeRepository;
import org.example.repository.TrainerRepository;
import org.example.repository.TrainingRepository;
import org.example.repository.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TrainingService {

    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final TrainingRepository trainingRepository;

    private final TraineeRepository traineeRepository;

    private final TrainerRepository trainerRepository;

    private final TrainingTypeRepository trainingTypeRepository;

    private final TokenService tokenService;

    private final FitnessWorkloadServiceClient fitnessWorkloadServiceClient;


    @Autowired
    public TrainingService(TrainingRepository trainingRepository, TraineeRepository traineeRepository, TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository, TokenService tokenService, FitnessWorkloadServiceClient fitnessWorkloadServiceClient) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.tokenService = tokenService;
        this.fitnessWorkloadServiceClient = fitnessWorkloadServiceClient;
    }

    @Transactional
    public boolean createTraining(TrainingCreateDTO trainingCreateDTO) {
        Trainee trainee = traineeRepository.findByUserUsername(trainingCreateDTO.getTraineeUsername())
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found"));
        Trainer trainer = trainerRepository.findByUserUsername(trainingCreateDTO.getTrainerUsername())
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));
        trainer.getTraineeList().add(trainee);
        trainee.getTrainerList().add(trainer);

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(trainingCreateDTO.getTrainingTypeName())
                .orElseThrow(() -> new TrainingTypeNotFoundException("Training type not found"));

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingType.getTrainingTypeName().name())
                .trainingType(trainingType)
                .trainingDate(trainingCreateDTO.getTrainingDate())
                .trainingDuration(trainingCreateDTO.getTrainingDuration())
                .build();

        Training savedTraining = trainingRepository.save(training);

        String message = processWorkload(savedTraining);

        log.info("Training successfully created");
        log.info(message);
        return Optional.ofNullable(savedTraining).isPresent();
    }

    @Transactional(readOnly = true)
    public Training getTrainingById(long id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException("Training not found"));
        log.info("Training successfully retrieved by id");
        return training;
    }

    @Transactional
    public Training updateTraining(Training training) {
        Training updatedTraining = trainingRepository.save(training);
        log.info("Training successfully updated");
        return updatedTraining;
    }

    @Transactional
    public boolean deleteTraining(Training training) {
        Trainee trainee = training.getTrainee();
        Trainer trainer = training.getTrainer();
        trainer.getTraineeList().remove(trainee);
        trainee.getTrainerList().remove(trainer);
        trainingRepository.delete(training);
        String message = processWorkload(training);
        log.info("Training successfully deleted");
        log.info(message);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingList(String username,
                                                 Date periodFrom,
                                                 Date periodTo,
                                                 String trainerName,
                                                 String trainingTypeName) {
        validateDates(periodFrom, periodTo);
        List<Training> trainingList = trainingRepository.findByTraineeUserUsernameAndTrainingDateBetweenAndTrainerUserUsernameAndTrainingTypeTrainingTypeName(username, periodFrom, periodTo,
                trainerName, trainingTypeName);
        log.info("Successfully retrieved trainee's training list");
        return trainingList;
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingList(String username,
                                                 Date periodFrom,
                                                 Date periodTo,
                                                 String traineeName) {
        validateDates(periodFrom, periodTo);
        List<Training> trainingList = trainingRepository.findByTrainerUserUsernameAndTrainingDateBetweenAndTraineeUserUsername(username, periodFrom, periodTo, traineeName);
        log.info("Successfully retrieved trainer's training list");
        return trainingList;
    }

    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        List<Training> trainings = trainingRepository.findAll();
        log.info("Retrieved all trainings successfully");
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> finaAllTrainingTypes() {
        List<TrainingType> trainingTypes = trainingTypeRepository.findAll();
        log.info("Retrieved all training types successfully");
        return trainingTypes;
    }

    @Transactional(readOnly = true)
    public int getWorkload(String username, int year, int month) {
        String token = getWorkloadServiceToken();
        return fitnessWorkloadServiceClient.getWorkload(token, username, year, month);
    }

    private void validateDates(Date periodFrom, Date periodTo) {
        if (periodTo.before(periodFrom)) {
            String periodFromStr = SIMPLE_DATE_FORMAT.format(periodFrom);
            String periodToStr = SIMPLE_DATE_FORMAT.format(toString());

            String errorMessage = String.format(
                    "'Period to' date %s must be after 'period from' date %s",
                    periodFromStr,
                    periodToStr);
            throw new IllegalDateArgumentException(errorMessage);
        }
    }

    private String processWorkload(Training savedTraining) {
        TrainingRequestDTO trainingRequestDTO = TrainingRequestDTO.builder()
                .username(savedTraining.getTrainer().getUsername())
                .firstName(savedTraining.getTrainer().getUser().getFirstName())
                .lastName(savedTraining.getTrainer().getUser().getLastName())
                .isActive(savedTraining.getTrainer().getUser().isActive())
                .trainingDuration(savedTraining.getTrainingDuration())
                .trainingDate(savedTraining.getTrainingDate())
                .actionType(ActionType.ADD)
                .build();
        String token = getWorkloadServiceToken();
        return fitnessWorkloadServiceClient.processWorkload(token, trainingRequestDTO);
    }

    private String getWorkloadServiceToken() {
        final String BEARER = "Bearer ";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return BEARER + tokenService.generateWorkloadServiceToken(authentication);
    }
}
