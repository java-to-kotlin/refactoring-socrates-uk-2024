package example.leaderboard

import example.leaderboard.Transactor.Mode.ReadWrite

interface Transactor<out Resource> {
    enum class Mode { ReadOnly, ReadWrite }
    
    fun <T> perform(mode: Mode = ReadWrite, work: (Resource)-> T): T
}
