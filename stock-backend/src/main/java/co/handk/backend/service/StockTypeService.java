package co.handk.backend.service;
import co.handk.backend.entity.StockType;
import co.handk.common.model.dto.create.CreateStockTypeDTO;
import co.handk.common.model.dto.update.UpdateStockTypeDTO;
import co.handk.common.model.vo.StockTypeVO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface StockTypeService extends IService<StockType> {
    Boolean create(@NotNull CreateStockTypeDTO dto);
    StockTypeVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateStockTypeDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<StockTypeVO> pageQuery(@NotNull GoodsTypeQueryDTO query);
}
