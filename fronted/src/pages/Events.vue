<template>
  <MainLayout>
    <div class="events-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-cartoon header-cartoon--left">
          <img src="/src/assets/cartoon1.jpg" alt="cartoon decoration" />
        </div>
        <div class="header-content">
          <h1 class="page-title">活动中心</h1>
          <h2 class="page-subtitle">华创活动...还挺多？</h2>
        </div>
        <div class="header-cartoon header-cartoon--right">
          <img src="/src/assets/cartoon2.png" alt="cartoon decoration" />
        </div>
      </div>

      <!-- 内容容器 -->
      <div class="content-container">

        <!-- 筛选和搜索 -->
        <div class="filters-section">
          <div class="filter-group">
            <label class="filter-label">活动状态：</label>
            <el-radio-group v-model="selectedType" @change="handleSearch">
              <el-radio-button label="upcoming">即将开始</el-radio-button>
              <el-radio-button label="ongoing">进行中</el-radio-button>
              <el-radio-button label="completed">已结束</el-radio-button>
            </el-radio-group>
          </div>

          <div class="search-group">
            <el-input
                v-model="searchQuery"
                placeholder="搜索活动标题或内容..."
                class="search-input-custom"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <i class="fas fa-search"></i>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 加载态 + 活动列表 -->
        <div
            class="events-list-wrapper"
            v-loading="loading"
            element-loading-text="精彩活动加载中..."
        >
          <!-- 活动列表 -->
          <div v-if="events.length > 0" class="events-list">
            <div
                v-for="event in events"
                :key="event.id"
                class="event-card"
                @click="router.push(`/events/${event.id}`)"
            >
              <div class="event-header">
                <el-tag :type="getEventStatus(event).type" effect="light">
                  {{ getEventStatus(event).text }}
                </el-tag>
              </div>

              <h3 class="event-title">{{ event.title }}</h3>

              <p class="event-description">{{ event.description || event.content }}</p>

              <div class="event-details">
                <div v-if="event.startTime || event.start_time" class="event-time">
                  <i class="fas fa-clock"></i>
                  <span>开始时间：{{ formatDateTime(event.startTime || event.start_time) }}</span>
                </div>
                <div v-if="event.endTime || event.end_time" class="event-time">
                  <i class="fas fa-clock"></i>
                  <span>结束时间：{{ formatDateTime(event.endTime || event.end_time) }}</span>
                </div>
                <div v-if="event.location" class="event-location">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ event.location }}</span>
                </div>
              </div>

              <div class="event-footer">
                <div class="event-meta">
                  <span class="views">
                    <i class="fas fa-eye"></i>
                    {{ event.viewCount || event.view_count || event.views || 0 }}
                  </span>
                  <span v-if="event.maxParticipants || event.max_participants" class="participants">
                    <i class="fas fa-users"></i>
                    {{ event.participantCount || event.participant_count || event.participants || 0 }}/{{ event.maxParticipants || event.max_participants }}
                  </span>
                </div>

                <el-button type="primary" size="small" round @click.stop="router.push(`/events/${event.id}`)">
                  查看详情 <i class="fas fa-arrow-right" style="margin-left: 5px;"></i>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty
              v-else-if="!loading"
              description="暂无符合条件的活动"
          />
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="totalEvents > 0">
          <el-pagination
              background
              layout="total, prev, pager, next, jumper"
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="totalEvents"
              @current-change="handlePageChange"
          />
        </div>

      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import axios from 'axios'

const router = useRouter()

// 状态变量（分页/筛选/加载）
const loading = ref(true)
const events = ref([])
const totalEvents = ref(0) // Element Plus 分页器只需要总条数
const currentPage = ref(1)
const pageSize = ref(10)
const selectedType = ref('upcoming')
const searchQuery = ref('')

// 获取活动数据
// 说明：兼容不同后端字段与响应结构，避免接口调整导致前端空白
const fetchEvents = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      type: selectedType.value
    }

    if (searchQuery.value) {
      params.search = searchQuery.value
    }

    const response = await axios.get('/api/portal/events', {params})
    const data = response.data

    // 处理不同响应格式（兼容性代码）
    let list = []
    let total = 0

    if (data?.data && Array.isArray(data.data)) {
      // 结构 A: { code, data: [...], total / totalCount }
      list = data.data
      total = data.total || data.totalCount || 0
    } else if (data?.data?.data && Array.isArray(data.data.data)) {
      // 结构 B: { code, data: { data: [...], total / totalCount } }
      list = data.data.data
      total = data.data.total || data.data.totalCount || data.total || data.totalCount || 0
    } else if (data?.events && Array.isArray(data.events)) {
      // 结构 C: { events: [...], total / totalCount }
      list = data.events
      total = data.total || data.totalCount || list.length
    } else if (Array.isArray(data)) {
      // 结构 D: 直接数组 [...]
      list = data
      total = list.length
    }

    events.value = list
    totalEvents.value = total

  } catch (error) {
    console.error('获取活动数据失败:', error)
    events.value = []
    totalEvents.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索/筛选触发入口
// selectedType 切换、搜索提交时都走这个方法，统一重置到第一页
const handleSearch = () => {
  currentPage.value = 1
  fetchEvents()
}

// 分页切换：分页器已同步 currentPage，只负责重新拉取数据
const handlePageChange = () => {
  fetchEvents()
  window.scrollTo({top: 0, behavior: 'smooth'})
}

onMounted(() => {
  fetchEvents()
})

// 辅助函数：活动状态映射（配合 el-tag 展示颜色与文案）
const getEventStatus = (event) => {
  const now = new Date()
  const startDate = new Date(event.startTime || event.start_time)
  const endDate = new Date(event.endTime || event.end_time)

  if (now < startDate) {
    return {text: '即将开始', type: 'primary'} // 蓝色
  } else if (now >= startDate && now <= endDate) {
    return {text: '进行中', type: 'success'}   // 绿色
  } else {
    return {text: '已结束', type: 'info'}      // 灰色
  }
}

// 辅助函数：日期时间格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style lang="scss" scoped>
@import '../styles/variables.scss';

.events-page {
  min-height: 100vh;
  background-color: $background-color;
}

/* 头部样式保持不变，因为是纯视觉展示 */
.page-header {
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
    background: radial-gradient(circle at 20% 80%, rgba(79, 172, 254, 0.08) 0%, transparent 50%),
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

  .page-subtitle {
    font-size: 1.1rem;
    font-weight: 400;
    color: #64748b;
    font-family: $font-family-heading; // 使用字体族变量替代硬编码值
    margin: 0;
    letter-spacing: -0.01em;
    opacity: 0.9;
    animation: metaFadeIn 1s ease-out 0.3s both;
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
  }
}


.content-container {
  padding: $spacing-lg;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 60vh;
}

.filters-section {
  background: white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-sm;

  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-md;

  .filter-group {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .filter-label {
      font-weight: 500;
      color: $text-color;
    }
  }

  .search-group {
    .search-input-custom {
      width: 300px; /* 搜索框宽度 */
    }
  }
}

/* 列表容器
  min-height 保证 loading 和 empty 状态时不会塌缩得太难看
*/
.events-list-wrapper {
  min-height: 300px;
}

.events-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;

  .event-card {
    background: white;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-sm;
    transition: all 0.3s ease;
    cursor: pointer;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    &:hover {
      box-shadow: $shadow-md;
      transform: translateY(-2px);
    }

    .event-header {
      padding: $spacing-md;
      border-bottom: 1px solid $border-color;
      display: flex;
      justify-content: flex-end; /* 标签靠右 */
    }

    .event-title {
      padding: 0 $spacing-md;
      margin: $spacing-md 0;
      font-size: $font-size-lg;
      font-weight: 600;
    }

    .event-description {
      padding: 0 $spacing-md;
      margin-bottom: $spacing-md;
      color: $gray-color;
      font-size: $font-size-sm;
      height: 4.5em; /* 限制高度 */
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }

    .event-details {
      padding: 0 $spacing-md;
      margin-bottom: $spacing-md;

      .event-time, .event-location {
        display: flex;
        align-items: center;
        gap: 8px;
        color: $gray-dark-color;
        font-size: $font-size-sm;
        margin-bottom: 4px;

        i {
          color: $primary-color;
          width: 16px;
          text-align: center;
        }
      }
    }

    .event-footer {
      padding: $spacing-md;
      border-top: 1px solid $border-color;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;

      .event-meta {
        display: flex;
        gap: 15px;
        font-size: $font-size-xs;
        color: $gray-color;

        span {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }
  }
}

.pagination-container {
  margin-top: $spacing-2xl;
  display: flex;
  justify-content: center; /* 居中分页器 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filters-section {
    flex-direction: column;
    align-items: stretch;

    .search-group .search-input-custom {
      width: 100%;
    }
  }
}
</style>