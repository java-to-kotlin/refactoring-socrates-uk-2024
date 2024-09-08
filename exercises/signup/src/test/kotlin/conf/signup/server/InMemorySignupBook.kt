package conf.signup.server

class InMemorySignupBook : SignupBook {
    private val signupsById: MutableMap<SessionId, SignupSheet> = HashMap()
    
    override fun sheetFor(session: SessionId): SignupSheet? {
        // Return a copy of the sheet, to emulate behaviour of database
        return signupsById[session]?.clone()
    }
    
    private fun SignupSheet.clone(): SignupSheet {
        val clone = SignupSheet(sessionId, capacity)
        signups.forEach { clone.signUp(it) }
        return clone
    }
    
    override fun save(signup: SignupSheet) {
        val sessionId = signup.sessionId ?: error("SignupSheet has no sessionId")
        signupsById[sessionId] = signup
    }
}
