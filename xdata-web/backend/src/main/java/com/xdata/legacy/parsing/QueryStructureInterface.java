/**
 * 
 */
package com.xdata.legacy.parsing;
import com.xdata.util.TableMap;

import java.util.Vector;
import com.xdata.legacy.parsing.ForeignKey;
import com.xdata.util.TableMap;

/**
 * @author shree
 *
 */
public interface QueryStructureInterface {
		
		public TableMap getTableMap();
		public void setTableMap(TableMap tableMap);
		public void buildQueryStructureJSQL(String queryId, 
				String queryString, boolean debug,AppTest_Parameters dbAppParameters) throws Exception;
		
		public Vector<ForeignKey> getForeignKeyVectorModified();
		public void setForeignKeyVectorModified(
				Vector<ForeignKey> foreignKeyVectorModified);
		
		public String toString();	
	}

