package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.mapper.GoodsSkuMapper;
import co.handk.backend.service.GoodsSkuService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GoodsSkuServiceImpl extends BaseServiceImpl<GoodsSkuMapper, GoodsSku, GoodsSkuVO>
        implements GoodsSkuService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (!(dto instanceof CreateGoodsSkuDTO createDto)) {
            return super.saveByDto(dto);
        }
        if (!StringUtils.hasText(createDto.getSkuCode())) {
            throw new RuntimeException("SKUコードは必須です");
        }
        String skuCode = createDto.getSkuCode().trim();

        Long goodsId = createDto.getGoodsId();
        if (goodsId == null) {
            Goods goods = new Goods();
            goods.setName(skuCode);
            goods.setEnglishName(skuCode);
            goods.setStatus(StatusEnum.NOMAL.getCode());
            goods.setSort(NumberConstant.ZERO);
            goods.setIsHot(NumberConstant.ZERO);
            int inserted = goodsMapper.insert(goods);
            if (inserted <= NumberConstant.ZERO || goods.getId() == null) {
                throw new RuntimeException("商品の作成に失敗しました");
            }
            goodsId = goods.getId();
        }

        GoodsSku entity = new GoodsSku();
        entity.setGoodsId(goodsId);
        entity.setSkuCode(skuCode);
        entity.setSkuName(StringUtils.hasText(createDto.getSkuName()) ? createDto.getSkuName().trim() : skuCode);
        entity.setPrice(createDto.getPrice());
        entity.setCurrency(StringUtils.hasText(createDto.getCurrency()) ? createDto.getCurrency().trim() : CommonConstant.DEFAULT_CURRENCY_JPY);
        entity.setCostPrice(createDto.getCostPrice());
        entity.setUpdatePrice(createDto.getUpdatePrice());
        entity.setPriceUpdateTime(createDto.getPriceUpdateTime());
        entity.setBarcode(createDto.getBarcode());
        entity.setWeight(createDto.getWeight());
        entity.setVolume(createDto.getVolume());
        entity.setStatus(StatusEnum.NOMAL.getCode());
        return this.save(entity);
    }

    @Override
    protected GoodsSkuVO toVO(GoodsSku entity) {
        if (entity == null) {
            return null;
        }
        GoodsSkuVO vo = new GoodsSkuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> GoodsSku toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsSku entity = new GoodsSku();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
