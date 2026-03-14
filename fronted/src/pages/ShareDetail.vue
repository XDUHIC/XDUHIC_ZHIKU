<template>
  <MainLayout>
    <div class="share-detail-page">
      <div class="page-header">
        <div class="header-cartoon header-cartoon--left">
          <img :src="cartoon1" alt="cartoon decoration" />
        </div>
        <div class="header-content">
          <h1 class="page-title">{{ detail.title || '经验分享详情' }}</h1>
          <div class="page-meta">
            <span v-if="detail.category" class="meta-item">
              <i class="fas fa-tag"></i>
              {{ getCategoryLabel(detail.category) }}
            </span>
            <span v-if="detail.createdAt" class="meta-item">
              <i class="fas fa-calendar-alt"></i>
              {{ formatDateTime(detail.createdAt) }}
            </span>
            <span v-if="detail.viewCount != null" class="meta-item">
              <i class="fas fa-eye"></i> {{ detail.viewCount }} 次查看
            </span>
          </div>
        </div>
        <div class="header-cartoon header-cartoon--right">
          <img :src="cartoon2" alt="cartoon decoration"/>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        加载中...
      </div>

      <template v-else>
        <div class="content-container">
          <div class="document-content">
            <div v-if="htmlContent" class="markdown-content" v-html="htmlContent"></div>
            <div v-else class="empty">暂未找到分享内容</div>
          </div>
        </div>

        <section class="discussion-section" v-if="!isStaticDetail">
          <h3>讨论区</h3>
          <!--复用答疑模块-->
          <p class="discussion-tip">围绕本篇分享提问与交流~</p>

          <div class="discussion-input">
            <el-input
                v-model="newDiscussionContent"
                type="textarea"
                :rows="6"
                placeholder="写下你的问题或想法..."
            />
            <el-button type="primary" :loading="postingDiscussion" @click="submitDiscussion">发布讨论</el-button>
          </div>

          <div v-if="discussionLoading" class="loading">讨论加载中...</div>
          <div v-else-if="discussions.length === 0" class="empty">暂时还没有讨论，来发第一条吧～</div>
          <div v-else class="discussion-list">
            <article v-for="item in discussions" :key="item.id" class="discussion-item">

              <div class="discussion-main">
                <div class="avatar-wrapper">
                  <el-avatar :size="40" :src="item.authorAvatar">
                    {{ item.authorName ? item.authorName.charAt(0) : '匿' }}
                  </el-avatar>
                </div>

                <div class="content-wrapper">
                  <div class="discussion-author">{{ item.authorName || '匿名用户' }}</div>
                  <div class="discussion-content">{{ item.content }}</div>
                  <div class="discussion-meta">{{ formatDateTime(item.createdAt) }}</div>
                </div>
              </div>

              <div class="answer-list" v-if="discussionAnswers[item.id]?.length">
                <div v-for="(ans, index) in discussionAnswers[item.id]" :key="ans.id" class="answer-item">
                  <el-avatar :size="24" :src="ans.authorAvatar" class="answer-avatar">
                    {{ ans.authorName ? ans.authorName.charAt(0) : '匿' }}
                  </el-avatar>

                  <div class="answer-content">
                    <div class="answer-header">
                      <span class="answer-author">{{ ans.authorName || '匿名用户' }}</span>
                      <span class="answer-time">{{ formatDateTime(ans.createdAt) }}</span>
                    </div>
                    <div class="answer-text">{{ ans.content }}</div>
                  </div>
                </div>
              </div>

              <div class="answer-input">
                <el-input
                    v-model="replyDraft[item.id]"
                    type="textarea"
                    :rows="4"
                    placeholder="回复这条讨论..."
                />
                <el-button size="small" :loading="postingAnswerId === item.id" @click="submitAnswer(item.id)">回复</el-button>
              </div>
            </article>
          </div>
        </section>
      </template>

    </div>
  </MainLayout>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useAuthStore} from '../stores'
import {createDiscussionAnswer, createShareDiscussion, getDiscussionAnswers, getShareDiscussions} from '../api/shares'
import {useRoute, useRouter} from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import { getShareDetail } from '../api/shares'
import {config} from '../utils/config.js'
// 正确导入图片资源
import cartoon1 from '../assets/cartoon1.jpg'
import cartoon2 from '../assets/cartoon2.png'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const detail = ref({})
const htmlContent = ref('')
const authStore = useAuthStore()
const discussions = ref([])
const discussionAnswers = ref({})
const discussionLoading = ref(false)
const newDiscussionContent = ref('')
const postingDiscussion = ref(false)
const postingAnswerId = ref(null)
const replyDraft = ref({})
const isStaticDetail = ref(false)

const notify = (type, message) => {
  if (typeof ElMessage !== 'undefined' && ElMessage && typeof ElMessage[type] === 'function') {
    ElMessage[type](message)
    return
  }
  console[type === 'error' ? 'error' : 'warn'](`[notify:${type}] ${message}`)
}

// 记录 Markdown 文档的基础路径（用于修正相对资源路径）
let currentMdBaseUrl = ''

const getAbsoluteUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  return config.getUploadUrl(url)
}

const toAbsoluteWithBase = (maybeRelative, base) => {
  if (!maybeRelative) return maybeRelative
  // data URL 或锚点、邮件等不处理
  if (/^(data:|mailto:|tel:|#)/i.test(maybeRelative)) return maybeRelative
  // 已是绝对 URL
  if (/^https?:\/\//i.test(maybeRelative)) return maybeRelative
  if (maybeRelative.startsWith('/')) return maybeRelative
  
  // 如果base存在且不是绝对URL，尝试拼接
  if (base && !base.startsWith('http')) {
    try {
      // 处理相对路径
      if (maybeRelative.startsWith('./')) {
        maybeRelative = maybeRelative.substring(2)
      }
      if (maybeRelative.startsWith('../')) {
        // 计算上级目录
        const baseParts = base.split('/').filter(part => part)
        const relativeParts = maybeRelative.split('/').filter(part => part)
        
        // 计算需要回退的层级
        let backLevels = 0
        for (let part of relativeParts) {
          if (part === '..') {
            backLevels++
          } else {
            break
          }
        }
        
        // 构建最终路径
        const finalParts = baseParts.slice(0, -backLevels)
        const fileParts = relativeParts.slice(backLevels)
        const result = '/' + finalParts.join('/') + '/' + fileParts.join('/')
        return result
      } else {
        // 直接拼接
        const result = base + maybeRelative
        return result
      }
    } catch (error) {
      console.error('路径处理错误:', error)
    }
  }

  // 如果base不存在或处理失败，使用config.getUploadUrl处理
  return config.getUploadUrl(maybeRelative)
}

const postProcessHtmlWithBase = (rawHtml, base) => {
  if (!rawHtml) return rawHtml
  const container = document.createElement('div')
  container.innerHTML = rawHtml
  
  // 处理图片
  container.querySelectorAll('img').forEach(img => {
    const src = img.getAttribute('src')
    if (src) {
      const fixed = toAbsoluteWithBase(src, base)
      if (fixed) {
        img.setAttribute('src', fixed)
      }
    }
  })
  
  // 处理链接
  container.querySelectorAll('a').forEach(a => {
    const href = a.getAttribute('href')
    if (href) {
      const fixed = toAbsoluteWithBase(href, base)
      if (fixed) {
        a.setAttribute('href', fixed)
      }
    }
  })
  
  return container.innerHTML
}

const loadExternalLibraries = async () => {
  return new Promise((resolve, reject) => {
    let loadedCount = 0
    const totalLibs = 2

    const checkComplete = () => {
      loadedCount++
      if (loadedCount === totalLibs) {
        resolve()
      }
    }

    // 加载 marked
    const markedScript = document.createElement('script')
    markedScript.src = 'https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js'
    markedScript.onload = checkComplete
    markedScript.onerror = () => reject(new Error('Failed to load marked'))
    document.head.appendChild(markedScript)

    // 加载 DOMPurify
    const purifyScript = document.createElement('script')
    purifyScript.src = 'https://cdn.jsdelivr.net/npm/dompurify@3.0.9/dist/purify.min.js'
    purifyScript.onload = checkComplete
    purifyScript.onerror = () => reject(new Error('Failed to load DOMPurify'))
    document.head.appendChild(purifyScript)
  })
}

const loadMarkdownAndRender = async (mdTextOrUrl) => {
  try {
    let mdText = ''
    currentMdBaseUrl = ''

    const isLikelyUrl = /^https?:\/\//.test(mdTextOrUrl) || mdTextOrUrl.startsWith('/') || mdTextOrUrl.startsWith('uploads/')
    
    if (isLikelyUrl) {
      // 为markdown文件URL添加时间戳参数，强制清理缓存
      const separator = mdTextOrUrl.includes('?') ? '&' : '?'
      const urlWithTimestamp = `${mdTextOrUrl}${separator}_t=${Date.now()}&_cache=no`
      const abs = getAbsoluteUrl(urlWithTimestamp)
      
      // 记录该文档所在目录作为 base，用于修正相对路径资源
      if (abs.startsWith('http')) {
        try {
          const u = new URL(abs)
          // 去掉文件名，保留目录
          currentMdBaseUrl = `${u.origin}${u.pathname.substring(0, u.pathname.lastIndexOf('/') + 1)}`
        } catch (_) {
          currentMdBaseUrl = ''
        }
      } else {
        // 处理相对路径，计算基础路径
        const pathParts = abs.split('/').filter(part => part) // 过滤空字符串
        pathParts.pop() // 移除文件名
        currentMdBaseUrl = '/' + pathParts.join('/') + '/'
      }
      
      // 使用fetch而不是axios，避免可能的拦截器问题，并添加缓存控制
      const response = await fetch(abs, {
        cache: 'no-cache',
        headers: {
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0',
          'If-Modified-Since': '0'
        }
      })
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      mdText = await response.text()
    } else {
      mdText = mdTextOrUrl || ''
    }

    if (!mdText || mdText.trim() === '') {
      htmlContent.value = '<p style="color: orange;">暂无内容</p>'
      return
    }

    // 使用本地安装的marked和DOMPurify，如果失败则回退到CDN
    try {
      marked.setOptions({breaks: true, gfm: true})
      const rawHtml = marked.parse(mdText)

      // 用 DOMPurify 清洗前，先按 base 修正相对路径（避免相对资源 404）
      const withFixedLinks = postProcessHtmlWithBase(rawHtml, currentMdBaseUrl)

      htmlContent.value = DOMPurify.sanitize(withFixedLinks)
    } catch (e) {
      console.warn('本地库渲染失败，尝试使用CDN:', e)
      // 如果本地库失败，尝试使用CDN
      if (!window.marked || !window.DOMPurify) {
        await loadExternalLibraries()
      }

      window.marked.setOptions({breaks: true, gfm: true})
      const rawHtml = window.marked.parse(mdText)
      const withFixedLinks = postProcessHtmlWithBase(rawHtml, currentMdBaseUrl)
      htmlContent.value = window.DOMPurify.sanitize(withFixedLinks)
    }
  } catch (e) {
    console.error('渲染 Markdown 失败:', e)
    htmlContent.value = `<p style="color: red;">加载文档失败: ${e.message}</p>`
  }
}

const normalizeShareResponse = (body) => {
  if (!body || typeof body !== 'object') {
    return {}
  }

  const isSuccess = body.success === true || body.code === 0 || body.code === 200
  if (!isSuccess) {
    throw new Error(body.message || '获取分享详情失败')
  }

  return body.data ?? body
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    isStaticDetail.value = route.query.isStatic === 'true'

    if (isStaticDetail.value) {
      detail.value = {
        title: `${route.query.author || '优秀校友'}的经验分享`,
        category: 'learn',
        createdAt: new Date().toISOString(),
        viewCount: null
      }
      await loadMarkdownAndRender(`/uploads/shares/contents/${id}.md`)
    } else {
      const response = await getShareDetail(id)
      const share = normalizeShareResponse(response?.data)
      detail.value = share || {}

      const contentUrl = share?.textUrl || `/uploads/shares/contents/${id}.md`
      await loadMarkdownAndRender(contentUrl)
    }
  } catch (e) {
    console.error('获取分享详情失败:', e)
    detail.value = {}
    htmlContent.value = '<div class="empty">获取内容失败，请确保后端对应目录下存在该 md 文件。</div>'
  } finally {
    loading.value = false
  }
}
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', {year: 'numeric', month: 'long', day: 'numeric'})
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getCategoryLabel = (category) => {
  const labelMap = {
    'learn': '学习经验',
    'job': '求职经验',
    'plan': '规划建议',
    'others': '其他'
  }
  return labelMap[category] || '其他'
}

const getCategoryType = (category) => {
  const typeMap = {
    'learn': 'primary',
    'job': 'success',
    'plan': 'warning',
    'others': 'info'
  }
  return typeMap[category] || 'info'
}

const goBack = () => {
  router.push('/share')
}

const loadDiscussions = async () => {
  if (isStaticDetail.value) return
  const shareId = route.params.id
  discussionLoading.value = true
  try {
    const { data } = await getShareDiscussions(shareId, { page: 1, size: 20 })
    const list = data?.data?.data || []
    discussions.value = list

    const answerMap = {}
    await Promise.all(list.map(async (q) => {
      const resp = await getDiscussionAnswers(q.id, { page: 1, size: 50 })
      answerMap[q.id] = resp.data?.data?.data || []
    }))
    discussionAnswers.value = answerMap
  } catch (e) {
    console.error('加载讨论失败', e)
  } finally {
    discussionLoading.value = false
  }
}

const submitDiscussion = async () => {
  if (!authStore.isLoggedIn) {
    notify('warning', '请先登录再发布讨论')
    router.push('/auth/login')
    return
  }
  const content = (newDiscussionContent.value || '').trim()
  if (!content) {
    notify('warning', '讨论内容不能为空')
    return
  }
  postingDiscussion.value = true
  try {
    const title = content.length > 24 ? `${content.slice(0, 24)}...` : content
    await createShareDiscussion(route.params.id, { title, content })
    newDiscussionContent.value = ''
    notify('success', '讨论发布成功')
    await loadDiscussions()
  } catch (e) {
    notify('error', e.response?.data?.message || '讨论发布失败')
  } finally {
    postingDiscussion.value = false
  }
}

const submitAnswer = async (questionId) => {
  if (!authStore.isLoggedIn) {
    notify('warning', '请先登录再回复')
    router.push('/auth/login')
    return
  }
  const content = (replyDraft.value[questionId] || '').trim()
  if (!content) {
    notify('warning', '回复内容不能为空')
    return
  }
  postingAnswerId.value = questionId
  try {
    await createDiscussionAnswer(questionId, { content })
    replyDraft.value[questionId] = ''
    notify('success', '回复成功')
    const resp = await getDiscussionAnswers(questionId, { page: 1, size: 50 })
    discussionAnswers.value[questionId] = resp.data?.data?.data || []
  } catch (e) {
    notify('error', e.response?.data?.message || '回复失败')
  } finally {
    postingAnswerId.value = null
  }
}

onMounted(async () => {
  await fetchDetail()
  await loadDiscussions()
})
</script>

<style lang="scss" scoped>
@import '../styles/variables.scss';

.share-detail-page {
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
}

.share-detail-page .page-header {
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f7ff 50%, #faf6ff 100%);
  color: #2d3748;
  padding: 50px 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  min-height: 200px;
  border-radius: 0 0 30px 30px;
  overflow: hidden;
  z-index: 10;
  margin-bottom: 0;

  // 添加背景装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 80%, rgba(79, 172, 254, 0.08) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(139, 69, 247, 0.06) 0%, transparent 50%);
    pointer-events: none;
  }

  // 添加几何装饰元素
  &::after {
    content: '';
    position: absolute;
    top: -30px;
    right: -30px;
    width: 120px;
    height: 120px;
    background: rgba(79, 172, 254, 0.08);
    border-radius: 50%;
    animation: float 6s ease-in-out infinite;
  }

  .header-content {
    text-align: center;
    z-index: 2;
    display: flex;
    flex-direction: column;
    gap: 20px;
    max-width: 700px;
    padding: 0 20px;
    position: relative;
  }

  .page-title {
    font-size: clamp(1.8rem, 4vw, 2.8rem);
    font-weight: 700;
    color: #2d3748;
    margin: 0;
    font-family: $font-family-heading; // 使用字体族变量替代硬编码值
    letter-spacing: -0.01em;
    line-height: $line-height-tight; // 使用行高变量替代硬编码值 1.3
    text-shadow: 0 2px 8px rgba(79, 172, 254, 0.1);
    animation: titleFadeIn 1s ease-out;
    position: relative;

    // 添加装饰性下划线
    &::after {
      content: '';
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%);
      width: 60px;
      height: 3px;
      background: linear-gradient(90deg, transparent, rgba(79, 172, 254, 0.4), transparent);
      border-radius: 2px;
      animation: underlineGrow 1s ease-out 0.5s both;
    }
  }

  .page-meta {
    display: flex;
    justify-content: center;
    gap: 24px;
    font-size: 14px;
    color: #64748b;
    animation: metaFadeIn 1s ease-out 0.3s both;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 20px;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(79, 172, 254, 0.1);
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.8);
        transform: translateY(-1px);
        box-shadow: 0 4px 15px rgba(79, 172, 254, 0.15);
      }

      i {
        color: #4facfe;
        font-size: 12px;
      }
    }
  }

  .header-cartoon {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 1;
    animation: float 3s ease-in-out infinite;

    img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 50%;
      box-shadow: 0 6px 20px rgba(79, 172, 254, 0.2);
      border: 2px solid rgba(255, 255, 255, 0.8);
      transition: all 0.3s ease;
      filter: brightness(1.05);

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 8px 25px rgba(79, 172, 254, 0.3);
      }
    }

    &--left {
      left: 8%;
      animation-delay: -1s;
    }

    &--right {
      right: 8%;
      animation-delay: -2s;
    }
  }

  @media (max-width: 768px) {
    padding: 40px 0 50px;
    border-radius: 0 0 20px 20px;

    .header-cartoon {
      display: none;
    }

    .page-meta {
      flex-direction: column;
      gap: 12px;
      align-items: center;

      .meta-item {
        padding: 6px 12px;
        font-size: 12px;
      }
    }
  }

  @media (max-width: 480px) {
    .page-meta {
      .meta-item {
        width: auto;
        max-width: 200px;
        justify-content: center;
      }
    }
  }
}

.content-container {
  max-width: 900px;
  margin: 32px auto;
  padding: 0 16px;

  .document-content {
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .empty {
      text-align: center;
      color: #999;
      font-size: 16px;
      padding: 40px 20px;
    }
  }
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #666;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #f3f3f3;
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

// 动画定义
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: translateY(-50%) scale(0.3);
  }
  50% {
    opacity: 1;
    transform: translateY(-50%) scale(1.05);
  }
  70% {
    transform: translateY(-50%) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
}

// 添加AnnouncementDetail页面的动画
@keyframes float {
  0%, 100% {
    transform: translateY(-50%) translateX(0);
  }
  50% {
    transform: translateY(-50%) translateX(10px);
  }
}

@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes metaFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes underlineGrow {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 60px;
    opacity: 1;
  }
}

// Markdown 内容样式
:deep(.markdown-content) {
  line-height: $line-height-relaxed; // 使用行高变量替代硬编码值 1.7
  color: #333;
  font-size: 16px;
  font-family: $font-family-base; // 使用字体族变量替代硬编码值

  h1, h2, h3, h4, h5, h6 {
    color: #2c3e50;
    margin-top: 2em;
    margin-bottom: 1em;
    font-weight: 600;
    font-family: $font-family-heading; // 使用字体族变量替代硬编码值
  }

  h1 {
    font-size: 2em;
    border-bottom: 2px solid #eee;
    padding-bottom: 0.5em;
    margin-top: 0;
  }

  h2 {
    font-size: 1.6em;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.3em;
  }

  h3 {
    font-size: 1.4em;
  }

  h4 {
    font-size: 1.2em;
  }

  h5, h6 {
    font-size: 1em;
  }

  p {
    margin-bottom: 1.2em;
    text-align: justify;
    word-break: break-word;
  }

  blockquote {
    border-left: 4px solid #ddd;
    background: #f8f6fc;
    padding: 1%;
    margin: 1% 0;
    border-radius: 13px;
    font-style: italic;
    color: #666;
  }

  code {
    background: #f1f3f4;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-family: $font-family-mono; // 使用字体族变量替代硬编码值
    font-size: 0.9em;
    color: #d73a49;
  }

  pre {
    background: #fffaf4;
    border: 1px solid #ccccd2;
    border-radius: 13px;
    padding: 1%;
    overflow-x: auto;
    margin: 1% 0;

    code {
      background: none;
      padding: 0;
      color: #24292e;
      font-size: 0.85em;
    }
  }

  ul, ol {
    margin: 1.2em 0;
    padding-left: 2em;

    li {
      margin-bottom: 0.5em;
      line-height: $line-height-normal; // 使用行高变量替代硬编码值 1.6
    }
  }

  table {
    width: 100%;
    margin: 1.5em 0;
    border: 2px solid #e1e4e8;
    border-radius: 13px;
    overflow: hidden;
    border-collapse: separate;
    border-spacing: 0;

    th, td {
      padding: 0.75em 1em;
      text-align: left;
      border-bottom: 1px solid #e1e4e8;
      border-right: 1px solid #e1e4e8;

      &:last-child {
        border-right: none;
      }
    }

    th {
      background: #f6f8fa;
      font-weight: 600;
      color: #24292e;
    }

    tr:last-child td {
      border-bottom: none;
    }
  }

  img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin: 1em 0;
  }

  a {
    color: #0366d6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  hr {
    border: none;
    border-top: 1px solid #e1e4e8;
    margin: 2em 0;
  }
}

.discussion-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.discussion-tip {
  color: #666;
  margin-bottom: 12px;
}

.discussion-input, .answer-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.discussion-item {
  border-top: 1px solid #eee;
  padding: 12px 0;
}

.discussion-author, .answer-author {
  font-weight: 600;
}

.discussion-meta {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.answer-list {
  margin: 8px 0;
  padding: 8px;
  background: #fafafa;
  border-radius: 8px;
}

.answer-item + .answer-item {
  margin-top: 6px;
}

/* --- 讨论区头像与排版样式 --- */

.discussion-item {
  border-top: 1px solid #eee;
  padding: 16px 0;
}

/* 主评论的 Flex 布局 */
.discussion-main {
  display: flex;
  gap: 12px; /* 头像和文字的间距 */
  align-items: flex-start; /* 顶部对齐 */
}

.avatar-wrapper {
  flex-shrink: 0; /* 防止头像被挤压变形 */
}

.content-wrapper {
  flex: 1; /* 让文字部分撑满剩余空间 */
}

.discussion-author {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  margin-bottom: 4px;
}

.discussion-content {
  color: #333;
  line-height: 1.5;
  word-break: break-word; /* 防止长串英文或链接撑爆容器 */
}

.discussion-meta {
  color: #999;
  font-size: 12px;
  margin-top: 6px;
}

/* 楼中楼回复的样式 */
.answer-list {
  margin: 12px 0 12px 52px; /* 左侧留出主评论头像的宽度 (40px头像 + 12px间距) */
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.answer-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.answer-item + .answer-item {
  margin-top: 10px;
}

.answer-avatar {
  flex-shrink: 0;
  margin-top: 2px; /* 微调小头像的垂直对齐 */
}

.answer-content {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
}

.answer-author {
  font-weight: 600;
  color: #4facfe; /* 给作者名字加一点主题色区分 */
}

/* 回复框向右缩进对齐 */
.answer-input {
  margin-left: 52px;
}

/* 子回复的头部布局（名字和时间水平排列） */
.answer-header {
  display: flex;
  justify-content: space-between; /* 名字在左，时间在右 */
  align-items: center;
  margin-bottom: 2px;
}

.answer-author {
  font-weight: 600;
  color: #4facfe;
  font-size: 13px; /* 子回复名字稍微小一点 */
}

/* 子回复的时间样式 */
.answer-time {
  color: #bbb; /* 颜色更淡一些 */
  font-size: 11px; /* 字体更小一些 */
}

.answer-text {
  color: #444;
  line-height: 1.4;
  font-size: 13px;
}

/* 调整子回复列表的内边距 */
.answer-list {
  margin: 12px 0 12px 52px;
  padding: 10px 14px; /* 稍微加宽一点内边距 */
  background: #f9f9f9;
  border-radius: 8px;
}

/* 楼中楼整体容器 */
.answer-list {
  margin: 12px 0 12px 52px;
  padding: 8px 16px;
  background-color: #fcfcfc; /* 极浅的灰色背景 */
  border-radius: 8px;
  border: 1px solid $gray-color; /* 给整个回复块一个淡淡的边框 */
}

/* 每一条具体的回复 */
.answer-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 0; /* 增加上下内边距 */
  position: relative;

  /* 除了最后一条，其余每一条下面都加一条很淡的分割线 */
  &:not(:last-child) {
    border-bottom: 1px dashed $gray-dark-color; /* 使用虚线 */
  }
}

.answer-avatar {
  flex-shrink: 0;
  border: 1px solid #fff; /* 给小头像加白边，在浅灰背景下更突出 */
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.answer-content {
  flex: 1;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.answer-author {
  font-weight: 600;
  color: #555; /* 名字颜色稍微深一点 */
  font-size: 13px;
}

.answer-time {
  color: #999;
  font-size: 11px;
}

.answer-text {
  color: #444;
  line-height: 1.6; /* 增加行高，提升阅读体验 */
  font-size: 13px;
  white-space: pre-wrap; /* 保持换行格式 */
}
</style>





