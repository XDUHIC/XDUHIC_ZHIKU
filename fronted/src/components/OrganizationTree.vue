<template>
  <div class="organization-tree-container">
    <!-- 顶部标题和统计信息 -->
    <div class="tree-header">
      <h2>{{ companyName }} - 组织架构图</h2>
      <div class="stats">
        <span>最后更新: {{ lastUpdated }}</span>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-box">
      <input
          type="text"
          v-model="searchKeyword"
          placeholder="搜索部门、负责人或职位..."
          @input="handleSearch"
      />
      <span v-if="searchKeyword" class="search-results">
        找到 {{ searchResultsCount }} 个结果
      </span>
    </div>

    <!-- 树状图主体 -->
    <div class="tree-wrapper">
      <!-- 递归渲染树状结构 -->
      <tree-node
          :node="organizationData"
          :depth="0"
          :search-match="searchMatches"
          @node-click="handleNodeClick"
      />
    </div>

    <!-- 节点详情面板 -->
    <div v-if="selectedNode" class="node-detail-panel">
      <div class="panel-header">
        <h3>详情信息</h3>
        <button @click="selectedNode = null" class="close-btn">×</button>
      </div>
      <div class="panel-content">
        <node-detail :node="selectedNode" />
      </div>
    </div>
  </div>
</template>

<script>
import TreeNode from './TreeNode.vue'
import NodeDetail from './NodeDetail.vue'
import axios from 'axios'
const realToken = ''   // 粘贴你从 Postman 获取的完整 token

export default {
  name: 'OrganizationTree',
  components: {
    TreeNode,
    NodeDetail
  },
  data() {
    return {
      companyName: '',
      lastUpdated: '',
      organizationData: null,
      searchKeyword: '',
      searchMatches: new Set(), // 存储匹配节点的ID
      selectedNode: null,
      expandedState: new Map() // 存储节点的展开状态
    }
  },
  created() {
    this.fetchOrganization()
  },
  computed: {
    searchResultsCount() {
      return this.searchMatches.size
    }
  },
  methods: {

    async fetchOrganization() {
      try {
        // 确保 realToken 已定义（先打印看看）
        console.log('使用的 token:', realToken)

        const response = await axios.get('/api/organization', {
          headers: { Authorization: `Bearer ${realToken}` }
        })
        console.log('请求成功，响应数据:', response.data)

        const data = response.data
        this.companyName = data.company
        this.lastUpdated = data.lastUpdated
        this.organizationData = data.organization

      } catch (error) {
        console.error('获取组织架构数据失败:', error)
        // 可以显示错误信息给用户
      }
    },

    // 处理节点点击事件
    handleNodeClick(node) {
      this.selectedNode = node
      console.log('选中节点:', node)
    },

    // 处理搜索
    handleSearch() {
      this.searchMatches.clear()

      if (!this.searchKeyword.trim()) {
        return
      }

      const keyword = this.searchKeyword.toLowerCase()
      this.searchTree(this.organizationData, keyword)
    },

    // 递归搜索树
    searchTree(node, keyword) {
      let match = false

      // 检查节点名称
      if (node.name && node.name.toLowerCase().includes(keyword)) {
        match = true
      }

      // 检查负责人信息
      // 检查负责人信息 (新代码，处理数组)
      if (node.leader && Array.isArray(node.leader)) {
        for (const leaderItem of node.leader) {
          if (
              (leaderItem.name && leaderItem.name.toLowerCase().includes(keyword)) ||
              (leaderItem.position && leaderItem.position.toLowerCase().includes(keyword))
          ) {
            match = true
            break // 找到一个匹配即可
          }
        }
      }

      // 检查成员信息
      if (node.members && node.members.length > 0) {
        for (const member of node.members) {
          if (
              (member.name && member.name.toLowerCase().includes(keyword)) ||
              (member.position && member.position.toLowerCase().includes(keyword))
          ) {
            match = true
            break
          }
        }
      }

      if (match) {
        this.searchMatches.add(node.id)
        // 同时标记所有父节点为匹配（便于展开显示）
        //this.markAncestors(node.id)
      }

      // 递归搜索子节点
      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          this.searchTree(child, keyword)
        }
      }
    },

    // 标记祖先节点（便于搜索时展开路径）
    // markAncestors(nodeId) {
    //   // 这里简化处理，实际应用中可能需要更复杂的祖先查找逻辑
    //   // 可以通过在节点数据结构中添加parentId来实现
    // },


  }
}
</script>

<style scoped>

/* 从主站提取的设计变量 */
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

/* OrganizationTree.vue 原有样式，按以下修改 */
.organization-tree-container {
  /* 原 background: #f8fafc; 改为页面背景色，如果希望树状图本身是卡片，则背景白色并加阴影 */
  background: white; /* 改为白色卡片背景，主站卡片背景白色 */
  border-radius: var(--border-radius-md); /* 新增圆角 */
  box-shadow: var(--box-shadow-sm); /* 新增阴影，与主站卡片一致 */
  padding: 24px; /* 保持，或调整为 24px 与主站卡片内边距一致 */
  max-width: 1200px;
  margin: 24px auto; /* 增加上下边距，与主站间距一致 */
}

.tree-header {
  /* 原 background: linear-gradient... 移除 */
  background: transparent; /* 新增 */
  padding: 0 0 16px 0; /* 调整下边距 */
  border-bottom: 1px solid var(--border-light); /* 新增分割线 */
  color: var(--text-dark);
  box-shadow: none; /* 移除原阴影 */
  margin-bottom: 16px;
}

.tree-header h2 {
  font-size: 24px; /* 与主站 h2 一致 */
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text-dark);
}

.stats {
  color: var(--text-light);
  font-size: 14px;
}

/* 搜索框 */
.search-box {
  background: white; /* 保持白色背景 */
  border: 1px solid var(--border-color); /* 新增边框 */
  border-radius: var(--border-radius-md); /* 圆角 8px */
  padding: 4px 4px 4px 16px; /* 调整内边距，模仿主站搜索框 */
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: none; /* 移除原阴影 */
}

.search-box input {
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 15px;
  flex: 1;
  outline: none;
}

.search-box input:focus {
  border-color: var(--primary-color); /* 聚焦时边框变色，但实际没有边框，可以加 outline 或 box-shadow */
  /* 主站搜索框聚焦时边框变主色，但 input 无边框，可给外层加样式 */
}

.search-box:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(228,30,43,0.1); /* 可选，模仿聚焦效果 */
}

/* 搜索结果提示 */
.search-results {
  color: var(--text-light);
  font-size: 13px;
  margin-left: 12px;
}

/* 树状图主体，原 .tree-wrapper 样式可简化 */
.tree-wrapper {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border: none;
  min-height: 400px;
}

/* 右侧详情面板 */
.node-detail-panel {
  position: fixed;
  top: 100px;
  right: 30px;
  width: 350px;
  background: white;
  border-radius: var(--border-radius-md);
  box-shadow: var(--box-shadow-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
  z-index: 1000;
}

.node-detail-panel .panel-header {
  background: #E41E2B;
  color: white;
  padding: 16px;
  border-radius: 8px 8px 0 0;
  border: none; /* 如果有边框可以去掉 */
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-light);
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: rgba(0,0,0,0.05);
  color: var(--primary-color);
}

.panel-content {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

/* 按钮样式（全部展开/收起） */
.btn-small {
  background: white;
  color: var(--text-dark);
  border: 1px solid var(--border-color);
  padding: 6px 15px;
  border-radius: var(--border-radius-full);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-small:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.search-box {
  background: white;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.search-box input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.3s;
}

.search-box input:focus {
  outline: none;
  border-color: #4f46e5;
}

.search-results {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
}

.tree-wrapper {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  min-height: 400px;
  position: relative;
}

.node-detail-panel {
  position: fixed;
  top: 100px;
  right: 30px;
  width: 350px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.panel-header {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.panel-content {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}
</style>