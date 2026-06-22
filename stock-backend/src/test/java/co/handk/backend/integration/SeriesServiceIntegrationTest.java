package co.handk.backend.integration;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.query.SeriesQueryDTO;
import co.handk.common.model.vo.SeriesVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SeriesServiceIntegrationTest {

    private static final Long ADMIN_USER_ID = 1L;

    @Autowired
    private SeriesService seriesService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private MakerService makerService;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(ADMIN_USER_ID);
    }

    @Test
    void pageDoesNotRequireLegacySeriesBrandColumn() {
        Series series = new Series();
        series.setName("series-page-check");
        series.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(seriesService.save(series));

        SeriesQueryDTO query = new SeriesQueryDTO();
        query.setPageNum(1L);
        query.setPageSize(20L);
        PageResult<SeriesVO> result = seriesService.page(query);

        assertTrue(result.getRecords().stream().anyMatch(item -> series.getId().equals(item.getId())));
    }

    @Test
    void pageCanFilterByBrandThroughDirectParentColumn() {
        Brand brand = new Brand();
        brand.setName("series-filter-brand");
        assertTrue(brandService.save(brand));

        Series series = new Series();
        series.setName("series-filter-target");
        series.setBrandId(brand.getId());
        series.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(seriesService.save(series));

        Maker maker = new Maker();
        maker.setName("series-filter-maker");
        maker.setSeriesId(series.getId());
        maker.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(makerService.save(maker));

        SeriesQueryDTO query = new SeriesQueryDTO();
        query.setPageNum(1L);
        query.setPageSize(20L);
        query.setBrandId(brand.getId());
        PageResult<SeriesVO> result = seriesService.page(query);

        assertEquals(1, result.getRecords().stream().filter(item -> series.getId().equals(item.getId())).count());
        SeriesVO record = result.getRecords().stream()
                .filter(item -> series.getId().equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(brand.getId(), record.getBrandId());
    }
}
