package co.handk.backend.service;

import co.handk.backend.entity.GoodsSkuSpec;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsSkuSpecService extends BaseService<GoodsSkuSpec, GoodsSkuSpecVO> {
}