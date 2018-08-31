package jee.billy.springstudy;

import jee.billy.springstudy.bean.Person;
import jee.billy.springstudy.bean.PersonAnnotation;
import jee.billy.springstudy.windows.newbean.WindowsBean;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import java.util.Objects;
import java.util.Set;


public class Spring1st extends SpringBaseTest {

    /**
     * 加载一个BeanFactory
     */
    @Test
    public void testRunBeanFactory() {
        String locations = "person.xml";
        BeanFactory bf = new XmlBeanFactory(new ClassPathResource(locations));
        Person p = bf.getBean("person", Person.class);
        LOG.debug(p.toString());
    }

    /**
     * 方法一启动spring
     */
    @Test
    public void testRunSpringWay1() {
        String[] locations = new String[]{"classpath*:person.xml"};
        // 读取bean.xml中的内容
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext(locations);
        Person p = ctx.getBean("person", Person.class);
        LOG.debug(p.toString());
        ctx.close();
    }

    /**
     * 方法二启动spring
     *
     * 说明：GenericXmlApplicationContext会最终实现ResourceLoader接口，ClassPathXmlApplicationContext和它一样，
     * 所以说，想以main方法启动Spring，必须依靠ResourceLoader的实现类才行。
     *
     */
    @Test
    public void testRunSpringWay2() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:person.xml");
        // 不执行refresh命令就去启动服务会报错的。 只执行context.start();也会报错的
        context.refresh();
        int beanDefinitionCount = context.getBeanDefinitionCount();
        LOG.debug("There is/are " + beanDefinitionCount + " bean(s) in total.");
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            LOG.debug(beanName + " -> " + beanDefinition.toString());
        }
        Person p = context.getBean(Person.class);
        LOG.debug(p.toString());
        context.close();
    }

    /**
     * 检查是是否是BeanFactory子类
     */
    @Test
    public void testIsBeanFactory() {
        boolean assignableFrom = BeanFactory.class.isAssignableFrom(GenericXmlApplicationContext.class);
        LOG.debug("GenericXmlApplicationContext < BeanFactory: " + assignableFrom);
    }

    /**
     * StaticApplicationContext类的作用是，硬编码Bean及其对类，不使用配置文件
     */
    @Test
    public void testRunSpringByStaticApplicationContext() {
        StaticApplicationContext staticApplicationContext = new StaticApplicationContext();
        staticApplicationContext.registerSingleton("person", Person.class);
        Person p = staticApplicationContext.getBean(Person.class);
        LOG.debug(p.toString());
    }

    @Test
    public void testRunSpringByAnnotationConfigApplicationContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        String basePackage = "jee.billy.springstudy.bean";
        String basePackageWithVariable = "jee.billy.springstudy.${sun.desktop}.newbean";
        context.scan(basePackage, basePackageWithVariable);
        context.refresh();

        int beanDefinitionCount = context.getBeanDefinitionCount();
        LOG.debug("There is/are " + beanDefinitionCount + " bean(s) in total.");
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            LOG.debug(beanName + " -> " + beanDefinition.toString());
        }
        Person p = context.getBean(PersonAnnotation.class);
        LOG.debug(p.toString());
        WindowsBean windowsBean = context.getBean(WindowsBean.class);
        LOG.debug(windowsBean.toString());
        context.close();
    }


    /**
     * 获取一个类的所有接口和祖先类
     */
    @Test
    public void testGetGenericXmlApplicationContextAncestor() {
        Class<GenericXmlApplicationContext> clazz = GenericXmlApplicationContext.class;
        Class<? super GenericXmlApplicationContext> superclass = clazz.getSuperclass();
        Set<Class<?>> allInterfacesForClassAsSet = ClassUtils.getAllInterfacesForClassAsSet(clazz);
        System.out.println("interfaces: ");
        for (Class<?> aClass : allInterfacesForClassAsSet) {
            System.out.println(aClass.getName());
        }

        System.out.println("\nancestor classes: ");
        while (!Objects.equals(Object.class, superclass)) {
            System.out.println(superclass.getName());
            superclass = superclass.getSuperclass();
        }
    }

}
