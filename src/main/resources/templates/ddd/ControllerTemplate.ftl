import ${config.layers['application'].components['Service'].basePackage}.${className}Service;
import ${config.layers['application'].components['DTO'].basePackage}.${className}DTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${className}")
public class ${className}${componentType} {
    private final ${className}Service service;

    public ${className}${componentType}(${className}Service service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ${className}DTO getById(@PathVariable String id) {
        // TODO: 实现获取逻辑
        return null;
    }

    @PostMapping
    public void create(@RequestBody ${className}DTO dto) {
        // TODO: 实现创建逻辑
    }
}