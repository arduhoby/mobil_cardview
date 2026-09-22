# KartView

Cam efektli (glassmorphism) kartlar ve onları yöneten, yeniden kullanılabilir Compose tabanlı bir "rolodex" kart yığını kütüphanesi.

Bu depo üç Gradle modülünden oluşur:

| Modül | Açıklama | Bağımlılık |
|-------|----------|-----------|
| `:rolodex` | Genel kart yığını/çekmecesi komponenti (dikey kaydırma, odaklama, yatay dismiss) | `com.kartview:rolodex` |
| `:glasscard` | Cam efektli kart çizimi, gradyan/dome/gloss/bevel ve arka plan blur yardımcıları | `com.kartview:glasscard` |
| `:app` | Her iki kütüphaneyi kullanan tanıtım (demo) uygulaması | — |

Paketler **GitHub Packages** üzerinden `com.kartview:rolodex` ve `com.kartview:glasscard` olarak yayınlanır.

---

## Gereksinimler

- JDK 17+
- Android Studio (Kotlin + Compose Builder)
- Minimum API seviyesi: 24
- Derleme SDK: 36

---

## Kurulum (başka bir projede kullanmak)

### 1. Maven deposunu tanıt

`settings.gradle.kts` içine GitHub Packages deposunu ekle:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/arduhoby/mobil_cardview")
            credentials {
                username = providers.gradleProperty("gpr.user").get()
                password = providers.gradleProperty("gpr.key").get()
            }
        }
        google()
        mavenCentral()
    }
}
```

### 2. Kimlik bilgilerini ekle

`~/.gradle/gradle.properties` dosyasına (Windows: `%USERPROFILE%\.gradle\gradle.properties`):

```properties
gpr.user=arduhoby
gpr.key=<sizin_PAT_buraya>
```

> GitHub Packages, **public** paketlerde bile okumak için kimlik doğrulama ister. Token için:
> `GitHub → Settings → Developer settings → Tokens → (classic)`: `read:packages` (indirme), `write:packages` (yayın).
> Veya fine-grained token: Packages → Read/Write.

### 3. Bağımlılıkları ekle

`app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.kartview:rolodex:0.1.0")
    implementation("com.kartview:glasscard:0.1.0")
}
```

> Compose, BOM ile geldiği için kendi projendeki `androidx.compose.*` sürümleriyle uyumludur (bkz. POM'daki `dependencyManagement`).

---

## Kullanım

### Rolodex (kart yığını)

Dikey sürükleme ile kartlar arasında hareket edilir; odaktaki kart tam boy olarak öne çıkar, diğerleri kompakt halde arkada sıralanır. Odaktaki kartı yatay sürüklemek **dismiss** (kaldırma) yapar.

```kotlin
import com.kartview.rolodex.Rolodex
import com.kartview.rolodex.RolodexConfig

Rolodex(
    items = cards,                       // T tipinde liste
    key = { it.id },
    cardHeight = 280.dp,                 // kartların tam boy yüksekliği
    onDismiss = { card -> remove(card) },// odak kartı yatay savrulunca çağrılır
    modifier = Modifier.fillMaxSize(),
    spacing = 6.dp,                      // kartlar arası boşluk
    config = RolodexConfig(),            // isteğe bağlı görsel ayar
    onFocusChanged = { index -> /* odak değişince */ },
) { item, focused ->
    // Her kartın içeriği; `focused` o kartın odakta olup olmadığını söyler.
    MyCardView(item, highlighted = focused)
}
```

#### `RolodexConfig` parametreleri

| Parametre | Varsayılan | Anlamı |
|-----------|-----------|--------|
| `stepRatio` | `0.44f` | Kart adımı = `cardHeight * stepRatio + spacing` |
| `rowHeightScale` | `0.40f` | Odak dışı kartların yükseklik ölçeği |
| `widthFirstRow` | `0.90f` | 1. sıradaki (odak komşusu) kart genişliği |
| `widthSecondRow` | `0.80f` | 2. sıradaki kart genişliği |
| `widthMin` | `0.42f` | Deformasyonda en minimum genişlik |
| `deformStartRow` | `2.75f` | Daralma/rotasyon deformasyonunun başladığı sıra |
| `deformSpan` | `1.25f` | Deformasyonun tamamlandığı sıra aralığı |
| `cascadePerUnit` | `0.20f` | Uzak kartların sağa kayma miktarı (viewport oranıyla) |
| `cascadeMaxWidth` | `0.55f` | Maksimum sağa kayma (viewport oranı) |
| `rotationDeg` | `14f` | Maksimum Y ekseni dönüşü (derece) |
| `fadeStartRow` | `4.1f` | Solmanın başladığı sıra |
| `fadeSpan` | `0.7f` | Solmanın tamamlandığı sıra aralığı |

### GlassCard (cam efektli kart)

```kotlin
import com.kartview.glasscard.GlassCard
import com.kartview.glasscard.GlassCardDefaults
import com.kartview.glasscard.rememberGlassBackdrop

val backdrop = rememberGlassBackdrop(viewSize, wallpaper = { canvas ->
    // arka plan (gradyen/desen/resim) çizimi
})

GlassCard(
    modifier = Modifier
        .size(280.dp)
        // odaktaki kart için blur arka plan: backdrop'u aşağıda kartın içinden geçir
        .graphicsLayer {
            // ...
        },
    settings = GlassSettings(),        // cam parametreleri (opaklık, renk, kenar…)
    backdrop = backdrop,               // null ise sadece düz cam
    content = { /* kart içeriği */ },
)
```

Cam parametrelerinin tam dökümü için `GlassCardDefaults` ve `GlassSettings` sınıflarına bakın.

---

## Yayınlama (kendi sürümünü çıkarma)

### Elle yayın

```powershell
$env:GITHUB_ACTOR="arduhoby"
$env:GITHUB_TOKEN="<PAT>"
.\gradlew.bat :rolodex:publishReleasePublicationToGitHubPackagesRepository `
              :glasscard:publishReleasePublicationToGitHubPackagesRepository `
              -PpkVersion=0.2.0
```

### Otomatik yayın (tag ile)

Bu depo GitHub Actions workflow'ü (`.github/workflows/publish.yml`) içerir. `v0.2.0` gibi bir **tag** push edildiğinde `rolodex` ve `glasscard` otomatik olarak `0.2.0` sürümüyle GitHub Packages'e yayınlanır:

```bash
git tag v0.2.0 && git push origin v0.2.0
```

---

## Mimari

- **Performans:** `scroll` bir `Animatable`'dır; tüm transformlar `.graphicsLayer` içinde çizim fazında okunur → sürüklerken recomposition olmaz.
- **Odaklama:** `zIndex` ile odaktaki kart daima en üstte; komşu kartlar üzerine binmez.
- **Deformasyon:** 2. ve 3. sıradakiler temiz küçültülmüş kopyalardır; daralma/rotasyon/solma yalnız uzak (4.+) sırada başlar.
- **Dismiss:** odak kart yatay sürüklenip eşiği aşarsa animasyonla ekrandan çıkar; aksi halde geri yaylanır.

---

## Lisans

MIT — bkz. [`LICENSE`](LICENSE).