/**
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.objenesis.instantiator.sun;

import org.springframework.objenesis.ObjenesisException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Helper methods providing access to {@link sun.reflect.ReflectionFactory} via reflection, for use
 * by the {@link org.springframework.objenesis.instantiator.ObjectInstantiator}s that use it.
 * 
 * @author Henri Tremblay
 */
@SuppressWarnings("restriction")
class SunReflectionFactoryHelper {

   @SuppressWarnings("unchecked")
   public static <T> Constructor<T> newConstructorForSerialization(Class<T> type,
      Constructor<?> constructor) {
      Class<?> reflectionFactoryClass = getReflectionFactoryClass();
      Object reflectionFactory = createReflectionFactory(reflectionFactoryClass);

      Method newConstructorForSerializationMethod = getNewConstructorForSerializationMethod(
         reflectionFactoryClass);

      try {
         return (Constructor<T>) newConstructorForSerializationMethod.invoke(
            reflectionFactory, type, constructor);
      }
      catch(IllegalArgumentException e) {
         throw new ObjenesisException(e);
      }
      catch(IllegalAccessException e) {
         throw new ObjenesisException(e);
      }
      catch(InvocationTargetException e) {
         throw new ObjenesisException(e);
      }
   }

   private static Class<?> getReflectionFactoryClass() {
      try {
         return Class.forName("sun.reflect.ReflectionFactory");
      }
      catch(ClassNotFoundException e) {
         throw new ObjenesisException(e);
      }
   }

   private static Object createReflectionFactory(Class<?> reflectionFactoryClass) {
      try {
         Method method = reflectionFactoryClass.getDeclaredMethod(
            "getReflectionFactory");
         return method.invoke(null);
      }
      catch(NoSuchMethodException e) {
         throw new ObjenesisException(e);
      }
      catch(IllegalAccessException e) {
         throw new ObjenesisException(e);
      }
      catch(IllegalArgumentException e) {
         throw new ObjenesisException(e);
      }
      catch(InvocationTargetException e) {
         throw new ObjenesisException(e);
      }
   }

   private static Method getNewConstructorForSerializationMethod(Class<?> reflectionFactoryClass) {
      try {
         return reflectionFactoryClass.getDeclaredMethod(
            "newConstructorForSerialization", Class.class, Constructor.class);
      }
      catch(NoSuchMethodException e) {
         throw new ObjenesisException(e);
      }
   }
}
