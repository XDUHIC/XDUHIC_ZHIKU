<template>
  <div class="node-detail">
    <!-- 节点类型标签 -->
    <div class="node-type-badge" :class="node.type">
      <span v-if="node.type === 'company'">🏢 俱乐部</span>
      <span v-else-if="node.type === 'department'">🏛️ 部门</span>
      <span v-else-if="node.type === 'team'">👥 小组</span>
    </div>

    <!-- 基本信息 -->
    <div class="detail-section">
      <h3>{{ node.name }}</h3>
      <div v-if="node.email" class="detail-item">
        <span class="label">部门邮箱:</span>
        <a :href="'mailto:' + node.email" class="value link">{{ node.email }}</a>
      </div>
    </div>

    <!-- 负责人信息 -->
    <!-- 负责人信息 -->
    <div v-if="node.leader && node.leader.length" class="detail-section leader-info">
      <h4>负责人（{{ node.leader.length }}位）</h4>
      <div v-for="leaderItem in node.leader" :key="leaderItem.id" class="leader-card">
        <div class="leader-avatar">{{ getInitials(leaderItem.name) }}</div>
        <div class="leader-details">
          <div class="leader-name">{{ leaderItem.name }}</div>
          <div class="leader-position">{{ leaderItem.position }}</div>
          <div v-if="leaderItem.email" class="leader-contact">
            <a :href="'mailto:' + leaderItem.email">📧 {{ leaderItem.email }}</a>
          </div>
          <div v-if="leaderItem.phone" class="leader-contact">
            📞 {{ leaderItem.phone }}
          </div>
        </div>
      </div>
    </div>

    <!-- 成员列表 -->
    <div v-if="node.members && node.members.length > 0" class="detail-section">
      <h4>团队成员 ({{ node.members.length }}人)</h4>
      <div class="members-list">
        <div v-for="member in node.members" :key="member.id" class="member-item">
          <div class="member-avatar">{{ getInitials(member.name) }}</div>
          <div class="member-info">
            <div class="member-name">{{ member.name }}</div>
            <div class="member-position">{{ member.position }}</div>
            <div v-if="member.email" class="member-email">
              <a :href="'mailto:' + member.email">{{ member.email }}</a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <!-- 统计信息：仅限部门和团队层级，且有自定义人数时才显示 -->
    <div v-if="(node.type === 'department' || node.type === 'team') && node.totalMembers !== undefined" class="detail-section stats">
      <h4>统计信息</h4>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ node.totalMembers }}</div>
          <div class="stat-label">总人数</div>
        </div>
        <!-- 原有的“下属团队数”和“组织层级”统计可以根据需要保留或删除 -->
        <div v-if="node.children" class="stat-item">
          <div class="stat-value">{{ node.children.length }}</div>
          <div class="stat-label">下属{{ node.type === 'department' ? '团队' : '小组' }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ getDepth() }}</div>
          <div class="stat-label">组织层级</div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'NodeDetail',
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  methods: {
    // 获取姓名的首字母
    getInitials(name) {
      if (!name) return '?'
      return name.split(' ')
          .map(word => word[0])
          .join('')
          .toUpperCase()
          .slice(0, 2)
    },

    // 计算总人数（负责人 + 成员）
    getTotalMembers() {
      let total = this.node.members ? this.node.members.length : 0
      if (this.node.leader) total += 1
      return total
    },

    // 计算节点深度（递归计算子节点）
    getDepth() {
      const calculateDepth = (node) => {
        if (!node.children || node.children.length === 0) {
          return 1
        }

        let maxChildDepth = 0
        for (const child of node.children) {
          const childDepth = calculateDepth(child)
          if (childDepth > maxChildDepth) {
            maxChildDepth = childDepth
          }
        }

        return 1 + maxChildDepth
      }

      return calculateDepth(this.node)
    }
  }
}
</script>

<style scoped>
:root {
  --primary-color: #E41E2B;
  --primary-hover: #b81621; /* 近似 darken */
  --text-dark: #181818;
  --text-light: #666666;
  --text-lighter: #CCCCCC;
  --bg-light: #F5F5F5;
  --bg-page: #f8f8f8;
  --border-color: #E5E5E5;
  --border-light: #E5E7EB;
  --box-shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.1);
  --box-shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --border-radius-md: 8px;
  --border-radius-sm: 4px;
  --border-radius-full: 9999px;
  --font-family: "HarmonyOS Sans SC", Arial, sans-serif;
}
/* NodeDetail.vue 原有样式修改 */
.node-detail {
  color: var(--text-dark);
}

.node-type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--border-radius-full);
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 16px;
  background: var(--bg-light);
  color: var(--text-light);
}

/* 部门类型 badge 可保留颜色，但不需要强绑定，可以去掉特殊颜色或保留 */
.node-type-badge.company {
  background: #e6f7ff;
  color: #1890ff;
}
.node-type-badge.department {
  background: #f6ffed;
  color: #52c41a;
}
.node-type-badge.team {
  background: #fff2e8;
  color: #fa8c16;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-light);
}
.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  color: var(--text-dark);
  font-size: 18px;
  font-weight: 600;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: var(--text-light);
  font-size: 16px;
  font-weight: 500;
}

/* 负责人卡片 */
.leader-card {
  background: white;
  border-left: 4px solid #E41E2B;  /* 华为红左边框 */
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.leader-avatar {
  background: #E41E2B;  /* 华为红背景 */
  color: white;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.leader-details {
  flex: 1;
}

.leader-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-dark);
  margin-bottom: 4px;
}

.leader-position {
  color: var(--text-light);
  font-size: 14px;
  margin-bottom: 4px;
}

.leader-contact a {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 13px;
}
.leader-contact a:hover {
  text-decoration: underline;
}

/* 成员列表 */
.members-list {
  max-height: 250px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--border-radius-md);
  margin-bottom: 8px;
  background: white;
  border: 1px solid var(--border-light);
  transition: background 0.3s;
}

.member-item:hover {
  background: var(--bg-light);
}

.member-avatar {
  width: 36px;
  height: 36px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
}

.member-name {
  font-weight: 500;
  color: var(--text-dark);
  margin-bottom: 2px;
}

.member-position {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 2px;
}

.member-email {
  font-size: 12px;
}
.member-email a {
  color: var(--primary-color);
  text-decoration: none;
}
.member-email a:hover {
  text-decoration: underline;
}

/* 统计信息 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
}
.stat-item {
  background: #f5f5f5;  /* 浅灰背景 */
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #E41E2B;  /* 华为红数字 */
}
.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

</style>