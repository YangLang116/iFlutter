class ${className} {
<#if comment??>${comment}</#if>

  <#list fieldList as field>
    <#if (field.type == 2 && field.subType??)>
  final List<${field.subType.className}> ${field.displayName};
    <#else>
  final ${field.className} ${field.displayName};
    </#if>
  </#list>

  <#if fieldList?size == 0>
  ${className}();
  <#else>
  ${className}({
    <#list fieldList as field>
    this.${field.displayName},
    </#list>
  });
  </#if>

  factory ${className}.fromJson(Map<String, dynamic> json) {
    return ${className}(
      <#list fieldList as field>
        <#if (field.type == 2 && field.subType??)>
          <#if field.subType.type == 1>
      ${field.displayName}: json['${field.key}'] == null? null : List<${field.subType.className}>.from(json['${field.key}'].map((x) => ${field.subType.className}.fromJson(x))),
          <#else>
      ${field.displayName}: json['${field.key}'] == null? null : json['${field.key}'].cast<${field.subType.className}>(),
          </#if>
        <#elseif field.type == 1>
      ${field.displayName}: json['${field.key}'] == null? null : ${field.className}.fromJson(json['${field.key}']),
        <#else>
      ${field.displayName}: json['${field.key}'],
        </#if>
      </#list>
    );
  }

  Map<String, dynamic> toJson() => {
    <#list fieldList as field>
      <#if (field.type == 2 && field.subType?? && field.subType.type == 1)>
    '${field.key}': ${field.displayName} == null? null : ${field.displayName}.map((e) => e.toJson()).toList(),
      <#elseif field.type == 1>
    '${field.key}': ${field.displayName} == null? null : ${field.displayName}.toJson(),
      <#else>
    '${field.key}': ${field.displayName},
      </#if>
    </#list>
  };
}