import { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 使用方式：v-permission="['desensitization:rule:add']"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []

    if (value && value instanceof Array && value.length > 0) {
      const requiredPermissions = value
      const hasPermission = requiredPermissions.some((permission: string) => {
        return permissions.includes(permission)
      })

      if (!hasPermission) {
        el.style.display = 'none'
        // 或者直接移除元素
        // el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('权限指令需要传入权限数组，如：v-permission="[\'desensitization:rule:add\']"')
    }
  }
}

/**
 * 角色指令
 * 使用方式：v-role="['admin']"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userStore = useUserStore()
    const roles = userStore.roles || []

    if (value && value instanceof Array && value.length > 0) {
      const requiredRoles = value
      const hasRole = requiredRoles.some((role: string) => {
        return roles.includes(role)
      })

      if (!hasRole) {
        el.style.display = 'none'
      }
    } else {
      throw new Error('角色指令需要传入角色数组，如：v-role="[\'admin\']"')
    }
  }
}

export default {
  permission,
  role
}
