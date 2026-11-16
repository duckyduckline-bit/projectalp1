# Tahap 1: Peta, karakter, dan GUI

**Ditunjukkan pada:** Selasa 17 Nov 2026

## Cara menjalankan

Windows: klik dua kali `run.bat`.  Mac/Linux: `sh run.sh`.
Atau dari VS Code: buka folder ini lalu jalankan `Main.java`. Butuh JDK 17 atau lebih baru.

## Yang ada di tahap ini

- Jendela game dan Main Menu (PLAY GAME dan EXIT berfungsi; How to Play dan SDGs masih menampilkan pesan "menyusul")
- Peta kolam dalam 2D array (`int[][] map` di GameLogic), dibuat acak tiap game
- Bebek bergerak dengan W/A/S/D atau panah, menoleh kiri/kanan, dan dibatasi tepi kolam
- Tekan ESC untuk kembali ke menu

## Latihan: pertanyaan yang mungkin ditanyakan dosen

- Kenapa peta disimpan sebagai 2D array dan bagaimana isinya (WATER, LAND, LILY, ROCK)?
- Bagaimana `canStand()` mencegah bebek keluar dari air?
- Bagaimana game loop berjalan (`javax.swing.Timer` memanggil `actionPerformed` tiap 16 ms)?
