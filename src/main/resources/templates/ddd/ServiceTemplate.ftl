
import ${config.layers['domain'].components['Repository'].basePackage}.${className}Repository;
import ${config.layers['domain'].components['Entity'].basePackage}.${className}Entity;
import ${config.layers['application'].components['DTO'].basePackage}.${className}Dto;
import org.springframework.stereotype.Service;

@Service
public class ${className}${componentType} {
private final ${className}Repository repository;

public ${className}${componentType}(${className}Repository repository) {
this.repository = repository;
}

public ${className}Dto getById(String id) {
${className}Entity entity = repository.findById(id);
// TODO: 转换entity为DTO
return null;
}

public void save(${className}Dto dto) {
// TODO: 转换DTO为entity
${className}Entity entity = null;
repository.save(entity);
}
}