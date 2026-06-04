package co.handk.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class PermissionScopeVO implements Serializable {

    private Set<String> menuCodes = new HashSet<>();

    private Set<String> permissionCodes = new HashSet<>();

    private Set<String> roleCodes = new HashSet<>();

    private boolean superAdmin;

    private boolean allDataWrite;

    private List<MenuPermissionVO> menus = new ArrayList<>();

    @Data
    public static class MenuPermissionVO implements Serializable {
        private String key;
        private String label;
        private String module;
        private String path;
        private String dataScope;
        private Integer sort;
        private ActionPermissionVO actions = new ActionPermissionVO();
    }

    @Data
    public static class ActionPermissionVO implements Serializable {
        private boolean read;
        private boolean create;
        private boolean edit;
        private boolean delete;
        private boolean batchDelete;
        private boolean inlineEdit;
    }
}
