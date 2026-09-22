import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || '{}'))

  function setLogin(data) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('user_token', data.token)
    localStorage.setItem('user_info', JSON.stringify(data.user))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
  }

  const isLoggedIn = () => !!token.value

  return { token, userInfo, setLogin, logout, isLoggedIn }
})
