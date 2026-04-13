package co.handk.backend.service;

import co.handk.backend.entity.GoodsSku;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.dto.query.GoodsSkuQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsSkuService extends IService<GoodsSku> {
    Boolean create(@NotNull CreateGoodsSkuDTO dto);

    GoodsSkuVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateGoodsSkuDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<GoodsSkuVO> pageQuery(@NotNull GoodsSkuQueryDTO query);
}
