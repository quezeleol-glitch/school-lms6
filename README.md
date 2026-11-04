# MAN 19 Asesmen (APK)

Dua tab:
1. **Asesmen**: membuka https://man19.assessment.web.id/login di dalam aplikasi (WebView, login tetap ke server asli).
2. **AI Guru**: chat AI auto-jawab (Gemini) khusus guru, dikunci PIN. Preset: Kunci jawaban, Koreksi esai, Balas siswa, Buat soal.

> API key & PIN guru (ROXB) SUDAH DIISI di file `local.properties`. Tidak perlu setting Secrets.

## Cara A: Build otomatis lewat GitHub (tanpa install apa-apa)
1. Buat repo baru di GitHub, pilih **Private** (wajib, karena ada API key).
2. Upload semua isi folder ini (termasuk `local.properties` dan folder `.github`).
3. Tab **Actions** > "Build APK" > Run workflow.
4. Setelah selesai (±5 menit), download file **MAN19-Asesmen-APK** > ekstrak > install `app-release.apk` di HP.

## Cara B: Android Studio
1. Buka folder ini di Android Studio (Hedgehog atau lebih baru).
2. `local.properties` sudah terisi, langsung lanjut.
3. Build > Build APK(s).

## Catatan keamanan
- PIN di aplikasi hanya gerbang ringan. Untuk benar-benar "khusus guru", pembatasan sebaiknya di server (role guru).
- API key ikut tertanam di APK. Jangan sebar APK ke siswa, dan batasi key di Google AI Studio/Cloud (kuota & restriksi aplikasi Android).
- APK release ditandatangani debug key supaya langsung bisa diinstall. Untuk Play Store, pakai keystore sendiri.
