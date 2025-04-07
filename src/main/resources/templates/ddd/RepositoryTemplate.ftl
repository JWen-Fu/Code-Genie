package ${config.layers['domain'].components['Repository'].basePackage};

public interface ${className}Repository {
${className}Entity findById(String id);
void save(${className}Entity entity);
}