package co.handk.backend.service.impl;

import co.handk.backend.entity.Customer;
import co.handk.backend.mapper.CustomerMapper;
import co.handk.backend.service.CustomerService;
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
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    public Boolean create(Customer entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Customer get(Long id) {
        Customer entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(Customer entity) {
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
    public List<Customer> listAll() {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0).orderByDesc(Customer::getUpdateTime);
        return customerMapper.selectList(wrapper);
    }

    @Override
    public PageResult<Customer> pageQuery(PageQuery query) {
        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0).orderByDesc(Customer::getUpdateTime);
        Page<Customer> resultPage = customerMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
