package hphuc.model3dwithfirebase

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var btnDownload: Button
    lateinit var arFragment: ArFragment
    private var modelRenderable : ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var file: File? = null


        FirebaseApp.initializeApp(this)
        val storage = FirebaseStorage.getInstance()
        val modelRef = storage.reference.child("out.glb")

        val onComplete = OnCompleteListener<FileDownloadTask.TaskSnapshot> {
            file?.let { it1 -> buildModel(it1) }
        }

        arFragment = this.supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        btnDownload = findViewById(R.id.btnDownload)
        btnDownload.setOnClickListener {
            file = File.createTempFile("out", "glb")
            file?.let { modelRef.getFile(it).addOnCompleteListener(onComplete) }
        }

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.renderable = modelRenderable
            arFragment.arSceneView.scene.addChild(anchorNode)
        }
    }

    private fun buildModel(file: File) {
        val render  = RenderableSource.builder()
            .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
            .build()

        ModelRenderable.builder().setSource(this, render).setRegistryId(file.path).build().thenAccept {
            Toast.makeText(this, "Build success", Toast.LENGTH_SHORT).show()
            modelRenderable = it
        }
    }
}