import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'
import router from '../router'

export const useInstitutionStore = defineStore('institution', () => {
  const token = ref(localStorage.getItem('inst_token') || '')
  const name = ref(localStorage.getItem('inst_name') || '')
  const phone = ref(localStorage.getItem('inst_phone') || '')
  const userId = ref(localStorage.getItem('inst_user_id') || '')
  const institutionId = ref(localStorage.getItem('inst_id') || '')
  const institutionName = ref(localStorage.getItem('inst_inst_name') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(phoneInput, password) {
    const data = await request.post('/api/institution/login', {
      phone: phoneInput,
      password: password
    })
    token.value = data.token
    name.value = data.userInfo.name
    phone.value = data.userInfo.phone
    userId.value = String(data.userInfo.id)
    institutionId.value = String(data.institution.id)
    institutionName.value = data.institution.name

    localStorage.setItem('inst_token', data.token)
    localStorage.setItem('inst_name', data.userInfo.name)
    localStorage.setItem('inst_phone', data.userInfo.phone)
    localStorage.setItem('inst_user_id', String(data.userInfo.id))
    localStorage.setItem('inst_id', String(data.institution.id))
    localStorage.setItem('inst_inst_name', data.institution.name)
  }

  function logout() {
    token.value = ''
    name.value = ''
    phone.value = ''
    userId.value = ''
    institutionId.value = ''
    institutionName.value = ''
    localStorage.removeItem('inst_token')
    localStorage.removeItem('inst_name')
    localStorage.removeItem('inst_phone')
    localStorage.removeItem('inst_user_id')
    localStorage.removeItem('inst_id')
    localStorage.removeItem('inst_inst_name')
    router.push('/login')
  }

  return { token, name, phone, userId, institutionId, institutionName, isLoggedIn, login, logout }
})
