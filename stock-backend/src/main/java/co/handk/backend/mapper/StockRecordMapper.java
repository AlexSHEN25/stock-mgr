package co.handk.backend.mapper;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.query.CustomerStockQueryDTO;
import co.handk.common.model.vo.CustomerGoodsStockDetailVO;
import co.handk.common.model.vo.CustomerGoodsStockVO;
import co.handk.common.model.vo.CustomerGoodsMatrixCellVO;
import co.handk.common.model.vo.CustomerGoodsMatrixColumnVO;
import co.handk.common.model.vo.CustomerGoodsMatrixRowVO;
import co.handk.common.model.vo.CustomerOutboundTreeNodeVO;
import co.handk.common.model.vo.CustomerStockSummaryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {

    long countCustomerSummaries(@Param("query") CustomerStockQueryDTO query,
                                @Param("deptId") Long deptId,
                                @Param("ownerUserId") Long ownerUserId);

    List<CustomerStockSummaryVO> selectCustomerSummaries(@Param("query") CustomerStockQueryDTO query,
                                                         @Param("deptId") Long deptId,
                                                         @Param("ownerUserId") Long ownerUserId,
                                                         @Param("offset") long offset,
                                                         @Param("limit") long limit);

    long countCustomerGoods(@Param("query") CustomerStockQueryDTO query,
                            @Param("deptId") Long deptId,
                            @Param("ownerUserId") Long ownerUserId);

    List<CustomerGoodsStockVO> selectCustomerGoods(@Param("query") CustomerStockQueryDTO query,
                                                   @Param("deptId") Long deptId,
                                                   @Param("ownerUserId") Long ownerUserId,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countCustomerGoodsDetails(@Param("query") CustomerStockQueryDTO query,
                                   @Param("deptId") Long deptId,
                                   @Param("ownerUserId") Long ownerUserId);

    List<CustomerGoodsStockDetailVO> selectCustomerGoodsDetails(@Param("query") CustomerStockQueryDTO query,
                                                                @Param("deptId") Long deptId,
                                                                @Param("ownerUserId") Long ownerUserId,
                                                                @Param("offset") long offset,
                                                                @Param("limit") long limit);

    long countCustomerDeliveryScheduleDetails(@Param("query") CustomerStockQueryDTO query,
                                              @Param("deptId") Long deptId,
                                              @Param("ownerUserId") Long ownerUserId);

    List<CustomerGoodsStockDetailVO> selectCustomerDeliveryScheduleDetails(@Param("query") CustomerStockQueryDTO query,
                                                                           @Param("deptId") Long deptId,
                                                                           @Param("ownerUserId") Long ownerUserId,
                                                                           @Param("offset") long offset,
                                                                           @Param("limit") long limit);

    long countCustomerDeliveryScheduleTreeCountries(@Param("query") CustomerStockQueryDTO query,
                                                    @Param("deptId") Long deptId,
                                                    @Param("ownerUserId") Long ownerUserId);

    List<String> selectCustomerDeliveryScheduleTreeCountries(@Param("query") CustomerStockQueryDTO query,
                                                             @Param("deptId") Long deptId,
                                                             @Param("ownerUserId") Long ownerUserId,
                                                             @Param("offset") long offset,
                                                             @Param("limit") long limit);

    List<CustomerOutboundTreeNodeVO> selectCustomerDeliveryScheduleTreeDetails(
            @Param("query") CustomerStockQueryDTO query,
            @Param("deptId") Long deptId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("countries") List<String> countries);

    long countCustomerGoodsMatrixRows(@Param("query") CustomerStockQueryDTO query,
                                      @Param("deptId") Long deptId,
                                      @Param("ownerUserId") Long ownerUserId);

    List<CustomerGoodsMatrixColumnVO> selectCustomerGoodsMatrixColumns(
            @Param("query") CustomerStockQueryDTO query,
            @Param("deptId") Long deptId,
            @Param("ownerUserId") Long ownerUserId);

    List<CustomerGoodsMatrixRowVO> selectCustomerGoodsMatrixRows(
            @Param("query") CustomerStockQueryDTO query,
            @Param("deptId") Long deptId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("offset") long offset,
            @Param("limit") long limit);

    List<CustomerGoodsMatrixCellVO> selectCustomerGoodsMatrixCells(
            @Param("query") CustomerStockQueryDTO query,
            @Param("deptId") Long deptId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("goodsIds") List<Long> goodsIds);

    long countCustomerGoodsTreeCountries(@Param("query") CustomerStockQueryDTO query,
                                         @Param("deptId") Long deptId,
                                         @Param("ownerUserId") Long ownerUserId);

    List<String> selectCustomerGoodsTreeCountries(@Param("query") CustomerStockQueryDTO query,
                                                  @Param("deptId") Long deptId,
                                                  @Param("ownerUserId") Long ownerUserId,
                                                  @Param("offset") long offset,
                                                  @Param("limit") long limit);

    List<CustomerOutboundTreeNodeVO> selectCustomerGoodsTreeDetails(
            @Param("query") CustomerStockQueryDTO query,
            @Param("deptId") Long deptId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("countries") List<String> countries);
}
