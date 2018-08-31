package jee.billy.springstudy;

import jee.billy.springstudy.bean.Person;
import jee.billy.springstudy.bean.PersonBeanNameWare;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-09 09:45
 */
public class PersonBeanNameAwareTests extends SpringBaseTest {

    @Test
    public void testBeanNameAware() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:spring/application-beannameaware.xml");
        // 不执行refresh命令就去启动服务会报错的。 只执行context.start();也会报错的
        context.refresh();
        int beanDefinitionCount = context.getBeanDefinitionCount();
        LOG.debug("There is/are " + beanDefinitionCount + " bean(s) in total.");

        Person p = context.getBean(PersonBeanNameWare.class);
        LOG.debug(p.toString());
        context.close();
    }

}
