package vendor

import kotliquery.queryOf
import kotliquery.sessionOf
import models.Mail
import models.TypeContrat
import models.User
import java.util.*
import javax.mail.*
import javax.mail.internet.*

//---------------------------------constance Dev --------------------------------
const val tableName = "demandes"
const val tableNameTypeContrat = "type_contrats"
const val tableNameUser = "users"
const val tableNameDivision = "divisions"

//----------------------------------queries---------------------------------
const val getDemandeNonPrisesEnCharge = """select * from $tableName where status_pris_en_charge = ? """
const val getTypeContratById = """select * from $tableNameTypeContrat where id = ?"""
const val getUserByTypeContrat = """select * from $tableNameUser where type_contrat_id = ?"""
const val getUser = """select * from $tableNameUser where id = ?"""
const val getDivisionByUser = """select * from $tableNameDivision where id = ?"""
const val getDemandeBaseOnPreavis = """select * from $tableName where contrat_resilier = false"""
const val getDemandePreavis = """select preavis from $tableName where id = ?"""

private val COLUMNs = arrayOf<String>("Date d’opération", "Numéro de téléphone", "Compte client (MG)", "Montant paiement", "N° Transaction")
//---------------------------------constance Dev --------------------------------=

//------------------------------functions--------------------------------
val session = sessionOf("jdbc:mysql://10.173.84.77/drj", "user_devops", "orange")

    //Recuperation de l'utilisateur qui a souscrit a la demande
    fun getUserSubscribe(idUser:Long):User?{

        //var divisionId:Long? = null
        return session.single(queryOf(getUser,idUser)){

            User(
                email = it.string("email"),
                division_id = it.long("division_id")
            )
    }

    }

    //Recuperation de la division de l'utilisateur qui a souscrit a la demande
    fun getUserDivision(divisionId:Long):String{

        var division:String = ""

        session.list(queryOf(getDivisionByUser,divisionId)){

            division = it.string("libelle")
        }
        return division
    }

    //Recuperation du type du Contrat demande
    fun getTypeConttratById(idTypeContrat:Long):TypeContrat{

        val typeContrat = TypeContrat()

        session.list(queryOf(getTypeContratById,idTypeContrat)){

            typeContrat.libelle =  it.string("libelle")
        }
        return typeContrat
    }

    //Recuperation des users charger d'elaborer le contrat
    fun getUsersByTypeConttratById(idTypeContrat:Long):List<User>{

      return session.list(queryOf(getUserByTypeContrat, idTypeContrat)){

            User(
                    name = it.string("name"),
                    email = it.string("email")
            )
        }
    }

    fun send(mail: Mail) {

    // Send mail without authentication - Work only on server allow access to SMTP
    // Begin
    val props = Properties()
    props["mail.smtp.host"] = "10.100.56.56"
    props["mail.from"] = "GestionContrat@orange-sonatel.com"
    val session = Session.getInstance(props, null)
    // End

    // Send mail with authentication
    // Begin
    /*val sender:String = "mamadoubobo.diallo3@orange-sonatel.com"
    val host:String = "10.100.56.56"
    val port:String = "587"
    val username:String = "stg_diallo87"
    val password:String = "Th%35708632"

    val props =  Properties()
    props.put("mail.smtp.host", host)
    props.put("mail.from", sender)
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", port)

    val session = Session.getInstance(props, object : javax.mail.Authenticator() {
       override fun getPasswordAuthentication(): PasswordAuthentication {
           return PasswordAuthentication(username, password)
       }
    })*/
    // End

    // Creation and send mail
    val msg = MimeMessage(session)

    if (mail.recipientTo != null)

        for (to in mail.recipientTo!!) {
            try {
                msg.addRecipient(Message.RecipientType.TO, InternetAddress(to))
            } catch (e: AddressException) {
                e.printStackTrace()
            } catch (e: MessagingException) {
                e.printStackTrace()
            }

        }

    if (mail.recipientCc != null)

        for (cc in mail.recipientCc!!) {
            try {
                msg.addRecipient(Message.RecipientType.CC, InternetAddress(cc))
            } catch (e: AddressException) {
                e.printStackTrace()
            } catch (e: MessagingException) {
                e.printStackTrace()
            }

        }

    try {
        msg.subject = mail.subject
        msg.sentDate = Date()
        msg.setContent(mail.message, "text/html; charset=utf-8")
        Transport.send(msg)
    } catch (mex: MessagingException) {
        System.err.println(mex.message)
    }

    }

