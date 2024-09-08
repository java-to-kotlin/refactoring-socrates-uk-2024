package example.leaderboard.hsqldb

import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import java.sql.PreparedStatement
import java.sql.ResultSet


fun PreparedStatement.set(index: Int, param: Value<Int>) =
    setInt(index, param.value)


fun <T> ResultSet.asSequence(f: ResultSet.() -> T) =
    generateSequence { if (next()) f() else null }


fun <T> ResultSet.singleOrNull(f: ResultSet.() -> T): T? =
    if (next()) {
        f().also { if (next()) error("received more than one result") }
    } else {
        null
    }


fun <T : Value<Int>> ResultSet.get(valueFactory: ValueFactory<T,Int>, columnName: String): T =
    valueFactory.of(getInt(columnName))


fun <T : Value<Int>> ResultSet.get(valueFactory: ValueFactory<T,Int>, columnIndex: Int): T =
    valueFactory.of(getInt(columnIndex))
