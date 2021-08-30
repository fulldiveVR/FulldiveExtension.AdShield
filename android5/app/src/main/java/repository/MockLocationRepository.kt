package repository

class LocationRepository {

    private val locations = listOf("Frankfurt", "London", "Los Angeles", "Montreal", "New York")

    fun getLocations() = locations
}