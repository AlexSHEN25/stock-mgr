package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class MasterRelationOptionsVO {
    private List<OptionVO> brandOptions;
    private List<OptionVO> seriesOptions;
    private List<OptionVO> makerOptions;
}
