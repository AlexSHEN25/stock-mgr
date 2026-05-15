package co.handk.backend.controller;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsLevelPrice;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.GoodsSkuSpec;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.service.GoodsLevelPriceService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.GoodsSkuSpecService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsBundleQueryDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.BatchSkuPriceDTO;
import co.handk.common.model.dto.update.BatchSkuStatusDTO;
import co.handk.common.model.dto.update.MemberPriceBatchUpsertDTO;
import co.handk.common.model.dto.update.MemberPriceUpsertItemDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsBundleVO;
import co.handk.common.model.vo.GoodsImageVO;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import co.handk.common.model.vo.GoodsSkuVO;
import co.handk.common.model.vo.GoodsVO;
import co.handk.common.model.vo.GoodsWorkbenchDetailVO;
import co.handk.common.model.vo.OptionVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private GoodsSkuSpecService goodsSkuSpecService;
    @Autowired
    private GoodsImageService goodsImageService;
    @Autowired
    private GoodsLevelPriceService goodsLevelPriceService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsDTO dto) {
        return goodsService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public GoodsVO get(@PathVariable("id") @NotNull Long id) {
        return goodsService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsDTO dto) {
        return goodsService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsVO> page(@Valid GoodsQueryDTO query) {
        return goodsService.page(query);
    }

    @GetMapping("/bundle/page")
    public PageResult<GoodsBundleVO> pageBundle(@Valid GoodsBundleQueryDTO query) {
        return goodsService.pageBundle(query);
    }

    @GetMapping("/workbench/{goodsId}")
    public GoodsWorkbenchDetailVO workbenchDetail(@PathVariable("goodsId") @NotNull Long goodsId) {
        GoodsVO goods = goodsService.getVOById(goodsId);
        List<GoodsSkuVO> skus = goodsSkuService.list(
                new QueryWrapper<GoodsSku>()
                        .eq("goods_id", goodsId)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("id")
        ).stream().map(goodsSku -> {
            GoodsSkuVO vo = new GoodsSkuVO();
            org.springframework.beans.BeanUtils.copyProperties(goodsSku, vo);
            return vo;
        }).collect(Collectors.toList());

        List<Long> skuIds = skus.stream().map(GoodsSkuVO::getId).toList();
        List<GoodsSkuSpecVO> specs = skuIds.isEmpty() ? List.of() : goodsSkuSpecService.list(
                new QueryWrapper<GoodsSkuSpec>()
                        .in("sku_id", skuIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("sort", "id")
        ).stream().map(spec -> {
            GoodsSkuSpecVO vo = new GoodsSkuSpecVO();
            org.springframework.beans.BeanUtils.copyProperties(spec, vo);
            return vo;
        }).collect(Collectors.toList());

        List<GoodsImageVO> images = skuIds.isEmpty() ? List.of() : goodsImageService.list(
                new QueryWrapper<GoodsImage>()
                        .in("sku_id", skuIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("sort", "id")
        ).stream().map(image -> {
            GoodsImageVO vo = new GoodsImageVO();
            org.springframework.beans.BeanUtils.copyProperties(image, vo);
            return vo;
        }).collect(Collectors.toList());

        List<GoodsLevelPriceVO> memberPrices = skuIds.isEmpty() ? List.of() : goodsLevelPriceService.list(
                new QueryWrapper<GoodsLevelPrice>()
                        .in("sku_id", skuIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("level_id", "id")
        ).stream().map(price -> {
            GoodsLevelPriceVO vo = new GoodsLevelPriceVO();
            org.springframework.beans.BeanUtils.copyProperties(price, vo);
            return vo;
        }).collect(Collectors.toList());

        GoodsWorkbenchDetailVO detail = new GoodsWorkbenchDetailVO();
        detail.setGoods(goods);
        detail.setSkus(skus);
        detail.setSpecs(specs);
        detail.setImages(images);
        detail.setMemberPrices(memberPrices);
        return detail;
    }

    @GetMapping("/workbench/options")
    public List<OptionVO> goodsOptions() {
        return goodsService.list(new QueryWrapper<Goods>()
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .eq("status", StatusEnum.NOMAL.getCode())
                        .orderByAsc("name"))
                .stream()
                .map(g -> new OptionVO(g.getId(), g.getName()))
                .collect(Collectors.toList());
    }

    @PutMapping("/workbench/sku/status")
    public Boolean batchUpdateSkuStatus(@RequestBody @NotNull @Valid BatchSkuStatusDTO dto) {
        int updated = goodsSkuService.getBaseMapper().update(
                null,
                new UpdateWrapper<GoodsSku>()
                        .in("id", dto.getSkuIds())
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .set("status", dto.getStatus().getCode())
        );
        return updated > NumberConstant.ZERO;
    }

    @PutMapping("/workbench/sku/price")
    public Boolean batchUpdateSkuPrice(@RequestBody @NotNull @Valid BatchSkuPriceDTO dto) {
        int updated = goodsSkuService.getBaseMapper().update(
                null,
                new UpdateWrapper<GoodsSku>()
                        .in("id", dto.getSkuIds())
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .set("price", dto.getPrice())
                        .set("currency", dto.getCurrency())
                        .set("price_update_time", java.time.LocalDateTime.now())
        );
        return updated > NumberConstant.ZERO;
    }

    @PutMapping("/workbench/member-price")
    public Boolean batchUpsertMemberPrice(@RequestBody @NotNull @Valid MemberPriceBatchUpsertDTO dto) {
        for (MemberPriceUpsertItemDTO item : dto.getItems()) {
            if (item.getId() != null) {
                GoodsLevelPrice entity = new GoodsLevelPrice();
                org.springframework.beans.BeanUtils.copyProperties(item, entity);
                if (entity.getCurrency() == null || entity.getCurrency().isBlank()) {
                    entity.setCurrency(CommonConstant.DEFAULT_CURRENCY_JPY);
                }
                entity.setStatus(StatusEnum.NOMAL.getCode());
                goodsLevelPriceService.updateById(entity);
                continue;
            }
            GoodsLevelPrice entity = new GoodsLevelPrice();
            org.springframework.beans.BeanUtils.copyProperties(item, entity);
            if (entity.getCurrency() == null || entity.getCurrency().isBlank()) {
                entity.setCurrency(CommonConstant.DEFAULT_CURRENCY_JPY);
            }
            entity.setStatus(StatusEnum.NOMAL.getCode());
            goodsLevelPriceService.save(entity);
        }
        return true;
    }
}
