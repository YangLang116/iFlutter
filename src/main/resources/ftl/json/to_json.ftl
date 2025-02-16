Map<String, dynamic> toJson() => {
  <#list fieldList as field>
    <#if (field.className == 'List' && field.subType?? && !field.subType.buildIn)>
  '${field.name}': ${field.name} == null? null : ${field.name}.map((e) => e.toJson()).toList(),
    <#elseif !field.buildIn>
  '${field.name}': ${field.name} == null? null : ${field.name}.toJson(),
    <#else>
  '${field.name}': ${field.name},
    </#if>
  </#list>
};
