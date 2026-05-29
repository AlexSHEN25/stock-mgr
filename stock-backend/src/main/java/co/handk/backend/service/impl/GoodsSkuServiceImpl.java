package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsSku;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.GoodsSkuMapper;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GoodsSkuServiceImpl extends BaseServiceImpl<GoodsSkuMapper, GoodsSku, GoodsSkuVO>
        implements GoodsSkuService {

    @Override
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateGoodsSkuDTO createDto) {
            validatePriceUpdateFields(createDto.getUpdatePrice(), createDto.getPriceUpdateTime());
            if (!StringUtils.hasText(createDto.getSkuName())) {
                createDto.setSkuName(createDto.getSkuCode());
            }
            if (!StringUtils.hasText(createDto.getBarcode())) {
                createDto.setBarcode(generateBarcode());
            }
        }
        return super.saveByDto(dto);
    }

    @Override
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateGoodsSkuDTO updateDto) {
            validatePriceUpdateFields(updateDto.getUpdatePrice(), updateDto.getPriceUpdateTime());
            if (!StringUtils.hasText(updateDto.getSkuName())) {
                updateDto.setSkuName(updateDto.getSkuCode());
            }
            if (!StringUtils.hasText(updateDto.getBarcode())) {
                updateDto.setBarcode(generateBarcode());
            }
        }
        return super.updateByDto(dto);
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

    private void validatePriceUpdateFields(java.math.BigDecimal updatePrice, LocalDateTime priceUpdateTime) {
        if (updatePrice != null && priceUpdateTime == null) {
            throw new BusinessException(
                    MessageKeyConstant.ERROR_RUNTIME,
                    "updatePrice入力時はpriceUpdateTimeが必須です"
            );
        }
    }

    private String generateBarcode() {
        String ts = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "BC" + ts + random;
    }
}
