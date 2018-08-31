package billy.spring.study.bean;

import billy.spring.study.ApplicationContextHelper;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-18 09:59
 */
public class PersonBean2Factory {

    public static PersonBean getPersonBean(int n) {
        Object bean = ApplicationContextHelper.getBean("person" + n + "Bean");
        return (PersonBean) bean;
    }
}
