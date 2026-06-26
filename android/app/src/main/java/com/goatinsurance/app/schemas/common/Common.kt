package com.goatinsurance.app.schemas.common

import com.google.gson.annotations.SerializedName

data class ResponseModel(
    val success: Boolean = true,
    val message: String = "",
    val data: Any? = null,
)

data class FileUploadResponse(
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: Int,
    @SerializedName("content_type") val contentType: String,
)
