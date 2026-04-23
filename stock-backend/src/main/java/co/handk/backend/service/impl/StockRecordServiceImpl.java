package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.create.CreateStockRecordDTO;
import co.handk.common.model.dto.update.UpdateStockRecordDTO;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
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
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    private final StockRecordMapper stockRecordMapper;

    @Override
    public Boolean create(CreateStockRecordDTO dto) {
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockRecordVO get(Long id) {
        StockRecord entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        StockRecordVO vo = new StockRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockRecordDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        StockRecord entity = new StockRecord();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(StockRecord::getId, id).set(StockRecord::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<StockRecordVO> pageQuery(StockRecordQueryDTO query) {
        Page<StockRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockRecord::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getBizNo()), StockRecord::getBizNo, query.getBizNo())
                .eq(query.getOrderId() != null, StockRecord::getOrderId, query.getOrderId())
                .eq(query.getOrderItemId() != null, StockRecord::getOrderItemId, query.getOrderItemId())
                .eq(query.getStockId() != null, StockRecord::getStockId, query.getStockId())
                .eq(query.getGoodsId() != null, StockRecord::getGoodsId, query.getGoodsId())
                .eq(query.getSkuId() != null, StockRecord::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), StockRecord::getSkuCode, query.getSkuCode())
                .like(StringUtils.isNotBlank(query.getGoodsName()), StockRecord::getGoodsName, query.getGoodsName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), StockRecord::getEnglishName, query.getEnglishName())
                .eq(query.getBrandId() != null, StockRecord::getBrandId, query.getBrandId())
                .like(StringUtils.isNotBlank(query.getBrandName()), StockRecord::getBrandName, query.getBrandName())
                .eq(query.getSeriesId() != null, StockRecord::getSeriesId, query.getSeriesId())
                .like(StringUtils.isNotBlank(query.getSeriesName()), StockRecord::getSeriesName, query.getSeriesName())
                .eq(query.getCategoryId() != null, StockRecord::getCategoryId, query.getCategoryId())
                .like(StringUtils.isNotBlank(query.getCategoryName()), StockRecord::getCategoryName, query.getCategoryName())
                .eq(query.getMakerId() != null, StockRecord::getMakerId, query.getMakerId())
                .like(StringUtils.isNotBlank(query.getMakerName()), StockRecord::getMakerName, query.getMakerName())
                .eq(query.getWarehouseId() != null, StockRecord::getWarehouseId, query.getWarehouseId())
                .eq(query.getBeforeQty() != null, StockRecord::getBeforeQty, query.getBeforeQty())
                .eq(query.getChangeQty() != null, StockRecord::getChangeQty, query.getChangeQty())
                .eq(query.getAfterQty() != null, StockRecord::getAfterQty, query.getAfterQty())
                .eq(query.getStockTypeId() != null, StockRecord::getStockTypeId, query.getStockTypeId())
                .eq(query.getSourceType() != null, StockRecord::getSourceType, query.getSourceType())
                .eq(query.getPrice() != null, StockRecord::getPrice, query.getPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), StockRecord::getCurrency, query.getCurrency())
                .eq(query.getPriceUpdateTime() != null, StockRecord::getPriceUpdateTime, query.getPriceUpdateTime())
                .eq(query.getCustomerId() != null, StockRecord::getCustomerId, query.getCustomerId())
                .like(StringUtils.isNotBlank(query.getCustomerName()), StockRecord::getCustomerName, query.getCustomerName())
                .eq(query.getRequesterId() != null, StockRecord::getRequesterId, query.getRequesterId())
                .like(StringUtils.isNotBlank(query.getRequesterName()), StockRecord::getRequesterName, query.getRequesterName())
                .eq(query.getOperatorId() != null, StockRecord::getOperatorId, query.getOperatorId())
                .like(StringUtils.isNotBlank(query.getOperatorName()), StockRecord::getOperatorName, query.getOperatorName())
                .like(StringUtils.isNotBlank(query.getRemark()), StockRecord::getRemark, query.getRemark());
        PageSortUtil.applyTimeSort(wrapper, query, StockRecord::getCreateTime, StockRecord::getUpdateTime);
        Page<StockRecord> resultPage =     stockRecordMapper.selectPage(page, wrapper);
        List<StockRecordVO> records = resultPage.getRecords().stream().map(entity -> {
            StockRecordVO vo = new StockRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

