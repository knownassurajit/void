package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * The Google Play services font provider is only trusted when the app supplies its signing
 * certificates. A corrupted array makes every downloadable font fail, so the checked-in arrays are
 * decoded and fingerprinted against the official values published in android/compose-samples.
 */
class FontCertsTest {

    @Test
    fun certsArray_listsDevAndProdArrays() {
        val certs = parseFontCerts()
        assertEquals(
            listOf("@array/$DEV_ARRAY", "@array/$PROD_ARRAY"),
            certs.arrayOfArrays.getValue(CERTS_ARRAY)
        )
    }

    @Test
    fun prodArray_decodesToOfficialCertificate() {
        val cert = singleCertificate(PROD_ARRAY)
        assertEquals(PROD_SHA256, cert.sha256Hex())
    }

    @Test
    fun devArray_decodesToOfficialCertificate() {
        val cert = singleCertificate(DEV_ARRAY)
        assertEquals(DEV_SHA256, cert.sha256Hex())
    }

    @Test
    fun everyCertItem_isStrictBase64X509() {
        val certs = parseFontCerts().stringArrays
        assertTrue(certs.isNotEmpty())
        certs.forEach { (name, items) ->
            assertTrue("$name has no items", items.isNotEmpty())
            items.forEach { item -> decodeCertificate(item) }
        }
    }

    private fun singleCertificate(arrayName: String): X509Certificate {
        val items = parseFontCerts().stringArrays.getValue(arrayName)
        assertEquals("$arrayName must hold exactly one certificate", 1, items.size)
        return decodeCertificate(items.single())
    }

    private fun decodeCertificate(item: String): X509Certificate {
        val compact = item.filterNot { it.isWhitespace() }
        val der = Base64.getDecoder().decode(compact)
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(der)) as X509Certificate
    }

    private fun X509Certificate.sha256Hex(): String =
        MessageDigest.getInstance("SHA-256").digest(encoded)
            .joinToString(separator = "") { "%02X".format(it) }

    private data class FontCerts(
        val stringArrays: Map<String, List<String>>,
        val arrayOfArrays: Map<String, List<String>>
    )

    private fun parseFontCerts(): FontCerts {
        val file = File(ProjectResources.resDir(), "values/font_certs.xml")
        assertTrue("font_certs.xml is missing", file.isFile)
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
        val stringArrays = root.childElements("string-array").associate { it.getAttribute("name") to it.itemTexts() }
        val arrays = root.childElements("array").associate { it.getAttribute("name") to it.itemTexts() }
        return FontCerts(stringArrays, arrays)
    }

    private fun Element.childElements(tag: String): List<Element> {
        val nodes = getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun Element.itemTexts(): List<String> = childElements("item").map { it.textContent.trim() }

    private companion object {
        const val CERTS_ARRAY = "com_google_android_gms_fonts_certs"
        const val DEV_ARRAY = "com_google_android_gms_fonts_certs_dev"
        const val PROD_ARRAY = "com_google_android_gms_fonts_certs_prod"
        const val DEV_SHA256 = "1975B2F17177BC89A5DFF31F9E64A6CAE281A53DC1D1D59B1D147FE1C82AFA00"
        const val PROD_SHA256 = "F0FD6C5B410F25CB25C3B53346C8972FAE30F8EE7411DF910480AD6B2D60DB83"
    }
}
