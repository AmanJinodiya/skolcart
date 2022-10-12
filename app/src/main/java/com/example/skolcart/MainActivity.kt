package com.example.skolcart

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.skolcart.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var videouri : Uri
    lateinit var progressDialog: ProgressDialog

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var list : ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Video")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                dataSnapshot.children.forEach{
                    list.add(it.child("videolink").value.toString())

                }
                binding.pager.adapter = adapter(list)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.d("aman", "loadPost:onCancelled", databaseError.toException())
            }
        }
        reference.addValueEventListener(postListener)

        binding.addBtn.setOnClickListener{
            dialog()
        }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it!!.resultCode == Activity.RESULT_OK)
            {
                var ur = it!!.data!!.data!!
                videouri = ur
                progressDialog!!.setTitle("Uploading...")
                progressDialog!!.show()
                uploadvideo()

            }
        }


    }
    private fun choosevideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 5)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.data != null) {
           Log.d("aman","aman")
            videouri = data.data!!
            progressDialog!!.setTitle("Uploading...")
            progressDialog!!.show()
            uploadvideo()
        }
    }

    private fun getfiletype(videouri: Uri): String? {
        val r = contentResolver
        // get the file type ,in this case its mp4
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri))
    }

    private fun uploadvideo() {
        if (videouri != null) {
            // save the selected video in Firebase storage
            Log.d("aman","1")
            val reference = FirebaseStorage.getInstance()
                .getReference("Files/" + System.currentTimeMillis() + "." + getfiletype(videouri))
            reference.putFile(videouri).addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful());
                // get the link of video
                val downloadUri: String = uriTask.getResult().toString()
                val reference1: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("Video")
                val map: HashMap<String, String> = HashMap()
                map["videolink"] = downloadUri
                reference1.child("" + System.currentTimeMillis()).setValue(map)
                // Video uploaded successfully
                // Dismiss dialog
                progressDialog!!.dismiss()
                Toast.makeText(this@MainActivity, "Video Uploaded!!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e -> // Error, Image not uploaded
                progressDialog!!.dismiss()
                Toast.makeText(this@MainActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                // Progress Listener for loading
                // percentage on the dialog box
                // show the progress bar
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog!!.setMessage("Uploaded " + progress.toInt() + "%")
            }
        }
    }

    fun dialog()
    {
        val dialog = Dialog(this)

        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.option)

            var record = dialog.findViewById<Button>(R.id.record)
            var device = dialog.findViewById<Button>(R.id.storage)

            device.setOnClickListener{
                progressDialog = ProgressDialog(this);
                choosevideo()
            }
            record.setOnClickListener{

            }

            dialog.show()
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.window!!.attributes.windowAnimations = R.style.DilogAnimation
            dialog.window!!.setGravity(Gravity.CENTER)

        }
    }

    fun rec()
    {
        var intent  = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        activityResultLauncher.launch(intent)

    }


}