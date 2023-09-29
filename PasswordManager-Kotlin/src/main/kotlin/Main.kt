import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

fun main() {
    // Secret key for AES encryption
    val secretKey = "0123456789abcdef" // 128-bit key

    // Define the file path
    val filePath = "src/main/kotlin/data.json"

    // Read the existing JSON data from the file or initialize an empty array if the file doesn't exist
    val jsonArray = readJsonArrayFromFile(filePath)

    // Initializing variable action
    var action = "0"

    // List of options
    val options = listOf("1","2","3","4")

    // Input of the master password
    print("Enter master password to grant access: ")
    val masterPassword = readln()

    // Use of "when" as an expression
    // Grants or denies access
    val access = when (masterPassword) {
        "PasswordManager123!" -> "granted"
        else -> "Denied"
    }

    // Displays menu of the program
    while (action != "4" && access == "granted") {

        println()
        println("1. New Password")
        println("2. See Password")
        println("3. Delete Password")
        println("4. Exit")
        println()
        print("Select Action: ")
        action = readln()

        // Use of "when" as a statement
        when (action) {
            //
            "1" -> {
                newPassword(secretKey,jsonArray, filePath)
                println("Password recorded")
            }
            // calling function to retrieve and decrypt a password
            "2" -> {
                val keyToFilter = enterID()
                retrievePassword(jsonArray, keyToFilter, secretKey)
            }
            // calling function that deletes passwords
            "3" -> {
                val keyToDelete = enterID()
                deletePaswword(filePath, keyToDelete)
            }
            // If inserted an invalid input
            !in options -> {
                println("Invalid Option")
            }
        }
    }
    println()

    // goodbye message
    if (access == "granted") {
        println("Have a nice day")
    } else {
        println("Access Denied")
    }
}

// Read the existing JSON data from the file or initialize an empty array if the file doesn't exit
fun readJsonArrayFromFile(filePath: String): JSONArray {
    val fileContent = if (File(filePath).exists()) File(filePath).readText() else "[]"
    return JSONArray(fileContent)
}

// Function to write the JSON file
fun writeJsonStringToFile(filePath: String, jsonString: String) {
    File(filePath).writeText(jsonString)
}

// Function to add a new password
fun newPassword(secretKey: String, jsonArray: JSONArray, filePath: String) {
    val ID = enterID()
    val Pass = encryptAES(enterPassword(), secretKey)
    val newJsonObject = JSONObject()
    newJsonObject.put(ID, Pass)
    jsonArray.put(newJsonObject)
    val updatedJsonString = jsonArray.toString()
    writeJsonStringToFile(filePath, updatedJsonString)
}

// Function to retrieve and decrypt password based on ID
fun retrievePassword(jsonArray: JSONArray, keyToFilter: String, secretKey: String) {
    //println("Data retrieved from the JSON file for key '$keyToFilter':")
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        if (jsonObject.has(keyToFilter)) {
            val value = jsonObject.getString(keyToFilter)
            val decrypted = decryptAES(value, secretKey)
            println("$keyToFilter: $decrypted")
        }
    }
}

// Function to delete a saved password
fun deletePaswword(filePath: String, keyToDelete: String) {
    val jsonArray = readJsonArrayFromFile(filePath)
    val iterator = jsonArray.iterator()
    while (iterator.hasNext()) {
        val jsonObject = iterator.next() as JSONObject
        if (jsonObject.has(keyToDelete)) {
            iterator.remove()
        }
    }
    val updatedJsonString = jsonArray.toString()
    writeJsonStringToFile(filePath, updatedJsonString)
    println("Password deleted")
}

// Asking user for the ID
fun enterID(): String {
    print("Enter ID: ")
    val ID = readln()
    return ID
}

// Asking user for the password
fun enterPassword(): String {
    print("Enter Password: ")
    val ID = readln()
    return ID
}

// Encrypting input
fun encryptAES(data: String, secretKey: String): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
    val ivSpec = IvParameterSpec(secretKey.toByteArray())
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
    val encryptedBytes = cipher.doFinal(data.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

// Decrypting password
fun decryptAES(encryptedData: String, secretKey: String): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
    val ivSpec = IvParameterSpec(secretKey.toByteArray())
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
    val encryptedBytes = Base64.getDecoder().decode(encryptedData)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes)
}