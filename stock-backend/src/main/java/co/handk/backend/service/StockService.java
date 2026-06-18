package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.query.CustomerStockQueryDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.CustomerGoodsStockDetailVO;
import co.handk.common.model.vo.CustomerGoodsStockVO;
import co.handk.common.model.vo.CustomerGoodsMatrixVO;
import co.handk.common.model.vo.CustomerOutboundTreeNodeVO;
import co.handk.common.model.vo.CustomerStockSummaryVO;
import co.handk.common.model.vo.StockVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockService extends BaseService<Stock, StockVO> {

    Long inbound(StockOperateDTO dto);

    Long outbound(StockOperateDTO dto);

    java.util.List<Long> allocateToGroups(StockGroupAllocateDTO dto);

    Long submitOrder(StockOrderSubmitDTO dto);

    Boolean approveOrder(Long orderId, Boolean approved, String approveRemark);

    Integer getGroupAvailableQty(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId,
                                 Long deptId, String groupCode);

    void exportSelfStock(StockQueryDTO query, HttpServletResponse response);

    PageResult<CustomerStockSummaryVO> pageCustomerStock(CustomerStockQueryDTO query);

    PageResult<CustomerGoodsStockVO> pageCustomerGoodsStock(CustomerStockQueryDTO query);

    PageResult<CustomerGoodsStockDetailVO> pageCustomerGoodsStockDetails(CustomerStockQueryDTO query);

    CustomerGoodsMatrixVO getCustomerGoodsMatrix(CustomerStockQueryDTO query);

    PageResult<CustomerOutboundTreeNodeVO> pageCustomerGoodsTree(CustomerStockQueryDTO query);

}
