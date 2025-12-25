import { DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 角色权限指令
 * v-hasRole="['admin', 'SECURITY_ADMIN']"
 */
export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value && Array.isArray(value)) {
      const userRoles = userStore.userInfo?.roles || []
      const hasRole = value.some(role => userRoles.includes(role))
      
      if (!hasRole) {
        el.style.display = 'none'
      }
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value && Array.isArray(value)) {
      const userRoles = userStore.userInfo?.roles || []
      const hasRole = value.some(role => userRoles.includes(role))
      
      el.style.display = hasRole ? '' : 'none'
    }
  }
}