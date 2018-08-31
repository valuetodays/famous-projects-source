package jee.billy.springstudy.bean;

import org.springframework.stereotype.Component;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-11 13:42
 */
@Component
public class PersonAnnotation extends Person {
    private String annotation = "annotation";

    @Override
    public String toString() {
        return "PersonAnnotation{" +
                "annotation='" + annotation + '\'' +
                "} " + super.toString();
    }
}
