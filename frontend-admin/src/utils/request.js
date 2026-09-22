import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '', timeout: 15000 })

function getRole() {
  const path = router.currentRoute?.value?.path || ''
  return (path.startsWith('/merchant/') || path === '/merchant') ? 'merchant' : 'admin'
}

function getRoleByUrl(url) {
  if (!url) return getRole()
  if (url.startsWith('/api/admin/')) return 'admin'
  // URL 路径中含 /merchant/ 的，一定是商家接口
  if (url.includes('/merchant/') || url.includes('/merchant')) return 'merchant'
  return getRole()
}

request.interceptors.request.use(config => {
  const role = getRoleByUrl(config.url)
  const token = localStorage.getItem(role === 'merchant' ? 'merchant_token' : 'admin_token')
  if (token) config.headers['token'] = token
  return config
})

let isRedirecting = false
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res
    if (res.code === 401) {
      if (!isRedirecting) {
        isRedirecting = true
        const role = getRole()
        ElMessage.error(res.message || '请先登录')
        localStorage.removeItem(role === 'merchant' ? 'merchant_token' : 'admin_token')
        router.push(role === 'merchant' ? '/merchant-login' : '/login').finally(() => { isRedirecting = false })
      }
      return Promise.reject(new Error(res.message || '请先登录'))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => { ElMessage.error(error.message || '网络错误'); return Promise.reject(error) }
)

export default request
