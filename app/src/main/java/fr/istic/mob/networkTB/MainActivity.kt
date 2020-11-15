package fr.istic.mob.networkTB

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileOutputStream
import java.lang.Float
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.ArrayList

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), View.OnLongClickListener, View.OnTouchListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var customDraw: CustomDraw
    private var addObject: Boolean = false
    private var addConnexion: Boolean = false
    private var update: Boolean = false
    private val STORAGE_PERMISSION_CODE = 1
    private val dossierSauvegarde = "/TPNetworkTsivAdama/Sauvegardes/"
    private val dossierSauvegardeImage = "/TPNetworkTsivAdama/Images/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        customDraw = findViewById(R.id.custom_draw)
        customDraw.setOnLongClickListener(this)
        customDraw.setOnTouchListener(this)
        if (savedInstanceState != null) {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.reset -> {
                if(customDraw.graphIsNotEmpty()){
                    clearConfirm()
                }
            }

            R.id.ajout_objet -> {
                addObject = true
                addConnexion = false
                update = false
            }

            R.id.ajout_connexion -> {
                addConnexion = true
                addObject = false
                update = false
            }

            R.id.modification_objet -> {
                update = true
                addObject = false
                addConnexion = false
            }
            R.id.sendNetwork ->{

               if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED) {

                    captureEcran()
                  // Toast.makeText(this, "Hi ", Toast.LENGTH_SHORT).show()
                }else{
                   //Toast.makeText(this, "Hi there! This is.", Toast.LENGTH_SHORT).show()
                   requestPermissionReadWrite()
               }
            }
            R.id.saveNetwork ->{

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED) {
                   // Toast.makeText(this, "Hi there", Toast.LENGTH_SHORT).show()
                    sauvegardReseau()
                }else{
                    //Toast.makeText(this, "Hi there! This is.", Toast.LENGTH_SHORT).show()
                    requestPermissionReadWrite()
                }
            }
            R.id.showNetwork -> {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED) {
                    // affiche la liste des reseaux sauvegardés
                    startActivity(Intent(this, Save_Network::class.java))
                }else{
                    requestPermissionReadWrite()
                }
            }


        }
        return super.onOptionsItemSelected(item)
    }
    // donne la permission d'acceder aux ressources externes du telephone

    private fun requestPermissionReadWrite() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
           AlertDialog.Builder(this)
                .setTitle("Besoin de permission")
                .setMessage("Cette action à besoin d'une permission")
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                    }).setNegativeButton("Annuler",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }
    // permet la capture de l'image du reseau à envoyer

     fun captureEcran(){
  try{
      val Destination = Environment.getExternalStorageDirectory().toString() + dossierSauvegardeImage

      val dir = File(Destination);

      if(!dir.exists()){
          dir.mkdirs()
          //Toast.makeText(this, "capturé", Toast.LENGTH_SHORT).show()
      }
      val path: String = Destination + "Network" + ".jpg"
      //ceation de  la capture d'ecran

      val view = window.decorView.rootView
      view.isDrawingCacheEnabled = true

      view.buildDrawingCache(true)
      val b = Bitmap.createBitmap(view.drawingCache)
      view.isDrawingCacheEnabled = false

      // création du fichier image /TPNetworkTsivAdama/Images/Network.jpg

      val fichierImage = File(path)

      val outputStream = FileOutputStream(fichierImage)
      b.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
      outputStream.flush()
      outputStream.close()

      val intent = Intent(Intent.ACTION_SEND)
      intent.setType("text/plain")

      intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ path))

      intent.putExtra(Intent.EXTRA_SUBJECT, "copie")

      intent.putExtra(Intent.EXTRA_TEXT, "ci-joint une capture du reseau")

      startActivity(Intent.createChooser(intent, "Titre:"))
      Toast.makeText(this, "coupe", Toast.LENGTH_SHORT).show()
      finish();
      Log.i("email", "envoie");


  }
  catch (ex: Throwable ) {
      ex.printStackTrace();
  }
     }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onLongClick(v: View): Boolean {
        if (addObject) {
            onCreateObject()
        }
        return true
    }

    fun onCreateObject() {
        val selectedItems = ArrayList<Int>() // Where we track the selected items
        val builder = AlertDialog.Builder(this)
        var objectNameEdit=EditText(this)//Champ de texte
        builder.setView(objectNameEdit)
        // Set the dialog title
        builder.setTitle(R.string.title_object)
            .setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    customDraw.draw(objectNameEdit.text.toString())
                })
            .setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                })

        builder.create()
        builder.show()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if(addConnexion){
            customDraw.onConnexion(event)
        }
        if(update){
            customDraw.onUpdate(event)
        }
        return super.onTouchEvent(event)
    }
    fun clearConfirm(){
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        // set message of alert dialog
        dialogBuilder.setMessage(R.string.msg_confirm)
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener {
                    dialog, id ->   customDraw.clearGraph()
            })
            // negative button text and action
            .setNegativeButton(R.string.no, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle(R.string.title_confirm)
        alert.show()
    }

        private fun sauvegardReseau() {
        val rand = Random()
        val nombreAleatoire = rand.nextInt(9999- 2 + 1) + 2
        val input = EditText(this)
        input.setText("Reseau_$nombreAleatoire")

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setTitle("savegarde")
        alertDialogBuilder
            .setMessage("entrez le nom du reseau à sauvegarder")
            .setPositiveButton("valider",
                DialogInterface.OnClickListener { dialog, id -> NetworkSaver(input.text.toString()) })
            .setNegativeButton("Annuler",
                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        alertDialogBuilder.setView(input)
       val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
// sauvegarde le reseau dans un document xml pour pouvoir le recuperer après

    fun NetworkSaver(savFileName: String){
        val savePath = Environment.getExternalStorageDirectory()
            .toString() + dossierSauvegarde;
        val dir = File(savePath)
        // SI le dossier n'existe pas on le crée
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val factory = DocumentBuilderFactory.newInstance()


    try {
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val document: Document = builder.newDocument()
        val racine: Element = document.createElement("graphe")
        document.appendChild(racine)

        val currentGraph: Graph = customDraw.getGraph()
        // Pour chaque noeud de notre graphe on sauvegarde les informations
        saveNodes(document, currentGraph, racine)


        // Pour chaque connexion du reseau actuel on sauvegarde
        //saveConnexions(document, currentGraph, racine)
        saveConnexions(document, currentGraph, racine)

        val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
        val transformer: Transformer = transformerFactory.newTransformer()
        val source = DOMSource(document)
        val sortie =
            StreamResult(File(savePath + savFileName.toString() + ".xml"))

        //Prol
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")

        //formatage
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        //sortie
        transformer.transform(source, sortie)
    } catch (e: ParserConfigurationException) {
        e.printStackTrace()
    } catch (e: TransformerConfigurationException) {
        e.printStackTrace()
    } catch (e: TransformerException) {
        e.printStackTrace()
    }


}

    private fun saveConnexions(document: Document,myGraph: Graph, racine: Element) {
        myGraph.connexions.forEach {
            val connexion: Element = document.createElement("connexion")
            connexion.setAttribute("type", "connexion")
            racine.appendChild(connexion)

            val itNoeudSource: Node = it.node1
            val node: Element = document.createElement("noeud")
            node.setAttribute("type", "noeud")
            racine.appendChild(node)
            val positionXS: Element = document.createElement("positionX")
            positionXS.appendChild(document.createTextNode(Float.toString(itNoeudSource.x)))
            val positionYS: Element = document.createElement("positionY")
            positionYS.appendChild(document.createTextNode(Float.toString(itNoeudSource.y)))
            node.appendChild(positionXS)
            node.appendChild(positionYS)

            // deuxième
            val itNoeudDes: Node = it.node2
            val node2: Element = document.createElement("noeud")
            node2.setAttribute("type", "noeud")
            racine.appendChild(node)
            val positionXD: Element = document.createElement("positionX")
            positionXD.appendChild(document.createTextNode(Float.toString(itNoeudDes.x)))
            val positionYD: Element = document.createElement("positionY")
            positionYD.appendChild(document.createTextNode(Float.toString(itNoeudDes.y)))
            node2.appendChild(positionXD)
            node2.appendChild(positionYD)

        }
    }
// permet de sauvegarder les  noeuds

    private fun saveNodes(document: Document, myGraph: Graph, racine: Element) {


        myGraph.nodes.forEach {
            val node: Element = document.createElement("noeud")
            node.setAttribute("type", "noeud")
            racine.appendChild(node)
            val positionX: Element = document.createElement("positionX")
            positionX.appendChild(document.createTextNode(Float.toString(it.x)))
            val positionY: Element = document.createElement("positionY")
            positionY.appendChild(document.createTextNode(Float.toString(it.y)))
            val etiquette: Element = document.createElement("etiquette")
            etiquette.appendChild(document.createTextNode(it.label))
            val color: Element = document.createElement("color")
            color.appendChild(document.createTextNode(it.color.toString()))
            node.appendChild(positionX)
            node.appendChild(positionY)
            node.appendChild(etiquette)
            node.appendChild(color)
        }
    }
}