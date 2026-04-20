<template>
  <div class="user-profile-container">
    <div class="profile-header">
      <button @click="router.back()" class="back-btn">← 返回</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!userInfo" class="error">
      <p>用户不存在或无权访问</p>
      <button @click="router.back()" class="btn btn-primary">返回</button>
    </div>

    <div v-else class="profile-content">
      <!-- 用户基本信息 -->
      <div class="basic-info-section">
        <div class="avatar-section">
          <img
            :src="userInfo.avatar || defaultAvatar"
            :alt="userInfo.nickname"
            class="user-avatar"
            @error="e => e.target.src = defaultAvatar"
          />
          <div class="user-basic">
            <h2 class="user-name">{{ userInfo.nickname || userInfo.username }}</h2>
            <div class="user-id">用户ID: {{ userInfo.id }}</div>
            <div class="join-time">加入时间: {{ formatTime(userInfo.createTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 信用信息 -->
      <div class="credit-section" v-if="creditInfo">
        <h3 class="section-title">💎 信用信息</h3>
        <div class="credit-display">
          <div class="credit-score">
            <div class="score-circle" :style="{ borderColor: creditInfo.levelColor }">
              <div class="score-value" :style="{ color: creditInfo.levelColor }">
                {{ creditInfo.score }}
              </div>
              <div class="score-label">信用分</div>
            </div>
            <div class="level-info">
              <div class="credit-level" :style="{ backgroundColor: creditInfo.levelColor }">
                {{ creditInfo.badgeName || creditInfo.level }}
              </div>
              <div class="level-desc">{{ creditInfo.badgeDesc || getLevelDescription(creditInfo.level) }}</div>
            </div>
          </div>
          <div class="credit-stats">
            <div class="stat-item">
              <div class="stat-value">{{ creditInfo.totalEvaluations }}</div>
              <div class="stat-label">总评价数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ creditInfo.goodRate }}%</div>
              <div class="stat-label">好评率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ creditInfo.avgScore }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价记录 -->
      <div class="evaluations-section" v-if="evaluations.length > 0">
        <h3 class="section-title">⭐ 收到的评价</h3>
        <div class="evaluations-list">
          <div v-for="evaluation in evaluations" :key="evaluation.id" class="evaluation-item">
            <div class="evaluation-header">
              <div class="evaluator-info">
                <img
                  :src="evaluation.evaluatorAvatar || defaultAvatar"
                  :alt="evaluation.evaluatorNickname"
                  class="evaluator-avatar"
                />
                <span class="evaluator-name">{{ evaluation.evaluatorNickname || '匿名用户' }}</span>
              </div>
              <div class="evaluation-score">
                <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= evaluation.score }">
                  ⭐
                </span>
              </div>
            </div>
            <div class="evaluation-content" v-if="evaluation.content">
              {{ evaluation.content }}
            </div>
            <div class="evaluation-time">
              {{ formatTime(evaluation.createTime) }}
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <button @click="loadMoreEvaluations" :disabled="loadingMore" class="btn btn-default">
            {{ loadingMore ? '加载中...' : '查看更多评价' }}
          </button>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-evaluations">
        <p>该用户暂无评价记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCreditInfo, getReceivedEvaluations } from '@/api/credit'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const loadingMore = ref(false)
const userInfo = ref(null)
const creditInfo = ref(null)
const evaluations = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(false)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Ccircle cx="40" cy="40" r="40" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="24"%3E头像%3C/text%3E%3C/svg%3E'

// 加载用户信息
const loadUserProfile = async () => {
  loading.value = true
  try {
    const userId = route.params.id

    // 获取用户信用信息（包含基本用户信息）
    const creditRes = await getCreditInfo(userId)
    if (creditRes.code === 200) {
      creditInfo.value = creditRes.data
      userInfo.value = creditRes.data.userInfo || {
        id: userId,
        nickname: `用户${userId}`,
        username: `用户${userId}`,
        createTime: new Date().toISOString()
      }
    }

    // 获取用户评价
    await loadEvaluations()
  } catch (error) {
    console.error('加载用户资料失败:', error)
    userInfo.value = null
  } finally {
    loading.value = false
  }
}

// 加载评价记录
const loadEvaluations = async (page = 1) => {
  try {
    const userId = route.params.id
    const res = await getReceivedEvaluations({
      userId,
      pageNum: page,
      pageSize: pageSize.value
    })

    if (res.code === 200) {
      const data = res.data
      if (page === 1) {
        evaluations.value = data.records || []
      } else {
        evaluations.value.push(...(data.records || []))
      }
      hasMore.value = evaluations.value.length < (data.total || 0)
    }
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

// 加载更多评价
const loadMoreEvaluations = async () => {
  loadingMore.value = true
  try {
    currentPage.value++
    await loadEvaluations(currentPage.value)
  } catch (error) {
    console.error('加载更多评价失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 获取等级描述
const getLevelDescription = (level) => {
  const descriptions = {
    '优秀': '信誉优秀，值得信赖',
    '良好': '信誉良好，可放心交易',
    '稳定': '信誉稳定，持续成长中',
    '成长中': '信用成长中，建议先小额交易',
    '新手': '新用户，建议先小额交易'
  }
  return descriptions[level] || '暂无信用记录'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.profile-header {
  margin-bottom: 20px;
}

.back-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.back-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.loading, .error {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.basic-info-section {
  margin-bottom: 30px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.user-id, .join-time {
  font-size: 14px;
  color: #999;
  margin-bottom: 4px;
}

.credit-section, .evaluations-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.credit-display {
  display: flex;
  gap: 30px;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.credit-score {
  display: flex;
  align-items: center;
  gap: 20px;
}

.score-circle {
  width: 100px;
  height: 100px;
  border: 4px solid #1890ff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
  color: #1890ff;
}

.score-label {
  font-size: 12px;
  color: #999;
}

.level-info {
  text-align: center;
}

.credit-level {
  display: inline-block;
  padding: 6px 16px;
  background: #1890ff;
  color: white;
  border-radius: 20px;
  font-weight: bold;
  margin-bottom: 8px;
}

.level-desc {
  font-size: 14px;
  color: #666;
}

.credit-stats {
  display: flex;
  gap: 30px;
  margin-left: auto;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.evaluations-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.evaluation-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.evaluator-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.evaluator-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.evaluator-name {
  font-weight: 500;
  color: #333;
}

.evaluation-score {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 16px;
  opacity: 0.3;
}

.star.active {
  opacity: 1;
}

.evaluation-content {
  color: #555;
  line-height: 1.6;
  margin-bottom: 8px;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.empty-evaluations {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.btn {
  padding: 8px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-default {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.btn-default:hover {
  border-color: #1890ff;
  color: #1890ff;
}

@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .credit-display {
    flex-direction: column;
    text-align: center;
  }

  .credit-stats {
    justify-content: center;
    margin-left: 0;
  }
}
</style>