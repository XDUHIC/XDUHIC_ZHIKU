<template>
  <div class="tree-node">
    <!-- 当前节点 -->
    <div
        class="node-content"
        :class="{
        'is-company': node.type === 'company',
        'is-department': node.type === 'department',
        'is-team': node.type === 'team',
        'search-match': isSearchMatch,
        'selected': isSelected
      }"
        :style="{ 'margin-left': depth * 30 + 'px' }"
        @click="handleClick"
    >
      <!-- 展开/折叠图标 -->
      <span
          v-if="hasChildren"
          class="expand-icon"
          @click.stop="toggleExpand"
      >
        {{ isExpanded ? '▼' : '▶' }}
      </span>
      <span v-else class="expand-placeholder"></span>

      <!-- 节点图标 -->
      <span class="node-icon">
        <span v-if="node.type === 'company'">🏢</span>
        <span v-else-if="node.type === 'department'">🏛️</span>
        <span v-else-if="node.type === 'team'">👥</span>
      </span>

      <!-- 节点信息 -->
      <div class="node-info">
        <div class="node-name-row">
          <span class="node-name">{{ node.name }}</span>

          <span v-if="node.leader && node.leader.length" class="node-leader">
  <span v-for="(leaderItem, index) in node.leader" :key="leaderItem.id">
    负责人{{ node.leader.length > 1 ? index + 1 : '' }}: {{ leaderItem.name }}
    <span class="leader-position">({{ leaderItem.position }})</span>
    <!-- 如果不是最后一个，添加分隔符 -->
    <span v-if="index < node.leader.length - 1">; </span>
  </span>
</span>

        </div>

        <!-- 成员统计 -->
        <div v-if="node.members && node.members.length > 0" class="member-stats">
          <span class="member-count">
            部长 {{ node.members.length }} 人
          </span>
          <span v-if="node.children && node.children.length > 0" class="children-count">
            下设 {{ node.children.length }} 个{{ node.type === 'department' ? '团队' : '小组' }}
          </span>
        </div>

        <!-- 搜索高亮 -->
        <div v-if="isSearchMatch && $parent.searchKeyword" class="search-highlight">
          匹配搜索: "{{ $parent.searchKeyword }}"
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="node-actions" @click.stop>
        <button
            v-if="node.email"
            class="action-btn email-btn"
            @click="sendEmail(node.email)"
            title="发送邮件"
        >
          📧
        </button>

        <!-- 联系负责人按钮组 -->
        <!-- 联系负责人按钮组 -->
        <div v-if="node.leader && node.leader.length" class="leader-actions">
          <template v-for="leaderItem in node.leader" :key="'email-' + leaderItem.id">
            <button
                v-if="leaderItem.email"
                class="action-btn email-btn"
                @click="sendEmail(leaderItem.email)"
                :title="`联系${leaderItem.name} (${leaderItem.position})`"
            >
              👨‍💼
            </button>
          </template>
        </div>

      </div>
    </div>

    <!-- 子节点 (递归渲染) -->
    <div v-if="hasChildren && isExpanded" class="children">
      <tree-node
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :depth="depth + 1"
          :search-match="searchMatch"
          @node-click="$emit('node-click', $event)"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TreeNode',
  props: {
    node: {
      type: Object,
      required: true
    },
    depth: {
      type: Number,
      default: 0
    },
    searchMatch: {
      type: Set,
      default: () => new Set()
    }
  },
  data() {
    return {
      // 默认展开深度小于2的节点
      isExpanded: this.depth < 2,
      isSelected: false
    }
  },
  computed: {
    hasChildren() {
      return this.node.children && this.node.children.length > 0
    },
    isSearchMatch() {
      return this.searchMatch.has(this.node.id)
    }
  },
  watch: {
    // 搜索匹配时自动展开节点
    isSearchMatch(newVal) {
      if (newVal) {
        this.isExpanded = true
      }
    }
  },
  methods: {
    handleClick() {
      this.isSelected = !this.isSelected
      this.$emit('node-click', this.node)
    },

    toggleExpand() {
      this.isExpanded = !this.isExpanded
    },

    sendEmail(email) {
      window.location.href = `mailto:${email}`
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

/* TreeNode.vue 原有样式修改 */
.tree-node {
  margin: 8px 0; /* 增加垂直间距 */
}

.node-content {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 60px;
  box-shadow: none; /* 移除原阴影，可悬停时加 */
}

.node-content:hover {
  border-color: var(--primary-color);
  box-shadow: var(--box-shadow-sm);
  transform: translateY(-1px);
}

/* 节点类型样式 - 去掉渐变背景，统一用左边框标识 */
.node-content.is-company {
  border-left: 4px solid #E41E2B;
}
.node-content.is-department {
  border-left: 4px solid #E41E2B;
}
.node-content.is-team {
  border-left: 4px solid #E41E2B;
}
/* 原背景色类可移除或覆盖 */
.node-content.is-company,
.node-content.is-department,
.node-content.is-team {
  background: white; /* 覆盖原渐变背景 */
}

/* 节点名称 */
.node-name {
  font-weight: 500;
  color: var(--text-dark);
  font-size: 16px;
}

/* 负责人标签 */
.node-leader {
  background: var(--bg-light);
  color: var(--text-light);
  padding: 4px 10px;
  border-radius: var(--border-radius-full);
  font-size: 13px;
  margin-left: 10px;
}
.leader-position {
  color: var(--text-lighter);
  font-style: normal;
}

/* 成员统计 */
.member-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}

/* 展开图标 */
.expand-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  font-size: 12px;
  color: var(--text-light);
  cursor: pointer;
  transition: transform 0.3s;
  user-select: none;
}

.expand-icon:hover {
  color: var(--primary-color);
  transform: scale(1.2);
}

/* 子节点缩进线 */
.children {
  margin-left: 30px;
  border-left: 2px dashed var(--border-light);
  padding-left: 20px;
  margin-top: 8px;
}

/* 操作按钮 */
.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.node-content:hover .node-actions {
  opacity: 1;
}

.action-btn {
  background: var(--bg-light);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-sm);
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  color: var(--text-light);
}

.action-btn:hover {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* 搜索高亮 */
.node-content.search-match {
  background: #fff2e8; /* 主站中橙色背景，用于高亮 */
  border-color: var(--primary-color);
}

.node-content.selected {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.9; }
}

.expand-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  transition: transform 0.3s;
  user-select: none;
}

.expand-icon:hover {
  color: #4f46e5;
  transform: scale(1.2);
}

.expand-placeholder {
  width: 20px;
  margin-right: 10px;
}

.node-icon {
  font-size: 20px;
  margin-right: 12px;
  width: 30px;
  text-align: center;
}

.node-info {
  flex: 1;
}

.node-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  flex-wrap: wrap;
  gap: 10px;
}

.node-name {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.node-leader {
  font-size: 14px;
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 15px;
}

.leader-position {
  color: #475569;
  font-style: italic;
}

.member-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #94a3b8;
}

.member-count, .children-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.search-highlight {
  font-size: 11px;
  color: #d97706;
  background: #fef3c7;
  padding: 3px 8px;
  border-radius: 10px;
  margin-top: 5px;
  display: inline-block;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.node-content:hover .node-actions {
  opacity: 1;
}

.action-btn {
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #4f46e5;
  color: white;
  border-color: #4f46e5;
}

.children {
  margin-left: 30px;
  border-left: 2px dashed #e2e8f0;
  padding-left: 15px;
  margin-top: 5px;
}
</style>