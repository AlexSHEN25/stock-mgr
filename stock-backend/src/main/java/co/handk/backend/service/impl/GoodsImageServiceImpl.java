package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsImage;
import co.handk.backend.mapper.GoodsImageMapper;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsImageDTO;
import co.handk.common.model.dto.query.GoodsImageQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsImageDTO;
import co.handk.common.model.vo.GoodsImageVO;
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
public class GoodsImageServiceImpl extends ServiceImpl<GoodsImageMapper, GoodsImage> implements GoodsImageService {

    private final GoodsImageMapper goodsImageMapper;

    @Override
    public Boolean create(CreateGoodsImageDTO dto) {
        GoodsImage entity = new GoodsImage();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsImageVO get(Long id) {
        GoodsImage entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsImageVO vo = new GoodsImageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateGoodsImageDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsImage entity = new GoodsImage();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(GoodsImage::getId, id)
                .set(GoodsImage::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsImageVO> pageQuery(GoodsImageQueryDTO query) {
        Page<GoodsImage> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<GoodsImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsImage::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getGoodsId() != null, GoodsImage::getGoodsId, query.getGoodsId())
                .eq(query.getSkuId() != null, GoodsImage::getSkuId, query.getSkuId())
                .like(StringUtils.isNotBlank(query.getSkuCode()), GoodsImage::getSkuCode, query.getSkuCode())
                .like(StringUtils.isNotBlank(query.getImageUrl()), GoodsImage::getImageUrl, query.getImageUrl())
                .eq(query.getSort() != null, GoodsImage::getSort, query.getSort());
        PageSortUtil.applyTimeSort(wrapper, query, GoodsImage::getCreateTime, GoodsImage::getUpdateTime);
        Page<GoodsImage> resultPage = goodsImageMapper.selectPage(page, wrapper);
        List<GoodsImageVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsImageVO vo = new GoodsImageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
