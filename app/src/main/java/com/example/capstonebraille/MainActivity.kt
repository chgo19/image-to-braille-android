package com.example.capstonebraille

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import org.w3c.dom.Text
import java.io.IOException
import java.lang.Exception

const val TEXT_MESSAGE = "com.example.capstonebraille.TEXT"

const val ENGLISH = " A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)=\n"
const val BRAILLE = "⠀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿\n"
val MAP = ENGLISH.zip(BRAILLE).toMap()

class MainActivity : AppCompatActivity() {

    private var _selectIntent = 101
    private lateinit var imageViewMain: ImageView
    private lateinit var imageViewMainUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewMain = findViewById(R.id.imageView_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == _selectIntent) {
            imageViewMainUri = data?.data!!
            imageViewMain.setImageURI(imageViewMainUri)
        }
    }

    fun selectImage(view: View) {
        val selectImageIntent = Intent(Intent.ACTION_PICK)
        selectImageIntent.type = "image/*"
        startActivityForResult(selectImageIntent, _selectIntent)
    }

    fun captureImage(view: View) {}

    fun detectText(view: View) {
        showToast(getString(R.string.text_detection_start))
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, imageViewMainUri)

            val recognizer = TextRecognition.getClient()
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    // Task completed successfully
                    showToast(getString(R.string.text_detection_successful))
                    showDetectTextResult(text)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    showToast(getString(R.string.text_detect_error))
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            showToast(getString(R.string.text_detect_error))
            e.printStackTrace()
        }
    }

    private fun showDetectTextResult(result: com.google.mlkit.vision.text.Text) {
        val textResultIntent = Intent(this, DisplayTextResultActivity::class.java).apply {
            putExtra(TEXT_MESSAGE, result.text)
        }
        startActivity(textResultIntent)
    }

    fun detectObjects(view: View) {}

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}