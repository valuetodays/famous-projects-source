package billy.spring.study;

import billy.spring.study.bean.PersonBean;
import billy.spring.study.common.SpringBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-17 17:34
 */
public class SpringRunTest extends SpringBaseTest {

    @Test
    public void run1() throws FileNotFoundException {
        ClassPathResource res = new ClassPathResource("/spring/applicationContext-run.xml");
        XmlBeanFactory factory = new XmlBeanFactory(res);
        PersonBean person = (PersonBean)factory.getBean("personBean", PersonBean.class);
        LOG.debug(person);
    }

    @Test
    public void run2() {
        String[] locations = new String[] {"classpath:/spring/applicationContext-run.xml"};
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);

        PersonBean person = (PersonBean)context.getBean("personBean", PersonBean.class);
        LOG.debug(person);
        context.close();
    }
}
