package template.data;

import com.xtu.plugin.flutter.action.generate.constructor.entity.GenConstructorFieldDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class GenConstructorTemplateData {

    private final String className;
    private final List<GenConstructorFieldDescriptor> fieldList;


    public GenConstructorTemplateData(@NotNull String className, @NotNull List<GenConstructorFieldDescriptor> fieldList) {
        this.className = className;
        this.fieldList = fieldList;
    }

    public String getClassName() {
        return className;
    }

    public List<GenConstructorFieldDescriptor> getFieldList() {
        return fieldList;
    }
}
