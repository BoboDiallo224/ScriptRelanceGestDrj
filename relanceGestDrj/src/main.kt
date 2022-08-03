import kotliquery.queryOf
import kotliquery.sessionOf
import models.Demande
import models.Mail
import models.TypeContrat
import models.User
import vendor.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


fun main(args: Array<String>) {
    /*
    * Session de connexion à la base de donnée
    * */
    val session = sessionOf("jdbc:mysql://10.173.84.77/drj", "user_devops", "orange")

    //val date = LocalDateTime.now()
    val ca = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    val newDate:String = dateFormat.format(ca.time)

    val date:Date = dateFormat.parse(newDate)

    val listMail = ArrayList<String>()

    val demandes = session.list(queryOf(getDemandeNonPrisesEnCharge,0)) {

        Demande(id = it.long("id"),
                raisonSocial = it.string("raison_social"),
                objetContrat = it.string("objet_contrat"),
                dateSouscription = it.sqlDate("date_souscription"),
                modalityRenewal = it.string("modality_renewal"),
                monthlyAmountPrestation = it.string("monthly_amount_prestation"),
                typeContratId = it.long("type_contrat_id"),
                user_id = it.long("user_id"))
    }

    demandes.forEach {

        val diff = (date.time - it.dateSouscription!!.time) / (1000 * 60 * 60 * 24)

        if (diff >= 2){

            //Recuperation de l'utilisateur qui a souscrit a la demande
            val  divisionId:Long? = getUserSubscribe(it.user_id!!)!!.division_id

            //Recuperation de la division de l'utilisateur qui a souscrit a la demande
            val division = getUserDivision(divisionId!!)

            //Recuperation du type du contrat de la demande
            val typeContrat:TypeContrat = getTypeConttratById(it.typeContratId!!)

            //Recuperation des users charger d'elaborer les contrat
            val users = getUsersByTypeConttratById(it.typeContratId!!)

            users.forEach {

                listMail.add(it.email!!)

                val mail = Mail("Alerte Prise en Charge", "Bonjour,<br> Vous avez une demande "+typeContrat.libelle+" de la par de " +division+
                        " non prise en charge qui date de "+diff+" jours. <br> Merci de prendre en charge. <br> Lien http://10.173.5.29/GestDrj.", listMail)
                send(mail)

            }
            listMail.clear()
        }

    }
    //if (!payments.isEmpty()) generateReporting(payments)

    //Relance expiration de contrat en fonction du preavis
    var preavis:Long? = null

    val demandesPreavis = session.list(queryOf(getDemandeBaseOnPreavis)) {

        Demande(id = it.long("id"),
                raisonSocial = it.string("raison_social"),
                objetContrat = it.string("objet_contrat"),
                dateEntreeEnVigueur = it.sqlDateOrNull("date_entree_en_vigueur"),
                dateFinContrat = it.sqlDateOrNull("date_fin_contrat"),
                dateSouscription = it.sqlDateOrNull("date_souscription"),
                modalityRenewal = it.string("modality_renewal"),
                monthlyAmountPrestation = it.string("monthly_amount_prestation"),
                typeContratId = it.long("type_contrat_id"),
                user_id = it.long("user_id"))
    }

    demandesPreavis.forEach {

        //println("id "+it.id)
        val diffPreavis = (it.dateFinContrat!!.month - it.dateEntreeEnVigueur!!.month)

        val tempsRestant:Int; val mounthOrDay:String

          if (diffPreavis == 0){
            tempsRestant = ((it.dateFinContrat!!.time - it.dateEntreeEnVigueur!!.time) / (1000 * 60 * 60 * 24)).toInt()
            mounthOrDay = "Jours"
        }
        else{
            tempsRestant = (it.dateFinContrat!!.month - it.dateEntreeEnVigueur!!.month)
            mounthOrDay = "Mois"
        }

        //Recuperation du Preavis
        session.list(queryOf(getDemandePreavis, it.id)){

            preavis = it.string("preavis").toLong()
        }

        if (diffPreavis <= preavis!! ){

            //Recuperation de la division de l'utilisateur qui a souscrit a la demande
            val  divisionId:Long? = getUserSubscribe(it.user_id!!)!!.division_id

            //Recuperation de l'email de l'utilisateur qui a souscrit a la demande
            val  emailUser:String? = getUserSubscribe(it.user_id!!)!!.email

            //Recuperation de la division de l'utilisateur qui a souscrit a la demande
            val division = getUserDivision(divisionId!!)

            //Recuperation du type du contrat de la demande
            val typeContrat:TypeContrat = getTypeConttratById(it.typeContratId!!)

            //Recuperation des users charger d'elaborer les contrat
            val users = getUsersByTypeConttratById(it.typeContratId!!)

            users.forEach {

                listMail.add(it.email!!)
                listMail.add(emailUser!!)

                val mail = Mail("Alerte Preavis Contrat", "Bonjour,<br> Le contrat "+typeContrat.libelle+" de la par de " +division+
                                " arrive à échéance dans "+tempsRestant+" "+mounthOrDay+". <br> Merci de renouveler. <br> Lien http://10.173.5.29/GestDrj.", listMail)

                send(mail)

            }

            listMail.clear()

        }
    }

}

