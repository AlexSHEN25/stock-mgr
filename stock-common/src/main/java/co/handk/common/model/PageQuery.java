package co.handk.common.model;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 通用分页请求基类
 */
@Data
public class PageQuery {

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 50, message = "每页大小最大为50")
    private Long pageSize = 10L;
}
