package co.handk.common.model.dto;

import lombok.Data;

@Data
public class OperateLogDTO {

    private Long id;

    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestIp;
    private String requestParam;
    private String responseData;
    private Integer status;
    private String errorMsg;
    private Integer costTime;
}
