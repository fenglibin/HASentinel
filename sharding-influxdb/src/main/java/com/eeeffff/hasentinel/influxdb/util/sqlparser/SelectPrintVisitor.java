package com.eeeffff.hasentinel.influxdb.util.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

/**
 * 
 * @author fenglibin
 *
 */
public class SelectPrintVisitor extends SQLASTVisitorAdapter {
	private SqlMeta sqlMeta = SqlMeta.builder().filedNames(new ArrayList<String>())
			.whereKeyValues(new HashMap<String, Object>()).build();

	@Override
	public boolean visit(SQLSelectQueryBlock x) {
		List<SQLSelectItem> selectItemList = x.getSelectList();
		selectItemList.forEach(selectItem -> {
			sqlMeta.getFiledNames().add(SQLUtils.toMySqlString(selectItem.getExpr()));
		});
		sqlMeta.setTableName(SQLUtils.toMySqlString(x.getFrom()));
		if (x.getWhere() != null) {
			sqlMeta.setWhereKeyValues(x.getWhere().getAttributes());
		}
		if (x.getLimit() != null) {
			sqlMeta.setLimit(Integer.parseInt(x.getLimit().getRowCount().toString()));
		}
		return true;
	}

	public SqlMeta getSqlMeta() {
		return sqlMeta;
	}

}
