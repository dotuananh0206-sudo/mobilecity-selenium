# MobileCity Selenium UI Tests

Java 17+, Maven, Selenium WebDriver, JUnit 5 and Page Object Model. This project checks public pages only. It does not place orders, submit payment, register accounts, send OTPs, use real credentials, or bypass CAPTCHA.

## Requirements

- JDK 17 or newer and Maven 3.9+.
- Google Chrome installed.
- Network access to MobileCity and, on first run, Selenium Manager's driver resolution/download endpoints.

Selenium Manager (bundled with Selenium 4) manages ChromeDriver automatically; a manually installed `chromedriver` is not required. Confirm your organization permits driver downloads if running behind a proxy.

## Run

From this directory:

```powershell
mvn clean test
```

Run one test class:

```powershell
mvn -Dtest=SearchTest test
```

Run headed Chrome or override the base URL:

```powershell
mvn test -Dmobilecity.headless=false
mvn test -Dmobilecity.baseUrl=https://mobilecity.vn/
```

Environment variables `MOBILECITY_BASE_URL`, `MOBILECITY_HEADLESS`, `MOBILECITY_WAIT_SECONDS`, `MOBILECITY_SCREENSHOT_DIR` and `MOBILECITY_REPORT_DIR` can also configure execution. Maven system properties take precedence. No credentials are required.

## Test scope and safety

- `HomeTest`, `SearchTest`, `ProductTest`, `LoginTest` and `RegisterTest` inspect public pages and form controls. Search accepts a local sample term but does not submit it.
- `CartTest` opens a cart only through a published public link. If no such link is found, JUnit aborts the test and records it as `BLOCKED`; it does not guess a route or click a purchase control.
- CAPTCHA, OTP, account creation, login submission, checkout, purchase/order placement, and payment are not automated.
- Website content, stock, prices and access restrictions may change. `BLOCKED` or `NOT EXECUTED` is not a website failure.

Test data is in `src/test/resources/testdata/testdata.properties`; it contains public URLs/synthetic input only.

## Results and evidence

- JUnit XML/text results: `target/surefire-reports/`.
- Run log: `reports/test-execution.log`.
- Failure screenshots: `screenshots/`.
- Selenium Manager/driver errors remain in the Maven output/Surefire reports. A screenshot is produced when the test method fails and the browser is still available.
- GitHub Actions uploads Surefire reports, the execution log and screenshots as a workflow artifact, including when tests fail. Artifacts are retained for 14 days.

Each run receives a generated `Test Run ID`. Preserve the Surefire reports, log and screenshots from the same run when updating `../2. TestCases.xlsx`. Do not mark a test `PASS` without verifying its assertion in a real run.

## Verified execution snapshot

On 2026-10-10, run `MC-20261010-174513` executed `mvn clean test` with Java 26.0.1 targeting Java 17, Maven 3.9.15, Chrome 154.0.8037.99 and Selenium 4.29.0. Surefire reported 7 tests, 0 failures, 0 errors and 1 skipped; six tests passed and the cart-link test was intentionally aborted and recorded as `BLOCKED` because no published cart link was found. This is a dated execution snapshot, not a claim that the website is defect-free.

Selenium emitted a CDP compatibility warning for Chrome 154, although the smoke tests completed. Re-evaluate the Selenium version and rerun the suite before expanding browser-specific coverage. A first registration test run also exposed a test-owned, case-sensitive locator defect; it was corrected using the observed form DOM and the registration test passed in the complete clean rerun. No MobileCity product defect was verified.

## Project layout

```text
src/main/java/mobilecity/config/     Central browser and test configuration
src/main/java/mobilecity/pages/      Page Object Model
src/main/java/mobilecity/utils/      Test data and evidence logging
src/test/java/mobilecity/base/       WebDriver lifecycle and failure evidence
src/test/java/mobilecity/tests/      Independent JUnit 5 tests
src/test/resources/testdata/         Non-sensitive test data
```

Open this folder as a Maven project in IntelliJ IDEA or Eclipse. Import `pom.xml`, select a JDK 17+ and run the Maven test lifecycle. The project is published at `https://github.com/dotuananh0206-sudo/mobilecity-selenium`; never commit `.env` or secrets.

## GitHub Actions and publishing

The workflow at `.github/workflows/selenium-test.yml` runs on pushes and pull requests targeting `main`, and can also be started manually from the Actions tab. It runs headless Chrome with Java 17 and uploads test evidence as an artifact.

The first verified GitHub Actions run (#1, commit `6cf977f`) completed with **failure**, not success: Maven reported 7 tests, 4 failures, 2 errors, and 1 skipped. The failed assertions/timeouts indicate that expected public-page controls/content were not observed in that runner execution; the available CI evidence does not establish whether the cause was website access/content, a changed page, or test compatibility. Do not report this run as a product defect or a passing CI run. Review the [workflow run](https://github.com/dotuananh0206-sudo/mobilecity-selenium/actions/runs/38047040851) and download its `mobilecity-test-results-1` artifact before diagnosing or updating locators.

Only this folder (`Team/5. Software/`) is the root of the dedicated public repository `dotuananh0206-sudo/mobilecity-selenium`. Do not publish the parent `Team/` folder, coursework workbooks, defect reports, screenshots, execution logs, or other internal evidence. `.gitignore` excludes generated output; `.env` and secrets must never be committed.

The repository is already initialized and pushed to `main`. For subsequent reviewed source changes, run from this project folder:

```powershell
git status --short --branch
git add .github src pom.xml README.md .gitignore .env.example
git status --short
git commit -m "Describe the reviewed change"
git push
```

Review `git status --short` before committing, and verify that the staged list contains only project source and configuration. If the remote has advanced, fetch and reconcile its history before pushing; do not overwrite history or force-push.

After the first push, open the repository's **Actions** tab, inspect the latest workflow run, and download the `mobilecity-test-results-*` artifact. A skipped cart test is reported by JUnit as skipped and is documented as `BLOCKED`; it is not a website defect or a successful cart verification.
