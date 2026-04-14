<template>
  <el-card class="evidence-compare-card" shadow="never">
    <template #header>
      <div class="panel-header">
        <div>
          <div class="panel-title">双方证据对照</div>
          <div class="panel-subtitle">并排审查双方提交材料，减少上下反复切换</div>
        </div>
      </div>
    </template>

    <el-row :gutter="14" class="compare-row">
      <el-col :xs="24" :md="12">
        <div class="party-panel applicant">
          <div class="party-title-wrap">
            <span class="party-title">申请人证据</span>
            <el-tag size="small" type="primary">{{ applicantEvidence.length }} 条</el-tag>
          </div>

          <div v-if="!applicantEvidence.length" class="compact-empty">
            <el-icon><Document /></el-icon>
            <span>暂无申请人证据</span>
          </div>

          <div v-else class="evidence-list">
            <div
              v-for="item in applicantEvidence"
              :key="item.id"
              class="evidence-item"
            >
              <div class="evidence-media">
                <el-image
                  v-if="isImage(item)"
                  :src="item.thumbnail || item.url"
                  :preview-src-list="getPreviewList(item)"
                  fit="cover"
                  preview-teleported
                  class="evidence-thumb"
                />
                <div v-else class="evidence-icon-box">
                  <el-icon>
                    <component :is="getTypeIcon(item.type)" />
                  </el-icon>
                </div>
              </div>
              <div class="evidence-main">
                <div class="evidence-top">
                  <span class="evidence-title">{{ item.title }}</span>
                  <el-tag size="small" :type="getTagType(item.type)">{{ getTypeLabel(item.type) }}</el-tag>
                </div>
                <div class="evidence-desc">{{ item.description || '无补充说明' }}</div>
                <div class="evidence-time">{{ item.createTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="party-panel respondent">
          <div class="party-title-wrap">
            <span class="party-title">被申请人证据</span>
            <el-tag size="small" type="danger">{{ respondentEvidence.length }} 条</el-tag>
          </div>

          <div v-if="!respondentEvidence.length" class="compact-empty">
            <el-icon><Document /></el-icon>
            <span>暂无被申请人证据</span>
          </div>

          <div v-else class="evidence-list">
            <div
              v-for="item in respondentEvidence"
              :key="item.id"
              class="evidence-item"
            >
              <div class="evidence-media">
                <el-image
                  v-if="isImage(item)"
                  :src="item.thumbnail || item.url"
                  :preview-src-list="getPreviewList(item)"
                  fit="cover"
                  preview-teleported
                  class="evidence-thumb"
                />
                <div v-else class="evidence-icon-box">
                  <el-icon>
                    <component :is="getTypeIcon(item.type)" />
                  </el-icon>
                </div>
              </div>
              <div class="evidence-main">
                <div class="evidence-top">
                  <span class="evidence-title">{{ item.title }}</span>
                  <el-tag size="small" :type="getTagType(item.type)">{{ getTypeLabel(item.type) }}</el-tag>
                </div>
                <div class="evidence-desc">{{ item.description || '无补充说明' }}</div>
                <div class="evidence-time">{{ item.createTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ChatDotRound, DataAnalysis, Document, Picture, Tickets } from '@element-plus/icons-vue'

defineProps({
  applicantEvidence: {
    type: Array,
    default: () => []
  },
  respondentEvidence: {
    type: Array,
    default: () => []
  }
})

const typeLabelMap = {
  image: '图片',
  chat: '聊天截图',
  text: '文字说明',
  system: '系统记录'
}

const typeTagMap = {
  image: 'success',
  chat: 'info',
  text: 'warning',
  system: 'primary'
}

const typeIconMap = {
  image: Picture,
  chat: ChatDotRound,
  text: Document,
  system: DataAnalysis
}

const getTypeLabel = (type) => typeLabelMap[type] || '其他'
const getTagType = (type) => typeTagMap[type] || 'info'
const getTypeIcon = (type) => typeIconMap[type] || Tickets
const isImage = (item) => item.type === 'image' && (item.thumbnail || item.url)
const getPreviewList = (item) => [item.url || item.thumbnail]
</script>

<style scoped>
.evidence-compare-card {
  border-radius: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.panel-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.compare-row {
  margin-top: 4px;
}

.party-panel {
  height: 100%;
  border: 1px solid #e6ebf5;
  border-radius: 10px;
  padding: 12px;
  background: #fbfdff;
}

.party-panel.applicant {
  border-color: #dce8ff;
}

.party-panel.respondent {
  border-color: #ffe0e0;
}

.party-title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.party-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.compact-empty {
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px dashed #d1d9e8;
  border-radius: 8px;
  font-size: 12px;
  color: #6b7280;
  background: #fff;
}

.evidence-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.evidence-item {
  display: flex;
  gap: 10px;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
  padding: 10px;
  background: #fff;
}

.evidence-media {
  width: 72px;
  flex-shrink: 0;
}

.evidence-thumb {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px solid #d9e2f2;
}

.evidence-icon-box {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px solid #d9e2f2;
  background: #f4f7fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #607089;
}

.evidence-main {
  flex: 1;
  min-width: 0;
}

.evidence-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.evidence-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.evidence-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
  color: #374151;
}

.evidence-time {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
</style>
