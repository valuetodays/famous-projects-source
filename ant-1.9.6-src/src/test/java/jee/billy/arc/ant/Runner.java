package jee.billy.arc.ant;

import org.apache.tools.ant.launch.LaunchException;
import org.apache.tools.ant.launch.Launcher;
import org.apache.tools.ant.launch.LogLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.MalformedURLException;

/**
 * @author <a href="http://blog.sina.com.cn/valuetodays">liulei-frx</a>
 * @date 2016-03-31 18:28
 * @since 2016-03-31 18:28
 */
public class Runner {

    public static void main(String[] args) throws MalformedURLException, LaunchException {
        LogLogger l = new LogLogger();
//        l.hello();
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "proxy-context-aop.xml");
//        Launcher la = context.getBean(Launcher.class);
        Launcher la = new Launcher();
        la.run(new String[]{});
        ((ClassPathXmlApplicationContext) context).close();
    }
}
