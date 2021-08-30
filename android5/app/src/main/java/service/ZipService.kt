package service

import model.BlokadaException
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.io.inputstream.ZipInputStream
import java.io.InputStream


object ZipService {

    private val fileService = FileService

    fun decodeStream(stream: InputStream, key: String): InputStream {
        var zipInput: InputStream? = null
        try {
            zipInput = ZipInputStream(stream, key.toCharArray())
            val entry = zipInput.nextEntry ?: throw BlokadaException("Unexpected format of the zip file")
            val decoded = fileService.load(zipInput)
            return decoded.toByteArray().inputStream()
        } catch (ex: Exception) {
            throw BlokadaException("Could not unpack zip file", ex)
        }
    }

}