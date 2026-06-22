package co.handk.backend.service.impl;

import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateSeriesDTO;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.dto.update.UpdateSeriesDTO;
import co.handk.common.model.vo.SeriesVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeriesServiceImpl extends BaseServiceImpl<SeriesMapper, Series, SeriesVO>
        implements SeriesService {

    private final BrandMapper brandMapper;
    private final MakerMapper makerMapper;
    private final MakerServiceImpl makerService;

    public SeriesServiceImpl(BrandMapper brandMapper,
                             MakerMapper makerMapper,
                             MakerServiceImpl makerService) {
        this.brandMapper = brandMapper;
        this.makerMapper = makerMapper;
        this.makerService = makerService;
    }

    @Override
    protected SeriesVO toVO(Series entity) {
        if (entity == null) {
            return null;
        }
        SeriesVO vo = new SeriesVO();
        BeanUtils.copyProperties(entity, vo);
        fillRelations(entity, vo);
        return vo;
    }

    @Override
    protected <D> Series toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<Series> buildWrapper(Q dto) {
        if (!(dto instanceof SeriesQueryDTO query)) {
            return super.buildWrapper(dto);
        }
        QueryWrapper<Series> wrapper = new QueryWrapper<>();
        if (query.getName() != null && !query.getName().isBlank()) {
            wrapper.like("name", query.getName().trim());
        }
        if (query.getEnglishName() != null && !query.getEnglishName().isBlank()) {
            wrapper.like("english_name", query.getEnglishName().trim());
        }
        if (query.getBrandId() != null) {
            wrapper.eq("brand_id", query.getBrandId());
        }
        if (query.getContent() != null && !query.getContent().isBlank()) {
            wrapper.like("content", query.getContent().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus().getCode());
        }
        return wrapper;
    }

    @Override
    public <Q> List<SeriesVO> list(Q dto) {
        List<SeriesVO> records = super.list(dto);
        fillRelations(records);
        return records;
    }

    @Override
    public <Q extends PageQuery> PageResult<SeriesVO> page(Q dto) {
        PageResult<SeriesVO> result = super.page(dto);
        fillRelations(result == null ? null : result.getRecords());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateSeriesDTO createDto && createDto.getBrandId() == null) {
            return false;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateSeriesDTO updateDto && updateDto.getBrandId() == null) {
            return false;
        }
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        int deleted = super.deleteByIdLogic(id);
        if (deleted > 0) {
            List<Maker> children = makerMapper.selectList(new QueryWrapper<Maker>()
                    .eq("series_id", id)
                    .eq("deleted", 0));
            for (Maker child : children) {
                makerService.deleteByIdLogic(child.getId());
            }
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int rows = 0;
        for (Long id : ids) {
            rows += deleteByIdLogic(id);
        }
        return rows;
    }

    private void fillRelations(Series entity, SeriesVO vo) {
        if (entity == null || vo == null) {
            return;
        }
        if (entity.getBrandId() != null) {
            Brand brand = brandMapper.selectById(entity.getBrandId());
            if (brand != null && (brand.getDeleted() == null || brand.getDeleted() == 0)) {
                vo.setBrandId(brand.getId());
                vo.setBrandName(brand.getName());
            }
        }
        List<Maker> makers = makerMapper.selectList(new QueryWrapper<Maker>()
                .eq("series_id", entity.getId())
                .eq("deleted", 0)
                .orderByAsc("id"));
        vo.setMakerIds(makers.stream().map(Maker::getId).toList());
        vo.setMakerNames(makers.stream()
                .map(Maker::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList());
        vo.setMakerCount((long) makers.size());
    }

    private void fillRelations(List<SeriesVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> brandIds = records.stream()
                .map(SeriesVO::getBrandId)
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
        List<Long> seriesIds = records.stream()
                .map(SeriesVO::getId)
                .filter(id -> id != null)
                .toList();
        Map<Long, List<Maker>> makersBySeries = seriesIds.isEmpty()
                ? Map.of()
                : makerMapper.selectList(new QueryWrapper<Maker>()
                        .in("series_id", seriesIds)
                        .eq("deleted", 0)
                        .orderByAsc("id"))
                .stream()
                .collect(Collectors.groupingBy(Maker::getSeriesId));
        for (SeriesVO record : records) {
            if (record == null) {
                continue;
            }
            record.setBrandName(record.getBrandId() == null ? null : brandNameMap.get(record.getBrandId()));
            List<Maker> makers = makersBySeries.getOrDefault(record.getId(), List.of());
            record.setMakerIds(makers.stream().map(Maker::getId).toList());
            record.setMakerNames(makers.stream()
                    .map(Maker::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .toList());
            record.setMakerCount((long) makers.size());
        }
    }
}
