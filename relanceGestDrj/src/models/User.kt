package models

data class User(
        val id : Long?=null,
        var name: String?="",
        var username: String="",
        var password : String="",
        var email : String?="",
        var matricule : String?="",
        var isActivated : Boolean = false,
        var typeContrat_id: Long? = null,
        var division_id: Long? = null,
        var direction: String? = null,
        var typeImageSignature: String? = null,
        var contentImageSignature: String? = null
)





