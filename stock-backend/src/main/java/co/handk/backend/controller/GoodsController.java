package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.backend.entity.Brand;
import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.SeriesService;
import co.handk.backend.service.SeriesBrandRelationService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.GoodsImportConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.goods.GoodsBatchUpsertDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsBatchUpsertResultVO;
import co.handk.common.model.vo.GoodsCascadeOptionsVO;
import co.handk.common.model.vo.GoodsFormOptionsVO;
import co.handk.common.model.vo.GoodsListVO;
import co.handk.common.model.vo.GoodsVO;
import co.handk.common.model.vo.OptionVO;
import co.handk.common.model.vo.TextOptionVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsController {
    private static final String IMPORT_FILE_PART = GoodsImportConstant.FILE_PART_NAME;

    private final GoodsService goodsService;
    private final BrandService brandService;
    private final SeriesService seriesService;
    private final CategoryService categoryService;
    private final MakerService makerService;
    private final SeriesBrandRelationService seriesBrandRelationService;
    private final BrandMakerRelationService brandMakerRelationService;
    private final PermissionQueryService permissionQueryService;

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

    @PostMapping("/batch/upsert")
    public GoodsBatchUpsertResultVO batchUpsert(@RequestBody @NotNull @Valid GoodsBatchUpsertDTO dto) {
        validateAdminOnlyImportAccess();
        return goodsService.batchUpsertGoods(dto);
    }

    @PostMapping("/import/upsert")
    public GoodsBatchUpsertResultVO importUpsert(@RequestPart(IMPORT_FILE_PART) MultipartFile file) {
        validateAdminOnlyImportAccess();
        return goodsService.importGoods(file);
    }

    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        validateAdminOnlyImportAccess();
        goodsService.downloadBatchTemplate(response);
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
        return findSeriesOptionsByBrandId(brandId);
    }

    @GetMapping("/options/brand")
    public List<OptionVO> brandOptions(@RequestParam("seriesId") Long seriesId) {
        return findBrandOptionsBySeriesId(seriesId);
    }

    @GetMapping("/options/maker")
    public List<OptionVO> makerOptions(@RequestParam("brandId") Long brandId) {
        return findMakerOptionsByBrandId(brandId);
    }

    @GetMapping("/options/cascade")
    public GoodsCascadeOptionsVO cascadeOptions(@RequestParam(value = "seriesId", required = false) Long seriesId,
                                                @RequestParam(value = "brandId", required = false) Long brandId) {
        GoodsCascadeOptionsVO vo = new GoodsCascadeOptionsVO();
        vo.setBrandOptions(seriesId == null
                ? toOptionList(brandService.list(new QueryWrapper<Brand>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")))
                : findBrandOptionsBySeriesId(seriesId));
        vo.setSeriesOptions(brandId == null
                ? toOptionList(seriesService.list(new QueryWrapper<Series>()
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")))
                : findSeriesOptionsByBrandId(brandId));
        vo.setMakerOptions(brandId == null ? List.of() : findMakerOptionsByBrandId(brandId));
        return vo;
    }

    private List<OptionVO> findSeriesOptionsByBrandId(Long brandId) {
        List<Long> seriesIds = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                        .eq("brand_id", brandId)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("id"))
                .stream()
                .map(SeriesBrandRelation::getSeriesId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (seriesIds.isEmpty()) {
            return List.of();
        }
        return toOptionList(seriesService.list(new QueryWrapper<Series>()
                .in("id", seriesIds)
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> findBrandOptionsBySeriesId(Long seriesId) {
        List<Long> brandIds = seriesBrandRelationService.list(new QueryWrapper<SeriesBrandRelation>()
                        .eq("series_id", seriesId)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("id"))
                .stream()
                .map(SeriesBrandRelation::getBrandId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (brandIds.isEmpty()) {
            return List.of();
        }
        return toOptionList(brandService.list(new QueryWrapper<Brand>()
                .in("id", brandIds)
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> findMakerOptionsByBrandId(Long brandId) {
        List<Long> makerIds = brandMakerRelationService.list(new QueryWrapper<BrandMakerRelation>()
                        .eq("brand_id", brandId)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("id"))
                .stream()
                .map(BrandMakerRelation::getMakerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (makerIds.isEmpty()) {
            return List.of();
        }
        return toOptionList(makerService.list(new QueryWrapper<Maker>()
                .in("id", makerIds)
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

    private void validateAdminOnlyImportAccess() {
        Long userId = UserContext.getUserId();
        if (userId == null || !permissionQueryService.isSuperAdmin(userId)) {
            throw new AccessDeniedException(
                    MessageKeyConstant.ERROR_NO_PERMISSION,
                    SecurityConstant.NO_PERMISSION_MESSAGE
            );
        }
    }

}
