package com.xtu.plugin.flutter.store;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

@State(name = "iFlutter", storages = {@Storage("iFlutter.xml")})
public class StorageService implements PersistentStateComponent<StorageEntity> {

    private StorageEntity storageEntity = new StorageEntity();

    public static StorageService getInstance(Project project) {
        return project.getService(StorageService.class);
    }

    @Override
    public @NotNull
    StorageEntity getState() {
        return storageEntity;
    }

    @Override
    public void loadState(@NotNull StorageEntity state) {
        this.storageEntity = state;
    }
}
