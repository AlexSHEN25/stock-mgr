package co.handk.backend.service;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.query.StockOrderQueryDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockOrderService extends IService<StockOrder> {
    Boolean create(@NotNull CreateStockOrderDTO dto);
    StockOrderVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateStockOrderDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<StockOrderVO> pageQuery(@NotNull StockOrderQueryDTO query);
}
