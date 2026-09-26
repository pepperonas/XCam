// Generates app/src/main/java/io/celox/xcam/ui/theme/Color.kt from the brand seed.
// Usage (the package ships ESM without file extensions, so bundle it first):
//   npm i @material/material-color-utilities
//   npx esbuild tools/color-scheme.mjs --bundle --platform=node --outfile=/tmp/cs.cjs && node /tmp/cs.cjs > app/src/main/java/io/celox/xcam/ui/theme/Color.kt
import {argbFromHex, hexFromArgb, Hct, SchemeTonalSpot, SchemeVibrant, MaterialDynamicColors as M} from '@material/material-color-utilities';
const SEED='#E5484D';
const seed = Hct.fromInt(argbFromHex(SEED));
const accent=['primary','onPrimary','primaryContainer','onPrimaryContainer','inversePrimary','secondary','onSecondary','secondaryContainer','onSecondaryContainer','tertiary','onTertiary','tertiaryContainer','onTertiaryContainer','surfaceTint','error','onError','errorContainer','onErrorContainer'];
const neutral=['background','onBackground','surface','onSurface','surfaceVariant','onSurfaceVariant','inverseSurface','inverseOnSurface','outline','outlineVariant','scrim','surfaceBright','surfaceContainer','surfaceContainerHigh','surfaceContainerHighest','surfaceContainerLow','surfaceContainerLowest','surfaceDim'];
let out=`package io.celox.xcam.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * XCam colour scheme, generated once from the brand seed with material-color-utilities
 * via tools/color-scheme.mjs (not a runtime dependency).
 *
 *  - Accent roles (primary/secondary/tertiary/error + containers) come from SchemeVibrant — a
 *    saturated "record red" the app is recognisable by.
 *  - Neutral roles (background, surface*, outline, inverse) come from SchemeTonalSpot of the same
 *    seed: calmer surfaces, so a large dark screen does not glow red at night.
 *
 * Regenerate instead of hand-editing single values — the roles are tuned against each other for
 * contrast (on* roles meet WCAG AA on their container).
 */

/** The brand seed every role derives from. */
val XCamSeed = Color(0xFF${SEED.slice(1)})

`;
for (const dark of [false,true]) {
  const v = new SchemeVibrant(seed, dark, 0), t = new SchemeTonalSpot(seed, dark, 0);
  out += `val ${dark?'XCamDarkColors':'XCamLightColors'} = ${dark?'darkColorScheme':'lightColorScheme'}(\n`;
  for (const r of accent) out += `    ${r} = Color(0xFF${hexFromArgb(M[r].getArgb(v)).slice(1).toUpperCase()}),\n`;
  for (const r of neutral) out += `    ${r} = Color(0xFF${hexFromArgb(M[r].getArgb(t)).slice(1).toUpperCase()}),\n`;
  out += `)\n\n`;
}
process.stdout.write(out.trimEnd()+'\n');
