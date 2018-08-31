package jee.billy.springstudy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Properties;

/**
 * 方法被Spring使用protected修饰，所以我们要把它们抽取出来
 * 真正调用代码的地方在
 * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)
 *
 * @author liulei@bshf360.com
 * @since 2018-05-08 10:41
 */
public class PropertiesParseTest {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesParseTest.class);

    // 注意，只有name1和name2是对应的key，所以当<code>pph.replacePlaceholders(alias2, props);</code>时alias2对应的并没有key，所以就原样输出了
    private final String name1 = "name1";
    private final String alias1 = "alias1";
    private final String name2 = "name2-${name1}-a${aaa}";
    private final String alias2 = "alias2-${alias1}-";

    protected String placeholderPrefix = PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX;
    protected String placeholderSuffix = PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX;

    @Test
    public void testParse() {
        Properties props = new Properties();
        props.setProperty(name1, alias1);
        props.setProperty(name2, alias2);

        PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix);
        String name1Final = pph.replacePlaceholders(name1, props);
        String alias1Final = pph.replacePlaceholders(alias1, props);
        String name2Final = pph.replacePlaceholders(name2, props);
        String alias2Final = pph.replacePlaceholders(alias2, props);
        LOG.debug("name1Final: " + name1Final);
        LOG.debug("alias1Final: " + alias1Final);
        LOG.debug("name2Final: " + name2Final);
        LOG.debug("alias2Final: " + alias2Final);
    }

}
