package ${config.layers['domain'].components['Repository'].basePackage};

public interface ${table.name}Repository {
${table.name}Entity findById(String id);
void save(${table.name}Entity entity);
}