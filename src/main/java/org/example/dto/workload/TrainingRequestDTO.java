package org.example.dto.workload;

import java.util.Date;

import org.example.enums.ActionType;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainingRequestDTO {

    @NotNull
    private String username;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private boolean isActive;

    @NotNull
    private Date trainingDate;

    private int trainingDuration;

    @NotNull
    private ActionType actionType;
}
