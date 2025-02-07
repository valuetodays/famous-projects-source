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

package org.springframework.jmx.export.metadata;

/**
 * Base class for all JMX metadata classes.
 *
 * @author Rob Harrop
 * @since 1.2
 */
public class AbstractJmxAttribute {

	private String description = "";

	private int currencyTimeLimit = -1;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setCurrencyTimeLimit(int currencyTimeLimit) {
		this.currencyTimeLimit = currencyTimeLimit;
	}

	public int getCurrencyTimeLimit() {
		return currencyTimeLimit;
	}

}
