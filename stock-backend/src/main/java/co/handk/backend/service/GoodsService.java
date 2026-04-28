package co.handk.backend.service;

import co.handk.backend.entity.Goods;
import co.handk.common.model.vo.GoodsVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsService extends BaseService<Goods, GoodsVO> {
}