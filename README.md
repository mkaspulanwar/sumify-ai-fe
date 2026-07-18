# Sumify AI: Meeting Summarizer

<p align="center">
  <img src="app/src/main/res/drawable/sumifyai_logo_horizontal.png" alt="Sumify AI" width="420" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material%203-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" />
  <img src="https://img.shields.io/badge/Retrofit-2D6B5C?style=for-the-badge" alt="Retrofit" />
  <img src="https://img.shields.io/badge/OkHttp-4F8CC9?style=for-the-badge" alt="OkHttp" />
  <img src="https://img.shields.io/badge/DataStore-1E88E5?style=for-the-badge" alt="DataStore" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" />
</p>

## Daftar Isi
- [Deskripsi](#deskripsi)
- [Project Ini Apa](#project-ini-apa)
- [Fitur](#fitur)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Dokumentasi](#dokumentasi)
- [Tech Stack](#tech-stack)
- [Permission](#permission)
- [Prasyarat](#prasyarat)
- [Build](#build)
- [Cara Fork dan Copy Project](#cara-fork-dan-copy-project)
- [Kontribusi](#kontribusi)
- [Lisensi](#lisensi)
- [Catatan](#catatan)

## Repository Terkait
Project ini merupakan repository untuk aplikasi Android atau tampilan user Sumify AI.

[![Backend Repository](https://img.shields.io/badge/Backend%20Repository-sumify--ai-181717?style=for-the-badge&logo=github)](https://github.com/AndriRahmadani12/sumify-ai)

## Tim
| Nama | GitHub |
| --- | --- |
| M. Kaspul Anwar | [![GitHub](https://img.shields.io/badge/mkaspulanwar-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mkaspulanwar) |
| Andri Rahmadani | [![GitHub](https://img.shields.io/badge/AndriRahmadani12-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/AndriRahmadani12) |
| Henny Kartika | [![GitHub](https://img.shields.io/badge/hennykartika-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/hennykartika) |

## Deskripsi
Sumify AI adalah aplikasi Android untuk merekam atau mengunggah audio rapat, lalu memprosesnya menjadi ringkasan, transkrip, dan file PDF hasil pemrosesan. Aplikasi ini mendukung mode demo untuk simulasi alur kerja tanpa backend aktif, serta mode online untuk terhubung ke API eksternal.

## Project Ini Apa
Project ini adalah client mobile untuk workflow pembuatan meeting summary. Fokus utamanya ada pada:
- perekaman audio langsung dari aplikasi
- pemilihan file audio dari perangkat
- upload audio ke server
- pemantauan status pemrosesan meeting
- penyimpanan riwayat lokal
- pengelolaan hasil favorit dan download PDF

## Fitur
- Welcome screen dengan status onboarding yang disimpan lokal
- Dashboard untuk akses utama ke pembuatan summary, riwayat favorit, dan settings
- Rekam audio langsung dari mikrofon
- Pilih file audio dari storage perangkat
- Upload audio beserta metadata: judul, deskripsi, dan bahasa
- Mode demo untuk simulasi proses transkripsi dan summarization
- Polling status meeting sampai selesai
- Detail meeting berisi transcript, summary, dan link PDF
- Simpan riwayat meeting secara lokal
- Tandai meeting sebagai favorit
- Hapus meeting dari riwayat
- Unduh file PDF ke folder Downloads
- Konfigurasi endpoint FastAPI berdasarkan build environment

## Arsitektur
Proyek ini memakai pola **Single-Activity + Jetpack Compose + MVVM** dengan
repository sebagai batas data layer dan manual dependency injection melalui
application container.

Alur utamanya:
1. `MainActivity` menjadi entry point tunggal.
2. `SumifyApplication` membuat `AppContainer` sebagai composition root.
3. `AppContainer` menyediakan repository, audio input manager, dan PDF downloader.
4. ViewModel per fitur memegang UI state dan meneruskan operasi data ke repository.
5. `MeetingRepository` mengoordinasikan API, penyimpanan JSON, dan DataStore.
6. UI membaca `StateFlow` dengan `collectAsStateWithLifecycle`.

### Layer Aplikasi
| Layer | Komponen | Tanggung Jawab |
| --- | --- | --- |
| Application | `SumifyApplication`, `AppContainer` | Membuat dan membagikan dependency aplikasi. |
| Entry Point | `MainActivity` | Menginisialisasi theme, ViewModel, navigation, dan start destination. |
| Presentation | `ui/screens`, `ui/components`, `ui/theme` | Menampilkan UI Compose dan mengirim event pengguna. |
| Navigation | `ui/navigation` | Mengatur route dan perpindahan antar-screen. |
| State Holder | `ui/viewmodel` | Memisahkan state onboarding, dashboard/settings, create summary, dan meeting. |
| Repository | `data/repository` | Menjadi entry point data bagi ViewModel. |
| Data Source | `data/source` | Adapter untuk Retrofit, JSON history, dan DataStore. |
| Platform Data | `data/audio`, `data/download` | Membungkus audio input dan Android DownloadManager. |
| Remote Model | `data/model/remote` | DTO yang mengikuti kontrak backend. |
| Domain Model | `domain/model` | Model aplikasi yang digunakan repository dan UI. |

### Komponen Utama
- `app/src/main/java/id/antasari/sumifyai/AppContainer.kt`
- `app/src/main/java/id/antasari/sumifyai/data/repository/MeetingRepository.kt`
- `app/src/main/java/id/antasari/sumifyai/data/repository/DefaultMeetingRepository.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/OnboardingViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/DashboardViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/CreateSummaryViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/MeetingViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/navigation/Navigation.kt`

## Struktur Proyek
```text
app/
  src/main/java/id/antasari/sumifyai/
    SumifyApplication.kt
    AppContainer.kt
    MainActivity.kt
    data/
      api/
      audio/
      download/
      local/
      model/remote/
      repository/
      source/
    domain/
      model/
    ui/
      components/
      navigation/
      screens/
      theme/
      viewmodel/
    utils/
  src/test/
    data/repository/
    ui/viewmodel/
docs/architecture/
```

### Penjelasan Folder
| Folder / File | Deskripsi |
| --- | --- |
| `app/` | Modul utama aplikasi Android. |
| `app/src/main/java/` | Source aplikasi yang dipisahkan menjadi data, domain, dan UI. |
| `app/src/main/res/` | Resource Android seperti drawable, icon, string, dan theme. |
| `app/src/test/` | Unit test repository dan ViewModel. |
| `docs/architecture/` | Diagram arsitektur dan sequence diagram. |
| `gradle/libs.versions.toml` | Version catalog plugin dan library. |

### Penjelasan Package
| Package | Fungsi |
| --- | --- |
| `id.antasari.sumifyai` | Application container dan entry point aplikasi. |
| `id.antasari.sumifyai.data.api` | Konfigurasi Retrofit dan endpoint backend. |
| `id.antasari.sumifyai.data.audio` | Perekaman dan penyalinan file audio ke cache. |
| `id.antasari.sumifyai.data.download` | Integrasi Android DownloadManager. |
| `id.antasari.sumifyai.data.local` | Implementasi penyimpanan JSON dan DataStore. |
| `id.antasari.sumifyai.data.model.remote` | DTO response API. |
| `id.antasari.sumifyai.data.repository` | Koordinasi remote, local, preference, dan demo workflow. |
| `id.antasari.sumifyai.data.source` | Kontrak serta adapter data source. |
| `id.antasari.sumifyai.domain.model` | Model `Meeting` yang dipakai aplikasi. |
| `id.antasari.sumifyai.ui.*` | Navigation, screen, komponen, theme, dan ViewModel per fitur. |

## Diagram Arsitektur
| Diagram | Preview | Deskripsi |
| --- | --- | --- |
| System Architecture Diagram | <img src="docs/architecture/System%20Architecture%20Diagram.png" alt="System Architecture Diagram" width="420" /> | Menjelaskan hubungan antara aplikasi Android, UI layer, ViewModel, local storage, dan backend API. |
| UML Sequence Diagram | <img src="docs/architecture/UML%20Sequence%20Diagram.png" alt="UML Sequence Diagram" width="420" /> | Menjelaskan urutan interaksi user, aplikasi, ViewModel, local storage, dan backend saat proses upload dan pembuatan summary. |

## Dokumentasi
### Alur Penggunaan
1. Buka aplikasi.
2. Lewati welcome screen saat pertama kali masuk.
3. Masuk ke dashboard.
4. Rekam audio atau pilih file audio dari perangkat.
5. Isi judul, deskripsi, dan bahasa.
6. Upload audio untuk diproses.
7. Pantau status meeting sampai selesai.
8. Buka detail meeting untuk melihat summary, transcript, dan PDF.

### Konfigurasi Backend
- Endpoint backend dikonfigurasi melalui Gradle property atau environment variable.
- Build debug memakai `http://10.0.2.2:8000/` secara default untuk Android Emulator.
- Build release wajib memakai endpoint HTTPS.
- Mode demo dapat diaktifkan dari `Settings` untuk menjalankan simulasi tanpa backend.

### Referensi File Penting
- `app/src/main/java/id/antasari/sumifyai/MainActivity.kt`
- `app/src/main/java/id/antasari/sumifyai/AppContainer.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/navigation/Navigation.kt`
- `app/src/main/java/id/antasari/sumifyai/data/repository/DefaultMeetingRepository.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/CreateSummaryViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/ui/viewmodel/MeetingViewModel.kt`
- `app/src/main/java/id/antasari/sumifyai/data/api/ApiConfig.kt`

## Tech Stack
| Teknologi | Versi | Penggunaan |
| --- | --- | --- |
| Android Gradle Plugin | `8.13.2` | Plugin build utama untuk aplikasi Android. |
| Kotlin | `2.0.21` | Bahasa utama pengembangan aplikasi. |
| Jetpack Compose BOM | `2024.09.00` | Mengelola versi library Compose agar konsisten. |
| Compose UI | BOM | Membangun antarmuka aplikasi secara deklaratif. |
| Compose Material 3 | BOM | Komponen UI Material Design 3. |
| Activity Compose | `1.12.2` | Integrasi Compose dengan Android Activity. |
| Navigation Compose | `2.8.5` | Navigasi antar screen Compose. |
| Lifecycle Runtime KTX | `2.10.0` | Lifecycle-aware coroutine dan utilitas AndroidX. |
| Lifecycle Runtime Compose | `2.10.0` | Pengumpulan Flow secara lifecycle-aware di Compose. |
| Lifecycle ViewModel Compose | `2.8.7` | Integrasi ViewModel dengan Compose. |
| DataStore Preferences | `1.1.1` | Penyimpanan preferensi seperti status welcome. |
| Retrofit | `2.11.0` | HTTP client untuk komunikasi dengan backend API. |
| Retrofit Gson Converter | `2.11.0` | Konversi JSON response API menjadi Kotlin data class. |
| OkHttp Logging Interceptor | `4.12.0` | Logging request dan response HTTP saat debugging. |
| Gson | `2.11.0` | Serialisasi dan deserialisasi JSON, termasuk history lokal. |
| JUnit | `4.13.2` | Unit testing. |
| Kotlin Coroutines Test | `1.9.0` | Test coroutine dan ViewModel secara deterministik. |
| AndroidX JUnit | `1.3.0` | Instrumentation testing Android. |
| Espresso | `3.7.0` | UI testing Android. |

### Dependency Utama
| Dependency | Artifact | Fungsi |
| --- | --- | --- |
| AndroidX Core KTX | `androidx.core:core-ktx` | Extension Kotlin untuk API Android. |
| Activity Compose | `androidx.activity:activity-compose` | Menjalankan UI Compose dari Activity. |
| Compose UI | `androidx.compose.ui:ui` | Fondasi rendering UI Compose. |
| Compose Graphics | `androidx.compose.ui:ui-graphics` | Dukungan drawing dan resource grafis Compose. |
| Compose Tooling Preview | `androidx.compose.ui:ui-tooling-preview` | Preview composable di Android Studio. |
| Compose Material 3 | `androidx.compose.material3:material3` | Komponen UI Material 3. |
| Navigation Compose | `androidx.navigation:navigation-compose` | Navigasi deklaratif antar screen. |
| Lifecycle Runtime Compose | `androidx.lifecycle:lifecycle-runtime-compose` | Menyediakan `collectAsStateWithLifecycle`. |
| Lifecycle ViewModel Compose | `androidx.lifecycle:lifecycle-viewmodel-compose` | Menghubungkan ViewModel dengan composable. |
| DataStore Preferences | `androidx.datastore:datastore-preferences` | Penyimpanan key-value modern berbasis coroutine Flow. |
| Retrofit | `com.squareup.retrofit2:retrofit` | Membuat HTTP API service. |
| Retrofit Gson Converter | `com.squareup.retrofit2:converter-gson` | Parsing JSON response dari backend. |
| OkHttp Logging | `com.squareup.okhttp3:logging-interceptor` | Debugging request dan response API. |
| Gson | `com.google.code.gson:gson` | Mengelola JSON lokal dan model API. |
| Coroutines Test | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | Test repository dan state holder berbasis coroutine. |

### Testing
Unit test saat ini mencakup:
- publikasi history lokal melalui repository
- upload mode demo tanpa memanggil remote data source
- perubahan status favorite
- state onboarding dan callback setelah welcome selesai

Jalankan unit test:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## Permission
Aplikasi ini menggunakan permission:
- `INTERNET`
- `RECORD_AUDIO`
- `MODIFY_AUDIO_SETTINGS`

## Prasyarat
- Android Studio terbaru yang mendukung proyek Android Gradle Kotlin DSL
- JDK 11
- Android SDK sesuai `compileSdk` dan `targetSdk`
- Backend API jika ingin memakai mode online

## Build
Endpoint backend tidak dapat diubah dari UI. Aplikasi membaca endpoint dari `BuildConfig` agar konfigurasi jaringan mengikuti environment deployment.

Build debug secara default memakai FastAPI lokal melalui alamat Android Emulator:

```text
http://10.0.2.2:8000/
```

Untuk mengganti endpoint debug, gunakan Gradle property:

```powershell
.\gradlew.bat assembleDebug -PSUMIFY_DEBUG_API_BASE_URL=https://dev-api.example.com/
```

atau environment variable:

```powershell
$env:SUMIFY_DEBUG_API_BASE_URL="https://dev-api.example.com/"
.\gradlew.bat assembleDebug
```

Build release wajib menerima endpoint HTTPS. Build akan gagal jika nilainya kosong atau memakai HTTP:

```powershell
$env:SUMIFY_RELEASE_API_BASE_URL="https://api.example.com/"
.\gradlew.bat assembleRelease
```

Nilai tersebut juga dapat disimpan di Gradle user properties pada `~/.gradle/gradle.properties`, bukan di repository:

```properties
SUMIFY_RELEASE_API_BASE_URL=https://api.example.com/
```

Jalankan dari Android Studio atau via Gradle:
```bash
./gradlew assembleDebug
```

## Cara Fork dan Copy Project
### Fork
1. Buka repository ini di GitHub.
2. Klik tombol `Fork`.
3. Pilih akun atau organisasi tujuan.
4. Clone repository hasil fork ke komputer lokal.

### Copy / Clone
```bash
git clone <url-repository>
cd sumifyai
```

### Jalankan Setelah Copy
1. Buka project di Android Studio.
2. Sinkronkan Gradle.
3. Sesuaikan base URL backend melalui Gradle property atau environment variable bila perlu.
4. Jalankan aplikasi di emulator atau device.

## Kontribusi
Kontribusi terbuka untuk perbaikan fitur, bugfix, dan dokumentasi. Panduan lengkap tersedia di [`CONTRIBUTING.md`](CONTRIBUTING.md).

### Alur Kontribusi
1. Fork repository ini.
2. Buat branch baru untuk perubahan.
3. Kerjakan perubahan yang dibutuhkan.
4. Pastikan project tetap berhasil di-build.
5. Push branch ke repository fork.
6. Buat Pull Request ke repository utama.

### Panduan Singkat
- Ikuti struktur kode yang sudah ada.
- Hindari perubahan yang tidak terkait dengan task.
- Gunakan format semantic commit message seperti `feat(upload): add validation for empty title`.
- Sertakan penjelasan singkat pada Pull Request.
- Jika ada perubahan perilaku, tambahkan dokumentasi seperlunya.

### Format Pull Request
Gunakan ringkasan singkat seperti:
```text
Judul: Fix upload flow on dashboard

Perubahan:
- memperbaiki alur upload
- menambah validasi input
- memperjelas pesan error
```

Template Pull Request dan Issue juga tersedia di folder `.github`.

## Lisensi
Lisensi project belum ditentukan. Tentukan lisensi sebelum project dipublikasikan atau digunakan ulang oleh pihak lain.

## Catatan
Base URL API tidak disimpan di UI. Default endpoint debug pada Android Emulator
mengarah ke `http://10.0.2.2:8000/`, sedangkan build release wajib menerima
endpoint HTTPS melalui konfigurasi build.
