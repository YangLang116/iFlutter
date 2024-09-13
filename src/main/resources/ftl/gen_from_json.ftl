factory ${className}.fromJson(Map<String, dynamic> json) {
    return ${className}(
      <#list fieldList as field>
        <#-- 处理List -->
        <#if field.className == 'List'>
          <#if (field.subType?? && !field.subType.buildIn)>
            ${field.name}: json['${field.name}'] == null ? ${field.nullable?string("null", "List<${field.subType.className}>.${listConstructor}([])")} : List<${field.subType.className}>.${listConstructor}(json['${field.name}'].map((x) => ${field.subType.className}.fromJson(x))),
          <#else>
            ${field.name}: json['${field.name}'],
          </#if>
        <#-- 处理Object -->
        <#elseif !field.buildIn>
          <#if field.nullable>
            ${field.name}: json['${field.name}'] == null ? null : ${field.className}.fromJson(json['${field.name}']),
          <#else>
            ${field.name}: ${field.className}.fromJson(json['${field.name}']),
          </#if>
        <#-- 通用处理 -->
        <#else>
          ${field.name}: json['${field.name}'],
        </#if>
      </#list>
    );
}
