package lol.saba.app.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spark.Response

inline fun <reified T> Response.json(data: T) = body(Json.encodeToString(data))
