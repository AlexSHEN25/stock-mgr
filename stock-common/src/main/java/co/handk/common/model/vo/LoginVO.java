package co.handk.common.model.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

}