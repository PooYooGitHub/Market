<template>
  <div class="reports">
    <h2>报表导出</h2>

    <el-card>
      <h3>数据报表生成</h3>
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="报表类型">
          <el-select v-model="reportForm.type" placeholder="请选择报表类型">
            <el-option label="仲裁案件统计报表" value="arbitration" />
            <el-option label="处理效率报表" value="efficiency" />
            <el-option label="月度汇总报表" value="monthly" />
            <el-option label="年度总结报表" value="yearly" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="reportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="导出格式">
          <el-radio-group v-model="reportForm.format">
            <el-radio value="excel">Excel</el-radio>
            <el-radio value="pdf">PDF</el-radio>
            <el-radio value="csv">CSV</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="generateReport">
            <el-icon><Download /></el-icon>
            生成报表
          </el-button>
          <el-button @click="previewReport">预览</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史报表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>历史报表</span>
      </template>

      <el-table :data="reportHistory" style="width: 100%">
        <el-table-column prop="name" label="报表名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="generateTime" label="生成时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'warning'">
              {{ row.status === 'completed' ? '已完成' : '生成中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="downloadReport(row)">
              下载
            </el-button>
            <el-button size="small" @click="deleteReport(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const reportForm = reactive({
  type: '',
  dateRange: null,
  format: 'excel'
})

const reportHistory = ref([])

const generateReport = () => {
  if (!reportForm.type) {
    ElMessage.warning('请选择报表类型')
    return
  }

  ElMessage.success('报表生成中，请稍候...')
  // 这里添加实际的报表生成逻辑
}

const previewReport = () => {
  ElMessage.info('报表预览功能开发中')
}

const downloadReport = (report) => {
  ElMessage.success(`正在下载 ${report.name}`)
}

const deleteReport = (report) => {
  ElMessage.success(`已删除 ${report.name}`)
}

const loadReportHistory = () => {
  // 模拟历史报表数据
  reportHistory.value = [
    {
      id: 1,
      name: '4月份仲裁案件统计',
      type: 'arbitration',
      generateTime: '2024-04-05 10:30',
      status: 'completed'
    },
    {
      id: 2,
      name: '处理效率月度报表',
      type: 'efficiency',
      generateTime: '2024-04-01 09:15',
      status: 'completed'
    }
  ]
}

onMounted(() => {
  loadReportHistory()
})
</script>

<style scoped>
.reports {
  padding: 20px;
}
</style>