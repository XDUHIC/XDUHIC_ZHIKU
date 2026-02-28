// Polyfill for older browsers 
import 'core-js/stable'
import 'regenerator-runtime/runtime'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
// 显式导入黑暗模式所需图标
import { faSun, faMoon } from '@fortawesome/free-solid-svg-icons'

import './styles/main.scss'
import './styles/dark-mode.scss'

library.add(fas, fab, far, faSun, faMoon)

/**
 * 初始化暗色模式
 */
function initDarkMode() {
    try {
        const savedTheme = localStorage.getItem('theme')
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches

        let isDark = savedTheme ? (savedTheme === 'dark') : prefersDark

        if (isDark) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    } catch (e) {
        console.error('Failed to init dark mode:', e)
    }
}

// 立即执行初始化
initDarkMode()

// 监听系统主题变化
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    if (!localStorage.getItem('theme')) {
        if (e.matches) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }
})

/**
 * 全局暗色模式切换函数
 */
window.toggleDarkMode = function() {
    const isDark = document.documentElement.classList.contains('dark')
    if (isDark) {
        document.documentElement.classList.remove('dark')
        localStorage.setItem('theme', 'light')
    } else {
        document.documentElement.classList.add('dark')
        localStorage.setItem('theme', 'dark')
    }
    // 触发一个自定义事件，通知组件主题已更改
    window.dispatchEvent(new CustomEvent('theme-changed', { detail: { theme: isDark ? 'light' : 'dark' } }))
}

window.getCurrentTheme = function() {
    return document.documentElement.classList.contains('dark') ? 'dark' : 'light'
}

const app = createApp(App)

app.component('font-awesome-icon', FontAwesomeIcon)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
