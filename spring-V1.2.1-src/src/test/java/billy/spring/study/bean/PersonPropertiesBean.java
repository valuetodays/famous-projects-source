package billy.spring.study.bean;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-24 15:36
 */
public class PersonPropertiesBean extends PersonBean {

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + super.toString();
    }
}
