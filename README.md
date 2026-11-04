# NewsPerso

Application Android de veille média personnelle : **vos flux RSS + vos webradios**, rien d'autre. 100 % locale, sans compte, sans traçage.

## Fonctionnalités

### 📰 Actualités
- Agrégateur RSS/Atom : les médias **français majeurs** sont préinstallés (Le Monde, France Info, France 24, BFM TV, Courrier International)
- Ajout/suppression de vos propres flux dans les paramètres
- **Pull-to-refresh**, articles triés du plus récent au plus ancien
- Recherche instantanée (titre + source)
- Dates relatives (« il y a 2 h ») et **temps de lecture estimé**
- **À lire plus tard** : marquez des articles d'une étoile, filtrez-les, purgez la liste
- Ouverture dans le navigateur, **partage** natif

### 📻 Radio
- Webradios préinstallées : FIP, France Inter, France Info, France Culture, Mouv', Radio Nova
- Ajout de vos propres flux audio (URL de stream)
- Lecture/pause d'un tap, indicateur « en direct », gestion de la mise en mémoire tampon

### ⚙️ Paramètres
- Gestion des flux RSS et des radios
- Langue : **Français** (défaut) / English
- Thème : système / sombre / clair (Material 3 DayNight)

## Compilation

Prérequis : Android Studio (Koala+), JDK 17/21 (⚠️ pas de JDK 25 avec Kotlin 1.9), SDK Android 35.

```bash
./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk
```

Ou dans Android Studio : **Build → Build APK(s)**.

## Installation sur le S22

Activer « Installer des apps inconnues » pour votre navigateur, transférer l'APK, installer. Les mises à jour s'installent par-dessus sans perte de données (flux, radios, favoris conservés).

## Notes techniques

- Parsing RSS 2.0 et Atom via `XmlPullParser`, sans dépendance externe
- Lecture audio via `MediaPlayer` (flux Icecast/Shoutcast testés)
- Données stockées en local (SharedPreferences + JSON), aucune donnée collectée
- `AGENTS.md` présent à la racine : guide pour tout agent de code intervenant sur le projet
