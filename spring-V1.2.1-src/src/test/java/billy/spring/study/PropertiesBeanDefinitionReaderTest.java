package billy.spring.study;

import billy.spring.study.bean.PersonPropertiesBean;
import billy.spring.study.common.SpringBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-24 15:26
 */
public class PropertiesBeanDefinitionReaderTest extends SpringBaseTest {

    @Test
    public void test() {
        BeanDefinitionRegistry beanDefinitionRegistry = new DefaultListableBeanFactory();
        PropertiesBeanDefinitionReader pbdr = new PropertiesBeanDefinitionReader(beanDefinitionRegistry);
        // 加载properties注册成Bean 开始
        // 两种方式功能一样，任选其一
        // 方式一，使用properties文件
        //pbdr.loadBeanDefinitions(new ClassPathResource("/spring/applicationContext-properties.properties"));

        // 方式二，使用key-value
        Map<String, String> beanDefinitionMap = new HashMap<>();
        beanDefinitionMap.put("personPropertiesBean.class", "billy.spring.study.bean.PersonPropertiesBean");
        beanDefinitionMap.put("personPropertiesBean.name", "billy");
        beanDefinitionMap.put("personPropertiesBean.age", "27");
        pbdr.registerBeanDefinitions(beanDefinitionMap);
        // 加载properties注册成Bean 结束

        BeanFactory beanFactory = (BeanFactory) beanDefinitionRegistry;
        Object personPropertiesBeanObj = beanFactory.getBean("personPropertiesBean");
        PersonPropertiesBean personPropertiesBean = (PersonPropertiesBean) personPropertiesBeanObj;
        LOG.debug(personPropertiesBean);

    }
}
