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


