# track-rpc
rpc远程调用

使用方式：

主要包含：服务端(server)、客户端(client)、共用包(common)、测试包(test)

1、配置服务端（在服务端的spring.xml文件中添加如下配置）：
  <context:property-placeholder location="server-rpc.properties"/>/*服务器配置文件*/
  
  <context:component-scan base-package="com.track.test.server"> /*其中的com.track.test.server是服务端扫描的包*/
  
  <!--配置服务注册组件-->
  <bean name="serviceDiscovery" class="com.track.server.registry.ZkServiceRegistry">
     <constructor-arg name="registryAddress" value="${registry.address}"/>
  </bean>
  <!--配置rpc代理-->
  <bean name="rpcProxy" class="com.track.server.RpcServer">
    <constructor-arg name="serverAddressPort" value="${server.address.port}"/>
    <constructor-arg name="serviceRegistry" ref="serviceDiscovery"/>
  </bean>
  
  server-rpc.properties内容：
  # ZooKeeper 服务器
  registry.address=hadoop01:2181,hadoop02:2181,hadoop03:2181
  # RPC 服务器
  server.address.port=8000
2、启动服务端:
  new ClassPathXmlApplicationContext("classpath:server-spring.xml");
  
3、配置客户端（在客户端的spring.xml文件中添加如下配置）：
  <context:property-placeholder location="client-rpc.properties"/>

  <!--配置服务发现-->
  <bean name="serviceDiscovery" class="com.track.client.discover.ZkServiceDiscovery">
      <constructor-arg name="registryAddress" value="${registry.address}"/>
  </bean>
  
  <!--配置rpc代理-->
  <bean name="rpcProxy" class="com.track.client.proxy.RpcProxy">
      <constructor-arg name="serviceDiscovery" ref="serviceDiscovery"/>
  </bean>
  
  <!--使用@RpcAutowired自动注入-->
  <bean name="autoInject" class="com.track.client.inject.RpcAutoInject"/>
  
  client-rpc.properties文件配置：
  # ZooKeeper 服务器
  registry.address=hadoop01:2181,hadoop02:2181,hadoop03:2181

4、在spring的客户端使用rpc远程调用:
  方式一:
  @Autowired
  private RpcProxy proxy;
  
  func(){
    Service service = proxy.newProxy(Service.class);// 提供服务的接口
    service.xxx();// 调用Service中的方法即可
  }
  
  方式二:
  @RpcAutowired
  private Service service;
  
  func(){
    service.xxx();// 直接调用service的方法即可
  }
  
  


