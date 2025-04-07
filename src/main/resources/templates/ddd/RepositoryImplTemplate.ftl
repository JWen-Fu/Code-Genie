package ${config.layers['infrastructure'].components['RepositoryImpl'].basePackage};

import ${config.layers['domain'].components['Repository'].basePackage}.${className}Repository;
import ${config.layers['domain'].components['Entity'].basePackage}.${className}Entity;
import org.springframework.stereotype.Repository;

@Repository
public class ${className}RepositoryImpl implements ${className}Repository {
@Override
public ${className}Entity findById(String id) {
// TODO: 实现JPA查询
return null;
}

@Override
public void save(${className}Entity entity) {
// TODO: 实现JPA保存
}
}