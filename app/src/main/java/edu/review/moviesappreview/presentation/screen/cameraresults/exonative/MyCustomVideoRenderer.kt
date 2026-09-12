package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import android.view.Surface
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.CryptoConfig
import androidx.media3.decoder.VideoDecoderOutputBuffer
import androidx.media3.exoplayer.video.DecoderVideoRenderer
import androidx.media3.exoplayer.video.VideoRendererEventListener
import android.os.Handler
import androidx.media3.common.C

// Keep your other imports (C, Format, MimeTypes, etc.)
import androidx.media3.exoplayer.RendererCapabilities

@UnstableApi
class MyCustomVideoRenderer(
    eventHandler: Handler? = null,
    eventListener: VideoRendererEventListener? = null
) : DecoderVideoRenderer(
    DEFAULT_ALLOWED_JOINING_TIME_MS,
    eventHandler,
    eventListener,
    MAX_DROPPED_FRAMES_TO_NOTIFY
) {

    // Add this property to your renderer class
    private var currentSurface: Surface? = null

    // 1. Create a variable to hold the decoder instance
    private var myCustomDecoder: MyCustomDecoder? = null

    companion object {
        private const val DEFAULT_ALLOWED_JOINING_TIME_MS = 5000L
        private const val MAX_DROPPED_FRAMES_TO_NOTIFY = 50
    }




    /**
     * Identifies this renderer in ExoPlayer logs and track selection.
     */
    override fun getName(): String {
        return "MyCustomCppVideoRenderer"
    }


    override fun handleMessage(messageType: Int, message: Any?) {
        // 👈 Use the Renderer class constant here
        if (messageType == MSG_SET_VIDEO_OUTPUT) {
            currentSurface = message as? Surface

            val newSurface = message as? Surface
            if (newSurface != null) {
                currentSurface = newSurface

                // 🚀 Pipe the physical screen down to the C++ hardware
                myCustomDecoder?.nativeDecoder?.setSurface(newSurface)
            }
        }

        super.handleMessage(messageType, message)
    }

    // 1. Applying Standard Rule: Accept ANY video, reject ALL audio/subtitles.
    @C.FormatSupport
    override fun supportsFormat(format: Format): Int {
        val mimeType = format.sampleMimeType ?: return RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)

        return if(mimeType.startsWith("video/")) {
            RendererCapabilities.create(C.FORMAT_HANDLED)
        } else {
            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
        }

        // Example for H.264 / HEVC. Change to match your C++ codec's supported format.
        /**
         * only support H264 for now
         */
//        return if (MimeTypes.VIDEO_H264.equals(mimeType, ignoreCase = true)) {
//            RendererCapabilities.create(C.FORMAT_HANDLED)
//        }
//        else {
//            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
//        }
    }


    // 2. Later, when creating the decoder, pass that exact mimeType down to C++!
    override fun createDecoder(
        format: Format,
        cryptoConfig: CryptoConfig?
    ): MyCustomDecoder {
        val mimeType = format.sampleMimeType ?: "video/avc"
        val decoder = MyCustomDecoder(format, currentSurface, mimeType)

        // 🚀 YOU MUST SAVE IT HERE so renderOutputBufferToSurface can use it!
        myCustomDecoder = decoder

        return decoder
    }

    override fun renderOutputBufferToSurface(
        outputBuffer: VideoDecoderOutputBuffer,
        surface: Surface
    ) {
        // 1. Read the hardware index (the claim ticket) we saved in C++
        outputBuffer.data!!.order(java.nio.ByteOrder.nativeOrder())
        val outIdx = outputBuffer.data!!.getInt(0)

        // 🚀 ADD THIS LOG:
//        android.util.Log.d("JNI_DEBUG", "KOTLIN: Handing off buffer to C++ for painting")

        // 🚀 THE NEW CHECK: Only paint if it's a valid hardware ticket!
        if (outIdx >= 0) {
            // 🚀 ADD THIS LOG:
//            android.util.Log.d("JNI_DEBUG", "KOTLIN: Handing off buffer to C++ for painting")

            // 🚀 THE HANDOFF: Tell C++ to paint the buffer onto the surface via GPU
            // changing decode?. "Silent Failure" to actual null-check
            //  decoder?.nativeDecoder?.render(outIdx)

            if(myCustomDecoder == null) {
                android.util.Log.e("JNI_DEBUG", "FATAL: myCustomDecoder is null! The safe call was hiding this.")
            }
            // 🚀 THE HANDOFF: Force the call!
            myCustomDecoder?.nativeDecoder?.render(outIdx)

            // 2. 🚀 CRITICAL: Wipe the ticket to mark it as consumed!
            outputBuffer.data!!.putInt(0, -1)
        }

        // 3. 🚀 Notify ExoPlayer the frame was drawn (This unblocks your Audio clock!)
        onProcessedOutputBuffer(outputBuffer.timeUs)

        // This prevents the system from stalling after exactly 8 frames.
        outputBuffer.release()
    }

    // 1. Add a variable to track the mode
    private var outputMode: Int = C.VIDEO_OUTPUT_MODE_NONE
    override fun setDecoderOutputMode(outputMode: Int) {
        // 2. Catch the mode ExoPlayer wants
        this.outputMode = outputMode

        // 3. Pass it down to your custom decoder
        myCustomDecoder?.setOutputMode(outputMode)
    }

    override fun shouldForceRenderOutputBuffer(
        earlyUs: Long,
        elapsedSinceLastRenderUs: Long
    ): Boolean {
        return super.shouldForceRenderOutputBuffer(earlyUs, elapsedSinceLastRenderUs)
    }

}