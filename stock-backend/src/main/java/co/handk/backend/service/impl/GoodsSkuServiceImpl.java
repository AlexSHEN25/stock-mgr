package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsSku;
import co.handk.backend.mapper.GoodsSkuMapper;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.dto.query.GoodsSkuQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
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
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements GoodsSkuService {

    private final GoodsSkuMapper goodsSkuMapper;

    @Override
    public Boolean create(CreateGoodsSkuDTO dto) {
        GoodsSku entity = new GoodsSku();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsSkuVO get(Long id) {
        GoodsSku entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsSkuVO vo = new GoodsSkuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsSkuDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsSku entity = new GoodsSku();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(GoodsSku::getId, id)
                .set(GoodsSku::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsSkuVO> pageQuery(GoodsSkuQueryDTO query) {
        Page<GoodsSku> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSku::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getGoodsId() != null, GoodsSku::getGoodsId, query.getGoodsId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), GoodsSku::getSkuCode, query.getSkuCode())
                .like(StringUtils.isNotBlank(query.getSkuName()), GoodsSku::getSkuName, query.getSkuName())
                .eq(query.getPrice() != null, GoodsSku::getPrice, query.getPrice())
                .eq(StringUtils.isNotBlank(query.getCurrency()), GoodsSku::getCurrency, query.getCurrency())
                .eq(query.getCostPrice() != null, GoodsSku::getCostPrice, query.getCostPrice())
                .eq(query.getUpdatePrice() != null, GoodsSku::getUpdatePrice, query.getUpdatePrice())
                .eq(query.getPriceUpdateTime() != null, GoodsSku::getPriceUpdateTime, query.getPriceUpdateTime())
                .like(StringUtils.isNotBlank(query.getBarcode()), GoodsSku::getBarcode, query.getBarcode())
                .eq(query.getWeight() != null, GoodsSku::getWeight, query.getWeight())
                .eq(query.getVolume() != null, GoodsSku::getVolume, query.getVolume())
                .eq(query.getStatus() != null, GoodsSku::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, GoodsSku::getCreateTime, GoodsSku::getUpdateTime);
        Page<GoodsSku> resultPage = goodsSkuMapper.selectPage(page, wrapper);
        List<GoodsSkuVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsSkuVO vo = new GoodsSkuVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}

