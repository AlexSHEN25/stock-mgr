package co.handk.backend.service.impl;

import co.handk.backend.entity.Brand;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.service.BrandService;
import co.handk.common.model.vo.BrandVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, Brand, BrandVO>
        implements BrandService {

    @Override
    protected BrandVO toVO(Brand entity) {
        if (entity == null) {
            return null;
        }
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Brand toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Brand entity = new Brand();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}