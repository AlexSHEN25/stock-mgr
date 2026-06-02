package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public interface StockService extends BaseService<Stock, StockVO> {

    Long inbound(StockOperateDTO dto);

    Long outbound(StockOperateDTO dto);

    Long submitOrder(StockOrderSubmitDTO dto);

    Boolean approveOrder(Long orderId, Boolean approved, String approveRemark);

    PageResult<StockVO> pageSelfStock(StockQueryDTO query);

    PageResult<StockVO> pageHandleStock(StockQueryDTO query);

    StockVO getSelfStockById(Long id);

    StockVO getHandleStockById(Long id);

    boolean updateSelfStock(UpdateStockDTO dto);

    boolean updateHandleStock(UpdateStockDTO dto);

    int deleteSelfStockById(Long id);

    int deleteHandleStockById(Long id);

    int deleteSelfStockBatch(List<Long> ids);

    int deleteHandleStockBatch(List<Long> ids);
}
