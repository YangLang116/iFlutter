package com.xtu.plugin.flutter.store.project;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;

@State(name = "iFlutter", storages = {@Storage("iFlutter.xml")})
public class ProjectStorageService implements PersistentStateComponent<ProjectStorageEntity> {

    private ProjectStorageEntity storageEntity = new ProjectStorageEntity();

    public static ProjectStorageService getInstance(Project project) {
        return project.getService(ProjectStorageService.class);
    }

    @Override
    public @NotNull
    ProjectStorageEntity getState() {
        return storageEntity;
    }

    @Override
    public void loadState(@NotNull ProjectStorageEntity state) {
        this.storageEntity = state;
    }
}
