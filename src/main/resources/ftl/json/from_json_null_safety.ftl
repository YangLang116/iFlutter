factory ${className}.fromJson(Map<String, dynamic> json) {
  return ${className}(
    <#list fieldList as field>
      <#if field.className == 'List'>
        <#if field.subType??>
          <#if field.subType.buildIn>
    ${field.name}: ${field.nullable?string("json['${field.name}']?.cast<${field.subType.className}>()" , "json['${field.name}'] == null ? <${field.subType.className}>[] : json['${field.name}'].cast<${field.subType.className}>()")},
          <#else>
    ${field.name}: json['${field.name}'] == null ? ${field.nullable?string("null", "<${field.subType.className}>[]")} : List<${field.subType.className}>.from(json['${field.name}'].map((x) => ${field.subType.className}.fromJson(x))),
          </#if>
        <#else>
    ${field.name}: json['${field.name}'],
        </#if>
      <#elseif !field.buildIn>
        <#if field.nullable>
    ${field.name}: json['${field.name}'] == null ? null : ${field.className}.fromJson(json['${field.name}']),
        <#else>
    ${field.name}: ${field.className}.fromJson(json['${field.name}']),
        </#if>
      <#else>
     ${field.name}: json['${field.name}'],
      </#if>
    </#list>
  );
}
