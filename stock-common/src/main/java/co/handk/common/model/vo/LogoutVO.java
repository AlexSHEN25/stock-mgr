package co.handk.common.model.vo;

import lombok.Data;

@Data
public class LogoutVO {
    private Boolean success;
    private Long userId;

    public static LogoutVO success(Long userId) {
        LogoutVO vo = new LogoutVO();
        vo.success = true;
        vo.userId = userId;
        return vo;
    }
}
