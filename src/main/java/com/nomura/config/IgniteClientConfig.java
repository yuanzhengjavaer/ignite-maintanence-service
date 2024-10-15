package com.nomura.config;

import com.nomura.common.Constants;
import com.nomura.components.filter.MaintenanceServiceFilter;
import com.nomura.components.remote.VehicleService;
import com.nomura.service.MaintenanceService;
import com.nomura.service.impl.MaintenanceServiceImpl;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.TransactionConfiguration;
import org.apache.ignite.logger.log4j2.Log4J2Logger;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

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
                //transactionnに関する
//                TransactionConfiguration defaultTxConfig = new TransactionConfiguration();
//                defaultTxConfig.setDefaultTxTimeout(5000L);
//                defaultTxConfig.setTxSerializableEnabled(true);
//                igniteConfiguration.setTransactionConfiguration(defaultTxConfig);
//                CacheConfiguration cacheConfiguration = new CacheConfiguration("maintenance");
//                cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
//                igniteConfiguration.setCacheConfiguration(cacheConfiguration);
                try {
                    String logConfigPath = this.getClass().
                            getClassLoader().
                            getResource("ignite-log4j2.xml").getPath();
                    logConfigPath = URLDecoder.decode(logConfigPath,"utf-8");
                    igniteConfiguration.setGridLogger(new Log4J2Logger(logConfigPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private ServiceConfiguration serviceConfiguration(ApplicationContext applicationContext) {
        ServiceConfiguration svcConfig = new ServiceConfiguration();
        svcConfig.setService(applicationContext.getBean("MaintenanceService",MaintenanceService.class));
        svcConfig.setName(MaintenanceService.SERVICE_NAME);
        svcConfig.setMaxPerNodeCount(1);
        svcConfig.setNodeFilter(new MaintenanceServiceFilter());
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

//    @Bean
//    public MaintenanceService maintenanceService() {
//        return new MaintenanceServiceImpl();
//    }

    @Bean
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

}
