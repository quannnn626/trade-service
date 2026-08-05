export interface UserLoginType {
  username: string
  password: string
}

export interface UserType {
  username: string
  password: string
  role: string
  roleId: string
}

export interface LoginResponseType {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  nickname: string
}
