package com.satya.bhoomisetu.util

object ApLocationData {

    val DISTRICTS = Constants.AP_DISTRICTS

    private val MANDALS_BY_DISTRICT: Map<String, List<String>> = mapOf(
        "Alluri Sitharama Raju" to listOf(
            "Addateegala", "Ananthagiri", "Araku Valley", "Chintapalle", "Chintoor",
            "Devipatnam", "Dumbriguda", "G. Madugula", "Gangavaram", "Gudem Kotha Veedhi",
            "Hukumpeta", "Koyyuru", "Kunavaram", "Maredumilli", "Munchingi Puttu",
            "Nellipaka", "Paderu", "Pedabayalu", "Rajavommangi", "Rampachodavaram",
            "Vararamachandrapuram", "Y. Ramavaram"
        ),
        "Anakapalli" to listOf(
            "Anakapalli", "Atchutapuram", "Butchayyapeta", "Cheedikada", "Chodavaram",
            "Devarapalli", "Elamanchili", "Golugonda", "K. Kotapadu", "Kasimkota",
            "Kotauratla", "Madugula", "Makavarapalem", "Munagapaka", "Nakkapalli",
            "Narsipatnam", "Nathavaram", "Paravada", "Payakaraopeta", "Rambilli",
            "Ravikamatham", "Rolugunta", "S. Rayavaram", "Sabbavaram"
        ),
        "Ananthapuramu" to listOf(
            "Anantapur Rural", "Anantapur Urban", "Atmakur", "B.K. Samudram", "Beluguppa",
            "Bommanahal", "Brahmasamudram", "D. Hirehal", "Garladinne", "Gooty",
            "Gummagatta", "Guntakal", "Kalyandurg", "Kambadur", "Kanekal",
            "Kudair", "Kundurpi", "Narpala", "Pamidi", "Peddapappur",
            "Peddavadugur", "Putlur", "Raptadu", "Rayadurg", "Settur",
            "Singanamala", "Tadpatri", "Uravakonda", "Vajrakarur", "Vidapanakal",
            "Yadiki"
        ),
        "Annamayya" to listOf(
            "B. Kothakota", "Chinamandem", "Chitvel", "Galiveedu", "Gurramkonda",
            "K.V. Palle", "Kalakada", "Kalikiri", "Kurabalakota", "Lakkireddipalle",
            "Madanapalle", "Mulakalacheruvu", "Nandalur", "Nimmanapalle", "Obulavaripalle",
            "Pedda Thippasamudram", "Peddamandyam", "Penagaluru", "Pileru", "Pullampeta",
            "Railway Kodur", "Rajampet", "Ramasamudram", "Ramapuram", "Rayachoti",
            "Sambepalle", "T. Sundupalle", "Thamballapalle", "Valmikipuram", "Veeraballi"
        ),
        "Bapatla" to listOf(
            "Addanki", "Amruthalur", "Bapatla", "Bhattiprolu", "Cherukupalli",
            "Chinaganjam", "Chirala", "Inkollu", "Karamchedu", "Karlapalem",
            "Kolluru", "Martur", "Nagaram", "Nizampatnam", "Parchur",
            "Pittalavanipalem", "Repalle", "Tsundur", "Vemuru", "Vetapalem", "Yeddanapudi"
        ),
        "Chittoor" to listOf(
            "Baireddipalle", "Bangarupalem", "Chittoor Rural", "Chittoor Urban", "Chowdepalle",
            "Gangadhara Nellore", "Gangavaram", "Gudipala", "Gudupalle", "Irala",
            "Karvetinagar", "Kuppam", "Nagari", "Nindra", "Palamaner",
            "Palasamudram", "Peddapanjani", "Penumuru", "Pulicherla", "Punganur",
            "Puthalapattu", "Ramakuppam", "Rompicherla", "Santhipuram", "Sodam",
            "Somala", "Srirangarajapuram", "Thavanampalle", "Vedurukuppam", "Venkatagirikota",
            "Vijayapuram", "Yadamari"
        ),
        "Dr. B. R. Ambedkar Konaseema" to listOf(
            "Ainavilli", "Alamuru", "Allavaram", "Amalapuram", "Ambajipeta",
            "Atreyapuram", "I. Polavaram", "K. Gangavaram", "Katrenikona", "Kothapeta",
            "Malikipuram", "Mamidikuduru", "Mandapeta", "Mummidivaram", "P. Gannavaram",
            "Ramachandrapuram", "Ravulapalem", "Razole", "Sakhinetipalle", "Uppalaguptam"
        ),
        "East Godavari" to listOf(
            "Anaparthi", "Biccavolu", "Chagallu", "Devarapalle", "Gokavaram",
            "Gopalapuram", "Kadiam", "Kapileswarapuram", "Korukonda", "Kovvur",
            "Nallajerla", "Nidadavole", "Peravali", "Rajahmundry Rural", "Rajahmundry Urban",
            "Rajanagaram", "Rangampeta", "Rayavaram", "Seethanagaram", "Tallapudi",
            "Undrajavaram"
        ),
        "Eluru" to listOf(
            "Agiripalli", "Bhimadole", "Buttayagudem", "Chatrai", "Chintalapudi",
            "Denduluru", "Dwaraka Tirumala", "Eluru Rural", "Eluru Urban", "Jangareddygudem",
            "Jeelugu Milli", "Kaikalur", "Kalidindi", "Kamavarapukota", "Koyyalagudem",
            "Kukunoor", "Lingapalem", "Mandavalli", "Mudinepalli", "Musunuru",
            "Nidamarru", "Nuzvid", "Pedapadu", "Pedavegi", "Polavaram",
            "T. Narasapuram", "Unguturu", "Velairpadu"
        ),
        "Guntur" to listOf(
            "Chebrolu", "Duggirala", "Guntur East", "Guntur West", "Kakumanu",
            "Kollipara", "Mangalagiri", "Medikonduru", "Pedakakani", "Pedanandipadu",
            "Phirangipuram", "Ponnur", "Prathipadu", "Tadepalle", "Tadikonda",
            "Tenali", "Thullur", "Vatticherukuru"
        ),
        "Kakinada" to listOf(
            "Gandepalle", "Gollaprolu", "Jaggampeta", "Kajuluru", "Kakinada Rural",
            "Kakinada Urban", "Karapa", "Kirlampudi", "Kotananduru", "Pedapudi",
            "Peddapuram", "Pithapuram", "Prathipadu", "Rowthulapudi", "Samalkota",
            "Sankhavaram", "Tallarevu", "Thondangi", "Tuni", "U. Kothapalli", "Yeleswaram"
        ),
        "Krishna" to listOf(
            "Avanigadda", "Bantumilli", "Bapulapadu", "Challapalli", "Gannavaram",
            "Ghantasala", "Gudivada", "Gudlavalleru", "Guduru", "Kankipadu",
            "Koduru", "Kruthivennu", "Machilipatnam", "Mopidevi", "Movva",
            "Nagayalanka", "Nandivada", "Pamarru", "Pamidimukkala", "Pedana",
            "Pedaparupudi", "Penamaluru", "Thotlavalluru", "Unguturu", "Vuyyuru"
        ),
        "Kurnool" to listOf(
            "Adoni Rural", "Adoni Urban", "Alur", "Aspari", "C. Belagal",
            "Chippagiri", "Devanakonda", "Gonegandla", "Gudur", "Halaharvi",
            "Holagunda", "Kallur", "Kodumur", "Kosigi", "Kowthalam",
            "Krishnagiri", "Kurnool Rural", "Kurnool Urban", "Maddikera", "Mantralayam",
            "Nandavaram", "Orvakal", "Pattikonda", "Pedda Kadubur", "Tuggali",
            "Veldurthi", "Yemmiganur"
        ),
        "Nandyal" to listOf(
            "Allagadda", "Atmakur", "Banaganapalle", "Bandi Atmakur", "Bethamcherla",
            "Chagalamarri", "Dhone", "Dornipadu", "Gadivemula", "Gospadu",
            "Jupadu Bunglow", "Koilkuntla", "Kolimigundla", "Kothapalle", "Mahanandi",
            "Midthur", "Nandikotkur", "Nandyal Rural", "Nandyal Urban", "Owk",
            "Pagidyala", "Pamulapadu", "Panyam", "Peapully", "Rudravaram",
            "Sanjamala", "Sirvel", "Srisailam", "Uyyalawada", "Velgodu"
        ),
        "NTR" to listOf(
            "A. Konduru", "Chandarlapadu", "G. Konduru", "Gampalagudem", "Ibrahimpatnam",
            "Jaggayyapeta", "Kanchikacherla", "Mylavaram", "Nandigama", "Penuganchiprolu",
            "Reddigudem", "Tiruvuru", "Vatsavai", "Veerullapadu", "Vijayawada Central",
            "Vijayawada East", "Vijayawada North", "Vijayawada Rural", "Vijayawada Urban",
            "Vijayawada West", "Vissannapeta"
        ),
        "Palnadu" to listOf(
            "Amaravathi", "Atchampet", "Bellamkonda", "Bollapalli", "Chilakaluripet",
            "Dachepalli", "Durgi", "Edlapadu", "Gurazala", "Ipuru",
            "Karempudi", "Krosuru", "Machavaram", "Macherla", "Muppalla",
            "Nadendla", "Narasaraopet", "Nekarikallu", "Nuzendla", "Pedakurapadu",
            "Piduguralla", "Rajupalem", "Rentachintala", "Rompicherla", "Sattenapalli",
            "Savalyapuram", "Veldurthy", "Vinukonda"
        ),
        "Parvathipuram Manyam" to listOf(
            "Balijipeta", "Bhamini", "Garugubilli", "Gummalakshmipuram", "Jiyyammavalasa",
            "Komarada", "Kurupam", "Makkuva", "Pachipenta", "Palakonda",
            "Parvathipuram", "Salur", "Seethampeta", "Seethanagaram", "Veeraghattam"
        ),
        "Prakasam" to listOf(
            "Ardhaveedu", "Bestavaripeta", "C.S. Puram", "Chimakurthi", "Cumbum",
            "Darsi", "Donakonda", "Dornala", "Giddalur", "Hanumanthunipadu",
            "Kanigiri", "Komarolu", "Konakanamitla", "Kondapi", "Kothapatnam",
            "Kurichedu", "Maddipadu", "Markapur", "Marripudi", "Mundlamuru",
            "Naguluppalapadu", "Ongole", "P.C. Palli", "Pamuru", "Peddaraveedu",
            "Podili", "Ponnaluru", "Pullalacheruvu", "Racherla", "Santhanuthalapadu",
            "Singarayakonda", "Tallur", "Tanguturu", "Tarlupadu", "Tripuranthakam",
            "Veligandla", "Yerragondapalem", "Zarugumalli"
        ),
        "Srikakulam" to listOf(
            "Amadalavalasa", "Burja", "Etcherla", "Ganguvarisigadam", "Gara",
            "Hiramandalam", "Ichchapuram", "Jalumuru", "Kanchili", "Kaviti",
            "Kotabommali", "Kothuru", "L.N. Peta", "Laveru", "Mandasa",
            "Meliaputti", "Nandigam", "Narasannapeta", "Palasa", "Pathapatnam",
            "Polaki", "Ponduru", "Ranastalam", "Santhabommali", "Saravakota",
            "Sarubujjili", "Sompeta", "Srikakulam", "Tekkali", "Vajrapukotturu"
        ),
        "Sri Potti Sriramulu Nellore" to listOf(
            "Allur", "Ananthasagaram", "Anumasamudrampeta", "Atmakur", "Bogole",
            "Buchireddypalem", "Chejerla", "Dagadarthi", "Duttalur", "Gudluru",
            "Indukurpet", "Jaladanki", "Kaligiri", "Kaluvoya", "Kandukur",
            "Kavali", "Kodavalur", "Kondapuram", "Kovur", "Lingasamudram",
            "Manubolu", "Marripadu", "Muthukur", "Nellore Rural", "Nellore Urban",
            "Podalakur", "Rapur", "Sangam", "Seetharamapuram", "Sydapuram",
            "T.P. Gudur", "Udayagiri", "Ulavapadu", "Varikuntapadu", "Venkatachalam",
            "Vinjamur", "Voletivaripalem"
        ),
        "Sri Sathya Sai" to listOf(
            "Agali", "Amadaguru", "Amarapuram", "Battalapalli", "Bukkapatnam",
            "Chennekottapalli", "Chilamathur", "Dharmavaram", "Gandlapenta", "Gorantla",
            "Gudibanda", "Hindupur", "Kadiri", "Kanaganapalli", "Kothacheruvu",
            "Lepakshi", "Madakasira", "Mudigubba", "N.P. Kunta", "Nallacheruvu",
            "Nallamada", "O.D. Cheruvu", "Parigi", "Penukonda", "Puttaparthi",
            "Ramagiri", "Roddam", "Rolla", "Somandepalli", "Tadimarri",
            "Talupula", "Tanakal"
        ),
        "Tirupati" to listOf(
            "Balayapalli", "Buchinaidu Kandriga", "Chandragiri", "Chinnagottigallu", "Chittamur",
            "Dakkili", "Doravarisatram", "K.V.B. Puram", "Nagalapuram", "Naidupet",
            "Narayanavanam", "Ozili", "Pakala", "Pellakur", "Pichatur",
            "Puttur", "Ramachandrapuram", "Renigunta", "Satyavedu", "Srikalahasti",
            "Sullurpeta", "Tada", "Thottambedu", "Tirupati Rural", "Tirupati Urban",
            "Vadamalapeta", "Vakadu", "Varadaiahpalem", "Venkatagiri", "Yerpedu",
            "Yerravaripalem"
        ),
        "Visakhapatnam" to listOf(
            "Anandapuram", "Bheemunipatnam", "Gajuwaka", "Gopalapatnam", "Maharanipeta",
            "Mulagada", "Padmanabham", "Pedagantyada", "Pendurthi", "Seethammadhara",
            "Visakhapatnam Rural"
        ),
        "Vizianagaram" to listOf(
            "Badangi", "Bhogapuram", "Bobbili", "Bondapalle", "Cheepurupalli",
            "Dattirajeru", "Denkada", "Gajapathinagaram", "Gantyada", "Garividi",
            "Gurla", "Jami", "Kothavalasa", "Lakkavarapukota", "Mentada",
            "Merakamudidam", "Nellimarla", "Pusapatirega", "Rajam", "Ramabhadrapuram",
            "Regidi Amadalavalasa", "Santhakaviti", "Srungavarapukota", "Therlam", "Vangara",
            "Vepada", "Vizianagaram"
        ),
        "West Godavari" to listOf(
            "Achanta", "Akividu", "Attili", "Bhimavaram", "Ganapavaram",
            "Iragavaram", "Kalla", "Mogalthur", "Narasapuram", "Palacole",
            "Palacoderu", "Pentapadu", "Penugonda", "Penumantra", "Poduru",
            "Tadepalligudem", "Tanuku", "Undi", "Veeravasaram", "Yelamanchili"
        ),
        "YSR Kadapa" to listOf(
            "Atlur", "B. Kodur", "Badvel", "Brahmamgarimattam", "C.K. Dinne",
            "Chakrayapet", "Chapad", "Chennur", "Duvvur", "Gopavaram",
            "Jammalamadugu", "Kadapa", "Kalasapadu", "Kamalapuram", "Khajipeta",
            "Kondapuram", "Lingala", "Muddanur", "Mydukur", "Mylavaram",
            "Peddamudium", "Pendlimarri", "Porumamilla", "Proddatur", "Pulivendula",
            "Rajupalem", "S.A.K.N. (Kasinayana)", "Sidhout", "Simhadripuram", "Thondur",
            "Vallur", "Veerapunayunipalle", "Vempalli", "Vemula", "Vontimitta",
            "Yerraguntla"
        )
    )

    // Common aliases mapping to canonical district names
    private val DISTRICT_ALIASES: Map<String, String> = mapOf(
        "konaseema" to "Dr. B. R. Ambedkar Konaseema",
        "dr. b.r. ambedkar konaseema" to "Dr. B. R. Ambedkar Konaseema",
        "dr b r ambedkar konaseema" to "Dr. B. R. Ambedkar Konaseema",
        "ambedkar konaseema" to "Dr. B. R. Ambedkar Konaseema",
        "kadapa" to "YSR Kadapa",
        "ysr" to "YSR Kadapa",
        "ysr district" to "YSR Kadapa",
        "anantapur" to "Ananthapuramu",
        "ananthapur" to "Ananthapuramu",
        "nellore" to "Sri Potti Sriramulu Nellore",
        "spsr nellore" to "Sri Potti Sriramulu Nellore",
        "s.p.s.r. nellore" to "Sri Potti Sriramulu Nellore",
        "sathya sai" to "Sri Sathya Sai",
        "manyam" to "Parvathipuram Manyam",
        "asr" to "Alluri Sitharama Raju",
        "alluri" to "Alluri Sitharama Raju"
    )

    private val VILLAGES_BY_MANDAL: Map<String, List<String>> = mapOf(
        "Mangalagiri" to listOf(
            "Nowlur", "Atkur", "Bethapudi", "Chinakakani", "Nidamarru",
            "Kaza", "Yerrabalem", "Nutakki", "Kuragallu", "Mangalagiri Town", "Chinavadlapudi"
        ),
        "Vijayawada Rural" to listOf(
            "Gunadala", "Nunna", "Enikepadu", "Prasadampadu", "Ramavarappadu",
            "Ambapuram", "Jakkampudi", "Kotturu", "Payakapuram", "Gollapudi"
        ),
        "Bheemunipatnam" to listOf(
            "Bheemili", "Tagarapuvalasa", "Nerellavalasa", "Chepaluppada",
            "Kapuluppada", "Nagarampalem", "Majjavaram"
        ),
        "Tirupati Rural" to listOf(
            "Perur", "Avilala", "Tiruchanur", "Daminedu", "Vedantapuram",
            "Mangalam", "Padmavathipuram", "Padi", "Mallavaram"
        ),
        "Guntur East" to listOf(
            "Nallapadu", "Gorantla", "Ankireddypalem", "Pedapalakaluru", "Etukuru", "Budampadu"
        ),
        "Guntur West" to listOf(
            "Pothuru", "Reddypalem", "Agathavarappadu", "Peda Kakani", "Chowdavaram", "Gujjanagundla"
        ),
        "Tenali" to listOf(
            "Angalakuduru", "Nandivelugu", "Pinapadu", "Sangam Jagarlamudi", "Chinaravuru", "Pedaravuru"
        ),
        "Thullur" to listOf(
            "Velagapudi", "Mandadam", "Uddandarayanipalem", "Malkapuram", "Ainavolu",
            "Nelapadu", "Rayapudi", "Borupalem", "Sakhamuru", "Lingayapalem"
        ),
        "Gajuwaka" to listOf(
            "Gajuwaka", "Kurmannapalem", "Aganampudi", "Tungalam", "Vadlapudi", "Duvvada"
        ),
        "Bhimavaram" to listOf(
            "Bhimavaram Town", "Chinna Amiram", "Peda Amiram", "Rayalam", "Toderu", "Losari", "Vendra"
        ),
        "Kakinada Rural" to listOf(
            "Sarpavaram", "Ramanayyapeta", "Turangi", "Kovvada", "Valasapakala", "Indrapalem"
        ),
        "Rajahmundry Rural" to listOf(
            "Dowleswaram", "Hukumpeta", "Kolamuru", "Morampudi", "Torredu", "Rajanagaram", "Bommaru"
        ),
        "Nellore Rural" to listOf(
            "Allipuram", "Buja Buja Nellore", "Kallurpalle", "Kanuparthipadu", "Pottepalem"
        ),
        "Kurnool Urban" to listOf(
            "Joharapuram", "Mamidalapadu", "Ulchala", "Dinnedevarapadu", "Munagalapadu", "Pandipadu"
        ),
        "Kadapa" to listOf(
            "Akkayapalle", "Chinnachowk", "Chemmumiahpet", "Palempalle", "Utukur", "Putlampalli"
        ),
        "Anantapur Urban" to listOf(
            "Anantapur Town", "Kakkalapalle", "Papampeta", "Rudraampeta", "Alamuru"
        ),
        "Puttaparthi" to listOf(
            "Puttaparthi Town", "Bukkapatnam", "Kothacheruvu", "Enumulapalle", "Beedupalli"
        ),
        "Machilipatnam" to listOf(
            "Machilipatnam Urban", "Chilakalapudi", "Arisepalli", "Gokavaram", "Karakatta", "Tallapalem"
        ),
        "Ongole" to listOf(
            "Ongole Town", "Kothapatnam Road", "Pellur", "Muktinuthalapadu", "Annavarappadu", "Venkateswara Nagar"
        ),
        "Narasaraopet" to listOf(
            "Narasaraopet Town", "Kakani", "Ravipadu", "Kesannapalle", "Jonnalagadda", "Pamidimarru"
        )
    )

    val COMMON_SURVEY_NUMBERS = listOf(
        "101", "102/1", "102/2", "103", "124/1", "124/2",
        "89/1A", "89/1B", "89/2", "145/A", "210/1", "210/3", "342/2B"
    )

    fun getMandals(district: String): List<String> {
        val trimmed = district.trim()
        if (trimmed.isEmpty()) return emptyList()

        // 1. Direct match
        MANDALS_BY_DISTRICT[trimmed]?.let { return it }

        // 2. Case-insensitive match
        MANDALS_BY_DISTRICT.keys.find { it.equals(trimmed, ignoreCase = true) }?.let { matchedKey ->
            return MANDALS_BY_DISTRICT[matchedKey] ?: emptyList()
        }

        // 3. Alias match
        val canonical = DISTRICT_ALIASES[trimmed.lowercase()]
        if (canonical != null) {
            MANDALS_BY_DISTRICT[canonical]?.let { return it }
        }

        // 4. Substring / partial match (e.g. "Konaseema" inside "Dr. B. R. Ambedkar Konaseema")
        MANDALS_BY_DISTRICT.keys.find { it.contains(trimmed, ignoreCase = true) || trimmed.contains(it, ignoreCase = true) }?.let { matchedKey ->
            return MANDALS_BY_DISTRICT[matchedKey] ?: emptyList()
        }

        return emptyList()
    }

    fun getVillages(mandal: String): List<String> {
        val trimmed = mandal.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Exact or case-insensitive match
        VILLAGES_BY_MANDAL.keys.find { it.equals(trimmed, ignoreCase = true) }?.let { key ->
            return VILLAGES_BY_MANDAL[key] ?: emptyList()
        }

        // Partial match
        VILLAGES_BY_MANDAL.keys.find { it.contains(trimmed, ignoreCase = true) || trimmed.contains(it, ignoreCase = true) }?.let { key ->
            return VILLAGES_BY_MANDAL[key] ?: emptyList()
        }

        return emptyList()
    }
}
