package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.vo.StockVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockService extends BaseService<Stock, StockVO> {

    Long inbound(StockOperateDTO dto);

    Long outbound(StockOperateDTO dto);

    Long submitOrder(StockOrderSubmitDTO dto);

    Boolean approveOrder(Long orderId, Boolean approved, String approveRemark);

    Integer getMyGroupAvailableQty(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId);

    PageResult<StockVO> pageSelfStock(StockQueryDTO query);

    PageResult<StockVO> pageGroupStock(StockQueryDTO query, Long deptId);

}
