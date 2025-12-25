export interface UserInfo {
  id: number
  username: string
  nickname: string
  email?: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface UserListParams {
  page?: number
  size?: number
  keyword?: string
}

export interface UserListResponse {
  list: UserInfo[]
  total: number
  page: number
  size: number
}