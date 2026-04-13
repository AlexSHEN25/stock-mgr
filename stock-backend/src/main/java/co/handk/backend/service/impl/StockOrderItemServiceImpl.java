package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.dto.create.CreateStockOrderItemDTO;
import co.handk.common.model.dto.update.UpdateStockOrderItemDTO;
import co.handk.common.model.vo.StockOrderItemVO;
import co.handk.backend.mapper.StockOrderItemMapper;
import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
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
public class StockOrderItemServiceImpl extends ServiceImpl<StockOrderItemMapper, StockOrderItem> implements StockOrderItemService {

    private final StockOrderItemMapper stockOrderItemMapper;

    @Override
    public Boolean create(CreateStockOrderItemDTO dto) {
        StockOrderItem entity = new StockOrderItem();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public StockOrderItemVO get(Long id) {
        StockOrderItem entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        StockOrderItemVO vo = new StockOrderItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockOrderItemDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        StockOrderItem entity = new StockOrderItem();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(StockOrderItem::getId, id).set(StockOrderItem::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<StockOrderItemVO> pageQuery(StockOrderItemQueryDTO query) {
        Page<StockOrderItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOrderItem::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getOrderId() != null, StockOrderItem::getOrderId, query.getOrderId())
                .eq(query.getGoodsId() != null, StockOrderItem::getGoodsId, query.getGoodsId())
                .eq(query.getSkuId() != null, StockOrderItem::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), StockOrderItem::getSkuCode, query.getSkuCode())
                .like(StringUtils.isNotBlank(query.getGoodsName()), StockOrderItem::getGoodsName, query.getGoodsName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), StockOrderItem::getEnglishName, query.getEnglishName())
                .eq(query.getBrandId() != null, StockOrderItem::getBrandId, query.getBrandId())
                .like(StringUtils.isNotBlank(query.getBrandName()), StockOrderItem::getBrandName, query.getBrandName())
                .eq(query.getSeriesId() != null, StockOrderItem::getSeriesId, query.getSeriesId())
                .like(StringUtils.isNotBlank(query.getSeriesName()), StockOrderItem::getSeriesName, query.getSeriesName())
                .eq(query.getTypeId() != null, StockOrderItem::getTypeId, query.getTypeId())
                .like(StringUtils.isNotBlank(query.getTypeName()), StockOrderItem::getTypeName, query.getTypeName())
                .eq(query.getMakerId() != null, StockOrderItem::getMakerId, query.getMakerId())
                .like(StringUtils.isNotBlank(query.getMakerName()), StockOrderItem::getMakerName, query.getMakerName())
                .eq(query.getBeforeQty() != null, StockOrderItem::getBeforeQty, query.getBeforeQty())
                .eq(query.getChangeQty() != null, StockOrderItem::getChangeQty, query.getChangeQty())
                .eq(query.getAfterQty() != null, StockOrderItem::getAfterQty, query.getAfterQty())
                .eq(query.getPrice() != null, StockOrderItem::getPrice, query.getPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), StockOrderItem::getCurrency, query.getCurrency())
                .like(StringUtils.isNotBlank(query.getRemark()), StockOrderItem::getRemark, query.getRemark());
        PageSortUtil.applyTimeSort(wrapper, query, StockOrderItem::getCreateTime, StockOrderItem::getUpdateTime);
        Page<StockOrderItem> resultPage =     stockOrderItemMapper.selectPage(page, wrapper);
        List<StockOrderItemVO> records = resultPage.getRecords().stream().map(entity -> {
            StockOrderItemVO vo = new StockOrderItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

