package jee.billy.springstudy;

import jee.billy.springstudy.bean.PersonAOP;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * 本示例导致spring启动不了
 *
 * @author liulei@bshf360.com
 * @since 2018-05-11 13:42
 */
public class PersonAOPTest extends SpringBaseTest {

    @Test
    public void testAOP() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:spring/application-aop.xml");
        context.refresh();
        int beanDefinitionCount = context.getBeanDefinitionCount();
        LOG.debug("There is/are " + beanDefinitionCount + " bean(s) in total.");

        PersonAOP p = context.getBean(PersonAOP.class);
        p.showMe();
        context.close();
    }
}
