<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 上传页面头部 -->
    <div class="bg-white border-b border-gray-200">
      <div class="max-w-6xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <svg class="w-8 h-8 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
              <path d="M19 5h-2V3a1 1 0 00-2 0v2H9V3a1 1 0 00-2 0v2H5a2 2 0 00-2 2v12a2 2 0 002 2h14a2 2 0 002-2V7a2 2 0 00-2-2zm0 14H5V9h14v10z"/>
            </svg>
            <h1 class="text-xl font-semibold text-gray-900">上传推文</h1>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 py-8">
      <div class="bg-white border border-gray-200 rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">推文详情</h2>
          <p class="mt-1 text-sm text-gray-600">分享微信公众号推送到华创知识库</p>
        </div>

        <form @submit.prevent="submitArticle" class="p-6 space-y-6">
          <!-- 推文标题 -->
          <div>
            <label for="title" class="block text-sm font-medium text-gray-900 mb-2">
              推文标题 <span class="text-red-500">*</span>
            </label>
            <input
              id="title"
              v-model="articleForm.title"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="请输入推文标题"
            />
          </div>

          <!-- 推文摘要 -->
          <div>
            <label for="summary" class="block text-sm font-medium text-gray-900 mb-2">
              推文摘要 <span class="text-red-500">*</span>
            </label>
            <textarea
              id="summary"
              v-model="articleForm.summary"
              rows="3"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="简要描述推文内容"
            ></textarea>
          </div>

          <!-- 推文URL -->
          <div>
            <label for="url" class="block text-sm font-medium text-gray-900 mb-2">
              推文链接 <span class="text-red-500">*</span>
            </label>
            <input
              id="url"
              v-model="articleForm.url"
              type="url"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="https://mp.weixin.qq.com/..."
            />
          </div>

          <!-- 发布时间和来源 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="publishTime" class="block text-sm font-medium text-gray-900 mb-2">
                发布时间 <span class="text-red-500">*</span>
              </label>
              <input
                id="publishTime"
                v-model="articleForm.publishTime"
                type="datetime-local"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
              <p class="mt-1 text-xs text-gray-500">选择推文在微信发布的日期时间</p>
            </div>

            <div>
              <label for="source" class="block text-sm font-medium text-gray-900 mb-2">
                来源 <span class="text-red-500">*</span>
              </label>
              <select
                id="source"
                v-model="articleForm.source"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="undergrad">本科</option>
                <option value="postgrad">研究生</option>
              </select>
            </div>
          </div>

          <!-- 封面图片上传 -->
          <div>
            <label class="block text-sm font-medium text-gray-900 mb-2">
              封面图片 <span class="text-red-500">*</span>
            </label>
            <div class="border-2 border-dashed border-gray-300 rounded-md p-4 text-center hover:border-gray-400 transition-colors">
              <el-upload
                ref="uploadRef"
                class="upload-dragger"
                drag
                :action="uploadUrl"
                :headers="uploadHeaders"
                :before-upload="beforeImageUpload"
                :on-success="handleImageUploadSuccess"
                :on-error="handleUploadError"
                :on-progress="handleUploadProgress"
                :file-list="fileList"
                :limit="1"
                :on-exceed="handleExceed"
                accept="image/*"
              >
                <div v-if="fileList.length === 0" class="cursor-pointer">
                  <svg class="w-8 h-8 text-gray-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                  </svg>
                  <p class="text-sm text-gray-600 mb-1">点击上传封面图片</p>
                  <p class="text-xs text-gray-500">支持 JPG、PNG 格式</p>
                </div>
                <div v-else class="text-green-600">
                  <svg class="w-8 h-8 mx-auto mb-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                  </svg>
                  <p class="text-sm font-medium">图片已上传</p>
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
              {{ submitting ? '提交中...' : '提交' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const uploadRef = ref()

// 表单数据
const articleForm = reactive({
  title: '',
  summary: '',
  url: '',
  coverImage: '',
  publishTime: '',
  source: 'undergrad'
})

// 上传相关
const fileList = ref([])
const uploadProgress = ref(0)
const submitting = ref(false)

const uploadUrl = computed(() => '/api/files/upload')
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
})

// 图片上传前检查
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }

  uploadProgress.value = 0
  return true
}

// 上传进度
const handleUploadProgress = (event) => {
  uploadProgress.value = Math.round((event.loaded / event.total) * 100)
}

// 图片上传成功
const handleImageUploadSuccess = (response) => {
  uploadProgress.value = 100
  if (response.success) {
    articleForm.coverImage = response.data.url || response.data.path
    ElMessage.success('图片上传成功!')
  } else {
    ElMessage.error(response.message || '图片上传失败!')
  }
}

// 上传失败
const handleUploadError = (error) => {
  uploadProgress.value = 0
  console.error('Upload error:', error)
  ElMessage.error('上传失败，请重试!')
}

// 文件数量超限
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件!')
}

// 重置表单
const resetForm = () => {
  articleForm.title = ''
  articleForm.summary = ''
  articleForm.url = ''
  articleForm.coverImage = ''
  articleForm.publishTime = ''
  articleForm.source = 'undergrad'
  fileList.value = []
  uploadProgress.value = 0
}

// 提交表单
const submitArticle = async () => {
  // 表单验证
  if (!articleForm.title.trim()) {
    ElMessage.error('请输入推文标题')
    return
  }
  if (!articleForm.summary.trim()) {
    ElMessage.error('请输入推文摘要')
    return
  }
  if (!articleForm.url.trim()) {
    ElMessage.error('请输入推文链接')
    return
  }
  if (!articleForm.coverImage) {
    ElMessage.error('请上传封面图片')
    return
  }

  try {
    submitting.value = true

    // 准备提交数据
    const submitData = {
      title: articleForm.title,
      summary: articleForm.summary,
      linkUrl: articleForm.url,
      coverImage: articleForm.coverImage,
      publishTime: articleForm.publishTime ? new Date(articleForm.publishTime).toISOString() : null,
      source: articleForm.source
    }

    // 提交到后端
    const response = await axios.post('/api/articles', submitData)

    if (response.data.success) {
      ElMessage.success('推文上传成功!')
      router.push('/articles')
    } else {
      ElMessage.error(response.data.message || '上传失败，请重试!')
    }
  } catch (error) {
    console.error('Submit error:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('上传失败，请重试!')
    }
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
  color: #374151;
  border-color: #d1d5db;
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
  border-color: $accent-blue-glow !important;
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
  color: #9ca3af;
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
