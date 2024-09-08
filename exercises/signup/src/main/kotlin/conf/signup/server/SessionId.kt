package conf.signup.server

import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.NonBlankStringValueFactory
import dev.forkhandles.values.StringValue


class SessionId (value: String) :
    StringValue(value),
    ComparableValue<StringValue,String>
{
    companion object : NonBlankStringValueFactory<SessionId>(::SessionId)
}

