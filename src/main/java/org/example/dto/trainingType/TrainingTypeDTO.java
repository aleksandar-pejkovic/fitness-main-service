package org.example.dto.trainingtype;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainingTypeDTO {

    private long id;

    @NotNull
    private String trainingTypeName;
}
