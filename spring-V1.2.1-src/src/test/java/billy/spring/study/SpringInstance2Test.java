package billy.spring.study;

import billy.spring.study.bean.PersonBean;
import billy.spring.study.common.SpringBaseTest;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-17 17:34
 */
public class SpringInstance2Test extends SpringBaseTest {

    /**
     *   Bean creation via static factory method
     *
     */
    @Test
    public void run() {
        String[] locations = new String[] {"classpath:/spring/applicationContext-instance2.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);

        PersonBean person = (PersonBean)context.getBean("properPerson2Bean", PersonBean.class);
        LOG.debug(person);
        context.close();
    }
}
