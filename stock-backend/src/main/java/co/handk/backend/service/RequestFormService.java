package co.handk.backend.service;

import co.handk.backend.entity.RequestForm;
import co.handk.backend.entity.StockOrder;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.query.RequestItemCartQueryDTO;
import co.handk.common.model.dto.update.RequestCartMoveDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.dto.update.RequestFormWithItemsDTO;
import co.handk.common.model.vo.RequestCandidateItemVO;
import co.handk.common.model.vo.RequestFormCartPreviewVO;
import co.handk.common.model.vo.RequestFormVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RequestFormService extends BaseService<RequestForm, RequestFormVO> {

    Long createFromOutbound(CreateRequestFromOutboundDTO dto);

    Long createFromOutbound(StockOrder outboundOrder, java.util.List<Long> stockOrderItemIds, String remark);

    Long saveWithItems(RequestFormWithItemsDTO dto);

    Boolean updateWithItems(RequestFormWithItemsDTO dto);

    Long reapplyInbound(Long requestId);

    Long reapplyInboundItem(RequestFormItemBatchDTO dto);

    java.util.List<RequestCandidateItemVO> listCandidateItems(Long requestId);

    PageResult<RequestCandidateItemVO> pageCartItems(RequestItemCartQueryDTO query);

    RequestFormCartPreviewVO previewCartItems(RequestItemCartQueryDTO query);

    Boolean addItemsToCart(RequestCartMoveDTO dto);

    Boolean removeItemsFromCart(RequestCartMoveDTO dto);

    Boolean addItemsFromStockOrder(RequestFormItemBatchDTO dto);

    Boolean matchItemsFromStockOrder(RequestFormItemBatchDTO dto);

    Boolean removeItemsFromRequest(RequestFormItemBatchDTO dto);

    void downloadRequestForm(Long requestId, String format, HttpServletResponse response);

    void downloadBDeptRequestForm(Long requestId, HttpServletResponse response);
}
