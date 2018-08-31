package jee.billy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://blog.sina.com.cn/valuetodays">liulei-frx</a>
 * @date 2016-04-25 12:37
 * @since 2016-04-25 12:37
 */
public class StackPrintUtil {

    private static Logger log = LoggerFactory.getLogger(StackPrintUtil.class);

    private static List<String> list = new ArrayList<>();
    static {
        list.add("sun.reflect.");
        list.add("java.lang.");
        list.add("com.intellij.");
    }

    private static boolean isIgnore(String prefix) {
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (prefix.startsWith(s)) {
                return true;
            }
        }

        return false;
    }
public static void p(String head) {
    Throwable t = new Throwable();
    StackTraceElement[] ss = t.getStackTrace();
    if (null != ss) {
        String strAll = "";
        for (int i = ss.length - 1; i >= 0; i--) {
            StackTraceElement s = ss[i];
            String className = s.getClassName();
            String methodName = s.getMethodName();
            if (isIgnore(className)) {
                return;
            }
            String str = "  | in " + className + "." + methodName + "()" + System.getProperty("line.separator");
            strAll += str;
        }
        if (strAll.length() > 0) {
            log.debug("===" + head + "===" + System.getProperty("line.separator") + strAll);
        }
    }
}
    public static void p() {
        p("");
    }
}
