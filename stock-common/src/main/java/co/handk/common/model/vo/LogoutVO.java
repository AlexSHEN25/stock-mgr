package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class LogoutVO extends BaseVO {
    @SchemaField(label = "\u6210\u529f", order = 50)
    private Boolean success;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;

    public static LogoutVO success(Long userId) {
        LogoutVO vo = new LogoutVO();
        vo.success = true;
        vo.userId = userId;
        return vo;
    }
}
