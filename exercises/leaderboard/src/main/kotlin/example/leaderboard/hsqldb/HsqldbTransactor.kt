package example.leaderboard.hsqldb

import example.leaderboard.Transactor
import example.leaderboard.Transactor.Mode
import example.leaderboard.Transactor.Mode.ReadOnly
import java.sql.Connection
import java.sql.SQLTransactionRollbackException
import javax.sql.DataSource

class HsqldbTransactor<Resource>(
    private val getConnection: () -> Connection,
    private val createResource: (Connection) -> Resource
) : Transactor<Resource> {
    constructor(dataSource: DataSource, createResource: (Connection) -> Resource) :
        this(dataSource::getConnection, createResource)
    
    override fun <T> perform(mode: Mode, work: (Resource) -> T): T =
        getConnection().use { c ->
            c.autoCommit = false
            c.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
            c.isReadOnly = (mode == ReadOnly)
            
            c.runTransaction(createResource(c), work)
        }
    
    private fun <T> Connection.runTransaction(resource: Resource, work: (Resource) -> T): T {
        var tryCount = 0
        
        while (true) {
            tryCount++
            
            try {
                val result = work(resource)
                commit()
                return result
            } catch (e: Exception) {
                if (tryCount == 3 || !e.isSerialisationFailure()) {
                    rollback()
                    throw e
                }
            }
        }
    }
}

private fun Exception.isSerialisationFailure() =
    (this as? SQLTransactionRollbackException)?.sqlState == "40001"
