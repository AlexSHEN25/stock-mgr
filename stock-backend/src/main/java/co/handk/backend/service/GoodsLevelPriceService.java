package co.handk.backend.service;

import co.handk.backend.entity.GoodsLevelPrice;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsLevelPriceService extends BaseService<GoodsLevelPrice, GoodsLevelPriceVO> {
}