package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.GoodsImageService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateGoodsImageDTO;
import co.handk.common.model.dto.query.GoodsImageQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsImageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/goodsImage")
@RequiredArgsConstructor
public class GoodsImageController {
    private final GoodsImageService goodsImageService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsImageDTO dto) {
        return goodsImageService.saveByDto(dto);
    }

    @PostMapping("/upload")
    public String upload(@RequestPart("file") MultipartFile file) {
        return goodsImageService.uploadImage(file);
    }

    @PutMapping("/{id:\\d+}/upload")
    public String replaceUpload(@PathVariable("id") Long id, @RequestPart("file") MultipartFile file) {
        return goodsImageService.replaceImage(id, file);
    }

    @GetMapping("/{id}")
    public GoodsImageVO get(@PathVariable("id") @NotNull Long id) {
        return goodsImageService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsImageDTO dto) {
        return goodsImageService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsImageService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return goodsImageService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsImageVO> page(@Valid GoodsImageQueryDTO query) {
        return goodsImageService.page(query);
    }
}

