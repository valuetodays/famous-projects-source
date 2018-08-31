package jee.billy.springstudy.bean;

import org.springframework.stereotype.Component;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-11 13:47
 */
@Component
public class PersonAOP extends Person {

    public void showMe() {
        System.out.println("I\'m aop");
    }
}
