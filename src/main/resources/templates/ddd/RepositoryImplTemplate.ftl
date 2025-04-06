package ${config.layers['infrastructure'].components['RepositoryImpl'].basePackage};

import ${config.layers['domain'].components['Repository'].basePackage}.${table.name}Repository;
import ${config.layers['domain'].components['Entity'].basePackage}.${table.name}Entity;
import org.springframework.stereotype.Repository;

@Repository
public class Jpa${table.name}RepositoryImpl implements ${table.name}Repository {
@Override
public ${table.name}Entity findById(String id) {
// TODO: 实现JPA查询
return null;
}

@Override
public void save(${table.name}Entity entity) {
// TODO: 实现JPA保存
}
}