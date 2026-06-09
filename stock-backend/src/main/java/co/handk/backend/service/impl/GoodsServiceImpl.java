package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.mapper.SeriesBrandRelationMapper;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.service.FileStorageService;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.SeriesBrandRelationService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsListVO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

    private final GoodsSkuService goodsSkuService;
    private final GoodsImageService goodsImageService;
    private final FileStorageService fileStorageService;
    private final BrandMakerRelationService brandMakerRelationService;
    private final SeriesBrandRelationService seriesBrandRelationService;
    private final BrandMakerRelationMapper brandMakerRelationMapper;
    private final SeriesBrandRelationMapper seriesBrandRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveGoods(CreateGoodsDTO dto) {
        validatePriceUpdateFields(dto.getUpdatePrice(), dto.getPriceUpdateTime());
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        if (!StringUtils.hasText(goods.getName())) {
            goods.setName(StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : dto.getSkuCode());
        }
        if (!StringUtils.hasText(goods.getEnglishName())) {
            goods.setEnglishName(goods.getName());
        }
        goods.setStatus(dto.getStatus() == null ? StatusEnum.NOMAL.getCode() : dto.getStatus().getCode());
        boolean goodsSaved = this.save(goods);
        if (!goodsSaved) {
            return false;
        }
        syncCascadingRelations(goods.getBrandId(), goods.getSeriesId(), goods.getMakerId());

        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goods.getId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setSkuName(StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : goods.getName());
        sku.setPrice(dto.getPrice());
        sku.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : CommonConstant.DEFAULT_CURRENCY_JPY);
        sku.setCostPrice(dto.getCostPrice());
        sku.setUpdatePrice(dto.getUpdatePrice());
        sku.setPriceUpdateTime(dto.getPriceUpdateTime());
        sku.setBarcode(StringUtils.hasText(dto.getBarcode()) ? dto.getBarcode() : generateBarcode());
        sku.setWeight(dto.getWeight());
        sku.setVolume(dto.getVolume());
        sku.setStatus(dto.getSkuStatus() == null ? StatusEnum.NOMAL.getCode() : dto.getSkuStatus().getCode());
        boolean skuSaved = goodsSkuService.save(sku);
        if (!skuSaved) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUの保存に失敗しました");
        }

        if (StringUtils.hasText(dto.getImageUrl())) {
            String normalizedImageUrl = fileStorageService.normalize(UploadBizType.GOODS, dto.getImageUrl());
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goods.getId());
            image.setSkuId(sku.getId());
            image.setSkuCode(sku.getSkuCode());
            image.setImageUrl(normalizedImageUrl);
            image.setSort(dto.getImageSort() == null ? NumberConstant.ZERO : dto.getImageSort());
            boolean imageSaved = goodsImageService.save(image);
            if (!imageSaved) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の保存に失敗しました");
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsVO getGoodsById(Long id) {
        Goods goods = this.getByIdNotDeleted(id);
        if (goods == null) {
            return null;
        }
        GoodsVO vo = toVO(goods);
        GoodsSku sku = goodsSkuService.getOne(new QueryWrapper<GoodsSku>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .orderByDesc("update_time")
                .last("LIMIT 1"));
        if (sku != null) {
            vo.setSkuCode(sku.getSkuCode());
            vo.setSkuName(sku.getSkuName());
            vo.setPrice(sku.getPrice());
            vo.setCurrency(sku.getCurrency());
            vo.setCostPrice(sku.getCostPrice());
            vo.setUpdatePrice(sku.getUpdatePrice());
            vo.setPriceUpdateTime(sku.getPriceUpdateTime());
            vo.setBarcode(sku.getBarcode());
            vo.setWeight(sku.getWeight());
            vo.setVolume(sku.getVolume());
        }
        GoodsImage image = goodsImageService.getOne(new QueryWrapper<GoodsImage>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .orderByAsc("sort", "id")
                .last("LIMIT 1"));
        if (image != null) {
            vo.setImageId(image.getId());
            vo.setImageUrl(fileStorageService.toApiPath(UploadBizType.GOODS, image.getImageUrl()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGoods(UpdateGoodsDTO dto) {
        validatePriceUpdateFields(dto.getUpdatePrice(), dto.getPriceUpdateTime());
        Goods existed = this.getByIdNotDeleted(dto.getId());
        if (existed == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品不存在");
        }
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setStatus(dto.getStatus() == null ? null : dto.getStatus().getCode());
        boolean goodsUpdated = super.updateByDto(goods);
        if (!goodsUpdated) {
            return false;
        }
        syncCascadingRelations(goods.getBrandId(), goods.getSeriesId(), goods.getMakerId());
        cleanupCascadingRelations(existed.getBrandId(), existed.getSeriesId(), existed.getMakerId());

        String skuNameToSave = StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : dto.getName();
        Integer skuStatusCode = dto.getSkuStatus() == null ? null : dto.getSkuStatus().getCode();
        UpdateWrapper<GoodsSku> skuWrapper = new UpdateWrapper<GoodsSku>()
                .eq("goods_id", dto.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode())
                .set(StringUtils.hasText(skuNameToSave), "sku_name", skuNameToSave)
                .set(dto.getPrice() != null, "price", dto.getPrice())
                .set(StringUtils.hasText(dto.getCurrency()), "currency", dto.getCurrency())
                .set(dto.getCostPrice() != null, "cost_price", dto.getCostPrice())
                .set(dto.getUpdatePrice() != null, "update_price", dto.getUpdatePrice())
                .set(dto.getPriceUpdateTime() != null, "price_update_time", dto.getPriceUpdateTime())
                .set(StringUtils.hasText(dto.getBarcode()), "barcode", dto.getBarcode())
                .set(dto.getWeight() != null, "weight", dto.getWeight())
                .set(dto.getVolume() != null, "volume", dto.getVolume())
                .set(skuStatusCode != null, "status", skuStatusCode);
        if (dto.getSkuId() != null) {
            skuWrapper.eq("id", dto.getSkuId());
        }
        boolean skuUpdated = goodsSkuService.update(null, skuWrapper);
        if (!skuUpdated) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUの更新に失敗しました");
        }

        if (StringUtils.hasText(dto.getImageUrl()) || dto.getImageSort() != null || dto.getImageId() != null) {
            String normalizedImageUrl = fileStorageService.normalize(UploadBizType.GOODS, dto.getImageUrl());
            UpdateWrapper<GoodsImage> imageWrapper = new UpdateWrapper<GoodsImage>()
                    .eq("goods_id", dto.getId())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set(StringUtils.hasText(normalizedImageUrl), "image_url", normalizedImageUrl)
                    .set(dto.getImageSort() != null, "sort", dto.getImageSort())
                    .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode());
            if (dto.getImageId() != null) {
                imageWrapper.eq("id", dto.getImageId());
            }
            boolean imageUpdated = goodsImageService.update(null, imageWrapper);
            if (!imageUpdated) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の更新に失敗しました");
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGoodsById(Long id) {
        Goods existed = this.getByIdNotDeleted(id);
        if (existed == null) {
            return 0;
        }
        int goodsRows = super.deleteByIdLogic(id);
        goodsSkuService.update(null, new UpdateWrapper<GoodsSku>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
        goodsImageService.update(null, new UpdateWrapper<GoodsImage>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
        cleanupCascadingRelations(existed.getBrandId(), existed.getSeriesId(), existed.getMakerId());
        return goodsRows;
    }

    private void syncCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        if (brandId != null && seriesId != null) {
            SeriesBrandRelation seriesRelation = seriesBrandRelationService.getOne(new QueryWrapper<SeriesBrandRelation>()
                    .eq("brand_id", brandId)
                    .eq("series_id", seriesId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (seriesRelation == null) {
                seriesRelation = new SeriesBrandRelation();
                seriesRelation.setBrandId(brandId);
                seriesRelation.setSeriesId(seriesId);
                seriesBrandRelationService.save(seriesRelation);
            }
        }
        if (brandId != null && makerId != null) {
            BrandMakerRelation makerRelation = brandMakerRelationService.getOne(new QueryWrapper<BrandMakerRelation>()
                    .eq("brand_id", brandId)
                    .eq("maker_id", makerId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (makerRelation == null) {
                makerRelation = new BrandMakerRelation();
                makerRelation.setBrandId(brandId);
                makerRelation.setMakerId(makerId);
                brandMakerRelationService.save(makerRelation);
            }
        }
    }

    private void cleanupCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        if (brandId != null && seriesId != null && !existsGoodsWithSeriesBrand(brandId, seriesId)) {
            seriesBrandRelationMapper.update(null, new UpdateWrapper<SeriesBrandRelation>()
                    .eq("brand_id", brandId)
                    .eq("series_id", seriesId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set("deleted", DeleteEnum.DELETED.getCode()));
        }
        if (brandId != null && makerId != null && !existsGoodsWithBrandMaker(brandId, makerId)) {
            brandMakerRelationMapper.update(null, new UpdateWrapper<BrandMakerRelation>()
                    .eq("brand_id", brandId)
                    .eq("maker_id", makerId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set("deleted", DeleteEnum.DELETED.getCode()));
        }
    }

    private boolean existsGoodsWithSeriesBrand(Long brandId, Long seriesId) {
        return this.count(new QueryWrapper<Goods>()
                .eq("brand_id", brandId)
                .eq("series_id", seriesId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())) > 0;
    }

    private boolean existsGoodsWithBrandMaker(Long brandId, Long makerId) {
        return this.count(new QueryWrapper<Goods>()
                .eq("brand_id", brandId)
                .eq("maker_id", makerId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())) > 0;
    }

    private void validatePriceUpdateFields(java.math.BigDecimal updatePrice, LocalDateTime priceUpdateTime) {
        if (updatePrice != null && priceUpdateTime == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "updatePrice入力時はpriceUpdateTimeが必須です"
            );
        }
    }

    private String generateBarcode() {
        String ts = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "BC" + ts + random;
    }


    @Override
    public PageResult<GoodsListVO> pageGoods(GoodsQueryDTO queryDTO) {
        Long total = baseMapper.countGoodsPage(queryDTO);
        if (total == null || total <= 0) {
            return PageResult.build(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), Collections.emptyList());
        }
        long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<GoodsListVO> records = baseMapper.selectGoodsPage(queryDTO, offset, queryDTO.getPageSize());
        return PageResult.build(total, queryDTO.getPageNum(), queryDTO.getPageSize(), records);
    }

    @Override
    protected GoodsVO toVO(Goods entity) {
        if (entity == null) {
            return null;
        }
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Goods toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
