package org.apache.tools.ant.launch;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://blog.sina.com.cn/valuetodays">liulei-frx</a>
 * @date 2016-04-22 20:02
 * @since 2016-04-22 20:02
 */
@Component
@Aspect
public class LogLogger {
    private static Logger log = LoggerFactory.getLogger(LogLogger.class);

    public void hello() {
        System.out.println("hello,syso");
        log.debug("hello, log.debut");
    }
    @Pointcut("execution(* org.apache.tools..*(..))")
//    @Pointcut("execution(* aop..*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint jp) {
        System.out.println("<<<<<<<<<<<<");
    }

//    @Around("execution(* com.billy.jee.littleleaf.web.service.*.*(..))")
    @Around("execution(* org.apache.tools.ant.*.*(..))")
    public Object doAround(ProceedingJoinPoint pjp)
            throws Throwable {

        long begin = System.nanoTime();
        Object o = pjp.proceed();
        long end = System.nanoTime();

        log.debug("[[[[[[[" + pjp.getTarget().getClass() + "." +
                pjp.getSignature().getName() + "[" + (end - begin) + "]");
        return o;
    }


}
