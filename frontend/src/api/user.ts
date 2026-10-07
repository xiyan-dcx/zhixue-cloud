import request from '@/utils/request'

export const pageUser = (params: any): Promise<any> =>
  request.get('/system/user/page', { params })

export const addUser = (data: any): Promise<any> =>
  request.post('/system/user', data)

export const updateUser = (data: any): Promise<any> =>
  request.put('/system/user', data)

export const deleteUser = (id: number): Promise<any> =>
  request.delete(`/system/user/${id}`)

export const listRole = (): Promise<any> => request.get('/system/role/list')
