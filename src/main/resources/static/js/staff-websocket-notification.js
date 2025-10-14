// Staff WebSocket Notification System
var stompClient = null;
var reconnectAttempts = 0;
var maxReconnectAttempts = 5;

// Connect to WebSocket
function connectWebSocket() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Disable debug logging
    stompClient.debug = null;
    
    stompClient.connect({}, function(frame) {
        console.log('✅ WebSocket Connected');
        reconnectAttempts = 0;
        
        // Subscribe to staff notifications
        stompClient.subscribe('/topic/staff/notifications', function(message) {
            var notification = JSON.parse(message.body);
            handleNotification(notification);
        });
        
        // Update connection status
        updateConnectionStatus(true);
        
    }, function(error) {
        console.error('❌ WebSocket Error:', error);
        updateConnectionStatus(false);
        
        // Attempt to reconnect
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            console.log(`🔄 Reconnecting... (${reconnectAttempts}/${maxReconnectAttempts})`);
            setTimeout(connectWebSocket, 3000);
        }
    });
}

// Handle incoming notification
function handleNotification(notification) {
    console.log('📬 New notification:', notification);
    
    // Show browser notification
    showBrowserNotification(notification);
    
    // Show in-app toast
    showToast(notification);
    
    // Update notification badge
    updateNotificationBadge();
    
    // Play notification sound (optional)
    playNotificationSound();
}

// Show browser notification
function showBrowserNotification(notification) {
    if (Notification.permission === "granted") {
        var title = getNotificationTitle(notification.type);
        var options = {
            body: notification.message,
            icon: '/images/logo.png',
            badge: '/images/badge.png',
            tag: notification.type,
            requireInteraction: notification.type === 'REFUND_REQUEST'
        };
        
        var browserNotif = new Notification(title, options);
        
        browserNotif.onclick = function() {
            window.focus();
            if (notification.type === 'REFUND_REQUEST') {
                window.location.href = '/staff/notifications';
            }
            browserNotif.close();
        };
        
        // Auto close after 10 seconds
        setTimeout(() => browserNotif.close(), 10000);
    }
}

// Show in-app toast notification
function showToast(notification) {
    var toast = document.createElement('div');
    toast.className = 'notification-toast fixed top-20 right-4 z-50 transform transition-all duration-300';
    
    var bgColor = notification.type === 'REFUND_REQUEST' ? 'bg-red-500' : 
                  notification.type === 'NEW_BOOKING' ? 'bg-green-500' : 
                  'bg-blue-500';
    
    var icon = notification.type === 'REFUND_REQUEST' ? 'fa-money-bill-wave' : 
               notification.type === 'NEW_BOOKING' ? 'fa-calendar-check' : 
               'fa-bell';
    
    toast.innerHTML = `
        <div class="${bgColor} text-white px-6 py-4 rounded-lg shadow-2xl max-w-md">
            <div class="flex items-start gap-3">
                <i class="fas ${icon} text-2xl"></i>
                <div class="flex-1">
                    <p class="font-bold mb-1">${getNotificationTitle(notification.type)}</p>
                    <p class="text-sm opacity-90">${notification.message}</p>
                    ${notification.bookingCode ? `<p class="text-xs mt-2 opacity-75">Mã: ${notification.bookingCode}</p>` : ''}
                </div>
                <button onclick="this.parentElement.parentElement.parentElement.remove()" 
                        class="text-white hover:text-gray-200">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.style.opacity = '1', 10);
    
    // Auto remove after 8 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 8000);
}

// Get notification title based on type
function getNotificationTitle(type) {
    switch(type) {
        case 'REFUND_REQUEST':
            return '💰 Yêu cầu hoàn tiền';
        case 'NEW_BOOKING':
            return '📅 Booking mới';
        case 'BOOKING_STATUS_UPDATE':
            return '🔔 Cập nhật booking';
        default:
            return '🔔 Thông báo mới';
    }
}

// Update notification badge
function updateNotificationBadge() {
    fetch('/staff/notifications/unread-count')
        .then(response => response.json())
        .then(data => {
            var badge = document.querySelector('.notification-badge');
            if (badge) {
                if (data.count > 0) {
                    badge.textContent = data.count;
                    badge.classList.remove('hidden');
                    badge.classList.add('badge-pulse');
                } else {
                    badge.classList.add('hidden');
                }
            }
        })
        .catch(error => console.error('Error updating badge:', error));
}

// Play notification sound
function playNotificationSound() {
    try {
        var audio = new Audio('/sounds/notification.mp3');
        audio.volume = 0.5;
        audio.play().catch(e => console.log('Audio play failed:', e));
    } catch (e) {
        console.log('Audio not available');
    }
}

// Update connection status indicator
function updateConnectionStatus(connected) {
    var indicator = document.getElementById('ws-status-indicator');
    if (indicator) {
        if (connected) {
            indicator.className = 'w-2 h-2 bg-green-500 rounded-full';
            indicator.title = 'Kết nối WebSocket';
        } else {
            indicator.className = 'w-2 h-2 bg-red-500 rounded-full';
            indicator.title = 'Mất kết nối WebSocket';
        }
    }
}

// Request notification permission
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().then(function(permission) {
            if (permission === 'granted') {
                console.log('✅ Notification permission granted');
            }
        });
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Request notification permission
    requestNotificationPermission();
    
    // Connect WebSocket
    connectWebSocket();
    
    // Update badge on load
    updateNotificationBadge();
    
    // Refresh badge every 30 seconds
    setInterval(updateNotificationBadge, 30000);
});

// Reconnect on page visibility change
document.addEventListener('visibilitychange', function() {
    if (!document.hidden && (!stompClient || !stompClient.connected)) {
        console.log('🔄 Page visible, reconnecting...');
        connectWebSocket();
    }
});
