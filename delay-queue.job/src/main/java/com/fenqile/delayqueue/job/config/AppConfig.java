/*
* 描述 name：AppConfig.java copyright：Copyright by fenqile.com author：fone version：2015年11月23日
*/
package com.fenqile.delayqueue.job.config;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;

/**
 * 工程启动类,初始化配置容器
 * <p/>
 * 正常来说不用修改。如果修改一定要清楚每项配置的含义
 */
@Configuration
@ComponentScan(basePackages = {"com.fenqile"}, useDefaultFilters = false, includeFilters = {
        @Filter(type = FilterType.ANNOTATION, value = Component.class),
        @Filter(type = FilterType.ANNOTATION, value = Service.class)
})
@EnableTransactionManagement
@EnableAspectJAutoProxy
//@EnableWebMvc   //因为使用了springboot，不允许这里再用该注解
@PropertySource("classpath:server.properties")
public class AppConfig
{

    /**
     * ConfigurableEnvironment
     */
    @Inject
    private ConfigurableEnvironment env;

    /**
     * 配置文件初始化
     *
     * @throws java.io.IOException IOException
     * @see
     */
    @PostConstruct
    void configureEnvironment() throws IOException
    {

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:*.properties");
        for (Resource resource : resources)
        {
            env.getPropertySources().addLast(new ResourcePropertySource(resource));
        }
    }

    /**
     * 生成PropertySourcesPlaceholderConfigurer
     *
     * @return PropertySourcesPlaceholderConfigurer
     * @see
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placehodlerConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
