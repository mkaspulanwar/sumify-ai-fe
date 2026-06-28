# Contributing to Sumify AI

Terima kasih sudah ingin berkontribusi ke Sumify AI. Dokumen ini menjelaskan alur kontribusi agar perubahan mudah ditinjau dan tetap konsisten dengan struktur project.

## Cara Berkontribusi
1. Fork repository ini.
2. Clone repository hasil fork.
3. Buat branch baru dari branch utama.
4. Lakukan perubahan sesuai kebutuhan.
5. Jalankan build atau test yang relevan.
6. Commit perubahan dengan pesan yang jelas.
7. Push branch ke fork Anda.
8. Buat Pull Request ke repository utama.

## Penamaan Branch
Gunakan nama branch yang singkat dan menjelaskan perubahan.

Contoh:
```text
feature/audio-upload-validation
fix/status-polling
docs/update-readme
refactor/history-manager
```

## Commit Message
Gunakan pesan commit yang jelas dan spesifik.

Contoh:
```text
Add validation for empty upload title
Fix meeting status polling after upload
Update README contribution guide
```

## Standar Kode
- Ikuti struktur package yang sudah ada.
- Gunakan Kotlin idiomatis dan mudah dibaca.
- Simpan state UI di ViewModel jika state dipakai lintas composable.
- Hindari perubahan besar yang tidak terkait dengan tujuan Pull Request.
- Jangan menyimpan credential, token, atau endpoint privat di repository.

## Build dan Verifikasi
Sebelum membuat Pull Request, pastikan project bisa di-build:
```bash
./gradlew assembleDebug
```

Jika perubahan menyentuh logic penting, jalankan test yang relevan:
```bash
./gradlew test
```

## Pull Request
Pull Request sebaiknya berisi:
- ringkasan perubahan
- alasan perubahan
- cara verifikasi
- screenshot atau screen recording jika mengubah UI
- catatan risiko jika ada

## Issue
Saat membuat issue, sertakan:
- deskripsi masalah atau usulan fitur
- langkah reproduksi jika bug
- hasil yang diharapkan
- hasil yang terjadi
- informasi perangkat atau emulator jika relevan

## Area Kontribusi
Kontribusi yang cocok untuk project ini:
- perbaikan UI dan UX
- peningkatan stabilitas upload audio
- perbaikan polling status meeting
- peningkatan dokumentasi
- penambahan test
- refactor kecil yang menjaga perilaku aplikasi tetap sama
