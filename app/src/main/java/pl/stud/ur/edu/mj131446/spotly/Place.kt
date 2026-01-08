package pl.stud.ur.edu.mj131446.spotly

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Parcelize
@Serializable
data class Place(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String? = null,
    val contact: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val imageFilename: String? = null,
    val rating: Int = 0
) : Parcelable {

    companion object {
        fun default() = Place(
            title = "Brak informacji",
            note = "Brak informacji",
            contact = "Brak informacji",
            lat = 0.0,
            lon = 0.0,
            imageFilename = null,
            rating = 0
        )
    }
}
