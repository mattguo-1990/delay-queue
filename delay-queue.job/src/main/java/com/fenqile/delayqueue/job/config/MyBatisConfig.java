
/*
 * MyBatis配置文件 name：HelloServiceImpl.java copyright：Copyright by fenqile.com author：fone
 * version：2015年11月23日
 */
package com.fenqile.delayqueue.job.config;

import com.fenqile.dao.sharding.plugin.ShardPlugin;
import com.fenqile.dao.transform.MultipleDataSource;
import com.fenqile.dao.transform.MySqlSessionTemplate;
import com.fenqile.dao.transform.Transporter;
import com.fenqile.delayqueue.job.zk.DbNodeChangeListener;
import com.fenqile.inbiz.configcenter.common.NodeChangeListener;
import com.fenqile.utils.common.ConfigCenterUtils;
import com.fenqile.utils.common.PropertiesUtils;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Preconditions;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * MyBatis配置文件
 * 正常来说不用改，参数配置直接在server.properties 中配置就可以了
 *
 * @author fone
 * @version 2015年11月24日
 * @see com.fenqile.delayqueue.job.config.MyBatisConfig
 */
@Configuration
@PropertySources({@PropertySource("classpath:config_center.properties"), @PropertySource("classpath:server.properties")
})
@EnableTransactionManagement
public class MyBatisConfig implements ApplicationContextAware
{

    /**
     * spring context
     */
    private ApplicationContext context;

    /**
     * 请到server.properties 中配置db.name
     */
    public static final String DB_NAME = "db.name"; //已经迁移到server.properties 这里只是key

    /**
     * 请到server.properties 中配置 zk 数据库 key
     */
    public static final String DB_KEY_PRE = "zk.db.key"; //已经迁移到server.properties 这里只是key

    /**
     * DTO 的 包路径
     */
    public static final String DOMAIN_PACKAGE = "db.dao.domain";//抽离到 server.properties 配置文件 com.fenqile.delayqueue.domain

    /**
     * DAO 的包路径
     */
    public static final String DAO_PACKAGE = "db.dao.base";//抽离到server.properties
    /*******************
     * zk config
     ****************************************************************/

    /**
     * zk_group
     */
    private static final String ZK_GROUP = "configcenter_groups";

    /**
     * ZK_DB_LISTENER_PATH
     */
    private static final String ZK_DB_LISTENER_PATH = "zk.db.path";

    /**
     * 打开分库分表插件
     */
    private static final String NEED_SHARDIND = "db.sharding";//只保留key值 需要就设置，不需要刻意不设置

    /**
     * 生成Transporter并从zookeeper上获取配置生成dataSource
     *
     * @param env 配置句柄
     * @return Transporter
     * @throws Exception
     * @see dataSourceTransporter
     */
    @Bean
    public Transporter dataSourceTransporter(Environment env) throws Exception
    {
        final String zkDbPath = env.getProperty(ZK_DB_LISTENER_PATH);
        final String group = Preconditions.checkNotNull(env.getProperty(ZK_GROUP));

        Map<String, String> dataMap = ConfigCenterUtils.get(group, zkDbPath);
        Transporter transporter = new Transporter();
        transporter.buildDataSources(env.getProperty(DB_KEY_PRE), env.getProperty(DB_NAME), dataMap);

        return transporter;
    }

    /**
     * 生成数据库节点监听器
     *
     * @param env                配置句柄
     * @param multipleDataSource 数据源
     * @param transporter        数据源转换器
     * @return NodeChangeListener
     * @see dbNodeChangeListener
     */
    @Bean
    @PostConstruct
    public NodeChangeListener dbNodeChangeListener(Environment env, MultipleDataSource multipleDataSource, Transporter transporter)
    {
        final String zkDbPath = env.getProperty(ZK_DB_LISTENER_PATH);
        final String group = Preconditions.checkNotNull(env.getProperty(ZK_GROUP));

        DbNodeChangeListener listener = new DbNodeChangeListener(zkDbPath, group);
        listener.setTransporter(transporter);
        listener.setEnvironment(env);//设置环境变量
        listener.setMultipleDataSource(multipleDataSource);
        ConfigCenterUtils.addListener(listener);
        return listener;
    }

    /**
     * 创建sqlSessionTemplate
     *
     * @param multipleDataSource 复合数据源
     * @return sqlSessionTemplate
     * @throws Exception Exception
     * @see sqlSessionTemplate
     */
    @Bean
    @Order(value = 2)
    public MySqlSessionTemplate sqlSessionTemplate(MultipleDataSource multipleDataSource, Environment env) throws Exception
    {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(multipleDataSource);
        factoryBean.setMapperLocations(context.getResources("classpath*:mappers/*.xml"));
        factoryBean.setTypeAliasesPackage(env.getProperty(DOMAIN_PACKAGE));

        List<Interceptor> interceptors = new ArrayList<Interceptor>();
        // page helper
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("dialect", "mysql");
        properties.setProperty("pageSizeZero", "true");
        pageHelper.setProperties(properties);
        interceptors.add(pageHelper);
        //设置是否需要 分库分表
        if (env.getProperty(NEED_SHARDIND) == null ? false : Boolean.parseBoolean(env.getProperty(NEED_SHARDIND)))
        {

            Resource table2DBResource = new ClassPathResource("/sharding/table2db.properties");
            Properties table2DBproperties = new Properties();
            table2DBproperties.load(table2DBResource.getInputStream());
            ShardPlugin shardPlugin = new ShardPlugin();
            Properties shardProperties = new Properties();
            shardProperties.setProperty("configsLocation", "sharding/mybatis-sharding-config.xml");
            shardPlugin.setProperties(shardProperties);
            shardPlugin.setTable2DB(PropertiesUtils.convertToMap(table2DBproperties));
            interceptors.add(shardPlugin);

        }

        Interceptor[] interceptorsArray = interceptors.toArray(new Interceptor[interceptors.size()]);

        factoryBean.setPlugins(interceptorsArray);

        return new MySqlSessionTemplate(factoryBean.getObject());

    }

    /**
     * mpperScannnerConfigurer
     *
     * @return mpperScannnerConfigurer
     * @see mpperScannnerConfigurer
     */
    @Bean
    @Lazy
    public MapperScannerConfigurer mpperScannnerConfigurer()
    {
        Map<String, String> propertiesMap = PropertiesUtils.readProperty("/server.properties");
        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.setSqlSessionTemplateBeanName("sqlSessionTemplate");
        msc.setBasePackage(propertiesMap.get(DAO_PACKAGE));
        return msc;
    }

    /**
     * 创建DataSourceTransactionManager
     *
     * @param multipleDataSource 复合数据源
     * @return DataSourceTransactionManager
     * @see transactionManager
     */
    @Bean
    public DataSourceTransactionManager transactionManager(MultipleDataSource multipleDataSource)
    {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(multipleDataSource);
        return transactionManager;
    }

    /**
     * 设置applicationContext
     *
     * @param applicationContext applicationContext
     * @throws org.springframework.beans.BeansException BeansException
     * @see setApplicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        context = applicationContext;
    }
}
