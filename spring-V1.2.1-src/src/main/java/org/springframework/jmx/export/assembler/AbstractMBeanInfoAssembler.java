/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.assembler;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * Abstract implementation of the <code>MBeanInfoAssembler</code> interface
 * that encapsulates the creation of a <code>ModelMBeanInfo</code> instance
 * but delegates the creation of metadata to subclasses.
 *
 * <p>This class offers to flavors of Class extraction from a managed bean
 * instance: <code>getTargetClass</code>, extracting the target class behind
 * any kind of AOP proxy, and <code>getClassToExpose</code>, returning the
 * class or interface that will be searched for annotations and exposed
 * to the JMX runtime.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #getTargetClass(Object)
 * @see #getClassToExpose(Object)
 * @see #getClassToExpose(Class)
 */
public abstract class AbstractMBeanInfoAssembler implements MBeanInfoAssembler {

	/**
	 * Create an instance of the <code>ModelMBeanInfoSupport</code> class supplied with all
	 * JMX implementations and populates the metadata through calls to the subclass.
	 * @param managedBean the bean that will be exposed (might be an AOP proxy)
	 * @param beanKey the key associated with the managed bean
	 * @return the populated ModelMBeanInfo instance
	 * @throws JMException in case of errors
	 * @see #getDescription(Object, String)
	 * @see #getAttributeInfo(Object, String)
	 * @see #getConstructorInfo(Object, String)
	 * @see #getOperationInfo(Object, String)
	 * @see #getNotificationInfo(Object, String)
	 * @see #populateMBeanDescriptor(Descriptor, Object, String)
	 */
	public ModelMBeanInfo getMBeanInfo(Object managedBean, String beanKey) throws JMException {
		checkManagedBean(managedBean);
		ModelMBeanInfo info = new ModelMBeanInfoSupport(
				getClassName(managedBean, beanKey), getDescription(managedBean, beanKey),
				getAttributeInfo(managedBean, beanKey), getConstructorInfo(managedBean, beanKey),
				getOperationInfo(managedBean, beanKey), getNotificationInfo(managedBean, beanKey));
		Descriptor desc = info.getMBeanDescriptor();
		populateMBeanDescriptor(desc, managedBean, beanKey);
		info.setMBeanDescriptor(desc);
		return info;
	}

	/**
	 * Check the given bean instance, throwing an IllegalArgumentException
	 * if it is not eligible for exposure with this assembler.
	 * <p>Default implementation is empty, accepting every bean instance.
	 * @param managedBean the bean that will be exposed (might be an AOP proxy)
	 * @throws IllegalArgumentException the bean is not valid for exposure
	 */
	protected void checkManagedBean(Object managedBean) throws IllegalArgumentException {
	}

	/**
	 * Return the actual bean class of the given bean instance.
	 * This is the class exposed to description-style JMX properties.
	 * <p>Default implementation returns the target class for an AOP proxy,
	 * and the plain bean class else.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @return the bean class to expose
	 * @see Advised#getTargetSource
	 * @see org.springframework.aop.TargetSource#getTargetClass
	 */
	protected Class getTargetClass(Object managedBean) {
		if (AopUtils.isCglibProxy(managedBean)) {
			return managedBean.getClass().getSuperclass();
		}
		if (managedBean instanceof Advised) {
			return ((Advised) managedBean).getTargetSource().getTargetClass();
		}
		return managedBean.getClass();
	}

	/**
	 * Return the class or interface to expose for the given bean.
	 * This is the class that will be searched for attributes and operations
	 * (for example, checked for annotations).
	 * <p>Default implementation returns the target class for a CGLIB proxy,
	 * and the class of the given bean else (for a JDK proxy or a plain bean class).
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @return the bean class to expose
	 * @see AopUtils#isCglibProxy(Object)
	 * @see Advised#getTargetSource()
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	protected Class getClassToExpose(Object managedBean) {
		if (AopUtils.isCglibProxy(managedBean)) {
			return managedBean.getClass().getSuperclass();
		}
		return managedBean.getClass();
	}

	/**
	 * Return the class or interface to expose for the given bean class.
	 * This is the class that will be searched for attributes and operations
	 * (for example, checked for annotations).
	 * <p>Default implementation returns the superclass for a CGLIB proxy,
	 * and the given bean class else (for a JDK proxy or a plain bean class).
	 * @param beanClass the bean class (might be an AOP proxy class)
	 * @return the bean class to expose
	 * @see AopUtils#isCglibProxyClass(Class)
	 */
	protected Class getClassToExpose(Class beanClass) {
		if (AopUtils.isCglibProxyClass(beanClass)) {
			return beanClass.getSuperclass();
		}
		return beanClass;
	}


	/**
	 * Get the class name of the MBean resource.
	 * <p>Default implementation returns a simple description for the MBean
	 * based on the class name.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @return the MBean description
	 * @throws JMException in case of errors
	 */
	protected String getClassName(Object managedBean, String beanKey) throws JMException {
		return getTargetClass(managedBean).getName();
	}

	/**
	 * Get the description of the MBean resource.
	 * <p>Default implementation returns a simple description for the MBean
	 * based on the class name.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @throws JMException in case of errors
	 */
	protected String getDescription(Object managedBean, String beanKey) throws JMException {
		String targetClassName = getTargetClass(managedBean).getName();
		if (AopUtils.isAopProxy(managedBean)) {
			return "Proxy for " + targetClassName;
		}
		return targetClassName;
	}

	/**
	 * Called after the <code>ModelMBeanInfo</code> instance has been constructed but
	 * before it is passed to the <code>MBeanExporter</code>.
	 * <p>Subclasses can implement this method to add additional descriptors to the
	 * MBean metadata. Default implementation is empty.
	 * @param descriptor the <code>Descriptor</code> for the MBean resource.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @throws JMException in case of errors
	 */
	protected void populateMBeanDescriptor(Descriptor descriptor, Object managedBean, String beanKey)
			throws JMException {
	}

	/**
	 * Get the constructor metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all constructors that should
	 * be exposed in the management interface for the managed resource.
	 * <p>Default implementation returns an empty array of <code>ModelMBeanConstructorInfo</code>.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @return the constructor metadata
	 * @throws JMException in case of errors
	 */
	protected ModelMBeanConstructorInfo[] getConstructorInfo(Object managedBean, String beanKey)
			throws JMException {
		return new ModelMBeanConstructorInfo[0];
	}

	/**
	 * Get the notification metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all notifications that should
	 * be exposed in the management interface for the managed resource.
	 * <p>Default implementation returns an empty array of <code>ModelMBeanNotificationInfo</code>.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @return the notification metadata
	 * @throws JMException in case of errors
	 */
	protected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey)
			throws JMException {
		return new ModelMBeanNotificationInfo[0];
	}


	/**
	 * Get the attribute metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all the attributes that should
	 * be exposed in the management interface for the managed resource.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @return the attribute metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanAttributeInfo[] getAttributeInfo(Object managedBean, String beanKey)
			throws JMException;

	/**
	 * Get the operation metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all operations that should
	 * be exposed in the management interface for the managed resource.
	 * @param managedBean the bean instance (might be an AOP proxy)
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @return the operation metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanOperationInfo[] getOperationInfo(Object managedBean, String beanKey)
			throws JMException;

}
