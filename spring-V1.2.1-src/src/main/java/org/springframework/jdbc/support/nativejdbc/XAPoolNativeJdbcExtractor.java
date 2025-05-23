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

package org.springframework.jdbc.support.nativejdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.enhydra.jdbc.core.CoreConnection;
//import org.enhydra.jdbc.core.CorePreparedStatement;

/**
 * Implementation of the NativeJdbcExtractor interface for ObjectWeb's XAPool.
 * Returns underlying native Connections and native PreparedStatements to
 * application code instead of XAPool's wrapper implementations; unwraps the
 * Connection for native Statements and native CallableStatements.
 * The returned JDBC classes can then safely be cast, e.g. to OracleResultSet.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * an XAPool DataSource: If a given object is not an XAPool wrapper, it will
 * be returned as-is.
 *
 * TODO [20180517] 找不到对应的jar
 *
 * @author Juergen Hoeller
 * @since 06.02.2004
 */
public class XAPoolNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	/**
	 * Return true, as CoreStatement does not allow access to the
	 * underlying Connection.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	/**
	 * Return true, as CoreCallableStatement does not allow access to the
	 * underlying Connection.
	 */
	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		/*if (con instanceof CoreConnection) {
			return ((CoreConnection) con).con;
		}*/
		return con;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		/*if (ps instanceof CorePreparedStatement) {
			return ((CorePreparedStatement) ps).ps;
		}*/
		return ps;
	}

}
