package co.handk.backend.service;

import co.handk.backend.entity.GoodsImage;
import co.handk.common.model.vo.GoodsImageVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface GoodsImageService extends BaseService<GoodsImage, GoodsImageVO> {
}