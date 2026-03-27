package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLog extends BaseEntity {

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
