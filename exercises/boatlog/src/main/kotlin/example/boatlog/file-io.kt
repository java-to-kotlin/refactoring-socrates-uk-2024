package example.boatlog

import com.ubertob.kondor.json.JsonStyle
import com.ubertob.kondor.json.toJson
import java.io.File

val logFile = File(System.getProperty("user.home")).resolve("maintenance-log.json")

fun loadMaintenanceLog(fromFile: File = logFile): MaintenanceLog =
    fromFile.inputStream()
        .use(MaintenanceLogFormat::fromJson)
        .orThrow()

fun saveMaintenanceLog(log: MaintenanceLog, toFile: File = logFile) =
    toFile.writer()
        .use { it.write(MaintenanceLogFormat.toJson(log, JsonStyle.pretty)) }

