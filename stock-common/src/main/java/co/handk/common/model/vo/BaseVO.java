package co.handk.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseVO implements Serializable {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
