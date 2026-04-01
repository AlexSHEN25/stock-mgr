package co.handk.backend.service.impl;

import co.handk.backend.entity.RequestForm;
import co.handk.common.model.dto.RequestFormDTO;
import co.handk.common.model.vo.RequestFormVO;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.service.RequestFormService;
import co.handk.common.model.PageQuery;
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
    public Boolean create(RequestFormDTO dto) {
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
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
    public Boolean update(RequestFormDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<RequestFormVO> listAll() {
        LambdaQueryWrapper<RequestForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestForm::getDeleted, 0).orderByDesc(RequestForm::getUpdateTime);
        return     requestFormMapper.selectList(wrapper).stream().map(entity -> {
            RequestFormVO vo = new RequestFormVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<RequestFormVO> pageQuery(PageQuery query) {
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
