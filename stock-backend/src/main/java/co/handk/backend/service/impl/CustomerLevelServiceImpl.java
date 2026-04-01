package co.handk.backend.service.impl;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.dto.CustomerLevelDTO;
import co.handk.common.model.vo.CustomerLevelVO;
import co.handk.backend.mapper.CustomerLevelMapper;
import co.handk.backend.service.CustomerLevelService;
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
public class CustomerLevelServiceImpl extends ServiceImpl<CustomerLevelMapper, CustomerLevel> implements CustomerLevelService {

    private final CustomerLevelMapper customerLevelMapper;

    @Override
    public Boolean create(CustomerLevelDTO dto) {
        CustomerLevel entity = new CustomerLevel();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public CustomerLevelVO get(Long id) {
        CustomerLevel entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        CustomerLevelVO vo = new CustomerLevelVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(CustomerLevelDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        CustomerLevel entity = new CustomerLevel();
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
    public List<CustomerLevelVO> listAll() {
        LambdaQueryWrapper<CustomerLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerLevel::getDeleted, 0).orderByDesc(CustomerLevel::getUpdateTime);
        return     customerLevelMapper.selectList(wrapper).stream().map(entity -> {
            CustomerLevelVO vo = new CustomerLevelVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<CustomerLevelVO> pageQuery(PageQuery query) {
        Page<CustomerLevel> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CustomerLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerLevel::getDeleted, 0).orderByDesc(CustomerLevel::getUpdateTime);
        Page<CustomerLevel> resultPage =     customerLevelMapper.selectPage(page, wrapper);
        List<CustomerLevelVO> records = resultPage.getRecords().stream().map(entity -> {
            CustomerLevelVO vo = new CustomerLevelVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
