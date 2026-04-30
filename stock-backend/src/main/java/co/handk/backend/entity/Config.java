package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Config extends BaseEntity {

    private String name;

    private String group;

    private String title;

    private String tip;

    private String type;

    private String value;

    private String content;
}
