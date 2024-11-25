package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateJSONMethodHandler extends BaseDartGenerateHandler {

    private final boolean createFromJson;
    private final boolean createToJson;

    public DartGenerateJSONMethodHandler(boolean createFromJson, boolean createToJson) {
        this.createFromJson = createFromJson;
        this.createToJson = createToJson;
    }

    @NotNull
    protected String getTitle() {
        return "IFlutter.FromJsonAndToJson";
    }

    @NotNull
    protected BaseCreateMethodsFix<DartComponent> createFix(@NotNull DartClass dartClass) {
        Project project = dartClass.getProject();
        ProjectStorageEntity storageEntity = ProjectStorageService.getStorage(project);
        return new DartGenerateJsonMethodCodeFix(dartClass,
                storageEntity.supportNullSafety,
                this.createFromJson, createToJson);
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
