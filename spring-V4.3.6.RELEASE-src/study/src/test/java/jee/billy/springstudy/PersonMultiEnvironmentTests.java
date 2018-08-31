package jee.billy.springstudy;

import jee.billy.springstudy.bean.Person;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Arrays;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-09 09:45
 */
public class PersonMultiEnvironmentTests extends SpringBaseTest {

    @Test
    public void testMultiEnvironment() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:spring/application-multi-environment.xml");
        // 不执行refresh命令就去启动服务会报错的。 只执行context.start();也会报错的
        context.refresh();

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] people = beanFactory.getAliases("person");
        Arrays.stream(people).forEach(System.out::println);

        Person p = context.getBean("person", Person.class);
        LOG.debug(p.toString());
        context.close();
    }

}
