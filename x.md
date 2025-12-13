

# @SpringBootApplication Visual Architecture

## 1. Annotation Composition

```
┌─────────────────────────────────────────────────────────────┐
│                  @SpringBootApplication                      │
│                                                              │
│  What you write:                                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ @SpringBootApplication                               │   │
│  │ public class MyApplication {                         │   │
│  │     public static void main(String[] args) {         │   │
│  │         SpringApplication.run(MyApplication.class);  │   │
│  │     }                                                 │   │
│  │ }                                                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  What Spring sees (Meta-annotation composition):            │
│                                                              │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │ @SpringBootConfiguration│ @EnableAutoConfiguration│      │
│  │ (extends @Configuration)│ (The Magic Trigger)     │      │
│  └──────────────────────┘  └──────────────────────┘        │
│                                                              │
│  ┌──────────────────────┐                                   │
│  │   @ComponentScan      │                                   │
│  │ (Finds your beans)    │                                   │
│  └──────────────────────┘                                   │
└─────────────────────────────────────────────────────────────┘
```

## 2. Complete Bootstrap Flow

```
START: SpringApplication.run()
│
├─→ Phase 1: INITIALIZATION (~100ms)
│   │
│   ├─→ Deduce Application Type
│   │   ├─ DispatcherServlet present? → SERVLET
│   │   ├─ DispatcherHandler present? → REACTIVE  
│   │   └─ Neither? → NONE
│   │
│   ├─→ Load ApplicationContextInitializers
│   │   └─ From META-INF/spring.factories
│   │
│   └─→ Load ApplicationListeners
│       └─ From META-INF/spring.factories
│
├─→ Phase 2: ENVIRONMENT PREPARATION (~200ms)
│   │
│   ├─→ Create Environment (StandardEnvironment)
│   │
│   ├─→ Load Property Sources (17 locations)
│   │   ├─ 1. Command line args (--server.port=8080)
│   │   ├─ 2. SPRING_APPLICATION_JSON
│   │   ├─ 3. System properties
│   │   ├─ 4. OS environment variables
│   │   ├─ 5. application-{profile}.yml
│   │   └─ ... (12 more locations)
│   │
│   └─→ Activate Profiles
│       ├─ Check: spring.profiles.active
│       └─ Result: [dev, local] or [default]
│
├─→ Phase 3: CONTEXT CREATION (~50ms)
│   │
│   ├─→ Create ApplicationContext
│   │   ├─ SERVLET → AnnotationConfigServletWebServerApplicationContext
│   │   ├─ REACTIVE → AnnotationConfigReactiveWebServerApplicationContext
│   │   └─ NONE → AnnotationConfigApplicationContext
│   │
│   └─→ Initialize BeanFactory
│       └─ DefaultListableBeanFactory created
│
├─→ Phase 4: AUTO-CONFIGURATION (~800ms) ★ CRITICAL
│   │
│   ├─→ Load ALL auto-config candidates
│   │   └─ From spring.factories (~130 classes)
│   │
│   ├─→ Filter using @Conditional annotations
│   │   │
│   │   ├─ DataSourceAutoConfiguration
│   │   │  ├─ @ConditionalOnClass(DataSource.class) → ✓ YES
│   │   │  ├─ @ConditionalOnMissingBean(DataSource.class) → ✓ YES
│   │   │  └─ RESULT: INCLUDE
│   │   │
│   │   └─ RedisAutoConfiguration
│   │      ├─ @ConditionalOnClass(RedisOperations.class) → ✗ NO
│   │      └─ RESULT: SKIP
│   │
│   └─→ Result: ~20-30 configs pass, ~100-110 excluded
│
├─→ Phase 5: COMPONENT SCANNING (~150ms)
│   │
│   ├─→ Scan packages: com.example.myapp.**
│   │
│   ├─→ Find annotated classes
│   │   ├─ @Component
│   │   ├─ @Service
│   │   ├─ @Repository
│   │   ├─ @Controller
│   │   └─ @Configuration
│   │
│   └─→ Register BeanDefinitions
│       └─ Found: 47 components
│
├─→ Phase 6: BEAN INSTANTIATION (~700ms)
│   │
│   ├─→ Process @ConfigurationProperties
│   │   └─ Bind properties from Environment
│   │
│   ├─→ Process @Value injections
│   │   └─ Resolve ${...} and #{...}
│   │
│   └─→ Instantiate beans (dependency order)
│       ├─ 1. DataSource
│       ├─ 2. EntityManagerFactory
│       ├─ 3. TransactionManager
│       ├─ 4. Repositories
│       ├─ 5. Services
│       └─ 6. Controllers
│
├─→ Phase 7: WEB SERVER START (~200ms)
│   │
│   ├─→ Create TomcatServletWebServerFactory
│   │
│   ├─→ Configure Tomcat
│   │   ├─ Port: 8080
│   │   └─ Context: /
│   │
│   └─→ Start embedded Tomcat
│       └─ Listening on port 8080
│
└─→ Phase 8: APPLICATION READY
    │
    ├─→ Fire: ApplicationReadyEvent
    │
    └─→ RESULT: Started in 2.847 seconds 🚀
```

## 3. Auto-Configuration Decision Tree

```
DataSourceAutoConfiguration Decision Tree:
│
├─→ Question 1: Is DataSource.class on classpath?
│   @ConditionalOnClass(DataSource.class)
│   │
│   ├─→ YES ✓ → Continue
│   └─→ NO ✗ → SKIP entire config → END
│
├─→ Question 2: User defined DataSource bean?
│   @ConditionalOnMissingBean(DataSource.class)
│   │
│   ├─→ FOUND ✗ → SKIP (use user's bean) → END
│   └─→ NOT FOUND ✓ → Continue
│
├─→ Question 3: Is R2DBC ConnectionFactory present?
│   @ConditionalOnMissingBean(ConnectionFactory.class)
│   │
│   ├─→ FOUND ✗ → Use R2DBC instead → END
│   └─→ NOT FOUND ✓ → Continue
│
└─→ Question 4: Which DataSource type?
    │
    ├─→ Option A: User specified type?
    │   spring.datasource.type=com.zaxxer.hikari.HikariDataSource
    │   │
    │   └─→ YES ✓ → Use specified type → END
    │
    ├─→ Option B: Embedded database?
    │   @Conditional(EmbeddedDatabaseCondition)
    │   │
    │   ├─ No spring.datasource.url AND
    │   └─ H2/HSQL/Derby on classpath?
    │      │
    │      └─→ YES ✓ → Create embedded H2 → END
    │
    └─→ Option C: Pooled DataSource (Priority order)
        │
        ├─→ 1. Try HikariCP
        │   @ConditionalOnClass(HikariDataSource.class)
        │   │
        │   ├─→ FOUND ✓ → Create HikariDataSource → END
        │   └─→ NOT FOUND → Try next
        │
        ├─→ 2. Try Tomcat JDBC Pool
        │   @ConditionalOnClass(org.apache.tomcat.jdbc.pool.DataSource)
        │   │
        │   ├─→ FOUND ✓ → Create TomcatDataSource → END
        │   └─→ NOT FOUND → Try next
        │
        ├─→ 3. Try Commons DBCP2
        │   @ConditionalOnClass(BasicDataSource.class)
        │   │
        │   ├─→ FOUND ✓ → Create BasicDataSource → END
        │   └─→ NOT FOUND → Try next
        │
        └─→ 4. Try Oracle UCP
            @ConditionalOnClass(PoolDataSourceImpl.class)
            │
            ├─→ FOUND ✓ → Create PoolDataSourceImpl → END
            └─→ NOT FOUND → FAIL (No pooling available)
```

## 4. Property Resolution Flow

```
Resolving: @Value("${server.port:8080}")
│
├─→ Step 1: Parse Expression
│   │
│   ├─ Placeholder: "server.port"
│   ├─ Default: "8080"
│   └─ Type: String (convert to int later)
│
├─→ Step 2: Search PropertySources (in order)
│   │
│   ├─→ Source 1: commandLineArgs
│   │   Search: "server.port"
│   │   Result: FOUND "9090" ✓
│   │   └─→ STOP SEARCHING (first match wins!)
│   │
│   ├─→ Source 2: SPRING_APPLICATION_JSON
│   │   (Skipped - already found)
│   │
│   ├─→ Source 3: systemProperties
│   │   (Skipped - already found)
│   │
│   └─→ Source 14: application.yml
│       (Would find "8080" here if not found earlier)
│
├─→ Step 3: Type Conversion
│   │
│   ├─ Input: "9090" (String)
│   ├─ Target: int
│   └─ ConversionService.convert("9090", Integer.class)
│       └─→ Result: 9090 (Integer)
│
└─→ Step 4: Inject into Field
    │
    └─→ ReflectionUtils.setField(field, bean, 9090)
        └─→ private int port = 9090; ✓

FINAL RESULT: port = 9090
```

## 5. Profile Activation Flow

```
Profile Activation Flow:
│
├─→ Step 1: Determine Active Profiles (Priority Order)
│   │
│   ├─→ 1. Command Line (HIGHEST)
│   │   --spring.profiles.active=prod,monitoring
│   │   FOUND ✓ → Active: [prod, monitoring]
│   │
│   ├─→ 2. Environment Variable
│   │   SPRING_PROFILES_ACTIVE=staging
│   │   (Overridden by command line)
│   │
│   ├─→ 3. application.properties
│   │   spring.profiles.active=dev
│   │   (Overridden by above)
│   │
│   └─→ 4. Default (if none specified)
│       Active: [default]
│
├─→ Step 2: Apply Profile Groups (Spring Boot 2.4+)
│   │
│   │ spring.profiles.group.production=[prod, cloud, monitoring]
│   │
│   │ Active: "production"
│   └─→ Expands to: [prod, cloud, monitoring]
│
├─→ Step 3: Load Profile-Specific Files (in order)
│   │
│   ├─→ 1. application.yml (base)
│   │   server.port: 8080
│   │   app.name: MyApp
│   │
│   ├─→ 2. application-prod.yml
│   │   server.port: 9000 ← OVERRIDES base
│   │   logging.level: WARN
│   │
│   └─→ 3. application-monitoring.yml
│       metrics.enabled: true
│
└─→ Step 4: Activate Profile-Specific Beans
    │
    ├─→ @Bean @Profile("prod")
    │   prodDataSource() ← ACTIVE ✓
    │
    ├─→ @Bean @Profile("dev")
    │   devDataSource() ← INACTIVE ✗
    │
    └─→ @Bean @Profile("!prod")
        mockService() ← INACTIVE ✗ (NOT prod)
```

## 6. @ConfigurationProperties vs @Value

```
╔══════════════════════════════════════════════════════════╗
║     Property Injection: Performance Comparison           ║
╚══════════════════════════════════════════════════════════╝

@Value Approach (Field-by-Field):
┌────────────────────────────────────────────────────────┐
│ @Component                                              │
│ public class AppConfig {                                │
│                                                         │
│   @Value("${app.name}")                                │
│   private String name;           ← Individual resolver │
│                                     (5-10ms each)       │
│   @Value("${app.timeout}")                             │
│   private Duration timeout;      ← Individual resolver │
│                                                         │
│   @Value("${app.max-connections}")                     │
│   private int maxConnections;    ← Individual resolver │
│                                                         │
│   // 10 fields = ~50-100ms total                       │
│ }                                                       │
└────────────────────────────────────────────────────────┘

@ConfigurationProperties Approach (Bulk Binding):
┌────────────────────────────────────────────────────────┐
│ @ConfigurationProperties(prefix = "app")                │
│ @Validated                                              │
│ public class AppProperties {                            │
│                                                         │
│   @NotBlank                                             │
│   private String name;                                  │
│                                                         │
│   @NotNull                                              │
│   private Duration timeout;                             │
│                                                         │
│   @Min(1) @Max(1000)                                   │
│   private int maxConnections;                           │
│                                                         │
│   private Database database;     ← Nested structure    │
│                                                         │
│   @Data                                                 │
│   public static class Database {                        │
│     private String host;                                │
│     private int port;                                   │
│   }                                                     │
│                                                         │
│   // ALL fields bound at once: ~5-10ms total           │
│   // + JSR-303 validation                              │
│   // + IDE autocomplete                                │
│ }                                                       │
└────────────────────────────────────────────────────────┘

Performance Comparison:
┌─────────────────────────────────────────────────────┐
│ Properties  │  @Value   │  @ConfigurationProperties │
├─────────────────────────────────────────────────────┤
│     10      │   50ms    │         5ms               │
│     50      │  250ms    │        10ms               │
│    100      │  500ms    │        15ms               │
└─────────────────────────────────────────────────────┘

Recommendation: Use @ConfigurationProperties!
```

## 7. Complete Request Flow

```
HTTP Request Flow in Spring Boot Application:
│
START: HTTP GET /users/123
│
├─→ 1. TOMCAT receives request
│   Port: 8080
│   Thread: http-nio-8080-exec-1
│
├─→ 2. SERVLET FILTERS
│   ├─ CharacterEncodingFilter (UTF-8)
│   ├─ FormContentFilter
│   ├─ RequestContextFilter
│   └─ (Custom filters if any)
│
├─→ 3. DISPATCHER SERVLET
│   org.springframework.web.servlet.DispatcherServlet
│   │
│   ├─→ Find Handler Mapping
│   │   RequestMappingHandlerMapping
│   │   └─→ Match: UserController.getUser(Long id)
│   │
│   ├─→ Find Handler Adapter
│   │   RequestMappingHandlerAdapter
│   │
│   └─→ Apply Interceptors (preHandle)
│       ├─ LoggingInterceptor
│       └─ AuthenticationInterceptor
│
├─→ 4. CONTROLLER
│   @RestController
│   public class UserController {
│       
│       @GetMapping("/users/{id}")
│       public UserDTO getUser(@PathVariable Long id) {
│           │
│           ├─→ Argument Resolution
│           │   @PathVariable id = 123
│           │
│           └─→ Call Service Layer
│               return userService.findById(id);
│       }
│   }
│
├─→ 5. SERVICE LAYER
│   @Service
│   @Transactional(readOnly = true)
│   public class UserService {
│       
│       public UserDTO findById(Long id) {
│           │
│           ├─→ Transaction Start
│           │   JpaTransactionManager
│           │   └─→ EntityManager.getTransaction().begin()
│           │
│           ├─→ Call Repository
│           │   User user = userRepository.findById(id);
│           │
│           ├─→ Map to DTO
│           │   UserDTO dto = mapper.toDTO(user);
│           │
│           └─→ Transaction Commit
│               └─→ EntityManager.getTransaction().commit()
│       }
│   }
│
├─→ 6. REPOSITORY LAYER (JPA)
│   @Repository
│   public interface UserRepository extends JpaRepository<User, Long> {
│       // Spring Data JPA auto-implements at runtime
│   }
│   │
│   ├─→ Spring Data JPA Proxy
│   │   SimpleJpaRepository (generated)
│   │
│   └─→ EntityManager.find(User.class, 123)
│
├─→ 7. HIBERNATE (ORM)
│   │
│   ├─→ Check 1st Level Cache (Session)
│   │   NOT FOUND → Continue
│   │
│   ├─→ Check 2nd Level Cache (if enabled)
│   │   NOT FOUND → Continue
│   │
│   ├─→ Generate SQL
│   │   SELECT u.id, u.name, u.email
│   │   FROM users u
│   │   WHERE u.id = ?
│   │
│   └─→ Execute via JDBC
│
├─→ 8. JDBC LAYER
│   │
│   ├─→ Get Connection (HikariCP Pool)
│   │   Pool: [10 connections]
│   │   Available: 7
│   │   └─→ Lease connection #3
│   │
│   ├─→ Prepare Statement
│   │   PreparedStatement ps = conn.prepareStatement(sql)
│   │   ps.setLong(1, 123)
│   │
│   └─→ Execute Query
│       ResultSet rs = ps.executeQuery()
│
├─→ 9. DATABASE DRIVER
│   MySQL Connector/J
│   │
│   ├─→ Convert to MySQL protocol
│   └─→ Send over TCP/IP
│       Host: localhost:3306
│
├─→ 10. DATABASE SERVER
│   MySQL Server
│   │
│   ├─→ Parse SQL
│   ├─→ Optimize query
│   ├─→ Execute (using index on id)
│   └─→ Return result set
│
├─→ RESPONSE PATH (reverse order)
│   │
│   ├─→ ResultSet → JDBC
│   ├─→ Map to User entity → Hibernate
│   ├─→ Return User → Repository
│   ├─→ Map to UserDTO → Service
│   ├─→ Return UserDTO → Controller
│   │
│   └─→ HTTP Response Conversion
│       ├─→ Apply @ResponseBody
│       ├─→ HttpMessageConverter
│       │   (MappingJackson2HttpMessageConverter)
│       ├─→ Convert UserDTO → JSON
│       │   {"id":123,"name":"John","email":"john@example.com"}
│       └─→ Write to response stream
│
└─→ END: HTTP 200 OK
    Content-Type: application/json
    Response: {"id":123,"name":"John","email":"john@example.com"}

Total Time: ~150ms
├─ Network: 5ms
├─ Tomcat/Filters: 2ms
├─- DispatcherServlet: 3ms
├─ Controller: 1ms
├─ Service: 1ms
├─ Transaction: 5ms
├─ Repository/Hibernate: 10ms
├─ JDBC: 5ms
├─ Database: 100ms ← Usually the bottleneck!
└─ Response serialization: 18ms
```

## 8. Bean Lifecycle

```
Bean Lifecycle in Spring Boot:
│
START: Bean Definition Registered
│
├─→ 1. INSTANTIATION
│   │
│   ├─→ Constructor Selection
│   │   ├─ @Autowired constructor?
│   │   ├─ Single constructor? (implicit)
│   │   └─ No-arg constructor (default)
│   │
│   └─→ Create Instance
│       new UserService()
│
├─→ 2. POPULATE PROPERTIES
│   │
│   ├─→ @Autowired fields
│   │   private UserRepository userRepository;
│   │   └─→ Inject dependency
│   │
│   ├─→ @Value fields
│   │   private String appName;
│   │   └─→ Resolve and inject
│   │
│   └─→ Setter injection (if any)
│
├─→ 3. BEAN NAME AWARE
│   implements BeanNameAware
│   │
│   └─→ setBeanName("userService")
│
├─→ 4. BEAN FACTORY AWARE
│   implements BeanFactoryAware
│   │
│   └─→ setBeanFactory(beanFactory)
│
├─→ 5. APPLICATION CONTEXT AWARE
│   implements ApplicationContextAware
│   │
│   └─→ setApplicationContext(context)
│
├─→ 6. PRE-INITIALIZATION
│   BeanPostProcessor.postProcessBeforeInitialization()
│   │
│   ├─→ @PostConstruct methods
│   │   @PostConstruct
│   │   public void init() { ... }
│   │
│   └─→ Custom processors
│
├─→ 7. INITIALIZATION
│   │
│   ├─→ InitializingBean.afterPropertiesSet()
│   │   (if implements InitializingBean)
│   │
│   └─→ Custom init-method
│       @Bean(initMethod = "init")
│
├─→ 8. POST-INITIALIZATION
│   BeanPostProcessor.postProcessAfterInitialization()
│   │
│   ├─→ AOP Proxy Creation
│   │   @Transactional, @Cacheable, @Async
│   │   └─→ CGLIB or JDK Dynamic Proxy
│   │
│   └─→ Custom processors
│
├─→ BEAN READY FOR USE ✓
│   Bean stored in ApplicationContext
│   Available for injection
│
... (Application running) ...
│
└─→ 9. DESTRUCTION (Application shutdown)
    │
    ├─→ @PreDestroy methods
    │   @PreDestroy
    │   public void cleanup() { ... }
    │
    ├─→ DisposableBean.destroy()
    │   (if implements DisposableBean)
    │
    └─→ Custom destroy-method
        @Bean(destroyMethod = "cleanup")

END: Bean destroyed
```

---

## Summary

**Key Takeaways:**

1. **@SpringBootApplication** = @Configuration + @EnableAutoConfiguration + @ComponentScan
2. **Auto-configuration** uses @Conditional annotations to intelligently configure beans
3. **Property resolution** follows 17 sources in priority order
4. **@ConfigurationProperties** is 10-50x faster than multiple @Value annotations
5. **Profiles** enable environment-specific configuration
6. **Bean lifecycle** has 9+ phases from instantiation to destruction
7. **Startup time** is dominated by auto-configuration filtering (~800ms of ~2800ms total)


```--------------------------------------------------------------------------------------  






```


# The Application Type
```
┌─────────────────────────────────────────────────────────────┐
│         Dependency Added          →    Application Type     │
├─────────────────────────────────────────────────────────────┤
│  spring-boot-starter-web          →    SERVLET              │
│  spring-boot-starter-webflux      →    REACTIVE             │
│  spring-boot-starter (only)       →    NONE                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Visual: Classpath Detection
```
Your pom.xml/build.gradle
        │
        ▼
┌─────────────────────────────────────┐
│  Add dependency                     │
│  spring-boot-starter-web            │
└─────────────────────────────────────┘
        │
        ▼ (Maven/Gradle downloads JARs)
┌─────────────────────────────────────┐
│  Classpath now contains:            │
│  ✓ spring-webmvc.jar                │
│  ✓ tomcat-embed-core.jar            │
│  ✓ javax.servlet-api.jar            │
│  ✓ DispatcherServlet.class          │
└─────────────────────────────────────┘
        │
        ▼ (Application starts)
┌───────────────────────────────────────────┐
│  SpringApplication.run()                  │
│                                           │
│  WebApplicationType.deduceFromClasspath() │
│    │                                      │
│    ├─→ ClassUtils.isPresent(              │
│    │    "o.s.w.s.DispatcherServlet")      │
│    │    → YES! ✓                          │
│    │                                      │
│    └─→ Return: SERVLET                    │
└───────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│  Create:                            │
│  ServletWebServerApplicationContext │
│                                     │
│  Start: Tomcat on port 8080         │
└─────────────────────────────────────┘
```
## Real Examples
### Example 1: Adding spring-boot-starter-web
```
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

**What this brings to classpath:**
```
spring-boot-starter-web/
├── spring-webmvc
│   └── org.springframework.web.servlet.DispatcherServlet ← KEY CLASS!
├── spring-web
├── tomcat-embed-core
│   └── Embedded Tomcat server
├── tomcat-embed-el
├── tomcat-embed-websocket
├── jackson-databind (JSON conversion)
└── javax.servlet-api
    └── javax.servlet.Servlet ← KEY CLASS!
```
**Detection result:**
```
// Spring Boot checks:
ClassUtils.isPresent("javax.servlet.Servlet", null) → TRUE ✓
ClassUtils.isPresent("o.s.w.c.ConfigurableWebApplicationContext", null) → TRUE ✓

// Result: SERVLET
```

**Console output:**
```
Tomcat started on port(s): 8080 (http)
Started MyApplication in 2.847 seconds
```

## The Actual Source Code
### Here's the exact code Spring Boot uses:
```
// org.springframework.boot.SpringApplication
private WebApplicationType deduceFromClasspath() {
    // Step 1: Try REACTIVE
    if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) 
        && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
        && !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
        return WebApplicationType.REACTIVE;
    }
    
    // Step 2: Try SERVLET
    for (String className : SERVLET_INDICATOR_CLASSES) {
        if (!ClassUtils.isPresent(className, null)) {
            return WebApplicationType.NONE; // Missing servlet class
        }
    }
    return WebApplicationType.SERVLET;
}
```
### The KEY classes it looks for:
```
private static final String WEBFLUX_INDICATOR_CLASS = 
    "org.springframework.web.reactive.DispatcherHandler";

private static final String WEBMVC_INDICATOR_CLASS = 
    "org.springframework.web.servlet.DispatcherServlet";

private static final String JERSEY_INDICATOR_CLASS = 
    "org.glassfish.jersey.servlet.ServletContainer";

private static final String[] SERVLET_INDICATOR_CLASSES = { 
    "javax.servlet.Servlet",
    "org.springframework.web.context.ConfigurableWebApplicationContext" 
};
```

### What ClassUtils.isPresent() does:
```
public static boolean isPresent(String className, ClassLoader classLoader) {
    try {
        // Try to load the class
        Class.forName(className, false, classLoader);
        return true; // Class found on classpath!
    } catch (ClassNotFoundException ex) {
        return false; // Class NOT on classpath
    }
}
``` 
***It's literally checking: "Can I find this class file in the JARs?"***



## Summary

**Yes, it's 100% based on dependencies!**
```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  Your Dependencies (pom.xml/build.gradle)                │
│            ↓                                             │
│  What gets downloaded to classpath                       │
│            ↓                                             │
│  Spring Boot scans: "Which classes exist?"               │
│            ↓                                             │
│  Application Type decided (SERVLET/REACTIVE/NONE)        │
│            ↓                                             │
│  Corresponding ApplicationContext created                │
│            ↓                                             │
│  Appropriate server started (Tomcat/Netty/None)          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```
```















```
# The Complete Flow
```
┌──────────────────────────────────────────────────────────────────┐
│  PHASE 1: BUILD TIME (Maven/Gradle)                              │
└──────────────────────────────────────────────────────────────────┘

Step 1: You run build command
┌────────────────────────────────────┐
│ Developer types:                   │
│ $ mvn clean package                │
│        OR                          │
│ $ ./gradlew build                  │
└────────────────────────────────────┘
        │
        ▼
Step 2: Maven/Gradle reads configuration file
┌───────────────────────────────────────┐
│ pom.xml (Maven):                      │
│ <dependency>                          │
│   <groupId>o.s.boot</groupId>         │
│   <artifactId>                        │
│     spring-boot-starter-web           │
│   </artifactId>                       │
│ </dependency>                         │
│                                       │
│ OR                                    │
│                                       │
│ build.gradle (Gradle):                │
│ dependencies {                        │
│   implementation                      │
│     'o.s.boot:spring-boot-starter-web'│
│ }                                     │
└───────────────────────────────────────┘
        │
        ▼
Step 3: Check LOCAL repository first
┌─────────────────────────────────────────────┐
│ Maven checks:                               │
│ ~/.m2/repository/                           │
│   org/springframework/boot/                 │
│     spring-boot-starter-web/                │
│       3.2.0/                                │
│         spring-boot-starter-web-3.2.0.jar   │
│                                             │
│ Gradle checks:                              │
│ ~/.gradle/caches/modules-2/                 │
│   files-2.1/                                │
│     org.springframework.boot/               │
│       spring-boot-starter-web/              │
│         3.2.0/<hash>/                       │
│           spring-boot-starter-web-3.2.0.jar │
└─────────────────────────────────────────────┘
        │
        ├─→ FOUND? ✓ Skip download, use cached version
        │            (Very fast! ~0.1 seconds)
        │
        └─→ NOT FOUND? ✗ Go to Step 4
        
Step 4: Download from REMOTE repository
┌──────────────────────────────────────┐
│ Maven/Gradle connects to:            │
│                                      │
│ Maven Central:                       │
│ https://repo.maven.apache.org/       │
│   maven2/                            │
│                                      │
│ Download:                            │
│ 1. spring-boot-starter-web-3.2.0.jar │
│ 2. spring-boot-starter-web-3.2.0.pom │ ← Metadata!
└──────────────────────────────────────┘
        │
        ▼
Step 5: Read POM file to find SUB-DEPENDENCIES (Transitive)
┌─────────────────────────────────────┐
│ spring-boot-starter-web-3.2.0.pom:  │
│                                     │
│ <dependencies>                      │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-boot-starter           │ ← Sub-dependency 1
│     </artifactId>                   │
│   </dependency>                     │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-boot-starter-tomcat    │ ← Sub-dependency 2
│     </artifactId>                   │
│   </dependency>                     │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-webmvc                 │ ← Sub-dependency 3
│     </artifactId>                   │
│   </dependency>                     │
│   <!-- 15+ more dependencies -->    │
│ </dependencies>                     │
└─────────────────────────────────────┘
        │
        ▼
Step 6: RECURSIVELY download all sub-dependencies
┌─────────────────────────────────────┐
│ For each sub-dependency:            │
│                                     │
│ 1. Check local cache                │
│    ├─→ Found? Use it                │
│    └─→ Not found? Download          │
│                                     │
│ 2. Read ITS pom.xml                 │
│                                     │
│ 3. Download ITS sub-dependencies    │
│                                     │
│ Example tree:                       │
│ spring-boot-starter-web             │
│ ├─ spring-boot-starter              │
│ │  ├─ spring-boot                   │
│ │  │  └─ spring-core                │
│ │  │     └─ spring-jcl              │
│ │  └─ spring-context                │
│ │     └─ spring-aop                 │
│ │        └─ spring-beans            │
│ ├─ spring-boot-starter-tomcat       │
│ │  ├─ tomcat-embed-core             │ ← KEY for SERVLET type!
│ │  ├─ tomcat-embed-el               │
│ │  └─ tomcat-embed-websocket        │
│ └─ spring-webmvc                    │
│    ├─ spring-web                    │
│    └─ spring-context                │
│                                     │
│ Total: 50+ JARs downloaded! 📦      │
└─────────────────────────────────────┘
        │
        ▼
Step 7: Save all JARs to LOCAL repository
┌─────────────────────────────────────┐
│ ~/.m2/repository/ (Maven)           │
│ OR                                  │
│ ~/.gradle/caches/ (Gradle)          │
│                                     │
│ Now contains:                       │
│ ✓ spring-boot-starter-web-3.2.0.jar │
│ ✓ spring-webmvc-6.1.1.jar           │
│ ✓ tomcat-embed-core-10.1.16.jar     │ ← Will be checked later!
│ ✓ javax.servlet-api-4.0.1.jar       │ ← Will be checked later!
│ ✓ ... (50+ JARs)                    │
│                                     │
│ Next build will be FAST! ⚡          │
└─────────────────────────────────────┘
        │
        ▼
Step 8: Build CLASSPATH
┌──────────────────────────────────────────┐
│ Maven/Gradle creates classpath:          │
│                                          │
│ /home/user/.m2/repository/               │
│   org/springframework/boot/              │
│     spring-boot-starter-web/3.2.0/       │
│       spring-boot-starter-web-3.2.0.jar: │
│   org/springframework/                   │
│     spring-webmvc/6.1.1/                 │
│       spring-webmvc-6.1.1.jar:           │
│   org/apache/tomcat/embed/               │
│     tomcat-embed-core/10.1.16/           │
│       tomcat-embed-core-10.1.16.jar:     │ ← Contains DispatcherServlet!
│   javax/servlet/                         │
│     javax.servlet-api/4.0.1/             │
│       javax.servlet-api-4.0.1.jar:       │ ← Contains Servlet.class!
│   ... (all JARs, colon-separated)        │
│                                          │
│ This is what Java will search!           │
└──────────────────────────────────────────┘
        │
        ▼
Step 9: Compile your code
┌───────────────────────────────────────────┐
│ javac -classpath <all-jars>               │
│   src/main/java/**/*.java                 │
│                                           │
│ Compiles:                                 │
│ MyApplication.java → MyApplication.class  │
│ UserController.java → UserController.class│
│ UserService.java → UserService.class      │
└───────────────────────────────────────────┘
        │
        ▼
Step 10: Package into JAR
┌────────────────────────────────────┐
│ Creates: target/my-app.jar         │
│                                    │
│ Contains:                          │
│ ├─ Your compiled .class files      │
│ ├─ All dependency JARs embedded    │
│ │  (in BOOT-INF/lib/)              │
│ └─ MANIFEST.MF with Main-Class     │
│                                    │
│ JAR includes FULL classpath inside!│
└────────────────────────────────────┘

BUILD COMPLETE! ✓
Output: my-app.jar (50 MB)

═══════════════════════════════════════════════════════════════════════════════════════════════

┌──────────────────────────────────────────────────────────────────┐
│  PHASE 2: RUNTIME (Application Startup)                          │
└──────────────────────────────────────────────────────────────────┘

Step 1: User runs the JAR
┌────────────────────────────────────┐
│ $ java -jar my-app.jar             │
└────────────────────────────────────┘
        │
        ▼
Step 2: JVM reads MANIFEST.MF
┌─────────────────────────────────────┐
│ MANIFEST.MF inside JAR:             │
│                                     │
│ Main-Class:                         │
│   org.springframework.boot.loader.  │
│     JarLauncher                     │ ← Spring Boot's launcher
│                                     │
│ Start-Class:                        │
│   com.example.MyApplication         │ ← Your actual main class
└─────────────────────────────────────┘
        │
        ▼
Step 3: JarLauncher extracts classpath
┌─────────────────────────────────────┐
│ JarLauncher:                        │
│ 1. Reads BOOT-INF/lib/              │
│ 2. Creates classpath with all JARs  │
│ 3. Sets up ClassLoader              │
│ 4. Calls your main() method         │
└─────────────────────────────────────┘
        │
        ▼
Step 4: SpringApplication.run() starts
┌─────────────────────────────────────┐
│ @SpringBootApplication              │
│ public class MyApplication {        │
│   public static void main(...) {    │
│     SpringApplication.run(          │
│       MyApplication.class, args);   │
│   }                                 │
│ }                                   │
└─────────────────────────────────────┘
        │
        ▼
Step 5: DEDUCE APPLICATION TYPE (This is where classpath matters!)
┌─────────────────────────────────────────┐
│ WebApplicationType.deduceFromClasspath()│
│                                         │
│ Question: What classes are available    │
│           in the classpath?             │
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│ ClassLoader.loadClass() checks:     │
│                                     │
│ 1. Check for REACTIVE:              │
│    ClassUtils.isPresent(            │
│      "o.s.w.r.DispatcherHandler")   │
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      spring-webflux-*.jar?          │
│    → NOT FOUND ✗                    │
│                                     │
│ 2. Check for SERVLET:               │
│    ClassUtils.isPresent(            │
│      "javax.servlet.Servlet")       │
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      javax.servlet-api-4.0.1.jar?   │
│    → FOUND! ✓                       │
│                                     │
│    ClassUtils.isPresent(            │
│      "o.s.w.s.DispatcherServlet")   │
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      spring-webmvc-6.1.1.jar?       │
│    → FOUND! ✓                       │
│                                     │
│ DECISION: SERVLET ✓                 │
└─────────────────────────────────────┘
        │
        ▼
Step 6: Create appropriate ApplicationContext
┌────────────────────────────────────────────────────┐
│ Because type = SERVLET:                            │
│                                                    │
│ Create:                                            │
│ AnnotationConfigServletWebServerApplicationContext │
│                                                    │
│ This will:                                         │
│ ✓ Start embedded Tomcat                            │
│ ✓ Register DispatcherServlet                       │
│ ✓ Enable @Controller support                       │
└────────────────────────────────────────────────────┘
        │
        ▼
Step 7: Start embedded Tomcat
┌─────────────────────────────────────┐
│ Uses: tomcat-embed-core-10.1.16.jar │
│       (from classpath!)             │
│                                     │
│ Tomcat started on port 8080         │
└─────────────────────────────────────┘

APPLICATION READY! 🚀
```

---

## Key Points Confirmed

### 1. **Same Process for Maven and Gradle**
```
Maven:  pom.xml → Check ~/.m2 → Download → Build classpath
         ↓              ↓          ↓            ↓
Gradle: build.gradle → Check ~/.gradle → Download → Build classpath
                                ↓
                        SAME RESULT: Classpath with JARs
```

### 2. **Transitive Dependencies** (Your "sub-dependencies")
```
You request: spring-boot-starter-web
             ↓
Maven/Gradle reads its POM file
             ↓
Finds 15+ sub-dependencies
             ↓
Downloads each one
             ↓
Reads THEIR POM files
             ↓
Downloads THEIR sub-dependencies
             ↓
Total: 50+ JARs from 1 dependency!
```

Example:
```
You add:
<dependency>spring-boot-starter-web</dependency>

Maven automatically downloads:
├─ spring-boot-starter-web
├─ spring-boot-starter (transitive)
├─ spring-boot (transitive of transitive)
├─ spring-core (transitive of transitive of transitive)
├─ tomcat-embed-core (transitive)
└─ ... (45+ more!)
```

### 3. **Classpath is Key**
```
BUILD TIME                    RUNTIME
Maven/Gradle                  Spring Boot
     ↓                             ↓
Downloads JARs                Checks classpath
     ↓                             ↓
Creates classpath             "What classes exist?"
     ↓                             ↓
Packages into JAR             Decides app type
                                   ↓
                              SERVLET/REACTIVE/NONE
```

---

## Real-World Example Timeline
```
First Build (Cold Start - No Cache):
════════════════════════════════════════════════════════════════════════════════════════════════
$ mvn clean package

[INFO] Downloading from central: https://repo.maven.apache.org/...
[INFO] Downloaded spring-boot-starter-web-3.2.0.jar (1.2 MB) at 500 KB/s
[INFO] Downloaded spring-webmvc-6.1.1.jar (987 KB) at 450 KB/s
[INFO] Downloaded tomcat-embed-core-10.1.16.jar (3.4 MB) at 600 KB/s
... (downloads 50+ JARs)
[INFO] BUILD SUCCESS
[INFO] Total time: 02:15 min ← SLOW! Downloading everything

Second Build (Cache Hit):
════════════════════════════════════════════════════════════════════════════════════════════════
$ mvn clean package

[INFO] Using cached dependencies from ~/.m2/repository
[INFO] BUILD SUCCESS
[INFO] Total time: 0:08 min ← FAST! Everything cached

Running the Application:
════════════════════════════════════════════════════════════════════════════════════════════════
$ java -jar target/my-app.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Deducing application type... ← Checking classpath
Found: javax.servlet.Servlet ← From classpath!
Found: DispatcherServlet ← From classpath!
Application type: SERVLET ← Decision made!

Starting ServletWebServerApplicationContext...
Starting Tomcat... ← Using tomcat-embed-core.jar from classpath
Tomcat started on port(s): 8080 (http)
Started MyApplication in 2.847 seconds
```

---

# The Complete Flow
```
┌──────────────────────────────────────────────────────────────────┐
│  PHASE 1: BUILD TIME (Maven/Gradle)                              │
└──────────────────────────────────────────────────────────────────┘

Step 1: You run build command
┌────────────────────────────────────┐
│ Developer types:                   │
│ $ mvn clean package                │
│        OR                          │
│ $ ./gradlew build                  │
└────────────────────────────────────┘
        │
        ▼
Step 2: Maven/Gradle reads configuration file
┌───────────────────────────────────────┐
│ pom.xml (Maven):                      │
│ <dependency>                          │
│   <groupId>o.s.boot</groupId>         │
│   <artifactId>                        │
│     spring-boot-starter-web           │
│   </artifactId>                       │
│ </dependency>                         │
│                                       │
│ OR                                    │
│                                       │
│ build.gradle (Gradle):                │
│ dependencies {                        │
│   implementation                      │
│     'o.s.boot:spring-boot-starter-web'│
│ }                                     │
└───────────────────────────────────────┘
        │
        ▼
Step 3: Check LOCAL repository first
┌─────────────────────────────────────────────┐
│ Maven checks:                               │
│ ~/.m2/repository/                           │
│   org/springframework/boot/                 │
│     spring-boot-starter-web/                │
│       3.2.0/                                │
│         spring-boot-starter-web-3.2.0.jar   │
│                                             │
│ Gradle checks:                              │
│ ~/.gradle/caches/modules-2/                 │
│   files-2.1/                                │
│     org.springframework.boot/               │
│       spring-boot-starter-web/              │
│         3.2.0/<hash>/                       │
│           spring-boot-starter-web-3.2.0.jar │
└─────────────────────────────────────────────┘
        │
        ├─→ FOUND? ✓ Skip download, use cached version
        │            (Very fast! ~0.1 seconds)
        │
        └─→ NOT FOUND? ✗ Go to Step 4
        
Step 4: Download from REMOTE repository
┌──────────────────────────────────────┐
│ Maven/Gradle connects to:            │
│                                      │
│ Maven Central:                       │
│ https://repo.maven.apache.org/       │
│   maven2/                            │
│                                      │
│ Download:                            │
│ 1. spring-boot-starter-web-3.2.0.jar │
│ 2. spring-boot-starter-web-3.2.0.pom │ ← Metadata!
└──────────────────────────────────────┘
        │
        ▼
Step 5: Read POM file to find SUB-DEPENDENCIES (Transitive)
┌─────────────────────────────────────┐
│ spring-boot-starter-web-3.2.0.pom:  │
│                                     │
│ <dependencies>                      │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-boot-starter           │ ← Sub-dependency 1
│     </artifactId>                   │
│   </dependency>                     │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-boot-starter-tomcat    │ ← Sub-dependency 2
│     </artifactId>                   │
│   </dependency>                     │
│   <dependency>                      │
│     <artifactId>                    │
│       spring-webmvc                 │ ← Sub-dependency 3
│     </artifactId>                   │
│   </dependency>                     │
│   <!-- 15+ more dependencies -->    │
│ </dependencies>                     │
└─────────────────────────────────────┘
        │
        ▼
Step 6: RECURSIVELY download all sub-dependencies
┌─────────────────────────────────────┐
│ For each sub-dependency:            │
│                                     │
│ 1. Check local cache                │
│    ├─→ Found? Use it                │
│    └─→ Not found? Download          │
│                                     │
│ 2. Read ITS pom.xml                 │
│                                     │
│ 3. Download ITS sub-dependencies    │
│                                     │
│ Example tree:                       │
│ spring-boot-starter-web             │
│ ├─ spring-boot-starter              │
│ │  ├─ spring-boot                   │
│ │  │  └─ spring-core                │
│ │  │     └─ spring-jcl              │
│ │  └─ spring-context                │
│ │     └─ spring-aop                 │
│ │        └─ spring-beans            │
│ ├─ spring-boot-starter-tomcat       │
│ │  ├─ tomcat-embed-core             │ ← KEY for SERVLET type!
│ │  ├─ tomcat-embed-el               │
│ │  └─ tomcat-embed-websocket        │
│ └─ spring-webmvc                    │
│    ├─ spring-web                    │
│    └─ spring-context                │
│                                     │
│ Total: 50+ JARs downloaded! 📦      │
└─────────────────────────────────────┘
        │
        ▼
Step 7: Save all JARs to LOCAL repository
┌─────────────────────────────────────┐
│ ~/.m2/repository/ (Maven)           │
│ OR                                  │
│ ~/.gradle/caches/ (Gradle)          │
│                                     │
│ Now contains:                       │
│ ✓ spring-boot-starter-web-3.2.0.jar │
│ ✓ spring-webmvc-6.1.1.jar           │
│ ✓ tomcat-embed-core-10.1.16.jar     │ ← Will be checked later!
│ ✓ javax.servlet-api-4.0.1.jar       │ ← Will be checked later!
│ ✓ ... (50+ JARs)                    │
│                                     │
│ Next build will be FAST! ⚡          │
└─────────────────────────────────────┘
        │
        ▼
Step 8: Build CLASSPATH
┌─────────────────────────────────────────┐
│ Maven/Gradle creates classpath:         │
│                                         │
│ /home/user/.m2/repository/              │
│   org/springframework/boot/             │
│     spring-boot-starter-web/3.2.0/      │
│       spring-boot-starter-web-3.2.0.jar:│
│   org/springframework/                  │
│     spring-webmvc/6.1.1/                │
│       spring-webmvc-6.1.1.jar:          │
│   org/apache/tomcat/embed/              │
│     tomcat-embed-core/10.1.16/          │
│       tomcat-embed-core-10.1.16.jar:    │ ← Contains DispatcherServlet!
│   javax/servlet/                        │
│     javax.servlet-api/4.0.1/            │
│       javax.servlet-api-4.0.1.jar:      │ ← Contains Servlet.class!
│   ... (all JARs, colon-separated)       │
│                                         │
│ This is what Java will search!          │
└─────────────────────────────────────────┘
        │
        ▼
Step 9: Compile your code
┌────────────────────────────────────────────┐
│ javac -classpath <all-jars>                │
│   src/main/java/**/*.java                  │
│                                            │
│ Compiles:                                  │
│ MyApplication.java → MyApplication.class   │
│ UserController.java → UserController.class │
│ UserService.java → UserService.class       │
└────────────────────────────────────────────┘
        │
        ▼
Step 10: Package into JAR
┌─────────────────────────────────────┐
│ Creates: target/my-app.jar          │
│                                     │
│ Contains:                           │
│ ├─ Your compiled .class files       │
│ ├─ All dependency JARs embedded     │
│ │  (in BOOT-INF/lib/)               │
│ └─ MANIFEST.MF with Main-Class      │
│                                     │
│ JAR includes FULL classpath inside! │
└─────────────────────────────────────┘

BUILD COMPLETE! ✓
Output: my-app.jar (50 MB)

═══════════════════════════════════════════════════════════════════

┌──────────────────────────────────────────────────────────────────┐
│  PHASE 2: RUNTIME (Application Startup)                          │
└──────────────────────────────────────────────────────────────────┘

Step 1: User runs the JAR
┌────────────────────────────────────┐
│ $ java -jar my-app.jar             │
└────────────────────────────────────┘
        │
        ▼
Step 2: JVM reads MANIFEST.MF
┌─────────────────────────────────────┐
│ MANIFEST.MF inside JAR:             │
│                                     │
│ Main-Class:                         │
│   org.springframework.boot.loader.  │
│     JarLauncher                     │ ← Spring Boot's launcher
│                                     │
│ Start-Class:                        │
│   com.example.MyApplication         │ ← Your actual main class
└─────────────────────────────────────┘
        │
        ▼
Step 3: JarLauncher extracts classpath
┌─────────────────────────────────────┐
│ JarLauncher:                        │
│ 1. Reads BOOT-INF/lib/              │
│ 2. Creates classpath with all JARs  │
│ 3. Sets up ClassLoader              │
│ 4. Calls your main() method         │
└─────────────────────────────────────┘
        │
        ▼
Step 4: SpringApplication.run() starts
┌─────────────────────────────────────┐
│ @SpringBootApplication              │
│ public class MyApplication {        │
│   public static void main(...) {    │
│     SpringApplication.run(          │
│       MyApplication.class, args);   │
│   }                                 │
│ }                                   │
└─────────────────────────────────────┘
        │
        ▼
Step 5: DEDUCE APPLICATION TYPE (This is where classpath matters!)
┌──────────────────────────────────────────┐
│ WebApplicationType.deduceFromClasspath() │
│                                          │
│ Question: What classes are available     │
│           in the classpath?              │
└──────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│ ClassLoader.loadClass() checks:     │
│                                     │
│ 1. Check for REACTIVE:              │
│    ClassUtils.isPresent(            │
│      "o.s.w.r.DispatcherHandler")   │
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      spring-webflux-*.jar?          │
│    → NOT FOUND ✗                    │
│                                     │
│ 2. Check for SERVLET:               │
│    ClassUtils.isPresent(            │
│      "javax.servlet.Servlet")       │
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      javax.servlet-api-4.0.1.jar?   │
│    → FOUND! ✓                       │
│                                     │
│    ClassUtils.isPresent(            │
│      "o.s.w.s.DispatcherServlet")   | 
│                                     │
│    Search in classpath:             │
│    BOOT-INF/lib/                    │
│      spring-webmvc-6.1.1.jar?       │
│    → FOUND! ✓                       │
│                                     │
│ DECISION: SERVLET ✓                 │
└─────────────────────────────────────┘
        │
        ▼
Step 6: Create appropriate ApplicationContext
┌────────────────────────────────────────────────────┐
│ Because type = SERVLET:                            │
│                                                    │
│ Create:                                            │
│ AnnotationConfigServletWebServerApplicationContext │
│                                                    │
│ This will:                                         │
│ ✓ Start embedded Tomcat                            │
│ ✓ Register DispatcherServlet                       │
│ ✓ Enable @Controller support                       │
└────────────────────────────────────────────────────┘
        │
        ▼
Step 7: Start embedded Tomcat
┌─────────────────────────────────────┐
│ Uses: tomcat-embed-core-10.1.16.jar │
│       (from classpath!)             │
│                                     │
│ Tomcat started on port 8080         │
└─────────────────────────────────────┘

APPLICATION READY! 🚀
```

## Visual Summary
```
┌─────────────────────────────────────────────────────────────┐
│                   THE COMPLETE PICTURE                      │
└─────────────────────────────────────────────────────────────┘

   pom.xml / build.gradle
   (What you write)
           │
           ▼
   Maven/Gradle Build Tool
           │
           ├─→ Check local cache (~/.m2 or ~/.gradle)
           │   ├─ Hit? Use it ✓
           │   └─ Miss? Download from Maven Central
           │
           ├─→ Download transitive dependencies (recursively)
           │
           ├─→ Build classpath (list of all JARs)
           │
           └─→ Package into JAR
               (includes all JARs + classpath info)
           │
           ▼
   my-app.jar
   (What gets deployed)
           │
           ▼
   java -jar my-app.jar
   (Runtime)
           │
           ▼
   JVM loads JAR
           │
           ├─→ Extracts classpath from JAR
           │
           └─→ SpringApplication.run()
                       │
                       ▼
               Check classpath for:
               - DispatcherServlet? → SERVLET ✓
               - DispatcherHandler? → REACTIVE ✗
               - Neither? → NONE ✗
                       │
                       ▼
               Create ServletWebServerApplicationContext
                       │
                       ▼
               Start Embedded Tomcat
               (using tomcat-embed-core.jar from classpath)
                       │
                       ▼
               Application Ready! 🚀
```


```













```

```
+-----------------------------------------------------------------------------------------+
|                               INCOMING HTTP REQUEST                                     |
|                                                                                         |
|  [METHOD]  https://www.panynj.gov/path/en/index.html         [QUERY STRING]       [HTTP BODY]                           |
|  PUT       /api/users/123     ?notify=true         { "name": "Jane", "role": "admin" }   |
|            +--------------+   +---------------+    +---------------------------------+   |
|            | Parsed by    |   | Read by       |    | Read by                         |   |
|            | HandlerMapping |   | Argument      |    | Argument Resolver +             |   |
|            |              |   | Resolver      |    | HttpMessageConverter (Jackson)  |   |
|            +--------------+   +---------------+    +---------------------------------+   |
+-----------------------------------------------------------------------------------------+
                         |
                         | (Request arrives)
                         v
+-----------------------------------------------------------------------------------------+
|                         DispatcherServlet (Front Controller)                          |
+-----------------------------------------------------------------------------------------+
                         |
                         | (1. "Which method handles this?")
                         v
+-----------------------------------------------------------------------------------------+
|                         HandlerMapping (The "Receptionist")                           |
|                                                                                         |
| * Finds `updateUser` method based on `PUT /api/users/{userId}`.                         |
| * Parses the URL and stores: `{"userId": "123"}`.                                       |
+-----------------------------------------------------------------------------------------+
                         |
                         | (2. "OK, found the method. Now, how do I call it?")
                         v
+--------------------------------------------------------------------------------------------------------+
|                         HandlerAdapter (The "Event Manager")                                           |
|                                                                                                        |
| * Sees `updateUser` needs 3 arguments.                                                                 |
| * Deploys its "Specialist" resolvers to get the values...                                              |
|                                                                                                        |
|   [Specialist 1: PathVariableMethodArgumentResolver]                                                   |
|   * Sees `@PathVariable("userId") Long id`                                                             |
|   * Asks HandlerMapping for its stored variables.                                                      |
|   * Gets "123" -> Converts to `Long 123` -------------------------------------> [Long id]              |
|                                                                                                        |
|   [Specialist 2: RequestParamMethodArgumentResolver]                                                   |
|   * Sees `@RequestParam(...) Boolean notify`                                                           |
|   * Scans the request's query string.                                                                  |
|   * Gets "true" -> Converts to `Boolean true` -------------------------------> [Boolean notify]        |
|                                                                                                        |
|   [Specialist 3: RequestResponseBodyMethodProcessor]                                                   |
|   * Sees `@RequestBody UserUpdateDTO userDTO`                                                          |
|   * Grabs the request's input stream (the body).                                                       |
|   * Uses Jackson to deserialize `{...}` into `UserUpdateDTO` object --------> [UserUpdateDTO userDTO]  |
|                                                                                                        |
+--------------------------------------------------------------------------------------------------------+
                         |
                         | (3. "All arguments are ready. Time to call the method.")
                         |
     [Long id = 123L]    |    [Boolean notify = true]    |    [UserUpdateDTO userDTO = ...]
                 \       |           |          |       /
                  \      |           |          |      /
                   v     v           v          v     v
+-----------------------------------------------------------------------------------------+
|                     CONTROLLER METHOD EXECUTION (Your Code)                             |
|                                                                                         |
|                     updateUser(123L, true, userDTO);                                    |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```



# Here is the exact step-by-step flow that happens when you call .authenticate():
```

1. Your LoginService calls authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)).

2. The AuthenticationManager takes this token.

3. It calls your UserDetailsServiceImpl.loadUserByUsername(username) method.

4. Your UserDetailsServiceImpl calls your userRepository.findByUsername(username). <-- THIS IS THE DATABASE CHECK.

5. The database returns your User entity (which includes the hashed password).

6. Your UserDetailsServiceImpl wraps this User in a Spring Security UserDetails object and returns it.

7. The AuthenticationManager now has two things:

        . The raw password from the token (request.getPassword()).

        . The hashed password from the UserDetails object (from the database).

8. It then uses your PasswordEncoder bean to compare them.

9. If they match: It returns the fully successful Authentication object.

10. If they DON'T match (or the user wasn't found): It throws the BadCredentialsException that your catch block is waiting for.
```

# 2. Detailed Security Filter Chain Flow

```
┌───────────────────────────────────────────────────────────────────────────────────────┐
│                        SECURITY FILTER CHAIN (15 Filters)                              │
│                        Executes in STRICT ORDER                                        │
└───────────────────────────────────────────────────────────────────────────────────────┘

Request enters FilterChainProxy
         │
         v
┌─────────────────────────────────────────────┐
│ 1. DisableEncodeUrlFilter                   │  Purpose: Disable URL encoding
│    - Prevents session ID in URL             │  of JSESSIONID (security risk)
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 2. WebAsyncManagerIntegrationFilter         │  Purpose: Integrate SecurityContext
│    - Propagate SecurityContext to @Async    │  with Spring MVC async processing
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 3. SecurityContextHolderFilter              │  ┌──────────────────────────┐
│    - Load SecurityContext from session      │  │ SecurityContextRepository│
│    - Store in SecurityContextHolder         │<─┤ (HttpSession default)   │
│                                             │  └──────────────────────────┘
│    Key Actions:                             │
│    ├─> SecurityContext ctx =                │
│    │   repo.loadContext(request)            │
│    └─> SecurityContextHolder.setContext(ctx)│
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 4. HeaderWriterFilter                       │  Security Headers:
│    - Add security headers to response       │  ├─> X-Frame-Options: DENY
│                                             │  ├─> X-Content-Type-Options
│                                             │  ├─> X-XSS-Protection
│                                             │  └─> Strict-Transport-Security
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 5. CorsFilter                               │  Purpose: Handle CORS preflight
│    - Process OPTIONS requests               │  and add CORS headers
│    - Add CORS headers                       │
│                                             │  If OPTIONS → Return immediately
└──────────────────┬──────────────────────────┘  If not → Continue chain
                   │
                   v
┌─────────────────────────────────────────────┐
│ 6. CsrfFilter                               │  ┌─────────────────────────┐
│    - Validate CSRF token                    │  │ CsrfTokenRepository     │
│    - Only for state-changing requests       │  │ (Cookie or Session)     │
│      (POST, PUT, DELETE, PATCH)             │<─┤                         │
│                                             │  └─────────────────────────┘
│    Flow:                                    │
│    ├─> Load expected token from repository  │
│    ├─> Extract actual token from request    │
│    │   (Header: X-CSRF-TOKEN or             │
│    │    Parameter: _csrf)                   │
│    ├─> Compare tokens                       │
│    └─> If mismatch → AccessDeniedException  │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 7. LogoutFilter                             │  URL: /logout (default)
│    - Match logout URL                       │
│    - Clear SecurityContext                  │  Actions:
│    - Invalidate session                     │  ├─> Invalidate HttpSession
│    - Delete cookies                         │  ├─> Clear SecurityContext
│    - Redirect to logout success URL         │  ├─> Delete remember-me cookie
│                                             │  └─> Redirect to /login?logout
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────────────────────────────────┐
│ 8. UsernamePasswordAuthenticationFilter                                 │
│    - Process login form submission                                      │
│    - URL: /login (POST) by default                                      │
│                                                                          │
│    ┌──────────────────────────────────────────────────────────────┐   │
│    │  AUTHENTICATION FLOW (Detailed in next diagram)              │   │
│    │                                                               │   │
│    │  1. Extract username & password from request                 │   │
│    │     └─> username = request.getParameter("username")          │   │
│    │     └─> password = request.getParameter("password")          │   │
│    │                                                               │   │
│    │  2. Create unauthenticated token                             │   │
│    │     └─> UsernamePasswordAuthenticationToken(user, pass)      │   │
│    │                                                               │   │
│    │  3. Delegate to AuthenticationManager                        │   │
│    │     └─> authManager.authenticate(token)                      │   │
│    │                                                               │   │
│    │  4. On Success:                                              │   │
│    │     ├─> Store authenticated token in SecurityContext         │   │
│    │     ├─> Generate remember-me cookie (if enabled)             │   │
│    │     ├─> Generate new session ID (session fixation protection)│   │
│    │     └─> Redirect to success URL (default: /)                 │   │
│    │                                                               │   │
│    │  5. On Failure:                                              │   │
│    │     ├─> Clear SecurityContext                                │   │
│    │     └─> Redirect to /login?error                             │   │
│    └──────────────────────────────────────────────────────────────┘   │
└──────────────────┬──────────────────────────────────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 9. DefaultLoginPageGeneratingFilter         │  Purpose: Generate default
│    - Generate /login page HTML              │  login form if no custom page
│    - Only if no custom login page           │  provided
│                                             │
│    If GET /login → Generate HTML form       │
│    Else → Continue chain                    │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 10. DefaultLogoutPageGeneratingFilter       │  Purpose: Generate default
│     - Generate /logout page                 │  logout confirmation page
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 11. BasicAuthenticationFilter               │  Header: Authorization: Basic
│     - Process HTTP Basic authentication     │  base64(username:password)
│     - Extract credentials from header       │
│                                             │  Only if Authorization header
│     Flow:                                   │  present with "Basic" scheme
│     ├─> Decode Base64 credentials           │
│     ├─> Create authentication token         │
│     ├─> Authenticate via AuthenticationMgr  │
│     └─> Store in SecurityContext            │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 12. RequestCacheAwareFilter                 │  Purpose: Restore original
│     - Restore original request after login  │  request that triggered auth
│                                             │
│     Example: User requests /dashboard       │
│     → Redirected to /login                  │
│     → After login, restored to /dashboard   │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 13. SecurityContextHolderAwareRequestFilter │  Purpose: Wrap request with
│     - Wrap HttpServletRequest               │  security-aware methods
│     - Add security methods to request       │
│                                             │  Added methods:
│     request.getUserPrincipal()              │  ├─> getUserPrincipal()
│     request.isUserInRole("ADMIN")           │  ├─> isUserInRole()
│     request.getRemoteUser()                 │  ├─> getRemoteUser()
│                                             │  └─> authenticate()
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 14. AnonymousAuthenticationFilter           │  Purpose: Create anonymous
│     - Create anonymous authentication       │  authentication if none exists
│     - Only if no authentication present     │
│                                             │  Anonymous User:
│     If SecurityContext is empty:            │  ├─> Username: "anonymousUser"
│     └─> Create AnonymousAuthenticationToken │  └─> Role: ROLE_ANONYMOUS
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 15. SessionManagementFilter                 │  Purpose: Session security
│     - Session fixation protection           │
│     - Concurrent session control            │  Session Fixation:
│     - Invalid session detection             │  ├─> Change session ID after auth
│                                             │  └─> Prevent session hijacking
│     Concurrent Sessions:                    │
│     ├─> Track active sessions per user      │  Concurrent Control:
│     ├─> Limit max sessions (e.g., 1)        │  ├─> Max 1 session per user
│     └─> Invalidate old session if exceeded  │  └─> Expire oldest session
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│ 16. ExceptionTranslationFilter              │  Purpose: Handle security
│     - Catch security exceptions             │  exceptions and convert to
│     - Trigger authentication entry point    │  HTTP responses
│                                             │
│     Exception Handling:                     │
│     ├─> AuthenticationException             │
│     │   └─> Trigger AuthenticationEntryPoint│
│     │       (redirect to /login)            │
│     │                                        │
│     └─> AccessDeniedException               │
│         └─> If authenticated:               │
│             └─> Trigger AccessDeniedHandler │
│                 (403 Forbidden)             │
│         └─> If not authenticated:           │
│             └─> Trigger AuthenticationEntry │
│                 Point (redirect to /login)  │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────────────────────────────┐
│ 17. AuthorizationFilter                                             │
│     - Final authorization check                                     │
│     - Check if user has required permissions                        │
│                                                                      │
│     ┌────────────────────────────────────────────────────────┐    │
│     │  AUTHORIZATION FLOW (Detailed below)                    │    │
│     │                                                          │    │
│     │  1. Get security metadata for current request           │    │
│     │     └─> SecurityMetadataSource.getAttributes(request)   │    │
│     │         Returns: [ROLE_ADMIN, ROLE_USER]                │    │
│     │                                                          │    │
│     │  2. Get current authentication                          │    │
│     │     └─> SecurityContextHolder.getContext()              │    │
│     │         .getAuthentication()                            │    │
│     │                                                          │    │
│     │  3. Delegate to AccessDecisionManager                   │    │
│     │     └─> accessDecisionManager.decide(                   │    │
│     │           authentication, request, attributes)          │    │
│     │                                                          │    │
│     │  4. Voting process (AffirmativeBased default):          │    │
│     │     ├─> RoleVoter.vote() → ACCESS_GRANTED               │    │
│     │     ├─> AuthenticatedVoter.vote() → ACCESS_ABSTAIN      │    │
│     │     └─> One GRANTED = Access Allowed                    │    │
│     │                                                          │    │
│     │  5. On Access Denied:                                   │    │
│     │     └─> Throw AccessDeniedException                     │    │
│     │         (Caught by ExceptionTranslationFilter)          │    │
│     └────────────────────────────────────────────────────────┘    │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   v
         Request reaches Controller
                   │
                   v
         @PreAuthorize check (if present)
                   │
                   v
         Business Logic Execution
```

# 3. Authentication Flow - Deep Dive

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                        AUTHENTICATION PROCESS                                  │
│                     (UsernamePasswordAuthentication)                           │
└───────────────────────────────────────────────────────────────────────────────┘

User submits login form
         │
         │  POST /login
         │  username=john
         │  password=secret123
         │
         v
┌────────────────────────────────────────────────────────────┐
│ UsernamePasswordAuthenticationFilter                       │
│                                                             │
│  Step 1: Extract credentials                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ String username = request.getParameter("username")  │  │
│  │ String password = request.getParameter("password")  │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  Step 2: Create unauthenticated token                      │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ UsernamePasswordAuthenticationToken authRequest =   │  │
│  │   new UsernamePasswordAuthenticationToken(          │  │
│  │     username,                                        │  │
│  │     password                                         │  │
│  │   );                                                 │  │
│  │ // authenticated = false at this point              │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────┬──────────────────────────────────┘
                          │
                          │ authManager.authenticate(authRequest)
                          v
┌────────────────────────────────────────────────────────────────────┐
│ AuthenticationManager (ProviderManager)                            │
│                                                                     │
│  List<AuthenticationProvider> providers = [                        │
│    DaoAuthenticationProvider,                                      │
│    LdapAuthenticationProvider,                                     │
│    OAuth2AuthenticationProvider                                    │
│  ];                                                                 │
│                                                                     │
│  Step 3: Iterate through providers                                 │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ for (AuthenticationProvider provider : providers) {          │ │
│  │                                                               │ │
│  │   if (!provider.supports(authRequest.getClass())) {          │ │
│  │     continue; // Skip unsupported providers                  │ │
│  │   }                                                           │ │
│  │                                                               │ │
│  │   try {                                                       │ │
│  │     Authentication result =                                  │ │
│  │       provider.authenticate(authRequest);                    │ │
│  │                                                               │ │
│  │     if (result != null) {                                    │ │
│  │       return result; // Success!                             │ │
│  │     }                                                         │ │
│  │   } catch (AuthenticationException e) {                      │ │
│  │     lastException = e;                                       │ │
│  │   }                                                           │ │
│  │ }                                                             │ │
│  │                                                               │ │
│  │ throw new ProviderNotFoundException();                       │ │
│  └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
                          │ provider.authenticate()
                          v
┌─────────────────────────────────────────────────────────────────────────┐
│ DaoAuthenticationProvider                                               │
│                                                                          │
│  Step 4: Load user from database                                        │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ UserDetails user =                                                │ │
│  │   userDetailsService.loadUserByUsername(username);                │ │
│  │                                                                    │ │
│  │ // Returns: User(                                                 │ │
│  │ //   username="john",                                             │ │
│  │ //   password="$2a$12$hashed...",  // BCrypt hash                 │ │
│  │ //   authorities=[ROLE_USER, ROLE_ADMIN],                         │ │
│  │ //   enabled=true,                                                │ │
│  │ //   accountNonExpired=true,                                      │ │
│  │ //   credentialsNonExpired=true,                                  │ │
│  │ //   accountNonLocked=true                                        │ │
│  │ // )                                                              │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                              │                                           │
│                              v                                           │
│  Step 5: Pre-authentication checks                                      │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ preAuthenticationChecks.check(user);                              │ │
│  │                                                                    │ │
│  │ Checks:                                                            │ │
│  │ ├─> if (!user.isAccountNonLocked())                               │ │
│  │ │     throw LockedException                                       │ │
│  │ ├─> if (!user.isEnabled())                                        │ │
│  │ │     throw DisabledException                                     │ │
│  │ └─> if (!user.isAccountNonExpired())                              │ │
│  │       throw AccountExpiredException                               │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                              │                                           │
│                              v                                           │
│  Step 6: Verify password                                                │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ String presentedPassword = authRequest.getCredentials()           │ │
│  │   .toString(); // "secret123"                                     │ │
│  │                                                                    │ │
│  │ String storedPassword = user.getPassword();                       │ │
│  │   // "$2a$12$hashed..."                                           │ │
│  │                                                                    │ │
│  │ boolean matches = passwordEncoder.matches(                        │ │
│  │   presentedPassword,                                              │ │
│  │   storedPassword                                                  │ │
│  │ );                                                                 │ │
│  │                                                                    │ │
│  │ if (!matches) {                                                   │ │
│  │   throw new BadCredentialsException("Bad credentials");           │ │
│  │ }                                                                  │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                              │                                           │
│                              v                                           │
│  Step 7: Post-authentication checks                                     │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ postAuthenticationChecks.check(user);                             │ │
│  │                                                                    │ │
│  │ Checks:                                                            │ │
│  │ └─> if (!user.isCredentialsNonExpired())                          │ │
│  │       throw CredentialsExpiredException                           │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                              │                                           │
│                              v                                           │
│  Step 8: Create authenticated token                                     │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ UsernamePasswordAuthenticationToken authenticated =               │ │
│  │   new UsernamePasswordAuthenticationToken(                        │ │
│  │     user,                    // principal                          │ │
│  │     null,                    // credentials (cleared for security) │ │
│  │     user.getAuthorities()    // [ROLE_USER, ROLE_ADMIN]           │ │
│  │   );                                                               │ │
│  │                                                                    │ │
│  │ authenticated.setDetails(authRequest.getDetails());                │ │
│  │ // authenticated = true                                           │ │
│  │                                                                    │ │
│  │ return authenticated;                                             │ │
│  └───────────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          │ Return authenticated token
                          v
┌────────────────────────────────────────────────────────────────────────┐
│ UsernamePasswordAuthenticationFilter (continued)                       │
│                                                                         │
│  Step 9: Success handling                                              │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ successfulAuthentication(request, response, authResult);         │ │
│  │                                                                   │ │
│  │ Actions:                                                          │ │
│  │ ├─> Store in SecurityContext:                                    │ │
│  │ │   SecurityContextHolder.getContext()                           │ │
│  │ │     .setAuthentication(authResult);                            │ │
│  │ │                                                                 │ │
│  │ ├─> Save SecurityContext to session:                             │ │
│  │ │   securityContextRepository.saveContext(                       │ │
│  │ │     SecurityContextHolder.getContext(),                        │ │
│  │ │     request,                                                   │ │
│  │ │     response                                                   │ │
│  │ │   );                                                            │ │
│  │ │                                                                 │ │
│  │ ├─> Session fixation protection:                                 │ │
│  │ │   HttpSession session = request.getSession(false);             │ │
│  │ │   if (session != null) {                                       │ │
│  │ │     String oldSessionId = session.getId();                     │ │
│  │ │     session.invalidate();                                      │ │
│  │ │   }                                                             │ │
│  │ │   HttpSession newSession = request.getSession(true);           │ │
│  │ │   // New session ID generated                                  │ │
│  │ │                                                                 │ │
│  │ ├─> Remember-me (if enabled):                                    │ │
│  │ │   if (rememberMeRequested) {                                   │ │
│  │ │     rememberMeServices.loginSuccess(                           │ │
│  │ │       request, response, authResult                            │ │
│  │ │     );                                                          │ │
│  │ │     // Sets remember-me cookie                                 │ │
│  │ │   }                                                             │ │
│  │ │                                                                 │ │
│  │ └─> Redirect to success URL:                                     │ │
│  │     successHandler.onAuthenticationSuccess(                      │ │
│  │       request, response, authResult                              │ │
│  │     );                                                            │ │
│  │     // Redirect to "/" or saved request URL                      │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘


Authentication Failure Flow:
───────────────────────────

If any step fails (password mismatch, account locked, etc.):
         │
         v
┌────────────────────────────────────────────────────────────────┐
│ Failure Handling                                               │
│                                                                 │
│  ├─> Clear SecurityContext:                                    │
│  │   SecurityContextHolder.clearContext();                     │
│  │                                                              │
│  ├─> Call failure handler:                                     │
│  │   failureHandler.onAuthenticationFailure(                   │
│  │     request, response, exception                            │
│  │   );                                                         │
│  │                                                              │
│  └─> Redirect to login with error:                             │
│      response.sendRedirect("/login?error");                    │
└─────────────────────────────────────────────────────────────────┘
```

# 4. Authorization Flow - Deep Dive
```
┌───────────────────────────────────────────────────────────────────────────┐
│                         AUTHORIZATION PROCESS                              │
│                    (Method Security & URL Security)                        │
└───────────────────────────────────────────────────────────────────────────┘

Request: GET /api/admin/users
Authenticated as: john (ROLE_USER, ROLE_ADMIN)
         │
         v
┌────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: URL-Based Authorization (AuthorizationFilter)                 │
│                                                                         │
│  Step 1: Match request to security configuration                       │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ SecurityMetadataSource finds matching pattern:                   │ │
│  │                                                                   │ │
│  │ Configuration:                                                    │ │
│  │   /api/public/**  → permitAll                                    │ │
│  │   /api/admin/**   → hasRole('ADMIN')  ← MATCH!                   │ │
│  │   /api/**         → authenticated                                │ │
│  │                                                                   │ │
│  │ Returns: ConfigAttribute[hasRole('ADMIN')]                       │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                              │                                          │
│                              v                                          │
│  Step 2: Get current authentication                                    │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ Authentication auth =                                             │ │
│  │   SecurityContextHolder.getContext().getAuthentication();        │ │
│  │                                                                   │ │
│  │ auth = UsernamePasswordAuthenticationToken(                      │ │
│  │   principal = User(username="john"),                             │ │
│  │   authorities = [                                                │ │
│  │     SimpleGrantedAuthority("ROLE_USER"),                         │ │
│  │     SimpleGrantedAuthority("ROLE_ADMIN")                         │ │
│  │   ],                                                              │ │
│  │   authenticated = true                                           │ │
│  │ )
│  │                                                                  │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                              │                                          │
│                              v                                          │
│  Step 3: Delegate to AccessDecisionManager                             │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ accessDecisionManager.decide(                                     │ │
│  │   authentication = auth,                                          │ │
│  │   object = FilterInvocation(request),                             │ │
│  │   configAttributes = [hasRole('ADMIN')]                           │ │
│  │ );                                                                 │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────────────────┐
│ AccessDecisionManager (AffirmativeBased - default)                      │
│                                                                          │
│  Step 4: Voting Process                                                 │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ Voters to consult:                                                │ │
│  │   1. RoleVoter                                                    │ │
│  │   2. AuthenticatedVoter                                           │ │
│  │   3. WebExpressionVoter (for SpEL expressions)                    │ │
│  │                                                                    │ │
│  │ Strategy: AffirmativeBased (one ACCESS_GRANTED = success)        │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                              │                                           │
│                              v                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ VOTER 1: RoleVoter                                              │  │
│  │                                                                  │  │
│  │ int vote(Authentication auth, Object obj,                       │  │
│  │          Collection<ConfigAttribute> attrs) {                   │  │
│  │                                                                  │  │
│  │   for (ConfigAttribute attr : attrs) {                          │  │
│  │     if (attr.getAttribute().startsWith("ROLE_")) {              │  │
│  │       // This voter supports role-based decisions               │  │
│  │                                                                  │  │
│  │       String requiredRole = "ROLE_ADMIN";                       │  │
│  │                                                                  │  │
│  │       for (GrantedAuthority authority : auth.getAuthorities()) {│  │
│  │         if (authority.getAuthority().equals(requiredRole)) {    │  │
│  │           return ACCESS_GRANTED; ✓                              │  │
│  │         }                                                        │  │
│  │       }                                                          │  │
│  │                                                                  │  │
│  │       return ACCESS_DENIED;                                     │  │
│  │     }                                                            │  │
│  │   }                                                              │  │
│  │                                                                  │  │
│  │   return ACCESS_ABSTAIN; // Doesn't handle non-role attributes  │  │
│  │ }                                                                │  │
│  │                                                                  │  │
│  │ Result: ACCESS_GRANTED ✓                                        │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                              │                                           │
│                              v                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ Decision: ACCESS_GRANTED                                        │  │
│  │                                                                  │  │
│  │ Since strategy is AffirmativeBased and one voter granted       │  │
│  │ access, authorization succeeds!                                 │  │
│  │                                                                  │  │
│  │ No exception thrown → Continue to controller                    │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          v
┌────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: Method-Level Authorization (if @PreAuthorize present)         │
│                                                                         │
│  Controller Method:                                                    │
│  @PreAuthorize("hasRole('ADMIN') and #userId == authentication.name") │
│  public User getUserDetails(@PathVariable Long userId) { ... }         │
│                                                                         │
│  Step 5: AOP Interceptor triggered                                     │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ MethodSecurityInterceptor.invoke()                               │ │
│  │                                                                   │ │
│  │ 1. Extract @PreAuthorize expression:                             │ │
│  │    "hasRole('ADMIN') and #userId == authentication.name"         │ │
│  │                                                                   │ │
│  │ 2. Create evaluation context:                                    │ │
│  │    - authentication = current auth                               │ │
│  │    - userId = method parameter value                             │ │
│  │    - returnObject = (not available in @PreAuthorize)             │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                              │                                          │
│                              v                                          │
│  Step 6: Evaluate SpEL expression                                      │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ Expression Evaluation Tree:                                       │ │
│  │                                                                   │ │
│  │           AND                                                     │ │
│  │          /   \                                                    │ │
│  │         /     \                                                   │ │
│  │   hasRole    EQUALS                                               │ │
│  │   ('ADMIN')   /   \                                               │ │
│  │              /     \                                              │ │
│  │          userId   authentication.name                             │ │
│  │                                                                   │ │
│  │ Evaluation:                                                       │ │
│  │ ├─> hasRole('ADMIN')                                             │ │
│  │ │   Check if "ROLE_ADMIN" in authorities                         │ │
│  │ │   Result: TRUE ✓                                               │ │
│  │ │                                                                 │ │
│  │ └─> #userId == authentication.name                               │ │
│  │     userId parameter = 123                                       │ │
│  │     authentication.name = "john"                                 │ │
│  │     User with id 123 has name "john"?                            │ │
│  │     Result: TRUE ✓                                               │ │
│  │                                                                   │ │
│  │ Final: TRUE AND TRUE = TRUE ✓                                    │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                              │                                          │
│                              v                                          │
│  Step 7: Authorization decision                                        │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ if (expressionResult == TRUE) {                                   │ │
│  │   // Allow method execution                                       │ │
│  │   return methodInvocation.proceed();                              │ │
│  │ } else {                                                           │ │
│  │   throw new AccessDeniedException("Access is denied");           │ │
│  │ }                                                                  │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬────────────────────────────────────────────────┘
                          │
                          v
              Method Execution Allowed
                          │
                          v
┌────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: Post-Method Authorization (if @PostAuthorize present)         │
│                                                                         │
│  @PostAuthorize("returnObject.owner == authentication.name")           │
│  public Document getDocument(Long id) { ... }                          │
│                                                                         │
│  Step 8: Method executes and returns                                   │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ Document document = method.invoke();                              │ │
│  │                                                                   │ │
│  │ Returned:                                                         │ │
│  │   Document(                                                       │ │
│  │     id = 1,                                                       │ │
│  │     title = "Secret Document",                                   │ │
│  │     owner = "john"                                               │ │
│  │   )                                                               │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                              │                                          │
│                              v                                          │
│  Step 9: Evaluate @PostAuthorize                                       │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ Expression: "returnObject.owner == authentication.name"           │ │
│  │                                                                   │ │
│  │ Evaluation:                                                       │ │
│  │ ├─> returnObject.owner = "john"                                  │ │
│  │ └─> authentication.name = "john"                                 │ │
│  │                                                                   │ │
│  │ Result: "john" == "john" = TRUE ✓                                │ │
│  │                                                                   │ │
│  │ Action: Return document to caller                                │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘


Authorization Failure Scenarios:
─────────────────────────────────

Scenario A: User lacks required role
┌───────────────────────────────────────────────────────────────┐
│ User: alice (ROLE_USER only)                                  │
│ Requires: ROLE_ADMIN                                          │
│                                                                │
│ RoleVoter.vote() → ACCESS_DENIED                              │
│                                                                │
│ AccessDecisionManager throws AccessDeniedException            │
│          ↓                                                     │
│ ExceptionTranslationFilter catches exception                  │
│          ↓                                                     │
│ If authenticated: AccessDeniedHandler                         │
│   → HTTP 403 Forbidden                                        │
│   → {"error": "Access Denied"}                                │
│                                                                │
│ If not authenticated: AuthenticationEntryPoint                │
│   → Redirect to /login                                        │
└───────────────────────────────────────────────────────────────┘

Scenario B: @PreAuthorize expression fails
┌───────────────────────────────────────────────────────────────┐
│ @PreAuthorize("#userId == authentication.name")               │
│                                                                │
│ User "john" tries to access userId=456 (another user)         │
│                                                                │
│ Expression evaluation:                                        │
│   456 == "john" → FALSE                                       │
│                                                                │
│ MethodSecurityInterceptor throws AccessDeniedException        │
│          ↓                                                     │
│ @ControllerAdvice / @ExceptionHandler catches it              │
│   → HTTP 403 Forbidden                                        │
└───────────────────────────────────────────────────────────────┘

Scenario C: @PostAuthorize filters result
┌───────────────────────────────────────────────────────────────┐
│ @PostAuthorize("returnObject.owner == authentication.name")   │
│                                                                │
│ Method returns Document(owner="alice")                        │
│ Current user: "john"                                          │
│                                                                │
│ Expression evaluation:                                        │
│   "alice" == "john" → FALSE                                   │
│                                                                │
│ Result: AccessDeniedException thrown                          │
│ Document is NOT returned to caller                            │
└───────────────────────────────────────────────────────────────┘

```

# 5. JWT Authentication Flow

```
┌───────────────────────────────────────────────────────────────────────────┐
│                          JWT AUTHENTICATION FLOW                           │
│                        (Stateless Authentication)                          │
└───────────────────────────────────────────────────────────────────────────┘

STEP 1: User Login (Initial Authentication)
═══════════════════════════════════════════

Client                    Server                     Database
  │                         │                            │
  │  POST /api/auth/login   │                            │
  │  {                      │                            │
  │    "username": "john",  │                            │
  │    "password": "secret" │                            │
  │  }                      │                            │
  │────────────────────────>│                            │
  │                         │                            │
  │                         │  AuthenticationManager     │
  │                         │  authenticate()            │
  │                         │            │               │
  │                         │            v               │
  │                         │  UserDetailsService        │
  │                         │  loadUserByUsername()      │
  │                         │───────────────────────────>│
  │                         │                            │
  │                         │   SELECT * FROM users      │
  │                         │   WHERE username='john'    │
  │                         │<───────────────────────────│
  │                         │                            │
  │                         │  PasswordEncoder           │
  │                         │  matches(raw, encoded)     │
  │                         │            │               │
  │                         │            v               │
  │                         │  ✓ Password valid          │
  │                         │                            │
  │                         │  JwtTokenProvider          │
  │                         │  generateToken()           │
  │                         │            │               │
  │                         │            v               │
  │                         │  ┌──────────────────────┐  │
  │                         │  │ JWT Token Structure  │  │
  │                         │  ├──────────────────────┤  │
  │                         │  │ Header:              │  │
  │                         │  │ {                    │  │
  │                         │  │   "alg": "HS512",    │  │
  │                         │  │   "typ": "JWT"       │  │
  │                         │  │ }                    │  │
  │                         │  ├──────────────────────┤  │
  │                         │  │ Payload:             │  │
  │                         │  │ {                    │  │
  │                         │  │   "sub": "john",     │  │
  │                         │  │   "roles": [         │  │
  │                         │  │     "ROLE_USER",     │  │
  │                         │  │     "ROLE_ADMIN"     │  │
  │                         │  │   ],                 │  │
  │                         │  │   "iat": 1699999999, │  │
  │                         │  │   "exp": 1700086399  │  │
  │                         │  │ }                    │  │
  │                         │  ├──────────────────────┤  │
  │                         │  │ Signature:           │  │
  │                         │  │ HMACSHA512(          │  │
  │                         │  │   base64(header) +   │  │
  │                         │  │   "." +              │  │
  │                         │  │   base64(payload),   │  │
  │                         │  │   secret_key         │  │
  │                         │  │ )                    │  │
  │                         │  └──────────────────────┘  │
  │                         │            │               │
  │                         │            v               │
  │  HTTP 200 OK            │  Token: eyJhbGc...xyz123  │
  │  {                      │                            │
  │    "token": "eyJhbGc...",                            │
  │    "type": "Bearer",    │                            │
  │    "expiresIn": 86400   │                            │
  │  }                      │                            │
  │<────────────────────────│                            │
  │                         │                            │
  │  Store token in:        │                            │
  │  - localStorage         │                            │
  │  - sessionStorage       │                            │
  │  - Memory (React state) │                            │
  │                         │                            │


STEP 2: Subsequent Requests (Token Validation)
═══════════════════════════════════════════════

Client                    JwtAuthFilter               UserDetailsService
  │                            │                              │
  │  GET /api/admin/users      │                              │
  │  Authorization: Bearer     │                              │
  │    eyJhbGc...xyz123        │                              │
  │───────────────────────────>│                              │
  │                            │                              │
  │                            │  Step 1: Extract JWT         │
  │                            │  ┌────────────────────────┐  │
  │                            │  │ String authHeader =    │  │
  │                            │  │   request.getHeader(   │  │
  │                            │  │     "Authorization"    │  │
  │                            │  │   );                   │  │
  │                            │  │                        │  │
  │                            │  │ if (authHeader        │  │
  │                            │  │   .startsWith(        │  │
  │                            │  │     "Bearer ")) {     │  │
  │                            │  │   jwt = authHeader    │  │
  │                            │  │     .substring(7);    │  │
  │                            │  │ }                      │  │
  │                            │  └────────────────────────┘  │
  │                            │            │                 │
  │                            │            v                 │
  │                            │  Step 2: Validate Token      │
  │                            │  ┌────────────────────────┐  │
  │                            │  │ JwtTokenProvider       │  │
  │                            │  │ validateToken(jwt)     │  │
  │                            │  │                        │  │
  │                            │  │ Checks:                │  │
  │                            │  │ ├─> Parse JWT          │  │
  │                            │  │ ├─> Verify signature   │  │
  │                            │  │ │   (using secret key) │  │
  │                            │  │ ├─> Check expiration   │  │
  │                            │  │ └─> Validate format    │  │
  │                            │  │                        │  │
  │                            │  │ Result: VALID ✓        │  │
  │                            │  └────────────────────────┘  │
  │                            │            │                 │
  │                            │            v                 │
  │                            │  Step 3: Extract username    │
  │                            │  ┌────────────────────────┐  │
  │                            │  │ String username =      │  │
  │                            │  │   jwtProvider          │  │
  │                            │  │     .getUsernameFrom   │  │
  │                            │  │       Token(jwt);      │  │
  │                            │  │                        │  │
  │                            │  │ // Extracts "sub"      │  │
  │                            │  │ // claim from payload  │  │
  │                            │  │                        │  │
  │                            │  │ username = "john"      │  │
  │                            │  └────────────────────────┘  │
  │                            │            │                 │
  │                            │            v                 │
  │                            │  Step 4: Load user details   │
  │                            │  loadUserByUsername("john")  │
  │                            │─────────────────────────────>│
  │                            │                              │
  │                            │  UserDetails (from DB/Cache) │
  │                            │<─────────────────────────────│
  │                            │                              │
  │                            │  Step 5: Create Authentication
  │                            │  ┌────────────────────────┐  │
  │                            │  │ Authentication auth =  │  │
  │                            │  │   new UsernamePassword │  │
  │                            │  │     AuthenticationToken│  │
  │                            │  │   (                    │  │
  │                            │  │     userDetails,       │  │
  │                            │  │     null,              │  │
  │                            │  │     userDetails        │  │
  │                            │  │       .getAuthorities()│  │
  │                            │  │   );                   │  │
  │                            │  │                        │  │
  │                            │  │ auth.setDetails(       │  │
  │                            │  │   new WebAuth...       │  │
  │                            │  │     .buildDetails(req) │  │
  │                            │  │ );                     │  │
  │                            │  └────────────────────────┘  │
  │                            │            │                 │
  │                            │            v                 │
  │                            │  Step 6: Set SecurityContext │
  │                            │  ┌────────────────────────┐  │
  │                            │  │ SecurityContextHolder  │  │
  │                            │  │   .getContext()        │  │
  │                            │  │   .setAuthentication(  │  │
  │                            │  │     auth               │  │
  │                            │  │   );                   │  │
  │                            │  └────────────────────────┘  │
  │                            │            │                 │
  │                            │            v                 │
  │                            │  filterChain.doFilter()      │
  │                            │  (Continue to next filter)   │
  │                            │            │                 │
  │                            │            v                 │
  │                  AuthorizationFilter checks permissions   │
  │                            │            │                 │
  │                            │            v                 │
  │                  Request reaches Controller               │
  │                            │            │                 │
  │                            │            v                 │
  │  HTTP 200 OK               │  Response Data               │
  │  { "users": [...] }        │                              │
  │<───────────────────────────│                              │
  │                            │                              │


Token Expiration Handling:
══════════════════════════

Client                    Server
  │                         │
  │  Request with           │
  │  expired JWT            │
  │────────────────────────>│
  │                         │
  │                         │  validateToken() checks exp
  │                         │  ┌──────────────────────┐
  │                         │  │ Claims claims =      │
  │                         │  │   Jwts.parser()      │
  │                         │  │     .parse...        │
  │                         │  │     .getBody();      │
  │                         │  │                      │
  │                         │  │ Date expiration =    │
  │                         │  │   claims             │
  │                         │  │     .getExpiration();│
  │                         │  │                      │
  │                         │  │ if (expiration       │
  │                         │  │   .before(           │
  │                         │  │     new Date())) {   │
  │                         │  │   throw Expired      │
  │                         │  │     JwtException;    │
  │                         │  │ }                    │
  │                         │  └──────────────────────┘
  │                         │            │
  │                         │            v
  │  HTTP 401 Unauthorized  │  Token expired
  │  {                      │
  │    "error":             │
  │      "Token expired",   │
  │    "code": "JWT_EXPIRED"│
  │  }                      │
  │<────────────────────────│
  │                         │
  │  Client detects 401     │
  │  ├─> Clear stored token │
  │  └─> Redirect to login  │
  │                         │


Refresh Token Flow (Optional):
═══════════════════════════════

Client                    Server
  │                         │
  │  POST /api/auth/refresh │
  │  {                      │
  │    "refreshToken":      │
  │      "refresh_abc..."   │
  │  }                      │
  │────────────────────────>│
  │                         │
  │                         │  Validate refresh token
  │                         │  ┌──────────────────────┐
  │                         │  │ 1. Check signature   │
  │                         │  │ 2. Verify not expired│
  │                         │  │ 3. Check not revoked │
  │                         │  │    (in DB/Redis)     │
  │                         │  └──────────────────────┘
  │                         │            │
  │                         │            v
  │                         │  Generate new access token
  │                         │  (and optionally new refresh)
  │                         │            │
  │  HTTP 200 OK            │            v
  │  {                      │  New tokens generated
  │    "accessToken":       │
  │      "eyJnew...",       │
  │    "refreshToken":      │
  │      "refresh_new...",  │
  │    "expiresIn": 3600    │
  │  }                      │
  │<────────────────────────│
  │                         │
  │  Store new tokens       │
  │                         │

### 6. Session vs JWT Comparison

┌────────────────────────────────────────────────────────────────────────────┐
│                    SESSION-BASED vs JWT AUTHENTICATION                      │
└────────────────────────────────────────────────────────────────────────────┘

SESSION-BASED AUTHENTICATION:
═════════════════════════════

Client              Server              Session Store (Redis/DB)
  │                   │                          │
  │  POST /login      │                          │
  │  credentials      │                          │
  │──────────────────>│                          │
  │                   │  Validate credentials    │
  │                   │          │               │
  │                   │          v               │
  │                   │  Create session          │
  │                   │  sessionId = UUID()      │
  │                   │          │               │
  │                   │          v               │
  │                   │  Store session data      │
  │                   │─────────────────────────>│
  │                   │  SET session:abc123 {    │
  │                   │    userId: 1,            │
  │                   │    username: "john",     │
  │                   │    roles: ["ADMIN"],     │
  │                   │    createdAt: ...        │
  │                   │  }                       │
  │                   │<─────────────────────────│
  │                   │          │               │
  │  Set-Cookie:      │          v               │
  │  JSESSIONID=abc123│  Send session ID         │
  │<──────────────────│                          │
  │                   │                          │
  │  Subsequent request                          │
  │  Cookie: JSESSIONID=abc123                   │
  │──────────────────>│                          │
  │                   │  Extract session ID      │
  │                   │          │               │
  │                   │          v               │
  │                   │  Load session data       │
  │                   │─────────────────────────>│
  │                   │  GET session:abc123      │
  │                   │<─────────────────────────│
  │                   │  { userId, username... } │
  │                   │          │               │
  │                   │          v               │
  │                   │  Reconstitute auth       │
  │                   │  Continue request        │
  │  Response         │          │               │
  │<──────────────────│          v               │
  │                   │                          │

Characteristics:
├─> Server stores state (session data)
├─> Stateful - session store required
├─> Easy to invalidate (delete session)
├─> Cookie-based (automatic browser handling)
├─> CSRF protection needed
└─> Scales with session replication/sticky sessions


JWT AUTHENTICATION:
═══════════════════

Client              Server              
  │                   │
  │  POST /login      │
  │  credentials      │
  │──────────────────>│
  │                   │  Validate credentials
  │                   │          │
  │                   │          v
  │                   │  Generate JWT
  │                   │  ┌────────────────────┐
  │                   │  │ Header + Payload + │
  │                   │  │ Signature          │
  │                   │  │ (all client-side)  │
  │                   │  └────────────────────┘
  │                   │          │
  │  {                │          v
  │    "token":       │  Send token
  │      "eyJhbGc..."│
  │  }                │
  │<──────────────────│
  │                   │
  │  Store token      │
  │  (localStorage)   │
  │                   │
  │  Subsequent request
  │  Authorization: Bearer eyJhbGc...
  │──────────────────>│
  │                   │  Extract token
  │                   │          │
  │                   │          v
  │                   │  Validate signature
  │                   │  (no DB lookup!)
  │                   │          │
  │                   │          v
  │                   │  Decode payload
  │                   │  Extract user info
  │                   │          │
  │  Response         │          v
  │<──────────────────│  Continue request
  │                   │

Characteristics:
├─> Server stores NO state
├─> Stateless - no session store needed
├─> Hard to invalidate (token lives until expiry)
├─> Header-based (manual client handling)
├─> CSRF protection not needed
└─> Scales horizontally easily


COMPARISON TABLE:
═════════════════

┌─────────────────────┬───────────────────┬──────────────────────┐
│ Feature             │ Session-Based     │ JWT-Based            │
├─────────────────────┼───────────────────┼──────────────────────┤
│ State               │ Stateful          │ Stateless            │
│                     │ (server stores)   │ (client stores)      │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Storage             │ Server memory/    │ Client storage       │
│                     │ Redis/Database    │ (localStorage/memory)│
├─────────────────────┼───────────────────┼──────────────────────┤
│ Scalability         │ Harder            │ Easier               │
│                     │ (needs sticky     │ (any server can      │
│                     │ sessions or       │ validate)            │
│                     │ replication)      │                      │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Revocation          │ Easy              │ Hard                 │
│                     │ (delete session)  │ (needs blacklist)    │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Payload Size        │ Small (only ID)   │ Large (all data)     │
│                     │ ~50 bytes         │ ~500-2000 bytes      │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Server Load         │ DB lookup per     │ No DB lookup         │
│                     │ request           │ (just validation)    │
├─────────────────────┼───────────────────┼──────────────────────┤
│ CSRF Protection     │ Required          │ Not required         │
│                     │ (cookie-based)    │ (header-based)       │
├─────────────────────┼───────────────────┼──────────────────────┤
│ XSS Vulnerability   │ Lower             │ Higher               │
│                     │ (HttpOnly cookie) │ (localStorage exposed)│
├─────────────────────┼───────────────────┼──────────────────────┤
│ Mobile Apps         │ Harder            │ Easier               │
│                     │ (cookie handling) │ (standard headers)   │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Cross-domain        │ Complex           │ Simple               │
│                     │ (CORS + cookies)  │ (just headers)       │
├─────────────────────┼───────────────────┼──────────────────────┤
│ Expiration          │ Server controls   │ Fixed at creation    │
│                     │ (can extend)      │ (cannot extend)      │
├─────────────────────┼───────────────────┼──────────────────────┤
│ User Data Updates   │ Immediate         │ Delayed              │
│                     │ (in session store)│ (until token refresh)│
└─────────────────────┴───────────────────┴──────────────────────┘

```

# 7. Complete Request Lifecycle with Security

```
┌────────────────────────────────────────────────────────────────────────────┐
│              COMPLETE REQUEST LIFECYCLE WITH SPRING SECURITY               │
│                                                                            │
│  Request: POST /api/documents                                              │
│  Headers: Authorization: Bearer eyJhbGc...                                 │
│  Body: { "title": "New Document", "content": "..." }                       │
└────────────────────────────────────────────────────────────────────────────┘

 TIME    COMPONENT                           ACTION
═════════════════════════════════════════════════════════════════════════════

  T0    ┌─────────────────────────────────────────────────────────────────┐
        │ Servlet Container (Tomcat)                                      │
        │                                                                  │
        │ 1. Receive TCP connection                                       │
        │ 2. Parse HTTP request                                           │
        │ 3. Create HttpServletRequest & HttpServletResponse objects      │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T1                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ DelegatingFilterProxy                                           │
        │                                                                  │
        │ - Bridge between Servlet container and Spring                   │
        │ - Delegates to FilterChainProxy (Spring bean)                   │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T2                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ FilterChainProxy                                                │
        │                                                                  │
        │ Request URL: /api/documents                                     │
        │ Method: POST                                                    │
        │                                                                  │
        │ Match against configured filter chains:                         │
        │ ├─> /api/** → API Security Filter Chain (JWT)                  │
        │ └─> /** → Web Security Filter Chain (Form Login)                │
        │                                                                  │
        │ MATCHED: API Security Filter Chain                              │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T3                     v
        ╔═════════════════════════════════════════════════════════════════╗
        ║               SECURITY FILTER CHAIN EXECUTION                   ║
        ╚═════════════════════════════════════════════════════════════════╝

  T4    ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 1: DisableEncodeUrlFilter                               │
        │ ✓ Pass through (no session ID encoding)                        │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T5                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 2: SecurityContextHolderFilter                          │
        │                                                                  │
        │ Action: Load SecurityContext                                    │
        │ ├─> Check HttpSession: EMPTY (stateless JWT config)            │
        │ └─> SecurityContext remains empty                              │
        │                                                                  │
        │ SecurityContextHolder.getContext() = EmptySecurityContext       │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T6                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 3: HeaderWriterFilter                                   │
        │                                                                  │
        │ Add security headers to response:                               │
        │ ├─> X-Content-Type-Options: nosniff                            │
        │ ├─> X-Frame-Options: DENY                                      │
        │ ├─> X-XSS-Protection: 1; mode=block                            │
        │ └─> Strict-Transport-Security: max-age=31536000                │
        │                                                                  │
        │ ✓ Headers added to response buffer                             │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T7                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 4: CorsFilter                                           │
        │                                                                  │
        │ Check request method: POST (not OPTIONS)                        │
        │ Add CORS headers:                                               │
        │ ├─> Access-Control-Allow-Origin: http://localhost:3000         │
        │ ├─> Access-Control-Allow-Credentials: true                     │
        │ └─> Access-Control-Allow-Methods: GET, POST, PUT, DELETE       │
        │                                                                  │
        │ ✓ Pass through                                                 │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T8                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 5: CsrfFilter                                           │
        │                                                                  │
        │ Check if CSRF protection needed:                                │
        │ ├─> Request is POST (state-changing) ✓                         │
        │ ├─> URL pattern: /api/** → CSRF disabled for APIs              │
        │ └─> Skip CSRF validation                                        │
        │                                                                  │
        │ ✓ Pass through (CSRF disabled for JWT APIs)                    │
        └────────────────┬────────────────────────────────────────────────┘
                         │
  T9                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 6: LogoutFilter                                         │
        │                                                                  │
        │ Check if logout request:                                        │
        │ └─> URL: /api/documents (not /logout)                          │
        │                                                                  │
        │ ✓ Pass through (not a logout request)                          │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T10                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 7: JwtAuthenticationFilter (CUSTOM)                     │
        │                                                                  │
        │ ┌─────────────────────────────────────────────────────────────┐│
        │ │ CRITICAL FILTER - Performs JWT Authentication               ││
        │ └─────────────────────────────────────────────────────────────┘│
        │                                                                  │
        │ Step 1: Extract JWT from Authorization header                   │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ String authHeader =                                │         │
        │ │   request.getHeader("Authorization");              │         │
        │ │                                                     │         │
        │ │ authHeader = "Bearer eyJhbGciOiJIUzUxMiJ9..."      │         │
        │ │                                                     │         │
        │ │ if (authHeader.startsWith("Bearer ")) {            │         │
        │ │   jwt = authHeader.substring(7);                   │         │
        │ │ }                                                   │         │
        │ │                                                     │         │
        │ │ jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2..."    │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 2: Validate JWT                                            │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ JwtTokenProvider.validateToken(jwt)                │         │
        │ │                                                     │         │
        │ │ Parse JWT:                                         │         │
        │ │ ├─> Decode Base64 header & payload                │         │
        │ │ ├─> Verify signature with secret key              │         │
        │ │ │   HMACSHA512(header.payload, SECRET_KEY)        │         │
        │ │ │                                                  │         │
        │ │ ├─> Check expiration:                             │         │
        │ │ │   exp = 1700086399                              │         │
        │ │ │   now = 1700000000                              │         │
        │ │ │   exp > now ✓ (not expired)                     │         │
        │ │ │                                                  │         │
        │ │ └─> Result: VALID ✓                               │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 3: Extract username from JWT                               │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ Claims claims = Jwts.parser()                      │         │
        │ │   .setSigningKey(SECRET_KEY)                       │         │
        │ │   .parseClaimsJws(jwt)                             │         │
        │ │   .getBody();                                      │         │
        │ │                                                     │         │
        │ │ String username = claims.getSubject();             │         │
        │ │ // username = "john"                               │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 4: Load UserDetails                                        │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ UserDetails userDetails =                          │         │
        │ │   userDetailsService                               │         │
        │ │     .loadUserByUsername("john");                   │         │
        │ │                                                     │         │
        │ │ → Database Query:                                  │         │
        │ │   SELECT u.*, r.*, p.*                             │         │
        │ │   FROM users u                                     │         │
        │ │   LEFT JOIN user_roles ur ON u.id = ur.user_id    │         │
        │ │   LEFT JOIN roles r ON ur.role_id = r.id          │         │
        │ │   LEFT JOIN role_permissions rp ON r.id=rp.role_id│         │
        │ │   LEFT JOIN permissions p ON rp.permission_id=p.id│         │
        │ │   WHERE u.username = 'john'                        │         │
        │ │                                                     │         │
        │ │ → Result:                                          │         │
        │ │   User {                                           │         │
        │ │     id=1,                                          │         │
        │ │     username="john",                               │         │
        │ │     authorities=[                                  │         │
        │ │       ROLE_USER,                                   │         │
        │ │       ROLE_ADMIN,                                  │         │
        │ │       PERMISSION_WRITE,                            │         │
        │ │       PERMISSION_DELETE                            │         │
        │ │     ],                                             │         │
        │ │     enabled=true,                                  │         │
        │ │     accountNonLocked=true                          │         │
        │ │   }                                                 │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 5: Create Authentication object                            │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ UsernamePasswordAuthenticationToken authentication │         │
        │ │   = new UsernamePasswordAuthenticationToken(       │         │
        │ │       userDetails,          // principal           │         │
        │ │       null,                 // credentials         │         │
        │ │       userDetails.getAuthorities() // authorities  │         │
        │ │     );                                             │         │
        │ │                                                     │         │
        │ │ authentication.setDetails(                         │         │
        │ │   new WebAuthenticationDetailsSource()             │         │
        │ │     .buildDetails(request)                         │         │
        │ │ );                                                  │         │
        │ │                                                     │         │
        │ │ // Details include: IP address, session ID, etc.   │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 6: Set authentication in SecurityContext                   │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ SecurityContextHolder.getContext()                 │         │
        │ │   .setAuthentication(authentication);              │         │
        │ │                                                     │         │
        │ │ // Now SecurityContext contains:                   │         │
        │ │ SecurityContext {                                  │         │
        │ │   authentication = UsernamePasswordAuthToken {     │         │
        │ │     principal = "john",                            │         │
        │ │     authorities = [ROLE_USER, ROLE_ADMIN, ...],    │         │
        │ │     authenticated = true                           │         │
        │ │   }                                                 │         │
        │ │ }                                                   │         │
        │ └────────────────────────────────────────────────────┘         │
        │                                                                  │
        │ ✓ User authenticated - continue filter chain                   │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T11                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 8: AnonymousAuthenticationFilter                        │
        │                                                                  │
        │ Check if authentication exists:                                 │
        │ └─> SecurityContext.getAuthentication() != null ✓              │
        │                                                                  │
        │ ✓ Skip (already authenticated)                                 │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T12                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 9: SessionManagementFilter                              │
        │                                                                  │
        │ Session management disabled for stateless JWT                   │
        │ SessionCreationPolicy: STATELESS                                │
        │                                                                  │
        │ ✓ Pass through (no session management)                         │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T13                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 10: ExceptionTranslationFilter                          │
        │                                                                  │
        │ Wrap next filters in try-catch:                                 │
        │ try {                                                            │
        │   filterChain.doFilter(request, response);                      │
        │ } catch (AuthenticationException e) {                           │
        │   → Trigger AuthenticationEntryPoint                            │
        │ } catch (AccessDeniedException e) {                             │
        │   → Trigger AccessDeniedHandler                                 │
        │ }                                                                │
        │                                                                  │
        │ ✓ Continue (wrapping next filters)                             │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T14                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Filter 11: AuthorizationFilter                                 │
        │                                                                  │
        │ ┌─────────────────────────────────────────────────────────────┐│
        │ │ CRITICAL FILTER - URL-Based Authorization                   ││
        │ └─────────────────────────────────────────────────────────────┘│
        │                                                                  │
        │ Step 1: Get security metadata for URL                           │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ URL: /api/documents                                │         │
        │ │ Method: POST                                       │         │
        │ │                                                     │         │
        │ │ Match against configuration:                       │         │
        │ │   /api/public/**  → permitAll                      │         │
        │ │   /api/documents  → hasRole('USER')  ← MATCH!      │         │
        │ │   /api/admin/**   → hasRole('ADMIN')               │         │
        │ │                                                     │         │
        │ │ Required: ConfigAttribute[hasRole('USER')]         │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 2: Get current authentication                              │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ Authentication auth =                              │         │
        │ │   SecurityContextHolder.getContext()               │         │
        │ │     .getAuthentication();                          │         │
        │ │                                                     │         │
        │ │ auth.getPrincipal() = "john"                       │         │
        │ │ auth.getAuthorities() = [                          │         │
        │ │   ROLE_USER,   ← HAS REQUIRED ROLE!                │         │
        │ │   ROLE_ADMIN,                                      │         │
        │ │   PERMISSION_WRITE,                                │         │
        │ │   PERMISSION_DELETE                                │         │
        │ │ ]                                                   │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 3: Authorization decision                                  │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ AccessDecisionManager.decide()                     │         │
        │ │                                                     │         │
        │ │ RoleVoter.vote():                                  │         │
        │ │ ├─> Required: ROLE_USER                            │         │
        │ │ ├─> User has: [ROLE_USER, ROLE_ADMIN, ...]        │         │
        │ │ └─> Result: ACCESS_GRANTED ✓                       │         │
        │ │                                                     │         │
        │ │ Decision: ALLOW ACCESS                             │         │
        │ └────────────────────────────────────────────────────┘         │
        │                                                                  │
        │ ✓ Authorization passed - continue to controller                │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T15                     v
        ╔═════════════════════════════════════════════════════════════════╗
        ║           SECURITY FILTER CHAIN COMPLETE                        ║
        ║           Request forwarded to DispatcherServlet                ║
        ╚═════════════════════════════════════════════════════════════════╝

 T16                     │
                         v
        ┌─────────────────────────────────────────────────────────────────┐
        │ DispatcherServlet                                              │
        │                                                                  │
        │ Step 1: Find handler mapping                                    │
        │ ├─> URL: /api/documents                                         │
        │ ├─> Method: POST                                                │
        │ └─> Controller: DocumentController.createDocument()            │
        │                                                                  │
        │ Step 2: Check method-level security                             │
        │ ├─> @PreAuthorize present? → Check                             │
        │ └─> No method security → Continue                              │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T17                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Method Security Interceptor (AOP)                              │
        │                                                                  │
        │ Controller method:                                              │
        │ @PreAuthorize("hasPermission('DOCUMENT', 'WRITE')")            │
        │ public Document createDocument(@RequestBody DocumentDTO dto) { │
        │   ...                                                            │
        │ }                                                                │
        │                                                                  │
        │ ┌─────────────────────────────────────────────────────────────┐│
        │ │ Evaluate @PreAuthorize Expression                           ││
        │ └─────────────────────────────────────────────────────────────┘│
        │                                                                  │
        │ Step 1: Create evaluation context                               │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ EvaluationContext:                                 │         │
        │ │ ├─> authentication = current auth                  │         │
        │ │ ├─> method parameters = [dto]                      │         │
        │ │ └─> root object = MethodSecurityExpressionRoot     │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 2: Evaluate SpEL expression                                │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ Expression: hasPermission('DOCUMENT', 'WRITE')     │         │
        │ │                                                     │         │
        │ │ Calls: PermissionEvaluator.hasPermission()         │         │
        │ │ ├─> authentication.getAuthorities()                │         │
        │ │ │   = [ROLE_USER, ROLE_ADMIN,                      │         │
        │ │ │      PERMISSION_WRITE, PERMISSION_DELETE]        │         │
        │ │ │                                                  │         │
        │ │ └─> Check if PERMISSION_WRITE exists ✓             │         │
        │ │                                                     │         │
        │ │ Result: TRUE                                       │         │
        │ └────────────────────────────────────────────────────┘         │
        │                                                                  │
        │ ✓ Method security passed - execute method                      │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T18                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ DocumentController.createDocument()                            │
        │                                                                  │
        │ Step 1: Access current user                                     │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ Authentication auth =                              │         │
        │ │   SecurityContextHolder.getContext()               │         │
        │ │     .getAuthentication();                          │         │
        │ │                                                     │         │
        │ │ String username = auth.getName(); // "john"        │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 2: Business logic                                          │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ Document doc = new Document();                     │         │
        │ │ doc.setTitle(dto.getTitle());                      │         │
        │ │ doc.setContent(dto.getContent());                  │         │
        │ │ doc.setOwner(username);  // Set owner to "john"    │         │
        │ │ doc.setCreatedAt(LocalDateTime.now());             │         │
        │ │                                                     │         │
        │ │ documentRepository.save(doc);                      │         │
        │ │                                                     │         │
        │ │ return doc;                                        │         │
        │ └────────────────────────────────────────────────────┘         │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T19                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ @PostAuthorize Check (if present)                              │
        │                                                                  │
        │ No @PostAuthorize on this method                                │
        │ ✓ Skip post-authorization                                      │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T20                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Response Processing                                            │
        │                                                                  │
        │ Step 1: Convert response to JSON                                │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ MessageConverter (Jackson)                         │         │
        │ │                                                     │         │
        │ │ Document object → JSON:                            │         │
        │ │ {                                                   │         │
        │ │   "id": 123,                                       │         │
        │ │   "title": "New Document",                         │         │
        │ │   "content": "...",                                │         │
        │ │   "owner": "john",                                 │         │
        │ │   "createdAt": "2024-11-06T10:30:00"              │         │
        │ │ }                                                   │         │
        │ └────────────────────────────────────────────────────┘         │
        │                         │                                       │
        │                         v                                       │
        │ Step 2: Write to response                                       │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ response.setStatus(200);                           │         │
        │ │ response.setContentType("application/json");       │         │
        │ │ response.getWriter().write(jsonString);            │         │
        │ └────────────────────────────────────────────────────┘         │
        │                                                                  │
        │ Step 3: Security headers already added by                       │
        │         HeaderWriterFilter                                      │
        │                                                                  │
        │ Final Response:                                                 │
        │ ┌────────────────────────────────────────────────────┐         │
        │ │ HTTP/1.1 200 OK                                    │         │
        │ │ Content-Type: application/json                     │         │
        │ │ X-Content-Type-Options: nosniff                    │         │
        │ │ X-Frame-Options: DENY                              │         │
        │ │ X-XSS-Protection: 1; mode=block                    │
        │ │ Strict-Transport-Security: max-age=31536000        │
        │ │                                                     │
        │ │ {                                                   │
        │ │   "id": 123,                                       │
        │ │   "title": "New Document",                         │
        │ │   "content": "...",                                │
        │ │   "owner": "john",                                 │
        │ │   "createdAt": "2024-11-06T10:30:00"              │
        │ │ }                                                   │
        │ └────────────────────────────────────────────────────┘
        └────────────────┬───────────────────────────────────────────────┘
                         │
 T21                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ SecurityContextHolderFilter (AFTER COMPLETION PHASE)           │
        │                                                                  │
        │ After response is written:                                       │
        │  - SecurityContextPersistenceFilter clears context OR leaves it  │
        │    depending on stateless policy.                                │
        │                                                                  │
        │ Because we use STATELESS:                                        │
        │  → SecurityContextHolder.clearContext()                          │
        │                                                                  │
        │ Result: no SecurityContext kept after request                    │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T22                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ FilterChain ends                                               │
        │                                                                  │
        │ - Control returns back to Servlet container                     │
        │ - Response is already committed                                 │
        │                                                                  │
        └────────────────┬────────────────────────────────────────────────┘
                         │
 T23                     v
        ┌─────────────────────────────────────────────────────────────────┐
        │ Servlet Container (Tomcat)                                     │
        │                                                                  │
        │ - Send HTTP response bytes over TCP                             │
        │ - Flush output stream                                           │
        │ - Close request processing                                      │
        │                                                                  │
        │ Final State:                                                    │
        │  • Security context cleared                                      │
        │  • No session created (JWT stateless)                           │
        │  • Filters reset for next request                               │
        └─────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════════
                        ✅ COMPLETE SPRING SECURITY FLOW
═══════════════════════════════════════════════════════════════════════════════


