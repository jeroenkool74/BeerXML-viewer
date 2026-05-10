# Google Play Console Content

This file contains draft Play Console content. The Play Console itself still needs to be completed manually in the developer account.

## App Bundle

Google Play Console does not generate the `.aab` for you. Build and upload the release App Bundle from this project. Play then verifies the bundle and generates optimized APKs for users.

Recommended local command:

```bash
./gradlew bundleRelease
```

## Privacy Policy URL

Publish `PRIVACY_POLICY.md` at a public HTTPS URL and enter that URL in Play Console.

Prepared GitHub Pages URL: `https://jeroen74.github.io/BeerXML-viewer/privacy-policy/`

Recommended option: GitHub Pages for this repository using the `docs/` folder on the `main` branch. The URL must be public, reachable without login, and should stay stable after release.

Other acceptable options: your own website, Netlify, Cloudflare Pages, or another static hosting provider with HTTPS. Avoid temporary links, private GitHub URLs, Google Drive links, or anything requiring sign-in.

## Feature Graphic

Use `play-store/feature-graphic.png` for the Google Play feature graphic. It is exported at `1024 x 500`; the editable source is `play-store/feature-graphic.svg`.

## Open-Source Notices

The release app distributes ftp4j 1.7.2 under GNU LGPL 2.1. Keep `THIRD_PARTY_NOTICES.md` and `LICENSES/LGPL-2.1.txt` with the release materials, and provide the upstream ftp4j 1.7.2 source archive link before publishing.

## English Listing

Short description:

BeerXML viewer for opening local BeerXML files and syncing exports from your FTP/FTPS server.

Full description:

BeerXML viewer lets homebrewers view BeerXML data on Android. Open BeerXML files from your device or configure your own FTP, FTPES, or FTPS server to download exported brewing data directly into the app.

Features:
- View hops, fermentables, yeasts, misc ingredients, water profiles, equipment, styles, mash profiles, recipes, and brews.
- Import local BeerXML files.
- Download BeerXML export files from your configured FTP/FTPS server.
- Supports English and Dutch using Android system language settings.
- FTP credentials stay on your device in encrypted app storage.

## Dutch Listing

Short description:

BeerXML-viewer voor lokale BeerXML-bestanden en synchronisatie via je FTP/FTPS-server.

Full description:

BeerXML viewer helpt hobbybrouwers BeerXML-gegevens op Android te bekijken. Open BeerXML-bestanden vanaf je apparaat of stel je eigen FTP-, FTPES- of FTPS-server in om geëxporteerde brouwgegevens direct in de app te downloaden.

Functies:
- Bekijk hop, vergistbare ingrediënten, gist, diversen, waterprofielen, apparatuur, stijlen, maischschema's, recepten en brouwsels.
- Importeer lokale BeerXML-bestanden.
- Download BeerXML-exportbestanden vanaf je ingestelde FTP/FTPS-server.
- Ondersteunt Nederlands en Engels via de Android-systeemtaal.
- FTP-inloggegevens blijven op je apparaat in versleutelde appopslag.

## Data Safety Draft

Data collected by developer: No.

Data shared with third parties by developer: No.

Data processed locally:
- FTP server address, path, username, and password.
- BeerXML files and parsed BeerXML app data.
- App preferences.

Network behavior:
- The app sends FTP credentials only to the user-configured FTP/FTPES/FTPS server for login.
- The app downloads BeerXML files only from the user-configured server.

Security practices:
- FTP settings are stored in encrypted Android app storage.
- FTP credentials are excluded from Android backup rules.
- Users can delete local data by clearing app storage or uninstalling the app.

## Content Rating Notes

Expected rating: suitable for general audiences. The app displays brewing recipe data provided by the user and does not include social features, gambling, ads, or user-generated public content.

## App Access

No account is required to use the app. FTP sync requires a user-provided FTP/FTPES/FTPS server.

## Screenshots Needed

Prepare screenshots for:
- Main list screen with loaded BeerXML data.
- Settings screen with FTP help and privacy card.
- Download progress banner.
- English and Dutch locale if you plan localized store listings.
