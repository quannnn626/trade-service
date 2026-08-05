import request from '@/axios'
import type { UserType } from './types'

interface RoleParams {
  roleName: string
}

export const loginApi = (data: UserType): Promise<IResponse<LoginResponseType>> => {
  return request.post({ url: '/api/auth/login', data })
}

export const refreshApi = (data: { userId: number; refreshToken: string }) => {
  return request.post<string>({ url: '/api/auth/refresh', data })
}

export const registerApi = (data: {
  username: string
  password: string
}): Promise<IResponse> => {
  return request.post({ url: '/api/auth/register', data })
}

export const loginOutApi = (userId: number): Promise<IResponse> => {
  return request.post({ url: '/api/auth/logout', params: { userId } })
}

export const getUserListApi = ({ params }: AxiosConfig) => {
  return request.get<{
    code: string
    data: {
      list: UserType[]
      total: number
    }
  }>({ url: '/mock/user/list', params })
}

export const getAdminRoleApi = (
  params: RoleParams
): Promise<IResponse<AppCustomRouteRecordRaw[]>> => {
  return request.get({ url: '/mock/role/list', params })
}

export const getTestRoleApi = (params: RoleParams): Promise<IResponse<string[]>> => {
  return request.get({ url: '/mock/role/list2', params })
}
