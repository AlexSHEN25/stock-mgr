package co.handk.backend.service;

import co.handk.backend.entity.GoodsSkuSpec;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuSpecDTO;
import co.handk.common.model.dto.query.GoodsSkuSpecQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuSpecDTO;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsSkuSpecService extends IService<GoodsSkuSpec> {
    Boolean create(@NotNull CreateGoodsSkuSpecDTO dto);

    GoodsSkuSpecVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateGoodsSkuSpecDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<GoodsSkuSpecVO> pageQuery(@NotNull GoodsSkuSpecQueryDTO query);
}
