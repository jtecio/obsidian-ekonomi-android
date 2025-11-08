# 🔨 Build & Test Guide - Obsidian Ekonomi

## Snabbstart - Bygg appen

### 1. Förutsättningar

- **Android Studio Hedgehog (2023.1.1)** eller senare
- **JDK 17** eller senare
- **Android SDK 34** (Android 14)
- **Fysisk Android-enhet** (rekommenderas för testning)

### 2. Öppna projektet

```bash
cd "/home/johan/Documents/Blackbox/Arbete/Android Apps/ObsidianEkonomi"
```

Öppna sedan mappen i Android Studio.

### 3. Synka Gradle

Android Studio → **File → Sync Project with Gradle Files**

Första gången tar det 2-5 minuter att ladda ner alla dependencies.

### 4. Kör appen

**Alternativ A: På fysisk enhet (rekommenderat)**
1. Anslut din Android-telefon via USB
2. Aktivera **Developer Options** och **USB Debugging** på telefonen
3. Android Studio → Klicka **▶️ Run**
4. Välj din enhet från listan

**Alternativ B: På emulator**
1. Android Studio → **Device Manager**
2. Skapa ny emulator (Pixel 6, API 34)
3. Starta emulatorn
4. Android Studio → Klicka **▶️ Run**

### 5. Första körningen

När appen startar första gången:

1. **Ge behörigheter** när appen frågar:
   - ✅ Filåtkomst (för att läsa/skriva Obsidian vault)
   - ✅ Kamera (för kvittofoton, kommer i framtida version)

2. **Konfigurera vault-sökväg:**

   Eftersom SettingsScreen inte är implementerad ännu, behöver du **temporärt sätta vault-sökvägen i koden**:

   **Öppna:** `app/src/main/java/se/blackbox/obsidianekonomi/data/SettingsRepository.kt`

   **Ändra default vault path (rad 36):**
   ```kotlin
   val vaultPath: String = "",  // ÄNDRA DENNA!
   ```

   Till:
   ```kotlin
   val vaultPath: String = "/storage/emulated/0/Documents/Blackbox",  // Din vault-sökväg
   ```

   **Eller för emulator (Om du kopierat vault-filer till emulatorn):**
   ```kotlin
   val vaultPath: String = "/storage/emulated/0/Download/TestVault",
   ```

3. **Rebuild appen** (Build → Rebuild Project)

4. **Kör igen** (▶️)

## 🧪 Testning

### Test 1: Lägg till en utgift

1. Öppna appen
2. Skriv **"150"** i belopp-fältet
3. Klicka **"Välj kategori..."** → Välj **🍔 Mat & Dryck**
4. Skriv **"Lunch på Max"** i beskrivning
5. Klicka **"Lägg till"**

**Förväntat resultat:**
- Toast-meddelande: ✅ "Transaktion sparad!"
- Transaktionen syns i listan "Idag (1)"
- Beloppet resettas, formuläret rensas

### Test 2: Verifiera att data sparades till vault

1. Öppna filhanteraren på telefonen/emulatorn
2. Navigera till: `Documents/Blackbox/Journal/Daily/2025/2025-11-08.md` (dagens datum)
3. Öppna filen i en text editor

**Förväntat resultat:**
```markdown
## 💰 Ekonomi

| Tid | Belopp | Kategori | Beskrivning | Kvitto |
|-----|--------|----------|-------------|--------|
| 14:23 | 150 kr | #🍔 | Lunch på Max | - |
```

### Test 3: Lägg till flera transaktioner

Lägg till:
- **50 kr** - 🚗 Transport - "Buss till jobbet"
- **120 kr** - 🍔 Mat & Dryck - "Kaffe och kaka"
- **500 kr** - 🛒 Shopping - "Nya skor"

**Förväntat resultat:**
- Listan "Idag" visar nu **(4)** transaktioner
- Total summa: **-820 kr**

### Test 4: Inkomst

1. Klicka **"Inkomst"**-filtret (istället för "Utgift")
2. Skriv **"5000"** kr
3. Välj kategori **💰 Inkomst**
4. Beskrivning: **"Lön"**
5. Lägg till

**Förväntat resultat:**
- Transaktionen visas med **+5000 kr** i grönt
- Formatet i markdown: `| 14:30 | +5000 kr | #💰 | Lön | - |`

### Test 5: Summering

1. Klicka **"📊 Summering"**-knappen (längst ner till höger)
2. Se summeringen för **Idag**
3. Klicka **"Denna vecka"**-filtret
4. Klicka **"Denna månad"**-filtret

**Förväntat resultat:**
- Huvudkortet visar totala utgifter
- "Per Kategori"-sektion visar breakdown:
  - 🍔 Mat & Dryck: 270 kr (33%)
  - 🚗 Transport: 50 kr (6%)
  - 🛒 Shopping: 500 kr (61%)
- Transaktionslista visar alla dagens transaktioner
- Om inkomst finns: Visar "Inkomster" och "Netto"

### Test 6: Läsa befintliga transaktioner

Om du redan har transaktioner i din Obsidian vault:

1. Stäng och öppna appen igen
2. Öppna **Summering**
3. Välj **"Denna månad"**

**Förväntat resultat:**
- Appen läser alla transaktioner från daily notes denna månad
- Summering visar korrekt totalsumma
- Kategorier summeras korrekt

## 🐛 Felsökning

### Problem: "Vault-sökväg inte konfigurerad"

**Orsak:** Default vault path är tom (`""`)

**Lösning:**
1. Öppna `app/src/main/java/se/blackbox/obsidianekonomi/data/SettingsRepository.kt`
2. Ändra `val vaultPath: String = ""` till din faktiska vault-sökväg
3. Rebuild projektet

### Problem: "Kunde inte läsa transaktioner"

**Möjliga orsaker:**
1. Filbehörighet inte given
2. Fel vault-sökväg
3. Daily notes-mappen finns inte

**Lösning:**
1. Kontrollera att behörighet är given (Settings → Apps → Obsidian Ekonomi → Permissions)
2. Verifiera vault-sökvägen (ska peka på vault-roten, ej till Journal/)
3. Skapa mappen manuellt: `Journal/Daily/2025/`
4. Kolla loggar: `adb logcat | grep ObsidianVault`

### Problem: "Inga transaktioner visas i Summering"

**Möjliga orsaker:**
1. Transaktioner inte sparade korrekt
2. Markdown-format matchar inte det som parsing-koden förväntar sig

**Lösning:**
1. Verifiera att filer finns i vault: `ls Journal/Daily/2025/`
2. Öppna filen och kontrollera formatet:
   ```markdown
   | Tid | Belopp | Kategori | Beskrivning | Kvitto |
   |-----|--------|----------|-------------|--------|
   | 14:23 | 150 kr | #🍔 | Test | - |
   ```
3. Kolla loggar för parsing-fel: `adb logcat | grep "parseTransactions"`

### Problem: "Kategori-emoji visas inte"

**Orsak:** Vissa emulatorer har inte stöd för alla emojis

**Lösning:**
- Testa på fysisk enhet istället
- Eller uppdatera emulator-systembilden

### Problem: Build-fel - "Could not find..."

**Orsak:** Gradle dependencies inte laddade

**Lösning:**
1. Android Studio → **File → Invalidate Caches → Invalidate and Restart**
2. Efter omstart: **File → Sync Project with Gradle Files**
3. Om problemet kvarstår: Radera `.gradle`-mappen i projektet och synka igen

## 📊 Loggar & Debugging

### Visa alla loggar

```bash
adb logcat | grep -E "(ObsidianVault|MainViewModel|HomeScreen|SummaryScreen)"
```

### Filtrera på nivå

**Endast errors:**
```bash
adb logcat *:E | grep ObsidianVault
```

**Debug + Info:**
```bash
adb logcat *:D | grep ObsidianVault
```

### Specifika log-meddelanden att leta efter

**Vid lyckad skrivning:**
```
ObsidianVault: Skrev transaktion till: /storage/.../2025-11-08.md
MainViewModel: Transaktion sparad: 150.0 kr - Mat & Dryck
```

**Vid lyckad läsning:**
```
MainViewModel: Laddade 15 transaktioner
ObsidianVault: Läste 3 transaktioner från 2025-11-08
```

**Vid fel:**
```
ObsidianVault: Fel vid läsning: /storage/.../2025-11-08.md (No such file or directory)
MainViewModel: Kunde inte spara transaktion: Permission denied
```

## 🔨 Build Variants

### Debug Build (för testning)

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (för distribution)

**Först:** Skapa `keystore.properties` i projekt-roten:

```properties
storeFile=/path/to/keystore.jks
storePassword=your_password
keyAlias=your_alias
keyPassword=your_key_password
```

**Sedan:**
```bash
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

### Installera APK via adb

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Testa på fysisk enhet

### Förbered telefonen

1. **Aktivera Developer Options:**
   - Inställningar → Om telefonen
   - Tryck 7 gånger på "Build number"

2. **Aktivera USB Debugging:**
   - Inställningar → Developer Options
   - Sätt på "USB Debugging"

3. **Anslut via USB**
   - Tillåt "USB Debugging" när telefonen frågar

4. **Verifiera anslutning:**
   ```bash
   adb devices
   ```

   Ska visa:
   ```
   List of devices attached
   abc123def456    device
   ```

### Kopiera vault till telefonen

**Alternativ 1: Via Obsidian Sync**
- Installera Obsidian på telefonen
- Logga in med samma konto
- Vaulten synkas automatiskt

**Alternativ 2: Via USB**
```bash
adb push "/home/johan/Documents/Blackbox" "/storage/emulated/0/Documents/Blackbox"
```

**Alternativ 3: Manuellt**
- Anslut telefon som USB-enhet
- Kopiera vault-mappen till `Documents/`

## 🎯 Checklista - Komplett test

- [ ] Appen startar utan crash
- [ ] Behörigheter kan ges
- [ ] Kan lägga till utgift
- [ ] Kan lägga till inkomst
- [ ] Transaktion sparas till korrekt daily note
- [ ] Markdown-formatet är korrekt
- [ ] Dagens transaktioner visas i listan
- [ ] Summering-skärmen öppnas
- [ ] Summering visar korrekt totalsumma
- [ ] Kan växla mellan Idag/Vecka/Månad
- [ ] Kategori-breakdown visas korrekt
- [ ] Kan läsa befintliga transaktioner från vault
- [ ] "Tillbaka"-knappen fungerar i Summering
- [ ] Toast-meddelanden visas vid sparande
- [ ] Fel visas om vault inte konfigurerad

## 🚀 Nästa steg efter testning

När grundläggande testning är klar:

1. **Rapportera buggar** - Skapa TODO för varje bug
2. **SettingsScreen** - Implementera så vault-sökväg kan sättas i UI
3. **Förbättra UX** - Animations, loading states, bättre error messages
4. **OCR** - Implementera kvittoskanning
5. **Play Store** - Förbered för publicering

---

**Senast uppdaterad:** 2025-11-08
**Version:** 0.1 (MVP)
