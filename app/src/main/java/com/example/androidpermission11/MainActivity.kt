package com.example.androidpermission11

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

const val PERMISSION_REQUEST_CODE= 1500
class MainActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var scaledBitMap : Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<Button>(R.id.savePDF)
            .setOnClickListener {
                checkPermission()
                requestPermissions()
            }
    }

    private fun createPDF() {
        val pdfDocument = PdfDocument()
        val pdfPageInfo = PdfDocument.PageInfo.Builder(1020,580,1).create()
        val pdf = pdfDocument.startPage(pdfPageInfo)

        val paint = Paint()
        val canvas = Canvas()
        bitmap = BitmapFactory.decodeResource(resources,R.drawable.header)
        scaledBitMap = Bitmap.createScaledBitmap(bitmap,1020,500,false)
        canvas.drawBitmap(scaledBitMap,0f,0f,paint)
        try {
            val fileOutputStream = FileOutputStream(File(Environment.getExternalStorageDirectory(),"PDF -" + UUID.randomUUID().toString()))
            pdfDocument.writeTo(fileOutputStream)
        }catch (ex : Exception){
            Log.d("TAG","Error File ${ex.message}")
        }

        pdfDocument.finishPage(pdf)
    }

    // TODO : Check For permissions
    private fun checkPermission() : Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager()
        } else {
            val result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            val result2  = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
        }
    }

    // TODO : Request For permissions
    private fun requestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    addCategory("android.intent.category.DEFAULT")
                    data = Uri.parse(Uri.parse("package:%s"+applicationContext.packageName).toString())
                    startActivityForResult(this, PERMISSION_REQUEST_CODE)

                }
            }catch (ex : Exception){
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    startActivityForResult(this, PERMISSION_REQUEST_CODE)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if(Environment.isExternalStorageManager()){
                    createPDF()
                } else {
                    Toast.makeText(this,"Allow permissions for android 11",Toast.LENGTH_SHORT).show()
                }
            } else {
                // below OS 11
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
                    val isFirstGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val isSecondGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(isFirstGranted &&  isSecondGranted){
                        createPDF()
                    } else {
                        Toast.makeText(this,"Allow permissions for android 11",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}