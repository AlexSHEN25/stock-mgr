package co.handk.backend.service;

import co.handk.backend.entity.Brand;
import co.handk.common.model.vo.BrandVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface BrandService extends BaseService<Brand, BrandVO> {
}