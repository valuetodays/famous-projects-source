/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cglib.core;


/**
 * The <code>GeneratorStrategy</code. is responsible for taking a
 * {@link ClassGenerator} and producing a byte array containing the
 * data for the generated <code>Class</code>.  By providing your
 * own strategy you may examine or modify the generated class before
 * it is loaded. Typically this will be accomplished by subclassing
 * {@link DefaultGeneratorStrategy} and overriding the appropriate
 * protected method.
 * @see AbstractClassGenerator#setStrategy
 */
public interface GeneratorStrategy {
    /**
     * Generate the class.
     * @param cg a class generator on which you can call {@link ClassGenerator#generateClass}
     * @return a byte array containing the bits of a valid Class
     */
    byte[] generate(ClassGenerator cg) throws Exception;

    /**
     * The <code>GeneratorStrategy</code> in use does not currently, but may
     * in the future, affect the caching of classes generated by {@link
     * AbstractClassGenerator}, so this is a reminder that you should
     * correctly implement <code>equals</code> and <code>hashCode</code>
     * to avoid generating too many classes.
     */
    boolean equals(Object o);
}
