import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let authToken = null
let unauthorizedHandler = null

client.interceptors.request.use(config => {
  if (authToken) config.headers.Authorization = `Bearer ${authToken}`
  return config
})

client.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 && error.config?.url !== '/auth/login') {
      unauthorizedHandler?.()
    }
    return Promise.reject(error)
  }
)

export function setAuthToken(token) {
  authToken = token || null
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export async function login(credentials) {
  const response = await client.post('/auth/login', credentials)
  return response.data
}

export async function getOverview() {
  const response = await client.get('/overview')
  return response.data
}

export async function searchBooks(keyword = '') {
  const response = await client.get('/books', { params: { keyword } })
  return response.data
}

export async function getBookCategories() {
  const response = await client.get('/books/categories')
  return response.data
}

export async function createBook(book) {
  const response = await client.post('/books', book)
  return response.data
}

export async function createBookCopy(bookId, copy) {
  const response = await client.post(`/books/${bookId}/copies`, copy)
  return response.data
}

export async function searchReaders(keyword = '') {
  const response = await client.get('/readers', { params: { keyword } })
  return response.data
}

export async function createReader(reader) {
  const response = await client.post('/readers', reader)
  return response.data
}

export async function updateReaderStatus(readerId, status) {
  const response = await client.put(`/readers/${readerId}/status`, { status })
  return response.data
}

export async function borrowBook(borrow) {
  const response = await client.post('/borrows', borrow)
  return response.data
}

export async function returnBook(bookReturn) {
  const response = await client.post('/returns', bookReturn)
  return response.data
}

export async function searchBorrowRecords(keyword = '') {
  const response = await client.get('/borrows', { params: { keyword } })
  return response.data
}

export async function searchOperationLogs(keyword = '') {
  const response = await client.get('/operation-logs', { params: { keyword } })
  return response.data
}

export async function renewBorrow(borrowId) {
  const response = await client.post(`/borrows/${borrowId}/renew`)
  return response.data
}

export async function payFine(fineId, amount) {
  const response = await client.post(`/fines/${fineId}/payments`, { amount })
  return response.data
}
