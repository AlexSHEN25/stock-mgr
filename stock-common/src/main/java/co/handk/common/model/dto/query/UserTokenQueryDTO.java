package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import java.time.LocalDateTime;
import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class UserTokenQueryDTO extends PageQuery {

    private Long id;

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private StatusEnum status;
}
