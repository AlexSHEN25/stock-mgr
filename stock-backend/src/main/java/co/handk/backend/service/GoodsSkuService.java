package co.handk.backend.service;

import co.handk.backend.entity.GoodsSku;
import co.handk.common.model.vo.GoodsSkuVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsSkuService extends BaseService<GoodsSku, GoodsSkuVO> {
}