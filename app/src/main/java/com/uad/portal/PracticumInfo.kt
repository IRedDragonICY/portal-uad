package com.uad.portal


data class PracticumInfo(
    val status: String,
    val message: String?,
    val data: List<DataItem>
)

data class DataItem(
    val id: Int,
    val jadwal_praktikum_id: Int,
    val nim: String,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String,
    val jadwal_praktikum: JadwalPraktikum
)

data class JadwalPraktikum(
    val id: Int,
    val praktikum_aktif_id: Int,
    val dosen_id: Int,
    val laboratorium_id: Int,
    val hari_id: Int,
    val jam_mulai: String,
    val jam_selesai: String,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String,
    val kapasitas: String,
    val uuidkey: String,
    val praktikum_aktif: PraktikumAktif,
    val lab: Lab,
    val dosen: Dosen,
    val hari: Hari,
    val jam: String?
)

data class PraktikumAktif(
    val id: Int,
    val periode_id: Int,
    val matakuliah_kode: String,
    val harga: Int,
    val additional_data: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val matakuliah: Matakuliah
)

data class Matakuliah(
    val id: Int,
    val kode: String,
    val nama: String,
    val semester: Int,
    val sks: Int,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String,
    val uuidkey_matkul: String
)

data class Lab(
    val id: Int,
    val nama: String,
    val laboran_id: Int,
    val created_at: String,
    val updated_at: String
)

data class Dosen(
    val id: Int,
    val user_id: Int,
    val nip: String,
    val nama: String,
    val email: String,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String
)

data class Hari(
    val id: Int,
    val nama: String,
    val created_at: String,
    val updated_at: String
)
