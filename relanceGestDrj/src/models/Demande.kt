package models


import java.util.*


data class Demande(

        var id : Long?=null,
        var raisonSocial: String?=null,
        var objetContrat: String?=null,
        var dateEntreeEnVigueur: Date?=null,
        var dateFinContrat: Date?=null,
        var dateSouscription: Date?=null,
        var preavis:String? = null,
        var modalityRenewal: String?=null,
        var monthlyAmountPrestation: String?=null,
        var typeContratId:Long? = null,
        var user_id: Long? = null
)


