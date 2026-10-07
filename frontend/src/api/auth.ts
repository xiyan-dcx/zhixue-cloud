import request from '@/utils/request'

export interface LoginForm {
  username: string
  password: string
}

export const login = (data: LoginForm): Promise<any> =>
  request.post('/auth/login', data)

export const logout = (): Promise<any> => request.post('/auth/logout')

export const getInfo = (): Promise<any> => request.get('/auth/getInfo')

export const getRouters = (): Promise<any> => request.get('/auth/getRouters')
