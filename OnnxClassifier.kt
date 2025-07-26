package com.example.thick_of_it

import android.content.res.AssetFileDescriptor
import ai.onnxruntime.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OnnxClassifier(assetFileDescriptor: AssetFileDescriptor) {
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession

    // Updated labels (6 classes)
    private val labels = listOf("Walking", "Walking Upstairs", "Walking Downstairs", "Sitting", "Standing", "Laying")

    init {
        val modelBytes = loadModel(assetFileDescriptor) // ✅ Returns ByteArray

        // ✅ Enable Optimization to Reduce Memory Usage
        val options = OrtSession.SessionOptions()
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)

        // ✅ Use ByteArray instead of ByteBuffer
        ortSession = ortEnv.createSession(modelBytes, options)
    }

    fun predict(features: FloatArray): String {
        val inputTensor = OnnxTensor.createTensor(ortEnv, features.toByteBuffer(), longArrayOf(1, features.size.toLong()))
        val output = ortSession.run(mapOf(ortSession.inputNames.first() to inputTensor))

        val floatBuffer = (output[0] as OnnxTensor).floatBuffer
        val predictionArray = FloatArray(floatBuffer.remaining())
        floatBuffer.get(predictionArray)

        val predictedIndex = predictionArray.indices.maxByOrNull { predictionArray[it] } ?: 0
        return labels.getOrElse(predictedIndex) { "Unknown" }
    }

    private fun FloatArray.toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(this.size * 4)
            .order(ByteOrder.nativeOrder())
        byteBuffer.asFloatBuffer().put(this)
        return byteBuffer
    }

    // ✅ Corrected: Load model as ByteArray (not ByteBuffer)
    private fun loadModel(assetFileDescriptor: AssetFileDescriptor): ByteArray {
        val inputStream = assetFileDescriptor.createInputStream()
        val modelSize = assetFileDescriptor.length.toInt()
        val buffer = ByteArray(modelSize)
        inputStream.read(buffer)
        inputStream.close()
        return buffer
    }
}
