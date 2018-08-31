package jee.billy.springstudy;

import jee.billy.springstudy.bean.Person;
import jee.billy.springstudy.bean.PersonAnnotation;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-11 13:42
 */
public class PersonAnnotationTest extends SpringBaseTest {

    @Test
    public void testGet() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:spring/application-annotation.xml");
        context.refresh();
        int beanDefinitionCount = context.getBeanDefinitionCount();
        LOG.debug("There is/are " + beanDefinitionCount + " bean(s) in total.");

        Person p = context.getBean(PersonAnnotation.class);
        LOG.debug(p.toString());
        context.close();
    }
}
