package org.example.feign;

import org.example.dto.workload.TrainingRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@FeignClient(name = "fitness-workload-service", fallback = FitnessWorkloadFallback.class)
public interface FitnessWorkloadServiceClient {

    @PostMapping("/api/workload")
    String processWorkload(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody TrainingRequestDTO trainingRequestDTO);

    @GetMapping("/api/workload")
    int getWorkload(
            @RequestHeader("Authorization") String token,
            @RequestParam String username,
            @RequestParam int year,
            @RequestParam int month
    );
}
