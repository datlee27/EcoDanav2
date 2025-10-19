// Owner Dashboard JavaScript Functions

// Global variables
let currentTab = 'overview';
let refreshInterval;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log('Owner Dashboard initialized');
    initializeCharts();
    setupEventListeners();
    setupSidebar();
    setupTopbarDropdowns(); // <-- THÊM MỚI: Kích hoạt logic dropdown cho topbar
    startAutoRefresh();

    // Set initial tab based on URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab');
    if (tab) {
        switchTab(tab);
    } else {
        // If no tab parameter, ensure dashboard stats are visible
        const dashboardStats = document.getElementById('dashboard-stats');
        const dashboardStats2 = document.getElementById('dashboard-stats-2');
        if (dashboardStats && dashboardStats2) {
            dashboardStats.style.display = 'grid';
            dashboardStats2.style.display = 'grid';
        }
    }

    // Simple user rows protection - Run once after page load
    setTimeout(() => {
        console.log('Running user rows protection...');
        forceShowAllRows();
    }, 1000);
});

// Sidebar functionality
function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebarClose = document.getElementById('sidebar-close');
    const sidebarOverlay = document.getElementById('sidebar-overlay');

    // Toggle sidebar on mobile
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            openSidebar();
        });
    }

    // Close sidebar
    if (sidebarClose) {
        sidebarClose.addEventListener('click', function() {
            closeSidebar();
        });
    }

    // Close sidebar when clicking overlay
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', function() {
            closeSidebar();
        });
    }

    // Close sidebar on escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeSidebar();
        }
    });

    // Handle window resize
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 1024) {
            closeSidebar();
        }
    });
}

function openSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebar-overlay');

    if (sidebar && sidebarOverlay) {
        sidebar.classList.add('sidebar-open');
        sidebarOverlay.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }
}

function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebar-overlay');

    if (sidebar && sidebarOverlay) {
        sidebar.classList.remove('sidebar-open');
        sidebarOverlay.classList.add('hidden');
        document.body.style.overflow = '';
    }
}

// *** THÊM MỚI: LOGIC CHO TOPBAR DROPDOWNS ***
function setupTopbarDropdowns() {
    const userMenuButton = document.getElementById('userMenuButton');
    const userMenuDropdown = document.getElementById('userMenuDropdown');
    const notificationBell = document.getElementById('notificationBell');
    const notificationDropdown = document.getElementById('notificationDropdown');

    // Hàm để bật/tắt dropdown và ẩn cái còn lại
    const toggleDropdown = (dropdownToToggle, otherDropdown) => {
        if (dropdownToToggle) {
            dropdownToToggle.classList.toggle('hidden');
        }
        if (otherDropdown) {
            otherDropdown.classList.add('hidden'); // Luôn ẩn cái còn lại
        }
    };

    // Click vào nút User Menu
    if (userMenuButton && userMenuDropdown) {
        userMenuButton.addEventListener('click', (event) => {
            event.stopPropagation(); // Ngăn click lan ra ngoài
            toggleDropdown(userMenuDropdown, notificationDropdown);
        });
    }

    // Click vào nút Thông báo
    if (notificationBell && notificationDropdown) {
        notificationBell.addEventListener('click', (event) => {
            event.stopPropagation(); // Ngăn click lan ra ngoài
            toggleDropdown(notificationDropdown, userMenuDropdown);
        });
    }

    // Click ra ngoài để đóng tất cả dropdown
    document.addEventListener('click', (event) => {
        // Đóng User Menu nếu click ra ngoài
        if (userMenuButton && userMenuDropdown && !userMenuDropdown.classList.contains('hidden')) {
            if (!userMenuDropdown.contains(event.target) && !userMenuButton.contains(event.target)) {
                userMenuDropdown.classList.add('hidden');
            }
        }

        // Đóng Notification Menu nếu click ra ngoài
        if (notificationBell && notificationDropdown && !notificationDropdown.classList.contains('hidden')) {
            if (!notificationDropdown.contains(event.target) && !notificationBell.contains(event.target)) {
                notificationDropdown.classList.add('hidden');
            }
        }
    });
}
// *** KẾT THÚC PHẦN THÊM MỚI ***

// Tab switching functionality
function switchTab(tabName, event) {
    console.log('Switching to tab:', tabName);

    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    console.log('Found', tabContents.length, 'tab contents');
    tabContents.forEach(content => {
        content.classList.remove('active');
        // Ghi đè CSS từ bookings-management.html
        if (!content.classList.contains('hidden')) {
            content.style.display = 'none';
        }
    });

    // Remove active class from all sidebar items
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    sidebarItems.forEach(item => {
        item.classList.remove('active');
    });

    // Show selected tab content
    const targetTab = document.getElementById(tabName);
    console.log('Target tab element:', targetTab);
    if (targetTab) {
        targetTab.classList.add('active');
        // Ghi đè CSS từ bookings-management.html
        targetTab.style.display = 'block';
    } else {
        // Thử tìm với hậu tố '-section' (dùng trong bookings-management)
        const targetTabSection = document.getElementById(tabName + '-section');
        if (targetTabSection) {
            targetTabSection.classList.add('active');
            targetTabSection.style.display = 'block';
        } else {
            console.error('Tab', tabName, 'not found!');
        }
    }

    // Add active class to clicked sidebar item (if called from click event)
    if (event && event.target && event.target.closest) {
        const clickedButton = event.target.closest('.sidebar-item');
        if (clickedButton) {
            clickedButton.classList.add('active');
        }
    } else {
        // If no event, find the sidebar item by data attribute and activate it
        const sidebarItem = document.querySelector(`[data-tab="${tabName}"]`);
        if (sidebarItem) {
            sidebarItem.classList.add('active');
        }
    }

    currentTab = tabName;

    // Show/hide dashboard statistics cards
    const dashboardStats = document.getElementById('dashboard-stats');
    const dashboardStats2 = document.getElementById('dashboard-stats-2');
    if (dashboardStats && dashboardStats2) {
        if (tabName === 'overview' || tabName === 'dashboard') { // Thêm 'dashboard'
            dashboardStats.style.display = 'grid';
            dashboardStats2.style.display = 'grid';
        } else {
            dashboardStats.style.display = 'none';
            dashboardStats2.style.display = 'none';
        }
    }

    // Close sidebar on mobile after selection
    if (window.innerWidth < 1024) {
        closeSidebar();
    }

    // Update URL without page reload
    try {
        const url = new URL(window.location);
        url.searchParams.set('tab', tabName);
        window.history.pushState({}, '', url);
    } catch (e) {
        console.warn("Could not update URL history.");
    }
}

// Setup event listeners
function setupEventListeners() {
    // Search functionality - COMPLETELY DISABLED
    const searchInputs = document.querySelectorAll('input[placeholder*="Search"]');
    searchInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            console.log('Search input changed but functionality is disabled');
            // Do nothing - completely disabled
        });
    });

    // Filter functionality - COMPLETELY DISABLED
    const filterSelects = document.querySelectorAll('select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function(e) {
            console.log('Filter changed but functionality is disabled');
            // Do nothing - completely disabled
        });
    });

    // Refresh button
    const refreshButton = document.querySelector('[data-refresh]');
    if (refreshButton) {
        refreshButton.addEventListener('click', refreshData);
    }

    // Reset all rows to visible on page load
    setTimeout(() => {
        resetAllRows();
        forceShowAllRows(); // Additional force show
    }, 1000);

    // Force show all rows immediately
    forceShowAllRows();

    // *** ADMIN-SPECIFIC [data-action] LISTENER REMOVED TO PREVENT CONFLICTS ***
    // Các trang Owner sử dụng onclick="" trực tiếp trong HTML (ví dụ: viewBooking(), approveBooking())
    // nên chúng ta xóa trình lắng nghe sự kiện chung của Admin đi.
}

// Search functionality - TEMPORARILY DISABLED FOR DEBUGGING
function handleSearch(event) {
    console.log('Search called with term:', event.target.value);
    console.log('Search functionality temporarily disabled for debugging');

    // TEMPORARILY DISABLED - Just show all rows
    const table = event.target.closest('.bg-white').querySelector('table');
    const rows = table.querySelectorAll('tbody tr');

    console.log('Found', rows.length, 'rows in table');

    rows.forEach((row, index) => {
        row.style.display = '';
        console.log('Row', index, 'forced to visible');
    });
}

// Filter functionality
function handleFilter(event) {
    console.log('Filter called with value:', event.target.value);
    console.log('Filter functionality temporarily disabled for debugging');

    // TEMPORARILY DISABLED - Just show all rows
    const table = event.target.closest('.bg-white')?.querySelector('table');
    if (!table) {
        console.log('Table not found for filter');
        return;
    }
    const rows = table.querySelectorAll('tbody tr');

    rows.forEach(row => {
        row.style.display = '';
        console.log('Row forced to visible by filter');
    });
}

// Force show all rows - simple approach
function forceShowAllRows() {
    console.log('Force showing all rows...');

    // Find all tables in the page
    const tables = document.querySelectorAll('table');
    tables.forEach((table, tableIndex) => {
        const rows = table.querySelectorAll('tbody tr');
        console.log('Force show - Table', tableIndex, 'has', rows.length, 'rows');

        rows.forEach((row, rowIndex) => {
            // Skip debug rows
            if (row.textContent.includes('DEBUG:')) {
                console.log('Force show - Skipping debug row', rowIndex);
                return;
            }

            // Simple force show
            row.style.display = '';
            row.style.visibility = 'visible';
            row.style.opacity = '1';
            row.classList.remove('hidden', 'd-none');
            row.removeAttribute('hidden');
            // console.log('Force show - Row', rowIndex, 'forced to visible'); // Tắt log này để đỡ rối
        });
    });
}

// Reset all rows to visible
function resetAllRows() {
    console.log('Resetting all rows to visible...');

    // Find all tables in the page
    const tables = document.querySelectorAll('table');
    tables.forEach((table, tableIndex) => {
        const rows = table.querySelectorAll('tbody tr');
        console.log('Table', tableIndex, 'has', rows.length, 'rows');

        rows.forEach((row, rowIndex) => {
            // Skip debug rows
            if (row.textContent.includes('DEBUG:')) {
                console.log('Skipping debug row', rowIndex);
                return;
            }

            row.style.display = '';
            console.log('Row', rowIndex, 'reset to visible');
        });
    });
}

// Debounce function
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

// API Helper Functions
async function makeApiCall(url, method = 'GET', data = null) {
    try {
        console.log('Making API call to:', url, 'with method:', method);
        console.log('Data:', data);

        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            credentials: 'same-origin' // Include cookies for session
        };

        // Add CSRF token if available
        if (csrfToken && csrfHeader) {
            options.headers[csrfHeader] = csrfToken;
            console.log('CSRF token added:', csrfToken);
        } else {
            console.log('CSRF token not found');
        }

        if (data) {
            options.body = new URLSearchParams(data);
            console.log('Request body:', options.body.toString());
        }

        const response = await fetch(url, options);
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);

        if (!response.ok) {
            // Try to get error message from response
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorData = await response.json();
                console.log('Error response data:', errorData);
                if (errorData.error) {
                    errorMessage = errorData.error;
                }
            } catch (e) {
                console.log('Could not parse error response as JSON');
            }

            if (response.status === 403) {
                throw new Error('Bạn không có quyền thực hiện hành động này. Vui lòng đăng nhập lại.');
            } else if (response.status === 401) {
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            } else {
                throw new Error(errorMessage);
            }
        }

        const result = await response.json();
        console.log('Response data:', result);
        return result;
    } catch (error) {
        console.error('API Error:', error);
        showNotification('Lỗi: ' + error.message, 'error');
        throw error;
    }
}

// Notification system
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 p-4 rounded-lg shadow-lg z-50 ${
        type === 'error' ? 'bg-red-500 text-white' :
            type === 'success' ? 'bg-green-500 text-white' :
                'bg-blue-500 text-white'
    }`;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 5000);
}

// *** ADMIN-SPECIFIC FUNCTIONS REMOVED ***
// (suspendUser, activateUser, updateVehicleStatus, updateBookingStatus,
// updateContractStatus, refundPayment, discount, insurance, notification functions
// have all been removed.)


// Chart initialization
function initializeCharts() {
    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart');
    if (revenueCtx) {
        new Chart(revenueCtx.getContext('2d'), {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Revenue (VND)',
                    data: [12000000, 19000000, 15000000, 25000000, 22000000, 30000000],
                    borderColor: 'rgb(59, 130, 246)',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                aspectRatio: 2,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return (value / 1000000).toFixed(0) + 'M VND';
                            },
                            font: {
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            font: {
                                size: 12
                            }
                        }
                    }
                }
            }
        });
    }

    // Booking Trends Chart
    const bookingTrendsCtx = document.getElementById('bookingTrendsChart');
    if (bookingTrendsCtx) {
        new Chart(bookingTrendsCtx.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Bookings',
                    data: [12, 19, 15, 25, 22, 30, 18],
                    backgroundColor: 'rgba(34, 197, 94, 0.8)',
                    borderColor: 'rgb(34, 197, 94)',
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                aspectRatio: 2,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            font: {
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            font: {
                                size: 12
                            }
                        }
                    }
                }
            }
        });
    }

    // Vehicle Status Chart
    const vehicleStatusCtx = document.getElementById('vehicleStatusChart');
    if (vehicleStatusCtx) {
        new Chart(vehicleStatusCtx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Available', 'In Use', 'Maintenance'],
                datasets: [{
                    data: [15, 8, 2],
                    backgroundColor: [
                        'rgba(34, 197, 94, 0.8)',
                        'rgba(59, 130, 246, 0.8)',
                        'rgba(239, 68, 68, 0.8)'
                    ],
                    borderColor: [
                        'rgb(34, 197, 94)',
                        'rgb(59, 130, 246)',
                        'rgb(239, 68, 68)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                aspectRatio: 1.5,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom',
                        labels: {
                            font: {
                                size: 11
                            },
                            padding: 15
                        }
                    }
                }
            }
        });
    }
}

// Auto-refresh functionality
function startAutoRefresh() {
    // Tạm thời tắt auto-refresh để gỡ lỗi
    // refreshInterval = setInterval(refreshData, 30000); // Refresh every 30 seconds
    console.log("Auto-refresh disabled for debugging.");
}

function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
}

function refreshData() {
    // API endpoint for owner analytics (assumed)
    fetch('/owner/api/analytics') // Changed from /admin/api/analytics
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text(); // Get as text first
        })
        .then(text => {
            try {
                const data = JSON.parse(text);
                console.log('Updated owner analytics:', data);
                updateDashboardData(data);
            } catch (parseError) {
                console.error('Error parsing owner analytics JSON:', parseError);
                console.log('Response text:', text.substring(0, 200) + '...');
            }
        })
        .catch(error => {
            console.error('Error updating owner analytics:', error);
        });
}

function updateDashboardData(data) {
    // Update statistics cards
    const totalUsers = document.querySelector('[data-stat="users"]');
    const totalVehicles = document.querySelector('[data-stat="vehicles"]');
    const totalBookings = document.querySelector('[data-stat="bookings"]');
    const totalRevenue = document.querySelector('[data-stat="revenue"]');

    // These may or may not exist on the owner page, check before setting
    if (totalUsers && data.totalUsers) totalUsers.textContent = data.totalUsers;
    if (totalVehicles && data.totalVehicles) totalVehicles.textContent = data.totalVehicles;
    if (totalBookings && data.totalBookings) totalBookings.textContent = data.totalBookings;
    if (totalRevenue && data.totalRevenue) totalRevenue.textContent = data.totalRevenue;
}

// Logout functionality
function logout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        // Show loading state
        showNotification('Đang đăng xuất...', 'info');

        // Create a form to submit logout request
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';

        // Add CSRF token if available
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken.getAttribute('content');
            form.appendChild(csrfInput);
        }

        document.body.appendChild(form);
        form.submit();
    }
}


// Test function
function testModal() {
    console.log('Test function called');
    alert('JavaScript đang hoạt động!');

    // Test simple modal first
    const testModal = document.getElementById('testModal');
    if (testModal) {
        console.log('Test modal found, showing...');
        testModal.classList.remove('hidden');
        testModal.style.display = 'flex';
    } else {
        console.error('Test modal not found!');
    }
}

function closeTestModal() {
    const testModal = document.getElementById('testModal');
    if (testModal) {
        testModal.classList.add('hidden');
        testModal.style.display = 'none';
    }
}

// Export functions for global access
// (Chúng ta không export mọi thứ, chỉ những hàm cần gọi từ HTML (ví dụ: onclick))
// Các hàm như setupSidebar, initializeCharts tự chạy, không cần export.
window.switchTab = switchTab;
window.openSidebar = openSidebar;
window.closeSidebar = closeSidebar;
window.logout = logout;
window.testModal = testModal;
window.closeTestModal = closeTestModal;
window.resetAllRows = resetAllRows;
window.forceShowAllRows = forceShowAllRows;

// *** ADMIN-SPECIFIC FUNCTIONS REMOVED FROM EXPORTS ***
// (suspendUser, activateUser, updateVehicleStatus, updateBookingStatus,
// updateContractStatus, refundPayment, discount, insurance, notification functions
// have all been removed.)