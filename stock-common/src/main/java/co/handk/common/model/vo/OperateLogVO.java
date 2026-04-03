package co.handk.common.model.vo;

import lombok.Data;

@Data
public class OperateLogVO extends BaseVO {
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
