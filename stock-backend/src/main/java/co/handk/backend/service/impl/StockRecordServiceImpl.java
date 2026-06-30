package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.vo.StockRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StockRecordServiceImpl extends BaseServiceImpl<StockRecordMapper, StockRecord, StockRecordVO>
        implements StockRecordService {

    private final GoodsService goodsService;
    private final GoodsSkuService goodsSkuService;

    public StockRecordServiceImpl(GoodsService goodsService, GoodsSkuService goodsSkuService) {
        this.goodsService = goodsService;
        this.goodsSkuService = goodsSkuService;
    }

    @Override
    protected StockRecordVO toVO(StockRecord entity) {
        if (entity == null) {
            return null;
        }
        StockRecordVO vo = new StockRecordVO();
        BeanUtils.copyProperties(entity, vo);
        fillGoodsSnapshot(vo);
        return vo;
    }

    @Override
    protected <D> StockRecord toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private void fillGoodsSnapshot(StockRecordVO vo) {
        if (vo == null) {
            return;
        }
        if ((!hasText(vo.getGoodsName()) || !hasText(vo.getEnglishName())) && vo.getGoodsId() != null) {
            Goods goods = goodsService.getByIdNotDeleted(vo.getGoodsId());
            if (goods != null) {
                if (!hasText(vo.getGoodsName())) {
                    vo.setGoodsName(goods.getName());
                }
                if (!hasText(vo.getEnglishName())) {
                    vo.setEnglishName(goods.getEnglishName());
                }
            }
        }
        if (!hasText(vo.getSkuCode()) && vo.getSkuId() != null) {
            GoodsSku sku = goodsSkuService.getByIdNotDeleted(vo.getSkuId());
            if (sku != null) {
                vo.setSkuCode(sku.getSkuCode());
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
