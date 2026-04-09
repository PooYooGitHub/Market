<template>
  <div class="data-statistics">
    <div class="header">
      <h1>统计分析</h1>
    </div>

    <div class="filters">
      <select v-model="timeRange" class="filter-select">
        <option value="7">近7天</option>
        <option value="30">近30天</option>
        <option value="90">近3个月</option>
        <option value="365">近1年</option>
      </select>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <h3>访问量统计</h3>
        <div class="stat-list">
          <div class="stat-item">
            <span>页面浏览量</span>
            <span class="value">{{ statistics.pageViews }}</span>
          </div>
          <div class="stat-item">
            <span>独立访客</span>
            <span class="value">{{ statistics.uniqueVisitors }}</span>
          </div>
          <div class="stat-item">
            <span>跳出率</span>
            <span class="value">{{ statistics.bounceRate }}%</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <h3>用户行为</h3>
        <div class="stat-list">
          <div class="stat-item">
            <span>新用户注册</span>
            <span class="value">{{ statistics.newUsers }}</span>
          </div>
          <div class="stat-item">
            <span>活跃用户</span>
            <span class="value">{{ statistics.activeUsers }}</span>
          </div>
          <div class="stat-item">
            <span>用户留存率</span>
            <span class="value">{{ statistics.retention }}%</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <h3>交易统计</h3>
        <div class="stat-list">
          <div class="stat-item">
            <span>成交订单</span>
            <span class="value">{{ statistics.completedOrders }}</span>
          </div>
          <div class="stat-item">
            <span>转化率</span>
            <span class="value">{{ statistics.conversionRate }}%</span>
          </div>
          <div class="stat-item">
            <span>平均客单价</span>
            <span class="value">¥{{ statistics.avgOrderValue }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="detailed-analysis">
      <h3>详细分析</h3>
      <div class="analysis-tabs">
        <button
          v-for="tab in analysisTabs"
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 'traffic'" class="chart-placeholder">
          流量分析图表区域
        </div>
        <div v-if="activeTab === 'users'" class="chart-placeholder">
          用户分析图表区域
        </div>
        <div v-if="activeTab === 'sales'" class="chart-placeholder">
          销售分析图表区域
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataStatistics',
  data() {
    return {
      timeRange: '30',
      activeTab: 'traffic',
      statistics: {
        pageViews: 45623,
        uniqueVisitors: 12847,
        bounceRate: 34.2,
        newUsers: 892,
        activeUsers: 5624,
        retention: 68.5,
        completedOrders: 1247,
        conversionRate: 4.8,
        avgOrderValue: 156.8
      },
      analysisTabs: [
        { key: 'traffic', label: '流量分析' },
        { key: 'users', label: '用户分析' },
        { key: 'sales', label: '销售分析' }
      ]
    }
  },
  watch: {
    timeRange() {
      // 时间范围改变时重新加载数据
      this.loadStatistics()
    }
  },
  methods: {
    loadStatistics() {
      // API调用逻辑
      console.log('Loading statistics for time range:', this.timeRange)
    }
  }
}
</script>

<style scoped>
.data-statistics {
  padding: 20px;
}

.header h1 {
  margin: 0;
  color: #333;
  margin-bottom: 20px;
}

.filters {
  margin-bottom: 30px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item span:first-child {
  color: #666;
}

.value {
  font-weight: bold;
  color: #007bff;
  font-size: 18px;
}

.detailed-analysis {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.detailed-analysis h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.analysis-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.tab-btn {
  background: none;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
}

.tab-btn.active {
  color: #007bff;
  border-bottom-color: #007bff;
}

.tab-content {
  min-height: 300px;
}

.chart-placeholder {
  height: 300px;
  border: 2px dashed #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  border-radius: 4px;
}
</style>