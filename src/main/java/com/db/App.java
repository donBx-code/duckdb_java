package com.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Date;
import java.util.StringJoiner;
/**
 * Hello world!
 *
 */
public class App 
{
    public static printColumnames(int i,)
    {

        System.out.print(null);
    }
    public static void main( String[] args )
    {
        System.out.println( "DUCK DB EXAMPLE" );

        try{
        Class.forName("org.duckdb.DuckDBDriver");
        Connection conn = DriverManager.getConnection("jdbc:duckdb:");


        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * from read_parquet('/Users/donbulosan/Code/myJava/duckdb_transform/duckdbtrx/src/resources/data/weather_by_city.parquet')");
        
        ResultSetMetaData rsm = rs.getMetaData();

        int colCount = rsm.getColumnCount();
        

        while(rs.next())
        {
            StringJoiner rowJoiner = new StringJoiner(" | ");
            for(int i = 1; i <= colCount ; i++) 
            {

                Object value = rs.getObject(i);
                rowJoiner.add(value != null ? value.toString():"NULL");

            }

            System.out.println(rowJoiner.toString());

        }
        
        } catch( Exception e){
            System.out.println(e);
        }
    }
}
