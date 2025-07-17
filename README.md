# Courier Tracking Service (Kurye Takip Servisi)

Bu proje, bir teknik değerlendirme senaryosu için geliştirilmiş, kuryelerin anlık coğrafi konumlarını işleyen, belirli iş kurallarına göre aksiyonlar alan ve sipariş atamaları yapan bir Restful web uygulamasıdır. Uygulama, modern Java ve Spring Boot pratikleri kullanılarak tasarlanmış ve Docker ile konteynerize edilmiştir.

## Projenin Ana Yetenekleri

-   **Anlık Konum Takibi**: Kuryelerin anlık `(lat, lng)` konumlarını REST API üzerinden alarak işler.
-   **Mağazaya Giriş Tespiti**: Kuryelerin, sisteme kayıtlı Migros mağazalarının 100 metrelik yarıçapına girdiğini tespit eder ve loglar.
    -   *İş Kuralı*: Bir kuryenin aynı mağaza yakınına 1 dakika içindeki tekrar girişleri loglanmaz.
-   **Toplam Mesafe Hesaplama**: Her kuryenin katettiği toplam mesafeyi (kuş uçuşu) anlık olarak hesaplar ve sorgulanabilir bir endpoint üzerinden sunar.
-   **Akıllı Sipariş Atama**: Yeni bir sipariş geldiğinde, müşterinin konumuna en yakın mağazayı, o mağazaya en yakın ve müsait olan kuryeyi bularak siparişi otomatik olarak atar.
-   **Güvenilir Asenkron İşleme**: Asenkron işlemlerde (örn: mağazaya giriş tespiti) meydana gelebilecek anlık hatalara karşı, başarısız olan olayları (events) veritabanına kaydedip periyodik olarak yeniden deneyen bir retry mekanizmasına sahiptir.

## Teknolojiler ve Mimarî Yaklaşımlar

-   **Framework**: Spring Boot 3.3.1
-   **Dil**: Java 21
-   **Veritabanı**: H2 In-Memory Database
-   **API Dokümantasyonu**: Springdoc OpenAPI (Swagger UI v3)
-   **Veri Dönüşümü**: MapStruct
-   **Derleme Aracı**: Apache Maven
-   **Konteynerizasyon**: Docker

### Uygulanan Tasarım Desenleri

1.  **Observer Pattern (Gözlemci Deseni)**: Servisler arası gevşek bağlılığı (decoupling) sağlamak amacıyla `ApplicationEventPublisher` kullanılmıştır. `CourierService`, bir konum güncellemesi yaptığında bir `CourierLocationEvent` yayınlar. Diğer servisler (`StoreService`, `OrderService`) bu olayı dinleyerek kendi iş mantıklarını tetikler.
2.  **DTO (Data Transfer Object) Pattern**: API katmanı ile servis katmanı arasında veri taşıma nesneleri kullanılarak, veritabanı entity'leri dış dünyaya kapalı tutulmuş, güvenlik ve esneklik artırılmıştır.

## Kurulum ve Çalıştırma

Projeyi çalıştırmak için iki yöntem bulunmaktadır. **Tavsiye edilen yöntem Docker kullanmaktır.**

### Yöntem 1: Docker ile Çalıştırma (Tavsiye Edilen)

Bu yöntem, bilgisayarınızda Java veya Maven kurulu olmasını gerektirmez. Sadece Docker Desktop yeterlidir.

**Gereksinimler:**
-   [Docker Desktop](https://www.docker.com/products/docker-desktop/)'ın kurulu ve çalışır durumda olması.

**Adımlar:**

1.  **Projeyi Klonlayın:**
    ```bash
    git clone <https://github.com/gurkanturann/courier-tracking-system>
    cd courier-tracker
    ```

2.  **Docker İmajını Oluşturun ve Konteyneri Başlatın:**
    Proje ana dizininde aşağıdaki `docker-compose` komutunu çalıştırın. Bu komut, `Dockerfile`'ı kullanarak imajı oluşturacak ve konteyneri arka planda başlatacaktır.
    ```bash
    docker-compose up --build -d
    ```

Uygulama başarıyla başladığında, `http://localhost:8081` adresi üzerinden erişilebilir olacaktır.

### Yöntem 2: Lokal Ortamda (Maven ile) Çalıştırma

**Gereksinimler:**
-   JDK 21
-   Apache Maven 3.6+

**Adımlar:**

1.  **Projeyi Klonlayın:**
    ```bash
    git clone <projenin-github-linki>
    cd courier-tracker
    ```

2.  **Projeyi Derleyin:**
    ```bash
    mvn clean install
    ```

3.  **Uygulamayı Çalıştırın:**
    ```bash
    java -jar target/courier-tracker-0.0.1-SNAPSHOT.jar
    ```

## API Dokümantasyonu ve Test

Uygulama çalışırken, tüm API endpoint'lerini interaktif bir arayüz üzerinden görmek ve test etmek için Swagger UI'ı kullanabilirsiniz.

-   **Swagger UI Adresi**: `http://localhost:8081/swagger-ui.html`

### Ana API Endpoint'leri

Aşağıda projenin ana API endpoint'lerinin bir özeti bulunmaktadır.

| Metot  | URL Yolu                                   | Açıklama                                                                |
| :----- | :----------------------------------------- | :---------------------------------------------------------------------- |
| `POST` | `/api/v1/courier/create-courier`           | Yeni bir kurye oluşturur.                                               |
| `GET`  | `/api/v1/courier`                          | Sistemdeki tüm kuryeleri sayfalama yaparak listeler.                    |
| `POST` | `/api/v1/courier/couriers/{id}/move`       | Belirtilen kuryenin konumunu günceller.                                 |
| `GET`  | `/api/v1/courier/couriers/{id}/get-total-distance` | Belirtilen kuryenin katettiği toplam mesafeyi döner.                  |
| `POST` | `/api/v1/orders`                           | Yeni bir sipariş oluşturur ve en uygun kurye/mağazaya atar.           |
| `GET`  | `/api/v1/store/entrances`                  | Kuryelerin mağazalara tüm giriş loglarını listeler.                     |

#### Örnek İstekler

-   **Tüm Kuryeleri Listeleme (Sayfalama ile):**
    ```http
    GET http://localhost:8081/api/v1/courier?page=0&size=5&sort=name,asc
    ```

-   **Konum Güncelleme:**
    ```http
    POST http://localhost:8081/api/v1/courier/couriers/1/move
    Content-Type: application/json

    {
      "lat": 41.05,
      "lng": 29.02
    }
    ```
## Veritabanı Konsolu

Uygulama çalışırken, H2 In-Memory veritabanının içeriğini tarayıcı üzerinden inceleyebilirsiniz.

-   **H2 Console Adresi**: `http://localhost:8081/h2-console`
-   **JDBC URL**: `jdbc:h2:mem:courierdb`
-   **Username**: `migros`
-   **Password**: `siparis`

## Unit Testleri Çalıştırma

Projenin tüm unit testlerini çalıştırmak için ana dizinde aşağıdaki komutu kullanabilirsiniz:
```bash
mvn test
```