package uni.matilde.lam01.ui.audio

import com.google.android.gms.maps.model.LatLng

data class DisplayedAudioInfo(
    val id: Int,
    val timestamp: String,
    val locationName: String,
    val latLng: LatLng,
    val filePath: String,
    val hidden: Boolean?
)
