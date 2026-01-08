package pl.stud.ur.edu.mj131446.spotly

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class PlaceRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spotly_prefs", Context.MODE_PRIVATE)
    private val KEY = "places_json"

    fun loadPlaces(): List<Place> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val arr = JSONArray(json)
        val out = mutableListOf<Place>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Place(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    note = o.optString("note", null),
                    contact = o.optString("contact", null),
                    lat = if (o.has("lat") && !o.isNull("lat")) o.getDouble("lat") else null,
                    lon = if (o.has("lon") && !o.isNull("lon")) o.getDouble("lon") else null,
                    imageFilename = o.optString("imageFilename", null),
                    rating = o.optInt("rating", 0)  // <- użyj optInt zamiast getInt
                )
            )
        }
        return out
    }

    fun savePlaces(list: List<Place>) {
        val arr = JSONArray()
        list.forEach { p ->
            val o = JSONObject()
            o.put("id", p.id)
            o.put("title", p.title)
            o.put("note", p.note)
            o.put("contact", p.contact)
            o.put("lat", p.lat)
            o.put("lon", p.lon)
            o.put("imageFilename", p.imageFilename)
            o.put("rating", p.rating)
            arr.put(o)
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun getPlace(placeId: String): Place? {
        return loadPlaces().find { it.id == placeId }
    }

    fun addPlace(place: Place) {
        val list = loadPlaces().toMutableList()
        list.add(0, place)
        savePlaces(list)
    }

    fun deletePlace(placeId: String) {
        val list = loadPlaces().filterNot { it.id == placeId }
        savePlaces(list)
    }

    fun saveBitmapToInternalStorage(bitmap: android.graphics.Bitmap, filename: String): String {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, out)
        }
        return file.absolutePath
    }

    fun loadBitmapFromInternalStorage(path: String): android.graphics.Bitmap? {
        return try {
            android.graphics.BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }
}
