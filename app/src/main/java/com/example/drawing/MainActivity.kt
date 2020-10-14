package com.example.drawing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener  {

    var TAG = "MainACtivity"
    lateinit var drawingpad : DrawingPad
    private val defaultColor = 0
    lateinit var db : FirebaseFirestore
    lateinit var  storageref : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setting drawing display
        drawingpad = findViewById(R.id.drawingPadxml)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        //initializing important drawing variables
        drawingPadxml.initialise(displayMetrics)


        //initializing button options
        val undo = findViewById(R.id.undo) as Button
        undo.setOnClickListener(this)
        val eraser = findViewById(R.id.eraser) as Button
        eraser.setOnClickListener(this)
        val clear = findViewById(R.id.clear) as Button
        clear.setOnClickListener(this)
        val upload = findViewById(R.id.upload) as Button
        upload.setOnClickListener(this)
        val redo = findViewById(R.id.redo) as Button
        redo.setOnClickListener(this)

        val colored = findViewById(R.id.red) as Button
        colored.setOnClickListener(this)
        val colorblack = findViewById(R.id.black) as Button
        colorblack.setOnClickListener(this)
        val colorblue = findViewById(R.id.blue) as Button
        colorblue.setOnClickListener(this)
        val colorgreen = findViewById(R.id.green) as Button
        colorgreen.setOnClickListener(this)
        val coloryellow = findViewById(R.id.yellow) as Button
        coloryellow.setOnClickListener(this)

        val signOut = findViewById(R.id.signOut) as Button
        signOut.setOnClickListener(this)

    }

    override fun onClick(p0: View) {
       when(p0.id){
           R.id.undo ->
               drawingpad.undo()
           R.id.eraser ->
               drawingpad.setColor(Color.WHITE)
           R.id.clear ->
               drawingpad.clear()
           R.id.upload ->{
               //converting layout to bitmap
               val linearLayout = findViewById(R.id.drawingPadxml) as DrawingPad
               linearLayout.isDrawingCacheEnabled = true
               linearLayout.buildDrawingCache()
               val bitmap = Bitmap.createBitmap(linearLayout.drawingCache)
               upload(bitmap)
           }
           R.id.redo ->
               drawingpad.redo()
            R.id.red ->
                drawingpad.setColor(Color.RED)
           R.id.black ->
               drawingpad.setColor(Color.BLACK)
           R.id.blue ->
               drawingpad.setColor(Color.BLUE)
           R.id.green ->
               drawingpad.setColor(Color.GREEN)
           R.id.yellow ->
               drawingpad.setColor(Color.YELLOW)

           R.id.signOut ->{
               AuthUI.getInstance()
                   .signOut(this)
                   .addOnCompleteListener {
                       val intent = Intent (this, SignInActivity::class.java)
                        startActivity(intent)
                   }
           }

       }

    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    fun upload(bitmap: Bitmap) {

        //showing progressbar
        progressview.visibility = View.VISIBLE
        progressbar.visibility = View.VISIBLE
        progresstext.visibility = View.VISIBLE

        db = FirebaseFirestore.getInstance()
        storageref = FirebaseStorage.getInstance().getReference()
        val imagesRef: StorageReference? = storageref.child("Drawings")

        //bitmap tp uri
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val data: ByteArray = bytes.toByteArray()

        //storing image
        imagesRef!!.putBytes(data)
            .addOnFailureListener(){
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Somthing Went Wrong")
                builder.setMessage("Check net connection and Try Again")
                builder.setPositiveButton("Try Again")
                { dialog, which ->

                    //upload(bitmap)

                }
                builder.setNegativeButton("cancel")
                { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
            .addOnSuccessListener {
                val imgUri = it.metadata!!.reference!!.downloadUrl;
                imgUri.addOnSuccessListener {

                    var ImageLink = it.toString()
                    //Log.d(TAG, ""+ImageLink)

                    //storing Ulr to Firestore
                    val item = hashMapOf<String, Any>()
                    item["drawing"] = ImageLink.trim()
                    db.collection("Drawings").document("" + FirebaseAuth.getInstance().currentUser?.email)
                        .set(item)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Drawing Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            //hiding progressbar
                            progressview.visibility = View.INVISIBLE
                            progressbar.visibility = View.INVISIBLE
                            progresstext.visibility = View.INVISIBLE
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error Uploading Drawing", Toast.LENGTH_SHORT).show()
                        }


                }

            }


    }
}