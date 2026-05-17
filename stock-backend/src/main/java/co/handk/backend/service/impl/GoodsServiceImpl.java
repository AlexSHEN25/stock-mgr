package co.handk.backend.service.impl;

import co.handk.backend.annotation.JoinQueryConfig;
import co.handk.backend.annotation.JoinTable;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.enums.JoinType;
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
import co.handk.common.model.dto.query.GoodsBundleQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsBundleVO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@JoinQueryConfig(
        baseTable = "t_goods",
        baseAlias = "t",
        joins = {
                @JoinTable(type = JoinType.LEFT, table = "t_brand", alias = "b", on = "b.id = t.brand_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_series", alias = "s", on = "s.id = t.series_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_category", alias = "c", on = "c.id = t.category_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_maker", alias = "m", on = "m.id = t.maker_id")
        }
)
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private GoodsImageService goodsImageService;

    @Override
    public PageResult<GoodsBundleVO> pageBundle(GoodsBundleQueryDTO queryDTO) {
        Long total = baseMapper.countBundlePage(queryDTO);
        if (total == null || total <= 0) {
            return PageResult.build(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), Collections.emptyList());
        }
        long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<GoodsBundleVO> records = baseMapper.selectBundlePage(queryDTO, offset, queryDTO.getPageSize());
        return PageResult.build(total, queryDTO.getPageNum(), queryDTO.getPageSize(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (!(dto instanceof CreateGoodsDTO createDto)) {
            return super.saveByDto(dto);
        }
        Goods goods = toEntity(createDto);
        if (goods.getStatus() == null) {
            goods.setStatus(StatusEnum.NOMAL.getCode());
        }
        boolean goodsSaved = this.save(goods);
        if (!goodsSaved) {
            return false;
        }
        if (!StringUtils.hasText(createDto.getSkuCode())) {
            throw new RuntimeException("SKUコードは必須です");
        }
        if (!StringUtils.hasText(createDto.getImageUrl())) {
            throw new RuntimeException("商品画像URLは必須です");
        }

        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goods.getId());
        sku.setSkuCode(createDto.getSkuCode().trim());
        sku.setSkuName(createDto.getSkuName());
        sku.setPrice(createDto.getPrice() == null ? java.math.BigDecimal.ZERO : createDto.getPrice());
        sku.setCurrency(StringUtils.hasText(createDto.getCurrency()) ? createDto.getCurrency().trim() : CommonConstant.DEFAULT_CURRENCY_JPY);
        sku.setCostPrice(createDto.getCostPrice());
        sku.setUpdatePrice(createDto.getUpdatePrice());
        sku.setPriceUpdateTime(createDto.getPriceUpdateTime());
        sku.setBarcode(createDto.getBarcode());
        sku.setWeight(createDto.getWeight());
        sku.setVolume(createDto.getVolume());
        sku.setStatus(createDto.getSkuStatus() == null ? StatusEnum.NOMAL.getCode() : createDto.getSkuStatus().getCode());
        if (!goodsSkuService.save(sku)) {
            throw new RuntimeException("SKUの登録に失敗しました");
        }

        GoodsImage image = new GoodsImage();
        image.setGoodsId(goods.getId());
        image.setSkuId(sku.getId());
        image.setSkuCode(sku.getSkuCode());
        image.setImageUrl(createDto.getImageUrl().trim());
        image.setSort(createDto.getImageSort() == null ? NumberConstant.ZERO : createDto.getImageSort());
        if (!goodsImageService.save(image)) {
            throw new RuntimeException("商品画像の登録に失敗しました");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (!(dto instanceof UpdateGoodsDTO updateDto)) {
            return super.updateByDto(dto);
        }
        boolean goodsUpdated = super.updateByDto(dto);
        if (!goodsUpdated) {
            return false;
        }
        if (updateDto.getSkuId() != null) {
            UpdateWrapper<GoodsSku> skuWrapper = new UpdateWrapper<GoodsSku>()
                    .eq("id", updateDto.getSkuId())
                    .eq("goods_id", updateDto.getId())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set(StringUtils.hasText(updateDto.getSkuCode()), "sku_code", updateDto.getSkuCode())
                    .set(StringUtils.hasText(updateDto.getSkuName()), "sku_name", updateDto.getSkuName())
                    .set(updateDto.getPrice() != null, "price", updateDto.getPrice())
                    .set(StringUtils.hasText(updateDto.getCurrency()), "currency", updateDto.getCurrency())
                    .set(updateDto.getCostPrice() != null, "cost_price", updateDto.getCostPrice())
                    .set(updateDto.getUpdatePrice() != null, "update_price", updateDto.getUpdatePrice())
                    .set(updateDto.getPriceUpdateTime() != null, "price_update_time", updateDto.getPriceUpdateTime())
                    .set(StringUtils.hasText(updateDto.getBarcode()), "barcode", updateDto.getBarcode())
                    .set(updateDto.getWeight() != null, "weight", updateDto.getWeight())
                    .set(updateDto.getVolume() != null, "volume", updateDto.getVolume())
                    .set(updateDto.getSkuStatus() != null, "status", updateDto.getSkuStatus().getCode());
            goodsSkuService.update(null, skuWrapper);
        }
        if (updateDto.getImageId() != null) {
            UpdateWrapper<GoodsImage> imageWrapper = new UpdateWrapper<GoodsImage>()
                    .eq("id", updateDto.getImageId())
                    .eq("goods_id", updateDto.getId())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .set(StringUtils.hasText(updateDto.getImageUrl()), "image_url", updateDto.getImageUrl())
                    .set(updateDto.getImageSort() != null, "sort", updateDto.getImageSort());
            goodsImageService.update(null, imageWrapper);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
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
