package com.example.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mImageButtonCurrentPaint: ImageButton? = null

    private val myCoroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawingView.setSizeForBrush(20f)

        mImageButtonCurrentPaint = binding.llPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        binding.ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        binding.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)

            } else {
                requestStoragePermission()
            }
        }

        binding.ibUndo.setOnClickListener {
            binding.drawingView.onClickUndo()
        }

        binding.ibSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                myCoroutineScope.launch {
                    showProgressDialog()
                    bitmapAsyncTask(getBitmapFromView(binding.flDrawingViewContainer))
                    cancelProgressDialog()
                }
            } else {
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        binding.ivBackground.visibility = View.VISIBLE
                        binding.ivBackground.setImageURI(data.data)
                    } else {
                        Toast.makeText(this@MainActivity, "Error in parsing the image of its corrupted.",
                                       Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        var bindingDialog = DialogBrushSizeBinding.inflate(layoutInflater)

        brushDialog.setContentView(bindingDialog.root)
        brushDialog.setTitle("Brush size:")

        val smallBtn = bindingDialog.ibSmallBrush
        smallBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(10f)
            brushDialog.dismiss()
        }

        val mediumBtn = bindingDialog.ibMediumBrush
        mediumBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(20f)
            brushDialog.dismiss()
        }

        val largeBtn = bindingDialog.ibLargeBrush
        largeBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(30f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)

            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = view
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {
            Toast.makeText(this, "Need permission to add a Background.", Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)  {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permission granted, now you can read the storage files.",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Oops you just denied the permission.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

    private suspend fun bitmapAsyncTask(mBitmap: Bitmap) {
        var result = withContext(Dispatchers.IO) {
            var result = ""

            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString()
                                 + File.separator + "KidDrawingApp_"
                                 + System.currentTimeMillis() / 1000 + ".png")

                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return@withContext result
        }
        withContext(Dispatchers.Main) {
            if (result.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "File saved successfully :$result", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Something went wrong while saving the file.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showProgressDialog() {
        mProgressDialog = Dialog(this@MainActivity)
        mProgressDialog.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog.show()
    }

    private fun cancelProgressDialog() {
        mProgressDialog.dismiss()
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2

    }
}