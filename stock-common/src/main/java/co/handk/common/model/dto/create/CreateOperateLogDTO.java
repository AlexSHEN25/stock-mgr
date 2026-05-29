package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateOperateLogDTO {

    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestIp;
    private String requestParam;
    private String responseData;
    private String errorMsg;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer costTime;
    private StatusEnum status;

}
