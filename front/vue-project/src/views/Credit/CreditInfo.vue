<template>
  <div class="credit-info">
    <el-card class="credit-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Medal /></el-icon> 我的信用信息</h3>
        </div>
      </template>

      <div class="credit-content" v-loading="loading">
        <div v-if="creditInfo" class="credit-display">
          <div class="score-section">
            <div class="score-circle">
              <div class="score-value" :style="{ color: creditInfo.levelColor }">
                {{ creditInfo.score }}
              </div>
              <div class="score-label">信用分</div>
            </div>
            <div class="level-info">
              <el-tag :color="creditInfo.levelColor" class="level-tag" size="large">
                {{ creditInfo.badgeName || creditInfo.level }}
              </el-tag>
              <div class="level-desc">
                {{ creditInfo.badgeDesc || getLevelDescription(creditInfo.level) }}
              </div>
            </div>
          </div>

          <div class="stats-section">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ creditInfo.totalEvaluations }}</div>
                  <div class="stat-label">总评价数</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ creditInfo.goodRate }}%</div>
                  <div class="stat-label">好评率</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="level-guide">
            <h4>信用等级说明</h4>
            <div class="level-list">
              <div v-for="level in levelGuide" :key="level.name" class="level-item">
                <el-tag :color="level.color" size="small">{{ level.name }}</el-tag>
                <span class="level-range">{{ level.range }}</span>
                <span class="level-desc">{{ level.description }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="evaluation-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Document /></el-icon> 评价记录</h3>
          <el-radio-group v-model="evaluationType" @change="loadEvaluations">
            <el-radio-button label="received">收到的评价</el-radio-button>
            <el-radio-button label="given">给出的评价</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="evaluation-list" v-loading="evaluationLoading">
        <div v-if="evaluations.length === 0" class="empty-state">
          <el-empty description="暂无评价记录" />
        </div>
        <div v-else>
          <div v-for="evaluation in evaluations" :key="evaluation.id" class="evaluation-item">
            <div class="evaluation-header">
              <div class="user-info">
                <el-avatar :src="getAvatar(evaluation)" size="small" />
                <span class="username">{{ getUsername(evaluation) }}</span>
              </div>
              <div class="evaluation-score">
                <el-rate v-model="evaluation.score" disabled show-score />
              </div>
            </div>
            <div class="evaluation-content">
              <p>{{ evaluation.comment || '该用户未留下评价内容' }}</p>
            </div>
            <div class="evaluation-time">
              {{ formatTime(evaluation.createTime) }}
            </div>
          </div>
        </div>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadEvaluations"
          style="margin-top: 20px; text-align: center;"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Medal, Document } from '@element-plus/icons-vue'
import { getCreditInfo, getReceivedEvaluations, getGivenEvaluations } from '@/api/credit'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const evaluationLoading = ref(false)
const creditInfo = ref(null)
const evaluations = ref([])
const evaluationType = ref('received')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const levelGuide = ref([
  { name: '钻石信誉', range: '900-950', color: '#67C23A', description: '长期稳定高信用用户' },
  { name: '金牌信誉', range: '850-899', color: '#E6A23C', description: '高可信交易伙伴' },
  { name: '银牌信誉', range: '750-849', color: '#909399', description: '信用良好，可以放心交易' },
  { name: '铜牌信誉', range: '650-749', color: '#B88230', description: '信用稳定，持续成长中' },
  { name: '新手认证', range: '350-649', color: '#C0C4CC', description: '交易次数较少，建议先小额交易' }
])

const loadCreditInfo = async () => {
  loading.value = true
  try {
    const response = await getCreditInfo()
    if (response.success) {
      creditInfo.value = response.data
    }
  } catch (error) {
    console.error('加载信用信息失败:', error)
    ElMessage.error('加载信用信息失败')
  } finally {
    loading.value = false
  }
}

const loadEvaluations = async () => {
  evaluationLoading.value = true
  try {
    const apiCall = evaluationType.value === 'received' ? getReceivedEvaluations : getGivenEvaluations
    const response = await apiCall({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (response.success) {
      evaluations.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载评价记录失败:', error)
    ElMessage.error('加载评价记录失败')
  } finally {
    evaluationLoading.value = false
  }
}

const getLevelDescription = (level) => {
  const levelMap = {
    '优秀': '信誉优秀，值得信赖',
    '良好': '信誉良好，可以放心交易',
    '稳定': '信誉稳定，持续成长中',
    '成长中': '信用成长中，建议先小额交易',
    '新手': '新用户，建议先小额交易'
  }
  return levelMap[level] || '请保持诚信交易'
}

const getAvatar = (evaluation) => {
  return evaluationType.value === 'received'
    ? evaluation.evaluatorAvatar
    : evaluation.targetAvatar
}

const getUsername = (evaluation) => {
  return evaluationType.value === 'received'
    ? evaluation.evaluatorName || evaluation.evaluatorNickname || '匿名用户'
    : evaluation.targetName || evaluation.targetNickname || '匿名用户'
}

const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
}

onMounted(() => {
  loadCreditInfo()
  loadEvaluations()
})
</script>

<style scoped>
.credit-info {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.credit-display {
  padding: 20px 0;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 15px;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.level-tag {
  border: none;
  color: white;
  font-size: 18px;
  font-weight: bold;
  padding: 8px 20px;
  border-radius: 20px;
}

.level-desc {
  font-size: 16px;
  color: #666;
}

.stats-section {
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.level-guide {
  margin-top: 30px;
  padding: 20px;
  background: #fafafa;
  border-radius: 10px;
}

.level-guide h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.level-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.level-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 8px 0;
}

.level-range {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}

.evaluation-list {
  min-height: 300px;
}

.evaluation-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 15px;
  background: white;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
  color: #303133;
}

.evaluation-content {
  margin: 15px 0;
}

.evaluation-content p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.empty-state {
  padding: 50px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .credit-info {
    padding: 10px;
  }

  .score-section {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .card-header {
    flex-direction: column;
    gap: 15px;
  }
}
</style>