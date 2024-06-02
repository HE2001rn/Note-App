package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var db : UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }
        })

        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePassword()
            }
        })

        binding.confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword()
            }
        })

        binding.signupButton.setOnClickListener{

            if (checkAllField()) {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                db.addUser(email, password )
                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this,"Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun validateEmail(): Boolean {
        val email = binding.emailEditText.text.toString()
        return if (email.isEmpty()) {
            binding.emailEditText.error = "This is a required field"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Check email format"
            false
        } else {
            binding.emailEditText.error = null // Clear error once corrected
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.passwordEditText.text.toString()
        return if (password.isEmpty()) {
            binding.passwordEditText.error = "This is a required field"
            false
        } else if (password.length <= 5) {
            binding.passwordEditText.error = "Password should be at least 6 characters"
            false
        } else {
            binding.passwordEditText.error = null // Clear error once corrected
            true
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        return if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = "This is a required field"
            false
        } else if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            false
        } else {
            binding.confirmPasswordEditText.error = null // Clear error once corrected
            true
        }
    }

    private fun checkAllField(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        return isEmailValid && isPasswordValid && isConfirmPasswordValid
    }

}