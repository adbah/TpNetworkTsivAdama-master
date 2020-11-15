package fr.istic.mob.networkTB;

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

private const val STROKE_WIDTH = 12f
private const val TAG = "CustomDraw"

class CustomDraw(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    // Holds the path you are currently drawing.
    private var path = Path()
    private var abscisse: Float = 0f
    private var ordonee: Float = 0f
    private var node1: Node?=null
    private var node2: Node?=null
    private var toUpdate:Boolean?=false

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private val textColor = ResourcesCompat.getColor(resources, R.color.colorBlack, null)
    private val connexionColor = ResourcesCompat.getColor(resources, R.color.colorBlue, null)

    //private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    //private lateinit var frame: RectF
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }
    private val textPaint = Paint().apply {
        color = textColor
        textSize = 30f
    }

    private val connexionPaint = Paint().apply {
        color = connexionColor
        textSize = 40f
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }


    companion object {
        var graph: Graph = Graph()
    }

    fun getGraph(): Graph {
        return graph;
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        abscisse = event.x
        ordonee = event.y
        invalidate()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }


    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        graph.nodes.forEach {
            canvas.drawRect(it.rectF, paint)
            canvas.drawText(it.label, it.rectF.right + 11, it.rectF.centerY(), textPaint)
        }

        graph.connexions.forEach {
            canvas.drawLine(it.node1.rectF.centerX(), it.node1.rectF.centerY(), it.node2.rectF.centerX(), it.node2.rectF.centerY(), connexionPaint)
        }
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        // extraCanvas.drawRect(it.rectF, paint)
        canvas.drawPath(path, paint)
        invalidate()
    }

    fun draw(text: String) {
        val frame = RectF(abscisse, ordonee, abscisse + 75, ordonee + 75)
        graph.nodes.add(Node(frame, abscisse, ordonee, text, Color.BLUE))
    }

    fun onConnexion(event: MotionEvent): Boolean {
        abscisse = event.x
        ordonee = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(abscisse, ordonee)
        node1 = getNode(abscisse, ordonee)
    }

    private fun touchMove() {
        path.lineTo(abscisse, ordonee)
       //extraCanvas.drawPath(path, connexionPaint)
        invalidate()
    }

    private fun touchUp() {
        node2 = getNode(abscisse, ordonee)
        //verifier que node1 et node2 != null
        if(node1==null || node2==null){
            path.reset()
        }else{
            if(node1!=null && node2!=null){
                if(node1==node2){
                    path.reset()
                }else{
                    graph.connexions.add(Connexion(node1 = node1!!, node2 = node2!!))
                }
            }
        }
        path.reset()
    }

    fun getNode(x: Float, y: Float): Node? {
        graph.nodes.forEach {
            if (it.rectF.contains(x, y))
                return it
        }
        return null
    }

    fun onUpdate(event: MotionEvent) : Boolean{
        abscisse = event.x
        ordonee = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchUpdateStart()
            MotionEvent.ACTION_MOVE -> touchUpdateMove()
            MotionEvent.ACTION_UP -> touchUpdateUp()
        }
        return true

    }

    private fun touchUpdateStart() {
        node1 = getNode(abscisse, ordonee)
        if(node1!=null){
            toUpdate=true
        }
    }

    private fun touchUpdateMove() {
        if(node1!=null && toUpdate==true) {
            node1!!.rectF.set(abscisse, ordonee, abscisse + 75, ordonee + 75)
        }
    }

    private fun touchUpdateUp() {
        if(node1!=null && toUpdate==true){
            val frame = RectF(abscisse, ordonee, abscisse + 75, ordonee + 75)
            if(graph.nodes.remove(node1!!)){
                graph.nodes.add(Node(frame, abscisse, ordonee, node1!!.label, Color.BLUE))
            }
        }
    }

    fun clearGraph() {
        graph.connexions.clear()
        graph.nodes.clear()
    }

    fun graphIsNotEmpty(): Boolean {
        if(graph.nodes.isNotEmpty()){
            return true
        }
        return false
    }
}
