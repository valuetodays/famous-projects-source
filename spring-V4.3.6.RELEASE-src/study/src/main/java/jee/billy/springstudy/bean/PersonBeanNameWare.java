package jee.billy.springstudy.bean;

import jee.billy.util.StackPrintUtil;
import org.springframework.beans.factory.BeanNameAware;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-09 09:46
 */
public class PersonBeanNameWare extends Person implements BeanNameAware {
    private String beanName;


    public String getBeanName() {
        return beanName;
    }

    @Override
    public String toString() {
        return "PersonBeanNameWare{" +
                "beanName='" + beanName + '\'' +
                "} " + super.toString();
    }

    @Override
    public void setBeanName(String name) {
        StackPrintUtil.p("PersonBeanNameWare");
        this.beanName = name;
    }
}
