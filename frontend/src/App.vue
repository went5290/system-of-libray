<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Collection, Download, List, Plus, Reading, Search, Setting, Switch, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { borrowBook, changePassword, createBook, createBookCategory, createBookCopy, createReader, getBookCategories, getBookCopies, getLibraryRules, getOverview, login, payFine, renewBorrow, returnBook, searchBooks, searchBorrowRecords, searchOperationLogs, searchReaders, setAuthToken, setUnauthorizedHandler, updateBook, updateBookCategory, updateBookCopyLocation, updateBookCopyStatus, updateLibraryRules, updateReader, updateReaderStatus } from './api'
import { downloadCsv } from './csv'

const SESSION_KEY = 'library-console-user'

function readSession() {
  try {
    const session = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null')
    if (!session?.token || !session?.expiresAt || new Date(session.expiresAt) <= new Date()) {
      localStorage.removeItem(SESSION_KEY)
      return null
    }
    return session
  } catch {
    return null
  }
}

function saveSession(user) {
  try {
    if (user) localStorage.setItem(SESSION_KEY, JSON.stringify(user))
    else localStorage.removeItem(SESSION_KEY)
  } catch {
    // Some embedded browsers disable local storage; the in-memory session still works.
  }
}

const currentUser = ref(readSession())
setAuthToken(currentUser.value?.token)
setUnauthorizedHandler(() => {
  currentUser.value = null
  setAuthToken(null)
  saveSession(null)
  activeSection.value = 'overview'
  ElMessage.warning('登录已失效，请重新登录')
})
const loginSubmitting = ref(false)
const loginFormRef = ref()
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref()
const books = ref([])
const categories = ref([])
const keyword = ref('')
const loading = ref(false)
const errorMessage = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const editingBook = ref(null)
const categoryDialogVisible = ref(false)
const categorySubmitting = ref(false)
const categoryFormRef = ref()
const editingCategory = ref(null)
const copyDialogVisible = ref(false)
const copySubmitting = ref(false)
const copyFormRef = ref()
const selectedBook = ref(null)
const copyListDialogVisible = ref(false)
const copyListLoading = ref(false)
const copyStatusUpdating = ref(null)
const bookCopies = ref([])
const selectedCopyBook = ref(null)
const copyLocationEditingId = ref(null)
const copyLocationDraft = ref('')
const copyLocationSubmitting = ref(false)
const activeSection = ref('overview')
const overview = ref(null)
const overviewLoading = ref(false)
const readers = ref([])
const readerKeyword = ref('')
const readerLoading = ref(false)
const readerDialogVisible = ref(false)
const readerSubmitting = ref(false)
const readerFormRef = ref()
const editingReader = ref(null)
const borrowSubmitting = ref(false)
const borrowFormRef = ref()
const lastBorrow = ref(null)
const circulationMode = ref('borrow')
const returnSubmitting = ref(false)
const returnFormRef = ref()
const lastReturn = ref(null)
const borrowRecords = ref([])
const borrowRecordKeyword = ref('')
const borrowRecordLoading = ref(false)
const operationLogs = ref([])
const operationLogKeyword = ref('')
const operationLogLoading = ref(false)
const ruleLoading = ref(false)
const ruleSubmitting = ref(false)
const ruleFormRef = ref()
const fineDialogVisible = ref(false)
const fineSubmitting = ref(false)
const selectedFineRecord = ref(null)
const fineFormRef = ref()

const loginForm = reactive({
  username: 'admin',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 72, message: '新密码长度必须为 8 至 72 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
}

const form = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  categoryId: null,
  description: ''
})

const rules = {
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择图书分类', trigger: 'change' }]
}

const categoryForm = reactive({
  categoryName: '',
  description: ''
})

const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const copyForm = reactive({
  barcode: '',
  shelfLocation: ''
})

const copyRules = {
  barcode: [{ required: true, message: '请输入馆藏条码', trigger: 'blur' }]
}

const readerForm = reactive({
  readerNo: '',
  phone: '',
  email: '',
  maxBorrowCount: 5
})

const readerRules = {
  readerNo: [{ required: true, message: '请输入借书证号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  maxBorrowCount: [{ required: true, message: '请输入最大借阅数', trigger: 'blur' }]
}

const borrowForm = reactive({
  readerNo: '',
  barcode: ''
})

const borrowRules = {
  readerNo: [{ required: true, message: '请输入借书证号', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入馆藏条码', trigger: 'blur' }]
}

const returnForm = reactive({
  barcode: ''
})

const returnRules = {
  barcode: [{ required: true, message: '请输入馆藏条码', trigger: 'blur' }]
}

const fineForm = reactive({
  amount: 0
})

const fineRules = {
  amount: [{ required: true, message: '请输入缴费金额', trigger: 'blur' }]
}

const ruleForm = reactive({
  borrowDays: 30,
  maxRenewCount: 1,
  finePerDay: 0.5
})

const ruleRules = {
  borrowDays: [{ required: true, message: '请输入默认借阅天数', trigger: 'blur' }],
  maxRenewCount: [{ required: true, message: '请输入最大续借次数', trigger: 'blur' }],
  finePerDay: [{ required: true, message: '请输入每日逾期费用', trigger: 'blur' }]
}

const totalCopies = computed(() => books.value.reduce((sum, book) => sum + book.totalCopies, 0))
const availableCopies = computed(() => books.value.reduce((sum, book) => sum + book.availableCopies, 0))
const activeReaders = computed(() => readers.value.filter(reader => reader.status === 'ACTIVE').length)
const totalBorrowLimit = computed(() => readers.value.reduce((sum, reader) => sum + reader.maxBorrowCount, 0))
const activeBorrowRecords = computed(() => borrowRecords.value.filter(record => ['BORROWED', 'OVERDUE'].includes(record.status)).length)
const unpaidFineTotal = computed(() => borrowRecords.value.reduce((sum, record) => {
  if (!['UNPAID', 'PARTIAL'].includes(record.fineStatus)) return sum
  return sum + Number(record.fineAmount || 0) - Number(record.paidAmount || 0)
}, 0))

async function submitLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loginSubmitting.value = true
  try {
    currentUser.value = await login({ ...loginForm })
    setAuthToken(currentUser.value.token)
    saveSession(currentUser.value)
    loginForm.password = ''
    ElMessage.success(`欢迎回来，${currentUser.value.displayName}`)
    await Promise.all([loadOverview(), loadBooks()])
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '登录失败，请检查账号和密码')
  } finally {
    loginSubmitting.value = false
  }
}

async function logout() {
  await ElMessageBox.confirm('确定要退出当前账号吗？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => false)
    .then(confirmed => {
      if (!confirmed) return
      currentUser.value = null
      setAuthToken(null)
      saveSession(null)
      activeSection.value = 'overview'
    })
}

function resetPasswordForm() {
  Object.assign(passwordForm, { currentPassword: '', newPassword: '', confirmPassword: '' })
  passwordFormRef.value?.clearValidate()
}

async function submitPasswordChange() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSubmitting.value = true
  try {
    await changePassword({ ...passwordForm })
    passwordDialogVisible.value = false
    resetPasswordForm()
    currentUser.value = null
    setAuthToken(null)
    saveSession(null)
    activeSection.value = 'overview'
    ElMessage.success('密码修改成功，请使用新密码重新登录')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '修改密码失败')
  } finally {
    passwordSubmitting.value = false
  }
}

async function loadBooks() {
  loading.value = true
  errorMessage.value = ''
  try {
    books.value = await searchBooks(keyword.value)
  } catch {
    errorMessage.value = '暂时无法连接后端服务，请确认 Oracle 与 Spring Boot 已启动。'
  } finally {
    loading.value = false
  }
}

async function openCreateDialog() {
  try {
    categories.value = await getBookCategories()
    editingBook.value = null
    dialogVisible.value = true
  } catch {
    ElMessage.error('无法加载图书分类')
  }
}

async function openCategoryDialog() {
  try {
    categories.value = await getBookCategories()
    categoryDialogVisible.value = true
  } catch {
    ElMessage.error('无法加载图书分类')
  }
}

function editCategory(category) {
  editingCategory.value = category
  Object.assign(categoryForm, {
    categoryName: category.categoryName,
    description: category.description || ''
  })
}

function resetCategoryForm() {
  editingCategory.value = null
  Object.assign(categoryForm, { categoryName: '', description: '' })
  categoryFormRef.value?.clearValidate()
}

async function submitCategory() {
  const valid = await categoryFormRef.value.validate().catch(() => false)
  if (!valid) return

  categorySubmitting.value = true
  try {
    if (editingCategory.value) {
      await updateBookCategory(editingCategory.value.id, { ...categoryForm })
      ElMessage.success('分类已更新')
    } else {
      await createBookCategory({ ...categoryForm })
      ElMessage.success('分类已新增')
    }
    resetCategoryForm()
    categories.value = await getBookCategories()
    await loadBooks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || (editingCategory.value ? '更新分类失败' : '新增分类失败'))
  } finally {
    categorySubmitting.value = false
  }
}

async function openEditDialog(book) {
  try {
    categories.value = await getBookCategories()
    editingBook.value = book
    Object.assign(form, {
      isbn: book.isbn,
      title: book.title,
      author: book.author || '',
      publisher: book.publisher || '',
      publishDate: book.publishDate || '',
      categoryId: book.categoryId,
      description: book.description || ''
    })
    dialogVisible.value = true
  } catch {
    ElMessage.error('无法加载图书分类')
  }
}

function resetForm() {
  Object.assign(form, {
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    categoryId: null,
    description: ''
  })
  formRef.value?.clearValidate()
  editingBook.value = null
}

async function submitBook() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { ...form, publishDate: form.publishDate || null }
    if (editingBook.value) {
      await updateBook(editingBook.value.id, payload)
      ElMessage.success('书目编辑成功')
    } else {
      await createBook(payload)
      ElMessage.success('书目新增成功')
      keyword.value = ''
    }
    dialogVisible.value = false
    resetForm()
    await loadBooks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || (editingBook.value ? '编辑书目失败' : '新增书目失败'))
  } finally {
    submitting.value = false
  }
}

function openCopyDialog(book) {
  selectedBook.value = book
  copyDialogVisible.value = true
}

async function openCopyListDialog(book) {
  selectedCopyBook.value = book
  copyListDialogVisible.value = true
  copyListLoading.value = true
  try {
    bookCopies.value = await getBookCopies(book.id)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载馆藏副本失败')
  } finally {
    copyListLoading.value = false
  }
}

function resetCopyList() {
  selectedCopyBook.value = null
  bookCopies.value = []
  copyStatusUpdating.value = null
  cancelCopyLocationEdit()
}

function startCopyLocationEdit(copy) {
  copyLocationEditingId.value = copy.id
  copyLocationDraft.value = copy.shelfLocation || ''
}

function cancelCopyLocationEdit() {
  copyLocationEditingId.value = null
  copyLocationDraft.value = ''
}

async function submitCopyLocation(copy) {
  if (copyLocationDraft.value.length > 80) {
    ElMessage.warning('书架位置不能超过 80 个字符')
    return
  }
  copyLocationSubmitting.value = true
  try {
    await updateBookCopyLocation(copy.id, copyLocationDraft.value)
    bookCopies.value = await getBookCopies(selectedCopyBook.value.id)
    cancelCopyLocationEdit()
    ElMessage.success('书架位置已更新')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新书架位置失败')
  } finally {
    copyLocationSubmitting.value = false
  }
}

function copyStatusLabel(status) {
  return {
    AVAILABLE: '可借',
    BORROWED: '借出',
    LOST: '遗失',
    DAMAGED: '损坏',
    MAINTENANCE: '维护中'
  }[status] || status
}

function copyStatusType(status) {
  return {
    AVAILABLE: 'success',
    BORROWED: 'primary',
    LOST: 'danger',
    DAMAGED: 'warning',
    MAINTENANCE: 'info'
  }[status] || 'info'
}

async function changeCopyStatus(copy, status) {
  if (status === copy.status) return
  try {
    await ElMessageBox.confirm(
      `确认将馆藏副本 ${copy.barcode} 的状态修改为“${copyStatusLabel(status)}”？`,
      '修改馆藏副本状态',
      { confirmButtonText: '确认修改', cancelButtonText: '取消', type: 'warning' }
    )
    copyStatusUpdating.value = copy.id
    await updateBookCopyStatus(copy.id, status)
    bookCopies.value = await getBookCopies(selectedCopyBook.value.id)
    await loadBooks()
    ElMessage.success('馆藏副本状态已更新')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.response?.data?.message || '更新馆藏副本状态失败')
  } finally {
    copyStatusUpdating.value = null
  }
}

function resetCopyForm() {
  Object.assign(copyForm, { barcode: '', shelfLocation: '' })
  selectedBook.value = null
  copyFormRef.value?.clearValidate()
}

async function submitCopy() {
  const valid = await copyFormRef.value.validate().catch(() => false)
  if (!valid) return

  copySubmitting.value = true
  try {
    await createBookCopy(selectedBook.value.id, { ...copyForm })
    ElMessage.success('馆藏副本登记成功')
    copyDialogVisible.value = false
    resetCopyForm()
    await loadBooks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登记馆藏副本失败')
  } finally {
    copySubmitting.value = false
  }
}

async function switchSection(section) {
  activeSection.value = section
  if (section === 'overview') await loadOverview()
  if (section === 'readers') await loadReaders()
  if (section === 'records') await Promise.all([loadBorrowRecords(), loadLibraryRules()])
  if (section === 'rules') await loadLibraryRules()
  if (section === 'logs') await loadOperationLogs()
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    overview.value = await getOverview()
  } catch {
    ElMessage.error('无法加载系统概览')
  } finally {
    overviewLoading.value = false
  }
}

async function loadReaders() {
  readerLoading.value = true
  try {
    readers.value = await searchReaders(readerKeyword.value)
  } catch {
    ElMessage.error('无法加载读者列表')
  } finally {
    readerLoading.value = false
  }
}

async function loadBorrowRecords() {
  borrowRecordLoading.value = true
  try {
    borrowRecords.value = await searchBorrowRecords(borrowRecordKeyword.value)
  } catch {
    ElMessage.error('无法加载借阅记录')
  } finally {
    borrowRecordLoading.value = false
  }
}

async function loadOperationLogs() {
  operationLogLoading.value = true
  try {
    operationLogs.value = await searchOperationLogs(operationLogKeyword.value)
  } catch {
    ElMessage.error('无法加载操作日志')
  } finally {
    operationLogLoading.value = false
  }
}

async function loadLibraryRules() {
  ruleLoading.value = true
  try {
    Object.assign(ruleForm, await getLibraryRules())
  } catch {
    ElMessage.error('无法加载借阅规则')
  } finally {
    ruleLoading.value = false
  }
}

async function submitLibraryRules() {
  const valid = await ruleFormRef.value.validate().catch(() => false)
  if (!valid) return

  ruleSubmitting.value = true
  try {
    Object.assign(ruleForm, await updateLibraryRules({ ...ruleForm }))
    ElMessage.success('借阅规则已保存，新办理业务将立即使用新规则')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存借阅规则失败')
  } finally {
    ruleSubmitting.value = false
  }
}

function canRenew(record) {
  return record.status === 'BORROWED' && record.renewCount < ruleForm.maxRenewCount
}

function borrowStatusType(status) {
  return { BORROWED: 'primary', OVERDUE: 'danger', RETURNED: 'success', LOST: 'warning' }[status] || 'info'
}

function logActionLabel(action) {
  return {
    CREATE_BOOK: '新增书目',
    UPDATE_BOOK: '编辑书目',
    CREATE_BOOK_CATEGORY: '新增图书分类',
    UPDATE_BOOK_CATEGORY: '编辑图书分类',
    CREATE_BOOK_COPY: '登记副本',
    UPDATE_BOOK_COPY_STATUS: '更新副本状态',
    UPDATE_BOOK_COPY_LOCATION: '更新副本位置',
    CREATE_READER: '新增读者',
    UPDATE_READER: '编辑读者',
    UPDATE_READER_STATUS: '更新读者状态',
    BORROW_BOOK: '办理借书',
    RETURN_BOOK: '办理归还',
    RENEW_BORROW: '办理续借',
    PAY_FINE: '罚款缴费',
    UPDATE_LIBRARY_RULES: '更新借阅规则',
    CHANGE_PASSWORD: '修改登录密码'
  }[action] || action
}

async function renewRecord(record) {
  try {
    await ElMessageBox.confirm(
      `确认续借《${record.bookTitle}》？应还日期将延长 30 天。`,
      '确认续借',
      { confirmButtonText: '确认续借', cancelButtonText: '取消', type: 'warning' }
    )
    await renewBorrow(record.id)
    ElMessage.success('续借成功')
    await loadBorrowRecords()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.response?.data?.message || '续借失败')
  }
}

function remainingFine(record) {
  return Number(record.fineAmount || 0) - Number(record.paidAmount || 0)
}

function openFineDialog(record) {
  selectedFineRecord.value = record
  fineForm.amount = remainingFine(record)
  fineDialogVisible.value = true
}

function resetFineForm() {
  selectedFineRecord.value = null
  fineForm.amount = 0
  fineFormRef.value?.clearValidate()
}

async function submitFinePayment() {
  const valid = await fineFormRef.value.validate().catch(() => false)
  if (!valid) return

  fineSubmitting.value = true
  try {
    await payFine(selectedFineRecord.value.fineId, fineForm.amount)
    ElMessage.success('罚款缴费成功')
    fineDialogVisible.value = false
    resetFineForm()
    await loadBorrowRecords()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '罚款缴费失败')
  } finally {
    fineSubmitting.value = false
  }
}

function resetReaderForm() {
  editingReader.value = null
  Object.assign(readerForm, { readerNo: '', phone: '', email: '', maxBorrowCount: 5 })
  readerFormRef.value?.clearValidate()
}

function openCreateReaderDialog() {
  resetReaderForm()
  readerDialogVisible.value = true
}

function openEditReaderDialog(reader) {
  editingReader.value = reader
  Object.assign(readerForm, {
    readerNo: reader.readerNo,
    phone: reader.phone || '',
    email: reader.email || '',
    maxBorrowCount: reader.maxBorrowCount
  })
  readerDialogVisible.value = true
}

async function submitReader() {
  const valid = await readerFormRef.value.validate().catch(() => false)
  if (!valid) return

  readerSubmitting.value = true
  try {
    if (editingReader.value) {
      await updateReader(editingReader.value.id, { ...readerForm })
      ElMessage.success('读者资料已更新')
    } else {
      await createReader({ ...readerForm })
      ElMessage.success('读者新增成功')
    }
    readerDialogVisible.value = false
    resetReaderForm()
    readerKeyword.value = ''
    await loadReaders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || (editingReader.value ? '更新读者资料失败' : '新增读者失败'))
  } finally {
    readerSubmitting.value = false
  }
}

async function changeReaderStatus(reader, status) {
  if (status === reader.status) return
  try {
    await ElMessageBox.confirm(
      `确认将读者 ${reader.readerNo} 的状态修改为 ${status}？`,
      '修改读者状态',
      { confirmButtonText: '确认修改', cancelButtonText: '取消', type: 'warning' }
    )
    await updateReaderStatus(reader.id, status)
    ElMessage.success('读者状态已更新')
    await loadReaders()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.response?.data?.message || '更新读者状态失败')
  }
}

async function submitBorrow() {
  const valid = await borrowFormRef.value.validate().catch(() => false)
  if (!valid) return

  borrowSubmitting.value = true
  try {
    lastBorrow.value = await borrowBook({ ...borrowForm })
    ElMessage.success('借书办理成功')
    Object.assign(borrowForm, { readerNo: '', barcode: '' })
    borrowFormRef.value?.clearValidate()
    await loadBooks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '借书办理失败')
  } finally {
    borrowSubmitting.value = false
  }
}

function switchCirculationMode(mode) {
  circulationMode.value = mode
  lastBorrow.value = null
  lastReturn.value = null
}

async function submitReturn() {
  const valid = await returnFormRef.value.validate().catch(() => false)
  if (!valid) return

  returnSubmitting.value = true
  try {
    lastReturn.value = await returnBook({ ...returnForm })
    ElMessage.success('归还办理成功')
    returnForm.barcode = ''
    returnFormRef.value?.clearValidate()
    await loadBooks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '归还办理失败')
  } finally {
    returnSubmitting.value = false
  }
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
}

function exportDate() {
  return new Date().toLocaleDateString('sv-SE')
}

function exportCurrentRows(filename, columns, rows) {
  if (!rows.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  downloadCsv(`${filename}-${exportDate()}.csv`, columns, rows)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

function exportBooks() {
  exportCurrentRows('馆藏书目', [
    { label: 'ISBN', value: row => row.isbn },
    { label: '书名', value: row => row.title },
    { label: '作者', value: row => row.author },
    { label: '出版社', value: row => row.publisher },
    { label: '出版日期', value: row => row.publishDate },
    { label: '分类', value: row => row.categoryName },
    { label: '馆藏数量', value: row => row.totalCopies },
    { label: '可借数量', value: row => row.availableCopies },
    { label: '简介', value: row => row.description }
  ], books.value)
}

function exportReaders() {
  exportCurrentRows('读者名单', [
    { label: '借书证号', value: row => row.readerNo },
    { label: '手机号', value: row => row.phone },
    { label: '邮箱', value: row => row.email },
    { label: '最大借阅数', value: row => row.maxBorrowCount },
    { label: '状态', value: row => row.status },
    { label: '创建时间', value: row => formatDateTime(row.createdAt) }
  ], readers.value)
}

function exportBorrowRecords() {
  exportCurrentRows('借阅记录', [
    { label: '借书证号', value: row => row.readerNo },
    { label: '书名', value: row => row.bookTitle },
    { label: '馆藏条码', value: row => row.barcode },
    { label: '借出时间', value: row => formatDateTime(row.borrowedAt) },
    { label: '应还时间', value: row => formatDateTime(row.dueAt) },
    { label: '归还时间', value: row => formatDateTime(row.returnedAt) },
    { label: '续借次数', value: row => row.renewCount },
    { label: '借阅状态', value: row => row.status },
    { label: '罚款金额', value: row => Number(row.fineAmount || 0).toFixed(2) },
    { label: '已缴金额', value: row => Number(row.paidAmount || 0).toFixed(2) },
    { label: '罚款状态', value: row => row.fineStatus }
  ], borrowRecords.value)
}

onMounted(async () => {
  if (!currentUser.value) return
  await Promise.all([loadOverview(), loadBooks()])
})
</script>

<template>
  <section v-if="!currentUser" class="login-page">
    <div class="login-intro">
      <div class="login-brand"><span>L</span>书境</div>
      <p class="eyebrow">Library Management System</p>
      <h1>让馆藏与阅读，<br />在秩序中相遇。</h1>
      <p>连接 Oracle 数据库的图书馆管理控制台，为馆藏、读者与借阅流转提供统一管理。</p>
      <small>Oracle 12c · LJW4</small>
    </div>
    <div class="login-panel">
      <div class="login-card">
        <p class="eyebrow">Welcome Back</p>
        <h2>登录管理控制台</h2>
        <p class="login-help">请输入管理员账号，继续处理今天的图书馆事务。</p>
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" @keyup.enter="submitLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" size="large" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" size="large" show-password autocomplete="current-password" />
          </el-form-item>
          <el-button class="login-submit" type="primary" size="large" :loading="loginSubmitting" @click="submitLogin">
            登录系统
          </el-button>
        </el-form>
        <p class="login-tip">默认管理员账号：admin</p>
      </div>
    </div>
  </section>

  <template v-else>
  <el-container class="shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">L</div>
        <div>
          <strong>书境</strong>
          <span>Library Console</span>
        </div>
      </div>
      <el-menu :default-active="activeSection" class="menu" @select="switchSection">
        <el-menu-item index="overview"><el-icon><Collection /></el-icon>总览</el-menu-item>
        <el-menu-item index="books"><el-icon><Reading /></el-icon>馆藏查询</el-menu-item>
        <el-menu-item index="readers"><el-icon><User /></el-icon>读者管理</el-menu-item>
        <el-menu-item index="borrow"><el-icon><Switch /></el-icon>借阅办理</el-menu-item>
        <el-menu-item index="records"><el-icon><List /></el-icon>借阅记录</el-menu-item>
        <el-menu-item index="rules"><el-icon><Setting /></el-icon>借阅规则</el-menu-item>
        <el-menu-item index="logs"><el-icon><List /></el-icon>操作日志</el-menu-item>
      </el-menu>
      <div class="sidebar-user">
        <el-avatar size="small">{{ currentUser.displayName?.slice(0, 1) }}</el-avatar>
        <div><strong>{{ currentUser.displayName }}</strong><span>{{ currentUser.roles?.[0] }}</span></div>
        <div class="sidebar-user-actions">
          <el-button link @click="passwordDialogVisible = true">改密</el-button>
          <el-button link @click="logout">退出</el-button>
        </div>
      </div>
      <div class="oracle-badge">Oracle 12c · LJW4</div>
    </el-aside>

    <el-main v-if="activeSection === 'overview'" v-loading="overviewLoading" class="main">
      <header>
        <div>
          <p class="eyebrow">Library Overview</p>
          <h1>今天，也让图书馆有序运转。</h1>
          <p class="subtitle">核心数据直接来自 Oracle，帮助你快速掌握馆藏与借阅状态。</p>
        </div>
        <el-avatar size="large">览</el-avatar>
      </header>

      <section class="overview-grid">
        <article class="overview-card feature">
          <span>可借馆藏</span>
          <strong>{{ overview?.availableCopies ?? 0 }}</strong>
          <small>共 {{ overview?.totalCopies ?? 0 }} 册实体馆藏</small>
        </article>
        <article class="overview-card">
          <span>收录书目</span>
          <strong>{{ overview?.totalBooks ?? 0 }}</strong>
          <small>已录入系统的书目</small>
        </article>
        <article class="overview-card">
          <span>正常读者</span>
          <strong>{{ overview?.activeReaders ?? 0 }}</strong>
          <small>可正常办理借阅</small>
        </article>
        <article class="overview-card">
          <span>当前在借</span>
          <strong>{{ overview?.activeBorrows ?? 0 }}</strong>
          <small>尚未归还的馆藏</small>
        </article>
        <article class="overview-card alert-card">
          <span>已逾期</span>
          <strong>{{ overview?.overdueBorrows ?? 0 }}</strong>
          <small>超过应还日期的记录</small>
        </article>
        <article class="overview-card">
          <span>待缴罚款</span>
          <strong>¥{{ Number(overview?.unpaidFineAmount || 0).toFixed(2) }}</strong>
          <small>未缴与部分缴纳余额</small>
        </article>
      </section>

      <section class="overview-note">
        <div>
          <p class="eyebrow">Live Data</p>
          <h2>数据实时来自 Oracle</h2>
        </div>
        <p>进入总览页时自动刷新。借书、还书、缴费和读者状态变更后，再次进入即可查看最新统计。</p>
      </section>
    </el-main>

    <el-main v-else-if="activeSection === 'books'" class="main">
      <header>
        <div>
          <p class="eyebrow">馆藏管理中心</p>
          <h1>让每一本书，都恰好被找到。</h1>
          <p class="subtitle">查询馆藏状态，管理借阅流转，并掌握图书馆日常运行情况。</p>
        </div>
        <el-avatar size="large">管</el-avatar>
      </header>

      <section class="stats">
        <article><span>收录书目</span><strong>{{ books.length }}</strong><small>当前查询结果</small></article>
        <article><span>馆藏副本</span><strong>{{ totalCopies }}</strong><small>实体图书总数</small></article>
        <article class="highlight"><span>当前可借</span><strong>{{ availableCopies }}</strong><small>可立即办理借阅</small></article>
      </section>

      <section class="catalog">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Catalog</p>
            <h2>馆藏查询</h2>
          </div>
          <div class="catalog-actions">
            <el-input v-model="keyword" clearable placeholder="书名、作者或 ISBN" @keyup.enter="loadBooks">
              <template #append><el-button :icon="Search" @click="loadBooks" /></template>
            </el-input>
            <el-button :icon="Download" @click="exportBooks">导出当前结果</el-button>
            <el-button @click="openCategoryDialog">分类管理</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增书目</el-button>
          </div>
        </div>

        <el-alert v-if="errorMessage" :title="errorMessage" type="warning" :closable="false" show-icon />
        <el-table v-else v-loading="loading" :data="books" stripe>
          <el-table-column prop="title" label="书名" min-width="220" />
          <el-table-column prop="author" label="作者" min-width="130" />
          <el-table-column prop="categoryName" label="分类" width="110" />
          <el-table-column prop="isbn" label="ISBN" min-width="150" />
          <el-table-column prop="availableCopies" label="可借 / 馆藏" width="120">
            <template #default="{ row }">
              <strong class="availability">{{ row.availableCopies }}</strong> / {{ row.totalCopies }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="primary" @click="openCopyDialog(row)">登记副本</el-button>
              <el-button link type="primary" @click="openCopyListDialog(row)">副本明细</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-main>

    <el-main v-else-if="activeSection === 'readers'" class="main">
      <header>
        <div>
          <p class="eyebrow">读者服务中心</p>
          <h1>清晰管理每一位读者。</h1>
          <p class="subtitle">管理借书证、联系方式和借阅额度，为后续借阅办理做好准备。</p>
        </div>
        <el-avatar size="large">读</el-avatar>
      </header>

      <section class="stats">
        <article><span>读者总数</span><strong>{{ readers.length }}</strong><small>当前查询结果</small></article>
        <article><span>正常读者</span><strong>{{ activeReaders }}</strong><small>状态为 ACTIVE</small></article>
        <article class="highlight"><span>借阅额度</span><strong>{{ totalBorrowLimit }}</strong><small>最大可借总数</small></article>
      </section>

      <section class="catalog">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Readers</p>
            <h2>读者管理</h2>
          </div>
          <div class="catalog-actions">
            <el-input v-model="readerKeyword" clearable placeholder="借书证号、手机号或邮箱" @keyup.enter="loadReaders">
              <template #append><el-button :icon="Search" @click="loadReaders" /></template>
            </el-input>
            <el-button :icon="Download" @click="exportReaders">导出当前结果</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreateReaderDialog">新增读者</el-button>
          </div>
        </div>

        <el-table v-loading="readerLoading" :data="readers" stripe empty-text="暂无读者">
          <el-table-column prop="readerNo" label="借书证号" min-width="160" />
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="email" label="邮箱" min-width="210" />
          <el-table-column prop="maxBorrowCount" label="最大借阅数" width="120" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditReaderDialog(row)">编辑资料</el-button>
              <el-dropdown trigger="click" @command="changeReaderStatus(row, $event)">
                <el-button link type="primary">修改状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="ACTIVE" :disabled="row.status === 'ACTIVE'">设为正常</el-dropdown-item>
                    <el-dropdown-item command="SUSPENDED" :disabled="row.status === 'SUSPENDED'">暂停借阅</el-dropdown-item>
                    <el-dropdown-item command="CLOSED" :disabled="row.status === 'CLOSED'">注销读者</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-main>

    <el-main v-else-if="activeSection === 'borrow'" class="main">
      <header>
        <div>
          <p class="eyebrow">借阅流转中心</p>
          <h1>快速完成每一次借出。</h1>
          <p class="subtitle">输入借书证号与馆藏条码，系统会自动校验资格并计算应还日期。</p>
        </div>
        <el-avatar size="large">借</el-avatar>
      </header>

      <section class="borrow-layout">
        <article class="borrow-card">
          <el-segmented
            class="circulation-switch"
            :model-value="circulationMode"
            :options="[{ label: '办理借书', value: 'borrow' }, { label: '办理归还', value: 'return' }]"
            @change="switchCirculationMode"
          />
          <template v-if="circulationMode === 'borrow'">
            <p class="eyebrow">Borrow</p>
            <h2>办理借书</h2>
            <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-position="top">
              <el-form-item label="借书证号" prop="readerNo">
                <el-input v-model="borrowForm.readerNo" size="large" placeholder="扫描或输入借书证号" />
              </el-form-item>
              <el-form-item label="馆藏条码" prop="barcode">
                <el-input v-model="borrowForm.barcode" size="large" placeholder="扫描或输入馆藏条码" @keyup.enter="submitBorrow" />
              </el-form-item>
              <el-button class="borrow-submit" type="primary" size="large" :loading="borrowSubmitting" @click="submitBorrow">
                确认借出
              </el-button>
            </el-form>
          </template>
          <template v-else>
            <p class="eyebrow">Return</p>
            <h2>办理归还</h2>
            <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-position="top">
              <el-form-item label="馆藏条码" prop="barcode">
                <el-input v-model="returnForm.barcode" size="large" placeholder="扫描或输入馆藏条码" @keyup.enter="submitReturn" />
              </el-form-item>
              <p class="return-help">系统会自动查找在借记录，并计算可能产生的逾期费用。</p>
              <el-button class="borrow-submit" type="primary" size="large" :loading="returnSubmitting" @click="submitReturn">
                确认归还
              </el-button>
            </el-form>
          </template>
        </article>

        <article class="borrow-result">
          <template v-if="circulationMode === 'borrow' && lastBorrow">
            <p class="eyebrow">Completed</p>
            <h2>借书办理成功</h2>
            <dl>
              <dt>书名</dt><dd>{{ lastBorrow.bookTitle }}</dd>
              <dt>借书证号</dt><dd>{{ lastBorrow.readerNo }}</dd>
              <dt>馆藏条码</dt><dd>{{ lastBorrow.barcode }}</dd>
              <dt>借出时间</dt><dd>{{ formatDateTime(lastBorrow.borrowedAt) }}</dd>
              <dt>应还时间</dt><dd class="due-date">{{ formatDateTime(lastBorrow.dueAt) }}</dd>
            </dl>
          </template>
          <template v-else-if="circulationMode === 'return' && lastReturn">
            <p class="eyebrow">Completed</p>
            <h2>归还办理成功</h2>
            <dl>
              <dt>书名</dt><dd>{{ lastReturn.bookTitle }}</dd>
              <dt>借书证号</dt><dd>{{ lastReturn.readerNo }}</dd>
              <dt>馆藏条码</dt><dd>{{ lastReturn.barcode }}</dd>
              <dt>归还时间</dt><dd>{{ formatDateTime(lastReturn.returnedAt) }}</dd>
              <dt>逾期天数</dt><dd>{{ lastReturn.overdueDays }} 天</dd>
              <dt>应缴罚款</dt><dd class="due-date">¥ {{ Number(lastReturn.fineAmount).toFixed(2) }}</dd>
            </dl>
          </template>
          <template v-else>
            <p class="eyebrow">Ready</p>
            <h2>等待办理</h2>
            <p class="empty-copy">
              {{ circulationMode === 'borrow' ? '完成借书后，此处将显示书目信息和应还日期。' : '完成归还后，此处将显示逾期天数和罚款金额。' }}
            </p>
          </template>
        </article>
      </section>
    </el-main>

    <el-main v-else-if="activeSection === 'records'" class="main">
      <header>
        <div>
          <p class="eyebrow">借阅档案中心</p>
          <h1>掌握每一本书的流转。</h1>
          <p class="subtitle">查询借阅状态、归还时间与罚款信息，快速了解当前借阅情况。</p>
        </div>
        <el-avatar size="large">录</el-avatar>
      </header>

      <section class="stats">
        <article><span>记录数量</span><strong>{{ borrowRecords.length }}</strong><small>当前查询结果</small></article>
        <article><span>当前在借</span><strong>{{ activeBorrowRecords }}</strong><small>包含逾期记录</small></article>
        <article class="highlight"><span>未缴罚款</span><strong>¥{{ unpaidFineTotal.toFixed(2) }}</strong><small>当前结果未缴金额</small></article>
      </section>

      <section class="catalog">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Records</p>
            <h2>借阅记录</h2>
          </div>
          <div class="catalog-actions">
            <el-input v-model="borrowRecordKeyword" clearable placeholder="借书证号、馆藏条码或书名" @keyup.enter="loadBorrowRecords">
              <template #append><el-button :icon="Search" @click="loadBorrowRecords" /></template>
            </el-input>
            <el-button :icon="Download" @click="exportBorrowRecords">导出当前结果</el-button>
          </div>
        </div>

        <el-table v-loading="borrowRecordLoading" :data="borrowRecords" stripe empty-text="暂无借阅记录">
          <el-table-column prop="readerNo" label="借书证号" min-width="150" />
          <el-table-column prop="bookTitle" label="书名" min-width="190" />
          <el-table-column prop="barcode" label="馆藏条码" min-width="130" />
          <el-table-column label="借出时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.borrowedAt) }}</template>
          </el-table-column>
          <el-table-column label="应还时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.dueAt) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="105">
            <template #default="{ row }">
              <el-tag :type="borrowStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="renewCount" label="续借次数" width="100" />
          <el-table-column label="罚款" width="110">
            <template #default="{ row }">
              <span :class="{ 'fine-due': Number(row.fineAmount) > Number(row.paidAmount) }">
                ¥{{ Number(row.fineAmount || 0).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="145" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canRenew(row)" link type="primary" @click="renewRecord(row)">
                续借
              </el-button>
              <el-button v-if="['UNPAID', 'PARTIAL'].includes(row.fineStatus)" link type="danger" @click="openFineDialog(row)">
                缴费
              </el-button>
              <span v-if="!canRenew(row) && !['UNPAID', 'PARTIAL'].includes(row.fineStatus)" class="muted-action">—</span>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-main>

    <el-main v-else-if="activeSection === 'rules'" class="main">
      <header>
        <div>
          <p class="eyebrow">Library Rules</p>
          <h1>让借阅规则清晰且可控。</h1>
          <p class="subtitle">规则保存后，新办理的借书、续借和逾期罚款会立即使用最新设置。</p>
        </div>
        <el-avatar size="large">规</el-avatar>
      </header>

      <section class="stats">
        <article><span>默认借阅周期</span><strong>{{ ruleForm.borrowDays }}</strong><small>天</small></article>
        <article><span>最大续借次数</span><strong>{{ ruleForm.maxRenewCount }}</strong><small>次</small></article>
        <article class="highlight"><span>每日逾期费用</span><strong>¥{{ Number(ruleForm.finePerDay).toFixed(2) }}</strong><small>每册每天</small></article>
      </section>

      <section class="catalog rule-panel" v-loading="ruleLoading">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Settings</p>
            <h2>借阅规则设置</h2>
          </div>
        </div>
        <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="150px">
          <el-form-item label="默认借阅天数" prop="borrowDays">
            <el-input-number v-model="ruleForm.borrowDays" :min="1" :max="365" />
            <span class="rule-help">办理借书和续借时增加的天数，范围 1 至 365 天。</span>
          </el-form-item>
          <el-form-item label="最大续借次数" prop="maxRenewCount">
            <el-input-number v-model="ruleForm.maxRenewCount" :min="0" :max="20" />
            <span class="rule-help">设置为 0 表示不允许续借。</span>
          </el-form-item>
          <el-form-item label="每日逾期费用" prop="finePerDay">
            <el-input-number v-model="ruleForm.finePerDay" :min="0" :max="999.99" :precision="2" :step="0.5" />
            <span class="rule-help">归还时按逾期天数计算，可设置为 0。</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="ruleSubmitting" @click="submitLibraryRules">保存借阅规则</el-button>
          </el-form-item>
        </el-form>
      </section>
    </el-main>

    <el-main v-else class="main">
      <header>
        <div>
          <p class="eyebrow">Audit Trail</p>
          <h1>每一次关键操作，都有迹可循。</h1>
          <p class="subtitle">查看管理员对书目、读者、借阅和罚款产生的操作记录。</p>
        </div>
        <el-avatar size="large">志</el-avatar>
      </header>

      <section class="stats">
        <article><span>日志数量</span><strong>{{ operationLogs.length }}</strong><small>最近 200 条内筛选</small></article>
        <article><span>当前用户</span><strong>{{ currentUser.username }}</strong><small>{{ currentUser.roles?.[0] }}</small></article>
        <article class="highlight"><span>审计范围</span><strong>8</strong><small>关键业务动作</small></article>
      </section>

      <section class="catalog">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Operation Logs</p>
            <h2>操作日志</h2>
          </div>
          <div class="catalog-actions">
            <el-input v-model="operationLogKeyword" clearable placeholder="动作、目标、详情或操作人" @keyup.enter="loadOperationLogs">
              <template #append><el-button :icon="Search" @click="loadOperationLogs" /></template>
            </el-input>
          </div>
        </div>

        <el-table v-loading="operationLogLoading" :data="operationLogs" stripe empty-text="暂无操作日志">
          <el-table-column label="时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="135">
            <template #default="{ row }">
              <el-tag>{{ logActionLabel(row.action) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="displayName" label="操作人" width="130" />
          <el-table-column prop="targetType" label="目标类型" width="135" />
          <el-table-column prop="targetId" label="目标编号" width="100" />
          <el-table-column prop="detail" label="详情" min-width="300" />
          <el-table-column prop="ipAddress" label="IP 地址" width="130" />
        </el-table>
      </section>
    </el-main>
  </el-container>

  <el-dialog v-model="passwordDialogVisible" title="修改登录密码" width="460px" @closed="resetPasswordForm">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input v-model="passwordForm.currentPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSubmitting" @click="submitPasswordChange">确认修改</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dialogVisible" :title="editingBook ? '编辑书目' : '新增书目'" width="560px" @closed="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" maxlength="20" /></el-form-item>
      <el-form-item label="书名" prop="title"><el-input v-model="form.title" maxlength="200" /></el-form-item>
      <el-form-item label="作者"><el-input v-model="form.author" maxlength="150" /></el-form-item>
      <el-form-item label="出版社"><el-input v-model="form.publisher" maxlength="150" /></el-form-item>
      <el-form-item label="出版日期">
        <el-date-picker v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitBook">{{ editingBook ? '保存修改' : '确认新增' }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="categoryDialogVisible" title="图书分类管理" width="680px" @closed="resetCategoryForm">
    <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="88px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="categoryForm.categoryName" maxlength="80" placeholder="例如 科学" />
      </el-form-item>
      <el-form-item label="分类描述">
        <el-input v-model="categoryForm.description" type="textarea" :rows="2" maxlength="500" placeholder="可选" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="categorySubmitting" @click="submitCategory">
          {{ editingCategory ? '保存分类修改' : '新增分类' }}
        </el-button>
        <el-button v-if="editingCategory" @click="resetCategoryForm">取消编辑</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="categories" stripe empty-text="暂无分类">
      <el-table-column prop="categoryName" label="分类名称" width="150" />
      <el-table-column prop="description" label="描述" min-width="300">
        <template #default="{ row }">{{ row.description || '暂无描述' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="editCategory(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="copyDialogVisible" title="登记馆藏副本" width="460px" @closed="resetCopyForm">
    <p class="dialog-context">书目：<strong>{{ selectedBook?.title }}</strong></p>
    <el-form ref="copyFormRef" :model="copyForm" :rules="copyRules" label-width="88px">
      <el-form-item label="馆藏条码" prop="barcode">
        <el-input v-model="copyForm.barcode" maxlength="50" placeholder="例如 LIB-000005" />
      </el-form-item>
      <el-form-item label="书架位置">
        <el-input v-model="copyForm.shelfLocation" maxlength="80" placeholder="例如 A-01-03" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="copyDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="copySubmitting" @click="submitCopy">确认登记</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="copyListDialogVisible" title="馆藏副本明细" width="760px" @closed="resetCopyList">
    <p class="dialog-context">书目：<strong>{{ selectedCopyBook?.title }}</strong></p>
    <el-table v-loading="copyListLoading" :data="bookCopies" stripe empty-text="暂无馆藏副本">
      <el-table-column prop="barcode" label="馆藏条码" min-width="150" />
      <el-table-column prop="shelfLocation" label="书架位置" min-width="130">
        <template #default="{ row }">
          <el-input
            v-if="copyLocationEditingId === row.id"
            v-model="copyLocationDraft"
            maxlength="80"
            size="small"
            placeholder="未设置"
            @keyup.enter="submitCopyLocation(row)"
          />
          <span v-else>{{ row.shelfLocation || '未设置' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="acquiredAt" label="入馆日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="copyStatusType(row.status)">{{ copyStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <template v-if="copyLocationEditingId === row.id">
            <el-button link type="primary" :loading="copyLocationSubmitting" @click="submitCopyLocation(row)">保存</el-button>
            <el-button link @click="cancelCopyLocationEdit">取消</el-button>
          </template>
          <template v-else>
            <el-button link type="primary" @click="startCopyLocationEdit(row)">编辑位置</el-button>
            <span v-if="row.status === 'BORROWED'" class="muted-action">请先归还</span>
            <el-dropdown v-else trigger="click" :disabled="copyStatusUpdating === row.id" @command="changeCopyStatus(row, $event)">
              <el-button link type="primary" :loading="copyStatusUpdating === row.id">修改状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="AVAILABLE" :disabled="row.status === 'AVAILABLE'">设为可借</el-dropdown-item>
                  <el-dropdown-item command="MAINTENANCE" :disabled="row.status === 'MAINTENANCE'">设为维护中</el-dropdown-item>
                  <el-dropdown-item command="DAMAGED" :disabled="row.status === 'DAMAGED'">设为损坏</el-dropdown-item>
                  <el-dropdown-item command="LOST" :disabled="row.status === 'LOST'">设为遗失</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <el-dialog v-model="readerDialogVisible" :title="editingReader ? '编辑读者资料' : '新增读者'" width="500px" @closed="resetReaderForm">
    <el-form ref="readerFormRef" :model="readerForm" :rules="readerRules" label-width="100px">
      <el-form-item label="借书证号" prop="readerNo">
        <el-input v-model="readerForm.readerNo" maxlength="30" placeholder="例如 R-2026-0001" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="readerForm.phone" maxlength="30" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="readerForm.email" maxlength="120" />
      </el-form-item>
      <el-form-item label="最大借阅数" prop="maxBorrowCount">
        <el-input-number v-model="readerForm.maxBorrowCount" :min="1" :max="99" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="readerDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="readerSubmitting" @click="submitReader">{{ editingReader ? '保存修改' : '确认新增' }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="fineDialogVisible" title="罚款缴费" width="460px" @closed="resetFineForm">
    <p class="dialog-context">读者：<strong>{{ selectedFineRecord?.readerNo }}</strong> · {{ selectedFineRecord?.bookTitle }}</p>
    <div class="fine-summary">
      <span>应缴 ¥{{ Number(selectedFineRecord?.fineAmount || 0).toFixed(2) }}</span>
      <span>已缴 ¥{{ Number(selectedFineRecord?.paidAmount || 0).toFixed(2) }}</span>
      <strong>剩余 ¥{{ remainingFine(selectedFineRecord || {}).toFixed(2) }}</strong>
    </div>
    <el-form ref="fineFormRef" :model="fineForm" :rules="fineRules" label-width="100px">
      <el-form-item label="本次缴费" prop="amount">
        <el-input-number v-model="fineForm.amount" :min="0.01" :max="remainingFine(selectedFineRecord || {})" :precision="2" :step="0.5" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="fineDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="fineSubmitting" @click="submitFinePayment">确认缴费</el-button>
    </template>
  </el-dialog>
  </template>
</template>
