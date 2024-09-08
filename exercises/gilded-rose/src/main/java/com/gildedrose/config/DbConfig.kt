package com.gildedrose.config

import com.zaxxer.hikari.HikariDataSource
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonEmptyString
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
) {
    constructor(environment: Environment) : this(
        jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(environment),
        username = EnvironmentKey.nonEmptyString().required("db.username")(environment),
        password = EnvironmentKey.nonEmptyString().required("db.password")(environment),
    )

    fun dslContext(): DSLContext {
        val dataSource = hikariDataSource()
        return DSL.using(dataSource, SQLDialect.H2)
    }

    fun hikariDataSource(): HikariDataSource {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = jdbcUrl.toString()
        dataSource.username = username
        dataSource.password = password
        dataSource.validate()
        return dataSource
    }
}




