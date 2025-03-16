import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Sign up user with email and password
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

    // Sign in user with email and password
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
                    // If the email is valid and the reset email was sent
                    onComplete(true, "Password reset email sent.")
                } else {
                    // Handle the error: Email may not be registered
                    val errorMessage = task.exception?.localizedMessage ?: "Error sending reset email."

                    // Check if the error is specifically related to the unregistered email
                    if (errorMessage.contains("There is no user record")) {
                        onComplete(false, "This email is not registered.")
                    } else {
                        onComplete(false, errorMessage) // Handle other errors
                    }
                }
            }
    }




    // Sign out the user
    fun signOut() {
        auth.signOut()
    }

    // Check if the user is already signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
