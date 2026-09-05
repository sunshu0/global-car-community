// Run against an isolated backend on port 18080.
// Playwright may be installed locally or supplied with PLAYWRIGHT_MODULE_DIR.
import { createRequire } from 'node:module'
import { createServer } from 'vite'
import assert from 'node:assert/strict'
import { fileURLToPath } from 'node:url'

const require = createRequire(import.meta.url)
const { chromium } = require(
  process.env.PLAYWRIGHT_MODULE_DIR
    ? process.env.PLAYWRIGHT_MODULE_DIR + '/playwright'
    : 'playwright',
)
const root = fileURLToPath(new URL('../', import.meta.url))
const server = await createServer({
  root,
  server: {
    host: '127.0.0.1',
    port: 15173,
    strictPort: true,
    proxy: { '/api': { target: 'http://127.0.0.1:18080', changeOrigin: true } },
  },
})
let browser
try {
  await server.listen()
  browser = await chromium.launch({ headless: true, channel: process.env.BROWSER_CHANNEL || 'msedge' })
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  const errors = []
  page.on('pageerror', (error) => errors.push(error.message))
  await page.goto('http://127.0.0.1:15173/')
  const panel = page.locator('#account')
  await panel.getByRole('button', { name: 'New here? Create an account' }).click()
  const email = 'browser-' + Date.now() + '@example.com'
  await panel.getByLabel('Display name').fill('Browser Owner')
  await panel.getByLabel('Email', { exact: true }).fill(email)
  await panel.getByLabel('Password', { exact: true }).fill('Garage@2026')
  await panel.getByRole('button', { name: 'Create account', exact: true }).click()
  await panel.getByText('Account created. Sign in with your new password.').waitFor()
  await panel.getByLabel('Password', { exact: true }).fill('Garage@2026')
  await panel.getByRole('button', { name: 'Sign in', exact: true }).click()
  await panel.getByRole('heading', { name: 'Welcome, Browser Owner' }).waitFor()
  await page.reload()
  await panel.getByRole('heading', { name: 'Welcome, Browser Owner' }).waitFor()
  await page.locator('#car-make').fill('Nissan')
  await page.locator('#car-model').fill('370Z')
  await page.locator('#car-location').fill('Auckland, New Zealand')
  await page.getByRole('button', { name: 'Submit for review' }).click()
  await page.getByText('Nissan 370Z was submitted for review.').waitFor()
  const cookies = await page.context().cookies()
  const sessionCookie = cookies.find((cookie) => cookie.name === 'JSESSIONID')
  assert.ok(sessionCookie?.httpOnly, 'Session cookie must be HttpOnly')
  assert.equal(sessionCookie.sameSite, 'Lax')
  await panel.scrollIntoViewIfNeeded()
  await page.screenshot({ path: root + '../backend/target/auth-desktop.png' })
  await page.setViewportSize({ width: 390, height: 844 })
  await panel.scrollIntoViewIfNeeded()
  assert.ok(await page.evaluate(() =>
    document.documentElement.scrollWidth <= window.innerWidth), 'No horizontal overflow')
  await page.screenshot({ path: root + '../backend/target/auth-mobile.png' })
  await panel.getByRole('button', { name: 'Sign out', exact: true }).click()
  await panel.getByText('You are signed out.').waitFor()
  await page.reload()
  await panel.getByRole('button', { name: 'Sign in', exact: true }).waitFor()
  await page.locator('#car-make').fill('Anonymous')
  await page.locator('#car-model').fill('Attempt')
  await page.locator('#car-location').fill('Auckland, New Zealand')
  await page.getByRole('button', { name: 'Submit for review' }).click()
  await page.getByText('Please sign in before submitting a car.').waitFor()
  await panel.getByLabel('Email', { exact: true }).fill(email)
  await panel.getByLabel('Password', { exact: true }).fill('WrongPassword')
  await panel.getByRole('button', { name: 'Sign in', exact: true }).click()
  await panel.getByRole('alert').waitFor()
  assert.match(await panel.getByRole('alert').innerText(), /Email or password is incorrect/)
  assert.deepEqual(errors, [], 'No browser JavaScript errors')
  console.log('PASS: real browser auth, owner submission, anonymous rejection, mobile layout and logout')
} finally {
  await browser?.close()
  await server.close()
}
