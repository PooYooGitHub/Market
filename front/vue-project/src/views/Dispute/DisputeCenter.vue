<template>
  <div class="dispute-center-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <div>
            <h3>争议中心</h3>
            <p>统一查看我买到的与我卖出的争议处理进度</p>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我买到的" name="buyer">
          <DisputeMyList />
        </el-tab-pane>
        <el-tab-pane label="我卖出的" name="seller">
          <SellerDisputePending />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DisputeMyList from './DisputeMyList.vue'
import SellerDisputePending from './SellerDisputePending.vue'

const route = useRoute()
const router = useRouter()

const normalizeTab = (tab) => (tab === 'seller' ? 'seller' : 'buyer')
const activeTab = ref(normalizeTab(route.query.tab))

watch(
  () => route.query.tab,
  (tab) => {
    const normalized = normalizeTab(tab)
    if (activeTab.value !== normalized) {
      activeTab.value = normalized
    }
  },
  { immediate: true }
)

const handleTabChange = (tabName) => {
  const normalized = normalizeTab(tabName)
  if (route.query.tab === normalized) {
    return
  }
  router.replace({
    path: '/dispute/center',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}
</script>

<style scoped>
.dispute-center-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-row h3 {
  margin: 0;
}

.header-row p {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}
</style>
