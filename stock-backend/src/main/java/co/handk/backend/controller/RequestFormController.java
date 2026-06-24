package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.RequestFormService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.dto.query.RequestItemCartQueryDTO;
import co.handk.common.model.dto.update.RequestCartMoveDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.dto.update.RequestFormWithItemsDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/requestForm")
public class RequestFormController {
    private final RequestFormService requestFormService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestFormDTO dto) {
        return requestFormService.saveByDto(dto);
    }

    @PostMapping("/withItems")
    public Long createWithItems(@RequestBody @NotNull @Valid RequestFormWithItemsDTO dto) {
        return requestFormService.saveWithItems(dto);
    }

    @PostMapping("/fromOutbound")
    public Long createFromOutbound(@RequestBody @NotNull @Valid CreateRequestFromOutboundDTO dto) {
        return requestFormService.createFromOutbound(dto);
    }

    @PostMapping("/reapplyInbound/{id}")
    public Long reapplyInbound(@PathVariable("id") @NotNull Long id) {
        return requestFormService.reapplyInbound(id);
    }

    @PostMapping("/items/reapplyInbound")
    public Long reapplyInboundItem(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.reapplyInboundItem(dto);
    }

    @GetMapping("/{id}/candidateItems")
    public List<RequestCandidateItemVO> candidateItems(@PathVariable("id") @NotNull Long id) {
        return requestFormService.listCandidateItems(id);
    }

    @GetMapping("/items/cart/page")
    public PageResult<RequestCandidateItemVO> cartItems(@Valid RequestItemCartQueryDTO query) {
        return requestFormService.pageCartItems(query);
    }

    @PostMapping("/items/cart/add")
    public Boolean addCartItems(@RequestBody @NotNull @Valid RequestCartMoveDTO dto) {
        return requestFormService.addItemsToCart(dto);
    }

    @PostMapping("/items/cart/remove")
    public Boolean removeCartItems(@RequestBody @NotNull @Valid RequestCartMoveDTO dto) {
        return requestFormService.removeItemsFromCart(dto);
    }

    @PostMapping("/items/add")
    public Boolean addItems(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.addItemsFromStockOrder(dto);
    }

    @PostMapping("/items/match")
    public Boolean matchItems(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.matchItemsFromStockOrder(dto);
    }

    @PostMapping("/items/remove")
    public Boolean removeItems(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.removeItemsFromRequest(dto);
    }

    @GetMapping("/{id}")
    public RequestFormVO get(@PathVariable("id") @NotNull Long id) {
        return requestFormService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestFormDTO dto) {
        return requestFormService.updateByDto(dto);
    }

    @PutMapping("/withItems")
    public Boolean updateWithItems(@RequestBody @NotNull @Valid RequestFormWithItemsDTO dto) {
        return requestFormService.updateWithItems(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return requestFormService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return requestFormService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<RequestFormVO> page(@Valid RequestFormQueryDTO query) {
        return requestFormService.page(query);
    }

    @GetMapping({"/{id}/download", "/download/{id}"})
    public void download(@PathVariable("id") @NotNull Long id,
                         @RequestParam(value = "format", defaultValue = "excel") String format,
                         HttpServletResponse response) {
        requestFormService.downloadRequestForm(id, format, response);
    }

    @GetMapping({"/{id}/pdf", "/pdf/{id}"})
    public void downloadPdf(@PathVariable("id") @NotNull Long id, HttpServletResponse response) {
        requestFormService.downloadRequestForm(id, "pdf", response);
    }
}

