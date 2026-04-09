// 消息提示服务
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

export const Message = {
  // 成功消息
  success(message, duration = 3000) {
    return ElMessage.success({
      message,
      duration,
      showClose: true
    })
  },

  // 错误消息
  error(message, duration = 4000) {
    return ElMessage.error({
      message,
      duration,
      showClose: true
    })
  },

  // 警告消息
  warning(message, duration = 3500) {
    return ElMessage.warning({
      message,
      duration,
      showClose: true
    })
  },

  // 信息消息
  info(message, duration = 3000) {
    return ElMessage.info({
      message,
      duration,
      showClose: true
    })
  },

  // 加载中消息
  loading(message = '加载中...') {
    return ElMessage({
      message,
      type: 'info',
      duration: 0,
      showClose: false,
      customClass: 'loading-message'
    })
  }
}

export const MessageBox = {
  // 确认对话框
  confirm(message, title = '提示', options = {}) {
    return ElMessageBox.confirm(
      message,
      title,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        ...options
      }
    )
  },

  // 输入对话框
  prompt(message, title = '输入', options = {}) {
    return ElMessageBox.prompt(
      message,
      title,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        ...options
      }
    )
  },

  // 警告对话框
  alert(message, title = '提示', options = {}) {
    return ElMessageBox.alert(
      message,
      title,
      {
        confirmButtonText: '确定',
        ...options
      }
    )
  }
}

export const Notification = {
  // 成功通知
  success(title, message = '', duration = 4500) {
    return ElNotification.success({
      title,
      message,
      duration,
      position: 'top-right'
    })
  },

  // 错误通知
  error(title, message = '', duration = 4500) {
    return ElNotification.error({
      title,
      message,
      duration,
      position: 'top-right'
    })
  },

  // 警告通知
  warning(title, message = '', duration = 4500) {
    return ElNotification.warning({
      title,
      message,
      duration,
      position: 'top-right'
    })
  },

  // 信息通知
  info(title, message = '', duration = 4500) {
    return ElNotification.info({
      title,
      message,
      duration,
      position: 'top-right'
    })
  }
}

// 导出默认的消息服务
export default Message