package co.handk.backend.controller;

import co.handk.backend.service.MakerService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import co.handk.common.model.vo.MakerVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/maker")
@RequiredArgsConstructor
public class MakerController {
    private final MakerService makerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateMakerDTO dto) {
        return makerService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public MakerVO get(@PathVariable("id") @NotNull Long id) {
        return makerService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateMakerDTO dto) {
        return makerService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return makerService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return makerService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<MakerVO> page(@Valid MakerQueryDTO query) {
        return makerService.page(query);
    }
}
