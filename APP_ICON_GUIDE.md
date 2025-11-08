# App Icon Guide - Obsidian Ekonomi

## Ikon-koncept

**Tema:** Ekonomi + Obsidian markdown

**Huvudelement:**
- 💰 Mynt/pengasymbol (ekonomi)
- 📝 Dokument/markdown-symbol (Obsidian)
- Färger: Lila/blå gradient (associerar med Obsidian-branding)

## Ikonstorlekar som behövs

För Android behöver du skapa ikoner i följande storlekar:

### Adaptive Icon (Rekommenderat för Android 8+)

**Foreground layer:**
- `res/mipmap-mdpi/ic_launcher_foreground.png` - 108 x 108 px
- `res/mipmap-hdpi/ic_launcher_foreground.png` - 162 x 162 px
- `res/mipmap-xhdpi/ic_launcher_foreground.png` - 216 x 216 px
- `res/mipmap-xxhdpi/ic_launcher_foreground.png` - 324 x 324 px
- `res/mipmap-xxxhdpi/ic_launcher_foreground.png` - 432 x 432 px

**Background layer:**
- `res/mipmap-mdpi/ic_launcher_background.png` - 108 x 108 px
- `res/mipmap-hdpi/ic_launcher_background.png` - 162 x 162 px
- `res/mipmap-xhdpi/ic_launcher_background.png` - 216 x 216 px
- `res/mipmap-xxhdpi/ic_launcher_background.png` - 324 x 324 px
- `res/mipmap-xxxhdpi/ic_launcher_background.png` - 432 x 432 px

### Legacy Icon (för Android 7 och äldre)

- `res/mipmap-mdpi/ic_launcher.png` - 48 x 48 px
- `res/mipmap-hdpi/ic_launcher.png` - 72 x 72 px
- `res/mipmap-xhdpi/ic_launcher.png` - 96 x 96 px
- `res/mipmap-xxhdpi/ic_launcher.png` - 144 x 144 px
- `res/mipmap-xxxhdpi/ic_launcher.png` - 192 x 192 px

### Round Icon (vissa Android-startskärmar)

- `res/mipmap-mdpi/ic_launcher_round.png` - 48 x 48 px
- `res/mipmap-hdpi/ic_launcher_round.png` - 72 x 72 px
- `res/mipmap-xhdpi/ic_launcher_round.png` - 96 x 96 px
- `res/mipmap-xxhdpi/ic_launcher_round.png` - 144 x 144 px
- `res/mipmap-xxxhdpi/ic_launcher_round.png` - 192 x 192 px

## Design-rekommendationer

### Färgpalett

**Primär gradient:**
- Lila: `#6750A4` (Material You primary)
- Mörkare lila: `#4F378B`
- Blå accent: `#2196F3`

**Alternativ (enklare):**
- Solid bakgrund: `#6750A4` (lila)
- Vit förgrund med emoji: 💰 eller 📝

### Designförslag 1: Minimalistisk emoji

**Bakgrund:** Solid lila gradient
**Förgrund:**
- Stor emoji i center: 💰
- Liten markdown-hashtag (#) i hörnet

### Designförslag 2: Abstract

**Bakgrund:** Lila-blå gradient
**Förgrund:**
- Stiliserat mynt med linjer (markdown-stil)
- Avrundade hörn

### Designförslag 3: Text-baserad

**Bakgrund:** Material gradient
**Förgrund:**
- Stor "Ö" (för Ökonomi)
- Stiliserat som mynt

## Snabb lösning - Använd Android Studio

1. **Högerklicka på `res` i Android Studio**
2. Välj **New → Image Asset**
3. Välj **Launcher Icons (Adaptive and Legacy)**
4. Använd:
   - **Icon Type:** Text (skriv "💰" eller "ÖE")
   - **Background:** Solid color `#6750A4`
   - **Foreground scaling:** Resize 80%
5. Klicka **Next** → **Finish**

Android Studio genererar automatiskt alla storlekar!

## Alternativ - Generera online

**Webbverktyg:**
1. **Android Asset Studio:** https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
   - Ladda upp en bild eller text
   - Genererar alla storlekar
   - Ladda ner zip-fil
   - Extrahera till projektet

2. **Figma/Canva:**
   - Skapa en 512x512 px design
   - Exportera som PNG
   - Använd Android Asset Studio för att generera alla storlekar

## För snabb MVP-testning

**Temporär lösning:**
Appen använder just nu Android's default ikon (grön Android-robot).

**För att byta till emoji-ikon (2 min):**

1. Öppna Android Studio
2. Högerklicka `res` → New → Image Asset
3. Text: `💰`
4. Background color: `#6750A4`
5. Next → Finish

## Play Store-krav (för publicering)

När du publicerar på Play Store behöver du också:

**High-res icon:**
- 512 x 512 px PNG med transparent bakgrund
- Används i Play Store listing

**Feature graphic:**
- 1024 x 500 px PNG
- Banner som visas överst i Play Store

**Screenshots:**
- Minst 2 screenshots (rekommenderat 4-8)
- 1080 x 1920 px (portrait)
- Visa olika delar av appen (hem, summering, inställningar)

## Nuvarande status

**Just nu:** Appen använder default Android-ikon (grön robot)

**Behöver göras:**
- [ ] Skapa/generera app-ikon (5-15 min)
- [ ] Lägga till i projektet
- [ ] Testa att ikonen syns korrekt

**Prioritet:** Medium (funktionaliteten är viktigare för MVP, men ikon ser proffsigare ut)

---

**Tips:** För MVP-testning räcker det med en enkel emoji-baserad ikon (💰 på lila bakgrund). När appen är klar för Play Store kan du anlita en designer för en mer polerad ikon.
