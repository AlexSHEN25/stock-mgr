package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.BrandMapper;
import co.handk.backend.mapper.MakerMapper;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.FileStorageService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.BrandTreeMakerSaveDTO;
import co.handk.common.model.dto.BrandTreeSaveDTO;
import co.handk.common.model.dto.BrandTreeSeriesSaveDTO;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandTreeNodeVO;
import co.handk.common.model.vo.BrandVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, Brand, BrandVO>
        implements BrandService {

    private final FileStorageService fileStorageService;
    private final SeriesMapper seriesMapper;
    private final MakerMapper makerMapper;
    private final SeriesServiceImpl seriesService;
    private final MakerServiceImpl makerService;

    public BrandServiceImpl(FileStorageService fileStorageService,
                            SeriesMapper seriesMapper,
                            MakerMapper makerMapper,
                            SeriesServiceImpl seriesService,
                            MakerServiceImpl makerService) {
        this.fileStorageService = fileStorageService;
        this.seriesMapper = seriesMapper;
        this.makerMapper = makerMapper;
        this.seriesService = seriesService;
        this.makerService = makerService;
    }

    @Override
    protected BrandVO toVO(Brand entity) {
        if (entity == null) {
            return null;
        }
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setImage(fileStorageService.toApiPath(UploadBizType.BRAND, vo.getImage()));
        fillChildren(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> Brand toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Brand entity = new Brand();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateBrandDTO createDto) {
            createDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, createDto.getImage()));
        }
        Brand brand = toEntity(dto);
        if (brand == null) {
            return false;
        }
        return save(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateBrandDTO updateDto) {
            updateDto.setImage(fileStorageService.normalize(UploadBizType.BRAND, updateDto.getImage()));
        }
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        int deleted = super.deleteByIdLogic(id);
        if (deleted > 0) {
            List<Series> children = seriesMapper.selectList(new QueryWrapper<Series>()
                    .eq("brand_id", id)
                    .eq("deleted", 0));
            for (Series child : children) {
                seriesService.deleteByIdLogic(child.getId());
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

    @Override
    public String uploadImage(MultipartFile file) {
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, null);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }

    @Override
    public String replaceImage(Long id, MultipartFile file) {
        if (id == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "brand id is required");
        }
        Brand existed = this.getByIdNotDeleted(id);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "brand does not exist");
        }
        String imagePath = fileStorageService.upload(UploadBizType.BRAND, file, existed.getImage());
        existed.setImage(imagePath);
        this.updateById(existed);
        return fileStorageService.toApiPath(UploadBizType.BRAND, imagePath);
    }

    @Override
    public List<BrandTreeNodeVO> listTree() {
        List<Brand> brands = super.list(new QueryWrapper<Brand>()
                .eq("status", 1)
                .orderByAsc("id"));
        List<Series> seriesList = seriesMapper.selectList(new QueryWrapper<Series>()
                .eq("deleted", 0)
                .eq("status", 1)
                .orderByAsc("id"));
        List<Maker> makerList = makerMapper.selectList(new QueryWrapper<Maker>()
                .eq("deleted", 0)
                .eq("status", 1)
                .orderByAsc("id"));

        Map<Long, List<Series>> seriesByBrand = new LinkedHashMap<>();
        for (Series series : seriesList) {
            if (series.getBrandId() == null) {
                continue;
            }
            seriesByBrand.computeIfAbsent(series.getBrandId(), ignored -> new java.util.ArrayList<>()).add(series);
        }

        Map<Long, List<Maker>> makersBySeries = new LinkedHashMap<>();
        for (Maker maker : makerList) {
            if (maker.getSeriesId() == null) {
                continue;
            }
            makersBySeries.computeIfAbsent(maker.getSeriesId(), ignored -> new java.util.ArrayList<>()).add(maker);
        }

        return brands.stream().map(brand -> {
            BrandTreeNodeVO brandNode = buildBrandNode(brand);

            List<BrandTreeNodeVO> seriesNodes = seriesByBrand.getOrDefault(brand.getId(), List.of()).stream().map(series -> {
                BrandTreeNodeVO seriesNode = buildSeriesNode(series, brand.getId());

                List<BrandTreeNodeVO> makerNodes = makersBySeries.getOrDefault(series.getId(), List.of()).stream().map(maker -> {
                    return buildMakerNode(maker, brand.getId(), series.getId());
                }).toList();
                seriesNode.setChildren(makerNodes);
                return seriesNode;
            }).toList();
            brandNode.setChildren(seriesNodes);
            return brandNode;
        }).toList();
    }

    @Override
    public BrandTreeNodeVO getTreeDetail(Long id) {
        Brand brand = this.getByIdNotDeleted(id);
        if (brand == null) {
            return null;
        }
        BrandTreeNodeVO brandNode = buildBrandNode(brand);
        List<Series> seriesList = seriesMapper.selectList(new QueryWrapper<Series>()
                .eq("brand_id", id)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .orderByAsc("id"));
        List<Long> seriesIds = seriesList.stream().map(Series::getId).filter(Objects::nonNull).toList();
        Map<Long, List<Maker>> makersBySeries = seriesIds.isEmpty()
                ? Map.of()
                : makerMapper.selectList(new QueryWrapper<Maker>()
                        .in("series_id", seriesIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .orderByAsc("id"))
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(Maker::getSeriesId, LinkedHashMap::new, java.util.stream.Collectors.toList()));
        List<BrandTreeNodeVO> seriesNodes = new ArrayList<>();
        for (Series series : seriesList) {
            BrandTreeNodeVO seriesNode = buildSeriesNode(series, id);
            List<BrandTreeNodeVO> makerNodes = makersBySeries.getOrDefault(series.getId(), List.of())
                    .stream()
                    .map(maker -> buildMakerNode(maker, id, series.getId()))
                    .toList();
            seriesNode.setChildren(makerNodes);
            seriesNodes.add(seriesNode);
        }
        brandNode.setChildren(seriesNodes);
        return brandNode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTree(BrandTreeSaveDTO dto) {
        Brand brand = saveBrandNode(dto);
        syncSeriesNodes(brand.getId(), dto.getSeries());
        return brand.getId();
    }

    private void fillChildren(Long brandId, BrandVO vo) {
        if (brandId == null || vo == null) {
            return;
        }
        List<Series> children = seriesMapper.selectList(new QueryWrapper<Series>()
                .eq("brand_id", brandId)
                .eq("deleted", 0)
                .orderByAsc("id"));
        List<Long> seriesIds = children.stream().map(Series::getId).toList();
        List<String> seriesNames = children.stream()
                .map(Series::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
        vo.setSeriesIds(seriesIds);
        vo.setSeriesNames(seriesNames);
        vo.setSeriesCount((long) seriesIds.size());
    }

    private Brand saveBrandNode(BrandTreeSaveDTO dto) {
        Brand entity = dto.getId() == null ? new Brand() : this.getByIdNotDeleted(dto.getId());
        if (dto.getId() != null && entity == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "brand does not exist");
        }
        entity.setName(trimToNull(dto.getName()));
        entity.setEnglishName(trimToNull(dto.getEnglishName()));
        entity.setImage(fileStorageService.normalize(UploadBizType.BRAND, dto.getImage()));
        entity.setContent(trimToNull(dto.getContent()));
        entity.setStatus(resolveStatus(dto.getStatus(), entity.getStatus()));
        boolean success = entity.getId() == null ? this.save(entity) : this.updateById(entity);
        if (!success || entity.getId() == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save brand tree");
        }
        return entity;
    }

    private void syncSeriesNodes(Long brandId, List<BrandTreeSeriesSaveDTO> submittedSeries) {
        List<Series> existingSeries = seriesMapper.selectList(new QueryWrapper<Series>()
                .eq("brand_id", brandId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .orderByAsc("id"));
        Map<Long, Series> existingSeriesMap = existingSeries.stream()
                .filter(item -> item.getId() != null)
                .collect(java.util.stream.Collectors.toMap(Series::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Set<Long> submittedSeriesIds = new HashSet<>();
        for (BrandTreeSeriesSaveDTO item : safeSeries(submittedSeries)) {
            Series series = item.getId() == null ? new Series() : existingSeriesMap.get(item.getId());
            if (item.getId() != null && series == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "series does not belong to brand");
            }
            series.setName(trimToNull(item.getName()));
            series.setEnglishName(trimToNull(item.getEnglishName()));
            series.setBrandId(brandId);
            series.setContent(trimToNull(item.getContent()));
            series.setStatus(resolveStatus(item.getStatus(), series.getStatus()));
            boolean success = series.getId() == null ? seriesService.save(series) : seriesService.updateById(series);
            if (!success || series.getId() == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save series");
            }
            submittedSeriesIds.add(series.getId());
            syncMakerNodes(brandId, series.getId(), item.getMakers());
        }
        for (Series existing : existingSeries) {
            if (existing.getId() != null && !submittedSeriesIds.contains(existing.getId())) {
                seriesService.deleteByIdLogic(existing.getId());
            }
        }
    }

    private void syncMakerNodes(Long brandId, Long seriesId, List<BrandTreeMakerSaveDTO> submittedMakers) {
        List<Maker> existingMakers = makerMapper.selectList(new QueryWrapper<Maker>()
                .eq("series_id", seriesId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .orderByAsc("id"));
        Map<Long, Maker> existingMakerMap = existingMakers.stream()
                .filter(item -> item.getId() != null)
                .collect(java.util.stream.Collectors.toMap(Maker::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Set<Long> submittedMakerIds = new HashSet<>();
        for (BrandTreeMakerSaveDTO item : safeMakers(submittedMakers)) {
            Maker maker = item.getId() == null ? new Maker() : existingMakerMap.get(item.getId());
            if (item.getId() != null && maker == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "maker does not belong to series");
            }
            maker.setName(trimToNull(item.getName()));
            maker.setEnglishName(trimToNull(item.getEnglishName()));
            maker.setSeriesId(seriesId);
            maker.setStatus(resolveStatus(item.getStatus(), maker.getStatus()));
            boolean success = maker.getId() == null ? makerService.save(maker) : makerService.updateById(maker);
            if (!success || maker.getId() == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save maker");
            }
            submittedMakerIds.add(maker.getId());
        }
        for (Maker existing : existingMakers) {
            if (existing.getId() != null && !submittedMakerIds.contains(existing.getId())) {
                makerService.deleteByIdLogic(existing.getId());
            }
        }
    }

    private List<BrandTreeSeriesSaveDTO> safeSeries(List<BrandTreeSeriesSaveDTO> source) {
        return source == null ? List.of() : source;
    }

    private List<BrandTreeMakerSaveDTO> safeMakers(List<BrandTreeMakerSaveDTO> source) {
        return source == null ? List.of() : source;
    }

    private Integer resolveStatus(co.handk.common.enums.StatusEnum status, Integer fallback) {
        if (status != null) {
            return status.getCode();
        }
        return fallback != null ? fallback : 1;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BrandTreeNodeVO buildBrandNode(Brand brand) {
        BrandTreeNodeVO brandNode = new BrandTreeNodeVO();
        brandNode.setId(brand.getId());
        brandNode.setName(brand.getName());
        brandNode.setEnglishName(brand.getEnglishName());
        brandNode.setImage(fileStorageService.toApiPath(UploadBizType.BRAND, brand.getImage()));
        brandNode.setContent(brand.getContent());
        brandNode.setNodeType("brand");
        brandNode.setStatus(brand.getStatus());
        return brandNode;
    }

    private BrandTreeNodeVO buildSeriesNode(Series series, Long brandId) {
        BrandTreeNodeVO seriesNode = new BrandTreeNodeVO();
        seriesNode.setId(series.getId());
        seriesNode.setName(series.getName());
        seriesNode.setEnglishName(series.getEnglishName());
        seriesNode.setContent(series.getContent());
        seriesNode.setNodeType("series");
        seriesNode.setBrandId(brandId);
        seriesNode.setStatus(series.getStatus());
        return seriesNode;
    }

    private BrandTreeNodeVO buildMakerNode(Maker maker, Long brandId, Long seriesId) {
        BrandTreeNodeVO makerNode = new BrandTreeNodeVO();
        makerNode.setId(maker.getId());
        makerNode.setName(maker.getName());
        makerNode.setEnglishName(maker.getEnglishName());
        makerNode.setNodeType("maker");
        makerNode.setBrandId(brandId);
        makerNode.setSeriesId(seriesId);
        makerNode.setStatus(maker.getStatus());
        return makerNode;
    }
}
