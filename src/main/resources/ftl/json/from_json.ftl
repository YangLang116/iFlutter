factory ${className}.fromJson(Map<String, dynamic> json) {
  return ${className}(
    <#list fieldList as field>
      <#if field.className == 'List'>
        <#if field.subType??>
          <#if field.subType.buildIn>
    ${field.name}: json['${field.name}'] == null ? null : json['${field.name}'].cast<${field.subType.className}>(),
          <#else>
    ${field.name}: json['${field.name}'] == null ? null : List<${field.subType.className}>.from(json['${field.name}'].map((x) => ${field.subType.className}.fromJson(x))),
          </#if>
        <#else>
    ${field.name}: json['${field.name}'],
        </#if>
      <#elseif !field.buildIn>
    ${field.name}: json['${field.name}'] == null ? null : ${field.className}.fromJson(json['${field.name}']),
      <#else>
     ${field.name}: json['${field.name}'],
      </#if>
    </#list>
  );
}
