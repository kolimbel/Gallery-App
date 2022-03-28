package pwr.konrad.galleryapp

//import pl.maty.galleryapp.adapter.RecyclerAdapter
//import pl.maty.galleryapp.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pwr.konrad.galleryapp.R
import pwr.konrad.galleryapp.Image
import pwr.konrad.galleryapp.ImageAdapter


class MainActivity : AppCompatActivity() {
    private var imageRecyclerView: RecyclerView? = null
    private var imagesList: ArrayList<Image>? = null

    var allImagesUri: Uri? = null
    var projectionArray: Array<String>? = null
    var cursorMain: Cursor? = null

    var imagesCounter = 0
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageRecyclerView = findViewById(R.id.rvImages)

        imageRecyclerView?.layoutManager = GridLayoutManager(this, 2)

        //Storage Permissions
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }

        allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        projectionArray = arrayOf(
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN
        )
        cursorMain = this@MainActivity.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projectionArray,
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        )


        imagesList = ArrayList()
        if (imagesList!!.isEmpty()) {
            loadImagesPackage()
            imageRecyclerView?.adapter = ImageAdapter(this, imagesList!!)
        }

        initScrollListener()
    }

    private fun initScrollListener() {

        imageRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                currentItems = linearLayoutManager!!.childCount
                totalItems = linearLayoutManager!!.itemCount
                scrollOutItems = linearLayoutManager!!.findFirstVisibleItemPosition()

                if (currentItems + scrollOutItems == totalItems) {
                    loadImagesPackage()
                }
            }
        }
        )
    }


    private fun loadImagesPackage() {
        var i = 0
        try {
            if (imagesCounter == 0) {
                cursorMain!!.moveToFirst()
            } else {
                cursorMain!!.moveToPosition(imagesCounter)
            }
            while (cursorMain!!.moveToNext() && i < 15) {
                val image = Image()
                image.imagePath =
                    cursorMain?.getString(cursorMain!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                image.imageName =
                    cursorMain?.getString(cursorMain!!.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                imagesList?.add(image)

                imageRecyclerView?.post(Runnable {
                    kotlin.run { imageRecyclerView?.adapter?.notifyItemInserted(imagesList!!.size - 1) }
                })
                i++
            }
            imagesCounter += i
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}