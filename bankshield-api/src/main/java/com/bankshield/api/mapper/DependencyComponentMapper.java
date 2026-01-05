package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DependencyComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DependencyComponentMapper extends BaseMapper<DependencyComponent> {

    @Select("SELECT * FROM dependency_component WHERE component_type = #{type} ORDER BY vulnerability_count DESC")
    List<DependencyComponent> selectByType(@Param("type") String type);

    @Select("SELECT * FROM dependency_component WHERE vulnerability_count > 0 ORDER BY highest_severity, vulnerability_count DESC")
    List<DependencyComponent> selectWithVulnerabilities();

    @Select("SELECT COUNT(*) FROM dependency_component WHERE vulnerability_count > 0")
    int countVulnerableComponents();
}
