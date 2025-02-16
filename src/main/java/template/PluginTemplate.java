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
    private static String process(@NotNull String path, @NotNull Object data) throws TemplateException, IOException {
        Template template = configuration.getTemplate(path);
        CharArrayWriter writer = new CharArrayWriter();
        template.process(data, writer);
        return writer.toString();
    }

    private static boolean isSupportNullSafety(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).supportNullSafety;
    }

    @NotNull
    public static String getResFileContent(@NotNull String className,
                                           @NotNull List<ResFileTemplateData.Field> fieldList) {
        ResFileTemplateData data = new ResFileTemplateData(className, fieldList);
        try {
            return process("res_file.ftl", data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getResFileContent", e);
            return "";
        }
    }

    @NotNull
    public static String getGenConstructor(@NotNull Project project,
                                           @NotNull String className,
                                           @NotNull List<GenConstructorFieldDescriptor> fieldList) {
        if (fieldList.isEmpty()) return String.format("%s();", className);
        boolean nullSafety = isSupportNullSafety(project);
        String path = nullSafety ? "constructor/constructor_null_safety.ftl" : "constructor/constructor.ftl";
        GenConstructorTemplateData data = new GenConstructorTemplateData(className, fieldList);
        try {
            return process(path, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenConstructor", e);
            return "";
        }
    }

    @NotNull
    public static String getGenFromJSONMethod(@NotNull Project project,
                                              @NotNull String className,
                                              @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        boolean nullSafety = isSupportNullSafety(project);
        String path = nullSafety ? "json/from_json_null_safety.ftl" : "json/from_json.ftl";
        GenFromJSONTemplateData data = new GenFromJSONTemplateData(className, fieldList);
        try {
            return process(path, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenFromJSONMethod", e);
            return "";
        }
    }

    @NotNull
    public static String getGenToJSONMethod(@NotNull Project project,
                                            @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        boolean nullSafety = isSupportNullSafety(project);
        String path = nullSafety ? "json/to_json_null_safety.ftl" : "json/to_json.ftl";
        GenToJSONTemplateData data = new GenToJSONTemplateData(fieldList);
        try {
            return process(path, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getGenToJSONMethod", e);
            return "";
        }
    }

    @NotNull
    public static String getJ2DContent(@NotNull Project project,
                                       @NotNull String className,
                                       @Nullable String comment,
                                       @NotNull List<J2DFieldDescriptor> fieldList) {
        boolean nullSafety = isSupportNullSafety(project);
        String path = nullSafety ? "j2d/j2d_null_safety.ftl" : "j2d/j2d.ftl";
        J2DTemplateData data = new J2DTemplateData(className, comment, fieldList);
        try {
            return process(path, data);
        } catch (Exception e) {
            LogUtils.error("PluginTemplate getJ2DContent", e);
            return "";
        }
    }
}
