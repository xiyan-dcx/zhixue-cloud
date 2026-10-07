import request from '@/utils/request'

export const getOverview = (): Promise<any> =>
  request.get('/business/dashboard/overview')

export const getEnrollTrend = (): Promise<any> =>
  request.get('/business/dashboard/enrollTrend')

export const getCourseTop10 = (): Promise<any> =>
  request.get('/business/dashboard/courseTop10')

export const getTeacherRevenue = (): Promise<any> =>
  request.get('/business/dashboard/teacherRevenue')

export const getMonthRevenue = (): Promise<any> =>
  request.get('/business/dashboard/monthRevenue')
