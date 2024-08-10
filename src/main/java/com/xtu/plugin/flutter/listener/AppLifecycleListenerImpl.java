package com.xtu.plugin.flutter.listener;

import com.intellij.ide.AppLifecycleListener;
import com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import org.jetbrains.annotations.NotNull;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.util.List;

public class AppLifecycleListenerImpl implements AppLifecycleListener {

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new WebPImageReaderSpi(), ImageReaderSpi.class);
        registry.registerServiceProvider(new SVGImageReaderSpi(), ImageReaderSpi.class);
    }

}
