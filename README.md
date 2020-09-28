## SpringBoot整合Redis实现消息队列的发布订阅模式

### Redis下的消息队列有两种实现模式：`生产者消费者模式` 及 `发布订阅模式`

本篇将结合Relax中的真实业务场景，从零开始详细说明如何使用 `发布订阅模式` 实现简单的功能。以下内容中涉及到的代码及配置均可直接在新版微服务框架中使用，但仅供参考。

#### 1. 依赖管理

**pom.xml文件中引入以下依赖:**

spring-boot-web基础依赖：

````xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
````

整合Redis：

````xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
````

Spring-boot-aop切面相关（具体业务中使用）：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

#### 2. 环境配置

本文使用yml文件作为环境配置文件：

````yaml
配置服务
server:
  port: yourPort
  servlet:
    context-path: /yourContextPath


配置Redis
spring:
  redis:
    host: yourRedisHost
    port: yourRedisPort
````



### 发布端实现：

#### 1. 添加通用Redis模版覆盖默认模版

定义一个 `redisTemplate` bean对象覆盖默认的模版对象，用于负责Redis下的所有接口调用。该bean主要配置了redis中不同key，value的具体序列化方式，避免乱码问题。其中，key和hash的key采用String的序列化方式，value和hash的value使用Json的序列化方式。同时为了开发方便，模版直接使用 `<String, Object>` 类型。

````java
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnfactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnfactory);

        Jackson2JsonRedisSerializer jsonSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jsonSerializer.setObjectMapper(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
````

#### 2. 定义一个消息监听容器用于添加监听器和订阅主题

spring-redis使用了`RedisMessageListenerContainer`进行消息监听，客户端需要自己实现`MessageListener`（监听器），并配合指定的`topic`（主题）注册到监听容器中，这样当指定主题下有消息时，容器会通知给监听器，监听器即可获取到消息并作出处理。

向容器中注册前，可对外提供一个空的Map，用于添加多个监听器和对应的主题。其他应用中的配置可以通过获取这个bean来向容器中添加自定义的监听器和主题。

````java
    @Bean("messageListeners")
    public Map<MessageListener, Set<Topic>> messageListeners() {
        return Maps.newHashMap();
    }
````

添加完成后，可将这个bean作为参数注册到监听容器中。注意当监听容器中没有主题时会抛出异常，所以可以判断当Map为空就不创建这个bean了。

````java
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("messageListeners") Map<MessageListener, Set<Topic>> messageListeners,
            RedisConnectionFactory redisConnfactory) {
        if (messageListeners.size() == 0) {
            return null;
        }
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnfactory);
        container.setMessageListeners(Collections.unmodifiableMap(messageListeners));
        List<String> tNames = Lists.newArrayList();
        messageListeners.forEach((key, value) -> tNames
                .addAll(value.stream().map(Topic::getTopic).collect(Collectors.toList())));
        log.info("Redis消息订阅池配置完毕，已成功订阅{}个主题: {}", tNames.size(), tNames);
        return container;
    }
````

#### 3. 实现一个消息发布者类

消息发布者负责操作Redis进行消息广播，实现非常简单。绑定redisTemplate后即可简单封装一下直接调用。

````java
@Slf4j 
@Component
public class RedisMessagePublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publish(String topic, Object message) {
        log.info("Publishing message...");
        redisTemplate.convertAndSend(topic, message);
    }
}
````

#### 4. 测试消息发布

用一个简单的Controller验证消息是否可以正常发布：

````java
@RestController
@RequestMapping(value = "/redisMsg")
public class PublishController {

    @Autowired
    private RedisMessagePublisher publisher;

    @GetMapping("/publish/{topic}/{message}")
    public String publishTopicMsg(@PathVariable("topic") String topic,
                                  @PathVariable("message") String message) {
        publisher.publish(topic, message);
        return "Publishing message...";
    }
}
````

配置好环境参数后，即可启动服务进行验证。如我的本地服务参数如下：

````yaml
 配置服务
server:
  port: 9090
  servlet:
    context-path: /MQ


 配置Redis
spring:
  redis:
    host: 172.17.189.164
    port: 31602
````

可以使用 `curl` 命令进行测试验证:

````shell
curl -X GET http://localhost:9090/MQ/redisMsg/publish/test/test123
````

结果：

服务终端显示日志信息：

````
2020-09-02 22:02:09.487  INFO 47985 h.y.t.key.message.RedisMessagePublisher  : Publishing message...
````

客户端返回信息：

````shell
Publishing message...
````

可以看出已经成功的进行了消息的广播。至此，消息发布端的功能就完成了简单的实现，接下来进行消息订阅端的功能实现。



### 订阅端实现：

订阅端的Redis配置与发布端相同，在此不再赘述。

每个订阅端都是一个客户端，客户端可根据自己的不同需求实现不同的监听器，订阅不同的主题内容。

#### 1. 实现一个监听器

监听器的实现方式有两种，一种是直接新建一个监听器类，类内需要实现一个默认的监听方法。另一种是实现 `MessageListener` 接口，通过调用接口中的 `onMessage()` 方法进行消息的接收。

1）新建一个监听器类：

````java
@Slf4j
@Component
public class RedisMessageReceiver {

    public void receiveMsg(Object msg) {
        log.info("Received message...");
        log.info(JSONObject.toJSONString(msg));
    }
}
````

2）实现 `MessageListener` 接口：

可以绑定redisTempleate获取反序列化器，将消息内容反序列化为定义的格式。

````java
@Slf4j
@Component
public class RedisMessageSubscriber implements MessageListener {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        Object obj = redisTemplate.getValueSerializer().deserialize(message.getBody());
        String channel = new String(message.getChannel());
        log.info("Received message...");
        log.info(channel + ": " + JSONObject.toJSONString(obj));
    }
}
````

#### 2. 定义一个监听器bean配置进SpringBoot中

对应监听器的实现方式，配置监听器也有两种方法。

1）配置新建的监听器类：

new一个`MessageListenerAdapter`，第一个参数为监听器对象，第二个参数为此监听器中的监听方法名。

````java
    @Bean("listener1")
    public MessageListenerAdapter listener1(RedisMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMsg");
    }
````

2）配置实现 `MessageListener` 接口的监听器类：

直接new一个接口实现类的对象即可。

````java
    @Bean("listener2")
    public RedisMessageSubscriber listener2() {
        return new RedisMessageSubscriber();
    }
````

#### 3. 定义主题，并将其添加至消息监听容器中

主题有两种定义的方式，使用`ChannelTopic` 或 `PatternTopic`，前者会对主题字符串进行全匹配，后者则会匹配表达式。此处仅演示`ChannelTopic`的定义方式。

获取发布端对外提供的Map，向其中添加不同的监听器和对应的主题，如此处添加了主题`chatroom` 和 `test`

````java
    @Bean("topic1")
    public ChannelTopic topic1(@Qualifier("listener1") MessageListener listener,
                               @Qualifier("messageListeners") Map<MessageListener, Set<Topic>> messageListeners)		{
        ChannelTopic topic = ChannelTopic.of("chatroom");
        messageListeners.put(listener, Collections.singleton(topic));
        return topic;
    }
````

````java
    @Bean("topic2")
    public ChannelTopic topic2(@Qualifier("listener2") MessageListener listener,
                               @Qualifier("messageListeners") Map<MessageListener, Set<Topic>> messageListeners)  		{
        ChannelTopic topic = ChannelTopic.of("test");
        messageListeners.put(listener, Collections.singleton(topic));
        return topic;
    }
````

为保证监听器容器能够加载到所有的主题配置，需要控制bean的加载顺序，在定义`redisMessageListenerContainer`的方法上添加注解`@DependsOn`即可。

````java
    @Bean
    @DependsOn({"topic1", "topic2"})
    public RedisMessageListenerContainer redisMessageListenerContainer(...) {
      	...
    }
````

#### 4. 测试消息接收

依然使用上文中定义的controller和环境配置进行测试，启动服务进行验证。

可在服务启动时在终端看到日志显示订阅配置：

````
2020-09-02 23:14:42.522  INFO 51227 heart.your.to.key.config.RedisConfig     : Redis消息订阅池配置完毕，已成功订阅2个主题: [chatroom, test]
````



使用 `curl` 命令进行测试验证:

````shell
curl -X GET http://localhost:9090/MQ/redisMsg/publish/test/helloworld
````

结果：

服务终端显示日志信息：

````
2020-09-02 23:28:16.712  INFO 51227  h.y.t.key.message.RedisMessagePublisher  : Publishing message...
2020-09-02 23:28:16.726  INFO 51227  h.y.to.key.message.RedisMessageReceiver  : Received message...
2020-09-02 23:28:16.727  INFO 51227  h.y.to.key.message.RedisMessageReceiver  : "\"helloworld\""
````

客户端返回信息：

````shell
Publishing message...
````



使用 `curl` 命令进行测试验证:

````
curl -X GET http://localhost:9090/MQ/redisMsg/publish/chatroom/hello-everyone
````

结果：

服务终端显示日志信息：

````
2020-09-02 23:32:18.985  INFO 51227  h.y.t.key.message.RedisMessagePublisher  : Publishing message...
2020-09-02 23:32:30.345  INFO 51227  h.y.t.k.message.RedisMessageSubscriber   : Received message...
2020-09-02 23:33:40.076  INFO 51227  h.y.t.k.message.RedisMessageSubscriber   : chatroom: "hello-everyone"
````

客户端返回信息：

````
Publishing message...
````

可以看出，客户端已经成功的接收到了订阅主题中的内容。

至此，消息订阅端的功能实现也完成了。



### 结合Relax业务

目前微服务架构下的身份中心业务涉及到了消息的发布和订阅。当身份中心里的各类实例数据发生更新（增、删、改）时，服务意向外广播变更信息，然后接收到此信息的应用或服务即可同步数据的变更。身份中心中结合使用切面和redis实现了数据变更消息的对外同步与订阅。

#### 1. 添加一个切面类

添加一个切面类的方法很简单，在一个spring组件类上加上`@Aspect`注解即可。

切面中会用到先前定义的消息发送者类，直接注入进来即可。

````java
@Aspect
@Component
public class DataUpdateMQAspect {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;
		...
}
````

向切面中添加一个切点方法，指定拦截的接口。此处以`UserService`为例。`@Pointcut`注解中的配置及表达式规则在此不赘述了，可以自行查询。

````java
@Aspect
@Component
public class DataUpdateMQAspect {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;
		
    private static final String executions = "&&(execution(* create(..))||execution(* delete(..))||"
                                             + "execution(* update(..))||execution(* batch*(..)))";

  	@Pointcut("target(com.its.ione.v3.idc.api.service.impl.UserServiceImpl)" + executions)
    public void syncUser() {
    }
}
````

添加一个通知方法，用于指定当切点方法被执行时需要做什么。`@AfterReturning`是一个通知注解，代表当切点方法被成功执行并返回后才会触发该通知方法。常见的通知注解一共有5个，可以根据需求变化，具体含义在此不再赘述了，可以自行查询。

```java
@Aspect
@Component
public class DataUpdateMQAspect {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;
		
    private static final String executions = "&&(execution(* create(..))||execution(* delete(..))||"
                                             + "execution(* update(..))||execution(* batch*(..)))";

  	@Pointcut("target(com.its.ione.v3.idc.api.service.impl.UserServiceImpl)" + executions)
    public void syncUser() {
    }
  
  	@AfterReturning(pointcut = "syncUser()", returning = "retVal")
    public void publishUserMsg(JoinPoint point, Object retVal) {
        //具体业务具体处理
        //redisMessagePublisher.publish("data changed!", retVal)
    }
}
```

#### 2. 测试消息发布及接收

启动身份中心服务后，调用User相关的create接口，结果如下：

消息主题：

````
Msg@ione-identity-center/domain_data/update
````

`curl`命令：

````shell
 curl -X POST "http://ip:port/identity-center/v3/api/user/" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"account\": \"lichenke\", \"password\": \"4321\"}"
````

消息内容：

````
{"timeStamp":1599103582814,"handleType":"create","param":[{"password":"4421","id":"3d9d2fcc-ed95-11ea-a40a-bfed95aa3784","account":"lichenke"}],"ids":["3d9d2fcc-ed95-11ea-a40a-bfed95aa3784"],"type":"08a6df22-d146-11ea-a129-7f22afd72543"}
````

将消息内容格式化为Json格式如下：

````json
{
    "timeStamp": 1599103582814,
    "handleType": "create",
    "param": [
        {
            "password": "4421",
            "id": "3d9d2fcc-ed95-11ea-a40a-bfed95aa3784",
            "account": "lichenke"
        }
    ],
    "ids": [
        "3d9d2fcc-ed95-11ea-a40a-bfed95aa3784"
    ],
    "type": "08a6df22-d146-11ea-a129-7f22afd72543"
}
````

