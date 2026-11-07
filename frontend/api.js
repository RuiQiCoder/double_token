const TOKEN_KEY = 'accessToken'

function storedToken() {
  try { return typeof window === 'undefined' ? '' : window.localStorage.getItem(TOKEN_KEY) || '' }
  catch { return '' }
}

let accessToken = storedToken()
let refreshPromise
export const setToken = value => {
  accessToken = value || ''
  if (typeof window === 'undefined') return
  try {
    if (accessToken) window.localStorage.setItem(TOKEN_KEY, accessToken)
    else window.localStorage.removeItem(TOKEN_KEY)
  } catch {
    console.warn('浏览器禁止访问 Local Storage，访问令牌仅保存在本页内存中。')
  }
}
export const hasToken = () => Boolean(accessToken)

async function send(path, options = {}) {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 12000)
  try {
    const response = await fetch(path, {
      ...options, credentials: 'include', signal: controller.signal,
      headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}), ...options.headers },
    })
    const text = await response.text()
    let body
    try { body = JSON.parse(text) } catch { body = text }
    const code = typeof body === 'object' && body !== null ? body.code : undefined
    if (!response.ok || (code !== undefined && code !== 200)) {
      const error = new Error(body?.message || `请求失败（HTTP ${response.status}）`)
      error.code = code || response.status
      throw error
    }
    return body
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('请求超时，请检查后端是否启动')
    if (error instanceof TypeError) throw new Error('无法连接后端，请检查服务和代理配置')
    throw error
  } finally { clearTimeout(timeout) }
}

export async function refresh() {
  if (!refreshPromise) refreshPromise = send('/user/login/refresh', { method: 'POST' })
    .then(result => {
      if (!result.data?.accessToken) throw new Error('服务未返回访问令牌')
      setToken(result.data.accessToken)
      return result
    }).catch(error => { setToken(''); throw error }).finally(() => { refreshPromise = undefined })
  return refreshPromise
}

export async function request(path, options, retry = true) {
  try { return await send(path, options) } catch (error) {
    if (retry && error.code === 401 && path === '/test/t2') {
      await refresh()
      return send(path, options)
    }
    throw error
  }
}

export async function authenticate(username, password, register) {
  const result = await send('/user/login', {
    method: 'POST', body: JSON.stringify({ username, password, isLogin: register ? '0' : '1' }),
  })
  if (!register) {
    if (!result.data?.accessToken) throw new Error('服务未返回访问令牌')
    setToken(result.data.accessToken)
  }
  return result
}
