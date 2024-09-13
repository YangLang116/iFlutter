class ${className} {

   <#list fieldList as field>
     <#if (field.type == 2 && field.subType??)>
        final List<${field.subType.className}>${nullKey} ${field.displayName};
     <#else>
        final ${field.className}${nullKey} ${field.displayName};
     </#if>
   </#list>

   ${className}({
     <#list fieldList as field>
        this.${field.displayName},
     </#list>
   });

   factory ${className}.fromJson(Map<String, dynamic> json) {
     return ${className}(
       <#list fieldList as field>
         <#if (field.type == 2 && field.subType?? && field.subType.type == 1)>
           ${field.displayName}: json['${field.key}'] == null? null : List<${field.subType.className}>.${listConstructor}(json['${field.key}'].map((x) => ${field.subType.className}.fromJson(x))),
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
         '${field.key}': ${field.displayName}${nullKey}.map((e) => e.toJson()).toList(),
       <#elseif field.type == 1>
         '${field.key}': ${field.displayName}${nullKey}.toJson(),
       <#else>
         '${field.key}': ${field.displayName},
       </#if>
     </#list>
   };
}