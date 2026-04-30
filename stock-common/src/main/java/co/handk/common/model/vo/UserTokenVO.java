package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTokenVO extends BaseVO {
    @SchemaField(label = "\u30c8\u30fc\u30af\u30f3", order = 50)
    private String token;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;
    @SchemaField(label = "\u30ed\u30b0\u30a4\u30f3\u6642\u523b", order = 50)
    private LocalDateTime loginTime;
    @SchemaField(label = "\u6709\u52b9\u671f\u9650", order = 50)
    private LocalDateTime expireTime;
    @SchemaField(label = "loginIp", order = 50)
    private String loginIp;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
