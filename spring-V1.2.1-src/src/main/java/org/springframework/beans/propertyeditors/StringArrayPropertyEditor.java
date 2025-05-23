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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * Editor for String arrays. Strings must be in CSV format.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see StringUtils#commaDelimitedListToStringArray
 * @see StringUtils#arrayToCommaDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	public void setAsText(String s) throws IllegalArgumentException {
		String[] array = StringUtils.commaDelimitedListToStringArray(s);
		setValue(array);
	}

	public String getAsText() {
		String[] array = (String[]) this.getValue();
		return StringUtils.arrayToCommaDelimitedString(array);
	}

}
