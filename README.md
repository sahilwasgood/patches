# Patches

Preset icon packs, headers and button styles for YouTube and YT Music.

<p align="center">
  <a href="https://morphe.software/add-source?github=sahilwasgood/patches">
    <img src=".github/assets/add-to-manager.svg" alt="Add to Morphe" width="380">
  </a>
</p>

## Preview

### YouTube

<img src=".github/assets/preview-yt.png" alt="YouTube logo pack previews" width="720">

### YT Music

<img src=".github/assets/preview-ytm.png" alt="YT Music logo pack previews" width="720">

## Quickstart

1. Tap the button above to add this source in Morphe Manager.
2. Pick YouTube or YT Music and select the patches.
3. Choose a preset in each App icon or Button style option, then patch and install.

<details>
<summary>Contents, patch list, custom art, build, license</summary>

## Included

Logo packs. 21 for YouTube and 14 for YT Music, including Vanced Light and
Vanced Black. The notification icon matches the pack, never stock.

Overlay buttons. Bold, Rounded and Thin player button styles for YouTube.

Shorts buttons. Cairo, Outline, Outline Circle, Round and YouTube Outline
styles for YouTube Shorts. Stock leaves the app untouched.

## Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.2.1](https://github.com/sahilwasgood/patches/releases/tag/v1.2.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 YouTube Music&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;9.40.51 | 🧪&nbsp;9.39.52 | 🧪&nbsp;9.38.51 | 9.20.53 | 9.15.51 |
| :---: | :---: | :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Logo packs](#logo-packs) | Applies a preset app icon (all packs incl. Vanced Light). Turn off Morphe's Custom branding patch, both set the launcher icon. | • App icon<br>• Custom icon |

</details>

<details open>
<summary>📦 YouTube&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;21.41.525 | 🧪&nbsp;21.40.161 | 🧪&nbsp;21.39.522 | 🧪&nbsp;21.38.130 | 🧪&nbsp;21.28.208 | 🧪&nbsp;21.23.492 | 21.20.405 | 21.16.256 | 20.51.39 | 20.21.37 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Logo packs](#logo-packs) | Applies a preset app icon (all packs incl. Vanced Light). Turn off Morphe's Custom branding patch, both set the launcher icon. Use Custom icon with a folder for your own art. | • App icon<br>• Custom icon |
| [Overlay button style](#overlay-button-style) | Player overlay button style (Bold, Rounded, Thin). Covers different buttons than Morphe's Player icon style, keep both on. Files that reference renamed drawables are skipped, so new app versions degrade to fewer styled buttons instead of failing. | • Button style |
| [Shorts button style](#shorts-button-style) | Shorts action button style. Stock leaves the app untouched. Turn off Morphe's Shorts icon style patch, both style the same buttons. | • Button style |

</details>

<!-- PATCHES_END -->

## Custom art

Bring your own icon folder and header folder instead of a preset.
See the Custom icon and Custom header options inside each patch. Ready made
Vanced Light folders live in folders/ in this repo.

## Build it yourself

Needs Java 21 and `./gradlew buildAndroid`. The bundle lands in
`patches/build/libs`. Details live in the
[Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

## License

GPLv3. See [LICENSE](LICENSE).

</details>
