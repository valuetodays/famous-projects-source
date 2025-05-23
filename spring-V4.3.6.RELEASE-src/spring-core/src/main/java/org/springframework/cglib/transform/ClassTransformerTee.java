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
package org.springframework.cglib.transform;


import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;

public class ClassTransformerTee extends ClassTransformer {
    private ClassVisitor branch;
    
    public ClassTransformerTee(ClassVisitor branch) {
        super(Opcodes.ASM5);
        this.branch = branch;
    }
    
    public void setTarget(ClassVisitor target) { 
        cv = new ClassVisitorTee(branch, target);
    }
}
