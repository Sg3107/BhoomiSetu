package com.satya.bhoomisetu.domain.model

data class LegalDocumentItem(
    val id: String,
    val documentType: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedByRole: String,
    val uploadedAt: Long,
    val verificationStatus: String, // PENDING, VERIFIED, NEEDS_CHANGES, REJECTED
    val remarks: String
)

data class LegalAdvisorRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String?, // Redacted if PENDING unless owner
    val advisorId: String,
    val advisorName: String,
    val advisorPhone: String?, // Redacted if PENDING unless owner
    val advisorEnrollment: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val propertyTitle: String,
    val description: String,
    val status: String, // PENDING, ACCEPTED, REJECTED, VERIFIED, NEEDS_CHANGES, COMPLETED
    val verificationSummary: String,
    val documents: List<LegalDocumentItem>,
    val createdAt: Long,
    val acceptedAt: Long?,
    val updatedAt: Long
)

object LandVerificationDocTypes {
    val REQUIRED_DOCS = listOf(
        DocTypeInfo(
            code = "SALE_DEED",
            title = "Sale Deed / Title Deed (Link Documents)",
            description = "Parent registered deeds establishing continuous ownership chain for past 30 years"
        ),
        DocTypeInfo(
            code = "PATTADAR_PASSBOOK",
            title = "Pattadar Passbook / 1B (ROR)",
            description = "Official revenue record of rights certifying land ownership in Andhra Pradesh"
        ),
        DocTypeInfo(
            code = "ADANGAL",
            title = "Adangal / Pahani Extract",
            description = "Current cultivation details, crop, and government/assigned land status classification"
        ),
        DocTypeInfo(
            code = "ENCUMBRANCE_CERTIFICATE",
            title = "Encumbrance Certificate (EC Form 15/16)",
            description = "Sub-Registrar record proving land is free from bank mortgages, court charges or prior sale"
        ),
        DocTypeInfo(
            code = "MUTATION_ORDER",
            title = "Mutation Proceedings / Order",
            description = "Tahsildar's order acknowledging transfer of title in revenue records"
        ),
        DocTypeInfo(
            code = "LAYOUT_PLAN",
            title = "Survey Sketch (FMB) / Layout Approval",
            description = "Field Measurement Book sketch showing land boundaries, subdivisions, and roads"
        ),
        DocTypeInfo(
            code = "TAX_RECEIPT",
            title = "Property Tax / Land Revenue Receipt",
            description = "Latest revenue payment receipts confirming active ownership standing"
        ),
        DocTypeInfo(
            code = "OTHER",
            title = "Other Legal Document",
            description = "Any power of attorney, legal heir certificate, or partition deed"
        )
    )
}

data class DocTypeInfo(
    val code: String,
    val title: String,
    val description: String
)
