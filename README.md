# ChemiaAPP

Aplikacja mobilna na Androida do nauki chemii w formie quizu.

## Co to jest?

**CHemia** to quiz chemiczny z pytaniami wielokrotnego wyboru, różnymi trybami gry i rankingiem (Firebase).  
Pytania są ładowane z pliku JSON, co ułatwia ich edycję bez zmian w logice aplikacji.

## Funkcje

- Quiz chemiczny z pytaniami po polsku
- Losowe pytania i losowa kolejność
- Tryby gry (np. `quick` / `study` / `exam` / `blitz`)
- Ekran wyników po zakończeniu quizu
- Integracja z Firebase (ranking / dane użytkownika)
- Baza pytań w `app/src/main/assets/questions.json`

## Stack technologiczny

- Kotlin
- Android (Gradle Kotlin DSL)
- Firebase (Google Services, Firestore)
- Android SDK / Android Studio

## Wymagania

- Android Studio (najnowsza stabilna wersja)
- JDK zgodny z konfiguracją projektu
- Android SDK (w projekcie: `minSdk 24`, `targetSdk 36`)
- Dostęp do Internetu (dla funkcji Firebase)

## Szybki start

1. Sklonuj repozytorium.
2. Otwórz projekt w Android Studio.
3. Upewnij się, że konfiguracja Firebase jest poprawna (`google-services.json`).
4. Zbuduj i uruchom aplikację.

### Build z terminala (Windows PowerShell)

```powershell
Set-Location "C:\Users\dluka\AndroidStudioProjects\CHemia"
.\gradlew.bat :app:assembleDebug
```

APK debug znajdziesz zwykle w:  
`app\build\outputs\apk\debug\app-debug.apk`

## Build release APK

```powershell
Set-Location "C:\Users\dluka\AndroidStudioProjects\CHemia"
.\gradlew.bat :app:assembleRelease
```

APK release znajdziesz zwykle w:  
`app\build\outputs\apk\release\app-release.apk`

> Uwaga: build release może zatrzymać się na lint, jeśli wykryje błąd layoutu lub inny błąd krytyczny.

## Struktura projektu (skrót)

- `app/src/main/java/...` — kod aplikacji (Activity, repozytoria, modele)
- `app/src/main/res/` — layouty, kolory, style, zasoby UI
- `app/src/main/assets/questions.json` — baza pytań i odpowiedzi
- `app/build.gradle.kts` — konfiguracja modułu app
- `build.gradle.kts` — konfiguracja główna
- `google-services.json` — konfiguracja Firebase

## Pytania i odpowiedzi (JSON)

Pytania są przechowywane w:  
`app/src/main/assets/questions.json`

Każdy wpis zawiera m.in.:

- `id`
- `text`
- `category`
- `level`
- `options`
- `correctAnswerIndex`
- `explanation`

## Firebase

Projekt używa Firebase, więc sprawdź:

- `google-services.json` (root i/lub `app/`, zgodnie z konfiguracją)
- reguły Firestore (`firestore.rules`)
- indeksy (`firestore.indexes.json`)

## Troubleshooting

### `adb` nie jest rozpoznawane

Jeśli terminal zwraca `adb is not recognized`:

- dodaj `platform-tools` do `PATH`, lub
- używaj terminala wbudowanego Android Studio (często ma poprawne środowisko).

### Build release failuje na lint

Uruchom najpierw:

```powershell
Set-Location "C:\Users\dluka\AndroidStudioProjects\CHemia"
.\gradlew.bat :app:lint
```

Napraw wskazane błędy i ponów:

```powershell
Set-Location "C:\Users\dluka\AndroidStudioProjects\CHemia"
.\gradlew.bat :app:assembleRelease
```

## Licencja

APACHE LICENSE 2.0
