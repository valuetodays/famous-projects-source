package billy.spring.study.bean;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-18 09:58
 */
public class Person2Bean extends PersonBean {
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + super.toString();
    }
}
