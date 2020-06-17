package com.fenqile.delayqueue.job.zk;

import com.alibaba.fastjson.JSON;
import com.fenqile.dao.transform.MultipleDataSource;
import com.fenqile.dao.transform.Transporter;
import com.fenqile.delayqueue.job.config.MyBatisConfig;
import com.fenqile.inbiz.configcenter.common.NodeChangeListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * zookeeper监听 动态修改数据源
 */
public class DbNodeChangeListener extends NodeChangeListener
{

    public DbNodeChangeListener(String path, String group)
    {
        super(path, group);
    }

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbNodeChangeListener.class);

    /**
     * 数据源转换
     */
    private Transporter transporter;

    private Environment environment;

    public void setEnvironment(Environment environment)
    {
        this.environment = environment;
    }

    /**
     * 多路复用数据源
     */
    private MultipleDataSource multipleDataSource;

    public MultipleDataSource getMultipleDataSource()
    {
        return multipleDataSource;
    }

    public void setMultipleDataSource(MultipleDataSource multipleDataSource)
    {
        this.multipleDataSource = multipleDataSource;
    }

    public Transporter getTransporter()
    {
        return transporter;
    }

    public void setTransporter(Transporter transporter)
    {
        this.transporter = transporter;
    }


    @Override
    protected void createNodeCallback(TreeCacheEvent event)
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateNodeCallback(TreeCacheEvent event)
    {
        synchronized (this)
        {
            ChildData childData = event.getData();
            try
            {
                String data = new String(childData.getData(), "utf8");

                Map<String, String> dataMap = JSON.parseObject(data, Map.class);
                LOGGER.debug("==========dataMap:{}", dataMap);
                this.transporter.buildDataSources(environment.getProperty(MyBatisConfig.DB_KEY_PRE), environment.getProperty(MyBatisConfig.DB_NAME), dataMap);

                multipleDataSource.addTargetDataSources(this.transporter.transportTargetDataSources());
            } catch (UnsupportedEncodingException e)
            {
                LOGGER.error("updateNodeCallback UnsupportedEncodingException", e);
            } catch (Exception e)
            {
                LOGGER.error("updateNodeCallback transport fail", e);
            }
        }
    }

    @Override
    protected void deleteNodeCallback(TreeCacheEvent event)
    {
        // ignore

    }
}
