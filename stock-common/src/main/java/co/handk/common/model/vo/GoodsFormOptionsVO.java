package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsFormOptionsVO {
    private List<OptionVO> brandOptions;
    private List<OptionVO> seriesOptions;
    private List<OptionVO> categoryOptions;
    private List<OptionVO> makerOptions;
    private List<TextOptionVO> statusOptions;
    private List<TextOptionVO> skuStatusOptions;
    private List<TextOptionVO> currencyOptions;
}
