// 管理员后台公共JS文件
// 注意：由于Spring Boot的context-path是/api，API调用需要包含/api前缀
const API_BASE_URL = '/api';
let authToken = localStorage.getItem('admin_token');

// 设置请求头
function getHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    return headers;
}

// 通用请求函数
async function apiRequest(url, options = {}) {
    try {
        const response = await fetch(API_BASE_URL + url, {
            ...options,
            headers: {
                ...getHeaders(),
                ...options.headers
            }
        });

        const data = await response.json();
        
        if (response.status === 401) {
            // 未授权，跳转到登录页
            localStorage.removeItem('admin_token');
            window.location.href = '/api/static/admin/login.html';
            return null;
        }

        if (data.code === 200) {
            return data.data;
        } else {
            throw new Error(data.message || '请求失败');
        }
    } catch (error) {
        console.error('API请求错误:', error);
        throw error;
    }
}

// 登录
async function login(username, password) {
    const response = await fetch(API_BASE_URL + '/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    });

    const data = await response.json();
    
    if (data.code === 200 && data.data.token) {
        authToken = data.data.token;
        localStorage.setItem('admin_token', authToken);
        return true;
    } else {
        throw new Error(data.message || '登录失败');
    }
}

// 登出
function logout() {
    localStorage.removeItem('admin_token');
    authToken = null;
    window.location.href = '/api/static/admin/login.html';
}

// 检查登录状态
function checkAuth() {
    if (!authToken && !window.location.pathname.includes('login.html')) {
        window.location.href = '/api/static/admin/login.html';
    }
}

// 数据统计API
const StatsAPI = {
    getOverview: () => apiRequest('/admin/stats/overview'),
    getUserGrowth: (startDate, endDate) => {
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        return apiRequest(`/admin/stats/user-growth?${params}`);
    },
    getContentTrend: (startDate, endDate) => {
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        return apiRequest(`/admin/stats/content-trend?${params}`);
    },
    getActiveUsers: (days = 7) => apiRequest(`/admin/stats/active-users?days=${days}`),
    getHotContents: (limit = 10) => apiRequest(`/admin/stats/hot-contents?limit=${limit}`)
};

// 用户管理API
const UserAPI = {
    getAllUsers: (page = 1, size = 20, keyword = '', status = null) => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });
        if (keyword) params.append('keyword', keyword);
        if (status !== null) params.append('status', status.toString());
        return apiRequest(`/admin/users?${params}`);
    },
    getUserDetail: (id) => apiRequest(`/admin/users/${id}`),
    deleteUser: (id) => apiRequest(`/admin/users/${id}`, { method: 'DELETE' }),
    disableUser: (id) => apiRequest(`/admin/users/${id}/disable`, { method: 'PUT' }),
    enableUser: (id) => apiRequest(`/admin/users/${id}/enable`, { method: 'PUT' })
};

// 内容管理API
const ContentAPI = {
    getAllContents: (page = 1, size = 20, userId = null, type = null, status = null) => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });
        if (userId) params.append('userId', userId.toString());
        if (type !== null) params.append('type', type.toString());
        if (status !== null) params.append('status', status.toString());
        return apiRequest(`/admin/contents?${params}`);
    },
    deleteContent: (id, reason = '') => {
        const params = new URLSearchParams();
        if (reason) params.append('reason', reason);
        return apiRequest(`/admin/contents/${id}?${params}`, { method: 'DELETE' });
    },
    markAsViolation: (id, reason) => {
        const params = new URLSearchParams({ reason });
        return apiRequest(`/admin/contents/${id}/violation?${params}`, { method: 'PUT' });
    }
};

// 工具函数
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
}

function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    return num.toLocaleString('zh-CN');
}

function showToast(message, type = 'success') {
    // 简单的toast提示
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }
    
    container.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

// 页面加载时检查认证
if (typeof window !== 'undefined') {
    window.addEventListener('DOMContentLoaded', () => {
        if (!window.location.pathname.includes('login.html')) {
            checkAuth();
        }
    });
}

