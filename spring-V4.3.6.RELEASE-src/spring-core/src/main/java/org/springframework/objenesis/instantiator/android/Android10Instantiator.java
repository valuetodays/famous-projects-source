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
package org.springframework.objenesis.instantiator.android;

import org.springframework.objenesis.ObjenesisException;
import org.springframework.objenesis.instantiator.ObjectInstantiator;
import org.springframework.objenesis.instantiator.annotations.Instantiator;
import org.springframework.objenesis.instantiator.annotations.Typology;

import java.io.ObjectInputStream;
import java.lang.reflect.Method;


/**
 * Instantiator for Android API level 10 and lover which creates objects without driving their
 * constructors, using internal methods on the Dalvik implementation of
 * {@link ObjectInputStream}.
 *
 * @author Piotr 'Qertoip' Włodarek
 */
@Instantiator(Typology.STANDARD)
public class Android10Instantiator<T> implements ObjectInstantiator<T> {
   private final Class<T> type;
   private final Method newStaticMethod;

   public Android10Instantiator(Class<T> type) {
      this.type = type;
      newStaticMethod = getNewStaticMethod();
   }

   public T newInstance() {
      try {
         return type.cast(newStaticMethod.invoke(null, type, Object.class));
      }
      catch(Exception e) {
         throw new ObjenesisException(e);
      }
   }

   private static Method getNewStaticMethod() {
      try {
         Method newStaticMethod = ObjectInputStream.class.getDeclaredMethod(
           "newInstance", Class.class, Class.class);
         newStaticMethod.setAccessible(true);
         return newStaticMethod;
      }
      catch(RuntimeException e) {
         throw new ObjenesisException(e);
      }
      catch(NoSuchMethodException e) {
         throw new ObjenesisException(e);
      }
   }

}
