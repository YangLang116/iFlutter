package template;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.generate.constructor.entity.GenConstructorFieldDescriptor;
import com.xtu.plugin.flutter.action.generate.json.entity.GenJSONMethodFieldDescriptor;
import com.xtu.plugin.flutter.action.j2d.handler.entity.J2DFieldDescriptor;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import template.data.*;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;

public class PluginTemplate {

    private static final Configuration configuration;

    static {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassLoaderForTemplateLoading(PluginTemplate.class.getClassLoader(), "/ftl");
    }

    @NotNull
    private static String process(@NotNull Template template, @NotNull Object data) throws TemplateException, IOException {
        CharArrayWriter writer = new CharArrayWriter();
        template.process(data, writer);
        return writer.toString().replace("\r", "");
    }

    @NotNull
    private static String getNullKey(@NotNull Project project) {
        boolean supportNullSafety = ProjectStorageService.getStorage(project).supportNullSafety;
        return supportNullSafety ? "?" : "";
    }

    @NotNull
    static String isUnModifiableList(@NotNull Project project) {
        boolean isUnModifiable = ProjectStorageService.getStorage(project).isUnModifiableFromJson;
        return isUnModifiable ? "unmodifiable" : "from";
    }

    @NotNull
    public static String getResFileContent(@NotNull ResFileTemplateData data) {
        try {
            Template template = configuration.getTemplate("res_file.ftl");
            return process(template, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getResFileContent", e);
            return "";
        }
    }

    @NotNull
    public static String getJ2DContent(@NotNull Project project,
                                       @NotNull String className,
                                       @Nullable String comment,
                                       @NotNull List<J2DFieldDescriptor> fieldList) {
        try {
            Template template = configuration.getTemplate("j2d.ftl");
            String nullKey = getNullKey(project);
            String listConstructor = isUnModifiableList(project);
            J2DTemplateData data = new J2DTemplateData(className, comment, fieldList, nullKey, listConstructor);
            return process(template, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getJ2DContent", e);
            return "";
        }
    }

    @NotNull
    public static String getGenConstructor(@NotNull String className,
                                           @NotNull List<GenConstructorFieldDescriptor> fieldList) {
        try {
            Template template = configuration.getTemplate("gen_constructor.ftl");
            GenConstructorTemplateData data = new GenConstructorTemplateData(className, fieldList);
            return process(template, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenConstructor", e);
            return "";
        }
    }

    @NotNull
    public static String getGenFromJSONMethod(@NotNull Project project,
                                              @NotNull String className,
                                              @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        try {
            Template template = configuration.getTemplate("gen_from_json.ftl");
            String listConstructor = isUnModifiableList(project);
            GenFromJSONTemplateData data = new GenFromJSONTemplateData(className, fieldList, listConstructor);
            return process(template, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenFromJSONMethod", e);
            return "";
        }
    }

    @NotNull
    public static String getGenToJSONMethod(@NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        try {
            Template template = configuration.getTemplate("gen_to_json.ftl");
            GenToJSONTemplateData data = new GenToJSONTemplateData(fieldList);
            return process(template, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenToJSONMethod", e);
            return "";
        }
    }
}
