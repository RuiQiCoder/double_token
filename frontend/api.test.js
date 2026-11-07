import { test } from 'node:test'
import assert from 'node:assert/strict'
import { authenticate, request, refresh, setToken, hasToken } from './api.js'

const response = (status, body) => ({ ok: status < 400, status, text: async () => typeof body === 'string' ? body : JSON.stringify(body) })

test('登录契约、过期重试、刷新合并与权限失败', async () => {
  const original = globalThis.fetch
  const originalWindow = globalThis.window
  const storage = new Map()
  globalThis.window = { localStorage: {
    getItem: key => storage.get(key) ?? null,
    setItem: (key, value) => storage.set(key, value),
    removeItem: key => storage.delete(key),
  } }
  try {
    globalThis.fetch = async (path, options) => {
      assert.equal(path, '/user/login')
      assert.deepEqual(JSON.parse(options.body), { username: 'demo_user', password: 'Demo@123', isLogin: '1' })
      assert.equal(options.credentials, 'include')
      return response(200, { code: 200, data: { accessToken: 'old' } })
    }
    await authenticate('demo_user', 'Demo@123', false)
    assert.equal(storage.get('accessToken'), 'old')
    let calls = 0
    globalThis.fetch = async (path, options) => {
      calls++
      if (path.endsWith('/refresh')) return response(200, { code: 200, data: { accessToken: 'new' } })
      return options.headers.Authorization === 'Bearer new' ? response(200, 't2') : response(401, { code: 401 })
    }
    assert.equal(await request('/test/t2'), 't2')
    assert.equal(calls, 3)
    assert.equal(storage.get('accessToken'), 'new')
    calls = 0
    await Promise.all([refresh(), refresh()])
    assert.equal(calls, 1)
    // 手动刷新后，下一次独立请求也必须使用新令牌。
    globalThis.fetch = async (path, options) => {
      if (path === '/user/login/refresh') {
        return response(200, { code: 200, data: { accessToken: 'rotated' } })
      }
      assert.equal(options.headers.Authorization, 'Bearer rotated')
      return response(200, 't2 with rotated token')
    }
    await refresh()
    assert.equal(storage.get('accessToken'), 'rotated')
    assert.equal(await request('/test/t2'), 't2 with rotated token')
    // 重新加载模块模拟刷新页面，验证从 Local Storage 恢复并用于请求。
    const restored = await import('./api.js?storage-restore')
    assert.equal(restored.hasToken(), true)
    assert.equal(await restored.request('/test/t2'), 't2 with rotated token')
    calls = 0
    globalThis.fetch = async () => { calls++; return response(403, { code: 403, message: '无权限访问' }) }
    await assert.rejects(request('/test/t2'), /无权限访问/)
    assert.equal(calls, 1)
    globalThis.fetch = async () => response(401, { code: 40102, message: '请重新登录' })
    await assert.rejects(refresh(), /请重新登录/)
    assert.equal(hasToken(), false)
    assert.equal(storage.has('accessToken'), false)
    setToken('logout-token')
    setToken('')
    assert.equal(storage.has('accessToken'), false)
  } finally {
    globalThis.fetch = original
    setToken('')
    if (originalWindow === undefined) delete globalThis.window
    else globalThis.window = originalWindow
  }
})
