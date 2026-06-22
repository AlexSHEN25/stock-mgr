package co.handk.backend.service.impl;

import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.MakerService;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.vo.MakerVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MakerServiceImpl extends BaseServiceImpl<MakerMapper, Maker, MakerVO>
        implements MakerService {

    private final SeriesMapper seriesMapper;
    private final BrandMapper brandMapper;

    public MakerServiceImpl(SeriesMapper seriesMapper, BrandMapper brandMapper) {
        this.seriesMapper = seriesMapper;
        this.brandMapper = brandMapper;
    }

    @Override
    protected MakerVO toVO(Maker entity) {
        if (entity == null) {
            return null;
        }
        MakerVO vo = new MakerVO();
        BeanUtils.copyProperties(entity, vo);
        fillRelations(entity, vo);
        return vo;
    }

    @Override
    protected <D> Maker toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Maker entity = new Maker();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<Maker> buildWrapper(Q dto) {
        if (!(dto instanceof MakerQueryDTO query)) {
            return super.buildWrapper(dto);
        }
        QueryWrapper<Maker> wrapper = new QueryWrapper<>();
        if (query.getName() != null && !query.getName().isBlank()) {
            wrapper.like("name", query.getName().trim());
        }
        if (query.getEnglishName() != null && !query.getEnglishName().isBlank()) {
            wrapper.like("english_name", query.getEnglishName().trim());
        }
        if (query.getSeriesId() != null) {
            wrapper.eq("series_id", query.getSeriesId());
        }
        if (query.getBrandId() != null) {
            List<Long> seriesIds = seriesMapper.selectList(new QueryWrapper<Series>()
                            .eq("brand_id", query.getBrandId())
                            .eq("deleted", 0))
                    .stream()
                    .map(Series::getId)
                    .toList();
            if (seriesIds.isEmpty()) {
                wrapper.eq("id", -1L);
            } else {
                wrapper.in("series_id", seriesIds);
            }
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus().getCode());
        }
        return wrapper;
    }

    @Override
    public <Q> List<MakerVO> list(Q dto) {
        List<MakerVO> records = super.list(dto);
        fillRelations(records);
        return records;
    }

    private void fillRelations(Maker entity, MakerVO vo) {
        if (entity == null || vo == null || entity.getSeriesId() == null) {
            return;
        }
        Series series = seriesMapper.selectById(entity.getSeriesId());
        if (series == null || (series.getDeleted() != null && series.getDeleted() != 0)) {
            return;
        }
        vo.setSeriesId(series.getId());
        vo.setSeriesName(series.getName());
        vo.setBrandId(series.getBrandId());
        if (series.getBrandId() != null) {
            Brand brand = brandMapper.selectById(series.getBrandId());
            if (brand != null && (brand.getDeleted() == null || brand.getDeleted() == 0)) {
                vo.setBrandName(brand.getName());
            }
        }
    }

    private void fillRelations(List<MakerVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> seriesIds = records.stream()
                .map(MakerVO::getSeriesId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, Series> seriesMap = seriesIds.isEmpty()
                ? Map.of()
                : seriesMapper.selectList(new QueryWrapper<Series>()
                        .in("id", seriesIds)
                        .eq("deleted", 0))
                .stream()
                .collect(Collectors.toMap(Series::getId, item -> item, (left, right) -> left));
        List<Long> brandIds = seriesMap.values().stream()
                .map(Series::getBrandId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> brandNameMap = brandIds.isEmpty()
                ? Map.of()
                : brandMapper.selectList(new QueryWrapper<Brand>()
                        .in("id", brandIds)
                        .eq("deleted", 0))
                .stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName, (left, right) -> left));
        for (MakerVO record : records) {
            if (record == null || record.getSeriesId() == null) {
                continue;
            }
            Series series = seriesMap.get(record.getSeriesId());
            if (series == null) {
                continue;
            }
            record.setSeriesName(series.getName());
            record.setBrandId(series.getBrandId());
            record.setBrandName(brandNameMap.get(series.getBrandId()));
        }
    }
}
