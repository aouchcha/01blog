public interface BeanFactory {
    Object getBean(String name) throws BeanException;
    <T> T getBean(Class <T> requiredType) throws BeanException;
    boolean containsBean(String name);
    boolean isSingleton(String name);
}

BeanDefition bd = new GenricBeanDefinition();
bd.setBeanClassName("_blog.backend.Service.UserService");
bd.setScope("Singleton");

BeanDefiniton {
    name: "userService";
    Class: "UserService.class"
    scope; "singelon (default)"
    dependencies: ["UserRepo"]
}

BeanDefinitionRegistry.register("UserService", bd)

getBean("Usersevice") =====> Creation Bean + DI;

------------------------------------------------------------------------------------------------------------------------------------------------------------------

ApplicationContext = interface (Beactory + features) =====> extend BeanFactory;

DI:

Controller ====> Service =====> Repo (no dependencies)
            |               |
        depends on        depends on


1- Constructor Injection (Recommended)
@Service
public class Order {
    private final OrderREpo repo;
    private final EmailService email;

    public Order() {}

    @Autowired //Optional in Spring 4.3 if only one constructor;
    public Order(OrderREpo repo, EmailService email) {
        this.repo = repo;
        this.email = email;
    }
}


2- Field Injection (commun but not recommanded)
@autowired
UserSErvice userService;


3- Setter Injection (For Optional DEpend)
@Service
public class Order {
   
    private EmailService email;

    @Autowired(required = false) //Optional Depend
    public void setEmailService(EmailService email) {
        this.email = email;
    }
}



3 caching
singleton cash ===> beans fully created
partialy cash ====> beans stared created waiting
factory cach ======> ();


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


WorkFlow:
Reuest => SpringContainer => Proxy => Aspect => Your Method => Response

Step by step Workflow:

Spring Boot Start

@EnabeAspectJAutoProxy (auto-enabled in spring boot)

Creation Proxy Object for bean matching pointcut

@Pointcut("execution(* com.example.service..*.*(..))")


@Service 
class OrderService {
    public void CreateOrder() {};
}


OrderService$$SpringProxy {
    private OrderService target;
    private List<Advice> advices;

    public CreateOrder() {
        for(adive: beforAdvices) {
            advice.Execute();
        }
        target.createOrder();
        for(adive: afterAdvices) {
            advice.Execute();
        }
    }
}


Proxy Types:

1- JDK Dynamic Proxy (interface-based)

2- CGLIB Proxy (class-based)


///////////////////////////////////////////////////////////////////


# Spring Boot IoC & DI: What Actually Happens Under the Hood?

Inversion of Control (IoC) is the concept where the control flow of a program is inverted: instead of the programmer controlling the flow, the framework manages it. In Spring Boot, this means the framework takes over the **creation of beans**, **dependency injection**, and the **bean lifecycle**.

But how exactly does this happen inside the framework?

Many developers confuse `BeanFactory` and `ApplicationContext` because they both handle bean management. To understand how Spring works, we need to look at the differences between them, how a `BeanDefinition` works, and how Spring solves complex dependency issues using internal caches.

## 1\. BeanFactory vs. ApplicationContext

The first point of confusion is the container itself.

### The BeanFactory

The `BeanFactory` is the root interface for accessing the Spring IoC container. It defines how Spring creates, configures, and provides access to beans. It is the "lazy" container.

```java
public interface BeanFactory {
    Object getBean(String name) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;
    boolean containsBean(String name);
    boolean isSingleton(String name);
}
```

The `BeanFactory` is known for being **lazy**. It scans for `@Component` (or its aliases), but it does not instantiate the object immediately. Instead, it creates a **BeanDefinition**.

### The BeanDefinition

Before Spring creates an actual instance of a class (a Bean), it creates a `BeanDefinition`. This is metadata—a recipe—describing *how* to create the bean.

```java
// Conceptual example of what Spring does internally
GenericBeanDefinition bd = new GenericBeanDefinition();
bd.setBeanClassName("com.blog.backend.service.UserService");
bd.setScope("singleton");

// Registering the definition
beanDefinitionRegistry.registerBeanDefinition("userService", bd);
```

Internally, the definition looks something like this:

  * **Name:** "userService"
  * **Class:** `UserService.class`
  * **Scope:** Singleton (default)
  * **Dependencies:** [`UserRepo`]

Once the definition is registered, the application starts. Because `BeanFactory` is lazy, the bean is only instantiated when you explicitly call `getBean("userService")`.

### The ApplicationContext

The `ApplicationContext` is the advanced container that extends `BeanFactory`. This is what Spring Boot uses by default. It adds enterprise-specific features:

  * Event Publishing
  * AOP (Aspect Oriented Programming) integration
  * Internationalization

**The Main Difference:**
While `BeanFactory` is lazy, `ApplicationContext` is **eager**. Upon startup, the ApplicationContext looks at all the `BeanDefinitions` and immediately creates and injects all Singleton beans. This ensures that configuration errors are caught immediately when the app starts, rather than at runtime.

-----

## 2\. Dependency Injection (DI)

Once the container is up, it needs to connect the objects. This is Dependency Injection.

Let's look at a standard relationship: a `UserService` that depends on a `UserRepository`.

```java
Controller ===> Service ===> Repo (no dependencies)
```

Spring creates the dependency graph and starts instantiating beans from the bottom up (Repo first, then Service). There are three ways to inject these dependencies.

### 2.1. Constructor Injection (Recommended)

This is the industry standard for Spring development.

```java
@Service
public class OrderService {
    
    private final OrderRepo repo;
    private final EmailService email;

    // @Autowired is optional here in Spring 4.3+ if there is only one constructor
    public OrderService(OrderRepo repo, EmailService email) {
        this.repo = repo;
        this.email = email;
    }
}
```

**Advantages:**

  * **Immutability:** Fields can be `final`.
  * **Safety:** It prevents `NullPointerException` because you cannot create the object without passing its dependencies.
  * **Testing:** Easy to unit test without loading the Spring Context (just pass mock objects into the constructor).

### 2.2. Field Injection (Not Recommended)

You see this often in tutorials because it is short, but it is generally discouraged in production code.

```java
@Service
public class UserService {
    @Autowired 
    private UserRepository userRepo;
}
```

**Weaknesses:**

  * **Hidden Dependencies:** It is not obvious what the class needs to function just by looking at the constructor.
  * **Immutability:** You cannot use `final` fields.
  * **Testing Difficulty:** You cannot easily inject mocks during unit testing without using Reflection or booting up the whole Spring Context.

### 2.3. Setter Injection (For Optional Dependencies)

This is useful if a dependency is optional or needs to be swapped after creation.

```java
@Service
public class OrderService {
   
    private EmailService email;

    @Autowired(required = false) // Optional Dependency
    public void setEmailService(EmailService email) {
        this.email = email;
    }
}
```

-----

## 3\. The Three-Level Cache (Solving Circular Dependencies)

This is the advanced part. How does Spring handle the creation lifecycle efficiently, specifically when two beans depend on each other (Circular Dependency)?

Spring uses **Three Levels of Caching** to manage the bean lifecycle:

1.  **Singleton Objects (Level 1 Cache):** This contains fully initialized beans. They are created, dependencies are injected, and they are ready to use.
2.  **Early Singleton Objects (Level 2 Cache):** This contains beans that are instantiated (the object exists in memory) but their dependencies have not been injected yet.
3.  **Singleton Factories (Level 3 Cache):** This contains factories (`ObjectFactory`) used to create the beans.

### The Flow

When Spring attempts to inject a bean (e.g., `UserRepo` into `UserService`):

1.  It checks the **Level 1 Cache**. If the bean is there, it returns it.
2.  If not, it checks **Level 2**.
3.  If not, it checks **Level 3**.

If the bean is being created for the first time, Spring marks it as "in creation," creates the raw object, and places a factory for it in the **Level 3 Cache**. It then attempts to populate its dependencies.

If a circular dependency is detected (Bean A needs Bean B, and Bean B needs Bean A), Spring can fetch the "raw" version of Bean A from the Level 3/Level 2 cache to finish creating Bean B. Once fully created, the bean moves up to the **Level 1 (Singleton) Cache**.

### Conclusion

Understanding the difference between the `BeanDefinition` (the recipe) and the actual Bean (the meal), and knowing how the `ApplicationContext` eagerly loads these using the three-level cache, distinguishes a Junior developer from a Senior engineer. While annotations like `@Autowired` make it look like magic, there is a robust, logical architecture ensuring your application runs smoothly.