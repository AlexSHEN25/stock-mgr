package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateGoodsTypeDTO {

    private String name;
    private StatusEnum status;
}
