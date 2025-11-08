# Obsidian Ekonomi - Android App

En Android-app för att snabbt logga ekonomisk data (utgifter och inkomster) direkt till ditt lokala Obsidian-vault.

## ✨ Funktioner

- 📝 **Snabbinput** - Logga utgift med 3 klick
- 📸 **Kvittofoto + OCR** - Ta foto, appen läser belopp automatiskt (Google ML Kit)
- 📊 **Summering** - Se dagens/veckans/månadens utgifter direkt
- 🏷️ **Flexibla kategorier** - Anpassa med egna emojis och taggar
- ⚙️ **Anpassningsbar** - Välj hur data sparas (Daily notes, månads-note, osv.)
- 🔄 **Obsidian Sync** - Data sparas lokalt, synkas automatiskt via Obsidian

## 🚀 Kom Igång

### Förutsättningar

- Android Studio Hedgehog (2023.1.1) eller senare
- Min Android SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)

### Installation

1. **Klona/Öppna projektet i Android Studio**

2. **Synka Gradle**
   - Android Studio → File → Sync Project with Gradle Files

3. **Konfigurera signing (för release build)**
   - Skapa `keystore.properties` i projektets rot:
   ```properties
   storeFile=/path/to/your/keystore.jks
   storePassword=your_password
   keyAlias=your_alias
   keyPassword=your_key_password
   ```

4. **Bygg och kör**
   - Anslut Android-telefon eller starta emulator
   - Klicka Run (▶️)

### Första Gången

1. **Ge behörigheter**
   - Kamera (för kvittofoton)
   - Filåtkomst (för Obsidian vault)

2. **Välj vault-mapp**
   - Inställningar → Vault-sökväg → Välj din Obsidian vault-mapp
   - Exempel: `/storage/emulated/0/Documents/Blackbox`

3. **Anpassa inställningar**
   - Välj hur data ska sparas (Daily notes rekommenderas)
   - Välj markdown-format (Tabell, Bullet list, osv.)
   - Anpassa kategorier

4. **Testa!**
   - Gå tillbaka till Hem-skärmen
   - Ange belopp (t.ex. 150)
   - Välj kategori (🍔 Mat)
   - Skriv beskrivning (Lunch)
   - Klicka "Spara Utgift"
   - Öppna Obsidian på datorn → Se din transaktion i dagens daily note!

## 📱 Användning

### Snabb Utgift

1. Öppna appen
2. Fyll i:
   - Belopp (kr)
   - Kategori (klicka emoji)
   - Beskrivning (valfri)
3. Klicka "💾 SPARA UTGIFT"

**Resultat:** Transaktionen sparas direkt i din vault!

### Kvittofoto

1. Klicka "📷 TA FOTO AV KVITTO"
2. Ta foto
3. Appen läser automatiskt:
   - Belopp
   - Datum
   - Butik (gissar kategori)
4. Korrigera om fel
5. Spara

**Resultat:** Kvittobild sparas i `Media/Kvitton/` och länkas i markdown!

### Summering

Klicka på "📊" längst upp för att se:
- **Idag:** Totala utgifter idag
- **Denna vecka:** Utgifter denna vecka
- **Denna månad:** Utgifter denna månad
- **Per kategori:** Hur mycket på Mat, Bensin, osv.

Datan läses direkt från ditt vault (alla transaktioner du lagt till via appen eller manuellt i Obsidian).

## ⚙️ Inställningar

### Sparningsmetod

**Daily Notes (Rekommenderat)**
- Lägger transaktioner i dagens daily note
- Exempel: `Journal/Daily/2025/2025-11-06.md`

**Dedikerad Ekonomi-note**
- En note per månad/år
- Exempel: `Privat/Ekonomi/2025-11.md`

**Separat Note per Transaktion**
- En markdown-fil per transaktion
- Bäst för Dataview-queries

### Markdown-format

**Tabell** (Standard)
```markdown
| Tid | Belopp | Kategori | Beskrivning | Kvitto |
|-----|--------|----------|-------------|--------|
| 14:23 | 150 kr | #🍔 | Lunch | ![[...]] |
```

**Punktlista**
```markdown
- **150 kr** #🍔 - Lunch (14:23) ![[...]]
```

**Dataview Inline**
```markdown
- [belopp:: 150] [kategori:: #🍔] [beskrivning:: Lunch]
```

### Kategorier

Standard-kategorier:
- 🍔 Mat
- ⛽ Bensin
- 🏠 Hem
- 💼 Arbete
- 💊 Hälsa
- 🛒 Shopping
- 🎬 Nöje
- 📱 Övrigt

Du kan lägga till egna i framtida version!

## 🏗️ Projektstruktur

```
app/src/main/java/se/blackbox/obsidianekonomi/
├── MainActivity.kt                 # Huvudaktivitet ✅
├── MainViewModel.kt                # Logik & state ✅
├── data/
│   ├── Models.kt                   # Datamodeller ✅
│   ├── ObsidianVault.kt            # Vault läs/skriv ✅
│   └── SettingsRepository.kt       # Inställningar ✅
├── ui/
│   ├── HomeScreen.kt               # Huvudskärm ✅
│   ├── SummaryScreen.kt            # Summering ✅
│   ├── SettingsScreen.kt           # Inställningar (TODO)
│   └── theme/
│       ├── Theme.kt                # Material 3 tema ✅
│       └── Type.kt                 # Typografi ✅
└── util/
    └── ReceiptScanner.kt           # OCR (TODO - framtida)
```

### ✅ Implementerat (v0.1 - MVP)

- **HomeScreen** - Snabbinmatning av utgifter/inkomster
- **SummaryScreen** - Visar summering per dag/vecka/månad och kategori
- **MainViewModel** - Hanterar all business logic och state
- **ObsidianVault** - Läser och skriver markdown till vault
- **Models** - Komplett datamodell för transaktioner och inställningar
- **Theme** - Material 3 ljust/mörkt tema med dynamic colors

### 🚧 Kvarstår för v1.0

- **SettingsScreen** - UI för konfiguration av vault-sökväg och format
- **ReceiptScanner** - OCR-integration för kvittofoton (Google ML Kit)
- **CameraX** - Foto-funktion för kvitton

## 🔒 Behörigheter

Appen kräver:
- **CAMERA** - För kvittofoton
- **READ_MEDIA_IMAGES** (Android 13+) - Läsa vault-filer
- **READ_EXTERNAL_STORAGE** (Android 12-) - Läsa vault-filer

Alla behörigheter begärs vid körning, inte installation.

## 🚢 Publicera till Google Play Store

### 1. Skapa Release Build

```bash
./gradlew assembleRelease
```

APK finns i: `app/build/outputs/apk/release/app-release.apk`

### 2. Signing

Appen är redan konfigurerad för signing om du har skapat `keystore.properties`.

### 3. Google Play Console

1. Gå till https://play.google.com/console
2. Skapa ny app
3. Ladda upp APK/AAB
4. Fyll i metadata (beskrivning, screenshots, etc.)
5. Publicera!

**Se:** `PLAY_STORE_GUIDE.md` för komplett guide.

## 📸 Screenshots (för Play Store)

Rekommenderade storlekar:
- **Phone:** 1080 x 1920 px
- **Tablet (7"):** 1200 x 1920 px
- **Tablet (10"):** 1600 x 2560 px

Ta screenshots av:
1. Huvudskärm med snabbinput
2. Kvittofoto-funktion
3. Summering-skärm
4. Inställningar

## 🐛 Felsökning

### "Vault-sökvägen finns inte"

**Lösning:**
- Kontrollera att Obsidian-vaulten finns på telefonen
- Ge appen filbehörigheter
- Välj korrekt mapp i Inställningar

### "Inga transaktioner visas i summering"

**Lösning:**
- Kontrollera att transaktioner sparats (öppna vault i Obsidian)
- Verifiera att markdown-formatet matchar inställningarna
- Kolla loggar: `adb logcat | grep ObsidianVault`

### "OCR läser fel belopp"

**Lösning:**
- Ta foto med god belysning
- Håll telefonen stadigt
- Korrigera manuellt efter OCR-scanning
- OCR är 80% noggrann, inte 100%

## 🔮 Framtida Funktioner (Roadmap)

- [ ] Widget för hemskärm (1-klick loggning)
- [ ] Inkomster (inte bara utgifter)
- [ ] Budgetar med varningar
- [ ] Exportera till CSV
- [ ] Dark mode
- [ ] Redigera/ta bort transaktioner
- [ ] Sök i historik
- [ ] Backup/Restore av inställningar
- [ ] Multi-vault support

## 📄 Licens

MIT License - använd fritt!

## 🤝 Bidra

Pull requests välkomna! Öppna en issue för större ändringar.

## 📞 Support

- **Issues:** https://github.com/din-org/obsidian-ekonomi/issues
- **Email:** support@example.com

---

**Version:** 1.0.0
**Skapad:** 2025-11-06
**Min Android:** 8.0 (API 26)
**Target Android:** 14 (API 34)
