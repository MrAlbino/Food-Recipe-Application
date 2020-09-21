package com.ayberkkose.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_yemek_tarifleri.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.security.Permission


class YemekTarifleriFragment : Fragment() {

    var secilenGorsel: Uri?=null
    var secilenBitmap:Bitmap?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_yemek_tarifleri, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            kaydet(it)
        }
        imageView.setOnClickListener {
            gorselSec(it)
        }

        arguments?.let {
            var gelenBilgi=YemekTarifleriFragmentArgs.fromBundle(it).bilgi

            if(gelenBilgi.equals("menudengeldim")){
                //yeni bir yemek eklemeye geldi
                yemekIsmiText.setText("")
                yemekMalzemeText.setText("")
                button.visibility=View.VISIBLE

                val gorselSecmeArkaplan=BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                imageView.setImageBitmap(gorselSecmeArkaplan)
            }
            else{
                //daha önce oluşturulan yemeği görmeye geldi
                button.visibility=View.INVISIBLE

                val secilenId=YemekTarifleriFragmentArgs.fromBundle(it).id

                context?.let {

                    try {

                        val db=it.openOrCreateDatabase("yemekler",Context.MODE_PRIVATE,null)
                        val cursor=db.rawQuery("SELECT * FROM yemekler WHERE id= ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex=cursor.getColumnIndex("yemekAdi")
                        val yemekMalzemeIndex=cursor.getColumnIndex("yemekMalzemesi")
                        val yemekGorselIndex=cursor.getColumnIndex("gorsel")

                        while(cursor.moveToNext()){

                            yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))

                            yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi=cursor.getBlob(yemekGorselIndex)

                            val bitmap=BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)

                            imageView.setImageBitmap(bitmap)
                        }

                        cursor.close()
                    }catch (e:Exception){

                        e.printStackTrace()
                    }
                }
            }
        }
    }
    fun kaydet(view:View){

        val yemekIsmi= yemekIsmiText.text.toString()
        val malzemeIsmi=yemekMalzemeText.text.toString()

        if(secilenBitmap!=null){
            val kucukBitmap=kucukBitmapYap(secilenBitmap!!,300)

            val outputStream=ByteArrayOutputStream()

            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)

            val byteDizisi=outputStream.toByteArray()

            try {
                context?.let {
                    val database=it.openOrCreateDatabase("yemekler",Context.MODE_PRIVATE,null)

                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY,yemekAdi VARCHAR,yemekMalzemesi VARCHAR, gorsel BLOB)")

                    val sqlString="INSERT INTO yemekler (yemekAdi,yemekMalzemesi,gorsel) VALUES (?,?,?)"
                    val statement=database.compileStatement(sqlString)

                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,malzemeIsmi)
                    statement.bindBlob(3,byteDizisi)

                    statement.execute()
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }

            val action=YemekTarifleriFragmentDirections.actionYemekTarifleriFragmentToYemekListeFragment()

            Navigation.findNavController(view).navigate(action)
        }

    }
    fun gorselSec(view:View){

        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){

            if(grantResults.isNotEmpty() &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2&&resultCode== Activity.RESULT_OK&&data!=null){

            secilenGorsel=data.data

            try {

                context?.let {
                    if(secilenGorsel!=null){
                        if(Build.VERSION.SDK_INT>=28){
                            val source=ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)

                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)

                        }else{

                            secilenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)

                            imageView.setImageBitmap(secilenBitmap)
                        }

                    }

                }
            }catch (e:Exception){
                println(e.message)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapYap(kullanicininSectigiBitmap:Bitmap,maxBoyut:Int):Bitmap{

        var width=kullanicininSectigiBitmap.width
        var height=kullanicininSectigiBitmap.height

        var oran:Double=width.toDouble()/height.toDouble()

        if(oran>1){
            //gorsel yatay

            width=maxBoyut

            val geciciHeight=width/oran

            height=geciciHeight.toInt()

        }else{
            //gorsel dikey

            height=maxBoyut

            val geciciWidth=height*oran

            width=geciciWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

}