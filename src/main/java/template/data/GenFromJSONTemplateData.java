package template.data;

import com.xtu.plugin.flutter.action.generate.json.entity.GenJSONMethodFieldDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class GenFromJSONTemplateData {

    private final String className;
    private final List<GenJSONMethodFieldDescriptor> fieldList;

    public GenFromJSONTemplateData(@NotNull String className,
                                   @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        this.className = className;
        this.fieldList = fieldList;
    }

    public String getClassName() {
        return className;
    }

    public List<GenJSONMethodFieldDescriptor> getFieldList() {
        return fieldList;
    }
}
