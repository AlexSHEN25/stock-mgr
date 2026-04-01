package co.handk.backend.service.impl;

import co.handk.backend.entity.Series;
import co.handk.common.model.dto.SeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.PageQuery;
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
public class SeriesServiceImpl extends ServiceImpl<SeriesMapper, Series> implements SeriesService {

    private final SeriesMapper seriesMapper;

    @Override
    public Boolean create(SeriesDTO dto) {
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public Series get(Long id) {
        Series entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(SeriesDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<SeriesVO> listAll() {
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0).orderByDesc(Series::getUpdateTime);
        return     seriesMapper.selectList(wrapper).stream().map(entity -> {
            SeriesVO vo = new SeriesVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<SeriesVO> pageQuery(PageQuery query) {
        Page<Series> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0).orderByDesc(Series::getUpdateTime);
        Page<Series> resultPage =     seriesMapper.selectPage(page, wrapper);
        List<SeriesVO> records = resultPage.getRecords().stream().map(entity -> {
            SeriesVO vo = new SeriesVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
