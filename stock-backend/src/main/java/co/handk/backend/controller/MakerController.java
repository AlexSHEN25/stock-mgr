package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.MakerService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

