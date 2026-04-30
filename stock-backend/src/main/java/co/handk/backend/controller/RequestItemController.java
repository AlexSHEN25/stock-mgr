package co.handk.backend.controller;

import co.handk.backend.service.RequestItemService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import co.handk.common.model.vo.RequestItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/requestItem")
public class RequestItemController {
    @Autowired
    private RequestItemService requestItemService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestItemDTO dto) {
        return requestItemService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public RequestItemVO get(@PathVariable("id") @NotNull Long id) {
        return requestItemService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestItemDTO dto) {
        return requestItemService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return requestItemService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<RequestItemVO> page(@Valid RequestItemQueryDTO query) {
        return requestItemService.page(query);
    }
}

