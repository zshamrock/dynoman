package com.akazlou.dynoman.domain

data class UpdateAnnouncement(
        val version: String, val announcement: String = "", val changelog: String = "", val url: String = "") {
    fun shouldAnnounce(): Boolean {
        return version != Version.CURRENT
    }
}