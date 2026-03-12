# Java Flower Shop

Ung dung JavaFX quan ly va ban hoa.

## Yeu cau

- JDK 17 tro len
- Khong can cai Maven rieng, repo da kem Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Chay sau khi clone

### Windows

1. Cai JDK 17+.
2. Dat bien moi truong `JAVA_HOME` tro toi thu muc JDK.
3. Chay:

```bat
run.bat
```

Hoac chay truc tiep bang Maven Wrapper:

```bat
.\mvnw.cmd -q -DskipTests clean javafx:run
```

### macOS / Linux

```bash
chmod +x run.sh
./run.sh
```

Hoac chay truc tiep bang Maven Wrapper:

```bash
./mvnw -q -DskipTests clean javafx:run
```

## Build

```bat
.\mvnw.cmd -q -DskipTests clean package
```

## Luu y

- Du an duoc cau hinh de build bang UTF-8 va target Java 17 de de chay tren nhieu may hon.
- Neu can dong goi ban portable cho Windows, dung script build-exe.bat.