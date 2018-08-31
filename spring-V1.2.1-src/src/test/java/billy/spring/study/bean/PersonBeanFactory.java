package billy.spring.study.bean;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-18 09:59
 */
public class PersonBeanFactory {

    public static PersonBean getPersonBean(int n) {
        System.out.println(n);
        if (n == 1) {
            return new Person1Bean();
        } else {
            return new Person2Bean();
        }
    }

    public PersonBean getInstance(int n) {
        System.out.println(n);
        if (n == 1) {
            return new Person1Bean();
        } else {
            return new Person2Bean();
        }
    }

}
