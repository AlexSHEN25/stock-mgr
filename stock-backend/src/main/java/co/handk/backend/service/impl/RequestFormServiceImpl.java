package co.handk.backend.service.impl;

import co.handk.backend.entity.RequestForm;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.service.RequestFormService;
import co.handk.common.model.vo.RequestFormVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RequestFormServiceImpl extends BaseServiceImpl<RequestFormMapper, RequestForm, RequestFormVO>
        implements RequestFormService {

    @Override
    protected RequestFormVO toVO(RequestForm entity) {
        if (entity == null) {
            return null;
        }
        RequestFormVO vo = new RequestFormVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RequestForm toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}