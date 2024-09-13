package template.data;

import com.xtu.plugin.flutter.action.generate.json.entity.GenJSONMethodFieldDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class GenToJSONTemplateData {

    private final List<GenJSONMethodFieldDescriptor> fieldList;

    public GenToJSONTemplateData(@NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        this.fieldList = fieldList;
    }

    public List<GenJSONMethodFieldDescriptor> getFieldList() {
        return fieldList;
    }
}
