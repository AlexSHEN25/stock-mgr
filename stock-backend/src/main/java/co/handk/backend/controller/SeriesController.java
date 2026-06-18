package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.SeriesService;
import co.handk.backend.service.BrandService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import co.handk.backend.entity.Brand;
import co.handk.common.enums.StatusEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesController {
    private final SeriesService seriesService;
    private final BrandService brandService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateSeriesDTO dto) {
        return seriesService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public SeriesVO get(@PathVariable("id") @NotNull Long id) {
        return seriesService.getVOById(id);
    }

    @GetMapping("/form/options")
    public MasterRelationOptionsVO formOptions() {
        MasterRelationOptionsVO vo = new MasterRelationOptionsVO();
        vo.setBrandOptions(brandService.list(new QueryWrapper<Brand>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")).stream().map(item -> new OptionVO(item.getId(), item.getName())).toList());
        return vo;
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateSeriesDTO dto) {
        return seriesService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return seriesService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return seriesService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<SeriesVO> page(@Valid SeriesQueryDTO query) {
        return seriesService.page(query);
    }
}

