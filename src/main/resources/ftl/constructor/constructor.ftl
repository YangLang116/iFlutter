${className}({
  <#list fieldList as field>
  this.${field.name},
  </#list>
});