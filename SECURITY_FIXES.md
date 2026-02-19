# Security Fixes - 2026-02-19

## Overview

Resolved 13 of 17 reported dependency vulnerabilities across Ruby (Gemfile.lock) and
Node.js (package-lock.json, ui/package-lock.json) dependency trees.

## Ruby Gems (Gemfile.lock)

### rack 3.1.19 -> 3.1.20

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #121 | High | CVE-2026-25500 | Directory traversal via `Rack::Directory` - string prefix match can be escaped to list directories outside the intended root |
| #123 | Moderate | CVE-2026-25500 | Stored XSS in `Rack::Directory` via `javascript:` filenames rendered into anchor `href` attributes |

**Fix:** `bundle update rack --conservative`

### faraday 1.10.2 -> 1.10.5

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #119 | Moderate | CVE-2026-25765 | SSRF via protocol-relative URL host override in `build_exclusive_url` - user-supplied paths like `//evil.com/path` override the base URL host |

**Fix:** `bundle update faraday --conservative`

### nokogiri 1.18.10 -> 1.19.1

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #3 | Moderate | GHSA-wx95-c6cv-8532 | Does not check the return value from `xmlC14NExecute`, contributing cause to ruby-saml vulnerability GHSA-x4h9-gwv3-r4m4 |

**Fix:** `bundle update nokogiri --conservative`

## Node.js - ui/ (ui/package-lock.json)

### webpack 5.96.1 -> 5.105.2

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #117 | Low | CVE-2025-68458 | `buildHttp` `allowedUris` bypass via URL userinfo (`@`) leading to build-time SSRF |
| #116 | Low | CVE-2025-68157 | `buildHttp` `HttpUriPlugin` `allowedUris` bypass via HTTP redirects leading to SSRF + cache persistence |

**Fix:** Updated `"webpack"` from `"^5.88.1"` to `"^5.104.1"` in `ui/package.json`.
Both CVEs only apply when `experiments.buildHttp` is explicitly enabled (off by default).

### webpack-dev-server 4.15.2 -> 5.2.3

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #99 | Moderate | CVE-2025-30360 | Source code theft via WebSocket hijacking when accessing a malicious site with non-Chromium browsers |
| #98 | Moderate | CVE-2025-30359 | Source code theft via classic script tag injection bypassing same-origin policy |

**Fix:** Updated `"webpack-dev-server"` from `"^4.15.1"` to `"^5.2.1"` in `ui/package.json`.
This is a **major version bump** (v4 -> v5). Verify the dev server still works correctly after this change.

### qs 6.13.0 -> 6.15.0 (via override)

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #113 | High | CVE-2025-15284 | `arrayLimit` bypass in bracket notation allows DoS via memory exhaustion |
| #120 | Low | CVE-2026-2391 | `arrayLimit` bypass in comma parsing allows denial of service |

**Fix:** Added `"qs": "^6.14.2"` to `overrides` in `ui/package.json`. qs is a transitive
dependency (via express/body-parser in webpack-dev-server).

### lodash 4.17.21 -> 4.17.23 (via override)

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #115 | Moderate | CVE-2025-13465 | Prototype Pollution in `_.unset` and `_.omit` - crafted paths can delete methods from global prototypes |

**Fix:** Added `"lodash": "^4.17.23"` to `overrides` in `ui/package.json`. lodash is a
transitive dependency used by multiple packages.

### @babel/runtime 7.0.0 -> 7.28.6 (via override)

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #5 | Moderate | CVE-2025-27789 | Inefficient RegExp complexity in generated code with `.replace` when transpiling named capturing groups - quadratic complexity on specific replacement patterns |

**Fix:** Added `"@babel/runtime": "^7.26.10"` to `overrides` in `ui/package.json`. The
vulnerable 7.0.0 instances were pinned by `react-swipeable-views`.

## Node.js - root (package-lock.json)

### phin 2.9.3 -> 3.7.1 (via override)

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #78 | Moderate | GHSA-x565-32qp-m3vf | Sensitive headers included in subsequent requests after redirect when `followRedirects` is enabled |

**Fix:** Added `"phin": "^3.7.1"` to `overrides` in `package.json`. phin is a transitive
dependency pulled in by jimp. Note: phin 3.7.1 is deprecated upstream; consider replacing
jimp with an alternative image library in the future.

## Unresolved

### elliptic 6.6.1 - no fix available

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #4 | Low | CVE-2025-14505 | Risky ECDSA implementation - incorrect byte-length computation of `k` value (RFC 6979) with leading zeros can produce faulty signatures |

**Status:** All versions through 6.6.1 are affected. No patched version has been released.
elliptic is a transitive dependency in the root `package-lock.json` (via shadow-cljs
crypto chain). Monitor for a release > 6.6.1.

### minimatch 3.1.2 - no fix in 3.x line

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #7 | High | - | ReDoS via repeated wildcards with non-matching literal in pattern - each `*` compiles to a `[^/]*?` regex group, causing O(4^N) backtracking |

**Status:** 3.1.2 is the latest release in the 3.x line. The fix is only available in
minimatch 10.x which has breaking API changes. All dev dependencies (eslint, glob, etc.)
require `minimatch ^3.x` and cannot use 10.x. This is a dev-only dependency and the
vulnerability requires a malicious glob pattern to be supplied to minimatch, so practical
risk is very low.

### ajv 6.12.6 - no fix in 6.x line

| Issue | Severity | CVE | Description |
|-------|----------|-----|-------------|
| #6 | Moderate | CVE-2025-69873 | ReDoS when using `$data` option - malicious regex patterns injected via JSON Pointer cause catastrophic backtracking |

**Status:** 6.12.6 is the latest 6.x release. The fix is in ajv 8.18.0 which has breaking
API changes incompatible with packages that depend on `ajv ^6.x` (eslint, schema-utils,
webpack). The vulnerability only applies when `$data: true` is explicitly enabled, which is
not the default and is not used by eslint or webpack schema-utils. Practical risk is very low.

## Files Changed

- `Gemfile.lock` - updated rack, faraday, and nokogiri versions
- `ui/package.json` - bumped webpack and webpack-dev-server, added qs, lodash, and @babel/runtime overrides
- `ui/package-lock.json` - regenerated
- `package.json` - added phin override
- `package-lock.json` - regenerated
