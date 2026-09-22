package com.kartview.app.data

import com.kartview.app.data.model.DataType
import com.kartview.app.data.model.DemoItem

object DemoData {
    val items: List<DemoItem> = listOf(
        DemoItem(
            id = 1,
            title = "Sahil yürüyüşü",
            subtitle = "Gün batımında Kadıköy sahilinde uzun bir yürüyüş.",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/beach1/600/400",
        ),
        DemoItem(
            id = 2,
            title = "Haftalık özet",
            subtitle = "Sprint planı tamamlandı, release 0.2 yolda.",
            type = DataType.Note,
            imageUrl = null,
        ),
        DemoItem(
            id = 3,
            title = "Yakındaki kahve dükkanları",
            subtitle = "Kazasker bölgesinde en iyi 5 mekan.",
            type = DataType.Location,
            imageUrl = null,
        ),
        DemoItem(
            id = 4,
            title = "Dağ manzarası",
            subtitle = "Hafta sonu keşfedilen kamp rotası.",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/mountain4/600/400",
        ),
        DemoItem(
            id = 5,
            title = "Kitap listesi",
            subtitle = "Bu ay okunacak üç kitap: Compose, System Design, Solo Developer.",
            type = DataType.Note,
            imageUrl = null,
        ),
        DemoItem(
            id = 6,
            title = "Toplantı notları",
            subtitle = "Cam CardView bileşeni üzerine mimari kararlar.",
            type = DataType.Note,
            imageUrl = null,
        ),
        DemoItem(
            id = 7,
            title = "Grafik bahanesi",
            subtitle = "Göl kenarında piknik ve mangal planı.",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/lake7/600/400",
        ),
        DemoItem(
            id = 8,
            title = "Konum: Bostancı",
            subtitle = "Yol tarifi ve park noktaları.",
            type = DataType.Location,
            imageUrl = null,
        ),
        DemoItem(
            id = 9,
            title = "Sonbahar fotoları",
            subtitle = "Moda sahili kareleri.",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/autumn9/600/400",
        ),
        DemoItem(
            id = 10,
            title = "Kamp ateşi",
            subtitle = "Hafta sonu orman kampı ve mangal planı.",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/camp10/600/400",
        ),
        DemoItem(
            id = 11,
            title = "Alışveriş listesi",
            subtitle = "Market için eksik malzemeler.",
            type = DataType.Note,
            imageUrl = null,
        ),
        DemoItem(
            id = 12,
            title = "Konum: Adalar",
            subtitle = "Vapur tarifesi ve bisiklet rotası.",
            type = DataType.Location,
            imageUrl = null,
        ),
        DemoItem(
            id = 13,
            title = "Yağmurlu şehir",
            subtitle = "İstanbul yağmurda nasıl daha güzel olmaz?",
            type = DataType.Photo,
            imageUrl = "https://picsum.photos/seed/rain13/600/400",
        ),
        DemoItem(
            id = 14,
            title = "Mimari notlar",
            subtitle = "Glass CardView mimari karar kaydı.",
            type = DataType.Note,
            imageUrl = null,
        ),
    )
}