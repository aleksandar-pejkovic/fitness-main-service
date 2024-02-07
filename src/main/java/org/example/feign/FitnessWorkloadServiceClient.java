package org.example.feign;

import org.example.dto.workload.TrainingRequestDTO;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@FeignClient(name = "fitness-workload-service", fallback = FitnessWorkloadFallback.class)
public interface FitnessWorkloadServiceClient {

    @LoadBalanced
    @PostMapping("/api/workload")
    String processWorkload(@Valid @RequestBody TrainingRequestDTO trainingRequestDTO);

    @LoadBalanced
    @GetMapping("/api/workload")
    int getWorkload(
            @RequestParam String username,
            @RequestParam int year,
            @RequestParam int month
    );
}
