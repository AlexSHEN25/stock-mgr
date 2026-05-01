package co.handk.backend.builder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class JoinQueryWrapper<T> extends QueryWrapper<T> {

    private String joinSql;

    public JoinQueryWrapper<T> join(String joinSql) {
        this.joinSql = joinSql;
        return this;
    }

    public String getJoinSql() {
        return joinSql;
    }
}
