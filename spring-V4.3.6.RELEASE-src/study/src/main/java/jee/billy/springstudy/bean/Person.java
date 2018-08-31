package jee.billy.springstudy.bean;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-03 17:48
 */
public class Person {
    private int age;
    private String name;

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person [age=" + age + ", name=" + name + "]";
    }
}