package co.handk.backend.service.impl;

import co.handk.backend.entity.StockType;
import co.handk.backend.mapper.StockTypeMapper;
import co.handk.backend.service.StockTypeService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockTypeDTO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.dto.update.UpdateStockTypeDTO;
import co.handk.common.model.vo.GoodsTypeVO;
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
public class GoodsTypeServiceImpl extends ServiceImpl<StockTypeMapper, StockType> implements StockTypeService {

    private final StockTypeMapper goodsTypeMapper;

    @Override
    public Boolean create(CreateStockTypeDTO dto) {
        StockType entity = new StockType();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public GoodsTypeVO get(Long id) {
        StockType entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        GoodsTypeVO vo = new GoodsTypeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateStockTypeDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        StockType entity = new StockType();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(StockType::getId, id)
                .set(StockType::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<GoodsTypeVO> pageQuery(GoodsTypeQueryDTO query) {
        Page<StockType> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<StockType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockType::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), StockType::getName, query.getName())
                .eq(query.getStatus() != null, StockType::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, StockType::getCreateTime, StockType::getUpdateTime);
        Page<StockType> resultPage = goodsTypeMapper.selectPage(page, wrapper);
        List<GoodsTypeVO> records = resultPage.getRecords().stream().map(entity -> {
            GoodsTypeVO vo = new GoodsTypeVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
