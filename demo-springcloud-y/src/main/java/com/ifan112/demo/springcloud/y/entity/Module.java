package com.ifan112.demo.springcloud.y.entity;

import javax.persistence.*;

@Entity
@Table(name = "module")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "artifact_id")
    private String artifactId;

    private String version;

    private String packing;

    @Column(name = "parent_module_id")
    private Integer parentModuleId;

    public Module() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPacking() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing = packing;
    }

    public Integer getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(Integer parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", packing='" + packing + '\'' +
                ", parentModuleId=" + parentModuleId +
                '}';
    }
}
