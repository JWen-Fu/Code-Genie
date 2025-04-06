package ${config.layers['interfaces'].components['Controller'].basePackage};

import ${config.layers['application'].components['Service'].basePackage}.${table.name}Service;
import ${config.layers['application'].components['DTO'].basePackage}.${table.name}DTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${table.name?lower_case}")
public class ${table.name}Controller {
private final ${table.name}Service service;

public ${table.name}Controller(${table.name}Service service) {
this.service = service;
}

@GetMapping("/{id}")
public ${table.name}DTO getById(@PathVariable String id) {
// TODO: 实现获取逻辑
return null;
}

@PostMapping
public void create(@RequestBody ${table.name}DTO dto) {
// TODO: 实现创建逻辑
}
}