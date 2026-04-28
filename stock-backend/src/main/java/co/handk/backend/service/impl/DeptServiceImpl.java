package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.service.DeptService;
import co.handk.common.model.vo.DeptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends BaseServiceImpl<DeptMapper, Dept, DeptVO>
        implements DeptService {

    @Override
    protected DeptVO toVO(Dept entity) {
        if (entity == null) {
            return null;
        }
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Dept toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Dept entity = new Dept();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}