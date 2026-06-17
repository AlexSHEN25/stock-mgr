package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsFormOptionsVO;
import co.handk.common.model.vo.GoodsListVO;
import co.handk.common.model.vo.GoodsVO;
import co.handk.common.model.vo.OptionVO;
import co.handk.common.model.vo.TextOptionVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsController {
    private final GoodsService goodsService;
    private final BrandService brandService;
    private final SeriesService seriesService;
    private final CategoryService categoryService;
    private final MakerService makerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsDTO dto) {
        return goodsService.saveGoods(dto);
    }

    @GetMapping("/{id}")
    public GoodsVO get(@PathVariable("id") @NotNull Long id) {
        return goodsService.getGoodsById(id);
    }

    @GetMapping("/{id}/detail")
    public GoodsVO detail(@PathVariable("id") @NotNull Long id) {
        return goodsService.getGoodsById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsDTO dto) {
        return goodsService.updateGoods(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsService.deleteGoodsById(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return goodsService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsListVO> page(@Valid GoodsQueryDTO query) {
        return goodsService.pageGoods(query);
    }

    @GetMapping("/form/options")
    public GoodsFormOptionsVO formOptions() {
        GoodsFormOptionsVO vo = new GoodsFormOptionsVO();
        vo.setBrandOptions(toOptionList(brandService.list(new QueryWrapper<Brand>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id"))));
        vo.setSeriesOptions(toOptionList(seriesService.list(new QueryWrapper<Series>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id"))));
        vo.setCategoryOptions(toOptionList(categoryService.list(new QueryWrapper<Category>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id"))));
        vo.setMakerOptions(toOptionList(makerService.list(new QueryWrapper<Maker>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id"))));
        vo.setStatusOptions(List.of(
                new TextOptionVO(String.valueOf(StatusEnum.NOMAL.getCode()), "有効"),
                new TextOptionVO(String.valueOf(StatusEnum.FOBBIDEN.getCode()), "無効")
        ));
        vo.setSkuStatusOptions(List.of(
                new TextOptionVO(String.valueOf(StatusEnum.NOMAL.getCode()), "有効"),
                new TextOptionVO(String.valueOf(StatusEnum.FOBBIDEN.getCode()), "無効")
        ));
        vo.setCurrencyOptions(List.of(new TextOptionVO(CommonConstant.DEFAULT_CURRENCY_JPY, CommonConstant.DEFAULT_CURRENCY_JPY)));
        return vo;
    }

    @GetMapping("/options/series")
    public List<OptionVO> seriesOptions(@RequestParam("brandId") Long brandId) {
        return toOptionList(seriesService.list(new QueryWrapper<Series>()
                .eq("brand_id", brandId)
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    @GetMapping("/options/brand")
    public List<OptionVO> brandOptions(@RequestParam("seriesId") Long seriesId) {
        Series series = seriesService.getByIdNotDeleted(seriesId);
        if (series == null || series.getBrandId() == null) {
            return List.of();
        }
        Brand brand = brandService.getByIdNotDeleted(series.getBrandId());
        if (brand == null || !StatusEnum.NOMAL.getCode().equals(brand.getStatus())) {
            return List.of();
        }
        return List.of(new OptionVO(brand.getId(), brand.getName()));
    }

    @GetMapping("/options/maker")
    public List<OptionVO> makerOptions(@RequestParam("brandId") Long brandId) {
        return toOptionList(makerService.list(new QueryWrapper<Maker>()
                .inSql("id", "SELECT maker_id FROM t_brand_maker_relation"
                        + " WHERE deleted = " + DeleteEnum.UNDELETED.getCode() + " AND brand_id = " + brandId)
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private <T> List<OptionVO> toOptionList(List<T> entities) {
        return entities.stream().map(entity -> {
            if (entity instanceof Brand brand) {
                return new OptionVO(brand.getId(), brand.getName());
            }
            if (entity instanceof Series series) {
                return new OptionVO(series.getId(), series.getName());
            }
            if (entity instanceof Category category) {
                return new OptionVO(category.getId(), category.getName());
            }
            if (entity instanceof Maker maker) {
                return new OptionVO(maker.getId(), maker.getName());
            }
            return null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

}
