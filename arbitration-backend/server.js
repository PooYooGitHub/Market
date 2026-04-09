const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const multer = require('multer');

const app = express();
const PORT = 8000;
const JWT_SECRET = 'arbitration-system-secret-key';

// 中间件
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 文件上传配置
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, './uploads/');
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage: storage });

// 创建数据库连接
const db = new sqlite3.Database('./arbitration.db');

// 初始化数据库表
db.serialize(() => {
  // 管理员表
  db.run(`CREATE TABLE IF NOT EXISTS admins (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    role TEXT DEFAULT 'admin',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  // 仲裁案件表
  db.run(`CREATE TABLE IF NOT EXISTS arbitration_cases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    case_number TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    applicant_name TEXT NOT NULL,
    applicant_contact TEXT,
    respondent_name TEXT NOT NULL,
    respondent_contact TEXT,
    amount REAL DEFAULT 0,
    status TEXT DEFAULT 'pending',
    priority TEXT DEFAULT 'normal',
    category TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    assigned_arbitrator TEXT,
    scheduled_date TEXT,
    resolution TEXT
  )`);

  // 仲裁员表
  db.run(`CREATE TABLE IF NOT EXISTS arbitrators (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    qualification TEXT,
    specialization TEXT,
    contact_info TEXT,
    status TEXT DEFAULT 'available',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  // 创建默认管理员账号
  const hashedPassword = bcrypt.hashSync('admin123', 10);
  db.run(`INSERT OR IGNORE INTO admins (username, password, name, email)
          VALUES ('admin', ?, '系统管理员', 'admin@arbitration.com')`, [hashedPassword]);

  // 插入一些示例数据
  db.run(`INSERT OR IGNORE INTO arbitration_cases
          (case_number, title, description, applicant_name, applicant_contact, respondent_name, respondent_contact, amount, category)
          VALUES
          ('ARB-2024-001', '商品质量纠纷', '购买的商品与描述不符，要求退款', '张三', '13812345678', '李四商店', '13987654321', 299.00, '商品纠纷'),
          ('ARB-2024-002', '服务合同纠纷', '服务提供方未按约定完成服务', '王五', '13776543210', '某某服务公司', '021-12345678', 5000.00, '服务纠纷'),
          ('ARB-2024-003', '运输损坏赔偿', '快递运输过程中商品损坏', '赵六', '13698765432', '快递公司', '400-123-4567', 800.00, '运输纠纷')`);

  db.run(`INSERT OR IGNORE INTO arbitrators
          (name, qualification, specialization, contact_info)
          VALUES
          ('陈仲裁', '高级仲裁员', '商事争议', '13511111111'),
          ('刘仲裁', '资深仲裁员', '消费纠纷', '13522222222'),
          ('程仲裁', '专业仲裁员', '合同纠纷', '13533333333')`);
});

// JWT验证中间件
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ code: 401, message: '未提供访问令牌' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ code: 403, message: '无效的访问令牌' });
    }
    req.user = user;
    next();
  });
};

// ==================== 认证相关API ====================

// 管理员登录
app.post('/api/admin/login', (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({
      code: 400,
      message: '用户名和密码不能为空'
    });
  }

  db.get('SELECT * FROM admins WHERE username = ?', [username], (err, row) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    if (!row) {
      return res.status(401).json({
        code: 401,
        message: '用户名或密码错误'
      });
    }

    if (!bcrypt.compareSync(password, row.password)) {
      return res.status(401).json({
        code: 401,
        message: '用户名或密码错误'
      });
    }

    const token = jwt.sign(
      { id: row.id, username: row.username, role: row.role },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    res.json({
      code: 200,
      message: '登录成功',
      data: {
        token,
        user: {
          id: row.id,
          username: row.username,
          name: row.name,
          email: row.email,
          role: row.role
        }
      }
    });
  });
});

// 获取当前用户信息
app.get('/api/admin/info', authenticateToken, (req, res) => {
  db.get('SELECT * FROM admins WHERE id = ?', [req.user.id], (err, row) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    if (!row) {
      return res.status(404).json({
        code: 404,
        message: '用户不存在'
      });
    }

    res.json({
      code: 200,
      message: '获取成功',
      data: {
        id: row.id,
        username: row.username,
        name: row.name,
        email: row.email,
        role: row.role,
        created_at: row.created_at
      }
    });
  });
});

// ==================== 仲裁案件管理API ====================

// 获取案件列表
app.get('/api/arbitration/cases', authenticateToken, (req, res) => {
  const { page = 1, size = 10, status, search } = req.query;
  const offset = (page - 1) * size;

  let sql = 'SELECT * FROM arbitration_cases WHERE 1=1';
  let countSql = 'SELECT COUNT(*) as total FROM arbitration_cases WHERE 1=1';
  const params = [];

  if (status) {
    sql += ' AND status = ?';
    countSql += ' AND status = ?';
    params.push(status);
  }

  if (search) {
    sql += ' AND (title LIKE ? OR case_number LIKE ? OR applicant_name LIKE ?)';
    countSql += ' AND (title LIKE ? OR case_number LIKE ? OR applicant_name LIKE ?)';
    const searchParam = `%${search}%`;
    params.push(searchParam, searchParam, searchParam);
  }

  sql += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
  params.push(parseInt(size), offset);

  // 获取总数
  db.get(countSql, params.slice(0, -2), (err, countResult) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    // 获取数据
    db.all(sql, params, (err, rows) => {
      if (err) {
        return res.status(500).json({
          code: 500,
          message: '数据库错误',
          error: err.message
        });
      }

      res.json({
        code: 200,
        message: '获取成功',
        data: {
          list: rows,
          total: countResult.total,
          page: parseInt(page),
          size: parseInt(size),
          pages: Math.ceil(countResult.total / size)
        }
      });
    });
  });
});

// 获取案件详情
app.get('/api/arbitration/cases/:id', authenticateToken, (req, res) => {
  const { id } = req.params;

  db.get('SELECT * FROM arbitration_cases WHERE id = ?', [id], (err, row) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    if (!row) {
      return res.status(404).json({
        code: 404,
        message: '案件不存在'
      });
    }

    res.json({
      code: 200,
      message: '获取成功',
      data: row
    });
  });
});

// 创建新案件
app.post('/api/arbitration/cases', authenticateToken, (req, res) => {
  const {
    title,
    description,
    applicant_name,
    applicant_contact,
    respondent_name,
    respondent_contact,
    amount = 0,
    category,
    priority = 'normal'
  } = req.body;

  if (!title || !applicant_name || !respondent_name) {
    return res.status(400).json({
      code: 400,
      message: '案件标题、申请人和被申请人不能为空'
    });
  }

  // 生成案件编号
  const case_number = 'ARB-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-6);

  const sql = `INSERT INTO arbitration_cases
    (case_number, title, description, applicant_name, applicant_contact,
     respondent_name, respondent_contact, amount, category, priority)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;

  db.run(sql, [
    case_number, title, description, applicant_name, applicant_contact,
    respondent_name, respondent_contact, amount, category, priority
  ], function(err) {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '创建案件失败',
        error: err.message
      });
    }

    res.json({
      code: 200,
      message: '案件创建成功',
      data: {
        id: this.lastID,
        case_number
      }
    });
  });
});

// 更新案件状态
app.put('/api/arbitration/cases/:id/status', authenticateToken, (req, res) => {
  const { id } = req.params;
  const { status, resolution } = req.body;

  if (!status) {
    return res.status(400).json({
      code: 400,
      message: '状态不能为空'
    });
  }

  const sql = `UPDATE arbitration_cases
               SET status = ?, resolution = ?, updated_at = CURRENT_TIMESTAMP
               WHERE id = ?`;

  db.run(sql, [status, resolution || null, id], function(err) {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '更新失败',
        error: err.message
      });
    }

    if (this.changes === 0) {
      return res.status(404).json({
        code: 404,
        message: '案件不存在'
      });
    }

    res.json({
      code: 200,
      message: '状态更新成功'
    });
  });
});

// 分配仲裁员
app.put('/api/arbitration/cases/:id/assign', authenticateToken, (req, res) => {
  const { id } = req.params;
  const { arbitrator, scheduled_date } = req.body;

  if (!arbitrator) {
    return res.status(400).json({
      code: 400,
      message: '仲裁员不能为空'
    });
  }

  const sql = `UPDATE arbitration_cases
               SET assigned_arbitrator = ?, scheduled_date = ?,
                   status = 'assigned', updated_at = CURRENT_TIMESTAMP
               WHERE id = ?`;

  db.run(sql, [arbitrator, scheduled_date, id], function(err) {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '分配失败',
        error: err.message
      });
    }

    if (this.changes === 0) {
      return res.status(404).json({
        code: 404,
        message: '案件不存在'
      });
    }

    res.json({
      code: 200,
      message: '仲裁员分配成功'
    });
  });
});

// ==================== 仲裁员管理API ====================

// 获取仲裁员列表
app.get('/api/arbitration/arbitrators', authenticateToken, (req, res) => {
  db.all('SELECT * FROM arbitrators ORDER BY created_at DESC', (err, rows) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    res.json({
      code: 200,
      message: '获取成功',
      data: rows
    });
  });
});

// 创建仲裁员
app.post('/api/arbitration/arbitrators', authenticateToken, (req, res) => {
  const { name, qualification, specialization, contact_info } = req.body;

  if (!name) {
    return res.status(400).json({
      code: 400,
      message: '仲裁员姓名不能为空'
    });
  }

  const sql = `INSERT INTO arbitrators (name, qualification, specialization, contact_info)
               VALUES (?, ?, ?, ?)`;

  db.run(sql, [name, qualification, specialization, contact_info], function(err) {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '创建仲裁员失败',
        error: err.message
      });
    }

    res.json({
      code: 200,
      message: '仲裁员创建成功',
      data: {
        id: this.lastID
      }
    });
  });
});

// ==================== 统计API ====================

// 获取统计数据
app.get('/api/arbitration/statistics', authenticateToken, (req, res) => {
  const stats = {};

  // 总案件数
  db.get('SELECT COUNT(*) as total FROM arbitration_cases', (err, result) => {
    if (err) {
      return res.status(500).json({
        code: 500,
        message: '数据库错误',
        error: err.message
      });
    }

    stats.totalCases = result.total;

    // 按状态统计
    db.all(`SELECT status, COUNT(*) as count
            FROM arbitration_cases
            GROUP BY status`, (err, statusData) => {
      if (err) {
        return res.status(500).json({
          code: 500,
          message: '数据库错误',
          error: err.message
        });
      }

      stats.statusStats = statusData;

      // 本月新增案件
      db.get(`SELECT COUNT(*) as thisMonth
              FROM arbitration_cases
              WHERE date(created_at) >= date('now', 'start of month')`, (err, monthData) => {
        if (err) {
          return res.status(500).json({
            code: 500,
            message: '数据库错误',
            error: err.message
          });
        }

        stats.thisMonthCases = monthData.thisMonth;

        // 仲裁员数量
        db.get('SELECT COUNT(*) as total FROM arbitrators', (err, arbData) => {
          if (err) {
            return res.status(500).json({
              code: 500,
              message: '数据库错误',
              error: err.message
            });
          }

          stats.totalArbitrators = arbData.total;

          res.json({
            code: 200,
            message: '获取成功',
            data: stats
          });
        });
      });
    });
  });
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    code: 200,
    message: 'Arbitration Backend Service is running',
    timestamp: new Date().toISOString()
  });
});

// 404处理
app.use('*', (req, res) => {
  res.status(404).json({
    code: 404,
    message: 'API接口不存在'
  });
});

// 错误处理中间件
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    code: 500,
    message: '服务器内部错误',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`🚀 仲裁系统后端服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🏥 健康检查: http://localhost:${PORT}/health`);
  console.log(`📚 默认管理员账号: admin / admin123`);
  console.log(`⏰ 启动时间: ${new Date().toLocaleString()}`);
});

module.exports = app;