package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockPageVO;
import co.handk.common.model.vo.StockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存 Service
 */
@Service
@Validated
public interface StockService  extends IService<Stock> {

    /**
     * 新增库存
     */
    Boolean create(@NotNull CreateStockDTO dto);

    /**
     * 根据ID查询库存
     */
    StockVO get(@NotNull Long id);

    /**
     * 修改库存
     */
    Boolean update(@NotNull UpdateStockDTO dto);

    /**
     * 删除库存（逻辑删除）
     */
    Boolean delete(@NotNull Long id);

    /**
     * 分页查询库存
     */
    PageResult<StockPageVO> pageQuery(@NotNull StockQueryDTO dto);

    /**
     * 撤销：按库存流水回滚库存数量为变更前
     */
    Boolean undo(@NotNull Long stockRecordId);

    /**
     * 重做：按库存流水恢复库存数量为变更后
     */
    Boolean redo(@NotNull Long stockRecordId);
}
