export interface UserInfo {
  username: string
  roles: string[]
  permissions: string[]
  avatar?: string
  email?: string
  phone?: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface LoginRequest extends LoginForm {}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
  expiresIn: number
}

export interface UserListParams {
  page?: number
  size?: number
  username?: string
  role?: string
}

export interface UserListResponse {
  total: number
  list: UserInfo[]
}
