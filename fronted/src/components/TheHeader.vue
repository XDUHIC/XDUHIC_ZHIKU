<template>
  <header class="header">
    <div class="header-container container">
      <!-- Logo部分 -->
      <div class="logo-container">
        <router-link to="/portal" class="logo-link">
          <img src="../assets/about-image.jpg" alt="华创智库" class="logo-img" />
          <span class="logo-text">华创智库</span>
        </router-link>
      </div>

      <!-- 导航链接 - 桌面端 -->
      <nav class="nav-desktop hidden-xs-down">
        <ul class="nav-list">
          <li v-for="item in navItems" :key="item.id" class="nav-item">
            <router-link :to="item.path" class="nav-link" active-class="active">
              {{ item.title }}
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- 右侧操作区 -->
      <div class="header-right-actions hidden-xs-down">
        <!-- 黑暗模式切换按钮 - 纯 HTML 结构确保兼容性 -->
        <div class="theme-toggle">
          <button type="button" class="theme-btn" @click="toggleDarkMode" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
            <font-awesome-icon :icon="isDark ? 'sun' : 'moon'" />
          </button>
        </div>

        <!-- 认证入口 -->
        <div class="auth-actions">
          <template v-if="!authStore.isLoggedIn">
            <button class="login-btn" @click="router.push('/auth/login')">登录 / 注册</button>
          </template>
          <template v-else>
            <el-dropdown>
              <span class="el-dropdown-link user-entry">
                <img :src="avatarUrl" alt="avatar" class="avatar" />
                <span class="username">{{ authStore.user.nickname || authStore.user.username }}</span>
                <font-awesome-icon icon="caret-down" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/auth/profile')">
                    <font-awesome-icon icon="user" class="dropdown-icon" />
                    个人主页
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/repository')">
                    <font-awesome-icon icon="code-branch" class="dropdown-icon" />
                    项目仓库
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <font-awesome-icon icon="sign-out-alt" class="dropdown-icon" />
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>

      <!-- 移动端菜单按钮 -->
      <button type="button" aria-label="切换移动端菜单" class="menu-toggle hidden-sm-up" @click="toggleMobileMenu">
        <font-awesome-icon :icon="isMobileMenuOpen ? 'xmark' : 'bars'" />
      </button>
    </div>

    <!-- 移动端导航菜单 -->
    <div class="mobile-menu" :class="{ 'is-open': isMobileMenuOpen }">
      <ul class="mobile-nav-list">
        <li v-for="item in navItems" :key="item.id" class="mobile-nav-item">
          <router-link
              :to="item.path"
              class="mobile-nav-link"
              active-class="active"
              @click="closeMobileMenu">
            <font-awesome-icon :icon="item.icon" />
            {{ item.title }}
          </router-link>
        </li>

        <!-- 移动端主题切换 -->
        <li class="mobile-nav-item">
          <button type="button" class="mobile-nav-link w-full border-none bg-transparent text-left" @click="toggleDarkMode">
            <font-awesome-icon :icon="isDark ? 'sun' : 'moon'" />
            {{ isDark ? '切换到亮色模式' : '切换到暗色模式' }}
          </button>
        </li>

        <!-- 移动端认证选项 -->
        <li class="mobile-nav-item mobile-auth-section">
          <template v-if="!authStore.isLoggedIn">
            <router-link
                to="/auth/login"
                class="mobile-nav-link mobile-auth-link"
                @click="closeMobileMenu">
              <font-awesome-icon icon="sign-in-alt" />
              登录 / 注册
            </router-link>
          </template>
          <template v-else>
            <div class="mobile-user-info">
              <div class="mobile-user-profile">
                <img :src="avatarUrl" alt="avatar" class="mobile-avatar" />
                <span class="mobile-username">{{ authStore.user.nickname || authStore.user.username }}</span>
              </div>
              <div class="mobile-user-actions">
                <router-link
                    to="/auth/profile"
                    class="mobile-nav-link"
                    @click="closeMobileMenu">
                  <font-awesome-icon icon="user" />
                  个人主页
                </router-link>
                <router-link
                    to="/repository"
                    class="mobile-nav-link"
                    @click="closeMobileMenu">
                  <font-awesome-icon icon="code-branch" />
                  项目仓库
                </router-link>
                <button
                    type="button"
                    class="mobile-nav-link mobile-logout-btn"
                    @click="handleLogout">
                  <font-awesome-icon icon="sign-out-alt" />
                  退出登录
                </button>
              </div>
            </div>
          </template>
        </li>
      </ul>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore, useAuthStore } from '../stores'
import { config } from '../utils/config.js'

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const isDark = ref(false)

const updateDarkState = () => {
  isDark.value = document.documentElement.classList.contains('dark')
}

onMounted(() => {
  authStore.loadFromStorage()
  updateDarkState()
  window.addEventListener('theme-changed', updateDarkState)
})

onUnmounted(() => {
  window.removeEventListener('theme-changed', updateDarkState)
})

const toggleDarkMode = () => {
  if (window.toggleDarkMode) {
    window.toggleDarkMode()
  }
}

const isMobileMenuOpen = computed(() => appStore.isMobileMenuOpen)
const toggleMobileMenu = () => appStore.toggleMobileMenu()
const closeMobileMenu = () => {
  if (appStore.isMobileMenuOpen) appStore.toggleMobileMenu()
}

const avatarUrl = computed(() => {
  const url = authStore.user.avatarUrl
  const fallback = new URL('../assets/about-image.jpg', import.meta.url).href
  if (!url) return fallback
  if (/^https?:\/\//i.test(url)) return url
  return config.getUploadUrl(url)
})

const handleLogout = () => {
  authStore.logout()
  closeMobileMenu()
  router.push('/portal')
}

const navItems = [
  { id: 1, title: '知识库', path: '/knowledge', icon: 'book' },
  { id: 2, title: '项目分享', path: '/projects', icon: 'code'},
  {id: 3, title: '华为竞赛', path: '/competitions', icon: 'trophy'},
  {id: 4, title: '实用工具', path: '/tools', icon: 'toolbox'},
  {id: 5, title: '华俱推文', path: '/articles', icon: 'newspaper'},
  {id: 6, title: '师兄师姐说', path: '/share', icon: 'comment'}
]
</script>

<style lang="scss" scoped>
/*
 * 变量现在通过 vite.config.js 全局自动注入
 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: $z-fixed;
}

.header-container {
  height: $layout-header-height;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-container {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: $primary-color;
}

.logo-img {
  height: 36px;
  margin-right: 10px;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  white-space: nowrap;
  @media (max-width: 768px) {
    display: none;
  }
}

.nav-desktop {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-list {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: block;
  padding: 10px 16px;
  color: $text-color;
  font-weight: 500;
  transition: color 0.3s;
  font-size: 16px;
  text-decoration: none;

  &:hover, &.active {
    color: $primary-color;
  }
}

.header-right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-toggle {
  display: flex;
  align-items: center;
}

.theme-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 20px;
  color: $text-color;
  padding: 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.auth-actions {
  display: flex;
  align-items: center;
}

.login-btn {
  background-color: $primary-color;
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;

  &:hover {
    opacity: 0.9;
  }
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.menu-toggle {
  font-size: 24px;
  color: $text-color;
  background: none;
  border: none;
  cursor: pointer;
}

.mobile-menu {
  display: none;
  position: fixed;
  top: $layout-header-height;
  left: 0;
  width: 100%;
  height: calc(100vh - $layout-header-height);
  background-color: white;
  z-index: $z-dropdown;
  transform: translateX(100%);
  transition: transform 0.3s ease;

  &.is-open {
    display: block;
    transform: translateX(0);
  }
}

.mobile-nav-list {
  padding: 16px;
}

.mobile-nav-item {
  margin-bottom: 8px;
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: $text-color;
  font-weight: 500;
  border-radius: 4px;
  text-decoration: none;

  .svg-inline--fa {
    margin-right: 12px;
    width: 18px;
  }

  &:hover, &.active {
    background-color: #f5f5f5;
    color: $primary-color;
  }
}

.mobile-auth-section {
  border-top: 1px solid #e2e8f0;
  margin-top: 16px;
  padding-top: 16px;
}

.mobile-auth-link {
  background-color: $primary-color;
  color: white !important;

  &:hover {
    background-color: darken($primary-color, 10%);
    color: white !important;
  }
}

.mobile-user-info {
  padding: 0 16px;
}

.mobile-user-profile {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 8px;
}

.mobile-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 12px;
}

.mobile-username {
  font-weight: 600;
  color: $text-color;
}

.mobile-user-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-logout-btn {
  background: none;
  border: none;
  text-align: left;
  width: 100%;
  cursor: pointer;

  &:hover {
    background-color: #fee2e2;
    color: #dc2626 !important;
  }
}
</style>