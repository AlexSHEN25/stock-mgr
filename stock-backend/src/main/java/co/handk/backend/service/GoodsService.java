package co.handk.backend.service;

import co.handk.backend.entity.Goods;
import co.handk.common.model.dto.GoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface GoodsService extends IService<Goods> {

    Boolean create(@NotNull GoodsDTO dto);

    GoodsVO get(@NotNull Long id);

    Boolean update(@NotNull GoodsDTO dto);

    Boolean delete(@NotNull Long id);
    List<GoodsVO> listAll();

    PageResult<GoodsVO> pageQuery(@NotNull PageQuery query);
}
