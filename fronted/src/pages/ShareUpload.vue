<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 分享上传页面头部 -->
    <div class="bg-white border-b border-gray-200">
      <div class="max-w-6xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <svg class="w-8 h-8 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
            <h1 class="text-xl font-semibold text-gray-900">创建新分享</h1>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 py-8">
      <div class="bg-white border border-gray-200 rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">分享详情</h2>
          <p class="mt-1 text-sm text-gray-600">和伙伴们分享你的经验</p>
        </div>

        <form @submit.prevent="submitShare" class="p-6 space-y-6">
          <!-- 分享标题 -->
          <div>
            <label for="title" class="block text-sm font-medium text-gray-900 mb-2">
              分享标题 <span class="text-red-500">*</span>
            </label>
            <input
              id="title"
              v-model="shareForm.title"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="某个领域学习经验/求职工作经历/生涯规划感悟都可以哦"
            />
            <p class="mt-1 text-xs text-gray-500">好的分享标题应该容易记忆且高度概括</p>
          </div>

          <!-- 分享描述 -->
          <div>
            <label for="description" class="block text-sm font-medium text-gray-900 mb-2">
              分享描述 <span class="text-gray-500">(可选)</span>
            </label>
            <textarea
              id="description"
              v-model="shareForm.content"
              rows="3"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="简要描述你的分享（15个字以内）"
            ></textarea>
          </div>

          <!-- 分享文档 -->
          <div>
            <label for="documentFile" class="block text-sm font-medium text-gray-900 mb-2">
              分享文档 <span class="text-red-500">*</span>
            </label>
            <div class="border-2 border-dashed border-gray-300 rounded-md p-4 text-center hover:border-gray-400 transition-colors">
              <el-upload
                ref="uploadRef"
                class="upload-dragger"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :file-list="fileList"
                :limit="1"
                :on-exceed="handleExceed"
                accept=".md"
              >
                <div v-if="fileList.length === 0" class="cursor-pointer">
                  <svg class="w-8 h-8 text-gray-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                  </svg>
                  <p class="text-sm text-gray-600 mb-1">点击上传分享文档</p>
                  <p class="text-xs text-gray-500">支持 .md 格式文件</p>
                </div>
                <div v-else class="text-green-600">
                  <svg class="w-8 h-8 mx-auto mb-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                  </svg>
                  <p class="text-sm font-medium">文件已选择</p>
                </div>
              </el-upload>
              
              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-4">
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div class="bg-blue-600 h-2 rounded-full transition-all duration-300" :style="{width: uploadProgress + '%'}"></div>
                </div>
                <p class="text-xs text-gray-500 mt-1">上传进度: {{ uploadProgress }}%</p>
              </div>
            </div>
          </div>

          <!-- 分享分类 -->
          <div>
            <label for="category" class="block text-sm font-medium text-gray-900 mb-2">
              分享分类 <span class="text-red-500">*</span>
            </label>
            <select
              id="category"
              v-model="shareForm.category"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">选择分享分类</option>
              <option
                v-for="category in categories"
                :key="category.value"
                :value="category.value"
              >
                {{ category.label }}
              </option>
            </select>
          </div>

          <!-- 提交按钮 -->
          <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              @click="resetForm"
              class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              重置
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ submitting ? '提交中...' : '提交分享' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const uploadRef = ref()
const selectedFile = ref(null)

// 表单数据
const shareForm = reactive({
  title: '',
  category: '',
  content: '',
  tags: ''
})

// 分类选项
const categories = [
  { label: '学习经验', value: 'learn' },
  { label: '求职经验', value: 'job' },
  { label: '规划建议', value: 'plan' },
  { label: '其他', value: 'others' }
]

const fileList = ref([])
const uploadProgress = ref(0)
const submitting = ref(false)

const getAccessToken = () => {
  let token = ''
  try {
    const raw = localStorage.getItem('authState')
    if (raw) {
      const parsed = JSON.parse(raw)
      token = parsed.accessToken || parsed.token || ''
    }
  } catch (e) {
    console.error('获取 token 失败:', e)
  }

  if (!token) {
    token = localStorage.getItem('token') || ''
  }

  return token
}

// 文件校验
const beforeUpload = (file) => {
  const isValidType = file.name.toLowerCase().endsWith('.md')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传 .md 格式的文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }

  return true
}

const handleFileChange = (uploadFile, uploadFiles) => {
  const raw = uploadFile?.raw
  if (!raw) return

  selectedFile.value = raw
  fileList.value = uploadFiles.slice(-1)
  uploadProgress.value = 100
}

const handleFileRemove = () => {
  selectedFile.value = null
  fileList.value = []
  uploadProgress.value = 0
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除当前文件再选择')
}

const resetForm = () => {
  shareForm.title = ''
  shareForm.category = ''
  shareForm.content = ''
  shareForm.tags = ''
  selectedFile.value = null
  fileList.value = []
  uploadProgress.value = 0
  uploadRef.value?.clearFiles?.()
}

const validateForm = () => {
  if (!shareForm.title.trim()) {
    ElMessage.error('请输入分享标题')
    return false
  }
  if (!shareForm.category) {
    ElMessage.error('请选择分享分类')
    return false
  }
  if (!selectedFile.value) {
    ElMessage.error('请先选择 Markdown 文档')
    return false
  }
  return true
}

const submitShare = async () => {
  if (submitting.value) return
  if (!validateForm()) return

  try {
    submitting.value = true

    const formData = new FormData()
    formData.append('title', shareForm.title.trim())
    formData.append('content', shareForm.content || '')
    formData.append('category', shareForm.category || 'others')
    formData.append('tags', shareForm.tags || '')
    formData.append('documentFile', selectedFile.value)

    const token = getAccessToken()
    const headers = token ? { Authorization: `Bearer ${token}` } : {}

    const response = await axios.post('/api/senior-shares', formData, { headers })
    const body = response?.data || {}
    const isSuccess = body.success === true || body.code === 0

    if (!isSuccess) {
      throw new Error(body.message || '分享失败，请重试!')
    }

    ElMessage.success('经验分享成功!')

    const shareId = body?.data?.id || body?.id
    const result = await ElMessageBox.confirm(
      '经验分享成功！是否立即查看你的分享？',
      '分享成功',
      {
        confirmButtonText: '查看分享',
        cancelButtonText: '返回列表',
        type: 'success'
      }
    ).catch(() => 'cancel')

    if (result === 'confirm' && shareId) {
      router.push(`/share/${shareId}`)
    } else {
      router.push('/share')
    }
  } catch (error) {
    console.error('Submit error:', error)
    const message = error?.response?.data?.message || error?.message || '分享失败，请重试!'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss">
@import '../styles/variables';
/* 自定义样式 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 上传组件样式覆盖 */
:deep(.el-upload-dragger) {
  border: none !important;
  background: transparent !important;
  width: 100% !important;
  height: auto !important;
  padding: 0 !important;
}

:deep(.el-upload-dragger:hover) {
  border: none !important;
  background: transparent !important;
}

/* 输入框样式 */
input[type="text"],
input[type="email"],
input[type="password"],
input[type="url"],
input[type="search"],
input[type="tel"],
input[type="number"],
textarea,
select {
  background-color: white;
  color: #374151; /* gray-700 */
  border-color: #d1d5db; /* gray-300 */
}

/* 深色模式兼容性 - 聚焦下强制保持输入框样式 */
input[type="text"]:focus,
input[type="email"]:focus,
input[type="password"]:focus,
input[type="url"]:focus,
input[type="search"]:focus,
input[type="tel"]:focus,
input[type="number"]:focus,
textarea:focus,
select:focus {
  background-color: $accent-blue-surface !important;
  color: $text-color !important;
  border-color: $accent-blue-glow !important; /* blue-500 */
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

input[type="text"]::placeholder,
input[type="email"]::placeholder,
input[type="password"]::placeholder,
input[type="url"]::placeholder,
input[type="search"]::placeholder,
input[type="tel"]::placeholder,
input[type="number"]::placeholder,
textarea::placeholder {
  color: #9ca3af; /* gray-400 */
}

/* 文件输入框样式 */
input[type="file"] {
  background-color: transparent;
  color: inherit;
}

/* 下拉选择框 */
select option {
  background-color: white;
  color: #374151;
}
</style>