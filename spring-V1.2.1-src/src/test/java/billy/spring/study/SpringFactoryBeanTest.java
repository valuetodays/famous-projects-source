package billy.spring.study;

import billy.spring.study.bean.PersonBean;
import billy.spring.study.common.SpringBaseTest;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-19 15:02
 */
public class SpringFactoryBeanTest extends SpringBaseTest {

    @Test
    public void test() {
        String[] locations = new String[] {"classpath:/spring/applicationContext-factorybean.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);

        PersonBean person = (PersonBean)context.getBean("personBean", PersonBean.class);
        LOG.debug(person);
        context.close();

    }
}
