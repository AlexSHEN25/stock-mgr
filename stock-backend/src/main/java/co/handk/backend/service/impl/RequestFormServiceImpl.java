package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestFormVO;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.service.RequestFormService;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestFormServiceImpl extends ServiceImpl<RequestFormMapper, RequestForm> implements RequestFormService {

    private final RequestFormMapper requestFormMapper;

    @Override
    public Boolean create(CreateRequestFormDTO dto) {
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RequestFormVO get(Long id) {
        RequestForm entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        RequestFormVO vo = new RequestFormVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateRequestFormDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(RequestForm::getId, id).set(RequestForm::getDeleted, 1).update();
    }

    @Override
    public PageResult<RequestFormVO> pageQuery(RequestFormQueryDTO query) {
        Page<RequestForm> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RequestForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestForm::getDeleted, 0).orderByDesc(RequestForm::getUpdateTime);
        Page<RequestForm> resultPage =     requestFormMapper.selectPage(page, wrapper);
        List<RequestFormVO> records = resultPage.getRecords().stream().map(entity -> {
            RequestFormVO vo = new RequestFormVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
