package co.handk.backend.service;

import co.handk.backend.entity.GoodsLevelPrice;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsLevelPriceDTO;
import co.handk.common.model.dto.query.GoodsLevelPriceQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsLevelPriceDTO;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsLevelPriceService extends IService<GoodsLevelPrice> {
    Boolean create(@NotNull CreateGoodsLevelPriceDTO dto);

    GoodsLevelPriceVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateGoodsLevelPriceDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<GoodsLevelPriceVO> pageQuery(@NotNull GoodsLevelPriceQueryDTO query);
}
