package com.satya.bhoomisetu.util

object Constants {
    const val BASE_URL = "https://bhoomisetu-backend-8fc1.onrender.com/api/v1/"
    const val PREFS_NAME = "bhoomisetu_prefs"
    const val DATABASE_NAME = "bhoomisetu_db"
    const val GOOGLE_WEB_CLIENT_ID = "639269568064-fj1oa7l342rjqdvulv3teu8kcoaa5urn.apps.googleusercontent.com"

    // Opened via Chrome Custom Tabs (CustomTabsLauncher), never an embedded WebView --
    // meebhoomi.ap.gov.in returns a real HTTP 403 for embedded WebView traffic.
    const val MEEBHOOMI_URL = "https://meebhoomi.ap.gov.in/"
    const val IGRS_EC_URL = "https://registration.ec.ap.gov.in/ecSearch/EncumbranceSearch"

    val AP_DISTRICTS = listOf(
        "Alluri Sitharama Raju",
        "Anakapalli",
        "Ananthapuramu",
        "Annamayya",
        "Bapatla",
        "Chittoor",
        "Dr. B. R. Ambedkar Konaseema",
        "East Godavari",
        "Eluru",
        "Guntur",
        "Kakinada",
        "Krishna",
        "Kurnool",
        "Nandyal",
        "NTR",
        "Palnadu",
        "Parvathipuram Manyam",
        "Prakasam",
        "Srikakulam",
        "Sri Potti Sriramulu Nellore",
        "Sri Sathya Sai",
        "Tirupati",
        "Visakhapatnam",
        "Vizianagaram",
        "West Godavari",
        "YSR Kadapa"
    )
}
