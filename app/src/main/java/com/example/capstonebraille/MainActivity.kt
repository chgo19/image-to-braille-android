package com.example.capstonebraille

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import com.google.mlkit.vision.text.TextRecognition
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

    fun captureImage(view: View) {
        showToast(getString(R.string.not_yet_implemented))
    }

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

    fun detectObjects(view: View) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, imageViewMainUri)

            val localModel = LocalModel.Builder()
                .setAssetFilePath("lite-model_object_detection_mobile_object_labeler_v1_1.tflite")
                .build()

            val customObjectDetectorOptions =
                CustomObjectDetectorOptions.Builder(localModel)
                    .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .setClassificationConfidenceThreshold(0.5f)
                    .setMaxPerObjectLabelCount(3)
                    .build()

//            showToast("here")
            val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

            objectDetector.process(image)
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    showToast(e.message.toString())
                }
                .addOnSuccessListener { detectedObjects ->
                    // Task completed successfully
                    showDetectObjectsResult(detectedObjects)
                }
        } catch (e: Exception) {
            showToast(getString(R.string.error_detecting_objects))
            e.printStackTrace()
        }
    }

    private fun showDetectObjectsResult(results: MutableList<DetectedObject>) {
        val objectsString = StringBuilder()
        for (detectedObject in results) {
            for (label in detectedObject.labels) {
                val confidence = label.confidence
//                if (confidence > 0) {
                objectsString.append(label.text)
                objectsString.append("\n")
//                }
            }
        }
        showToast(objectsString.toString())
        val textResultIntent = Intent(this, DisplayObjectDetectionResult::class.java).apply {
            putExtra(TEXT_MESSAGE, objectsString.toString())
        }
        startActivity(textResultIntent)
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}