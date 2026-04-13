package co.handk.backend.service.impl;

import org.apache.commons.lang3.StringUtils;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.RequestItem;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import co.handk.common.model.vo.RequestItemVO;
import co.handk.backend.mapper.RequestItemMapper;
import co.handk.backend.service.RequestItemService;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
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
public class RequestItemServiceImpl extends ServiceImpl<RequestItemMapper, RequestItem> implements RequestItemService {

    private final RequestItemMapper requestItemMapper;

    @Override
    public Boolean create(CreateRequestItemDTO dto) {
        RequestItem entity = new RequestItem();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RequestItemVO get(Long id) {
        RequestItem entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        RequestItemVO vo = new RequestItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateRequestItemDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        RequestItem entity = new RequestItem();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(RequestItem::getId, id).set(RequestItem::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<RequestItemVO> pageQuery(RequestItemQueryDTO query) {
        Page<RequestItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RequestItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestItem::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getRequestId() != null, RequestItem::getRequestId, query.getRequestId())
                .eq(query.getGoodsId() != null, RequestItem::getGoodsId, query.getGoodsId())
                .eq(query.getSkuId() != null, RequestItem::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSku()), RequestItem::getSkuCode, query.getSku())
                .like(StringUtils.isNotBlank(query.getGoodsName()), RequestItem::getGoodsName, query.getGoodsName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), RequestItem::getEnglishName, query.getEnglishName())
                .eq(query.getBrandId() != null, RequestItem::getBrandId, query.getBrandId())
                .like(StringUtils.isNotBlank(query.getBrandName()), RequestItem::getBrandName, query.getBrandName())
                .eq(query.getSeriesId() != null, RequestItem::getSeriesId, query.getSeriesId())
                .like(StringUtils.isNotBlank(query.getSeriesName()), RequestItem::getSeriesName, query.getSeriesName())
                .eq(query.getTypeId() != null, RequestItem::getTypeId, query.getTypeId())
                .like(StringUtils.isNotBlank(query.getTypeName()), RequestItem::getTypeName, query.getTypeName())
                .eq(query.getMakerId() != null, RequestItem::getMakerId, query.getMakerId())
                .like(StringUtils.isNotBlank(query.getMakerName()), RequestItem::getMakerName, query.getMakerName())
                .eq(query.getWarehouseId() != null, RequestItem::getWarehouseId, query.getWarehouseId())
                .eq(query.getPrice() != null, RequestItem::getPrice, query.getPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), RequestItem::getCurrency, query.getCurrency())
                .eq(query.getDiscount() != null, RequestItem::getDiscount, query.getDiscount())
                .eq(query.getRequestQty() != null, RequestItem::getRequestQty, query.getRequestQty())
                .eq(query.getApproveQty() != null, RequestItem::getApproveQty, query.getApproveQty())
                .eq(query.getOutQty() != null, RequestItem::getOutQty, query.getOutQty())
                .eq(query.getStockRecordId() != null, RequestItem::getStockRecordId, query.getStockRecordId())
                .like(StringUtils.isNotBlank(query.getRemark()), RequestItem::getRemark, query.getRemark());
        PageSortUtil.applyTimeSort(wrapper, query, RequestItem::getCreateTime, RequestItem::getUpdateTime);
        Page<RequestItem> resultPage =     requestItemMapper.selectPage(page, wrapper);
        List<RequestItemVO> records = resultPage.getRecords().stream().map(entity -> {
            RequestItemVO vo = new RequestItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

