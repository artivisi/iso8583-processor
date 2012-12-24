# Prosesor untuk protokol ISO-8583 #

## Apa itu ISO-8583 ##

ISO-8583 adalah protokol yang biasa digunakan untuk transaksi finansial.
Lebih jelas mengenai protokol ISO-8583 bisa dibaca di [Wikipedia](http://en.wikipedia.org/wiki/ISO_8583)

## Latar Belakang Pembuatan ##
Sebenarnya ada library open source untuk melakukan hal ini, yaitu [jPOS](http://jpos.org/),
tetapi ada beberapa keterbatasan yang kami temui, diantaranya:

* Lisensinya AGPL, tidak bisa digunakan di aplikasi komersil yang tidak open-source

* Banyak fitur-fitur yang tidak kami gunakan, diantaranya:

    * jPOS-EE
    * Karakter selain ASCII
    * dan sebagainya

  sehingga praktis yang digunakan hanyalah parser dari String ke ISO-8583 dan sebaliknya.

* Konfigurasi hanya bisa di XML dan Java, tidak bisa di database.

## Modul ##

* iso8583-processor-core : Class utama dengan dependensi minimal
* iso8583-processor-persistence : Support class untuk menyimpan dan mengambil konfigurasi mapping dari database. Menggunakan Spring Data JPA
* iso8583-processor-restful : Support class untuk mengakses konfigurasi mapping di database melalui HTTP dengan protokol REST

## Fitur Existing ##

* Konversi dari String ke ISO-8583
* Konversi dari ISO-8583 ke String

## Roadmap ##

* Menyimpan konfigurasi mapping di database
* User Interface untuk konfigurasi mapping
* Parsing untuk sub-data-element (misalnya bit 48, 60, dsb)
* Memproses data binary (misalnya untuk pinblock)

## Tidak akan dibuat ##

* Interfacing untuk TCP/IP.
  Silahkan gunakan library lain yang lebih baik
  seperti :
    * [Apache Mina](http://mina.apache.org/)
    * [JBoss Netty](https://netty.io/)
    * [Spring Integration](http://static.springsource.org/spring-integration/reference/htmlsingle/#ip)

* Karakter selain ASCII

## Kontak dan Kontribusi ##

Email ke endy.muhardin@gmail.com
