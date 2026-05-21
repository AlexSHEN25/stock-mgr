package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Customer;
import co.handk.backend.mapper.CustomerMapper;
import co.handk.backend.service.CustomerService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.vo.CustomerVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
public class CustomerServiceImpl extends BaseServiceImpl<CustomerMapper, Customer, CustomerVO>
        implements CustomerService {

    @Autowired
    private PermissionQueryService permissionQueryService;

    @Override
    protected CustomerVO toVO(Customer entity) {
        if (entity == null) {
            return null;
        }
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Customer toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Customer entity = new Customer();
        BeanUtils.copyProperties(dto, entity);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            entity.setOwnerUserId(userId);
        }
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<Customer> buildWrapper(Q dto) {
        QueryWrapper<Customer> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.eq("owner_user_id", userId);
        }
        return wrapper;
    }

    @Override
    public Customer getByIdNotDeleted(Serializable id) {
        Customer customer = super.getByIdNotDeleted(id);
        requireOwned(customer);
        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        Customer customer = super.getByIdNotDeleted(id);
        requireOwned(customer);
        return super.deleteByIdLogic(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return DeleteEnum.UNDELETED.getCode();
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return super.deleteBatchLogic(ids);
        }
        int rows = 0;
        for (Long id : ids) {
            Customer customer = super.getByIdNotDeleted(id);
            requireOwned(customer);
            rows += super.deleteByIdLogic(id);
        }
        return rows;
    }

    private void requireOwned(Customer customer) {
        if (customer == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        if (!userId.equals(customer.getOwnerUserId())) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "この顧客データにアクセスする権限がありません");
        }
    }
}

