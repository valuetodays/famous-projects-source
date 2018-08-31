package jee.billy.springstudy;

import org.junit.Test;
import org.springframework.util.ObjectUtils;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-11 15:31
 */
public class ObjectUtilsTest {
    @Test
    public void test() {
        String string = ObjectUtils.identityToString(this);
        System.out.println(string);
        System.out.println(this);
    }
}
