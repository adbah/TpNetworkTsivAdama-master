package fr.istic.mob.networkTB

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.*

class Save_Network : AppCompatActivity(){

    private var myListe: ListView? = null
    val arrayList = ArrayList<String>()

    private lateinit var fileList: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_network)
        val savePath =
            Environment.getExternalStorageDirectory()
                .toString() + "/TPNetworkTsivAdama/Sauvegardes/"
        val file = File(savePath)
        var i: Int;
        if (file!=null){
            fileList = file.list()
        }


        Toast.makeText(this, "ma liste "+fileList.size, Toast.LENGTH_SHORT).show()

        if (fileList != null){
            Arrays.sort(fileList)
            myListe = findViewById(R.id.saveListe) as ListView
             var adapter : ArrayAdapter<String> =  ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fileList)
            myListe!!.setAdapter(adapter)
            myListe!!.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
                val intent = Intent(baseContext, MainActivity::class.java)
                intent.putExtra("file", fileList.get(position))
                startActivity(intent)
                finish()
            })
        }
        }
    }
