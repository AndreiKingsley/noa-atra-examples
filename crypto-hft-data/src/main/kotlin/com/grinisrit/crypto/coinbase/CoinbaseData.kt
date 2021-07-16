package com.grinisrit.crypto.coinbase

import com.grinisrit.crypto.common.ChannelData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.time.Instant

interface CoinbaseData : ChannelData {
    val type: String
}

interface CoinbaseDataTime : CoinbaseData {
    val time: String
    val datetime: Instant
        get() = Instant.parse(time)
}

@Serializable
data class Heartbeat(
    override val type: String,
    val sequence: Long,
    val last_trade_id: Long,
    val product_id: String,
    override val time: String,
) : CoinbaseDataTime

@Serializable
data class Ticker(
    override val type: String,
    val trade_id: Long,
    val sequence: Long,
    override val time: String,
    val product_id: String,
    val price: String,
    val side: String,
    val last_size: String,
    val best_bid: String,
    val best_ask: String,
    val open_24h: String,
    val volume_24h: String,
    val low_24h: String,
    val high_24h: String,
    val volume_30d: String,
) : CoinbaseDataTime

@Serializable
data class OrderData(
    val price: String,
    val amount: String,
)

object OrderDataSerializer :
    JsonTransformingSerializer<OrderData>(OrderData.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element.jsonArray.let {
            buildJsonObject {
                put("price", it[0])
                put("amount", it[1])
            }
        }
    }
}

@Serializable
data class OrderUpdateData(
    val side: String,
    val price: String,
    val amount: String,
)

object OrderUpdateDataSerializer :
    JsonTransformingSerializer<OrderUpdateData>(OrderUpdateData.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element.jsonArray.let {
            buildJsonObject {
                put("side", it[0])
                put("price", it[1])
                put("amount", it[2])
            }
        }
    }
}

@Serializable
data class Snapshot(
    override val type: String,
    val product_id: String,
    val bids: List<@Serializable(with = OrderDataSerializer::class)OrderData>,
    val asks: List<@Serializable(with = OrderDataSerializer::class)OrderData>,
) : CoinbaseData

@Serializable
data class L2Update(
    override val type: String,
    val product_id: String,
    override val time: String,
    val changes: List<@Serializable(with = OrderUpdateDataSerializer::class) OrderUpdateData>,
) : CoinbaseDataTime

/*
@Serializable
data class Event(
    override val type: String,
): CoinbaseData

 */

// TODO()
object CoinbaseDataSerializer : JsonContentPolymorphicSerializer<CoinbaseData>(CoinbaseData::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"].toString()) {
        "\"ticker\"" -> Ticker.serializer()
        "\"snapshot\"" -> Snapshot.serializer()
        "\"l2update\"" -> L2Update.serializer()
        "\"heartbeat\"" -> Heartbeat.serializer()
        else -> throw Error(element.toString())
    }
}
