package com.eeeffff.hasentinel.influxdb.util.sqlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat.Condition;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class SqlParserUtil {
	/**
	 * SQL解析
	 * 
	 * @param sql
	 * @return
	 */
	public static SqlMeta parse(String sql) {
		try {
			// 新建 MySQL Parser
			SQLStatementParser parser = new MySqlStatementParser(sql);

			// 使用Parser解析生成AST，这里SQLStatement就是AST
			SQLStatement sqlStatement = parser.parseStatement();

			// 使用select访问者进行select的关键信息打印
			SelectPrintVisitor selectPrintVisitor = new SelectPrintVisitor();
			sqlStatement.accept(selectPrintVisitor);
			SqlMeta sqlMeta = selectPrintVisitor.getSqlMeta();

			MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
			sqlStatement.accept(visitor);
			List<Condition> conditions = Optional.ofNullable(visitor.getConditions())
					.orElse(new ArrayList<Condition>());
			conditions.forEach(c -> {
				sqlMeta.getWhereKeyValues().put(c.getColumn().getName(),
						c.getValues().size() > 0 ? c.getValues().get(0) : "");
			});
			return sqlMeta;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}
