package com.satya.bhoomisetu.domain.model

data class WriterClientDocument(
    val id: String,
    val documentType: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedAt: Long
)

data class WriterDraftDocument(
    val id: String,
    val title: String,
    val originalFileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String,
    val notes: String,
    val uploadedAt: Long,
    val isFinal: Boolean
)

data class DocumentWriterRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String?,
    val writerId: String,
    val writerName: String,
    val writerPhone: String?,
    val writerLicense: String,
    val district: String,
    val mandal: String,
    val village: String,
    val surveyNumber: String,
    val deedType: String,
    val propertyTitle: String,
    val description: String,
    val status: String, // PENDING, ACCEPTED, REJECTED, DRAFT_UPLOADED, COMPLETED
    val clientDocuments: List<WriterClientDocument>,
    val draftDocuments: List<WriterDraftDocument>,
    val createdAt: Long,
    val acceptedAt: Long?,
    val completedAt: Long?,
    val updatedAt: Long
)

data class DocumentWriterSummary(
    val id: String,
    val name: String,
    val licenseNumber: String,
    val districts: List<String>,
    val yearsOfExperience: Int = 0
)

object DeedTypes {
    val ALL = listOf(
        DeedTypeInfo("SALE_DEED", "Sale Deed (సేల్ డీడ్ / విక్రయ దస్తావేజు)", "Conveys absolute ownership and title from seller to buyer for consideration"),
        DeedTypeInfo("GIFT_DEED", "Gift Deed (దాన విక్రయం / గిఫ్ట్ డీడ్)", "Voluntary transfer of property to blood relatives or family without financial consideration"),
        DeedTypeInfo("PARTITION_DEED", "Partition Deed (భాగ పరిష్కార దస్తావేజు)", "Division of ancestral or jointly-owned family properties among legal heirs"),
        DeedTypeInfo("MORTGAGE_DEED", "Mortgage Deed (తాకట్టు / తనఖా దస్తావేజు)", "Security deed for securing loans from lenders or private institutions"),
        DeedTypeInfo("WILL", "Will / Testament (వీలునామా)", "Legal declaration of the intention regarding distribution of assets after lifetime"),
        DeedTypeInfo("SALE_AGREEMENT", "Agreement of Sale (అగ్రిమెంట్ ఆఫ్ సేల్)", "Contract detailing agreed terms, advance payment, and timeline prior to final registration"),
        DeedTypeInfo("LEASE_DEED", "Lease / Rental Deed (కౌలు / లీజు ఒప్పందం)", "Long-term commercial or agricultural tenancy contract specifying rents and duration"),
        DeedTypeInfo("SETTLEMENT_DEED", "Settlement Deed (సెటిల్మెంట్ దస్తావేజు)", "Disposing or settling property in favor of dependents or family members"),
        DeedTypeInfo("RELEASE_DEED", "Release Deed (హక్కు విడుదల దస్తావేజు)", "Relinquishment of co-owner share in ancestral property"),
        DeedTypeInfo("OTHER", "Other Deed / Agreement", "Custom legal property document or general deed")
    )
}

data class DeedTypeInfo(
    val code: String,
    val title: String,
    val description: String
)

object WriterDocTypes {
    val REQUIRED_DOCS = listOf(
        DocTypeInfo(
            code = "LINK_DOC",
            title = "Link / Previous Sale Deeds",
            description = "Prior registered parent title deeds showing ownership history"
        ),
        DocTypeInfo(
            code = "PATTADAR_PASSBOOK",
            title = "Pattadar Passbook / 1B (ROR)",
            description = "Official revenue passbook verifying Khata number & ownership in AP"
        ),
        DocTypeInfo(
            code = "ADANGAL",
            title = "Adangal / Pahani",
            description = "Village record extract verifying nature of land and survey number"
        ),
        DocTypeInfo(
            code = "ENCUMBRANCE_CERTIFICATE",
            title = "Encumbrance Certificate (EC)",
            description = "Recent 30-year EC from AP Registration department"
        ),
        DocTypeInfo(
            code = "ID_PROOF",
            title = "Aadhaar / PAN of Parties",
            description = "Identity proofs of Buyers, Sellers, or Executants & Witnesses"
        ),
        DocTypeInfo(
            code = "LAYOUT_PLAN",
            title = "Layout Sketch / FMB Plan",
            description = "Field Measurement Book sketch showing boundary measurements & schedule"
        ),
        DocTypeInfo(
            code = "TAX_RECEIPT",
            title = "Property Tax / Water Tax Receipt",
            description = "Latest municipality/panchayat tax receipt"
        ),
        DocTypeInfo(
            code = "OTHER",
            title = "Other Supporting Document",
            description = "Power of attorney, family tree, death certificate, or affidavit"
        )
    )
}
