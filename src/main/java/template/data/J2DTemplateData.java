package template.data;

import com.xtu.plugin.flutter.action.j2d.handler.entity.J2DFieldDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public class J2DTemplateData {

    private final String className;
    private final String comment;
    private final List<J2DFieldDescriptor> fieldList;

    public J2DTemplateData(@NotNull String className,
                           @Nullable String comment,
                           @NotNull List<J2DFieldDescriptor> fieldList) {
        this.className = className;
        this.comment = comment;
        this.fieldList = fieldList;
    }

    public String getClassName() {
        return className;
    }

    public String getComment() {
        return comment;
    }

    public List<J2DFieldDescriptor> getFieldList() {
        return fieldList;
    }
}
