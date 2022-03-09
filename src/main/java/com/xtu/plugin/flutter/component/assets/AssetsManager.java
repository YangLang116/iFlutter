package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.xtu.plugin.flutter.action.generate.res.GenerateResAction;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 处理flutter项目中的资源文件
 */
public class AssetsManager {

    private final Project project;
    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;

    public AssetsManager(@NotNull Project project) {
        this.project = project;
        specFileHandler = new PubSpecFileHandler();
        assetFileHandler = new AssetFileHandler(specFileHandler);
    }

    public void attach() {
        LogUtils.info("AssetsManager attach");
        PsiManager.getInstance(project).addPsiTreeChangeListener(psiTreeChangeListener);
    }

    public void detach() {
        LogUtils.info("AssetsManager detach");
        PsiManager.getInstance(project).removePsiTreeChangeListener(psiTreeChangeListener);
    }

    private final PsiTreeChangeListener psiTreeChangeListener = new PsiTreeChangeListener() {

        private boolean disableResCheck() {
            return !StorageService.getInstance(project).getState()
                    .resCheckEnable;
        }
        private boolean disableUpdatePubsepc() {
            return !StorageService.getInstance(project).getState()
                    .updatePubsepc;
        }

        @Override
        public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
        }

        @Override
        public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
        }

        @Override
        public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {
        }

        @Override
        public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
        }

        @Override
        public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
        }

        @Override
        public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

        }

        @Override
        public void childAdded(@NotNull PsiTreeChangeEvent event) {
            if (disableResCheck()) return;
            if (checkDisableUpdatePubsepc(event)) {
                return;
            }
            if (event.getChild() instanceof PsiFile) {
                assetFileHandler.onPsiFileAdded((PsiFile) event.getChild());
            }
        }

        @Override
        public void childRemoved(@NotNull PsiTreeChangeEvent event) {
            if (disableResCheck()) return;
            if (checkDisableUpdatePubsepc(event)) {
                return;
            }
            if (event.getChild() instanceof PsiFile) {
                assetFileHandler.onPsiFileRemoved((PsiFile) event.getChild());
            }
        }

        private boolean checkDisableUpdatePubsepc(@NotNull PsiTreeChangeEvent event) {
            if (disableUpdatePubsepc()){
                PsiElement parent = event.getParent();
                if (parent instanceof PsiDirectory){
                    PsiDirectory psiDirectory = (PsiDirectory)parent;
                    if (PluginUtils.supportAssetFoldName(psiDirectory.getProject()).contains(psiDirectory.getName())) {
                        GenerateResAction.generateFile(parent.getProject());
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void childReplaced(@NotNull PsiTreeChangeEvent event) {

        }

        @Override
        public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
            if (disableResCheck()) return;
            if (checkDisableUpdatePubsepc(event)) {
                return;
            }
            if (event.getParent() instanceof PsiFile) {
                specFileHandler.onPsiFileChanged((PsiFile) event.getParent());
            }
        }

        @Override
        public void childMoved(@NotNull PsiTreeChangeEvent event) {

        }

        @Override
        public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
            if (disableResCheck()||disableUpdatePubsepc()) return;
            if (checkDisableUpdatePubsepc(event)) {
                return;
            }
            if (PsiTreeChangeEvent.PROP_FILE_NAME.equals(event.getPropertyName())) {
                assetFileHandler.onPsiFileChanged((PsiFile) event.getElement(), (String) event.getOldValue(), (String) event.getNewValue());
            }
        }
    };


}
