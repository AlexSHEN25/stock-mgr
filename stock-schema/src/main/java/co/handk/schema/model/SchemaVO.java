package co.handk.schema.model;

import lombok.Data;

import java.util.List;

/**
 * Schema 返回给前端的数据结构
 */
@Data
public class SchemaVO {

    private String resource;
    private String name;
    /**
     * Top-level menu name (e.g. システム管理)
     */
    private String group;
    /**
     * Second-level menu name (e.g. ユーザー管理)
     */
    private String subGroup;
    /**
     * Raw group path from annotation (e.g. システム管理/ユーザー管理)
     */
    private String groupPath;
    /**
     * Current schema menu id (leaf)
     */
    private String menuId;
    /**
     * Parent menu id
     */
    private String parentId;
    /**
     * Top menu id
     */
    private String topMenuId;
    /**
     * Second-level menu id
     */
    private String subMenuId;

    private List<FieldVO> fields;
    private List<ActionVO> actions;

    @Data
    public static class FieldVO {
        private String name;
        private String title;
        private String type;
        private boolean required;
        private boolean table;
        private boolean search;
        private String dict;
        private String ref;

        private String refLabelField;
        private String refValueField;

        private String refDisplayField;
    }

    @Data
    public static class ActionVO {
        private String code;
        private String name;
        private String api;
        private String method;
    }
}
