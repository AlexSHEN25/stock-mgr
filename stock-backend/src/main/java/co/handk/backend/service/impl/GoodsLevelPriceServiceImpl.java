package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsLevelPrice;
import co.handk.backend.mapper.GoodsLevelPriceMapper;
import co.handk.backend.service.GoodsLevelPriceService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsLevelPriceDTO;
import co.handk.common.model.dto.query.GoodsLevelPriceQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsLevelPriceDTO;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsLevelPriceServiceImpl extends ServiceImpl<GoodsLevelPriceMapper, GoodsLevelPrice> implements GoodsLevelPriceService {

    private final GoodsLevelPriceMapper goodsLevelPriceMapper;

    @Override
    public Boolean create(CreateGoodsLevelPriceDTO dto) {
        GoodsLevelPrice entity = new GoodsLevelPrice();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsLevelPriceVO get(Long id) {
        GoodsLevelPrice entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        GoodsLevelPriceVO vo = new GoodsLevelPriceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsLevelPriceDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        GoodsLevelPrice entity = new GoodsLevelPrice();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(GoodsLevelPrice::getId, id)
                .set(GoodsLevelPrice::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsLevelPriceVO> pageQuery(GoodsLevelPriceQueryDTO query) {
        Page<GoodsLevelPrice> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsLevelPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsLevelPrice::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getGoodsId() != null, GoodsLevelPrice::getGoodsId, query.getGoodsId())
                .eq(query.getSkuId() != null, GoodsLevelPrice::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), GoodsLevelPrice::getSkuCode, query.getSkuCode())
                .eq(query.getLevelId() != null, GoodsLevelPrice::getLevelId, query.getLevelId())
                .eq(query.getPrice() != null, GoodsLevelPrice::getPrice, query.getPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), GoodsLevelPrice::getCurrency, query.getCurrency())
                .eq(query.getDiscount() != null, GoodsLevelPrice::getDiscount, query.getDiscount())
                .eq(query.getEffectiveTime() != null, GoodsLevelPrice::getEffectiveTime, query.getEffectiveTime())
                .eq(query.getExpireTime() != null, GoodsLevelPrice::getExpireTime, query.getExpireTime())
                .eq(query.getStatus() != null, GoodsLevelPrice::getStatus,
                        (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, GoodsLevelPrice::getCreateTime, GoodsLevelPrice::getUpdateTime);
        Page<GoodsLevelPrice> resultPage = goodsLevelPriceMapper.selectPage(page, wrapper);
        List<GoodsLevelPriceVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsLevelPriceVO vo = new GoodsLevelPriceVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

