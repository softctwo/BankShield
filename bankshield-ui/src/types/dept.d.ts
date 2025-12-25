export interface Dept {
  id: number
  parentId: number
  deptName: string
  deptCode: string
  leader?: string
  phone?: string
  email?: string
  sortOrder: number
  status: number
  createTime?: string
  updateTime?: string
  children?: Dept[]
}

export interface DeptTreeNode extends Dept {
  children?: DeptTreeNode[]
}

export interface DeptListParams {
  page?: number
  size?: number
  deptName?: string
  deptCode?: string
}

export interface DeptListResponse {
  list: Dept[]
  total: number
  page: number
  size: number
}

export interface DeptTreeResponse {
  tree: DeptTreeNode[]
}