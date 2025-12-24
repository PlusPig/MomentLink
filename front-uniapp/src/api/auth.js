import { request } from '@/utils/request'

export const login = (payload) => request.post('/auth/login', payload, { auth: false })

export const register = (payload) => request.post('/auth/register', payload, { auth: false })
