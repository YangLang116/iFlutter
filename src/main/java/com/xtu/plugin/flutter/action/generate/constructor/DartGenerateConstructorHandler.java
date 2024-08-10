package com.xtu.plugin.flutter.action.generate.constructor;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateConstructorHandler extends BaseDartGenerateHandler {

    @Override
    @NotNull
    protected String getTitle() {
        return "IFlutter.Constructor";
    }

    @Override
    protected boolean doAllowEmptySelection() {
        return true;
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass,
                                     @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(
                this.computeClassMembersMap(dartClass, false).values(),
                (component) -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }

    @Override
    @NotNull
    protected DartGenerateConstructorCodeFix createFix(@NotNull DartClass dartClass) {
        Project project = dartClass.getProject();
        ProjectStorageEntity storageEntity = ProjectStorageService.getStorage(project);
        return new DartGenerateConstructorCodeFix(storageEntity.supportNullSafety, dartClass);
    }
}
