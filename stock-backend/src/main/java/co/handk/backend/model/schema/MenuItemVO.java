package co.handk.backend.model.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MenuItemVO {
    private Long menuId;
    private Long parentId;
    private String name;
    private Integer sort;
    private String path;
    private List<MenuItemVO> children;
}
