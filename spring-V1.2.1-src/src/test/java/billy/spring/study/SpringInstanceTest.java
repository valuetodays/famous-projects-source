package billy.spring.study;

import billy.spring.study.bean.PersonBean;
import billy.spring.study.common.SpringBaseTest;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-17 17:34
 */
public class SpringInstanceTest extends SpringBaseTest {

    /**
     *   Bean creation via static factory method
     *
     */
    @Test
    public void runViaStaticFactory() {
        String[] locations = new String[] {"classpath:/spring/applicationContext-instance.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);

        PersonBean person = (PersonBean)context.getBean("properPersonBean", PersonBean.class);
        LOG.debug(person);
        context.close();
    }

    /**
     *   Bean creation via instance factory method
     *
     */
    @Test
    public void runViaInstanceFactory() {
        String[] locations = new String[] {"classpath:/spring/applicationContext-instance.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);

        PersonBean person = (PersonBean)context.getBean("properPersonBeanInstance", PersonBean.class);
        LOG.debug(person);
        context.close();
    }
}
