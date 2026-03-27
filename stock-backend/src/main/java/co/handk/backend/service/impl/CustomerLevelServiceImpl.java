package co.handk.backend.service.impl;

import co.handk.backend.entity.CustomerLevel;
import co.handk.backend.mapper.CustomerLevelMapper;
import co.handk.backend.service.CustomerLevelService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerLevelServiceImpl extends ServiceImpl<CustomerLevelMapper, CustomerLevel> implements CustomerLevelService {

    private final CustomerLevelMapper customerLevelMapper;

    @Override
    public Boolean create(CustomerLevel entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public CustomerLevel get(Long id) {
        CustomerLevel entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(CustomerLevel entity) {
        if (entity == null || Objects.isNull(entity.getId())) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(entity.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (Objects.isNull(id)) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<CustomerLevel> listAll() {
        LambdaQueryWrapper<CustomerLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerLevel::getDeleted, 0).orderByDesc(CustomerLevel::getUpdateTime);
        return customerLevelMapper.selectList(wrapper);
    }

    @Override
    public PageResult<CustomerLevel> pageQuery(PageQuery query) {
        Page<CustomerLevel> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CustomerLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerLevel::getDeleted, 0).orderByDesc(CustomerLevel::getUpdateTime);
        Page<CustomerLevel> resultPage = customerLevelMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
