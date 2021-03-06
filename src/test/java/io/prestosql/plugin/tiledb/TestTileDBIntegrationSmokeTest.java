/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.tiledb;

import io.airlift.tpch.TpchTable;
import io.prestosql.spi.PrestoException;
import io.prestosql.testing.AbstractTestIntegrationSmokeTest;
import io.prestosql.testing.MaterializedResult;
import io.prestosql.testing.QueryRunner;
import io.tiledb.java.api.Context;
import io.tiledb.java.api.TileDBError;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static io.prestosql.plugin.tiledb.TileDBErrorCode.TILEDB_UNEXPECTED_ERROR;
import static io.prestosql.spi.type.VarcharType.VARCHAR;
import static io.prestosql.testing.assertions.Assert.assertEquals;
import static io.tiledb.java.api.TileDBObject.remove;
import static org.assertj.core.api.Assertions.assertThat;

@Test
public class TestTileDBIntegrationSmokeTest
        extends AbstractTestIntegrationSmokeTest
{
    private Context ctx;

    public TestTileDBIntegrationSmokeTest()
    {
        super();
        try {
            ctx = new Context();
        }
        catch (TileDBError tileDBError) {
            throw new PrestoException(TILEDB_UNEXPECTED_ERROR, tileDBError);
        }
    }

    protected boolean isParameterizedVarcharSupported()
    {
        return false;
    }

    @Override
    protected QueryRunner createQueryRunner() throws Exception
    {
        return TileDBQueryRunner.createTileDBQueryRunner();
    }

    @Test
    public void testDescribeTable()
    {
        MaterializedResult actualColumns = computeActual("DESC orders").toTestTypes();
        MaterializedResult expectedColumns = getExpectedOrdersTableDescription(isParameterizedVarcharSupported());
        assertEquals(actualColumns, expectedColumns);
    }

    @Test
    public void testShowCreateTable()
    {
        assertThat((String) computeActual("SHOW CREATE TABLE orders").getOnlyValue())
                // If the connector reports additional column properties, the expected value needs to be adjusted in the test subclass
                .matches("CREATE TABLE tiledb.tiledb.orders \\Q(\n" +
                        "   orderkey bigint COMMENT 'Dimension',\n" +
                        "   custkey bigint COMMENT 'Dimension',\n" +
                        "   orderstatus varchar(1) COMMENT 'Attribute',\n" +
                        "   totalprice double COMMENT 'Attribute',\n" +
                        "   orderdate date COMMENT 'Attribute',\n" +
                        "   orderpriority varchar COMMENT 'Attribute',\n" +
                        "   clerk varchar COMMENT 'Attribute',\n" +
                        "   shippriority integer COMMENT 'Attribute',\n" +
                        "   comment varchar COMMENT 'Attribute'\n" +
                        ")");
    }

    @AfterClass(alwaysRun = true)
    public final void destroy()
    {
        for (TpchTable<?> table : TpchTable.getTables()) {
            try {
                remove(ctx, table.getTableName());
            }
            catch (TileDBError tileDBError) {
                throw new PrestoException(TILEDB_UNEXPECTED_ERROR, tileDBError);
            }
        }
    }

    private MaterializedResult getExpectedOrdersTableDescription(boolean parametrizedVarchar)
    {
        if (parametrizedVarchar) {
            return MaterializedResult.resultBuilder(getQueryRunner().getDefaultSession(), VARCHAR, VARCHAR, VARCHAR, VARCHAR)
                    .row("orderkey", "bigint", "", "Dimension")
                    .row("custkey", "bigint", "", "Dimension")
                    .row("orderstatus", "varchar(1)", "", "Attribute")
                    .row("totalprice", "double", "", "Attribute")
                    .row("orderdate", "date", "", "Attribute")
                    .row("orderpriority", "varchar(15)", "", "Attribute")
                    .row("clerk", "varchar(15)", "", "Attribute")
                    .row("shippriority", "integer", "", "Attribute")
                    .row("comment", "varchar(79)", "", "Attribute")
                    .build();
        }
        else {
            return MaterializedResult.resultBuilder(getQueryRunner().getDefaultSession(), VARCHAR, VARCHAR, VARCHAR, VARCHAR)
                    .row("orderkey", "bigint", "", "Dimension")
                    .row("custkey", "bigint", "", "Dimension")
                    .row("orderstatus", "varchar(1)", "", "Attribute")
                    .row("totalprice", "double", "", "Attribute")
                    .row("orderdate", "date", "", "Attribute")
                    .row("orderpriority", "varchar", "", "Attribute")
                    .row("clerk", "varchar", "", "Attribute")
                    .row("shippriority", "integer", "", "Attribute")
                    .row("comment", "varchar", "", "Attribute")
                    .build();
        }
    }
}
