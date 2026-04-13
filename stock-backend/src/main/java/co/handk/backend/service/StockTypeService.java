package co.handk.backend.service;
import co.handk.backend.entity.StockType;
import co.handk.common.model.dto.create.CreateGoodsTypeDTO;
import co.handk.common.model.dto.update.UpdateGoodsTypeDTO;
import co.handk.common.model.vo.GoodsTypeVO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface StockTypeService extends IService<StockType> {
    Boolean create(@NotNull CreateGoodsTypeDTO dto);
    GoodsTypeVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateGoodsTypeDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<GoodsTypeVO> pageQuery(@NotNull GoodsTypeQueryDTO query);
}
