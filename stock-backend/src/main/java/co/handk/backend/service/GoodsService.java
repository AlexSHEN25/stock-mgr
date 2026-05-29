package co.handk.backend.service;

import co.handk.backend.entity.Goods;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsListVO;
import co.handk.common.model.vo.GoodsVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsService extends BaseService<Goods, GoodsVO> {

    Boolean saveGoods(CreateGoodsDTO dto);

    GoodsVO getGoodsById(@NotNull Long id);

    Boolean updateGoods(@NotNull @Valid UpdateGoodsDTO dto);

    int deleteGoodsById(@NotNull Long id);

    PageResult<GoodsListVO> pageGoods(@Valid GoodsQueryDTO query);
}
