import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val path = Paths.get("./input.txt")
    val lines = Files.readAllLines(path)
    var i = 0
    var caseNumber = 1
    while (i < lines.count()) {
        val n = lines[i].toInt()

        val countries = lines.subList(i + 1, i + n + 1).map { countryStr ->
            val countryParts = countryStr.split(" ")
            Country(
                name = countryParts[0],
                llc = Point(countryParts[1].toInt(), countryParts[2].toInt()),
                ruc = Point(countryParts[3].toInt(), countryParts[4].toInt())
            )
        }

        val cities = countries.flatMap { country ->
            val cities = mutableListOf<City>()
            for (y in country.llc.y..country.ruc.y) {
                for (x in country.llc.x..country.ruc.x) {
                    val city = City(country, Point(x, y))
                    for (c in countries) {
                        if (city.country == c) {
                            city.taxOffice[c] = Balance(1_000_000)
                        } else {
                            city.taxOffice[c] = Balance(0)
                        }
                    }
                    country.cities += city
                    cities += city
                }
            }
            cities
        }.associateBy { it.coordinate.hashCode() }

        for ((_, city) in cities) {
            val x = city.coordinate.x
            val y = city.coordinate.y

            val ln = Point(x - 1, y)
            val rn = Point(x + 1, y)
            val tn = Point(x, y + 1)
            val bn = Point(x, y - 1)

            if (cities.containsKey(ln.hashCode())) {
                city.neighbors += cities[ln.hashCode()]!!
            }

            if (cities.containsKey(rn.hashCode())) {
                city.neighbors += cities[rn.hashCode()]!!
            }

            if (cities.containsKey(tn.hashCode())) {
                city.neighbors += cities[tn.hashCode()]!!
            }

            if (cities.containsKey(bn.hashCode())) {
                city.neighbors += cities[bn.hashCode()]!!
            }
        }

        fun complete(day: Int): Boolean {
            var result = true
            for (country in countries) {
                var countryComplete = true
                for (city in country.cities) {
                    if (!city.complete) {
                        countryComplete = false
                        break
                    }
                }

                if (countryComplete && country.completeDay == -1)  {
                    country.completeDay = day
                }
                if (!countryComplete) {
                    result = false
                }
            }
            return result
        }

        var day = 0
        do {
            for ((_, city) in cities) {
                city.invalidate()
            }
            for ((_, city) in cities) {
                city.send()
            }
            day += 1
        } while (!complete(day))

        println("Case number: $caseNumber")
        countries.sortedWith(Comparator { o1: Country, o2: Country ->
            val c: Int = o1.completeDay - o2.completeDay
            if (c == 0) {
                return@Comparator o1.name.compareTo(o2.name)
            } else {
                return@Comparator c
            }
        }).forEach { country ->
            println("${country.name} ${country.completeDay}")
        }

        caseNumber += 1
        i += n
        i += 1
    }
}

data class Point(val x: Int, val y: Int)

data class Country(
    val name: String,
    val llc: Point,
    val ruc: Point
) {
    val cities = mutableListOf<City>()

    var completeDay = -1

}

data class City(val country: Country, val coordinate: Point) {

    val taxOffice = mutableMapOf<Country, Balance>()

    var neighbors = mutableListOf<City>()

    val complete: Boolean
        get() = taxOffice.values.all { it.amount != 0 }

    fun send() {
        for (neighbor in neighbors) {
            for ((c, b) in taxOffice) {
                val expanses = b.expanses
                b.amount -= expanses
                val nb  = neighbor.taxOffice[c]!!
                nb.amount += expanses
            }
        }
    }

    fun invalidate() {
        for ((_, balance) in taxOffice) {
            balance.invalidate()
        }
    }
}

data class Balance(var amount: Int) {

    var expanses = amount / 1000

    fun invalidate() {
        expanses = amount / 1000
    }
}
