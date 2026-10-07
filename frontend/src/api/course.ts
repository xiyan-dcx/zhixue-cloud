import request from '@/utils/request'

export const pageCourse = (params: any): Promise<any> =>
  request.get('/business/course/page', { params })

export const getCourse = (id: number): Promise<any> =>
  request.get(`/business/course/${id}`)

export const addCourse = (data: any): Promise<any> =>
  request.post('/business/course', data)

export const updateCourse = (data: any): Promise<any> =>
  request.put('/business/course', data)

export const deleteCourse = (id: number): Promise<any> =>
  request.delete(`/business/course/${id}`)

export const changeCourseStatus = (id: number, status: number): Promise<any> =>
  request.put('/business/course/status', null, { params: { id, status } })

export const listChapter = (courseId: number): Promise<any> =>
  request.get('/business/chapter/list', { params: { courseId } })

export const addChapter = (data: any): Promise<any> =>
  request.post('/business/chapter', data)

export const updateChapter = (data: any): Promise<any> =>
  request.put('/business/chapter', data)

export const deleteChapter = (id: number): Promise<any> =>
  request.delete(`/business/chapter/${id}`)
