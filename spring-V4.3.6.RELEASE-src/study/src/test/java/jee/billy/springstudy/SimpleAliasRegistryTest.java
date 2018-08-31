package jee.billy.springstudy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.SimpleAliasRegistry;

/**
 * 初步理解（2018-05-08）是PropertyResolver使用StringValueResolver来操作，
 * 因为无法直接单独使用StringValueResolver的某一实现类
 *
 *
 * @author liulei@bshf360.com
 * @since 2018-05-08 10:10
 */
public class SimpleAliasRegistryTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleAliasRegistryTest.class);

    String name1 = "name1";
    String alias1 = "alias1";
    String name2 = "name2";
    String alias2 = "alias2-${name1}";

    /**
     * 存放和获取对应的name和alias
     */
    @Test
    public void testSimple() {
        SimpleAliasRegistry sar = new SimpleAliasRegistry();
        sar.registerAlias(name1, alias1);
        sar.registerAlias(name2, alias2);
        LOG.debug(sar.getAliases(name1)[0]);
        LOG.debug(sar.getAliases(name2)[0]);
    }

    /**
     * 使用EmbeddedValueResolver来测试含有占位符的字符串，但未成功
     */
    @Test
    public void testByValueResolver_EmbeddedValueResolver() {
        SimpleAliasRegistry sar = new SimpleAliasRegistry();
        sar.registerAlias(name1, alias1);
        sar.registerAlias(name2, alias2);
        sar.resolveAliases(new EmbeddedValueResolver(new DefaultListableBeanFactory()));
        LOG.debug(sar.getAliases(name2)[0]);
    }


}
