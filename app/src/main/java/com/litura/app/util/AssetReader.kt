package com.litura.app.util

import android.content.Context

object AssetReader {
    fun readJsonFromAssets(context: Context, path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun listBookDirectories(context: Context): List<String> {
        return context.assets.list("books")?.toList() ?: emptyList()
    }
}
