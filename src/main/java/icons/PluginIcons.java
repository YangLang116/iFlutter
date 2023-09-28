package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.net.URL;

public class PluginIcons {

    public static Icon GITHUB = IconLoader.getIcon("/icons/github.png", PluginIcons.class);
    public static Icon STAR = IconLoader.getIcon("/icons/star.png", PluginIcons.class);
    public static Icon LOGO = IconLoader.getIcon("/icons/logo.svg", PluginIcons.class);
    public static Icon WINDOW_ICON = IconLoader.getIcon("/icons/window_icon.svg", PluginIcons.class);

    public static URL FAIL_PIC = PluginIcons.class.getResource("/icons/preview_failed.png");
}
