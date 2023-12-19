package com.example.bpmnactiviti.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @Author yanbl
 * @Date 2023/12/18 16:57
 * @Description ...
 */
@Configuration
public class ActivitiConfiguration {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    public ActivitiConfiguration() {
    }

    //通过@Bean注解将SpringProcessEngineConfiguration实例声明为Spring Bean，使其可供其他组件注入和使用
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
        SpringProcessEngineConfiguration spec = new SpringProcessEngineConfiguration();
        //设置数据源，将注入的数据源设置到SpringProcessEngineConfiguration实例中
        spec.setDataSource(this.dataSource);
        //设置事务管理器将注入的事务管理器设置到SpringProcessEngineConfiguration实例中
        spec.setTransactionManager(this.platformTransactionManager);
        //设置数据库模式更新策略 true表示在启动时自动创建或更新Activiti引擎所需的数据库表结构
        spec.setDatabaseSchemaUpdate("true");
        Resource[] resources = null;
        //配置流程部署资源
        //使用PathMatchingResourcePatternResolver从classpath中的bpmn目录下加载所有以.bpmn为扩展名的文件作为流程定义资源，
        // 并将它们设置到SpringProcessEngineConfiguration实例中。
        try {
            resources = (new PathMatchingResourcePatternResolver()).getResources("classpath*:bpmn/*.bpmn");
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        spec.setDeploymentResources(resources);
        return spec;
    }
}
