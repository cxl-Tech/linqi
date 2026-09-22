// 图片URL处理工具
// 使用相对路径，图片请求走当前站点同源（开发时由 Vite 代理、生产时由网关转发到后端 /uploads）
const BASE_URL = ''

/**
 * 将相对路径图片URL转换为完整URL
 * @param {string} url - 图片URL（可能是相对路径或完整URL）
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(url) {
  if (!url) return '/placeholder.png'
  
  // 如果已经是完整URL（以http://或https://开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，拼接后端服务器地址
  if (url.startsWith('/')) {
    return BASE_URL + url
  }
  
  // 其他情况，直接返回
  return url
}
