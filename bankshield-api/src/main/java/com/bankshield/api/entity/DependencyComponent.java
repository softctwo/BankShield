package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dependency_component")
public class DependencyComponent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String componentName;
    private String componentType; // MAVEN/NPM/PIP/NUGET
    private String currentVersion;
    private String latestVersion;
    private String groupId;
    private String artifactId;
    private String packageName;
    private String license;
    private String description;
    private String homepageUrl;
    private String repositoryUrl;
    private Integer vulnerabilityCount;
    private String highestSeverity;
    private LocalDateTime lastScanTime;
    private String scanStatus;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
