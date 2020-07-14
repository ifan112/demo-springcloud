### @LoadBalanced 是如何激活负载均衡的？
`@LoadBalanced` 只是一个标识，用于注解 `RestTemplate` 组件。

在 `spring-cloud-commons` 包中有 `LoadBalancerAutoConfiguration` 这个自动配置类，它的启用条件之一是有 `LoadBalancerClient` 组件。
在引入 `spring-cloud-netflix-ribbon` 包时，这个就由基于 Netflix Ribbon 原生的 `ZoneAwareBalancerClient` 的 `RibbonLoadBalancerClient` 实现。

`LoadBalancerAutoConfiguration` 在启用之后，内部收集了所有由 `@LoadBalanced` 标识的 `RestTemplate` 组件，
然后对其自定义即添加了一个基于上面的 `RibbonLoadBalancerClient` 实现的 `LoadBalancerInterceptor` 请求拦截器。

```java
@ConditionalOnBean(LoadBalancerClient.class)
public class LoadBalancerAutoConfiguration {

    /**
     * 注意这个 @LoadBalanced 注解的用法，它将会导致只注入由其注解了的 RestTemplate 组件。
     *
     * 原理在于 @LoadBalanced 有一个 @Qualifier 注解，Spring 在注入时会特殊处理。
     * 参考：QualifierAnnotationAutowireCandidateResolver
     */
    @LoadBalanced
    @Autowired
    private List<RestTemplate> restTemplates = Collections.emptyList();

    @ConfigurationOnMissingClass("org.springframework.retry.support.RetryTemplate")
    static class LoadBalancerInterceptorConfig {
        
        /**
         * 注册基于 Netflix Ribbon ZoneAwareLoadBalancer 的 LoadBalancerInterceptor 
         */
        @Bean
        public LoadBalancerInterceptor ribbonInterceptor(
                    LoadBalancerClient loadBalancerClient, 
                    LoadBalancerRequestFactory requestFactory) {
            return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public RestTemplateCustomizer restTemplateCustomizer(
                final LoadBalancerInterceptor loadBalancerInterceptor) {
            return restTemplate -> {
                List<ClientHttpRequestInterceptor> list 
                        = new ArrayList<>(restTemplate.getInterceptors());
                // 将基于 Netflix Ribbon ZoneAwareLoadBalancer 的 LoadBalancerInterceptor 请求拦截器注册到 RestTemplate 组件中
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
        }
    }
}
```


之后，当通过 `RestTemplate` 发起请求时，LoadBalancerInterceptor 这个请求拦截器将会拦截请求，解析请求路径中 serverId，
然后根据负载均衡算法选择一个服务实例发出请求。

那么，基于 ZoneAwareLoadBalancer 是如何获取到服务注册信息的？

### Spring Cloud 是如何支持服务注册与发现组件的？

Spring Cloud 支持使用不同的服务注册与发现的组件，例如，Netflix Eureka、Consule 和 Alibaba Nacos。这是如何实现的呢？

不管使用何种服务注册与发现的组件，对于客户端的负载均衡目前都是使用 Netflix Ribbon 这个组件，具体实现是 `ZoneAwareLoadBalancer`。
如下，而它内部又依赖了一个 `ServerList` 组件负责提供所有的服务实例，然后依据负载均衡策略选择出一个指定的服务实例用于后面的请求。
```java
public class ZoneAwareLoadBalancer extends DynamicServerListLoadBalancer {
    // 继承自父类 DynamicServerListLoadBalancer
    // 不同的服务注册与发现的组件可以实现这个接口，用于提供服务列表
    volatile ServerList serverList;
} 
```

那么，在创建 `ZoneAwareLoadBalancer` 实例时提供一个 `ServerList` 的不同实现即可做到替换。以基于 Nacos 实现的服务注册与发现为例。

```java
@Configuration
public class NacosRibbonClientConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config,
            NacosDiscoveryProperties nacosDiscoveryProperties) {
        // Nacos 基于自身机制提供了 ServerList 的实现
        NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
        serverList.initWithNiwsConfig(config);

        return serverList; 
    }
}
``` 

另一方面

```java
public class RibbonClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
            ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
            IRule rule, IPing ping, ServerListUpdater serverListUpdater) {
        if (this.propertiesFactory.isSet(ILoadBalancer.class, name)) {
            return this.propertiesFactory.get(ILoadBalancer.class, config, name);
        }

        // 创建 ZonAwareLoadBalancer 实例的 ServerList 组件就是 NacosServerList
        return new ZoneAwareLoadBalancer<>(config, rule, ping, serverList, serverListFilter, serverListUpdater); 
    }

}
```




















