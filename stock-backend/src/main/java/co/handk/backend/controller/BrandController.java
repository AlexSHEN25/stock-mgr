package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.BrandService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.common.enums.StatusEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;
    private final SeriesService seriesService;
    private final MakerService makerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateBrandDTO dto) {
        return brandService.saveByDto(dto);
    }

    @PostMapping("/upload")
    public String upload(@RequestPart("file") MultipartFile file) {
        return brandService.uploadImage(file);
    }

    @PutMapping("/{id:\\d+}/upload")
    public String replaceUpload(@PathVariable("id") Long id, @RequestPart("file") MultipartFile file) {
        return brandService.replaceImage(id, file);
    }

    @GetMapping("/{id}")
    public BrandVO get(@PathVariable("id") @NotNull Long id) {
        return brandService.getVOById(id);
    }

    @GetMapping("/form/options")
    public MasterRelationOptionsVO formOptions() {
        MasterRelationOptionsVO vo = new MasterRelationOptionsVO();
        vo.setSeriesOptions(seriesService.list(new QueryWrapper<Series>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")).stream().map(item -> new OptionVO(item.getId(), item.getName())).toList());
        vo.setMakerOptions(makerService.list(new QueryWrapper<Maker>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")).stream().map(item -> new OptionVO(item.getId(), item.getName())).toList());
        return vo;
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateBrandDTO dto) {
        return brandService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return brandService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return brandService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<BrandVO> page(@Valid BrandQueryDTO query) {
        return brandService.page(query);
    }
}

