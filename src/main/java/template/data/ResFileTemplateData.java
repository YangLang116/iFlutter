package template.data;

import java.util.List;

@SuppressWarnings("unused")
public class ResFileTemplateData {

    private final String className;
    private final List<Field> fieldList;

    public ResFileTemplateData(String className, List<Field> fieldList) {
        this.className = className;
        this.fieldList = fieldList;
    }

    public String getClassName() {
        return className;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public static final class Field {

        private final String name;
        private final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
