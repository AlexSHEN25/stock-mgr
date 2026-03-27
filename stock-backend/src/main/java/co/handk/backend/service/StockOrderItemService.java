package co.handk.backend.service;

import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.dto.StockOrderItemDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface StockOrderItemService extends IService<StockOrderItem> {

    Boolean create(@NotNull StockOrderItemDTO dto);

    StockOrderItem get(@NotNull Long id);

    Boolean update(@NotNull StockOrderItemDTO dto);

    Boolean delete(@NotNull Long id);

    List<StockOrderItem> listAll();

    PageResult<StockOrderItem> pageQuery(@NotNull PageQuery query);
}
