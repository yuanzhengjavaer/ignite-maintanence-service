package com.nomura.config;

import com.nomura.common.Constants;
import com.nomura.components.filter.MaintenanceServiceFilter;
import com.nomura.components.remote.VehicleService;
import com.nomura.service.MaintenanceService;
import com.nomura.service.impl.MaintenanceServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.*;
import org.apache.ignite.events.EventType;
import org.apache.ignite.logger.log4j2.Log4J2Logger;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class IgniteClientConfig {

    @Bean
    public IgniteConfigurer clientConfiguration(@Autowired ApplicationContext applicationContext) {
        return new IgniteConfigurer() {
            @Override
            public void accept(IgniteConfiguration igniteConfiguration) {

                // 各serviceの間ではrpcで通信、もしこのswitchがonの場合、クラスタ内でhandleとして使用するclass infoをload
                igniteConfiguration.setPeerClassLoadingEnabled(true);
//                igniteConfiguration.setClientMode(true);
                igniteConfiguration.setServiceConfiguration(serviceConfiguration(applicationContext));
                igniteConfiguration.setUserAttributes(getUserAttributes());
                igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi());
                igniteConfiguration.setIgniteInstanceName("maintenance-service");
                igniteConfiguration.setIncludeEventTypes(EventType.EVT_CACHE_OBJECT_PUT,
                        EventType.EVT_CACHE_OBJECT_READ,
                        EventType.EVT_CACHE_OBJECT_REMOVED,
                        EventType.EVT_NODE_LEFT,
                        EventType.EVT_NODE_JOINED);

                try {
                    String logConfigPath = this.getClass().
                            getClassLoader().
                            getResource("ignite-log4j2.xml").getPath();
                    logConfigPath = URLDecoder.decode(logConfigPath, "utf-8");
                    igniteConfiguration.setGridLogger(new Log4J2Logger(logConfigPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }


    //    @Bean
    private ServiceConfiguration serviceConfiguration(ApplicationContext applicationContext) {
        ServiceConfiguration svcConfig = new ServiceConfiguration();
        MaintenanceService maintenanceService = applicationContext.getBean("maintenanceService", MaintenanceService.class);
        svcConfig.setService(maintenanceService);
        svcConfig.setName(MaintenanceService.SERVICE_NAME);
        svcConfig.setMaxPerNodeCount(3);
        svcConfig.setNodeFilter(new MaintenanceServiceFilter());
        svcConfig.setTotalCount(3);
        return svcConfig;
    }

    @Bean
    @DependsOn("ignite")
    public VehicleService vehicleService(@Autowired Ignite ignite) {
        // Getting access to VehicleService proxy. The proxy allows to call remotely deployed services.
        VehicleService vehicleService = ignite.services().serviceProxy(VehicleService.SERVICE_NAME,
                VehicleService.class, false);
        return vehicleService;
    }

    @Bean
    public MaintenanceService maintenanceService() {
        return new MaintenanceServiceImpl();
    }

    //    @Bean
    public TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder() {
        TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
        tcpDiscoveryVmIpFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47509"));
        return tcpDiscoveryVmIpFinder;
    }

    public TcpDiscoverySpi tcpDiscoverySpi() {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder());
        return tcpDiscoverySpi;
    }

    private Map<String, ?> getUserAttributes() {
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(Constants.SERVICE_NODE_TAG, true);
        return userAttributes;
    }


    @Configuration
    @MapperScan(basePackages = "com.nomura.dao", sqlSessionFactoryRef = "igniteSqlSessionFactory")
    static class IgniteMyBatisConfig {

        @Bean("igniteDataSource")
        @ConfigurationProperties(prefix = "spring.ignite.datasource")
        public DataSource igniteDataSource(Environment environment, IgniteConfiguration igniteConfig) {
            DataStorageConfiguration dataStorageConfig = new DataStorageConfiguration();
            DataRegionConfiguration defaultDataRegionConfig = new DataRegionConfiguration();
            defaultDataRegionConfig.setPersistenceEnabled(false);
            dataStorageConfig.setDefaultDataRegionConfiguration(defaultDataRegionConfig);
            igniteConfig.setDataStorageConfiguration(dataStorageConfig);

            ConnectorConfiguration configuration = new ConnectorConfiguration();
            configuration.setIdleTimeout(6000);
            configuration.setThreadPoolSize(100);
            configuration.setIdleTimeout(60000);

            igniteConfig.setConnectorConfiguration(configuration);

            return DataSourceBuilder.create()
                    .url(environment.getProperty("spring.ignite.datasource.url"))
                    .driverClassName(environment.getProperty("spring.ignite.datasource.driver-class-name")).type(HikariDataSource.class)
                    .build();
        }

        @Bean("igniteSqlSessionFactory")
        public SqlSessionFactory igniteSqlSessionFactory(DataSource igniteDataSource) throws Exception {
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
            factoryBean.setDataSource(igniteDataSource);
            factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/ignite/*.xml"));
            return factoryBean.getObject();
        }
    }

    @Configuration
    @EnableTransactionManagement
    public class SpringApplicationConfiguration {
        /**
         * txManagerがもう一個igniteNode起動する(IgniteSpringBean)
         *
         * @return
         */
        @Bean
        @DependsOn("ignite")
        public SpringTransactionManager transactionManager() {
            SpringTransactionManager mgr = new SpringTransactionManager();
            mgr.setTransactionConcurrency(TransactionConcurrency.PESSIMISTIC);
            IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
            TransactionConfiguration defaultTxConfig = new TransactionConfiguration();
            defaultTxConfig.setDefaultTxTimeout(5000L);
            CacheConfiguration cacheConfiguration = new CacheConfiguration("maintenance");
            cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            igniteConfiguration.setCacheConfiguration(cacheConfiguration);

//                defaultTxConfig.setTxSerializableEnabled(true);
            igniteConfiguration.setTransactionConfiguration(defaultTxConfig);
            igniteConfiguration.setIgniteInstanceName("maintenance-txManager");
            igniteConfiguration.setClientMode(true);
            igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi());
            igniteConfiguration.setPeerClassLoadingEnabled(true);
            mgr.setConfiguration(igniteConfiguration);

            return mgr;
        }

    }
}