package billy.spring.study;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Spring容器外的类获取容器内的对象
 *
 * @author liulei@bshf.com
 */
public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appCtx = applicationContext;
    }

    /**
     * 得到一个BEAN
     *
     * @param beanName bean的名字
     * @return 返回一个bean对象
     */
    public static Object getBean(String beanName) {
        return appCtx.getBean(beanName);
    }
}

