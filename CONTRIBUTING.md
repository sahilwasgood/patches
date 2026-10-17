# Contributing

Small repo, few rules.

- Make changes on the `dev` branch. `main` follows `dev` after each stable release.
- Use semantic commit messages: `feat:`, `fix:`, `chore:`. `feat` and `fix` create releases, `chore` does not.
- Do not hand edit generated files: `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`. The release workflow rewrites them.
- Patches are proven against a real device before a stable release. Release notes state which app versions were verified.
- Assets in this bundle must be original or license clean. Pack art and preview images here are original work.
