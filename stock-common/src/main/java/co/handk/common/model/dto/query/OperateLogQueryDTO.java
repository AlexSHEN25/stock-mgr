package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class OperateLogQueryDTO extends PageQuery {

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
    private StatusEnum status;
    private String errorMsg;
    private Integer costTime;
}
