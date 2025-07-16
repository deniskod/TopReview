import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "User signed up successfully")
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Error signing up")
                }
            }
    }


    fun signIn(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "User signed in successfully")
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Error signing in")
                }
            }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    onComplete(true, "Password reset email sent.")
                } else {

                    val errorMessage = task.exception?.localizedMessage ?: "Error sending reset email."


                    if (errorMessage.contains("There is no user record")) {
                        onComplete(false, "This email is not registered.")
                    } else {
                        onComplete(false, errorMessage)
                    }
                }
            }
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
