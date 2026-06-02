package co.handk.backend.service;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.vo.RequestCandidateItemVO;
import co.handk.common.model.vo.RequestFormVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RequestFormService extends BaseService<RequestForm, RequestFormVO> {

    Long createFromOutbound(CreateRequestFromOutboundDTO dto);

    Long reapplyInbound(Long requestId);

    java.util.List<RequestCandidateItemVO> listCandidateItems(Long requestId);

    Boolean addItemsFromStockOrder(RequestFormItemBatchDTO dto);

    Boolean matchItemsFromStockOrder(RequestFormItemBatchDTO dto);

    Boolean removeItemsFromRequest(RequestFormItemBatchDTO dto);

    void downloadRequestForm(Long requestId, String format, HttpServletResponse response);

    void downloadBDeptRequestForm(Long requestId, HttpServletResponse response);
}
