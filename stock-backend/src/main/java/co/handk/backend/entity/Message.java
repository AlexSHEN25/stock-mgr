package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "message", name = "通知メッセージ", group = "システム管理/システム設定管理")
public class Message extends BaseEntity {

    @SchemaField(title = "类型:1=上新商品,2=新闻资讯,3=产品册")
    private Integer type;

    @SchemaField(title = "用户ID")
    private Long userId;

    @SchemaField(title = "消息")
    private String message;

    @SchemaField(title = "信息源ID")
    private Integer sourceId;

    @SchemaField(title = "是否已读:0=否,1=是")
    private Integer isRead;

    @SchemaField(title = "发送状态:0待发送1已发送2失败")
    private Integer state;
}