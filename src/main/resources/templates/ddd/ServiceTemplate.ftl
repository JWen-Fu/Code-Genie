package ${config.layers['application'].components['Service'].basePackage};

import ${config.layers['domain'].components['Repository'].basePackage}.${table.name}Repository;
import ${config.layers['domain'].components['Entity'].basePackage}.${table.name}Entity;
import ${config.layers['application'].components['DTO'].basePackage}.${table.name}DTO;

public class ${table.name}Service {
private final ${table.name}Repository repository;

public ${table.name}Service(${table.name}Repository repository) {
this.repository = repository;
}

public ${table.name}DTO getById(String id) {
${table.name}Entity entity = repository.findById(id);
// TODO: 转换entity为DTO
return null;
}

public void save(${table.name}DTO dto) {
// TODO: 转换DTO为entity
${table.name}Entity entity = null;
repository.save(entity);
}
}