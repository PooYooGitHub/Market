<template>
  <div class="page-container">
    <main class="main-content">
      <div class="cart-container">
        <h2 class="page-title">🛒 购物车</h2>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="cartList.length === 0" class="empty-cart">
          <div class="empty-icon">🛒</div>
          <p>购物车空空如也</p>
          <button class="btn btn-primary" @click="router.push('/products')">去逛逛</button>
        </div>

        <div v-else class="cart-content">
          <!-- 购物车列表 -->
          <div class="cart-list">
            <!-- 全选 -->
            <div class="cart-header">
              <label class="check-all">
                <input type="checkbox" :checked="isAllSelected" @change="handleSelectAll" />
                全选
              </label>
              <span class="header-product">商品</span>
              <span class="header-price">单价</span>
              <span class="header-qty">数量</span>
              <span class="header-subtotal">小计</span>
              <span class="header-action">操作</span>
            </div>

            <div v-for="item in cartList" :key="item.id" class="cart-item" :class="{ 'item-disabled': item.productStatus !== 1 }">
              <label class="item-check">
                <input
                  type="checkbox"
                  :checked="item.selected"
                  :disabled="item.productStatus !== 1"
                  @change="handleSelect(item)"
                />
              </label>

              <div class="item-product" @click="router.push(`/product/${item.productId}`)">
                <img
                  :src="item.productImage || defaultImage"
                  :alt="item.productTitle"
                  class="item-img"
                  @error="e => e.target.src = defaultImage"
                />
                <div class="item-info">
                  <div class="item-title">{{ item.productTitle }}</div>
                  <div v-if="item.productStatus !== 1" class="item-status-warn">
                    {{ item.productStatus === 2 ? '该商品已售出' : '该商品已下架' }}
                  </div>
                  <div class="item-seller">卖家：{{ item.sellerNickname || '未知' }}</div>
                </div>
              </div>

              <div class="item-price">¥{{ Number(item.productPrice).toFixed(2) }}</div>

              <div class="item-qty">
                <button class="qty-btn" @click="changeQty(item, -1)" :disabled="item.quantity <= 1">-</button>
                <span class="qty-num">{{ item.quantity }}</span>
                <button class="qty-btn" @click="changeQty(item, 1)">+</button>
              </div>

              <div class="item-subtotal">
                ¥{{ (Number(item.productPrice) * item.quantity).toFixed(2) }}
              </div>

              <div class="item-action">
                <button class="btn-remove" @click="handleRemove(item.id)">删除</button>
              </div>
            </div>
          </div>

          <!-- 结算栏 -->
          <div class="cart-footer">
            <div class="footer-left">
              <label class="check-all">
                <input type="checkbox" :checked="isAllSelected" @change="handleSelectAll" />
                全选
              </label>
              <button class="btn-clear" @click="handleClear">清空购物车</button>
              <button class="btn-remove-selected" @click="handleRemoveSelected" :disabled="selectedItems.length === 0">
                删除选中 ({{ selectedItems.length }})
              </button>
            </div>
            <div class="footer-right">
              <div class="summary">
                已选 <span class="accent">{{ selectedItems.length }}</span> 件，
                合计：<span class="total-price">¥{{ totalPrice }}</span>
              </div>
              <button
                class="btn btn-checkout"
                :disabled="selectedItems.length === 0"
                @click="handleCheckout"
              >
                结算 ({{ selectedItems.length }})
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 结算确认弹窗 -->
    <div v-if="showCheckoutModal" class="modal-overlay" @click.self="showCheckoutModal = false">
      <div class="modal">
        <h3>确认下单</h3>
        <div class="checkout-items">
          <div v-for="item in selectedItems" :key="item.id" class="checkout-item">
            <img :src="item.productImage || defaultImage" class="checkout-img" />
            <div class="checkout-info">
              <div class="checkout-title">{{ item.productTitle }}</div>
              <div class="checkout-price">¥{{ Number(item.productPrice).toFixed(2) }} × {{ item.quantity }}</div>
            </div>
          </div>
        </div>
        <div class="checkout-total">
          合计：<span class="total-price">¥{{ totalPrice }}</span>
        </div>
        <div class="checkout-note">
          <p style="margin: 0 0 8px 0; font-weight: bold;">📌 注意事项：</p>
          <ul style="margin: 0; padding-left: 20px;">
            <li>每件商品将单独生成一笔订单</li>
            <li>下单成功后，商品将被锁定为"已售出"状态</li>
            <li>其他用户将无法购买这些商品</li>
          </ul>
        </div>
        <div class="modal-btns">
          <button class="btn btn-secondary" @click="showCheckoutModal = false">取消</button>
          <button class="btn btn-primary" :disabled="checkingOut" @click="confirmCheckout">
            {{ checkingOut ? '下单中...' : '确认下单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getCartList, updateCartQuantity, removeFromCart,
  removeMultipleFromCart, updateCartSelected, selectAllCart,
  clearCart, createOrder
} from '@/api/trade'

const router = useRouter()

const cartList = ref([])
const loading = ref(true)
const showCheckoutModal = ref(false)
const checkingOut = ref(false)

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjEyIiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

const selectedItems = computed(() => cartList.value.filter(i => i.selected && i.productStatus === 1))
const selectableItems = computed(() => cartList.value.filter(i => i.productStatus === 1))
const isAllSelected = computed(() => selectableItems.value.length > 0 && selectableItems.value.every(i => i.selected))
const totalPrice = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + Number(i.productPrice) * i.quantity, 0).toFixed(2)
)

const loadCart = async () => {
  try {
    loading.value = true
    const res = await getCartList()
    cartList.value = res.data || []
  } catch (e) {
    console.error('加载购物车失败', e)
  } finally {
    loading.value = false
  }
}

const handleSelect = async (item) => {
  // 如果商品不是在售状态，不允许选择
  if (item.productStatus !== 1) return

  try {
    await updateCartSelected(item.id, !item.selected)
    item.selected = !item.selected
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

const handleSelectAll = async () => {
  try {
    const newVal = !isAllSelected.value
    await selectAllCart(newVal)
    // 只更新可选择的商品状态
    selectableItems.value.forEach(i => { i.selected = newVal })
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

const changeQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await updateCartQuantity(item.id, newQty)
    item.quantity = newQty
  } catch (e) {
    alert('更新数量失败：' + e.message)
  }
}

const handleRemove = async (cartId) => {
  if (!confirm('确定删除该商品？')) return
  try {
    await removeFromCart(cartId)
    cartList.value = cartList.value.filter(i => i.id !== cartId)
  } catch (e) {
    alert('删除失败：' + e.message)
  }
}

const handleRemoveSelected = async () => {
  if (selectedItems.value.length === 0) return
  if (!confirm(`确定删除选中的 ${selectedItems.value.length} 件商品？`)) return
  try {
    const ids = selectedItems.value.map(i => i.id)
    await removeMultipleFromCart(ids)
    cartList.value = cartList.value.filter(i => !ids.includes(i.id))
  } catch (e) {
    alert('删除失败：' + e.message)
  }
}

const handleClear = async () => {
  if (!confirm('确定清空购物车？')) return
  try {
    await clearCart()
    cartList.value = []
  } catch (e) {
    alert('清空失败：' + e.message)
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) return
  showCheckoutModal.value = true
}

const confirmCheckout = async () => {
  checkingOut.value = true
  const results = []
  const successfulCartIds = [] // 记录成功下单的购物车项ID

  for (const item of selectedItems.value) {
    try {
      const res = await createOrder({ productId: item.productId })
      results.push({ success: true, title: item.productTitle, orderId: res.data?.id })
      successfulCartIds.push(item.id) // 记录购物车项ID，用于移除
    } catch (e) {
      results.push({ success: false, title: item.productTitle, error: e.message })
    }
  }
  checkingOut.value = false
  showCheckoutModal.value = false

  const success = results.filter(r => r.success)
  const failed = results.filter(r => !r.success)

  // 移除成功下单的商品
  if (successfulCartIds.length > 0) {
    try {
      await removeMultipleFromCart(successfulCartIds)
      cartList.value = cartList.value.filter(i => !successfulCartIds.includes(i.id))
    } catch (e) {
      console.error('移除购物车商品失败:', e)
      // 如果移除失败，刷新购物车
      await loadCart()
    }
  }

  let msg = ''
  if (success.length) {
    msg += `✅ ${success.length} 件商品下单成功！\n\n`
    msg += `💡 已自动从购物车中移除，请前往"我的订单"页面完成支付。\n`
  }
  if (failed.length) {
    msg += `\n❌ ${failed.length} 件下单失败：\n` + failed.map(f => `  • ${f.title}: ${f.error}`).join('\n')
  }

  alert(msg)

  if (success.length) {
    router.push('/orders')
  }
}

onMounted(loadCart)
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f8f9fa; display: flex; flex-direction: column; }
.main-content { flex: 1; padding: 24px; }
.cart-container { max-width: 1000px; margin: 0 auto; }
.page-title { font-size: 24px; font-weight: 700; color: #333; margin-bottom: 20px; }
.loading, .empty-cart { text-align: center; padding: 60px 0; color: #999; }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-cart p { font-size: 16px; margin-bottom: 20px; }

/* 列表头 */
.cart-header { display: grid; grid-template-columns: 40px 1fr 100px 120px 100px 80px; align-items: center; padding: 12px 16px; background: #fafafa; border: 1px solid #e8e8e8; border-radius: 8px 8px 0 0; font-size: 13px; color: #999; font-weight: 500; }

/* 购物车项 */
.cart-item { display: grid; grid-template-columns: 40px 1fr 100px 120px 100px 80px; align-items: center; padding: 16px; background: #fff; border: 1px solid #e8e8e8; border-top: none; transition: background .2s; }
.cart-item:hover { background: #fafffe; }
.cart-item.item-disabled { opacity: 0.6; background: #f8f8f8; }
.cart-item.item-disabled:hover { background: #f8f8f8; }
.item-check { display: flex; justify-content: center; }
.item-check input:disabled { cursor: not-allowed; }
.item-product { display: flex; gap: 12px; align-items: center; cursor: pointer; }
.item-img { width: 72px; height: 72px; object-fit: cover; border-radius: 6px; border: 1px solid #f0f0f0; flex-shrink: 0; }
.item-info { min-width: 0; }
.item-title { font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 220px; }
.item-title:hover { color: #1890ff; }
.item-status-warn { font-size: 12px; color: #ff4d4f; margin-top: 2px; }
.item-seller { font-size: 12px; color: #999; margin-top: 4px; }
.item-price { font-size: 15px; color: #333; text-align: center; }
.item-qty { display: flex; align-items: center; gap: 8px; justify-content: center; }
.qty-btn { width: 26px; height: 26px; border: 1px solid #d9d9d9; background: #fff; border-radius: 4px; cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center; transition: all .2s; }
.qty-btn:hover:not(:disabled) { border-color: #1890ff; color: #1890ff; }
.qty-btn:disabled { cursor: not-allowed; opacity: .4; }
.qty-num { font-size: 15px; min-width: 24px; text-align: center; }
.item-subtotal { font-size: 15px; color: #ff4d4f; font-weight: 600; text-align: center; }
.item-action { text-align: center; }
.btn-remove { border: none; background: none; color: #999; cursor: pointer; font-size: 13px; padding: 4px 8px; border-radius: 4px; transition: color .2s; }
.btn-remove:hover { color: #ff4d4f; }

/* 结算栏 */
.cart-footer { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #fff; border: 1px solid #e8e8e8; border-top: 2px solid #1890ff; border-radius: 0 0 8px 8px; margin-top: 0; position: sticky; bottom: 0; }
.footer-left { display: flex; align-items: center; gap: 20px; }
.check-all { display: flex; align-items: center; gap: 6px; font-size: 14px; cursor: pointer; }
.btn-clear, .btn-remove-selected { border: none; background: none; color: #999; cursor: pointer; font-size: 13px; padding: 4px 8px; border-radius: 4px; transition: color .2s; }
.btn-clear:hover { color: #ff4d4f; }
.btn-remove-selected:hover:not(:disabled) { color: #ff4d4f; }
.btn-remove-selected:disabled { opacity: .4; cursor: not-allowed; }
.footer-right { display: flex; align-items: center; gap: 20px; }
.summary { font-size: 14px; color: #555; }
.accent { color: #ff4d4f; font-weight: 700; font-size: 16px; margin: 0 2px; }
.total-price { color: #ff4d4f; font-size: 22px; font-weight: 700; }
.btn { padding: 8px 20px; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; font-weight: 500; transition: all .2s; }
.btn-primary { background: #1890ff; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #40a9ff; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-secondary { background: #f0f0f0; color: #555; }
.btn-secondary:hover { background: #e0e0e0; }
.btn-checkout { background: #ff4d4f; color: #fff; padding: 10px 32px; font-size: 16px; }
.btn-checkout:hover:not(:disabled) { background: #ff7875; }
.btn-checkout:disabled { opacity: .5; cursor: not-allowed; }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: #fff; border-radius: 12px; padding: 28px; width: 480px; max-width: 95vw; max-height: 80vh; overflow-y: auto; }
.modal h3 { margin: 0 0 20px; font-size: 18px; color: #333; }
.checkout-items { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; max-height: 320px; overflow-y: auto; }
.checkout-item { display: flex; gap: 12px; align-items: center; padding: 10px; background: #fafafa; border-radius: 8px; }
.checkout-img { width: 60px; height: 60px; object-fit: cover; border-radius: 6px; }
.checkout-title { font-size: 14px; color: #333; margin-bottom: 4px; }
.checkout-price { font-size: 13px; color: #ff4d4f; }
.checkout-total { text-align: right; font-size: 15px; color: #555; margin-bottom: 8px; }
.checkout-note { font-size: 12px; color: #999; margin-bottom: 16px; }
.modal-btns { display: flex; gap: 12px; justify-content: flex-end; }
</style>

