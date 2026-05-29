package com.xtu.plugin.flutter.component.assets.handler;

import java.util.List;

/**
 * 表示对 pubspec.yaml 资源列表的一次原子变更操作。
 * 多个 AssetMutation 可以合并到一次 WriteCommandAction 中批量执行，
 * 避免每个文件事件都单独触发 pubspec.yaml 的读-改-写。
 */
@FunctionalInterface
public interface AssetMutation {
    void apply(List<String> assetList, List<String> fontList);
}
