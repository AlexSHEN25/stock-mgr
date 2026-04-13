package co.handk.backend.service;

import co.handk.backend.entity.GoodsImage;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsImageDTO;
import co.handk.common.model.dto.query.GoodsImageQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsImageDTO;
import co.handk.common.model.vo.GoodsImageVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsImageService extends IService<GoodsImage> {
    Boolean create(@NotNull CreateGoodsImageDTO dto);

    GoodsImageVO get(@NotNull Long id);

    Boolean update(@NotNull UpdateGoodsImageDTO dto);

    Boolean delete(@NotNull Long id);

    PageResult<GoodsImageVO> pageQuery(@NotNull GoodsImageQueryDTO query);
}
