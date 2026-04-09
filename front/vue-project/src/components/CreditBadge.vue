<template>
  <div class="credit-badge" :class="{ compact: compact }">
    <div v-if="!compact" class="credit-full">
      <div class="credit-icon">
        <el-icon :color="levelColor" size="20">
          <Medal v-if="level === '优秀'" />
          <Trophy v-else-if="level === '良好'" />
          <Star v-else-if="level === '一般'" />
          <Warning v-else-if="level === '较差'" />
          <Close v-else />
        </el-icon>
      </div>
      <div class="credit-info">
        <div class="credit-score" :style="{ color: levelColor }">{{ score }}</div>
        <div class="credit-level">{{ level }}</div>
      </div>
      <div v-if="showTooltip" class="credit-tooltip">
        <el-tooltip :content="tooltipContent" placement="top">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>
    <div v-else class="credit-compact">
      <el-tag
        :color="levelColor"
        :type="getTagType(level)"
        size="small"
        round
        class="credit-tag"
      >
        <el-icon size="14">
          <Medal v-if="level === '优秀'" />
          <Trophy v-else-if="level === '良好'" />
          <Star v-else-if="level === '一般'" />
          <Warning v-else-if="level === '较差'" />
          <Close v-else />
        </el-icon>
        {{ compact ? level : `${score} ${level}` }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Medal, Trophy, Star, Warning, Close, InfoFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  score: {
    type: Number,
    default: 0
  },
  level: {
    type: String,
    default: '一般'
  },
  levelColor: {
    type: String,
    default: '#E6A23C'
  },
  compact: {
    type: Boolean,
    default: false
  },
  showTooltip: {
    type: Boolean,
    default: true
  }
})

// 计算属性
const tooltipContent = computed(() => {
  const tips = {
    '优秀': '信用优秀，值得信赖的交易伙伴',
    '良好': '信用良好，可以放心交易',
    '一般': '信用一般，建议谨慎交易',
    '较差': '信用较差，交易时请多加注意',
    '很差': '信用很差，交易存在风险'
  }
  return tips[props.level] || '请保持诚信交易'
})

const getTagType = (level) => {
  const typeMap = {
    '优秀': 'success',
    '良好': 'primary',
    '一般': 'warning',
    '较差': 'danger',
    '很差': 'info'
  }
  return typeMap[level] || 'warning'
}
</script>

<style scoped>
.credit-badge {
  display: inline-block;
}

.credit-full {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #dee2e6;
}

.credit-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.credit-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.credit-score {
  font-size: 20px;
  font-weight: bold;
  line-height: 1;
}

.credit-level {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.credit-tooltip {
  margin-left: 4px;
}

.info-icon {
  color: #999;
  cursor: pointer;
  transition: color 0.3s ease;
}

.info-icon:hover {
  color: #409eff;
}

.credit-compact {
  display: inline-block;
}

.credit-tag {
  border: none;
  color: white;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.compact .credit-tag {
  padding: 4px 8px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .credit-full {
    padding: 10px 12px;
    gap: 10px;
  }

  .credit-icon {
    width: 35px;
    height: 35px;
  }

  .credit-score {
    font-size: 18px;
  }

  .credit-level {
    font-size: 11px;
  }
}
</style>