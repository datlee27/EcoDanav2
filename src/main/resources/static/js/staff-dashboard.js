// Staff Dashboard JavaScript
// Modern & Interactive Features

// ==================== GLOBAL VARIABLES ====================
let refreshInterval;
const REFRESH_TIME = 30000; // 30 seconds

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Staff Dashboard Initialized');
    
    // Initialize features
    initAutoRefresh();
    initNotifications();
    initSearchFilters();
    initKeyboardShortcuts();
    initAnimations();
    
    // Show welcome message
    showWelcomeMessage();
});

// ==================== AUTO REFRESH ====================
function initAutoRefresh() {
    // Auto refresh every 30 seconds
    refreshInterval = setInterval(() => {
        console.log('🔄 Auto refreshing dashboard...');
        location.reload();
    }, REFRESH_TIME);
    
    // Show countdown
    updateRefreshCountdown();
}

function updateRefreshCountdown() {
    let seconds = REFRESH_TIME / 1000;
    setInterval(() => {
        seconds--;
        if (seconds <= 0) seconds = REFRESH_TIME / 1000;
        
        const countdownEl = document.getElementById('refresh-countdown');
        if (countdownEl) {
            countdownEl.textContent = `Tự động làm mới sau ${seconds}s`;
        }
    }, 1000);
}

// ==================== NOTIFICATIONS ====================
function initNotifications() {
    // Request notification permission
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}

function showNotification(title, message, type = 'info') {
    // Browser notification
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(title, {
            body: message,
            icon: '/images/logo.png',
            badge: '/images/badge.png'
        });
    }
    
    // In-app notification
    showToast(message, type);
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <i class="fas fa-${getToastIcon(type)}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function getToastIcon(type) {
    const icons = {
        'success': 'check-circle',
        'error': 'exclamation-circle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    return icons[type] || 'info-circle';
}

// ==================== BOOKING ACTIONS ====================
function approveBooking(bookingId) {
    if (confirm('Bạn có chắc chắn muốn duyệt đơn này?')) {
        showLoading();
        
        // Simulate API call
        setTimeout(() => {
            hideLoading();
            showNotification('Thành công', 'Đơn đặt xe đã được duyệt!', 'success');
            window.location.href = `/staff/bookings/${bookingId}/approve`;
        }, 500);
    }
}

function rejectBooking(bookingId) {
    const reason = prompt('Nhập lý do từ chối:');
    if (reason && reason.trim()) {
        showLoading();
        
        setTimeout(() => {
            hideLoading();
            showNotification('Đã từ chối', 'Đơn đặt xe đã bị từ chối', 'warning');
            window.location.href = `/staff/bookings/${bookingId}/reject?reason=${encodeURIComponent(reason)}`;
        }, 500);
    }
}

function viewBookingDetails(bookingId) {
    window.location.href = `/staff/bookings/${bookingId}`;
}

// ==================== SEARCH & FILTERS ====================
function initSearchFilters() {
    const searchInput = document.getElementById('booking-search');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
}

function handleSearch(e) {
    const searchTerm = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('.data-table tbody tr');
    
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// ==================== KEYBOARD SHORTCUTS ====================
function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Ctrl/Cmd + R: Refresh
        if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
            e.preventDefault();
            location.reload();
        }
        
        // Ctrl/Cmd + F: Focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            const searchInput = document.getElementById('booking-search');
            if (searchInput) searchInput.focus();
        }
        
        // Escape: Clear search
        if (e.key === 'Escape') {
            const searchInput = document.getElementById('booking-search');
            if (searchInput) {
                searchInput.value = '';
                searchInput.dispatchEvent(new Event('input'));
            }
        }
    });
}

// ==================== ANIMATIONS ====================
function initAnimations() {
    // Animate stats cards on scroll
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
    }, { threshold: 0.1 });
    
    document.querySelectorAll('.stat-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';
        observer.observe(card);
    });
}

// ==================== LOADING INDICATOR ====================
function showLoading() {
    const loader = document.createElement('div');
    loader.id = 'global-loader';
    loader.innerHTML = `
        <div class="loader-backdrop">
            <div class="loader-spinner">
                <div class="loading"></div>
                <p>Đang xử lý...</p>
            </div>
        </div>
    `;
    document.body.appendChild(loader);
}

function hideLoading() {
    const loader = document.getElementById('global-loader');
    if (loader) loader.remove();
}

// ==================== WELCOME MESSAGE ====================
function showWelcomeMessage() {
    const hour = new Date().getHours();
    let greeting;
    
    if (hour < 12) greeting = 'Chào buổi sáng';
    else if (hour < 18) greeting = 'Chào buổi chiều';
    else greeting = 'Chào buổi tối';
    
    console.log(`👋 ${greeting}! Welcome to Staff Dashboard`);
}

// ==================== UTILITY FUNCTIONS ====================
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(date) {
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(date));
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('Đã sao chép vào clipboard!', 'success');
    });
}

// ==================== EXPORT FUNCTIONS ====================
function exportToExcel() {
    showToast('Đang xuất dữ liệu...', 'info');
    // Implementation here
}

function exportToPDF() {
    showToast('Đang tạo PDF...', 'info');
    // Implementation here
}

// ==================== PRINT ====================
function printDashboard() {
    window.print();
}

// ==================== STATS UPDATE ====================
function updateStats() {
    fetch('/api/staff/stats')
        .then(response => response.json())
        .then(data => {
            // Update stat cards
            document.querySelector('[data-stat="pending"]').textContent = data.pending;
            document.querySelector('[data-stat="approved"]').textContent = data.approved;
            document.querySelector('[data-stat="ongoing"]').textContent = data.ongoing;
            document.querySelector('[data-stat="today"]').textContent = data.today;
        })
        .catch(error => {
            console.error('Error updating stats:', error);
        });
}

// ==================== CLEANUP ====================
window.addEventListener('beforeunload', () => {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
});

// ==================== GLOBAL EXPORTS ====================
window.staffDashboard = {
    approveBooking,
    rejectBooking,
    viewBookingDetails,
    showNotification,
    showToast,
    showLoading,
    hideLoading,
    exportToExcel,
    exportToPDF,
    printDashboard,
    updateStats
};

console.log('✅ Staff Dashboard JS Loaded');
