# Release Publishing

This project publishes platform releases with
[`Kir-Antipov/mc-publish`](https://github.com/Kir-Antipov/mc-publish). The
workflow files are the source of truth for project IDs, loaders, game versions,
and release metadata:

- `.github/workflows/publish.yml` builds the plugin and publishes when a `v*`
  tag is pushed.
- `.github/workflows/publish-existing-release.yml` manually republishes an
  already-built GitHub release asset to Modrinth and CurseForge.
- `.github/workflows/verify-platform-release.yml` checks public platform APIs
  for a released version.

## Platform IDs And Secrets

Current platform IDs:

- Modrinth project: `Pool7IDt`
- CurseForge project: `1435207`

Required GitHub Actions secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

The tag-triggered workflow also uses `GITHUB_TOKEN` to create/update the GitHub
release through mc-publish.

## Compatibility Versions

mc-publish does support compatibility tagging through the `game-versions` input.
It does not infer new Minecraft versions automatically, so update the list before
publishing a release.

Keep the `game-versions` list in both publish workflows aligned:

- `.github/workflows/publish.yml`
- `.github/workflows/publish-existing-release.yml`

Before publishing, check both platform version catalogs and include every release
version that AcuteLoot supports. Do not rely on only the latest patch version if
intermediate releases exist.

Useful checks:

```sh
curl -fsSL https://api.modrinth.com/v2/tag/game_version \
  | jq -r '.[] | select(.version_type == "release") | .version' \
  | head -30
```

```sh
curl -fsSL https://api.curse.tools/v1/cf/minecraft/version \
  | jq -r '.data[].versionString' \
  | head -30
```

For the 2.16.0 release, `1.21.11` was the previous latest supported version and
the release versions after it were:

- `26.1`
- `26.1.1`
- `26.1.2`

Snapshot/pre-release entries such as `26.2-pre-*` should not be added to normal
release publishing unless the plugin is intentionally being marked compatible
with snapshots.

## Publish Paths

### Normal Tag Release

Use `.github/workflows/publish.yml` when the build works in a clean GitHub
Actions environment.

High-level process:

1. Update release metadata and compatibility versions in the workflow.
2. Build and test locally.
3. Commit and push to `master`.
4. Create and push a `v*` tag.
5. Watch the `Publish Release to GitHub, Modrinth, and CurseForge` workflow.

Useful commands:

```sh
mvn clean test
mvn package -DskipTests
git status --short --branch
git tag vX.Y.Z
git push origin master vX.Y.Z
gh run watch --exit-status
```

### Republish Existing GitHub Release Asset

Use `.github/workflows/publish-existing-release.yml` when the GitHub release jar
already exists and should be uploaded or re-uploaded to Modrinth and CurseForge
without rebuilding. This is useful if platform metadata was wrong or if the
tag-triggered build failed for an environmental/dependency reason.

First make sure the GitHub release asset exists:

```sh
gh release view vX.Y.Z --json tagName,name,assets
```

Then run the manual workflow:

```sh
gh workflow run publish-existing-release.yml \
  --ref master \
  -f tag=vX.Y.Z \
  -f version=X.Y.Z \
  -f file=AcuteLoot-X.Y.Z.jar
```

Watch it:

```sh
gh run watch --exit-status
```

Inspect logs if needed:

```sh
gh run view RUN_ID --log
```

A successful mc-publish run should include messages like:

- `Successfully published assets to CurseForge`
- `Successfully published assets to Modrinth`
- `Successfully published the assets to CurseForge, Modrinth`

If re-uploading the same version, delete the old platform version first where
required by the platform UI/API. CurseForge uploads may enter review before they
become publicly visible.

## Verify A Publish

### GitHub Actions

Run the verification workflow:

```sh
gh workflow run verify-platform-release.yml --ref master -f version=X.Y.Z
gh run watch --exit-status
```

This workflow verifies:

- Modrinth via `https://api.modrinth.com/v2/project/Pool7IDt/version`
- CurseForge public listing via `https://api.curse.tools/v1/cf/mods/1435207/files`

CurseForge files under review may not appear in the public listing yet. In that
case, the verification workflow can fail even though mc-publish uploaded
successfully. Check the CurseForge dashboard for review status.

### Manual Modrinth Check

```sh
curl -fsSL https://api.modrinth.com/v2/project/Pool7IDt/version \
  | jq --arg version "X.Y.Z" '
      map(select(.version_number == $version))[0]
      | {
          id,
          name,
          version_number,
          status,
          featured,
          date_published,
          game_versions,
          loaders,
          files: [.files[] | {filename, size, url}]
        }'
```

Confirm:

- `version_number` is correct.
- `status` is `listed`.
- `game_versions` includes every supported release version.
- `loaders` includes `spigot`, `paper`, and `purpur`.
- The jar filename and size match the GitHub release asset.

### Manual CurseForge Check

```sh
curl -fsSL https://api.curse.tools/v1/cf/mods/1435207/files \
  | jq --arg version "X.Y.Z" '
      .data
      | map(select(.displayName == $version or .fileName == ("AcuteLoot-" + $version + ".jar")))[0]
      | {
          id,
          displayName,
          fileName,
          isAvailable,
          fileStatus,
          fileDate,
          fileLength,
          downloadUrl,
          gameVersions
        }'
```

Confirm:

- A matching file is present after CurseForge review completes.
- `isAvailable` is `true`.
- `gameVersions` includes every supported release version.
- `fileLength` matches the GitHub release asset size.

## Troubleshooting

- If the tag-triggered workflow fails during Maven dependency resolution, the
  manual existing-release workflow can still publish a known-good GitHub release
  jar because it does not run Maven.
- If Modrinth publishes but CurseForge is not visible publicly, check whether the
  CurseForge file is still under review.
- If a compatibility version is missing, delete or edit the platform release as
  allowed by the platform, update `game-versions` in both publish workflows, and
  re-run the manual existing-release workflow.
- If mc-publish behavior changes, check whether a newer `Kir-Antipov/mc-publish`
  version is available and update both publish workflows together.
