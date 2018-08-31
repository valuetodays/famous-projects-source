package jee.billy.springstudy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * 把一个字符串路径转换成Resource对象或对象数组
 *
 * @author liulei@bshf360.com
 * @since 2018-05-07 17:00
 */
public class ResourcePatternResolverTest {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcePatternResolverTest.class);

    @Test
    public void testParseResource() throws IOException {
        String location = "classpath*:spring/application-*.xml";
        ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
        Resource[] resources = rpr.getResources(location);
        for(Resource resource : resources) {
            LOG.debug("found: " + resource.toString());
        }
    }
}
