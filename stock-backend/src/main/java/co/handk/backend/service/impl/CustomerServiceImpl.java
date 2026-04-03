package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

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
        return this.lambdaUpdate().eq(Customer::getId, id).set(Customer::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<CustomerVO> pageQuery(CustomerQueryDTO query) {
        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getCustomerCode()), Customer::getCustomerCode, query.getCustomerCode())
                .like(StringUtils.isNotBlank(query.getName()), Customer::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), Customer::getEnglishName, query.getEnglishName())
                .like(StringUtils.isNotBlank(query.getContactPerson()), Customer::getContactPerson, query.getContactPerson())
                .like(StringUtils.isNotBlank(query.getPhone()), Customer::getPhone, query.getPhone())
                .like(StringUtils.isNotBlank(query.getEmail()), Customer::getEmail, query.getEmail())
                .like(StringUtils.isNotBlank(query.getCountry()), Customer::getCountry, query.getCountry())
                .like(StringUtils.isNotBlank(query.getCity()), Customer::getCity, query.getCity())
                .like(StringUtils.isNotBlank(query.getAddress()), Customer::getAddress, query.getAddress())
                .eq(query.getLevelId() != null, Customer::getLevelId, query.getLevelId())
                .like(StringUtils.isNotBlank(query.getRemark()), Customer::getRemark, query.getRemark())
                .eq(query.getStatus() != null, Customer::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Customer::getCreateTime, Customer::getUpdateTime);
        Page<Customer> resultPage =     customerMapper.selectPage(page, wrapper);
        List<CustomerVO> records = resultPage.getRecords().stream().map(entity -> {
            CustomerVO vo = new CustomerVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
