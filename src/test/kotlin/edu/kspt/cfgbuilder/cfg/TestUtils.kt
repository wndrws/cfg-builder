package edu.kspt.cfgbuilder.cfg

import com.google.common.io.Resources
import java.nio.charset.Charset

fun getPythonCodeExample(filename: String): String {
    val url = Resources.getResource("python/$filename.py")
    return Resources.toString(url, Charset.forName("UTF8"))
}