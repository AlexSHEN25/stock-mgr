package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.Customer;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import co.handk.common.model.vo.CustomerVO;
import co.handk.backend.mapper.CustomerMapper;
import co.handk.backend.service.CustomerService;
import co.handk.common.model.dto.query.CustomerQueryDTO;
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
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    public Boolean create(CreateCustomerDTO dto) {
        Customer entity = new Customer();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public CustomerVO get(Long id) {
        Customer entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateCustomerDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Customer entity = new Customer();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Customer::getId, id).set(Customer::getDeleted, 1).update();
    }

    @Override
    public PageResult<CustomerVO> pageQuery(CustomerQueryDTO query) {
        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, 0).orderByDesc(Customer::getUpdateTime);
        Page<Customer> resultPage =     customerMapper.selectPage(page, wrapper);
        List<CustomerVO> records = resultPage.getRecords().stream().map(entity -> {
            CustomerVO vo = new CustomerVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
