package com.earasoft.rdf4j.cassandra;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;

public class Test {

    public static void main(String[] args) {
        try(CqlSession session = CqlSession.builder().build();){

//            ResultSet rs = session.execute("SELECT release_version FROM system.local");
//
//            for (Row row : rs) {
//                // process the row
//                System.out.println(row);
//            }
//
            CreateKeyspace createKeyspace = SchemaBuilder.createKeyspace("testing")
                    .ifNotExists()
                    .withSimpleStrategy(1);

            session.execute(createKeyspace.build());

            session.execute("USE " + CqlIdentifier.fromCql("testing"));


            CreateTable createTable = SchemaBuilder.createTable("spoc1")

                    .withPartitionKey("s", DataTypes.TEXT)
                    .withPartitionKey("p", DataTypes.TEXT)
                    .withColumn("title", DataTypes.TEXT)
                    .withColumn("creation_date", DataTypes.TIMESTAMP);

            session.execute(createTable.build());



            PreparedStatement preparedInsertExpense =
                    session.prepare(
                            "INSERT INTO cyclist_expenses (cyclist_name, expense_id, amount, description, paid) "
                                    + "VALUES (:name, :id, :amount, :description, :paid)");


            BatchStatement batch =
                    BatchStatement.newInstance(
                            DefaultBatchType.LOGGED,
                            preparedInsertExpense.bind("Vera ADRIAN", 1, 7.95f, "Breakfast", false)).setKeyspace("testing");

            session.execute(batch);



        }

    }
}
