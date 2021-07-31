package com.grinisrit.crypto

import com.grinisrit.crypto.deribit.*
import org.litote.kmongo.KMongo
import org.litote.kmongo.*
import org.litote.kmongo.getCollection
import space.kscience.plotly.*
import java.time.Instant


class Orders(
    val prices: FloatArray,
    val amounts: FloatArray,
) {
    val size = prices.size

    fun getCost(amount: Float):Float {
        var rest = amount
        var cost = 0.0F

        for (i in 0 until size) {
            if (amounts[i] > rest) {
                cost += rest * prices[i]
                rest = 0.0F
                break
            }
            cost += amounts[i] * prices[i]
            rest -= amounts[i]
        }

        if (rest != 0.0F){
            return 0.0F
        }

        return cost
    }
}
/*
class Book(
    val asks: Orders,
    val bids: Orders
) {
    fun getBAS(amount: Float): Float = asks.getCost(amount) - bids.getCost(amount)
}

 */

fun getCost(amount: Float, data: List<OrderData>): Float {
    val size = data.size

    var rest = amount
    var cost = 0.0F

    for (i in 0 until size) {
        if (data[i].amount > rest) {
            cost += rest * data[i].price
            rest = 0.0F
            break
        }
        cost += data[i].amount * data[i].price
        rest -= data[i].amount
    }

    if (rest != 0.0F){
        return 0.0F
    }

    return cost
}

fun BookData.getBAS(amount: Float): Float {
    return getCost(amount, asks) - getCost(amount, bids)
}

fun loadBookData(): BookData {
    val mongo = KMongo.createClient("mongodb://localhost:27017")
    val col = mongo.getDatabase("deribit").getCollection<TimestampedMarketBook>("book")

    val res = col.findOne(TimestampedMarketBook::platform_data / Book::params/ BookParameters::data / BookData::instrument_name eq "BTC-PERPETUAL")

    return res!!.platform_data.params.data
}

fun main(){
    val amounts = (1..15).map { it * 1000F }
    val bookData = loadBookData()
    val prices = amounts.map { bookData.getBAS(it) }
   // println(bookData.getBAS(1000F))

   val plot = Plotly.plot {
        trace {
            x.set(amounts)
            y.set(prices)
            name = "kek"
        }

        layout {
            title = "Deribit BTC info"
            xaxis {
                title = "Amount"
            }
            yaxis {
                title = "Bid-ask spread, $"
            }
        }
    }

    plot.makeFile()
}