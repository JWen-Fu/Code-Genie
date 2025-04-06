package ${config.layers['application'].components['DTO'].basePackage};

<#if useLombok>
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;
</#if>

<#if useLombok>
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
</#if>
public class ${table.name}DTO {
<#list table.columns as column>
    private ${typeMapper.getJavaType(column.type)} ${column.name};
</#list>
}