package co.handk.backend.service.impl;

import co.handk.backend.entity.Maker;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.service.MakerService;
import co.handk.common.model.vo.MakerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class MakerServiceImpl extends BaseServiceImpl<MakerMapper, Maker, MakerVO>
        implements MakerService {

    @Override
    protected MakerVO toVO(Maker entity) {
        if (entity == null) {
            return null;
        }
        MakerVO vo = new MakerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Maker toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Maker entity = new Maker();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}