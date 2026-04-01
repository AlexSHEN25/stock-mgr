package co.handk.backend.service;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.dto.StockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface StockOrderService extends IService<StockOrder> {

    Boolean create(@NotNull StockOrderDTO dto);

    StockOrderVO get(@NotNull Long id);

    Boolean update(@NotNull StockOrderDTO dto);

    Boolean delete(@NotNull Long id);
    List<StockOrderVO> listAll();

    PageResult<StockOrderVO> pageQuery(@NotNull PageQuery query);
}
