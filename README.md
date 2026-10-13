# Patches

Patches for apps I use. Starting set: preset branding packs for YouTube and
YT Music (launcher logo packs including Vanced Light, player overlay button
styles, Shorts button styles). Static v1 — everything applies at patch time.
Compatible with Morphe.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=sahilwasgood/patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/sahilwasgood/patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 YT Music&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Logo packs](#logo-packs) | Applies a preset app icon (all packs incl. Vanced Light). | • App icon<br>• Custom icon |

</details>

<details open>
<summary>📦 YouTube&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Logo packs](#logo-packs) | Applies a preset app icon (all packs incl. Vanced Light). Use Custom icon with a folder for your own art. | • App icon<br>• Custom icon |
| [Overlay button style](#overlay-button-style) | Player overlay button style (Bold / Rounded / Thin). | • Button style |
| [Shorts button style](#shorts-button-style) | Shorts action button style. Stock leaves the app untouched. | • Button style |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Patches are licensed under the [GNU General Public License v3.0](LICENSE)
