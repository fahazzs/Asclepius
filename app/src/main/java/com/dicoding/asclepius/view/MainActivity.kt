package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            galleryButton.setOnClickListener{ startGallery() }
            analyzeButton.setOnClickListener{
                currentImageUri?.let {
                    analyzeImage(it)
                } ?: run {
                    showToast(getString(R.string.empty_image))
                }
            }
        }
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){ uri: Uri? ->
        if (uri != null){
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        currentImageUri?.let {
            Log.d("Image Uri", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(uri: Uri) {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        val imageClassifierHelper = ImageClassifierHelper(
            0.1f,
            3,
            "cancer_classification.tflite",
            this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener{
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResult(results: List<Classifications>?, inferenceTime: Long) {
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra(ResultActivity.EXTRA_IMG_URI, uri.toString())
                    intent.putExtra(ResultActivity.EXTRA_RESULT, results?.toTypedArray())
                    startActivity(intent)
                    moveToResult()
                }

            }
        )
        imageClassifierHelper.classifyStaticImage(uri)
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}