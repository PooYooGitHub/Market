// 商品相关 API
import request from '@/utils/request'

/**
 * 获取商品分类列表
 * @returns {Promise} 返回分类列表
 */
export function getCategoryList() {
  return request({
    url: '/api/product/category/list',
    method: 'get'
  })
}

/**
 * 获取商品列表
 * @param {Object} params - 查询参数
 * @param {string} [params.keyword] - 搜索关键词
 * @param {number} [params.categoryId] - 分类ID
 * @param {number} [params.minPrice] - 最低价格
 * @param {number} [params.maxPrice] - 最高价格
 * @param {string} [params.sortField] - 排序字段：price/view_count/create_time
 * @param {string} [params.sortOrder] - 排序方式：asc/desc
 * @param {number} [params.pageNum] - 页码
 * @param {number} [params.pageSize] - 每页条数
 * @returns {Promise} 返回分页商品列表
 */
export function getProductList(params) {
  return request({
    url: '/api/product/list',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * @param {number} id - 商品ID
 * @returns {Promise} 返回商品详情
 */
export function getProductDetail(id) {
  return request({
    url: `/api/product/detail/${id}`,
    method: 'get'
  })
}

/**
 * 发布商品
 * @param {Object} data - 商品信息
 * @param {string} data.title - 商品标题
 * @param {string} data.description - 商品描述
 * @param {number} data.price - 当前价格
 * @param {number} [data.originalPrice] - 原价
 * @param {number} data.categoryId - 分类ID
 * @param {Array<string>} data.imageUrls - 图片URL数组
 * @returns {Promise} 返回新创建的商品ID
 */
export function publishProduct(data) {
  return request({
    url: '/api/product/publish',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {Object} data - 商品信息
 * @param {number} data.id - 商品ID
 * @param {string} data.title - 商品标题
 * @param {string} data.description - 商品描述
 * @param {number} data.price - 当前价格
 * @param {number} [data.originalPrice] - 原价
 * @param {number} data.categoryId - 分类ID
 * @param {Array<string>} data.imageUrls - 图片URL数组
 * @returns {Promise}
 */
export function updateProduct(data) {
  return request({
    url: '/api/product/update',
    method: 'put',
    data
  })
}

/**
 * 删除商品
 * @param {number} id - 商品ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
  return request({
    url: `/api/product/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取我的商品
 * @param {Object} params - 查询参数
 * @param {number} [params.pageNum] - 页码
 * @param {number} [params.pageSize] - 每页条数
 * @returns {Promise} 返回分页商品列表
 */
export function getMyProducts(params) {
  return request({
    url: '/api/product/my',
    method: 'get',
    params
  })
}

/**
 * 获取平台统计数据
 * @returns {Promise} 返回统计数据
 */
export function getPlatformStatistics() {
  return request({
    url: '/api/product/statistics',
    method: 'get'
  })
}
