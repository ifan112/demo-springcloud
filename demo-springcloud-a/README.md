自动注入的原理是创建 `EurekaAutoServiceRegistration` 这个 Bean，它实现了 `Lifecycle` 和 `ApplicationListener` 这两个接口，
当该 Bean 并创建之后，它的 `start()` 方法会被调用，正是在该方法中实现了向服务注册中心注册当前服务实例信息。

可以通过 `@EnableDiscoveryClient#autoRegister` 方法或者 `spring.cloud.service-registry.auto-registration.enabled=true/false` 属性来控制是否自动注册服务实例到服务注册中心。

虽然不自动注册，但是与服务注册相关的 Bean 如 `CloudEurekaClient`, `CloudEurekaInstanceConfig`, `ApplicationInfoManager` 以及 `EurekaServiceRegistry` 等都已经创建好了。 
因此，我们可以通过注入 `EurekaServiceRegistry` 等实例，然后调用 `EurekaServiceRegistry#register` 方法向服务注册中心注册当前服务实例信息。



## 配置

eureka 相关的配置主要分为两类

1. eureka 客户端行为，格式为 eureka.client.xx。例如 eureka.client.registry-fetch-interval-seonds: 30 配置项指定了从 eureka server 获取服务注册信息的时间间隔。
2. 服务实例信息相关，格式为 eureka.instance.xx。例如 eureka.instance.health-check-url 配置项指定了服务健康监测的地址。

```yml

eureka:

  client:
    service-url:
      defaultZone: http://10.10.8.167:8761/eureka
    # 以下是非必要参数，均为默认值
    refresh:
      enable: true                                  # 是否刷新 eureka server 中注册的服务实例信息
    enabled: true                                   # 是否启用 eureka client，默认 true
    fetch-registry: true                            # 是否从 eureka server 获取服务注册信息
    register-with-eureka: true                      # 是否将当前服务实例信息注册到 eureka server
    registry-fetch-interval-seconds: 30             # 从 eureka server 获取服务注册信息的时间间隔，单位秒
    region: us-east-1                               # 服务实例所在地域，默认 us-east-1。服务实例所在的 zone 为当前地域下的第一个可用区，没有则为 defaultZone
    availability-zones:                             # region 与 zone 的映射
      cn-hangzhou: cn-hangzhou-b,cn-hangzhou-c
      cn-shanghai: cn-shanghai-a,cn-shanghai-b
    prefer-same-zone-eureka: true                   # 是否优先访问相同 zone 下的 eureka server

    eureka-service-url-poll-interval-seconds: 300   # 当 eureka server 信息变更时，客户端多久知道
    instance-info-replication-interval-seconds: 30  #
    initial-instance-info-replication-interval-seconds: 40 #

    eureka-server-read-timeout-seconds: 8           # 从 eureka server 读取信息时等待的超时时间，单位秒
    eureka-server-connect-timeout-seconds: 5        # 连接 eureka server 时等待的超时时间，单位秒
    eureka-connection-idle-timeout-seconds: 30      #
    eureka-server-total-connections: 200            # eureka client 与 server 允许建立的最大的连接数
    eureka-server-total-connections-per-host: 50    # eureka client 与每台 eureka server 之间允许建立的最大的连接数
    heartbeat-executor-thread-pool-size: 2          # 发送心跳的线程池的初始化大小
    heartbeat-executor-exponential-back-off-bound: 10
    cache-refresh-executor-thread-pool-size: 2      # cacheRefreshExecutor
    cache-refresh-executor-exponential-back-off-bound: 10
    g-zip-content: true                             # 从 eureka server 获取服务注册信息时是否使用 gZip 方式压缩内容

    use-dns-for-fetching-service-urls: false        # 是否使用 dns 机制来获取 eureka server 地址列表
    eureka-server-d-n-s-name: eureka-server.com     # 当使用 dns 机制来获取 eureka server 地址列表时访问 dns 服务的根域名，最终访问域名是 txt.us-east-1.eureka-server.com
    eureka-server-port: null                        # 当通过 dns 服务获取到 eureka server 地址后，访问时使用的端口
    eureka-server-u-r-l-context: null               # 当通过 dns 服务获取到 eureka server 地址后，访问时使用的路径

    disable-delta: false                            # 禁用增量请求，而是每次都从 eureka server 获取全量的注册信息
    filter-only-up-instances: true                  # 只获取那些状态是 UP 的服务实例的注册信息
    should-unregister-on-shutdown: true             # 当服务不可用时是否取消注册
    should-enforce-registration-at-init: false      # 当服务启动时是否强制注册信息

```



## 服务注册流程

在 netflix 中定义了两个配置接口，分别是 `EurekaClientConfig` 和 `EurekaInstanceConfig`，它们分别记录了 eureka 客户端和服务实例相关的配置。

在 `EurekaClientAutoConfiguration` 类中，创建了这两个配置类的实例。

```java
// spring-cloud-netflix-eureka-client.jar
@Configuration
public class EurekaClientAutoConfiguration {

    @Bean
    public EurekaClientConfigBean eurekaClientConfigBean(ConfigurableEnvironment env) {
        // eureka 客户端配置 Bean
        // 直接创建出 Bean 即可，它将会根据 eureka.client.xx 配置项自动装配属性
        return new EurekaClientConfigBean();
    }
    
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfigBean(
        InetUtils inetUtils, ManagementMetadataProvider managementMetadataProvider) {
        // 服务实例配置 Bean
        // ...
        return new EurekaInstanceConfigBean(..);
    }
}
```



在创建了 eureka 客户端和服务实例配置的 Bean 之后，便可以基于它们创建其它的组件了。

首先，根据 `EurekaInstanceConfig` Bean 创建 `ApplicationInfoManager` 组件。

```java
@Configuration
public class EurekaClientAutoConfiguration {
    
    @Bean
    public ApplicationInfoManager eurekaApplicationInfoManager(
        EurekaInstanceConfig config) {
        // 服务实例信息
        InstanceInfo instanceInfo = new InstanceInfoFactory().crete(config);
        // 创建服务实例管理器
        return new ApplicationInfoManager(config, instanceInfo);
    }
}
```

然后，创建 `EurekaClient` 组件

```java
@Configuration
public class EurekaClientAutoConfiguration {

    @Bean
    public EurekaClient eurekaClient(ApplicationInfoManager manager,
                                    EurekaClientConfig config, EurekaInstanceConfig instance,
                                    HealthCheckHandler healthCheckHandler) {
        // 基于 ApplicationInfoManager, EurekaClientConfig 和 EurekaInstanceConfig 创建 eureka client 组件
        // 在创建 eureka client 时，启动了服务实例信息注册线程等
        CloudEurekaClient cloudEurekaClient 
            = new CloudEurekaClient(manager, config, this.optionalArgs, this.context);
        cloudEurekaClient.registerHealthCheck(healthCheckHandler);
        
        return cloudEurekaClient;
    }
}
```

看一下 `CloudEurekaClient` 的源码

```java
// 由 spring cloud 实现的 CloudEurekaClient 集成了 netflix 原生的 DiscoveryClient
public class CloudEurekaClient extends com.netflix.discovery.DiscoveryClient {
    
    
    public CloudEurekaClient(ApplicationInfoManager applicationInfoManager,
                            EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs<?> args,
                            ApplicationEventPublisher publisher) {
        // 调用父类完成初始化
        super(applicationInfoManager, config, args);
        this.applicationInfoManager = applicationInfoManager;
        this.publisher = publisher;
        
        // 获取父类中定义的 eurekaTransaport 字段
        // 用于得到实现 eureka client 和 eureka server 之间 http 请求的组件 EurekaHttpClient
        this.eurekaTransportField = ReflectionUtils.findField(DiscoveryClient.class, "eurekaTransport");
        ReflectionUtils.makeAccessible(this.eurekaTransportField);
    }
}
```

接着看一下 CloudEurekaClient 的父类 - netflix 原生的 `DiscoveryClient`

```java
// eureka-client.jar
public class DiscoveryClient implements EurekaClient {
    
    public DiscoveryClient(ApplicationInfoManager applicationInfoManager,
                          EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args) {
        this(applicationInfoManager, config, args, ResolverUtils::randomize);
    }
    
    public DiscoveryClient(ApplicationInfoManager applicationInfoManager, 
                           final EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args, 
                           EndpointRandomizer randomizer) {
        this(applicationInfoManager, config, args, new Provider<BackupRegistry>() {
            // 当从 eureka server 获取服务注册信息失败时备用的服务注册中心
            // 根据 config 中的 eureka.client.backup-registry-impl 配置项指定的全限定类名创建出实例
        }, randomizer);
    }
    
    
    DiscoveryClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig config,
                   AbstractDiscoveryClientOptionalArgs args, Provider<BackupRegistry> backupRegistryProvider, 
                   EndpointRandomizer endpointRandomzier) {
        if (args != null) {
            this.healthCheckHandlerProvider = args.healthCheckHnadlerProvider;
            this.healthCheckCallbackProvider = args.healthCheckCallbackProvider;
            this.eventListeners.addAll(args.getEventListeners());
            this.preRegistrationHandler = args.preRegistrationHandler;
        } else {
            this.healthCheckHandlerProvider = null;
            this.healthCheckCallbackProvider = null;
            this.preRegistrationHandler = null;
        }
        
        this.applicationInfoManager = applicationInfoManager;
        InstanceInfo myInfo = applicationInfoManager.getInfo();
        
        clientConfig = config;
        staticClientConfig = clientConfig;
        transportConfig = config.getTransportConfig();
        instanceInfo = myInfo;
        
        if (myInfo != null) {
            appPathIdentifier = instanceInfo.getAppName() + "/" instanceInfo.getId();
        }
        
        this.backupRegistryProvider = backupRegistryProvider;
        this.endpointRandomizer = endpointRandomizer;
        this.urlRandomizer = new EndpointUtils.InstanceInfoBasedUrlRandomizer(instanceInfo);
        
        localRegionApps.set(new Applications());
        
        fetchRegistryGeneration = new AtomicLong(0);
        
        remoteRegionsToFetch = new AtomicReference<String>(clientConfig.fetchRegistryForRemoteRegions());
        remoteRegionsRef = new AtomicReference<>(remoteRegionsToFetch.get() == null ? null : remoteRegionsToFetch.get().split(","));
        
        if (config.shouldFetchRegistry()) {
            this.registryStalenessMonitor = new ThresholdLevelMetric(this, "registry.lastUpdateSec_", new long[]{15L, 30L, 60L, 120L, 240L, 480L});
        } else {
            this.registryStalenessMonitor = ThresholdLevelMetric.NO_OP_METRIC;
        }
        
        logger.info("Initializing Eureka in region {}", clientConfig.getRegion());
        
        if (!config.shouldRegisterWithEureka() && !config.shouldFetchRegistry()) {
            
            // 
            
            return;
        }
        
        try {
            // 
            scheduler = Exectors.newScheduledThreadPool(
                2, new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-%d")
                        .setDaemon(true)
                        .build());
            
            heartbeatExecutor = new ThreadPoolExecutor(
                1, clientConfig.getHeartbeatExecutorThreadPoolSize(),
                0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), 
                new ThreadFactoryBuilder().setNameFormat("DiscoveryClient-HeartbeatExecutor-%d")
                                          .setDaemon(true)
                                          .build()
            );
            
            cacheRefreshExecutor = new ThreadPoolExecutor(
                1, clientConfig.getCacheRefreshExecutorThreadPoolSize(),
                0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryBuilder().setNameFormat("DiscoveryClient-HeartbeatExecutor-%d")
                                          .setDaemon(true)
                                          .build()
            );
            
            eurekaTransport = new EurekaTransport();
            scheduleServerEndpointTask(eurekaTransport, args);
        }
    }
}
```



## 怎么注册



## 注册了什么




