package co.handk.backend.service;

import co.handk.backend.entity.Goods;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsService extends IService<Goods> {
    Boolean create(@NotNull CreateGoodsDTO dto);
    GoodsVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateGoodsDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<GoodsVO> pageQuery(@NotNull GoodsQueryDTO query);
}
