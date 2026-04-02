package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateMakerDTO {

    private String name;
    private StatusEnum status;
}
