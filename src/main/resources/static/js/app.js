/**
 * 网站访问量统计分析系统 - 公共工具函数
 */
const App = {
    async request(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                credentials: 'same-origin',
                ...options
            });
            if (response.status === 401) {
                window.location.href = '/login.html';
                return null;
            }
            return await response.json();
        } catch (e) {
            console.error('请求失败:', e);
            return { code: 500, msg: '网络请求失败' };
        }
    },
    async get(url) { return this.request(url); },
    async post(url, data = {}) { return this.request(url, { method: 'POST', body: new URLSearchParams(data).toString() }); },
    async del(url) { return this.request(url, { method: 'DELETE' }); },
    showToast(msg, type = 'info') {
        const toast = document.createElement('div');
        const colors = { success: '#d4edda', error: '#f8d7da', info: '#cce5ff' };
        const textColors = { success: '#155724', error: '#721c24', info: '#004085' };
        toast.style.cssText = `position:fixed;top:70px;left:50%;transform:translateX(-50%);padding:12px 24px;border-radius:6px;z-index:9999;font-size:14px;box-shadow:0 4px 12px rgba(0,0,0,0.15);background:${colors[type]||colors.info};color:${textColors[type]||textColors.info};transition:opacity 0.3s`;
        toast.textContent = msg;
        document.body.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 3000);
    },
    formatTime(t) { return t ? t.replace('T', ' ').substring(0, 19) : '-'; },
    formatSize(b) { return b < 1024 ? b+' B' : b < 1048576 ? (b/1024).toFixed(1)+' KB' : (b/1048576).toFixed(1)+' MB'; },
    taskTypeName(t) { return { RANK: '访问量排行', TIME_PEAK: '时间峰值', IP_DIST: 'IP分布', REGION: '地区分布', ALL: '综合分析' }[t] || t; },
    statusInfo(s) { return { PENDING: {text:'待处理',class:'status-pending'}, RUNNING: {text:'运行中',class:'status-running'}, FINISHED: {text:'已完成',class:'status-finished'}, FAILED: {text:'失败',class:'status-failed'} }[s] || {text:s,class:''}; }
};
