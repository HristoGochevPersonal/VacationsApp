package com.example.vacationsapp.presentation.utils

import android.app.Activity
import android.net.Uri
import android.provider.BaseColumns
import android.provider.DocumentsContract
import java.io.File

fun Activity.uriIsValid(uri: Uri): Boolean {

    val validUris = contentResolver.persistedUriPermissions.map { uri }

    return isLoadable(uri, validUris) == UriLoadable.YES
}

private enum class UriLoadable {
    YES, NO, MAYBE
}

private fun Activity.isLoadable(uri: Uri, granted: List<Uri>): UriLoadable {

    return when (uri.scheme) {
        "content" -> {
            if (DocumentsContract.isDocumentUri(this, uri))
                if (documentUriExists(uri) && granted.contains(uri))
                    UriLoadable.YES
                else
                    UriLoadable.NO
            else // Content URI is not from a document provider
                if (contentUriExists(uri))
                    UriLoadable.YES
                else
                    UriLoadable.NO
        }

        "file" -> {
            uri.path?.let {
                if (File(it).exists()) UriLoadable.YES else UriLoadable.NO
            } ?: UriLoadable.NO
        }

        // http, https, etc. No inexpensive way to test existence.
        else -> UriLoadable.MAYBE
    }
}

// All DocumentProviders should support the COLUMN_DOCUMENT_ID column
private fun Activity.documentUriExists(uri: Uri): Boolean =
    resolveUri(uri, DocumentsContract.Document.COLUMN_DOCUMENT_ID)

// All ContentProviders should support the BaseColumns._ID column
private fun Activity.contentUriExists(uri: Uri): Boolean =
    resolveUri(uri, BaseColumns._ID)

private fun Activity.resolveUri(uri: Uri, column: String): Boolean {

    val cursor = contentResolver.query(
        uri,
        arrayOf(column), // Empty projections are bad for performance
        null,
        null,
        null
    )

    val result = cursor?.moveToFirst() ?: false

    cursor?.close()

    return result
}

