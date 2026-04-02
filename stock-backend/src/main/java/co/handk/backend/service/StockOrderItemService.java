package co.handk.backend.service;
import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.dto.create.CreateStockOrderItemDTO;
import co.handk.common.model.dto.update.UpdateStockOrderItemDTO;
import co.handk.common.model.vo.StockOrderItemVO;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface StockOrderItemService extends IService<StockOrderItem> {
    Boolean create(@NotNull CreateStockOrderItemDTO dto);
    StockOrderItemVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateStockOrderItemDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<StockOrderItemVO> pageQuery(@NotNull StockOrderItemQueryDTO query);
}
