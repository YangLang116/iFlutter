Map<String, dynamic> toJson() => {
  <#list fieldList as field>
    <#-- 处理List -->
    <#if (field.className == 'List' && field.subType?? && !field.subType.buildIn)>
      '${field.name}': ${field.name}${field.dealNullable?string("?", "")}.map((e) => e${field.subType.dealNullable?string("?", "")}.toJson()).toList(),
    <#-- 处理Object -->
    <#elseif !field.buildIn>
      '${field.name}': ${field.name}${field.dealNullable?string("?", "")}.toJson(),
    <#-- 通用处理 -->
    <#else>
      '${field.name}': ${field.name},
    </#if>
  </#list>
};
