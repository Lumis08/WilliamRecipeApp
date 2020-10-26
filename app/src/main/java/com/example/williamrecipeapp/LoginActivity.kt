package com.example.williamrecipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.williamrecipeapp.model.UserSetting
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var userSetting: UserSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Action Bar
        supportActionBar?.title = getString(R.string.login)

        // Share Preference
        userSetting = UserSetting(this)

        buttonLogin.setOnClickListener {
            if (editTextEmail.length() == 0) {
                editTextEmail.error = "Please enter the email!"
            } else if (editTextPassword.length() == 0) {
                editTextPassword.error = "Please enter the password!"
            } else {
                login()
            }
        }
    }

    private fun login() {

        val email = editTextEmail.text.toString()
        val pass = editTextPassword.text.toString()

        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call = apiInterface.login(email, pass)

        call.enqueue(object : Callback<HashMap<String, String>> {
            override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Failed to login!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
            }

            override fun onResponse(call: Call<HashMap<String, String>>,
                                    response: Response<HashMap<String, String>>) {
                val success = response.body()?.get("success") ?: "0"
                if (success == "1") {
                    val userId = response.body()?.get("userID") ?: ""
                    userSetting.login(userId)
                    val intentMain = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intentMain)
                } else {
                    val msg = response.body()?.get("message") ?: "Wrong email or password!"
                    editTextEmail.error = msg
                    editTextPassword.error = msg
                }
            }

        })

    }

}