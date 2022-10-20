package com.example.parstagram

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.parse.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //set description of post
        //button to launch camera to take pic
        //imageview to show pic user has taken
        //a button to save and send post to parse server



        // Returns the File for a photo stored on disk given the fileName
        fun getPhotoFileUri(fileName: String): File {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            val mediaStorageDir =
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory")
            }

            // Return the file target for the photo based on filename
            return File(mediaStorageDir.path + File.separator + fileName)
        }

         fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(requestCode==CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
                if(resultCode== RESULT_OK){
                    val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                    val ivPreview: ImageView = findViewById(R.id.imageView)
                    findViewById<ImageView>(R.id.imageView).setImageBitmap(takenImage)
                }
                else{
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_LONG ).show()
                }
            }
        }

        fun onLaunchCamera() {
            // create Intent to take a picture and return control to the calling application
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Create a File reference for future access
            photoFile = getPhotoFileUri(photoFileName)

            // wrap File object into a content provider
            // required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            if (photoFile != null) {
                val fileProvider: Uri =
                    FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile!!)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.
                if (intent.resolveActivity(packageManager) != null) {
                    // Start the image capture intent to take photo
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
                }
            }
        }
        queryPosts()
        findViewById<Button>(R.id.takePicButton).setOnClickListener {
            //Launch camera to take pic
            onLaunchCamera()
        }
        findViewById<Button>(R.id.submitPicButton).setOnClickListener {
        //send post to server w/o image
            val description = findViewById<EditText>(R.id.textDescription).text.toString()
            val user = ParseUser.getCurrentUser()
            if(photoFile!=null){
                submitPost(description, user, photoFile!!)
            }
            else{
                Log.e(TAG, "Error Loading Picture")
                Toast.makeText(this, "Couldn't Submit Photo", Toast.LENGTH_SHORT).show()
            }

        //send pic to server
        }
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logOutUser()
        }
        //query for all posts in server

    }

    private fun submitPost(description: String, user:ParseUser, file:File) {
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground{ exception ->
            if(exception!=null){
                Log.e(TAG, "Error while saving post")
                exception.printStackTrace()
                Toast.makeText(this, "There was an error while saving your post.", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.i(TAG, "Successfully Saved Post")
                //TODO: Reset description text field imageview to be empty
            }
        }
    }

    private fun queryPosts() {
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        query.include((Post.KEY_USER))
        query.findInBackground(object: FindCallback<Post>{
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if(e!=null){
                    Log.e(TAG, "Error fetching posts")
                }
                else{
                    if(posts!=null){
                        for(post in posts){
                            Log.i(TAG, "Post "+post.getDescription()+" , username: "+post.getUser()?.username)

                        }
                    }
                }
            }

        })
    }
    companion object{
        const val TAG = "MainAcitivity"
    }

    private fun logOutUser(){
        ParseUser.logOut()
        val currentUser = ParseUser.getCurrentUser() // this will now be null

        Toast.makeText(this, "Successfully Logged Out", Toast.LENGTH_SHORT).show()
        goToLoginActivity()
    }

    private fun goToLoginActivity(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}