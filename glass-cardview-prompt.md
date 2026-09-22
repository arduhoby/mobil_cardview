# Görev: Cross-Platform "Glass CardView" Bileşeni (Android + iOS)

## Amaç
Android (Kotlin) ve iOS (Swift) projelerinde kullanılacak, modern "cam" (glassmorphism / Liquid Glass tarzı) görünüme sahip, tamamen özelleştirilebilir bir **CardView bileşeni** geliştir. İlk aşamada sadece **Android tarafı** implemente edilecek ve örnek verilerle çalışan bir **demo APK** üretilecek. Swift/iOS implementasyonu ikinci aşamadır ancak mimari en baştan iki platformu da destekleyecek şekilde kurulmalıdır.

## Teknoloji Seçimi
- **Android:** Jetpack Compose (View/XML sistemi değil). Blur ve cam efekti için `Modifier.blur`, `Modifier.graphicsLayer` ve gerekirse `RenderEffect` (Android 12+) kullan; düşük API seviyeleri için yarı saydam yüzey + gradient + ince border ile "graceful fallback" sağla.
- **iOS (ileride):** SwiftUI, iOS 26'daki yeni **Liquid Glass** API'si (`.glassEffect` / `GlassEffectContainer`) temel alınarak tasarlanacak; daha eski iOS sürümleri için `.ultraThinMaterial` tabanlı fallback.
- **Platformlar arası tutarlılık:** Ortak bir **design token şeması** tanımla (JSON: `cornerRadius`, `backgroundOpacity`, `blurIntensity`, `stripePosition`, `stripeThickness`, `stripeColor`, `shadow`, `borderWidth` vb.). Her platform bu token'ları kendi native tema sistemine (Compose `Theme` / SwiftUI `Environment`) map'leyerek okusun — yani görünüm tanımı ortak, render tamamen native.

## Minimum Sürüm ve Paketleme
- **Android minSdk:** 24 (Android 7.0) — pazar payının büyük kısmını kapsar. `RenderEffect` tabanlı gerçek blur sadece API 31+ (Android 12) cihazlarda aktif olsun; altındaki sürümlerde yarı saydam yüzey + gradient + border fallback'i kullanılsın. `targetSdk`/`compileSdk`: en güncel stabil sürüm (şu an 35).
- **iOS minimum sürüm (ileride):** iOS 16 — Liquid Glass (`.glassEffect`) sadece iOS 26+ cihazlarda devrede olsun, altındaki sürümlerde `.ultraThinMaterial` fallback'i kullanılsın.
- **Paketleme:** Bu aşamada bağımsız bir Maven/CocoaPods paketi olarak yayınlamıyoruz. Bileşen, projede ayrı bir **Gradle module** (`:glasscard`) olarak izole edilsin ki API yüzeyi netleşsin ve ileride bağımsız bir kütüphaneye (Maven/JitPack) dönüştürülmesi kolay olsun. Şimdilik hedef: tek repo içinde modüler, yeniden kullanılabilir kod — yayınlama işlemi bileşen olgunlaştıktan sonraki bir aşamaya bırakılıyor.

## Bileşen Gereksinimleri

### 1. Cam (Glass) Görünüm
- Arkasındaki içeriği hafifçe bulanıklaştıran gerçek bir blur efekti (performans sorunlarına dikkat — liste içinde çok sayıda kart render edilecek).
- Yarı saydam yüzey + hafif üst-alt gradient + ince (1px) parlak border ile "cam" hissi güçlendirilmeli.
- Köşe yarıçapı (corner radius) parametrik olmalı, varsayılan değer makul bir yuvarlaklıkta (örn. 20dp) olsun.
- Arka plan rengi/opaklığı ve blur yoğunluğu parametrik olmalı.

### 2. Renk Manşeti (Accent Stripe)
- Kartın **üstünde, altında, solunda veya sağında** gösterilebilen renkli bir şerit.
- Kullanıcı şeridin **pozisyonunu** (top/bottom/left/right) ve **kapalı/açık** durumunu seçebilmeli.
- Varsayılan kalınlık **3px**, ancak kullanıcı bunu ayarlar ekranından değiştirebilmeli.
- Bu rengin anlamı: **kartın kendisinin değil, içindeki verinin türünü/kategorisini** belirtir (örn. "fotoğraf", "not", "konum" gibi veri tipleri — demo verisinde örneklendir).
- Renk paleti kullanıcı tarafından seçilebilir olmalı (renk seçici + önceden tanımlı preset'ler).

### 3. İçerik Yapısı
- Kart içeriği **tamamen serbest/slot tabanlı** olmalı (Compose'da `content: @Composable () -> Unit` gibi). Bileşen kendi başına başlık/ikon/badge dayatmamalı; içeriği kullanan uygulama oluşturur.
- Demo uygulamasında örnek içerik olarak **liste + fotoğraflı veri kartları** kullanılacak (örn. bir görsel, başlık, kısa açıklama içeren örnek veri seti).

### 4. Ayarlar Ekranı (Demo Uygulama İçinde)
- Uygulama içinde, kullanıcının kart görünümünü **canlı önizleme** ile değiştirebileceği bir ayarlar ekranı olmalı.
- Değiştirilebilir tüm özellikler (köşe yarıçapı, arka plan rengi/opaklığı, blur yoğunluğu, şerit pozisyonu/kalınlığı/rengi, border, gölge — mümkün olan her şey) buradan kontrol edilebilmeli.
- Kullanıcı birden fazla **stil preset'i** oluşturabilmeli: **ekleme, düzenleme ve silme** desteklenmeli (isimlendirilmiş konfigürasyonlar olarak, örn. "Koyu Cam", "Pastel", "Yüksek Kontrast").
- Preset'ler cihazda kalıcı olarak saklanmalı (örn. DataStore/SQLite — proje genelinde SQLite tercihini kullan).

### 5. Kaydırma ile Kapatma Animasyonu (iOS Kilit Ekranı Bildirim Tarzı)
Kart, kullanıcı yukarı veya aşağı sürüklediğinde:
- Sürükleme mesafesiyle orantılı olarak küçülür (scale down)
- Sağa doğru kayar (translateX)
- Saydamlaşır (fade out)

iOS kilit ekranındaki bildirim geçişine benzer bir davranış hedefleniyor.

Davranış detayları:
- Bir eşik değeri (örn. dikey sürüklemenin **kart yüksekliğinin** ~%20'sini aşması) geçilirse kart spring animasyonla tamamen sağdan ekran dışına çıkar ve `onDismiss` callback'i tetiklenir (liste güncellenir).
- Eşik aşılmadan parmak kaldırılırsa kart spring animasyonla orijinal konum/scale/opacity'sine geri döner.
- Yön kısıtı (sadece yukarı / sadece aşağı / ikisi de), eşik oranı, animasyon süresi/spring sertliği geliştirici API'sinden konfigüre edilebilir olmalı (varsayılan makul değerlerle; ayarlar ekranından değil).
- Çıkış yönü her zaman sağadır; sürükleme yönü (yukarı/aşağı) yalnızca tetikleme koşulunu belirler.
- `onDismiss`, animasyon tamamen bittikten sonra (`onAnimationCompleted`) tetiklenir; liste güncellemesi kart ekrandayken yapılmaz (layout animasyonu çakışması önlenir).
- Dismiss animasyonu sürerken kart yeniden sürüklenemez (state: `DISMISSING`); hızlı art arda swipe'larda çift tetikleme olmaz.
- Dikey drag gesture liste scroll'u ezmez; gesture yalnızca kartın kendi alanında başlar ve scroll ile çakışırsa gesture tüketilir (`consume`).

Implementasyon:
- Android: `detectVerticalDragGestures` + birden fazla `Animatable` (offsetX, offsetY, scale, alpha) ile elle sürüklemeye bağlı animasyon.
- iOS (ileride): `DragGesture` + `withAnimation(.spring())` ile eşdeğer davranış.

İlgili token alanları (`design_tokens.json`): `dismissThreshold`, `dismissDuration`, `springStiffness`, `dismissDirection`.

### 6. Mimari Notlar
- Bileşeni ayrı bir **Gradle module** (örn. `:glasscard`) olarak yapılandır ki ileride bağımsız bir kütüphane gibi başka projelere de eklenebilsin.
- State management için **Riverpod eşdeğeri yok** (bu native Kotlin projesi) — Compose tarafında `ViewModel` + `StateFlow` kullan, feature-based klasör yapısına sadık kal.
- Ortak design-token şemasını ayrı bir dosyada (`design_tokens.json` veya benzeri) tut, hem dokümantasyon hem de gelecekteki iOS implementasyonu için referans olsun.

## Teslim Edilecekler (Bu Aşama İçin)
1. `:glasscard` Compose modülü: `GlassCard` composable + tüm parametreleri.
2. Demo Android uygulaması:
   - Örnek fotoğraflı/liste verisiyle dolu bir ana ekran (kartlar `GlassCard` ile render edilir, farklı veri tiplerine göre farklı şerit renkleri).
   - Gelişmiş ayarlar ekranı: tüm görsel özellikleri değiştirme + preset ekleme/düzenleme/silme.
3. Çalışan, kurulabilir bir **APK** çıktısı.
4. Kısa bir README: bileşenin parametreleri, varsayılan değerleri ve nasıl entegre edileceği.

## Sprint Planı (Kabul Kriterleriyle)

**Sprint 1 — Temel Bileşen**
- `GlassCard` composable'ı: corner radius, background opacity, blur intensity parametreleri çalışır durumda.
- Kabul kriteri: Farklı parametrelerle en az 3 varyasyon örnek ekranda yan yana görüntülenebiliyor.

**Sprint 2 — Renk Manşeti**
- Şerit pozisyonu (4 yön), kalınlık ve renk parametreleri eklenir.
- Kabul kriteri: Aynı kart, 4 farklı pozisyon ve özel renkle doğru şekilde render ediliyor; şerit kapalı durumda da sorunsuz çalışıyor.

**Sprint 3 — Demo Veri ve Liste Ekranı**
- Fotoğraflı örnek veri seti + liste ekranı entegre edilir, her veri tipi kendi şerit rengiyle gösterilir.
- Kabul kriteri: Liste akıcı scroll ediyor (blur nedeniyle performans sorunu yok), gerçek/örnek görseller doğru yükleniyor.

**Sprint 4 — Ayarlar Ekranı ve Preset Yönetimi**
- Canlı önizlemeli ayarlar ekranı + preset ekleme/düzenleme/silme + kalıcı saklama (SQLite/DataStore).
- Kabul kriteri: Bir preset oluşturulup uygulanabiliyor, kapatıp açınca ayar korunuyor, silinen preset listeden kayboluyor.

**Sprint 4.5 — Kaydırma ile Kapatma Animasyonu**
- Sürüklemeyle scale/translateX/fade + eşik üstünde spring ile sağdan çıkış ve `onDismiss`, eşik altında geri dönüş.
- Kabul kriteri: Kart eşik altında bırakılınca yumuşak şekilde geri dönüyor, eşik üstünde bırakılıp sağdan ekran dışına akıcı şekilde çıkıyor ve listeden kalkıyor; hızlı art arda swipe'larda animasyon takılmıyor/çakışmıyor; liste scroll'u ezilmiyor.

**Sprint 5 — Paketleme ve APK**
- Modülerleştirme (`:glasscard`), README, imzalı/debug APK üretimi.
- Kabul kriteri: APK bağımsız bir cihaza/emülatöre kurulup tüm özellikler (kart varyasyonları + ayarlar ekranı) hatasız çalışıyor.

---
Not: iOS/Swift implementasyonu bu prompt'un kapsamı dışında bırakılmıştır; design-token şeması ve mimari kararlar ileride SwiftUI + Liquid Glass (`.glassEffect`) tabanlı bir port için referans olacak şekilde belgelenmelidir.
