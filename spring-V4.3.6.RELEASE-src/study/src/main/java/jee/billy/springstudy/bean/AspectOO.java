package jee.billy.springstudy.bean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author liulei@bshf360.com
 * @since 2018-05-11 13:56
 */
@Aspect
public class AspectOO {
    private static final String POINTCUT = "execution(* jee.billy.springstudy.bean..showMe())";

    @Around(POINTCUT)
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("ssssssssssssssssssssssss");
        Object[] args = pjp.getArgs();
        Object result;

        try {
             result = pjp.proceed(args);
        } catch (Exception e) {
            throw e;
        }

        return result;
    }
}
