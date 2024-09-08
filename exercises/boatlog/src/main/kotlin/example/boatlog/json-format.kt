package example.boatlog

import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.array
import com.ubertob.kondor.json.datetime.str
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.num
import com.ubertob.kondor.json.str


object MaintenanceLogFormat : JAny<MaintenanceLog>() {
    val boat by str(MaintenanceLog::boat)
    val tasks by array(MaintenanceTaskFormat, MaintenanceLog::tasks)
    
    override fun JsonNodeObject.deserializeOrThrow() =
        MaintenanceLog(+boat, +tasks)
}

object MaintenanceTaskFormat : JSealed<MaintenanceTask>() {
    private val regular = "regular"
    private val cruising = "cruising"
    
    override val discriminatorFieldName: String = "when"
    
    override val subConverters = mapOf(
        regular to RegularMaintenanceFormat,
        cruising to CruisingMaintenanceFormat
    )
    
    override fun extractTypeName(obj: MaintenanceTask): String = when (obj) {
        is RegularMaintenance -> regular
        is CruisingMaintenance -> cruising
    }
}

object RegularMaintenanceFormat : JAny<RegularMaintenance>() {
    val task by str(RegularMaintenance::description)
    val frequency by num(RegularMaintenance::frequency)
    val lastDone by str(RegularMaintenance::lastDone)
    
    override fun JsonNodeObject.deserializeOrThrow() =
        RegularMaintenance(+task, +frequency, +lastDone)
}

object CruisingMaintenanceFormat : JAny<CruisingMaintenance>() {
    val task by str(CruisingMaintenance::description)
    val frequency by num(CruisingMaintenance::frequency)
    val remaining by num(CruisingMaintenance::remaining)
    
    override fun JsonNodeObject.deserializeOrThrow() =
        CruisingMaintenance(+task, +frequency, +remaining)
}
