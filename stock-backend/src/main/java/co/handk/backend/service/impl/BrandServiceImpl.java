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
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.BrandHierarchySaveDTO;
import co.handk.common.model.dto.BrandTreeMakerSaveDTO;
import co.handk.common.model.dto.BrandTreeSaveDTO;
import co.handk.common.model.dto.BrandTreeSeriesSaveDTO;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandHierarchyQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandHierarchyVO;
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
    public PageResult<BrandHierarchyVO> pageHierarchy(BrandHierarchyQueryDTO query) {
        long total = baseMapper.countHierarchy(query);
        List<BrandHierarchyVO> records = total == 0
                ? List.of()
                : baseMapper.selectHierarchy(query, pageOffset(query), query.getPageSize());
        return PageResult.build(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public BrandHierarchyVO getHierarchy(String key) {
        HierarchyKey parsed = parseHierarchyKey(key);
        return switch (parsed.nodeType()) {
            case "maker" -> hierarchyForMaker(parsed.id());
            case "series" -> hierarchyForSeries(parsed.id());
            case "brand" -> hierarchyForBrand(parsed.id());
            default -> throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "invalid hierarchy node type");
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BrandHierarchyVO saveHierarchy(BrandHierarchySaveDTO dto) {
        Brand brand = findOrCreateBrand(dto);
        Series series = findOrCreateSeries(brand.getId(), dto);
        Maker maker = findOrCreateMaker(series.getId(), dto);
        return hierarchyForMaker(maker.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BrandHierarchyVO updateHierarchy(BrandHierarchySaveDTO dto) {
        HierarchyKey key = parseHierarchyKey(dto.getId());
        return switch (key.nodeType()) {
            case "maker" -> updateMakerHierarchy(key.id(), dto);
            case "series" -> updateSeriesHierarchy(key.id(), dto);
            case "brand" -> updateBrandHierarchy(key.id(), dto);
            default -> throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "invalid hierarchy node type");
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteHierarchy(String key) {
        HierarchyKey parsed = parseHierarchyKey(key);
        return switch (parsed.nodeType()) {
            case "maker" -> deleteMakerHierarchy(parsed.id());
            case "series" -> deleteSeriesHierarchy(parsed.id());
            case "brand" -> deleteBrandHierarchy(parsed.id());
            default -> throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "invalid hierarchy node type");
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTree(BrandTreeSaveDTO dto) {
        Brand brand = saveBrandNode(dto);
        syncSeriesNodes(brand.getId(), dto.getSeries());
        return brand.getId();
    }

    private Brand findOrCreateBrand(BrandHierarchySaveDTO dto) {
        String name = requireText(dto.getBrandName(), "brand name is required");
        String englishName = trimToNull(dto.getBrandEnglishName());
        Brand brand = dto.getBrandId() == null ? findBrandByName(name, englishName) : this.getByIdNotDeleted(dto.getBrandId());
        if (brand == null) {
            brand = new Brand();
        }
        brand.setName(name);
        brand.setEnglishName(englishName);
        brand.setStatus(resolveStatus(dto.getStatus(), brand.getStatus()));
        boolean success = brand.getId() == null ? this.save(brand) : this.updateById(brand);
        if (!success || brand.getId() == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save brand");
        }
        return brand;
    }

    private Series findOrCreateSeries(Long brandId, BrandHierarchySaveDTO dto) {
        String name = requireText(dto.getSeriesName(), "series name is required");
        String englishName = trimToNull(dto.getSeriesEnglishName());
        Series series = dto.getSeriesId() == null ? findSeriesByName(brandId, name, englishName) : getSeriesNotDeleted(dto.getSeriesId());
        if (series == null) {
            series = new Series();
        }
        series.setName(name);
        series.setEnglishName(englishName);
        series.setBrandId(brandId);
        series.setStatus(resolveStatus(dto.getStatus(), series.getStatus()));
        boolean success = series.getId() == null ? seriesService.save(series) : seriesService.updateById(series);
        if (!success || series.getId() == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save series");
        }
        return series;
    }

    private Maker findOrCreateMaker(Long seriesId, BrandHierarchySaveDTO dto) {
        String name = requireText(dto.getMakerName(), "maker name is required");
        String englishName = trimToNull(dto.getMakerEnglishName());
        Maker maker = dto.getMakerId() == null ? findMakerByName(seriesId, name, englishName) : getMakerNotDeleted(dto.getMakerId());
        if (maker == null) {
            maker = new Maker();
        }
        maker.setName(name);
        maker.setEnglishName(englishName);
        maker.setSeriesId(seriesId);
        maker.setStatus(resolveStatus(dto.getStatus(), maker.getStatus()));
        boolean success = maker.getId() == null ? makerService.save(maker) : makerService.updateById(maker);
        if (!success || maker.getId() == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to save maker");
        }
        return maker;
    }

    private BrandHierarchyVO updateMakerHierarchy(Long makerId, BrandHierarchySaveDTO dto) {
        Maker maker = requireMaker(makerId);
        Series series = requireSeries(maker.getSeriesId());
        Brand brand = requireBrand(series.getBrandId());
        applyBrand(brand, dto);
        applySeries(series, brand.getId(), dto);
        applyMaker(maker, series.getId(), dto);
        return hierarchyForMaker(maker.getId());
    }

    private BrandHierarchyVO updateSeriesHierarchy(Long seriesId, BrandHierarchySaveDTO dto) {
        Series series = requireSeries(seriesId);
        Brand brand = requireBrand(series.getBrandId());
        applyBrand(brand, dto);
        applySeries(series, brand.getId(), dto);
        return hierarchyForSeries(series.getId());
    }

    private BrandHierarchyVO updateBrandHierarchy(Long brandId, BrandHierarchySaveDTO dto) {
        Brand brand = requireBrand(brandId);
        applyBrand(brand, dto);
        return hierarchyForBrand(brand.getId());
    }

    private void applyBrand(Brand brand, BrandHierarchySaveDTO dto) {
        brand.setName(requireText(dto.getBrandName(), "brand name is required"));
        brand.setEnglishName(trimToNull(dto.getBrandEnglishName()));
        brand.setStatus(resolveStatus(dto.getStatus(), brand.getStatus()));
        if (!this.updateById(brand)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to update brand");
        }
    }

    private void applySeries(Series series, Long brandId, BrandHierarchySaveDTO dto) {
        series.setName(requireText(dto.getSeriesName(), "series name is required"));
        series.setEnglishName(trimToNull(dto.getSeriesEnglishName()));
        series.setBrandId(brandId);
        series.setStatus(resolveStatus(dto.getStatus(), series.getStatus()));
        if (!seriesService.updateById(series)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to update series");
        }
    }

    private void applyMaker(Maker maker, Long seriesId, BrandHierarchySaveDTO dto) {
        maker.setName(requireText(dto.getMakerName(), "maker name is required"));
        maker.setEnglishName(trimToNull(dto.getMakerEnglishName()));
        maker.setSeriesId(seriesId);
        maker.setStatus(resolveStatus(dto.getStatus(), maker.getStatus()));
        if (!makerService.updateById(maker)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "failed to update maker");
        }
    }

    private boolean deleteMakerHierarchy(Long makerId) {
        Maker maker = getMakerNotDeleted(makerId);
        if (maker == null) {
            return true;
        }
        makerService.deleteByIdLogic(makerId);
        return true;
    }

    private boolean deleteSeriesHierarchy(Long seriesId) {
        Series series = getSeriesNotDeleted(seriesId);
        if (series == null) {
            return true;
        }
        seriesService.deleteByIdLogic(seriesId);
        return true;
    }

    private boolean deleteBrandHierarchy(Long brandId) {
        Brand brand = this.getByIdNotDeleted(brandId);
        if (brand == null) {
            return true;
        }
        deleteByIdLogic(brandId);
        return true;
    }

    private BrandHierarchyVO hierarchyForMaker(Long makerId) {
        Maker maker = requireMaker(makerId);
        Series series = requireSeries(maker.getSeriesId());
        Brand brand = requireBrand(series.getBrandId());
        BrandHierarchyVO vo = baseHierarchy(brand, series, maker);
        vo.setId("maker:" + maker.getId());
        vo.setNodeType("maker");
        return vo;
    }

    private BrandHierarchyVO hierarchyForSeries(Long seriesId) {
        Series series = requireSeries(seriesId);
        Brand brand = requireBrand(series.getBrandId());
        BrandHierarchyVO vo = baseHierarchy(brand, series, null);
        vo.setId("series:" + series.getId());
        vo.setNodeType("series");
        return vo;
    }

    private BrandHierarchyVO hierarchyForBrand(Long brandId) {
        Brand brand = requireBrand(brandId);
        BrandHierarchyVO vo = baseHierarchy(brand, null, null);
        vo.setId("brand:" + brand.getId());
        vo.setNodeType("brand");
        return vo;
    }

    private BrandHierarchyVO baseHierarchy(Brand brand, Series series, Maker maker) {
        BrandHierarchyVO vo = new BrandHierarchyVO();
        vo.setBrandId(brand.getId());
        vo.setBrandName(brand.getName());
        vo.setBrandEnglishName(brand.getEnglishName());
        if (series != null) {
            vo.setSeriesId(series.getId());
            vo.setSeriesName(series.getName());
            vo.setSeriesEnglishName(series.getEnglishName());
        }
        if (maker != null) {
            vo.setMakerId(maker.getId());
            vo.setMakerName(maker.getName());
            vo.setMakerEnglishName(maker.getEnglishName());
        }
        vo.setStatus(maker != null ? maker.getStatus() : series != null ? series.getStatus() : brand.getStatus());
        return vo;
    }

    private long pageOffset(BrandHierarchyQueryDTO query) {
        return (query.getPageNum() - 1L) * query.getPageSize();
    }

    private Brand findBrandByName(String name, String englishName) {
        QueryWrapper<Brand> wrapper = new QueryWrapper<Brand>()
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1");
        if (englishName == null) {
            wrapper.isNull("english_name");
        } else {
            wrapper.eq("english_name", englishName);
        }
        return this.getOne(wrapper);
    }

    private Series findSeriesByName(Long brandId, String name, String englishName) {
        QueryWrapper<Series> wrapper = new QueryWrapper<Series>()
                .eq("brand_id", brandId)
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1");
        if (englishName == null) {
            wrapper.isNull("english_name");
        } else {
            wrapper.eq("english_name", englishName);
        }
        return seriesMapper.selectOne(wrapper);
    }

    private Maker findMakerByName(Long seriesId, String name, String englishName) {
        QueryWrapper<Maker> wrapper = new QueryWrapper<Maker>()
                .eq("series_id", seriesId)
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1");
        if (englishName == null) {
            wrapper.isNull("english_name");
        } else {
            wrapper.eq("english_name", englishName);
        }
        return makerMapper.selectOne(wrapper);
    }

    private Brand requireBrand(Long brandId) {
        Brand brand = brandId == null ? null : this.getByIdNotDeleted(brandId);
        if (brand == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "brand does not exist");
        }
        return brand;
    }

    private Series requireSeries(Long seriesId) {
        Series series = getSeriesNotDeleted(seriesId);
        if (series == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "series does not exist");
        }
        return series;
    }

    private Maker requireMaker(Long makerId) {
        Maker maker = getMakerNotDeleted(makerId);
        if (maker == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "maker does not exist");
        }
        return maker;
    }

    private Series getSeriesNotDeleted(Long seriesId) {
        return seriesId == null ? null : seriesMapper.selectOne(new QueryWrapper<Series>()
                .eq("id", seriesId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
    }

    private Maker getMakerNotDeleted(Long makerId) {
        return makerId == null ? null : makerMapper.selectOne(new QueryWrapper<Maker>()
                .eq("id", makerId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
    }

    private HierarchyKey parseHierarchyKey(String key) {
        if (key == null || key.isBlank() || !key.contains(":")) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "invalid hierarchy id");
        }
        String[] parts = key.split(":", 2);
        try {
            return new HierarchyKey(parts[0].trim(), Long.parseLong(parts[1].trim()));
        } catch (NumberFormatException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "invalid hierarchy id");
        }
    }

    private String requireText(String value, String message) {
        String text = trimToNull(value);
        if (text == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, message);
        }
        return text;
    }

    private record HierarchyKey(String nodeType, Long id) {
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

    private Integer resolveStatus(Integer status, Integer fallback) {
        return status != null ? status : fallback != null ? fallback : 1;
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
