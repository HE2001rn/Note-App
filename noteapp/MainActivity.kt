package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NotesDatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var googleSignInClient: GoogleSignInClient
    private var driveService: Drive? = null

    companion object {
        private const val SIGN_IN_REQUEST_CODE = 1001
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NotesDatabaseHelper(this)
        notesAdapter = NotesAdapter(db.getAllNotes(), this)

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        binding.signoutButton.setOnClickListener {
            signOut()
        }

        binding.uploadButton.setOnClickListener {
            signInAndUploadNotes()
        }
    }

    override fun onResume() {
        super.onResume()
        notesAdapter.refreshData(db.getAllNotes())
    }

    private fun signOut() {
        finishAffinity() // Close all activities and exit the application
    }

    private fun signInAndUploadNotes() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && !account.isExpired) {
            uploadNotesToDrive(account)
        } else {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    uploadNotesToDrive(account)
                } else {
                    Log.e(TAG, "Account is null after sign-in.")
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Sign-in failed", e)
                Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadNotesToDrive(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

        driveService = Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName("NoteApp")
            .build()

        try {
            val notes = db.getAllNotes()
            val notesFile = File(filesDir, "notes.txt")
            val writer = FileWriter(notesFile)
            for (note in notes) {
                writer.write(note.toString() + "\n")
            }
            writer.close()

            val metadata = com.google.api.services.drive.model.File()
                .setName("notes.txt")
                .setMimeType("text/plain")

            val fileContent = FileContent("text/plain", notesFile)
            driveService?.files()?.create(metadata, fileContent)?.execute()
            Toast.makeText(this, "Notes uploaded to Google Drive", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Notes uploaded to Google Drive successfully.")
        } catch (e: UserRecoverableAuthIOException) {
            Log.e(TAG, "UserRecoverableAuthIOException occurred", e)
            startActivityForResult(e.intent, SIGN_IN_REQUEST_CODE)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to upload notes due to IOException", e)
            Toast.makeText(this, "Failed to upload notes due to an IO error.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error occurred", e)
            Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show()
        }
    }
}
