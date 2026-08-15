PS D:\Formation_openclassrooms\projet3-datashare\Datashare\datashare_frontend> npm audit
# npm audit report

@babel/core  <=7.29.0
@babel/core: Arbitrary File Read via sourceMappingURL Comment - https://github.com/advisories/GHSA-4x5r-pxfx-6jf8
fix available via `npm audit fix`
node_modules/@babel/core
  @angular/build  <=20.3.29 || 21.0.0-next.0 - 21.2.16 || 22.0.0-next.0 - 22.0.6
  Depends on vulnerable versions of @babel/core
  node_modules/@angular/build

@hono/node-server  <2.0.5
Severity: moderate
Node.js Adapter for Hono: Path traversal in `serve-static` on Windows via encoded backslash (`%5C`) - https://github.com/advisories/GHSA-frvp-7c67-39w9
fix available via `npm audit fix --force`
Will install @angular/cli@21.0.4, which is a breaking change
node_modules/@hono/node-server
  @modelcontextprotocol/sdk  1.25.0 - 1.29.0
  Depends on vulnerable versions of @hono/node-server
  node_modules/@modelcontextprotocol/sdk
    @angular/cli  20.3.14 - 20.3.33 || 21.0.5 - 22.1.3
    Depends on vulnerable versions of @modelcontextprotocol/sdk
    node_modules/@angular/cli

brace-expansion  4.0.0 - 5.0.8
Severity: high
brace-expansion: DoS via unbounded expansion length causing an out-of-memory process crash - https://github.com/advisories/GHSA-mh99-v99m-4gvg
brace-expansion: DoS via unbounded intermediate arrays, bypassing the CVE-2026-14257 mitigation - https://github.com/advisories/GHSA-rgw5-rvv9-x895
fix available via `npm audit fix`
node_modules/brace-expansion

esbuild  0.27.3 - 0.28.0
esbuild allows arbitrary file read when running the development server on Windows - https://github.com/advisories/GHSA-g7r4-m6w7-qqqr
fix available via `npm audit fix`
node_modules/vite/node_modules/esbuild

fast-uri  3.0.0 - 3.1.4
Severity: high
fast-uri vulnerable to host confusion via literal backslash authority delimiter - https://github.com/advisories/GHSA-v2hh-gcrm-f6hx
fast-uri vulnerable to host confusion via backslash authority introducer - https://github.com/advisories/GHSA-7p8r-x3mc-p8w7
fix available via `npm audit fix`
node_modules/fast-uri

hono  <=4.12.33
Severity: moderate
Hono: ReDoS in CORS middleware via Access-Control-Request-Headers - https://github.com/advisories/GHSA-8j4g-w8fx-2239
Hono: `memo()` retains SSR output across requests, leading to cross-user data disclosure - https://github.com/advisories/GHSA-f23p-vx2j-j53r
Hono: Proxy Helper does not remove response headers listed in the `Connection` header - https://github.com/advisories/GHSA-79qm-7rj5-m7r9
Hono: Algorithmic Complexity DoS in Language Middleware - https://github.com/advisories/GHSA-54fx-42gc-7vw4
fix available via `npm audit fix`
node_modules/hono

ip-address  <=10.3.0
Severity: high
ip-address: Address4 decodes leading-zero octets as decimal while resolvers decode them as octal, allowing SSRF and trust-boundary bypass - https://github.com/advisories/GHSA-mwp4-54f8-5fhr
ip-address: a CIDR suffix on the parsed address suppresses special-use classification and can bypass SSRF and trust-boundary checks - https://github.com/advisories/GHSA-4xrf-jv44-h6hh
ip-address: misclassification of IPv4-mapped/NAT64 IPv6 addresses can bypass SSRF and trust-boundary checks - https://github.com/advisories/GHSA-22jq-vg5j-6vgg
fix available via `npm audit fix`
node_modules/ip-address

js-yaml  4.0.0 - 4.3.0
Severity: high
js-yaml has prototype pollution in merge (<<) - https://github.com/advisories/GHSA-mh29-5h37-fv8m
JS-YAML: Quadratic-complexity DoS in merge key handling via repeated aliases - https://github.com/advisories/GHSA-h67p-54hq-rp68
js-yaml: YAML merge-key chains can force quadratic CPU consumption - https://github.com/advisories/GHSA-52cp-r559-cp3m
JS-YAML: Quadratic CPU consumption in !!omap resolution (3.x and 4.x) — CVE-2026-59870 fix not backported - https://github.com/advisories/GHSA-5p4m-2wfm-xmqj
fix available via `npm audit fix --force`
Will install @cypress/code-coverage@4.0.3, which is a breaking change
node_modules/js-yaml
  @cypress/code-coverage  2.0.0-beta.1 - 4.0.1
  Depends on vulnerable versions of js-yaml
  Depends on vulnerable versions of nyc
  node_modules/@cypress/code-coverage

nanoid  <3.3.17
Severity: high
nanoid: custom generators can loop indefinitely when size is zero - https://github.com/advisories/GHSA-2v37-7h3g-55p8
fix available via `npm audit fix`
node_modules/nanoid

postcss  <=8.5.22
Severity: moderate
PostCSS: incomplete fix of GHSA-6g55-p6wh-862q — attacker-controlled sourceMappingURL reads arbitrary .map files when `from` is unset - https://github.com/advisories/GHSA-fxqj-rqcc-2cmp
fix available via `npm audit fix`
node_modules/postcss

tar  <=7.5.20
Severity: moderate
node-tar: Uncontrolled recursion in mapHas/filesFilter allows uncatchable stack-overflow DoS via crafted long-path tar with member selection - https://github.com/advisories/GHSA-r292-9mhp-454m
fix available via `npm audit fix`
node_modules/tar

undici  <=6.27.0 || 7.0.0 - 7.28.0
Severity: high
undici vulnerable to downstream response desynchronization via retry interceptor - https://github.com/advisories/GHSA-8xcm-r25x-g524
undici vulnerable to downstream response desynchronization via retry interceptor - https://github.com/advisories/GHSA-8xcm-r25x-g524
undici vulnerable to cross-user information disclosure and parse-time crash via degenerate private cache directives - https://github.com/advisories/GHSA-4cwx-7wf7-3272
undici vulnerable to CRLF Injection via blob-like body 'type' property - https://github.com/advisories/GHSA-m8rv-5g2x-5cg5
undici vulnerable to CRLF Injection via blob-like body 'type' property - https://github.com/advisories/GHSA-m8rv-5g2x-5cg5
undici vulnerable to cross-user information disclosure via whitespace around equals in Cache-Control directives - https://github.com/advisories/GHSA-jr45-8vmc-qm54
undici vulnerable to cookie attribute injection via unsanitized domain and unparsed setCookie fields - https://github.com/advisories/GHSA-v3r7-h72x-cjcm
undici vulnerable to cookie attribute injection via unsanitized domain and unparsed setCookie fields - https://github.com/advisories/GHSA-v3r7-h72x-cjcm
fix available via `npm audit fix`
node_modules/node-gyp/node_modules/undici
node_modules/undici

uuid  <11.1.1
Severity: moderate
uuid: Missing buffer bounds check in v3/v5/v6 when buf is provided - https://github.com/advisories/GHSA-w5hq-g745-h8pq
fix available via `npm audit fix --force`
Will install nyc@18.0.0, which is a breaking change
node_modules/uuid
  istanbul-lib-processinfo  <=3.0.0
  Depends on vulnerable versions of uuid
  node_modules/istanbul-lib-processinfo
    nyc  15.0.0-alpha.0 - 17.1.0
    Depends on vulnerable versions of istanbul-lib-processinfo
    node_modules/@cypress/code-coverage/node_modules/nyc
    node_modules/nyc

19 vulnerabilities (3 low, 10 moderate, 6 high)

To address issues that do not require attention, run:
  npm audit fix

To address all issues (including breaking changes), run:
  npm audit fix --force



PS D:\Formation_openclassrooms\projet3-datashare\Datashare\datashare_frontend> npm audit fix

added 11 packages, removed 94 packages, changed 20 packages, and audited 848 packages in 27s

166 packages are looking for funding
  run `npm fund` for details

# npm audit report

@babel/core  <=7.29.0
@babel/core: Arbitrary File Read via sourceMappingURL Comment - https://github.com/advisories/GHSA-4x5r-pxfx-6jf8
fix available via `npm audit fix`
node_modules/@babel/core
  @angular/build  <=20.3.29 || 21.0.0-next.0 - 21.2.16 || 22.0.0-next.0 - 22.0.6
  Depends on vulnerable versions of @babel/core
  node_modules/@angular/build

@hono/node-server  <2.0.5
Severity: moderate
Node.js Adapter for Hono: Path traversal in `serve-static` on Windows via encoded backslash (`%5C`) - https://github.com/advisories/GHSA-frvp-7c67-39w9
fix available via `npm audit fix --force`
Will install @angular/cli@21.0.4, which is a breaking change
node_modules/@hono/node-server
  @modelcontextprotocol/sdk  1.25.0 - 1.29.0
  Depends on vulnerable versions of @hono/node-server
  node_modules/@modelcontextprotocol/sdk
    @angular/cli  20.3.14 - 20.3.33 || 21.0.5 - 22.1.3
    Depends on vulnerable versions of @modelcontextprotocol/sdk
    node_modules/@angular/cli

esbuild  0.27.3 - 0.28.0
esbuild allows arbitrary file read when running the development server on Windows - https://github.com/advisories/GHSA-g7r4-m6w7-qqqr
fix available via `npm audit fix`
node_modules/vite/node_modules/esbuild

js-yaml  4.0.0 - 4.3.0
Severity: high
js-yaml has prototype pollution in merge (<<) - https://github.com/advisories/GHSA-mh29-5h37-fv8m
JS-YAML: Quadratic-complexity DoS in merge key handling via repeated aliases - https://github.com/advisories/GHSA-h67p-54hq-rp68
js-yaml: YAML merge-key chains can force quadratic CPU consumption - https://github.com/advisories/GHSA-52cp-r559-cp3m
JS-YAML: Quadratic CPU consumption in !!omap resolution (3.x and 4.x) — CVE-2026-59870 fix not backported - https://github.com/advisories/GHSA-5p4m-2wfm-xmqj
fix available via `npm audit fix --force`
Will install @cypress/code-coverage@4.0.3, which is a breaking change
node_modules/js-yaml
  @cypress/code-coverage  2.0.0-beta.1 - 4.0.1
  Depends on vulnerable versions of js-yaml
  Depends on vulnerable versions of nyc
  node_modules/@cypress/code-coverage

uuid  <11.1.1
Severity: moderate
uuid: Missing buffer bounds check in v3/v5/v6 when buf is provided - https://github.com/advisories/GHSA-w5hq-g745-h8pq
fix available via `npm audit fix --force`
Will install nyc@18.0.0, which is a breaking change
node_modules/uuid
  istanbul-lib-processinfo  <=3.0.0
  Depends on vulnerable versions of uuid
  node_modules/istanbul-lib-processinfo
    nyc  15.0.0-alpha.0 - 17.1.0
    Depends on vulnerable versions of istanbul-lib-processinfo
    node_modules/@cypress/code-coverage/node_modules/nyc
    node_modules/nyc

11 vulnerabilities (3 low, 7 moderate, 1 high)

To address issues that do not require attention, run:
  npm audit fix

To address all issues (including breaking changes), run:
  npm audit fix --force


PS D:\Formation_openclassrooms\projet3-datashare\Datashare\datashare_frontend> npm audit fix --force
npm warn using --force Recommended protections disabled.
npm warn audit Updating @cypress/code-coverage to 4.0.3, which is a SemVer major change.
npm warn audit Updating @angular/cli to 21.0.4, which is a SemVer major change.
npm warn audit Updating nyc to 18.0.0, which is a SemVer major change.

added 246 packages, removed 48 packages, changed 44 packages, and audited 1046 packages in 21s

176 packages are looking for funding
  run `npm fund` for details

# npm audit report

@babel/core  <=7.29.0
@babel/core: Arbitrary File Read via sourceMappingURL Comment - https://github.com/advisories/GHSA-4x5r-pxfx-6jf8
fix available via `npm audit fix`
node_modules/@babel/core
  @angular/build  <=20.3.29 || 21.0.0-next.0 - 21.2.16 || 22.0.0-next.0 - 22.0.6
  Depends on vulnerable versions of @babel/core
  node_modules/@angular/build

@modelcontextprotocol/sdk  1.3.0 - 1.25.3
Severity: high
@modelcontextprotocol/sdk has cross-client data leak via shared server/transport instance reuse - https://github.com/advisories/GHSA-345p-7cg4-v4c7
Anthropic's MCP TypeScript SDK has a ReDoS vulnerability - https://github.com/advisories/GHSA-8r9q-7v3j-jr4g
fix available via `npm audit fix --force`
Will install @angular/cli@21.2.20, which is outside the stated dependency range
node_modules/@modelcontextprotocol/sdk
  @angular/cli  10.0.0-next.0 - 10.0.0-rc.1 || 14.1.0-next.0 - 14.1.0-rc.3 || 17.0.0-next.0 - 17.0.0-next.2 || 17.2.0-next.0 - 19.2.22 || 20.0.0-next.0 - 20.3.21 || 21.0.0-next.0 - 21.2.4 || 22.0.0-next.0 - 22.0.0-rc.3
  Depends on vulnerable versions of @angular-devkit/architect
  Depends on vulnerable versions of @angular-devkit/core
  Depends on vulnerable versions of @angular-devkit/schematics
  Depends on vulnerable versions of @modelcontextprotocol/sdk
  Depends on vulnerable versions of @schematics/angular
  node_modules/@angular/cli

ajv  7.0.0-alpha.0 - 8.17.1
Severity: moderate
ajv has ReDoS when using `$data` option - https://github.com/advisories/GHSA-2g4f-4pwh-qvx6
fix available via `npm audit fix --force`
Will install @angular/cli@21.2.20, which is outside the stated dependency range
node_modules/@angular-devkit/schematics/node_modules/ajv
node_modules/@angular/cli/node_modules/ajv
node_modules/@schematics/angular/node_modules/ajv
  @angular-devkit/core  12.0.0-next.0 - 19.2.22 || 20.0.0-next.0 - 20.3.21 || 21.0.0-next.0 - 21.2.4 || 22.0.0-next.0 - 22.0.0-rc.3
  Depends on vulnerable versions of ajv
  Depends on vulnerable versions of picomatch
  node_modules/@angular-devkit/schematics/node_modules/@angular-devkit/core
  node_modules/@angular/cli/node_modules/@angular-devkit/core
  node_modules/@schematics/angular/node_modules/@angular-devkit/core
    @angular-devkit/architect  0.1000.0-next.0 - 0.1000.0-rc.1 || 0.1702.0-next.0 - 0.1902.22 || 0.2000.0-next.0 - 0.2003.21 || 0.2100.0-next.0 - 0.2102.4 || 0.2200.0-next.0 - 0.2200.0-rc.3
    Depends on vulnerable versions of @angular-devkit/core
    node_modules/@angular/cli/node_modules/@angular-devkit/architect
    @angular-devkit/schematics  17.2.0-next.0 - 19.2.22 || 20.0.0-next.0 - 20.3.21 || 21.0.0-next.0 - 21.2.4 || 22.0.0-next.0 - 22.0.0-rc.3
    Depends on vulnerable versions of @angular-devkit/core
    node_modules/@angular-devkit/schematics
    @schematics/angular  17.2.0-next.0 - 19.2.22 || 20.0.0-next.0 - 20.3.21 || 21.0.0-next.0 - 21.2.4 || 22.0.0-next.0 - 22.0.0-rc.3
    Depends on vulnerable versions of @angular-devkit/core
    Depends on vulnerable versions of @angular-devkit/schematics
    node_modules/@schematics/angular

esbuild  0.27.3 - 0.28.0
esbuild allows arbitrary file read when running the development server on Windows - https://github.com/advisories/GHSA-g7r4-m6w7-qqqr
fix available via `npm audit fix`
node_modules/vite/node_modules/esbuild

picomatch  4.0.0 - 4.0.3
Severity: high
Picomatch: Method Injection in POSIX Character Classes causes incorrect Glob Matching - https://github.com/advisories/GHSA-3v7f-55p6-f55p
Picomatch has a ReDoS vulnerability via extglob quantifiers - https://github.com/advisories/GHSA-c2c7-rcm5-vvqj
fix available via `npm audit fix --force`
Will install @angular/cli@21.2.20, which is outside the stated dependency range
node_modules/@angular-devkit/schematics/node_modules/picomatch
node_modules/@angular/cli/node_modules/picomatch
node_modules/@schematics/angular/node_modules/picomatch

11 vulnerabilities (3 low, 5 moderate, 3 high)

To address issues that do not require attention, run:
  npm audit fix

To address all issues, run:
  npm audit fix --force
PS D:\Formation_openclassrooms\projet3-datashare\Datashare\datashare_frontend>
