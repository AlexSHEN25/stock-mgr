package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsSkuSpec;
import co.handk.backend.mapper.GoodsSkuSpecMapper;
import co.handk.backend.service.GoodsSkuSpecService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuSpecDTO;
import co.handk.common.model.dto.query.GoodsSkuSpecQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuSpecDTO;
import co.handk.common.model.vo.GoodsSkuSpecVO;
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
public class GoodsSkuSpecServiceImpl extends ServiceImpl<GoodsSkuSpecMapper, GoodsSkuSpec> implements GoodsSkuSpecService {

    private final GoodsSkuSpecMapper goodsSkuSpecMapper;

    @Override
    public Boolean create(CreateGoodsSkuSpecDTO dto) {
        GoodsSkuSpec entity = new GoodsSkuSpec();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsSkuSpecVO get(Long id) {
        GoodsSkuSpec entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        GoodsSkuSpecVO vo = new GoodsSkuSpecVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsSkuSpecDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        GoodsSkuSpec entity = new GoodsSkuSpec();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("謨ｰ謐ｮ荳榊ｭ伜惠");
        }
        return this.lambdaUpdate().eq(GoodsSkuSpec::getId, id)
                .set(GoodsSkuSpec::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsSkuSpecVO> pageQuery(GoodsSkuSpecQueryDTO query) {
        Page<GoodsSkuSpec> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsSkuSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSkuSpec::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getSkuId() != null, GoodsSkuSpec::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), GoodsSkuSpec::getSkuCode, query.getSkuCode())
                .eq(query.getSpecId() != null, GoodsSkuSpec::getSpecId, query.getSpecId())
                .like(StringUtils.isNotBlank(query.getSpecName()), GoodsSkuSpec::getSpecName, query.getSpecName())
                .like(StringUtils.isNotBlank(query.getSpecValue()), GoodsSkuSpec::getSpecValue, query.getSpecValue())
                .eq(query.getSort() != null, GoodsSkuSpec::getSort, query.getSort());
        PageSortUtil.applyTimeSort(wrapper, query, GoodsSkuSpec::getCreateTime, GoodsSkuSpec::getUpdateTime);
        Page<GoodsSkuSpec> resultPage = goodsSkuSpecMapper.selectPage(page, wrapper);
        List<GoodsSkuSpecVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsSkuSpecVO vo = new GoodsSkuSpecVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
