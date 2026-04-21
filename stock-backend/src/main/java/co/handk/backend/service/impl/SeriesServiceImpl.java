package co.handk.backend.service.impl;

import co.handk.backend.entity.Series;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.SeriesService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.common.model.vo.SeriesVO;
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
public class SeriesServiceImpl extends ServiceImpl<SeriesMapper, Series> implements SeriesService {

    private final SeriesMapper seriesMapper;

    @Override
    public Boolean create(CreateSeriesDTO dto) {
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public SeriesVO get(Long id) {
        Series entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        SeriesVO vo = new SeriesVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateSeriesDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Series::getId, id).set(Series::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<SeriesVO> pageQuery(SeriesQueryDTO query) {
        Page<Series> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getName()), Series::getName, query.getName())
                .like(StringUtils.isNotBlank(query.getEnglishName()), Series::getEnglishName, query.getEnglishName())
                .like(StringUtils.isNotBlank(query.getContent()), Series::getContent, query.getContent())
                .eq(query.getStatus() != null, Series::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, Series::getCreateTime, Series::getUpdateTime);
        Page<Series> resultPage =     seriesMapper.selectPage(page, wrapper);
        List<SeriesVO> records = resultPage.getRecords().stream().map(entity -> {
            SeriesVO vo = new SeriesVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
