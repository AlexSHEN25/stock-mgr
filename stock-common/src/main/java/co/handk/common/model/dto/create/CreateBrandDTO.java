package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateBrandDTO {

    private String name;
    private String englishName;
    private String image;
    private String content;
    private StatusEnum status;
}
