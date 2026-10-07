import request from '@/utils/request'

export const pageOrder = (params: any): Promise<any> =>
  request.get('/business/order/page', { params })
