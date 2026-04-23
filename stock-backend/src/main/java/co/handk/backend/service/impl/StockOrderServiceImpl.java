package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.StockOrderService;
import co.handk.common.model.dto.query.StockOrderQueryDTO;
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
public class StockOrderServiceImpl extends ServiceImpl<StockOrderMapper, StockOrder> implements StockOrderService {

    private final StockOrderMapper stockOrderMapper;

    @Override
    public Boolean create(CreateStockOrderDTO dto) {
        StockOrder entity = new StockOrder();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockOrderVO get(Long id) {
        StockOrder entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        StockOrderVO vo = new StockOrderVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockOrderDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        StockOrder entity = new StockOrder();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(StockOrder::getId, id).set(StockOrder::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<StockOrderVO> pageQuery(StockOrderQueryDTO query) {
        Page<StockOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrder::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getOrderNo()), StockOrder::getOrderNo, query.getOrderNo())
                .eq(query.getOrderType() != null, StockOrder::getOrderType, query.getOrderType())
                .eq(query.getStockTypeId() != null, StockOrder::getStockTypeId, query.getStockTypeId())
                .eq(query.getWarehouseId() != null, StockOrder::getWarehouseId, query.getWarehouseId())
                .eq(query.getSourceType() != null, StockOrder::getSourceType, query.getSourceType())
                .eq(query.getSourceId() != null, StockOrder::getSourceId, query.getSourceId())
                .eq(query.getTotalQty() != null, StockOrder::getTotalQty, query.getTotalQty())
                .eq(query.getState() != null, StockOrder::getState, query.getState())
                .eq(query.getRequesterId() != null, StockOrder::getRequesterId, query.getRequesterId())
                .like(StringUtils.isNotBlank(query.getRequesterName()), StockOrder::getRequesterName, query.getRequesterName())
                .eq(query.getOperatorId() != null, StockOrder::getOperatorId, query.getOperatorId())
                .like(StringUtils.isNotBlank(query.getOperatorName()), StockOrder::getOperatorName, query.getOperatorName())
                .like(StringUtils.isNotBlank(query.getRemark()), StockOrder::getRemark, query.getRemark())
                .eq(query.getApproverId() != null, StockOrder::getApproverId, query.getApproverId())
                .like(StringUtils.isNotBlank(query.getApproverName()), StockOrder::getApproverName, query.getApproverName())
                .eq(query.getApproveTime() != null, StockOrder::getApproveTime, query.getApproveTime())
                .eq(query.getVersion() != null, StockOrder::getVersion, query.getVersion())
                .eq(query.getFinishTime() != null, StockOrder::getFinishTime, query.getFinishTime());
        PageSortUtil.applyTimeSort(wrapper, query, StockOrder::getCreateTime, StockOrder::getUpdateTime);
        Page<StockOrder> resultPage =     stockOrderMapper.selectPage(page, wrapper);
        List<StockOrderVO> records = resultPage.getRecords().stream().map(entity -> {
            StockOrderVO vo = new StockOrderVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
