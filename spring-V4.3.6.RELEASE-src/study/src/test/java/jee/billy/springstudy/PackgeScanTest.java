package jee.billy.springstudy;

import org.junit.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * 测试包扫描路径
 *
 * 测试注解启动AnnotationConfigApplicationContext
 *
 * 代码见jee.billy.springstudy.Spring1st#testRunSpringByAnnotationConfigApplicationContext()
 *
 * @author liulei@bshf360.com
 * @since 2018-05-14 18:31
 */
public class PackgeScanTest extends SpringBaseTest {

    private String resourcePattern = "**/*.class";
    private ResourcePatternResolver resourcePatternResolver =
            ResourcePatternUtils.getResourcePatternResolver(null);

    @Test
    public void testGetResourcesFromPackage() throws IOException {
        String basePackage = "jee.billy.springstudy.bean";

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + this.resourcePattern;
        Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
        Arrays.stream(resources).forEach(System.out::println);
    }

    /**
     * 该代码见{@link ClassPathScanningCandidateComponentProvider}#resolveBasePackage()
     * @param basePackage the base package as specified by the user
     */
    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(basePackage);
    }


}
