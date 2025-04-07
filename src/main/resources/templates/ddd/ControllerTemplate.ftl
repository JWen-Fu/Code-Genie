package ${config.layers['interfaces'].components['Controller'].basePackage};

<#assign apiPrefix = config.layers['interfaces'].components['Controller'].additionalConfig.apiPrefix!"/api">

import ${config.layers['application'].components['Service'].basePackage}.${className}Service;
import ${config.layers['application'].components['DTO'].basePackage}.${className}DTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${apiPrefix}/${originalTableName?lower_case}")
public class ${className}Controller {
private final ${className}Service service;

public ${className}Controller(${className}Service service) {
this.service = service;
}

@GetMapping("/{id}")
public ${className}DTO getById(@PathVariable String id) {
return service.getById(id);
}

@PostMapping
public void create(@RequestBody ${className}DTO dto) {
service.save(dto);
}
}