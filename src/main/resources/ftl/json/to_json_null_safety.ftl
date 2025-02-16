Map<String, dynamic> toJson() => {
  <#list fieldList as field>
    <#if (field.className == 'List' && field.subType?? && !field.subType.buildIn)>
  '${field.name}': ${field.name}${field.nullable?string("?", "")}.map((x) => x.toJson()).toList(),
    <#elseif !field.buildIn>
  '${field.name}': ${field.name}${field.nullable?string("?", "")}.toJson(),
    <#else>
  '${field.name}': ${field.name},
    </#if>
  </#list>
};
