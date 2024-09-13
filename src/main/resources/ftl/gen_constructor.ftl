${className}({
  <#list fieldList as field>
    ${field.nullable?string("", "required")} this.${field.name},
  </#list>
});
