package org.example.feign;

import org.example.dto.workload.TrainingRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class FitnessWorkloadFallback implements FitnessWorkloadServiceClient {

    @Override
    public String processWorkload(TrainingRequestDTO trainingRequestDTO) {
        return "Workload service is down!";
    }

    @Override
    public int getWorkload(String username, int year, int month) {
        return 0;
    }
}
