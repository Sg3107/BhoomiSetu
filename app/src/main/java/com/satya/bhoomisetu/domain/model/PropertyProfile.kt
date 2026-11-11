package com.satya.bhoomisetu.domain.model

/** Which Meebhoomi document this property is tracked against. */
enum class RecordType { ADANGAL, ROR_1B }

/**
 * A property the user is keeping track of. Every field here is typed in by
 * the user themselves -- this is organizational metadata, never data fetched
 * or scraped from a government portal. That's a deliberate design choice:
 * meebhoomi.ap.gov.in disallows automated access in its robots.txt and
 * actively blocks embedded WebView traffic with a 403, confirmed against the
 * live site. This app helps the user organize and navigate to their own
 * property lookups; it does not reproduce the government's data itself.
 */
data class PropertyProfile(
    val id: String,
    val ownerId: String,
    val nickname: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val recordType: RecordType,
    val createdAt: Long
)

/** What kind of check or entry a PropertyNote records. */
enum class PropertyNoteType { MEEBHOOMI_CHECK, EC_CHECK, GENERAL }

/**
 * A note the user added about a property -- e.g. "Checked Adangal, pattadar
 * name matches seller" after visiting the real site themselves. This is the
 * user's own record-keeping, not extracted government data.
 */
data class PropertyNote(
    val id: String,
    val propertyId: String,
    val type: PropertyNoteType,
    val text: String,
    val createdAt: Long
)
