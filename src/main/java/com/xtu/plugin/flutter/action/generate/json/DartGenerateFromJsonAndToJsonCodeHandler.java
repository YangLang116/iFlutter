package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.xtu.plugin.flutter.store.StorageEntity;
import com.xtu.plugin.flutter.store.StorageService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateFromJsonAndToJsonCodeHandler extends BaseDartGenerateHandler {

    private final boolean hasFromJson;
    private final boolean hasToJson;

    public DartGenerateFromJsonAndToJsonCodeHandler(boolean hasFromJson, boolean hasToJson) {
        this.hasFromJson = hasFromJson;
        this.hasToJson = hasToJson;
    }

    @NotNull
    protected String getTitle() {
        return "IFlutter.FromJsonAndToJson";
    }

    @NotNull
    protected BaseCreateMethodsFix<DartComponent> createFix(@NotNull DartClass dartClass) {
        Project project = dartClass.getProject();
        StorageEntity storageEntity = StorageService.getInstance(project).getState();
        return new CreateFromJsonAndToJsonCodeFix(dartClass,
                storageEntity.flutter2Enable, storageEntity.isUnModifiableFromJson,
                this.hasFromJson, hasToJson);
    }

    protected void collectCandidates(@NotNull DartClass dartClass, @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(
                this.computeClassMembersMap(dartClass, false).values(),
                (component) -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }

    protected boolean doAllowEmptySelection() {
        return true;
    }
}
