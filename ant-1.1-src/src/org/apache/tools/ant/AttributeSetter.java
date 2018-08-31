package org.apache.tools.ant;

import java.lang.reflect.InvocationTargetException;

/**
 * individued from org.apache.tools.ant.IntrospectionHelper
 * @author Administrator
 *
 */
public interface AttributeSetter {
    public void set(Project p, Object parent, String value)
        throws InvocationTargetException, IllegalAccessException, 
               BuildException;
}