package org.apache.tools.ant;

import java.lang.reflect.InvocationTargetException;

/**
 * individued from org.apache.tools.ant.IntrospectionHelper
 * @author Administrator
 *
 */
public interface NestedCreator {
    public Object create(Object parent) 
        throws InvocationTargetException, IllegalAccessException, InstantiationException;
}