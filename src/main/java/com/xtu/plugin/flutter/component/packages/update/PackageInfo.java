package com.xtu.plugin.flutter.component.packages.update;

import java.util.Locale;
import java.util.Objects;

public class PackageInfo {

    public String name;
    public String currentVersion;
    public String latestVersion;

    public PackageInfo(String name, String currentVersion, String latestVersion) {
        this.name = name;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %s -> %s", name, currentVersion, latestVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageInfo that = (PackageInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(currentVersion, that.currentVersion) && Objects.equals(latestVersion, that.latestVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, currentVersion, latestVersion);
    }
}
