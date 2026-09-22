import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('user_token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_info')
      router.push('/login')
    }
    return Promise.reject(res)
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
