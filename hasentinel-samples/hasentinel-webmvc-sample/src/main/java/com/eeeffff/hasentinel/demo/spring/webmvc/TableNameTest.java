package com.eeeffff.hasentinel.demo.spring.webmvc;

public class TableNameTest {

	public static void main(String[] args) {
		String sql1 = "=\"SELECT table_name,table_comment FROM information_schema.`TABLES` WHERE TABLE_SCHEMA = 'crm' and table_name in (";
		String sql2 = "'\"&A2&\"'";
		String sql3 = ");\"";
		
		for(int i=2;i<=193;i++) {
			sql2 = sql2+"'\"&A"+i+"&\"'";
			if(i<193) {
				sql2+=",";
			}
		}
		System.out.println(sql1+sql2+sql3);
	}

}
