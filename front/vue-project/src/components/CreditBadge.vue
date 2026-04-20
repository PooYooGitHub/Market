<template>
  <div class="credit-badge" :class="{ compact: compact }">
    <div v-if="!compact" class="credit-full">
      <div class="credit-icon">
        <el-icon :color="displayColor" size="20">
          <Medal v-if="iconType === 'medal'" />
          <Trophy v-else-if="iconType === 'trophy'" />
          <Star v-else-if="iconType === 'star'" />
          <Warning v-else-if="iconType === 'warning'" />
          <Close v-else />
        </el-icon>
      </div>
      <div class="credit-info">
        <div class="credit-score" :style="{ color: displayColor }">{{ score }}</div>
        <div class="credit-level">{{ displayLabel }}</div>
      </div>
      <div v-if="showTooltip" class="credit-tooltip">
        <el-tooltip :content="tooltipContent" placement="top">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>
    <div v-else class="credit-compact">
      <el-tag
        :color="displayColor"
        :type="tagType"
        size="small"
        round
        class="credit-tag"
      >
        <el-icon size="14">
          <Medal v-if="iconType === 'medal'" />
          <Trophy v-else-if="iconType === 'trophy'" />
          <Star v-else-if="iconType === 'star'" />
          <Warning v-else-if="iconType === 'warning'" />
          <Close v-else />
        </el-icon>
        {{ compact ? displayLabel : `${score} ${displayLabel}` }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Medal, Trophy, Star, Warning, Close, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  score: {
    type: Number,
    default: 0
  },
  level: {
    type: String,
    default: '成长中'
  },
  levelColor: {
    type: String,
    default: '#E6A23C'
  },
  badgeName: {
    type: String,
    default: ''
  },
  badgeDesc: {
    type: String,
    default: ''
  },
  badgeColor: {
    type: String,
    default: ''
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

const displayLabel = computed(() => props.badgeName || props.level || '成长中')
const displayColor = computed(() => props.badgeColor || props.levelColor || '#E6A23C')

const tooltipContent = computed(() => {
  if (props.badgeDesc) {
    return props.badgeDesc
  }
  const tips = {
    '优秀': '信用优秀，值得信赖的交易伙伴',
    '良好': '信用良好，可以放心交易',
    '稳定': '信用稳定，持续成长中',
    '成长中': '信用成长中，建议先小额交易',
    '新手': '新用户，建议先小额交易'
  }
  return tips[props.level] || '请保持诚信交易'
})

const iconType = computed(() => {
  const label = displayLabel.value
  if (label.includes('钻石') || label.includes('金牌') || label.includes('优秀')) {
    return 'medal'
  }
  if (label.includes('银牌') || label.includes('良好')) {
    return 'trophy'
  }
  if (label.includes('铜牌') || label.includes('稳定')) {
    return 'star'
  }
  if (label.includes('成长')) {
    return 'warning'
  }
  return 'close'
})

const tagType = computed(() => {
  const label = displayLabel.value
  if (label.includes('钻石') || label.includes('金牌') || label.includes('优秀')) {
    return 'success'
  }
  if (label.includes('银牌') || label.includes('良好')) {
    return 'primary'
  }
  if (label.includes('铜牌') || label.includes('稳定')) {
    return 'warning'
  }
  if (label.includes('成长') || label.includes('新手')) {
    return 'info'
  }
  return 'info'
})
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