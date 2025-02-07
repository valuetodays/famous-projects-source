/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;

/**
 * Serves as an output stream to Javac. This let's us print messages
 * out to the log and detect whether or not Javac had an error
 * while compiling.
 *
 * @author James Duncan Davidson (duncan@x180.com)
 */

class JavacOutputStream extends OutputStream {

    private Task task;
    private StringBuffer line;
    private boolean errorFlag = false;

    /**
     * Constructs a new JavacOutputStream with the given task
     * as the output source for messages.
     */
    
    JavacOutputStream(Task task) {
        this.task = task;
        line = new StringBuffer();
    }

    /**
     * Write a character to the output stream. This method looks
     * to make sure that there isn't an error being reported and
     * will flush each line of input out to the project's log stream.
     */
    
    public void write(int c) throws IOException {
        char cc = (char)c;
        if (cc == '\r' || cc == '\n') {
            // line feed
            if (line.length() > 0) {
                processLine();
            }
        } else {
            line.append(cc);
        }
    }

    /**
     * Processes a line of input and determines if an error occured.
     */
    
    private void processLine() {
        String s = line.toString();
        if (s.indexOf("error") > -1) {
            errorFlag = true;
        }
        task.log(s);
        line = new StringBuffer();
    }

    /**
     * Returns the error status of the compile. If no errors occured,
     * this method will return false, else this method will return true.
     */
    
    boolean getErrorFlag() {
        return errorFlag;
    }
}

