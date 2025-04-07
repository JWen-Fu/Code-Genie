package ${config.layers['application'].components['Service'].basePackage};

import ${config.layers['domain'].components['Repository'].basePackage}.${className}Repository;
import ${config.layers['domain'].components['Entity'].basePackage}.${className}Entity;
import ${config.layers['application'].components['DTO'].basePackage}.${className}DTO;

public class ${className}Service {
private final ${className}Repository repository;

public ${className}Service(${className}Repository repository) {
this.repository = repository;
}

public ${className}DTO getById(String id) {
${className}Entity entity = repository.findById(id);
// TODO: 转换entity为DTO
return null;
}

public void save(${className}DTO dto) {
// TODO: 转换DTO为entity
${className}Entity entity = null;
repository.save(entity);
}
}