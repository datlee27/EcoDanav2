// Admin Dashboard JavaScript Functions

// Global variables
let currentTab = 'overview';
let refreshInterval;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard initialized');
    initializeCharts();
    setupEventListeners();
    setupSidebar();
    startAutoRefresh();
    
    // Set initial tab based on URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab');
    if (tab) {
        switchTab(tab);
    }
    
    // Test Add User button
    const addUserBtn = document.getElementById('addUserBtn');
    if (addUserBtn) {
        console.log('Add User button found');
        addUserBtn.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Add User button clicked via event listener');
            showAddUserModal();
        });
    } else {
        console.error('Add User button not found!');
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
    
    // Close modal when clicking outside
    document.addEventListener('click', function(event) {
        const modal = document.getElementById('addUserModal');
        if (modal && event.target === modal) {
            closeAddUserModal();
        }
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeAddUserModal();
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

// Tab switching functionality
function switchTab(tabName, event) {
    console.log('Switching to tab:', tabName);
    
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });

    // Remove active class from all sidebar items
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    sidebarItems.forEach(item => {
        item.classList.remove('active');
    });

    // Show selected tab content
    const targetTab = document.getElementById(tabName);
    if (targetTab) {
        targetTab.classList.add('active');
        console.log('Tab', tabName, 'is now active');
    } else {
        console.error('Tab', tabName, 'not found!');
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
    
    // Close sidebar on mobile after selection
    if (window.innerWidth < 1024) {
        closeSidebar();
    }
    
    // Update URL without page reload
    const url = new URL(window.location);
    url.searchParams.set('tab', tabName);
    window.history.pushState({}, '', url);
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

    // Action buttons with data attributes
    document.addEventListener('click', function(event) {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const action = button.getAttribute('data-action');
        
        switch(action) {
            case 'suspend':
                const suspendUserId = button.getAttribute('data-user-id');
                suspendUser(suspendUserId);
                break;
            case 'activate':
                const activateUserId = button.getAttribute('data-user-id');
                activateUser(activateUserId);
                break;
            case 'vehicle-status':
                const vehicleId = button.getAttribute('data-vehicle-id');
                const vehicleStatus = button.getAttribute('data-status');
                updateVehicleStatus(vehicleId, vehicleStatus);
                break;
            case 'booking-status':
                const bookingId = button.getAttribute('data-booking-id');
                const bookingStatus = button.getAttribute('data-status');
                updateBookingStatus(bookingId, bookingStatus);
                break;
            case 'contract-status':
                const contractId = button.getAttribute('data-contract-id');
                const contractStatus = button.getAttribute('data-status');
                updateContractStatus(contractId, contractStatus);
                break;
            case 'refund':
                const paymentId = button.getAttribute('data-payment-id');
                refundPayment(paymentId);
                break;
            case 'edit-discount':
                const editDiscountId = button.getAttribute('data-discount-id');
                editDiscount(editDiscountId);
                break;
            case 'delete-discount':
                const deleteDiscountId = button.getAttribute('data-discount-id');
                deleteDiscount(deleteDiscountId);
                break;
            case 'edit-insurance':
                const editInsuranceId = button.getAttribute('data-insurance-id');
                editInsurance(editInsuranceId);
                break;
            case 'delete-insurance':
                const deleteInsuranceId = button.getAttribute('data-insurance-id');
                deleteInsurance(deleteInsuranceId);
                break;
            case 'delete-notification':
                const notificationId = button.getAttribute('data-notification-id');
                deleteNotification(notificationId);
                break;
        }
    });
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
            console.log('Force show - Row', rowIndex, 'forced to visible');
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

// User Management Functions
function suspendUser(userId) {
    if (confirm('Are you sure you want to suspend this user?')) {
        makeApiCall('/admin/api/users/suspend', 'POST', { userId })
            .then(data => {
                if (data.success) {
                    showNotification('User suspended successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to suspend user: ' + error.message, 'error');
            });
    }
}

function activateUser(userId) {
    if (confirm('Are you sure you want to activate this user?')) {
        makeApiCall('/admin/api/users/activate', 'POST', { userId })
            .then(data => {
                if (data.success) {
                    showNotification('User activated successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to activate user: ' + error.message, 'error');
            });
    }
}

// Vehicle Management Functions
function updateVehicleStatus(vehicleId, status) {
    if (confirm(`Are you sure you want to change this vehicle status to ${status}?`)) {
        makeApiCall('/admin/api/vehicles/status', 'POST', { vehicleId, status })
            .then(data => {
                if (data.success) {
                    showNotification('Vehicle status updated successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to update vehicle status: ' + error.message, 'error');
            });
    }
}

// Booking Management Functions
function updateBookingStatus(bookingId, status) {
    if (confirm(`Are you sure you want to change this booking status to ${status}?`)) {
        makeApiCall('/admin/api/bookings/status', 'POST', { bookingId, status })
            .then(data => {
                if (data.success) {
                    showNotification('Booking status updated successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to update booking status: ' + error.message, 'error');
            });
    }
}

// Contract Management Functions
function updateContractStatus(contractId, status) {
    if (confirm(`Are you sure you want to change this contract status to ${status}?`)) {
        makeApiCall('/admin/api/contracts/status', 'POST', { contractId, status })
            .then(data => {
                if (data.success) {
                    showNotification('Contract status updated successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to update contract status: ' + error.message, 'error');
            });
    }
}

// Payment Management Functions
function refundPayment(paymentId) {
    const reason = prompt('Please enter refund reason (optional):');
    if (confirm('Are you sure you want to refund this payment?')) {
        makeApiCall('/admin/api/payments/refund', 'POST', { paymentId, reason: reason || '' })
            .then(data => {
                if (data.success) {
                    showNotification('Payment refunded successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to refund payment: ' + error.message, 'error');
            });
    }
}

// Discount Management Functions
function showAddDiscountModal() {
    // TODO: Implement modal for adding discount
    showNotification('Add Discount feature coming soon', 'info');
}

function editDiscount(discountId) {
    // TODO: Implement modal for editing discount
    showNotification('Edit Discount feature coming soon', 'info');
}

function deleteDiscount(discountId) {
    if (confirm('Are you sure you want to delete this discount?')) {
        makeApiCall('/admin/api/discounts/delete', 'POST', { discountId })
            .then(data => {
                if (data.success) {
                    showNotification('Discount deleted successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to delete discount: ' + error.message, 'error');
            });
    }
}

// Insurance Management Functions
function showAddInsuranceModal() {
    // TODO: Implement modal for adding insurance
    showNotification('Add Insurance feature coming soon', 'info');
}

function editInsurance(insuranceId) {
    // TODO: Implement modal for editing insurance
    showNotification('Edit Insurance feature coming soon', 'info');
}

function deleteInsurance(insuranceId) {
    if (confirm('Are you sure you want to delete this insurance policy?')) {
        makeApiCall('/admin/api/insurance/delete', 'POST', { insuranceId })
            .then(data => {
                if (data.success) {
                    showNotification('Insurance deleted successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to delete insurance: ' + error.message, 'error');
            });
    }
}

// Notification Management Functions
function showSendNotificationModal() {
    // TODO: Implement modal for sending notification
    showNotification('Send Notification feature coming soon', 'info');
}

function deleteNotification(notificationId) {
    if (confirm('Are you sure you want to delete this notification?')) {
        makeApiCall('/admin/api/notifications/delete', 'POST', { notificationId })
            .then(data => {
                if (data.success) {
                    showNotification('Notification deleted successfully', 'success');
                    location.reload();
                }
            })
            .catch(error => {
                showNotification('Failed to delete notification: ' + error.message, 'error');
            });
    }
}

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
    refreshInterval = setInterval(refreshData, 30000); // Refresh every 30 seconds
}

function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
}

function refreshData() {
    fetch('/admin/api/analytics')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text(); // Get as text first
        })
        .then(text => {
            try {
                const data = JSON.parse(text);
                console.log('Updated analytics:', data);
                updateDashboardData(data);
            } catch (parseError) {
                console.error('Error parsing analytics JSON:', parseError);
                console.log('Response text:', text.substring(0, 200) + '...');
            }
        })
        .catch(error => {
            console.error('Error updating analytics:', error);
        });
}

function updateDashboardData(data) {
    // Update statistics cards
    const totalUsers = document.querySelector('[data-stat="users"]');
    const totalVehicles = document.querySelector('[data-stat="vehicles"]');
    const totalBookings = document.querySelector('[data-stat="bookings"]');
    const totalRevenue = document.querySelector('[data-stat="revenue"]');
    
    if (totalUsers && data.totalUsers) totalUsers.textContent = data.totalUsers;
    if (totalVehicles && data.totalVehicles) totalVehicles.textContent = data.totalVehicles;
    if (totalBookings && data.totalBookings) totalBookings.textContent = data.totalBookings;
    if (totalRevenue && data.totalRevenue) totalRevenue.textContent = data.totalRevenue;
}

// Add User Modal Functions
function showAddUserModal() {
    console.log('showAddUserModal called');
    
    // Debug: Check all elements with id containing 'addUser'
    const allElements = document.querySelectorAll('[id*="addUser"]');
    console.log('All elements with addUser in id:', allElements);
    
    // Debug: Check all divs with id
    const allDivsWithId = document.querySelectorAll('div[id]');
    console.log('All divs with id:', allDivsWithId);
    
    // Find the modal directly (it's now at the end of body)
    const modal = document.getElementById('addUserModal');
    console.log('Modal element:', modal);
    
    // Alternative search methods
    const modalByQuery = document.querySelector('#addUserModal');
    console.log('Modal by querySelector:', modalByQuery);
    
    const modalByClass = document.querySelector('.modal-hidden');
    console.log('Modal by class modal-hidden:', modalByClass);
    
    if (!modal) {
        console.error('Add User Modal not found!');
        console.log('Available elements in DOM:');
        console.log('Body children count:', document.body.children.length);
        console.log('Last 5 body children:', Array.from(document.body.children).slice(-5));
        
        // Try to create modal dynamically
        createModalDynamically();
        return;
    }
    
    try {
        // Remove hidden classes and add visible class
        modal.classList.remove('hidden', 'modal-hidden');
        modal.classList.add('modal-visible');
        modal.style.display = 'flex';
        modal.style.visibility = 'visible';
        modal.style.opacity = '1';
        modal.style.zIndex = '9999';
        document.body.style.overflow = 'hidden';
        
        // Focus on first input
        setTimeout(() => {
            const firstNameInput = document.getElementById('firstName');
            if (firstNameInput) {
                firstNameInput.focus();
            }
        }, 100);
        
        console.log('Modal shown successfully');
        console.log('Modal classes:', modal.className);
        console.log('Modal style display:', modal.style.display);
    } catch (error) {
        console.error('Error showing modal:', error);
        alert('Lỗi khi hiển thị modal: ' + error.message);
    }
}

// Create modal dynamically if not found
function createModalDynamically() {
    console.log('Creating modal dynamically...');
    
    const modal = document.createElement('div');
    modal.id = 'addUserModal';
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 z-50';
    modal.style.display = 'flex';
    modal.style.zIndex = '9999';
    
    modal.innerHTML = `
        <div class="flex items-center justify-center min-h-screen p-4">
            <div class="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
                <!-- Modal Header -->
                <div class="flex items-center justify-between p-6 border-b border-gray-200">
                    <h3 class="text-lg font-semibold text-gray-900">Thêm User Mới</h3>
                    <button onclick="closeAddUserModal()" class="text-gray-400 hover:text-gray-600 transition-colors">
                        <i class="fas fa-times text-xl"></i>
                    </button>
                </div>
                
                <!-- Modal Body -->
                <form id="addUserForm" class="p-6 space-y-4">
                    <!-- First Name -->
                    <div>
                        <label for="firstName" class="block text-sm font-medium text-gray-700 mb-1">Họ</label>
                        <input type="text" id="firstName" name="firstName" required
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Last Name -->
                    <div>
                        <label for="lastName" class="block text-sm font-medium text-gray-700 mb-1">Tên</label>
                        <input type="text" id="lastName" name="lastName" required
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Username -->
                    <div>
                        <label for="username" class="block text-sm font-medium text-gray-700 mb-1">Tên đăng nhập</label>
                        <input type="text" id="username" name="username" required
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Email -->
                    <div>
                        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                        <input type="email" id="email" name="email" required
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Phone Number -->
                    <div>
                        <label for="phoneNumber" class="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
                        <input type="tel" id="phoneNumber" name="phoneNumber" required
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Password -->
                    <div>
                        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Mật khẩu</label>
                        <input type="password" id="password" name="password" required minlength="6"
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Confirm Password -->
                    <div>
                        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">Xác nhận mật khẩu</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required minlength="6"
                               class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    </div>
                    
                    <!-- Role -->
                    <div>
                        <label for="role" class="block text-sm font-medium text-gray-700 mb-1">Vai trò</label>
                        <select id="role" name="role" required
                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                            <option value="">Chọn vai trò</option>
                            <option value="Customer">Customer</option>
                            <option value="Staff">Staff</option>
                            <option value="Owner">Owner</option>
                            <option value="Admin">Admin</option>
                        </select>
                    </div>
                    
                    <!-- Status -->
                    <div>
                        <label for="status" class="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
                        <select id="status" name="status" required
                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                            <option value="Active">Active</option>
                            <option value="Pending">Pending</option>
                            <option value="Inactive">Inactive</option>
                        </select>
                    </div>
                </form>
                
                <!-- Modal Footer -->
                <div class="flex items-center justify-end space-x-3 p-6 border-t border-gray-200">
                    <button onclick="closeAddUserModal()" 
                            class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors">
                        Hủy
                    </button>
                    <button onclick="submitAddUser()" 
                            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md transition-colors">
                        <i class="fas fa-plus mr-1"></i>
                        Thêm User
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    console.log('Modal created and added to DOM');
    
    // Focus on first input
    setTimeout(() => {
        const firstNameInput = document.getElementById('firstName');
        if (firstNameInput) {
            firstNameInput.focus();
        }
    }, 100);
}

function closeAddUserModal() {
    const modal = document.getElementById('addUserModal');
    if (modal) {
        // If modal was created dynamically, remove it completely
        if (modal.parentNode) {
            modal.parentNode.removeChild(modal);
            console.log('Dynamic modal removed from DOM');
        } else {
            // If modal is in HTML, hide it
            modal.classList.add('hidden', 'modal-hidden');
            modal.classList.remove('modal-visible');
            modal.style.display = 'none';
        }
        
        document.body.style.overflow = '';
        
        // Reset form
        const form = document.getElementById('addUserForm');
        if (form) {
            form.reset();
        }
    }
}

// Edit User Modal Functions
function showEditUserModal(userId) {
    console.log('Opening edit modal for user:', userId);
    console.log('User ID type:', typeof userId);
    console.log('User ID length:', userId ? userId.length : 'null');
    
    // Validate userId
    if (!userId || userId === 'undefined' || userId === 'null') {
        showNotification('User ID không hợp lệ', 'error');
        return;
    }
    
    // Get user data first
    const url = `/admin/api/users/${userId}`;
    console.log('API URL:', url);
    makeApiCall(url, 'GET')
        .then(data => {
            if (data.success) {
                const user = data.user;
                
                // Fill form with user data
                document.getElementById('editUserId').value = user.id;
                document.getElementById('editFirstName').value = user.firstName || '';
                document.getElementById('editLastName').value = user.lastName || '';
                document.getElementById('editUsername').value = user.username || '';
                document.getElementById('editEmail').value = user.email || '';
                document.getElementById('editPhoneNumber').value = user.phoneNumber || '';
                document.getElementById('editRole').value = user.roleName || '';
                document.getElementById('editStatus').value = user.status || 'Active';
                
                // Clear password fields
                document.getElementById('editPassword').value = '';
                document.getElementById('editConfirmPassword').value = '';
                
                // Show modal
                const modal = document.getElementById('editUserModal');
                if (modal) {
                    modal.classList.remove('hidden', 'modal-hidden');
                    modal.classList.add('modal-visible');
                    modal.style.display = 'flex';
                    document.body.style.overflow = 'hidden';
                }
            } else {
                showNotification('Không thể tải thông tin user: ' + data.error, 'error');
            }
        })
        .catch(error => {
            showNotification('Lỗi khi tải thông tin user: ' + error.message, 'error');
        });
}

function closeEditUserModal() {
    const modal = document.getElementById('editUserModal');
    if (modal) {
        modal.classList.add('hidden', 'modal-hidden');
        modal.classList.remove('modal-visible');
        modal.style.display = 'none';
        document.body.style.overflow = '';
        // Reset form
        const form = document.getElementById('editUserForm');
        if (form) {
            form.reset();
        }
    }
}

function submitEditUser() {
    const form = document.getElementById('editUserForm');
    const formData = new FormData(form);
    
    // Validate form
    if (!validateEditUserForm()) {
        return;
    }
    
    // Prepare user data
    const userData = {
        userId: formData.get('userId'),
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        username: formData.get('username'),
        email: formData.get('email'),
        phoneNumber: formData.get('phoneNumber'),
        password: formData.get('password'),
        role: formData.get('role'),
        status: formData.get('status')
    };
    
    // Submit to API
    console.log('Submitting edit user data:', userData);
    makeApiCall('/admin/api/users/edit', 'POST', userData)
        .then(data => {
            if (data.success) {
                showNotification('Cập nhật user thành công!', 'success');
                closeEditUserModal();
                // Refresh the page to show updated user
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showNotification('Lỗi: ' + data.error, 'error');
            }
        })
        .catch(error => {
            showNotification('Lỗi: ' + error.message, 'error');
        });
}

function validateEditUserForm() {
    const form = document.getElementById('editUserForm');
    const password = form.querySelector('#editPassword').value;
    const confirmPassword = form.querySelector('#editConfirmPassword').value;
    
    // If password is provided, validate it
    if (password && password.length < 6) {
        showNotification('Mật khẩu phải có ít nhất 6 ký tự', 'error');
        return false;
    }
    
    if (password && password !== confirmPassword) {
        showNotification('Mật khẩu xác nhận không khớp', 'error');
        return false;
    }
    
    return true;
}

function submitAddUser() {
    console.log('submitAddUser called');
    const form = document.getElementById('addUserForm');
    console.log('Form element:', form);
    
    if (!form) {
        console.error('Add User Form not found!');
        showNotification('Lỗi: Không tìm thấy form thêm user', 'error');
        return;
    }
    
    const formData = new FormData(form);
    
    // Validate form
    if (!validateAddUserForm()) {
        console.log('Form validation failed');
        return;
    }
    
    // Show loading state
    const submitBtn = document.querySelector('button[onclick="submitAddUser()"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i>Đang thêm...';
    submitBtn.disabled = true;
    
    // Prepare data
    const userData = {
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        username: formData.get('username'),
        email: formData.get('email'),
        phoneNumber: formData.get('phoneNumber'),
        password: formData.get('password'),
        role: formData.get('role'),
        status: formData.get('status')
    };
    
    // Submit to API
    console.log('Submitting user data:', userData);
    console.log('Form validation passed, making API call...');
    makeApiCall('/admin/api/users/add', 'POST', userData)
        .then(data => {
            console.log('API response:', data);
            if (data.success) {
                showNotification('Thêm user thành công!', 'success');
                closeAddUserModal();
                // Refresh the page to show new user
                setTimeout(() => {
                    location.reload();
                }, 1000);
            } else {
                showNotification('Lỗi: ' + (data.error || 'Không thể thêm user'), 'error');
            }
        })
        .catch(error => {
            console.error('API call failed:', error);
            showNotification('Lỗi: ' + error.message, 'error');
        })
        .finally(() => {
            // Reset button state
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        });
}

function validateAddUserForm() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    if (password !== confirmPassword) {
        showNotification('Mật khẩu xác nhận không khớp!', 'error');
        return false;
    }
    
    if (password.length < 6) {
        showNotification('Mật khẩu phải có ít nhất 6 ký tự!', 'error');
        return false;
    }
    
    return true;
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
window.switchTab = switchTab;
window.openSidebar = openSidebar;
window.closeSidebar = closeSidebar;
window.showAddUserModal = showAddUserModal;
window.closeAddUserModal = closeAddUserModal;
window.submitAddUser = submitAddUser;
window.showEditUserModal = showEditUserModal;
window.closeEditUserModal = closeEditUserModal;
window.submitEditUser = submitEditUser;
window.logout = logout;
window.suspendUser = suspendUser;
window.activateUser = activateUser;
window.updateVehicleStatus = updateVehicleStatus;
window.updateBookingStatus = updateBookingStatus;
window.updateContractStatus = updateContractStatus;
window.refundPayment = refundPayment;
window.showAddDiscountModal = showAddDiscountModal;
window.editDiscount = editDiscount;
window.deleteDiscount = deleteDiscount;
window.showAddInsuranceModal = showAddInsuranceModal;
window.editInsurance = editInsurance;
window.deleteInsurance = deleteInsurance;
window.showSendNotificationModal = showSendNotificationModal;
window.deleteNotification = deleteNotification;
window.testModal = testModal;
window.closeTestModal = closeTestModal;
window.createModalDynamically = createModalDynamically;
window.resetAllRows = resetAllRows;
window.forceShowAllRows = forceShowAllRows;
