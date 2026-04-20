const DISPUTE_REASON_LABELS = Object.freeze({
  QUALITY_ISSUE: '商品质量问题',
  DESCRIPTION_MISMATCH: '商品描述不符',
  SHIPPING_DELAY: '物流或发货问题',
  SERVICE_ISSUE: '售后服务问题',
  WRONG_ITEM: '发错商品',
  MISSING_ITEMS: '缺件/少件',
  NO_DELIVERY: '未收到货',
  OTHER: '其他'
})

const DISPUTE_REQUEST_TYPE_LABELS = Object.freeze({
  FULL_REFUND: '全额退款',
  PARTIAL_REFUND: '部分退款',
  RETURN_AND_REFUND: '退货退款',
  REPLACE: '补发/换货'
})

const DISPUTE_STATUS_LABELS = Object.freeze({
  WAIT_SELLER_RESPONSE: '待卖家响应',
  WAIT_BUYER_CONFIRM: '待买家确认',
  NEGOTIATION_SUCCESS: '协商成功',
  NEGOTIATION_FAILED: '协商失败',
  SELLER_TIMEOUT: '卖家超时',
  ESCALATED_TO_ARBITRATION: '已升级仲裁',
  ARBITRATION_DECIDED: '平台已裁决',
  ARBITRATION_EXECUTING: '裁决执行中',
  ARBITRATION_EXECUTED: '裁决执行完成',
  CLOSED: '已关闭'
})

const SELLER_PROPOSAL_TYPE_LABELS = Object.freeze({
  FULL_REFUND: '全额退款',
  PARTIAL_REFUND: '部分退款',
  RETURN_AND_REFUND: '退货退款',
  REPLACE: '补发/换货'
})

const FREIGHT_BEARER_LABELS = Object.freeze({
  BUYER: '买家承担',
  SELLER: '卖家承担',
  PLATFORM: '平台承担',
  SHARED: '双方分担'
})

const EXECUTION_STATUS_LABELS = Object.freeze({
  PENDING: '待执行',
  IN_PROGRESS: '执行中',
  SUCCESS: '执行成功',
  FAILED: '执行失败',
  CLOSED: '已关闭'
})

const mapOrFallback = (map, value) => map[value] || value || '-'

export const getDisputeReasonLabel = (value) => mapOrFallback(DISPUTE_REASON_LABELS, value)

export const getDisputeRequestTypeLabel = (value) => mapOrFallback(DISPUTE_REQUEST_TYPE_LABELS, value)

export const getDisputeStatusLabel = (value) => mapOrFallback(DISPUTE_STATUS_LABELS, value)

export const getSellerProposalTypeLabel = (value) => mapOrFallback(SELLER_PROPOSAL_TYPE_LABELS, value)

export const getFreightBearerLabel = (value) => mapOrFallback(FREIGHT_BEARER_LABELS, value)

export const getExecutionStatusLabel = (value) => mapOrFallback(EXECUTION_STATUS_LABELS, value)
