package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

    private final GoodsSkuService goodsSkuService;
    private final GoodsImageService goodsImageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveGoods(CreateGoodsDTO dto) {
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

        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goods.getId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setSkuName(StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : goods.getName());
        sku.setPrice(dto.getPrice());
        sku.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : CommonConstant.DEFAULT_CURRENCY_JPY);
        sku.setCostPrice(dto.getCostPrice());
        sku.setUpdatePrice(dto.getUpdatePrice());
        sku.setPriceUpdateTime(dto.getPriceUpdateTime());
        sku.setBarcode(dto.getBarcode());
        sku.setWeight(dto.getWeight());
        sku.setVolume(dto.getVolume());
        sku.setStatus(dto.getSkuStatus() == null ? StatusEnum.NOMAL.getCode() : dto.getSkuStatus().getCode());
        boolean skuSaved = goodsSkuService.save(sku);
        if (!skuSaved) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUの登録に失敗しました");
        }

        if (StringUtils.hasText(dto.getImageUrl())) {
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goods.getId());
            image.setSkuId(sku.getId());
            image.setSkuCode(sku.getSkuCode());
            image.setImageUrl(dto.getImageUrl());
            image.setSort(dto.getImageSort() == null ? NumberConstant.ZERO : dto.getImageSort());
            boolean imageSaved = goodsImageService.save(image);
            if (!imageSaved) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の登録に失敗しました");
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
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGoods(UpdateGoodsDTO dto) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setStatus(dto.getStatus() == null ? null : dto.getStatus().getCode());
        boolean goodsUpdated = super.updateByDto(goods);
        if (!goodsUpdated) {
            return false;
        }

        UpdateWrapper<GoodsSku> skuWrapper = new UpdateWrapper<GoodsSku>()
                .eq("goods_id", dto.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode())
                .set(StringUtils.hasText(dto.getSkuName()), "sku_name", dto.getSkuName())
                .set(dto.getPrice() != null, "price", dto.getPrice())
                .set(StringUtils.hasText(dto.getCurrency()), "currency", dto.getCurrency())
                .set(dto.getCostPrice() != null, "cost_price", dto.getCostPrice())
                .set(dto.getUpdatePrice() != null, "update_price", dto.getUpdatePrice())
                .set(dto.getPriceUpdateTime() != null, "price_update_time", dto.getPriceUpdateTime())
                .set(StringUtils.hasText(dto.getBarcode()), "barcode", dto.getBarcode())
                .set(dto.getWeight() != null, "weight", dto.getWeight())
                .set(dto.getVolume() != null, "volume", dto.getVolume())
                .set(dto.getSkuStatus() != null, "status", dto.getSkuStatus().getCode());
        if (dto.getSkuId() != null) {
            skuWrapper.eq("id", dto.getSkuId());
        }
        goodsSkuService.update(null, skuWrapper);

        if (StringUtils.hasText(dto.getImageUrl()) || dto.getImageSort() != null || dto.getImageId() != null) {
            UpdateWrapper<GoodsImage> imageWrapper = new UpdateWrapper<GoodsImage>()
                    .eq("goods_id", dto.getId())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set(StringUtils.hasText(dto.getImageUrl()), "image_url", dto.getImageUrl())
                    .set(dto.getImageSort() != null, "sort", dto.getImageSort())
                    .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode());
            if (dto.getImageId() != null) {
                imageWrapper.eq("id", dto.getImageId());
            }
            goodsImageService.update(null, imageWrapper);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGoodsById(Long id) {
        int goodsRows = super.deleteByIdLogic(id);
        goodsSkuService.update(null, new UpdateWrapper<GoodsSku>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
        goodsImageService.update(null, new UpdateWrapper<GoodsImage>()
                .eq("goods_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .set("deleted", DeleteEnum.DELETED.getCode()));
        return goodsRows;
    }

    @Override
    public PageResult<GoodsVO> pageGoods(GoodsQueryDTO queryDTO) {
        Long total = baseMapper.countGoodsPage(queryDTO);
        if (total == null || total <= 0) {
            return PageResult.build(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), Collections.emptyList());
        }
        long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<GoodsVO> records = baseMapper.selectGoodsPage(queryDTO, offset, queryDTO.getPageSize());
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
