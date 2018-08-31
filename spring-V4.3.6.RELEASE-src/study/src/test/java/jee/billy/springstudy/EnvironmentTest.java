package jee.billy.springstudy;

import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ClassUtils;

/**
 * 初步理解为：Environment类的作用是提供系统级别的key-value关系
 *
 * @author liulei@bshf360.com
 * @since 2018-05-14 18:35
 */
public class EnvironmentTest extends SpringBaseTest {

    @Test
    public void test() {
        Environment environment = new StandardEnvironment();
        System.out.println(environment);

        String s = ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders("com.aaa"));
        System.out.println(s);
    }
}
