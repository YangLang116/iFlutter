package com.xtu.plugin.flutter.store.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.xtu.plugin.flutter.store.ide.entity.IdeStorageEntity;
import org.jetbrains.annotations.NotNull;

@State(name = "iFlutter", storages = {@Storage("idea.iFlutter.xml")})
public class IdeStorageService implements PersistentStateComponent<IdeStorageEntity> {

    private IdeStorageEntity storageEntity = new IdeStorageEntity();

    public static IdeStorageEntity getStorage() {
        return ApplicationManager.getApplication()
                .getService(IdeStorageService.class)
                .getState();
    }

    @Override
    @NotNull
    public IdeStorageEntity getState() {
        return storageEntity;
    }

    @Override
    public void loadState(@NotNull IdeStorageEntity state) {
        this.storageEntity = state;
    }
}
