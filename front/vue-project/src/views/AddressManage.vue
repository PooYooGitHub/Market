<template>
  <div class="address-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <div>
            <h3>收货地址管理</h3>
            <p class="subtitle">用于下单时选择收货信息，支持设置默认地址</p>
          </div>
          <el-button type="primary" @click="openCreateDialog">新增地址</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="addressList.length === 0" description="暂无收货地址">
          <el-button type="primary" @click="openCreateDialog">新增地址</el-button>
        </el-empty>

        <div v-else class="address-list">
          <div v-for="item in addressList" :key="item.id" class="address-item">
            <div class="address-main">
              <div class="line-1">
                <span class="name">{{ item.receiverName }}</span>
                <span class="phone">{{ item.receiverPhone }}</span>
                <el-tag v-if="item.isDefault" type="success" effect="plain" size="small">默认地址</el-tag>
              </div>
              <div class="line-2">
                {{ formatAddress(item) }}
              </div>
            </div>
            <div class="address-actions">
              <el-button size="small" @click="openEditDialog(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
              <el-button
                v-if="!item.isDefault"
                size="small"
                type="primary"
                plain
                @click="handleSetDefault(item)"
              >
                设为默认
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑收货地址' : '新增收货地址'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model.trim="form.receiverName" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model.trim="form.receiverPhone" maxlength="20" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model.trim="form.province" maxlength="50" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model.trim="form.city" maxlength="50" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model.trim="form.district" maxlength="50" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model.trim="form.detailAddress" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="邮编" prop="postalCode">
          <el-input v-model.trim="form.postalCode" maxlength="20" placeholder="可选，6位数字" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAddress,
  deleteAddress,
  getAddressList,
  setDefaultAddress,
  updateAddress
} from '@/api/user'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()
const addressList = ref([])

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: false
})

const phoneValidator = (_, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
    return
  }
  if (!/^1\d{10}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
    return
  }
  callback()
}

const postalCodeValidator = (_, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (!/^\d{6}$/.test(value)) {
    callback(new Error('邮编应为6位数字'))
    return
  }
  callback()
}

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [{ validator: phoneValidator, trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  postalCode: [{ validator: postalCodeValidator, trigger: 'blur' }]
}

const resetForm = () => {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.postalCode = ''
  form.isDefault = false
}

const loadAddressList = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res?.data || []
  } catch (error) {
    ElMessage.error(error?.message || '加载地址失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (item) => {
  isEdit.value = true
  editingId.value = item.id
  form.receiverName = item.receiverName || ''
  form.receiverPhone = item.receiverPhone || ''
  form.province = item.province || ''
  form.city = item.city || ''
  form.district = item.district || ''
  form.detailAddress = item.detailAddress || ''
  form.postalCode = item.postalCode || ''
  form.isDefault = Boolean(item.isDefault)
  dialogVisible.value = true
}

const buildPayload = () => ({
  receiverName: form.receiverName,
  receiverPhone: form.receiverPhone,
  province: form.province,
  city: form.city,
  district: form.district,
  detailAddress: form.detailAddress,
  postalCode: form.postalCode,
  isDefault: form.isDefault
})

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value) {
      await updateAddress(editingId.value, payload)
      ElMessage.success('地址更新成功')
    } else {
      await createAddress(payload)
      ElMessage.success('地址新增成功')
    }
    dialogVisible.value = false
    await loadAddressList()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该收货地址吗？', '删除地址', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddress(item.id)
    ElMessage.success('删除成功')
    await loadAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

const handleSetDefault = async (item) => {
  try {
    await setDefaultAddress(item.id)
    ElMessage.success('设置默认地址成功')
    await loadAddressList()
  } catch (error) {
    ElMessage.error(error?.message || '设置默认地址失败')
  }
}

const formatAddress = (item) => {
  const area = [item.province, item.city, item.district].filter(Boolean).join(' ')
  const postal = item.postalCode ? `（邮编：${item.postalCode}）` : ''
  return `${area} ${item.detailAddress || ''}${postal}`.trim()
}

onMounted(loadAddressList)
</script>

<style scoped>
.address-page {
  max-width: 960px;
  margin: 24px auto;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-row h3 {
  margin: 0;
}

.subtitle {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.line-1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  color: #303133;
}

.phone {
  color: #606266;
}

.line-2 {
  color: #606266;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .address-page {
    margin: 12px;
  }

  .address-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .address-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
