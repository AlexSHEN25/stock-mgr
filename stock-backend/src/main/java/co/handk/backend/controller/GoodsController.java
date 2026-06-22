package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.SeriesService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.GoodsImportConstant;
import co.handk.common.constant.NumberConstant;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    @GetMapping("/export")
    public void export(@ModelAttribute GoodsQueryDTO query, HttpServletResponse response) {
        goodsService.exportGoods(query, response);
    }

    @PostMapping("/batch/upsert")
    public GoodsBatchUpsertResultVO batchUpsert(@RequestBody @NotNull @Valid GoodsBatchUpsertDTO dto) {
        validateAdminOnlyImportAccess();
        return goodsService.batchUpsertGoods(dto);
    }

    @PostMapping("/import/upsert")
    public GoodsBatchUpsertResultVO importUpsert(@RequestPart(IMPORT_FILE_PART) MultipartFile file,
                                                 @ModelAttribute GoodsQueryDTO query) {
        validateAdminOnlyImportAccess();
        return goodsService.importGoods(file, query);
    }

    @GetMapping("/import/template")
    public void downloadImportTemplate(@ModelAttribute GoodsQueryDTO query,
                                       HttpServletResponse response) {
        validateAdminOnlyImportAccess();
        goodsService.downloadBatchTemplate(query, response);
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
    public List<OptionVO> makerOptions(@RequestParam("brandId") Long brandId,
                                       @RequestParam(value = "seriesId", required = false) Long seriesId) {
        return findMakerOptions(brandId, seriesId);
    }

    @GetMapping("/options/cascade")
    public GoodsCascadeOptionsVO cascadeOptions(@RequestParam(value = "seriesId", required = false) Long seriesId,
                                                @RequestParam(value = "brandId", required = false) Long brandId,
                                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                @RequestParam(value = "makerId", required = false) Long makerId) {
        GoodsCascadeOptionsVO vo = new GoodsCascadeOptionsVO();
        vo.setBrandOptions(findBrandCascadeOptions(seriesId, categoryId, makerId));
        vo.setSeriesOptions(findSeriesCascadeOptions(brandId, categoryId, makerId));
        vo.setCategoryOptions(findCategoryCascadeOptions(brandId, seriesId, makerId));
        vo.setMakerOptions(findMakerCascadeOptions(brandId, seriesId, categoryId));
        return vo;
    }

    private List<OptionVO> findBrandCascadeOptions(Long seriesId, Long categoryId, Long makerId) {
        List<Long> brandIds = distinctGoodsRelationIds(null, seriesId, categoryId, makerId)
                .stream()
                .map(Goods::getBrandId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return loadBrandOptions(brandIds);
    }

    private List<OptionVO> findSeriesCascadeOptions(Long brandId, Long categoryId, Long makerId) {
        List<Long> seriesIds = distinctGoodsRelationIds(brandId, null, categoryId, makerId)
                .stream()
                .map(Goods::getSeriesId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return loadSeriesOptions(seriesIds);
    }

    private List<OptionVO> findCategoryCascadeOptions(Long brandId, Long seriesId, Long makerId) {
        List<Long> categoryIds = distinctGoodsRelationIds(brandId, seriesId, null, makerId)
                .stream()
                .map(Goods::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return loadCategoryOptions(categoryIds);
    }

    private List<OptionVO> findMakerCascadeOptions(Long brandId, Long seriesId, Long categoryId) {
        List<Long> makerIds = distinctGoodsRelationIds(brandId, seriesId, categoryId, null)
                .stream()
                .map(Goods::getMakerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return loadMakerOptions(makerIds);
    }

    private List<Goods> distinctGoodsRelationIds(Long brandId, Long seriesId, Long categoryId, Long makerId) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<Goods>()
                .select("brand_id", "series_id", "category_id", "maker_id");
        if (brandId != null) {
            wrapper.eq("brand_id", brandId);
        }
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (makerId != null) {
            wrapper.eq("maker_id", makerId);
        }
        return goodsService.list(wrapper);
    }

    private List<OptionVO> loadBrandOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toOptionList(brandService.list(new QueryWrapper<Brand>()
                .in("id", deduplicateIds(ids))
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> loadSeriesOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toOptionList(seriesService.list(new QueryWrapper<Series>()
                .in("id", deduplicateIds(ids))
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> loadCategoryOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toOptionList(categoryService.list(new QueryWrapper<Category>()
                .in("id", deduplicateIds(ids))
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> loadMakerOptions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toOptionList(makerService.list(new QueryWrapper<Maker>()
                .in("id", deduplicateIds(ids))
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<Long> deduplicateIds(List<Long> ids) {
        Set<Long> ordered = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                ordered.add(id);
            }
        }
        return ordered.stream().toList();
    }

    private List<OptionVO> findSeriesOptionsByBrandId(Long brandId) {
        return toOptionList(seriesService.list(new QueryWrapper<Series>()
                .eq("brand_id", brandId)
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> findBrandOptionsBySeriesId(Long seriesId) {
        Series series = seriesService.getByIdNotDeleted(seriesId);
        if (series == null || series.getBrandId() == null) {
            return List.of();
        }
        return toOptionList(brandService.list(new QueryWrapper<Brand>()
                .eq("id", series.getBrandId())
                .eq("status", StatusEnum.NOMAL.getCode())
                .orderByAsc("id")));
    }

    private List<OptionVO> findMakerOptions(Long brandId, Long seriesId) {
        if (seriesId != null) {
            return toOptionList(makerService.list(new QueryWrapper<Maker>()
                    .eq("series_id", seriesId)
                    .eq("status", StatusEnum.NOMAL.getCode())
                    .orderByAsc("id")));
        }
        List<Long> seriesIds = seriesService.list(new QueryWrapper<Series>()
                        .eq("brand_id", brandId)
                        .eq("status", StatusEnum.NOMAL.getCode())
                        .orderByAsc("id"))
                .stream()
                .map(Series::getId)
                .toList();
        if (seriesIds.isEmpty()) {
            return List.of();
        }
        return toOptionList(makerService.list(new QueryWrapper<Maker>()
                .in("series_id", seriesIds)
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
