package uni.matilde.lam01.features.audio.model

import com.google.android.gms.maps.model.LatLng

data class DisplayedAudioInfo(
    val id: Int,
    val timestamp: String,
    val locationName: String,
    val latLng: LatLng,
    val filePath: String,
    val hidden: Boolean?
)
