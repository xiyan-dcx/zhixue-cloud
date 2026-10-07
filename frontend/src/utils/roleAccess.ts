export const canViewDashboard = (roleKey: string): boolean =>
  roleKey === 'admin' || roleKey === 'teacher'

export const getHomePath = (roleKey: string): string =>
  roleKey === 'student' ? '/my-courses' : canViewDashboard(roleKey) ? '/dashboard' : '/profile'
