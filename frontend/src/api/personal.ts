import request from '@/utils/request'
export const myCourses = (params: any): Promise<any> => request.get('/my/courses', { params })
export const purchasedCourseIds = (): Promise<any> => request.get('/my/courses/purchased')
export const restoreMyCourse = (id: number): Promise<any> => request.post(`/my/courses/${id}/restore`)
export const joinMyCourse = (id: number, data?: { confirmed: boolean; expectedAmount: number }): Promise<any> => request.post(`/my/courses/${id}`, data)
export const removeMyCourse = (id: number): Promise<any> => request.delete(`/my/courses/${id}`)
export const getProfile = (): Promise<any> => request.get('/profile')
export const saveProfile = (nickname: string): Promise<any> => request.put('/profile', { nickname })
export const changePassword = (data: { oldPassword: string; newPassword: string }): Promise<any> => request.put('/profile/password', data)
export const uploadAvatar = (file: File): Promise<any> => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/profile/avatar', form)
}
