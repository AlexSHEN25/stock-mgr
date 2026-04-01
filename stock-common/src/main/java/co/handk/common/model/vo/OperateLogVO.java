package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OperateLogVO {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
