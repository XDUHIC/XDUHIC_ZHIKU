<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<script setup>
import {RouterView} from 'vue-router'
import {onMounted, onUnmounted} from 'vue'
import {useAuthStore} from './stores'
import {useRouter} from 'vue-router'

const auth = useAuthStore()
const router = useRouter()



// 定期检查token有效性
let tokenCheckInterval = null

let frameId = null
let lastPointerEvent = null

function resetHeaderGlowPosition() {
  document.querySelectorAll('.page-header').forEach((header) => {
    header.style.setProperty('--header-glow-x', '50%')
    header.style.setProperty('--header-glow-y', '36%')
  })
}

function applyHeaderGlowPosition(event) {
  const target = event.target instanceof Element ? event.target : null
  if (!target) return false

  const header = target.closest('.page-header')
  if (!header) return false

  const rect = header.getBoundingClientRect()
  if (rect.width <= 0 || rect.height <= 0) return false

  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100

  header.style.setProperty('--header-glow-x', `${Math.max(0, Math.min(100, x))}%`)
  header.style.setProperty('--header-glow-y', `${Math.max(0, Math.min(100, y))}%`)
  return true
}

function onPointerMove(event) {
  lastPointerEvent = event
  if (frameId) return

  frameId = requestAnimationFrame(() => {
    frameId = null
    if (lastPointerEvent) {
      const matched = applyHeaderGlowPosition(lastPointerEvent)
      if (!matched) {
        resetHeaderGlowPosition()
      }
    }
  })
}

onMounted(() => {
  // 初始化时加载认证状态
  auth.loadFromStorage()

  // 如果已登录，立即检查token有效性
  if (auth.isLoggedIn) {
    checkTokenValidity()
  }

  // 设置定期检查token有效性（每5分钟检查一次）
  tokenCheckInterval = setInterval(() => {
    if (auth.isLoggedIn) {
      checkTokenValidity()
    }
  }, 5 * 60 * 1000)

  window.addEventListener('pointermove', onPointerMove, {passive: true})
})

onUnmounted(() => {
  if (tokenCheckInterval) {
    clearInterval(tokenCheckInterval)
  }

  window.removeEventListener('pointermove', onPointerMove)
  if (frameId) {
    cancelAnimationFrame(frameId)
    frameId = null
  }
})

async function checkTokenValidity() {
  try {
    // 首先检查本地token是否已过期
    if (auth.isTokenExpired()) {
      console.log('Token已过期，静默清除登录状态')
      auth.logout()
      // 不跳转页面，让用户继续浏览
      return
    }

    // 定期验证token有效性（减少频率，避免过多请求）
    const now = Date.now()
    const lastCheck = auth.lastTokenCheck || 0
    const checkInterval = 10 * 60 * 1000 // 10分钟检查一次

    if (now - lastCheck > checkInterval) {
      const isValid = await auth.checkTokenValidity()
      if (!isValid) {
        console.log('Token验证失败，静默清除登录状态')
        auth.logout()
        // 不跳转页面，让用户继续浏览
      }
      auth.lastTokenCheck = now
    }
  } catch (error) {
    console.error('Token有效性检查失败:', error)
  }
}
</script>

<style lang="scss">
@import "./styles/variables.scss";
/*
 * 移除外部 @import 引入，改用 main.scss 全局注入
 * 彻底解决 Windows 环境下的路径找不到报错问题
 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>