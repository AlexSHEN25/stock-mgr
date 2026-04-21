package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsVO;
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
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsMapper goodsMapper;

    @Override
    public Boolean create(CreateGoodsDTO dto) {
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsVO get(Long id) {
        Goods entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Goods::getId, id).set(Goods::getDeleted, DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsVO> pageQuery(GoodsQueryDTO query) {
        Page<Goods> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getDeleted, DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Goods::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), Goods::getEnglishName, query.getEnglishName())
                .like(StringUtils.isNotBlank(query.getDescription()), Goods::getDescription, query.getDescription())
                .eq(query.getSeriesId() != null, Goods::getSeriesId, query.getSeriesId())
                .eq(query.getBrandId() != null, Goods::getBrandId, query.getBrandId())
                .eq(query.getCategoryId() != null, Goods::getCategoryId, query.getCategoryId())
                .eq(query.getMakerId() != null, Goods::getMakerId, query.getMakerId())
                .eq(query.getStatus() != null, Goods::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()))
                .eq(query.getIsHot() != null, Goods::getIsHot, query.getIsHot());
        PageSortUtil.applyTimeSort(wrapper, query, Goods::getCreateTime, Goods::getUpdateTime);
        Page<Goods> resultPage =     goodsMapper.selectPage(page, wrapper);
        List<GoodsVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
