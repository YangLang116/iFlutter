package template.data;

import com.xtu.plugin.flutter.action.j2d.handler.entity.J2DFieldDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class J2DTemplateData {

    private final String className;
    private final List<J2DFieldDescriptor> fieldList;
    private final String nullKey;
    private final String listConstructor;

    public J2DTemplateData(@NotNull String className,
                           @NotNull List<J2DFieldDescriptor> fieldList,
                           @NotNull String nullKey,
                           @NotNull String listConstructor) {
        this.className = className;
        this.fieldList = fieldList;
        this.nullKey = nullKey;
        this.listConstructor = listConstructor;
    }

    public String getClassName() {
        return className;
    }

    public List<J2DFieldDescriptor> getFieldList() {
        return fieldList;
    }

    public String getNullKey() {
        return nullKey;
    }

    public String getListConstructor() {
        return listConstructor;
    }
}
