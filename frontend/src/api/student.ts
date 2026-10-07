import request from '@/utils/request'

export const pageStudent = (params: any): Promise<any> =>
  request.get('/business/student/page', { params })

export const addStudent = (data: any): Promise<any> =>
  request.post('/business/student', data)

export const updateStudent = (data: any): Promise<any> =>
  request.put('/business/student', data)

export const deleteStudent = (id: number): Promise<any> =>
  request.delete(`/business/student/${id}`)

export const exportStudent = (): Promise<any> =>
  request.get('/business/student/export', { responseType: 'blob' })

export const downloadTemplate = (): Promise<any> =>
  request.get('/business/student/template', { responseType: 'blob' })
