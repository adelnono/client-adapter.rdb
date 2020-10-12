package com.alibaba.otter.canal.client.adapter.rdb.test;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.otter.canal.client.adapter.rdb.config.ConfigLoader;
import com.alibaba.otter.canal.client.adapter.rdb.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.support.DatasourceConfig;

public class ConfigLoadTest {

    @Before
    public void before() {
        // 加载数据源连接池
        DatasourceConfig.DATA_SOURCES.put("defaultDS", TestConstant.dataSource);
    }

    @Test
    public void testLoad() {
        Properties env = new Properties(System.getProperties());
        env.put("SERVICE_QZ_SHOP_CODE", "0008");
        env.put("SERVICE_QZ_ENT_ID", "0");
        env.put("SERVICE_QZ_ERP_CODE", "ACH");
        Map<String, MappingConfig> configMap =  ConfigLoader.load(env);

        Assert.assertFalse(configMap.isEmpty());
    }
}
