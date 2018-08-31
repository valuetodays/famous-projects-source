package jee.billy.springstudy.bean;

import jee.billy.util.StackPrintUtil;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-07 18:31
 */
public class PersonInitializingBean extends Person implements InitializingBean {
    private String msg;


    @Override
    public void afterPropertiesSet() throws Exception {
        StackPrintUtil.p("PersonInitializingBean");
        msg = "PersonInitializingBean";
    }

    @Override
    public String toString() {
        return "PersonInitializingBean{" +
                "msg='" + msg + '\'' +
                "} " + super.toString();
    }
}
