# AGENTS.md — Guide pour agents de code

## Qu'est-ce que ce projet

NewsPerso : application Android de veille média personnelle. Agrégateur de flux RSS
(l'utilisateur choisit ses médias) + lecteur de webradios. Application 100 % locale,
aucun compte, aucune donnée envoyée ailleurs que vers les flux/streams choisis.

- Propriétaire du projet : LordSeranox (utilisateur non-développeur, francophone)
- Repo : github.com/LordSeranox/NewsPerso
- App installée sur un Samsung Galaxy S22

## Stack et conventions

- Kotlin, vues XML + ViewBinding (pas de Compose), Material 3 DayNight
- `compileSdk`/`targetSdk` 35, `minSdk` 31, JDK 17, AGP 8.5.2, Kotlin 1.9.24
- Réseau : threads bruts + `runOnUiThread` (pas de coroutines)
- Persistance : SharedPreferences + JSON (pas de Room)
- Langue FR par défaut + traduction complète dans `values-en/strings.xml`
  (⚠️ le fichier FR contient des échappements `\u2019` littéraux pour les apostrophes — convention à respecter)
- Thème : couleurs `np_*` dupliquées dans `values/` (clair) et `values-night/` (sombre)
- Palette accent : vert sarcelle `#0E7C6B` (clair) / `#35C4A8` (sombre)
- Images : Coil pour les favicons, repli avatar à initiale (`ui/LogoHelper`)

## Architecture

- `MainActivity` — hôte : ViewPager2 (slide Actualités <-> Radio), mini-player en bas
- `NewsFragment` — liste d'articles (pull-to-refresh, recherche, filtre favoris, aperçu dépliable)
- `RadioFragment` — liste des radios, contrôle du service
- `RadioService` — service foreground : MediaPlayer + notification, lecture en tâche de fond
- `SettingsActivity` — gestion flux/radios, langue, thème, purge favoris
- `rss/RssParser` — parsing XML (formats RSS 2.0 et Atom)
- `rss/RssFetcher` — téléchargement HTTP des flux
- `data/SourcesRepository` — flux et radios de l'utilisateur (préremplis FR)
- `data/FavoritesRepository` — articles « À lire plus tard »

## Règles pour toute modification

1. Toute nouvelle string doit être ajoutée dans `values/strings.xml` **et** `values-en/strings.xml`
2. Toute nouvelle couleur doit exister dans `values/` **et** `values-night/`
3. Pas de nouvelle dépendance sans justification forte
4. Vérifier la cohérence : `grep R.string` vs fichiers strings avant de pousser
5. L'utilisateur installe les APK par-dessus la version existante : **ne jamais casser
   le format des préférences** (compatibilité ascendante obligatoire)
6. Envoyer les changements par commit direct sur `main` (workflow établi avec l'utilisateur)

## Compilation

```bash
./gradlew assembleDebug   # APK dans app/build/outputs/apk/debug/
```

⚠️ Environnement de l'utilisateur : Android Studio récent avec JBR 25 par défaut —
le JDK Gradle doit être réglé sur un JDK 21 (`ms-21` dans `C:/Users/pierr/.jdks/`)
sinon le daemon Kotlin 1.9 plante (IllegalArgumentException: 25.0.4).
