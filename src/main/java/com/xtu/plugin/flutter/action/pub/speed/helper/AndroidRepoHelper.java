package com.xtu.plugin.flutter.action.pub.speed.helper;

import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.store.ide.IdeStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AndroidRepoHelper {

    private static final String REPO_SPLIT = ",";
    //默认镜像仓库地址
    private static final List<String> DEFAULT_MIRROR_REPO = Arrays.asList("https://maven.aliyun.com/repository/gradle-plugin",
            "https://maven.aliyun.com/repository/public",
            "https://maven.aliyun.com/repository/google",
            "https://maven.aliyun.com/repository/central",
            "https://maven.aliyun.com/repository/jcenter");

    @Nullable
    public static List<String> getRepoList() {
        String mirrorRepoStr = IdeStorageService.getStorage().mirrorRepoStr;
        if (StringUtils.isEmpty(mirrorRepoStr)) return null;
        return Arrays.asList(mirrorRepoStr.split(REPO_SPLIT));
    }

    @NotNull
    public static String getRepoStr(@Nullable List<String> repoList) {
        if (repoList == null) return "";
        return StringUtils.join(repoList, REPO_SPLIT);
    }

    @NotNull
    public static String getDefaultRepoStr() {
        return StringUtils.join(DEFAULT_MIRROR_REPO, REPO_SPLIT);
    }
}
