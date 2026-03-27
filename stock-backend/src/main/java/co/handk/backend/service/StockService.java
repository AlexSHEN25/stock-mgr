package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.dto.StockDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 库存 Service
 */
@Service
@Validated
public interface StockService  extends IService<Stock> {

    /**
     * 新增库存
     */
    Boolean create(@NotNull StockDTO dto);

    /**
     * 根据ID查询库存
     */
    Stock get(@NotNull Long id);

    /**
     * 修改库存
     */
    Boolean update(@NotNull StockDTO dto);

    /**
     * 删除库存（逻辑删除）
     */
    Boolean delete(@NotNull Long id);

    /**
     * 查询全部库存
     */
    List<Stock> listAll();

    /**
     * 分页查询库存
     */
    PageResult<StockPageVO> pageQuery(@NotNull StockPageQueryDTO dto);
}
