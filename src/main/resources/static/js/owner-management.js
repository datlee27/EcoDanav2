// --- Dữ liệu sẽ được chèn từ HTML ---
// const allBookingsData = [];
// const csrfToken = null;
// const csrfHeaderName = null;
// const currentPage = 'bookings';

// --- Hàm render một booking item ---
function renderBookingItem(booking) {
    const status = booking.status || 'Unknown';
    const statusLower = status.toLowerCase();
    const statusMap = {
        'pending': { text: 'Chờ duyệt', class: 'status-pending', icon: 'fa-clock text-yellow-500' },
        'approved': { text: 'Đã duyệt', class: 'status-approved', icon: 'fa-check-circle text-green-500' },
        'awaitingdeposit': { text: 'Chờ cọc', class: 'status-approved', icon: 'fa-hand-holding-usd text-green-500' },
        'confirmed': { text: 'Đã cọc', class: 'status-ongoing', icon: 'fa-calendar-check text-blue-500' },
        'ongoing': { text: 'Đang thuê', class: 'status-ongoing', icon: 'fa-car-side text-blue-500' },
        'completed': { text: 'Hoàn thành', class: 'status-completed', icon: 'fa-flag-checkered text-gray-500' },
        'rejected': { text: 'Từ chối', class: 'status-rejected', icon: 'fa-ban text-red-500' },
        'cancelled': { text: 'Đã hủy', class: 'status-cancelled', icon: 'fa-times-circle text-red-500' },
        'unknown': { text: 'Không xác định', class: 'status-cancelled', icon: 'fa-question-circle text-gray-500' }
    };
    const statusInfo = statusMap[statusLower] || statusMap['unknown'];

    const customerName = (booking.user?.firstName || '') + ' ' + (booking.user?.lastName || booking.user?.username || 'N/A');
    const vehicleInfo = (booking.vehicle?.vehicleModel || 'N/A') + ' (' + (booking.vehicle?.licensePlate || 'N/A') + ')';
    const pickup = booking.pickupDateTime ? new Date(booking.pickupDateTime).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'N/A';
    const returnDate = booking.returnDateTime ? new Date(booking.returnDateTime).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'N/A';
    const created = booking.createdDate ? new Date(booking.createdDate).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }) : 'N/A';
    const amount = booking.totalAmount ? booking.totalAmount.toLocaleString('vi-VN') + ' VND' : 'N/A';
    const pickupLocation =booking.pickupLocation?.pickupLocation || +'('+booking.pickupLocation||'N/A'+')';

    // --- Actions (Nút bấm) ---
    let actionsHtml = `
        <button onclick="viewBooking('${booking.bookingId}')"
                class="text-blue-600 hover:text-blue-800 transition-colors" title="Xem chi tiết">
            <i class="fas fa-eye text-lg"></i>
        </button>
    `;
    if (statusLower === 'pending') {
        actionsHtml += `
            <button onclick="approveBooking('${booking.bookingId}')"
                    class="text-green-600 hover:text-green-800 transition-colors ml-3" title="Duyệt booking">
                <i class="fas fa-check text-lg"></i>
            </button>
            <button onclick="rejectBooking('${booking.bookingId}')"
                    class="text-red-600 hover:text-red-800 transition-colors ml-3" title="Từ chối booking">
                <i class="fas fa-times text-lg"></i>
            </button>
        `;
    }

    if (statusLower === 'confirmed') {
        actionsHtml += `
            <button onclick="handoverBooking('${booking.bookingId}')"
                    class="text-green-600 hover:text-green-800 transition-colors ml-3" title="Giao xe">
                <i class="fas fa-key text-lg"></i>
            </button>
        `;
    }

    // === THÊM MỚI LOGIC HIỂN THỊ NÚT HOÀN THÀNH ===
    if (statusLower === 'ongoing') {
        actionsHtml += `
            <button onclick="completeBooking('${booking.bookingId}')"
                    class="text-blue-600 hover:text-blue-800 transition-colors ml-3" title="Hoàn thành chuyến đi">
                <i class="fas fa-flag-checkered text-lg"></i>
            </button>
        `;
    }
    // === KẾT THÚC THÊM MỚI ===

    // --- HTML của thẻ booking ---
    return `
        <div class="booking-item grid grid-cols-1 md:grid-cols-12 gap-4 items-center"
             data-status="${status}"
             data-code="${booking.bookingCode}"
             data-customer="${customerName}"
             data-created="${booking.createdDate}"
             
             data-amount="${booking.totalAmount}">

            <div class="md:col-span-4">
                <p class="font-semibold text-primary text-sm">${booking.bookingCode}</p>
                <p class="text-sm text-gray-800 font-medium truncate">${customerName}</p>
                <p class="text-xs text-gray-500 truncate">${vehicleInfo}</p>
            </div>

            <div class="md:col-span-3 text-sm">
            
              <p class="text-gray-700"><strong class="font-medium text-gray-500">Ở:</strong> ${pickupLocation}</p>
                <p class="text-gray-700"><strong class="font-medium text-gray-500">Từ:</strong> ${pickup}</p>
                <p class="text-gray-700"><strong class="font-medium text-gray-500">Đến:</strong> ${returnDate}</p>
            </div>

            <div class="md:col-span-2">
                <span class="status-badge ${statusInfo.class}">
                    <i class="fas ${statusInfo.icon} mr-2"></i> ${statusInfo.text}
                </span>
            </div>

            <div class="md:col-span-2 md:text-right">
                <p class="text-sm font-bold text-gray-800">${amount}</p>
                <p class="text-xs text-gray-500">Ngày tạo: ${created}</p>
            </div>

            <div class="md:col-span-1 md:text-right">
                ${actionsHtml}
            </div>
        </div>
    `;
}

// --- Hàm thay đổi tab trạng thái ---
function changeStatusTab(tabElement) {
    // Xóa class 'active' khỏi tất cả các tab
    document.querySelectorAll('#status-tabs .status-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    // Thêm class 'active' cho tab vừa click
    tabElement.classList.add('active');
    // Lọc và render lại danh sách
    filterAndSortBookings();
}


// --- Hàm sắp xếp và lọc (đọc từ tab) ---
function filterAndSortBookings() {
    // Lấy trạng thái từ tab đang active
    const statusFilterElement = document.querySelector('#status-tabs .status-tab.active');
    if (!statusFilterElement) return; // Thoát nếu chưa có tab

    const statusFilter = statusFilterElement.dataset.status;
    const searchInput = document.getElementById("search-input").value.toLowerCase();
    const sortSelect = document.getElementById("sort-select").value;

    // 1. Lọc
    let filteredBookings = allBookingsData.filter(booking => {
        const status = booking.status || 'Unknown';
        const customerName = (booking.user?.firstName || '') + ' ' + (booking.user?.lastName || booking.user?.username || 'N/A');
        const code = booking.bookingCode || '';

        // Lọc trạng thái (hỗ trợ gom nhóm: "Approved,AwaitingDeposit")
        const statusMatch = !statusFilter || statusFilter.split(',').includes(status);

        // Lọc tìm kiếm
        const searchMatch = !searchInput ||
            code.toLowerCase().includes(searchInput) ||
            customerName.toLowerCase().includes(searchInput);

        return statusMatch && searchMatch;
    });

    // 2. Sắp xếp
    filteredBookings.sort((a, b) => {
        switch (sortSelect) {
            case "newest":
                return new Date(b.createdDate) - new Date(a.createdDate);
            case "oldest":
                return new Date(a.createdDate) - new Date(b.createdDate);
            case "amount-high":
                return (b.totalAmount || 0) - (a.totalAmount || 0);
            case "amount-low":
                return (a.totalAmount || 0) - (b.totalAmount || 0);
            default:
                return 0;
        }
    });

    // 3. Render (vào 1 container duy nhất)
    renderFilteredBookings(filteredBookings);
}

// --- Hàm render danh sách (đơn giản hóa) ---
function renderFilteredBookings(filteredBookings) {
    const container = document.getElementById('booking-list-container');
    const noBookingsMessage = document.getElementById('no-bookings-message');

    // Xóa nội dung cũ, ngoại trừ noBookingsMessage
    while (container.firstChild && container.firstChild !== noBookingsMessage) {
        container.removeChild(container.firstChild);
    }

    if (filteredBookings.length === 0) {
        if (noBookingsMessage) {
            noBookingsMessage.classList.remove('hidden');
        }
    } else {
        if (noBookingsMessage) {
            noBookingsMessage.classList.add('hidden');
        }
        let allItemsHtml = '';
        filteredBookings.forEach(booking => {
            allItemsHtml += renderBookingItem(booking);
        });
        container.insertAdjacentHTML('afterbegin', allItemsHtml); // Thêm vào đầu container
    }
}


// --- Các hàm Modal ---

let currentBookingId = null;

// Lấy CSRF token (nếu cần cho POST/PUT/DELETE)
function getCsrfHeaders() {
    // Sửa đổi: Chỉ trả về object headers cho JSON, hoặc một phần cho FormData
    const headers = { 'Content-Type': 'application/json' };
    if (typeof csrfToken !== 'undefined' && csrfToken && typeof csrfHeaderName !== 'undefined' && csrfHeaderName) {
        headers[csrfHeaderName] = csrfToken;
    }
    return headers;
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if(modal) modal.classList.add('hidden');

    if (modalId === 'reject-modal') {
        const rejectReason = document.getElementById('reject-reason');
        if(rejectReason) rejectReason.value = '';
    }

    // === SỬA ĐỔI: Reset khi đóng modal complete ===
    if (modalId === 'complete-modal') {
        currentBookingId = null;

        // Reset nút Hoàn thành
        const btn = document.getElementById('confirmCompleteBtn');
        if(btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-flag-checkered mr-2"></i>Hoàn thành';
        }

        // THÊM MỚI: Reset nút Bảo trì
        const maintenanceBtn = document.getElementById('confirmMaintenanceBtn');
        if(maintenanceBtn) {
            maintenanceBtn.disabled = false;
            maintenanceBtn.innerHTML = '<i class="fas fa-tools mr-2"></i>Hoàn thành & Bảo trì';
        }

        // Reset form fields
        const notes = document.getElementById('complete-notes');
        if(notes) notes.value = '';
        const images = document.getElementById('complete-images');
        if(images) images.value = null;
        const preview = document.getElementById('complete-images-preview');
        if(preview) preview.innerHTML = '<p class="text-xs text-gray-400 p-2">Chưa chọn ảnh nào.</p>';
        const error = document.getElementById('complete-images-error');
        if(error) error.classList.add('hidden');
    }
    // === KẾT THÚC SỬA ĐỔI ===
}

function showNotification(isSuccess, message) {
    const successEl = document.getElementById('success-notification');
    const errorEl = document.getElementById('error-notification');

    if(successEl) {
        successEl.style.transform = 'translateX(100%)';
        const span = successEl.querySelector('span');
        if(span) span.textContent = '';
    }
    if(errorEl) {
        errorEl.style.transform = 'translateX(100%)';
        const span = errorEl.querySelector('span');
        if(span) span.textContent = '';
    }

    if (isSuccess && successEl) {
        const span = successEl.querySelector('span');
        if(span) span.textContent = message;
        successEl.style.transform = 'translateX(0)';
        setTimeout(() => { successEl.style.transform = 'translateX(100%)'; }, 4000);
    } else if (!isSuccess && errorEl) {
        const span = errorEl.querySelector('span');
        if(span) span.textContent = message;
        errorEl.style.transform = 'translateX(0)';
        setTimeout(() => { errorEl.style.transform = 'translateX(100%)'; }, 4000);
    }
}

function closeNotification(button) {
    // Tìm thẻ cha gần nhất có ID kết thúc bằng "-notification" và ẩn nó đi
    const notificationDiv = button.closest('div[id$="-notification"]');
    if (notificationDiv) {
        notificationDiv.style.transform = 'translateX(100%)';
        // Xóa hẳn sau khi animation kết thúc (tùy chọn)
        setTimeout(() => {
            if (notificationDiv) {
                notificationDiv.remove();
            }
        }, 500);
    }
}

// Xem chi tiết
function viewBooking(bookingId) {
    const modal = document.getElementById('booking-detail-modal');
    const content = document.getElementById('booking-detail-content');
    if (!modal || !content) return;

    modal.classList.remove('hidden');
    content.innerHTML = `<div class="text-center py-8"><i class="fas fa-spinner fa-spin text-4xl text-primary"></i><p class="mt-2 text-gray-600">Loading...</p></div>`;

    fetch(`/owner/management/bookings/${bookingId}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            content.innerHTML = `
                <div class="space-y-4">
                    <div class="p-4 border rounded-lg bg-gray-50">
                        <h4 class="text-lg font-semibold text-gray-700 mb-3 border-b pb-2 flex items-center"><i class="fas fa-receipt mr-3 text-primary"></i>Thông tin Booking</h4>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                            <p><strong>Mã:</strong> <span>${data.bookingCode || 'N/A'}</span></p>
                            <p><strong>Trạng thái:</strong> <span class="status-badge status-${data.status ? data.status.toLowerCase() : ''}">${data.status || 'N/A'}</span></p>
                            <p><strong>Tổng tiền:</strong> <span class="font-bold text-green-600">${data.totalAmount ? data.totalAmount.toLocaleString('vi-VN') + ' VND' : 'N/A'}</span></p>
                            <p><strong>Loại thuê:</strong> <span>${data.rentalType || 'N/A'}</span></p>
                            <p><strong>Nhận xe:</strong> <span>${data.pickupDateTime ? new Date(data.pickupDateTime).toLocaleString('vi-VN') : 'N/A'}</span></p>
                            <p><strong>Trả xe:</strong> <span>${data.returnDateTime ? new Date(data.returnDateTime).toLocaleString('vi-VN') : 'N/A'}</span></p>
                            <p class="md:col-span-2"><strong>Địa điểm giao xe:</strong> <span class="font-medium text-blue-600">${data.pickupLocation || 'Chưa có thông tin'}</span></p>
                        </div>
                    </div>
                    <div class="p-4 border rounded-lg bg-gray-50">
                        <h4 class="text-lg font-semibold text-gray-700 mb-3 border-b pb-2 flex items-center"><i class="fas fa-user-circle mr-3 text-primary"></i>Thông tin khách hàng</h4>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                            <p><strong>Tên:</strong> <span>${data.customerName || 'N/A'}</span></p>
                            <p><strong>Email:</strong> <span>${data.customerEmail || 'N/A'}</span></p>
                            <p><strong>SĐT:</strong> <span>${data.customerPhone || 'N/A'}</span></p>
                            <p><strong>Ngày sinh:</strong> <span>${data.customerDOB ? new Date(data.customerDOB).toLocaleDateString('vi-VN') : 'N/A'}</span></p>
                        </div>
                    </div>
                    <div class="p-4 border rounded-lg bg-gray-50">
                        <h4 class="text-lg font-semibold text-gray-700 mb-3 border-b pb-2 flex items-center"><i class="fas fa-car-side mr-3 text-primary"></i>Thông tin xe</h4>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                            <p><strong>Mẫu xe:</strong> <span>${data.vehicleModel || 'N/A'}</span></p>
                            <p><strong>Biển số:</strong> <span>${data.licensePlate || 'N/A'}</span></p>
                            <p><strong>Phân loại:</strong> <span>${data.vehicleCategory || 'N/A'}</span></p>
                            <p><strong>Truyền động:</strong> <span>${data.transmission || 'N/A'}</span></p>
                        </div>
                    </div>
                </div>
            `;
        })
        .catch(error => {
            console.error('Error fetching booking details:', error);
            content.innerHTML = `<p class="text-center text-red-500">Không thể tải chi tiết. Vui lòng thử lại.</p>`;
        });
}

// Duyệt
function approveBooking(bookingId) {
    currentBookingId = bookingId;
    const modal = document.getElementById('approve-modal');
    if(modal) modal.classList.remove('hidden');
}

function confirmApprove() {
    if (!currentBookingId) return;
    const btn = document.getElementById('confirmApproveBtn');
    if(btn) btn.disabled = true;

    fetch(`/owner/management/bookings/${currentBookingId}/approve`, {
        method: 'POST',
        headers: getCsrfHeaders()
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification(true, 'Đã duyệt booking thành công!');
                closeModal('approve-modal');
                location.reload(); // Tải lại
            } else {
                throw new Error(data.message || 'Không thể duyệt booking');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(false, `Lỗi: ${error.message}`);
            if(btn) btn.disabled = false;
        });
}

// Từ chối
function rejectBooking(bookingId) {
    currentBookingId = bookingId;
    const modal = document.getElementById('reject-modal');
    if(modal) modal.classList.remove('hidden');
}

function confirmReject() {
    if (!currentBookingId) return;
    const reasonEl = document.getElementById('reject-reason');
    const reason = reasonEl ? reasonEl.value : '';

    if (!reason || reason.trim() === '') {
        alert('Vui lòng nhập lý do từ chối');
        return;
    }

    const btn = document.getElementById('confirmRejectBtn');
    if(btn) btn.disabled = true;

    fetch(`/owner/management/bookings/${currentBookingId}/reject`, {
        method: 'POST',
        headers: getCsrfHeaders(),
        body: JSON.stringify({ reason: reason })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification(true, 'Đã từ chối booking thành công!');
                closeModal('reject-modal');
                location.reload(); // Tải lại
            } else {
                throw new Error(data.message || 'Không thể từ chối booking');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(false, `Lỗi: ${error.message}`);
            if(btn) btn.disabled = false;
        });
}

// --- SỬA ĐỔI: Hàm Hoàn thành (Complete) ---
function completeBooking(bookingId) {
    currentBookingId = bookingId;
    const modal = document.getElementById('complete-modal');
    if(modal) {
        modal.classList.remove('hidden');

        // Reset form fields khi mở
        const notes = document.getElementById('complete-notes');
        if(notes) notes.value = '';

        const images = document.getElementById('complete-images');
        if(images) images.value = null; // Quan trọng: reset file input

        const preview = document.getElementById('complete-images-preview');
        if(preview) preview.innerHTML = '<p class="text-xs text-gray-400 p-2">Chưa chọn ảnh nào.</p>';

        const error = document.getElementById('complete-images-error');
        if(error) error.classList.add('hidden');

        // Reset cả 2 nút
        const btn = document.getElementById('confirmCompleteBtn');
        if(btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-flag-checkered mr-2"></i>Hoàn thành';
        }
        const maintenanceBtn = document.getElementById('confirmMaintenanceBtn');
        if(maintenanceBtn) {
            maintenanceBtn.disabled = false;
            maintenanceBtn.innerHTML = '<i class="fas fa-tools mr-2"></i>Hoàn thành & Bảo trì';
        }
    }
}

// Sửa đổi: Thêm tham số isMaintenance, mặc định là false
function confirmComplete(isMaintenance = false) {
    if (!currentBookingId) return;

    const btn = document.getElementById('confirmCompleteBtn');
    const maintenanceBtn = document.getElementById('confirmMaintenanceBtn'); // Nút mới
    const notes = document.getElementById('complete-notes').value;
    const imageInput = document.getElementById('complete-images');
    const files = imageInput.files;

    // Kiểm tra số lượng ảnh (dù đã check ở onchange, check lại an toàn)
    if (files.length > 5) {
        const errorEl = document.getElementById('complete-images-error');
        if(errorEl) {
            errorEl.textContent = 'Bạn chỉ có thể upload tối đa 5 ảnh.';
            errorEl.classList.remove('hidden');
        }
        return;
    }

    // Vô hiệu hóa cả hai nút
    if(btn) btn.disabled = true;
    if(maintenanceBtn) maintenanceBtn.disabled = true;

    // Hiển thị loading trên nút đã được nhấp
    const clickedBtn = isMaintenance ? maintenanceBtn : btn;
    if(clickedBtn) clickedBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Đang xử lý...';

    const formData = new FormData();
    formData.append('notes', notes);
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append('images', files[i]); // Backend sẽ nhận List<MultipartFile> ten la 'images'
        }
    }

    // THÊM MỚI: Gửi cờ 'setMaintenance'
    // FormData không thể gửi boolean, gửi string "true"
    formData.append('setMaintenance', isMaintenance.toString());

    // Lấy CSRF header (nhưng không set Content-Type)
    const csrfHeaders = getCsrfHeaders();
    const headers = {};
    if (csrfHeaderName && csrfHeaders[csrfHeaderName]) {
        headers[csrfHeaderName] = csrfHeaders[csrfHeaderName];
    }

    fetch(`/owner/management/bookings/${currentBookingId}/complete`, {
        method: 'POST',
        headers: headers, // Chỉ gửi CSRF header, không gửi Content-Type
        body: formData
    })
        .then(response => {
            // Kiểm tra nếu response không phải JSON (ví dụ: lỗi server 500)
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                return response.json();
            } else {
                // Nếu không phải JSON, ném lỗi với status text
                throw new Error(`Server error: ${response.statusText}`);
            }
        })
        .then(data => {
            if (data.success) {
                showNotification(true, 'Đã hoàn thành booking!');
                closeModal('complete-modal');
                location.reload(); // Tải lại
            } else {
                throw new Error(data.message || 'Không thể hoàn thành booking');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification(false, `Lỗi: ${error.message}`);

            // Kích hoạt lại cả hai nút
            if(btn) btn.disabled = false;
            if(maintenanceBtn) maintenanceBtn.disabled = false;

            // Reset lại text cho nút đã click
            if(clickedBtn) clickedBtn.innerHTML = isMaintenance
                ? '<i class="fas fa-tools mr-2"></i>Hoàn thành & Bảo trì'
                : '<i class="fas fa-flag-checkered mr-2"></i>Hoàn thành';
        });
}
// --- KẾT THÚC SỬA ĐỔI ---

// --- Khởi chạy lần đầu ---
document.addEventListener('DOMContentLoaded', function() {

    // Biến 'currentPage' được lấy từ thẻ script inline trong HTML
    if (typeof currentPage !== 'undefined' && currentPage && typeof highlightOwnerSidebarLink === 'function') {
        highlightOwnerSidebarLink(currentPage);
    }

    if (typeof loadNotificationCount === 'function') {
        loadNotificationCount();
    }

    // Ẩn <p> message đi để JS thêm vào
    const noBookingsMessage = document.getElementById('no-bookings-message');
    if(noBookingsMessage) {
        // Tạm thời giữ lại, nhưng đảm bảo nó bị ẩn ban đầu
        noBookingsMessage.classList.add('hidden');
    }

    // Render danh sách ban đầu (cho tab "Tất cả")
    filterAndSortBookings();
});
// --- MỚI: Hàm mở Modal Giao Xe ---
function handoverBooking(bookingId) {
    currentBookingId = bookingId;
    const modal = document.getElementById('handover-modal');
    const form = document.getElementById('handover-form');

    if(modal && form) {
        // Cập nhật action của form
        form.action = `/owner/management/bookings/${bookingId}/handover`;

        // Reset form (xóa ảnh và text cũ nếu có)
        form.reset();

        // Mở modal
        modal.classList.remove('hidden');
    } else {
        console.error('Handover modal or form not found!');
    }
}