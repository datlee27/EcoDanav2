/**
 * Mã JavaScript này dành cho tệp admin-refund-requests-fragment.html (server-side rendered).
 * Nó cung cấp các hàm trợ giúp cho các sự kiện onclick của modal.
 *
 * Các hàm fetch dữ liệu cũ (như initializeRefundRequestsTab, loadStatistics) đã bị loại bỏ
 * vì dữ liệu hiện được nạp bởi Thymeleaf.
 */
(function() {

    /**
     * Hiển thị modal chi tiết yêu cầu hoàn tiền.
     * Hàm này được gọi bởi 'onclick="showRefundDetailModal(this)"' từ HTML.
     * @param {HTMLElement} buttonElement - Nút đã được nhấp vào, chứa các thuộc tính data-*.
     */
    window.showRefundDetailModal = function(buttonElement) {
        const data = buttonElement.dataset;
        const modal = document.getElementById('refundDetailModal');
        if (!modal) {
            console.error('Modal "refundDetailModal" not found!');
            return;
        }

        // 1. Điền thông tin cơ bản vào modal
        document.getElementById('modalRefundId').textContent = 'ID: ' + (data.refundId || '-');
        document.getElementById('modalRefundAmount').textContent = data.refundAmount ? parseFloat(data.refundAmount).toLocaleString('vi-VN') + ' ₫' : '0 ₫';
        document.getElementById('modalBookingCode').textContent = data.bookingCode || '-';
        document.getElementById('modalCreatedDate').textContent = data.createdDate || '-';
        document.getElementById('modalCustomerName').textContent = data.customerName || '-';
        document.getElementById('modalCustomerEmail').textContent = data.customerEmail || '-';
        document.getElementById('modalCustomerPhone').textContent = data.customerPhone || '-';
        document.getElementById('modalCancelReason').textContent = data.cancelReason || '-';

        // 2. Cập nhật Status Badge
        const statusBadge = document.getElementById('statusBadge');
        statusBadge.textContent = data.status || 'Unknown';
        // Reset các lớp CSS cũ
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold inline-block';

        if (data.status === 'Pending') {
            statusBadge.classList.add('bg-yellow-100', 'text-yellow-800');
            statusBadge.innerHTML = '<i class="fas fa-clock mr-1"></i> Chờ duyệt';
        } else if (data.status === 'Approved') {
            statusBadge.classList.add('bg-green-100', 'text-green-800');
            statusBadge.innerHTML = '<i class="fas fa-check mr-1"></i> Đã duyệt';
        } else if (data.status === 'Rejected') {
            statusBadge.classList.add('bg-red-100', 'text-red-800');
            statusBadge.innerHTML = '<i class="fas fa-times mr-1"></i> Từ chối';
        }

        // 3. Ẩn/hiện và điền thông tin Ngân hàng
        const bankSection = document.getElementById('bankAccountSection');
        // Kiểm tra data.bankName có tồn tại và không phải là chuỗi 'undefined'
        if (data.bankName && data.bankName !== 'undefined') {
            document.getElementById('modalBankName').textContent = data.bankName || '-';
            document.getElementById('modalAccountNumber').textContent = data.accountNumber || '-';
            document.getElementById('modalAccountHolder').textContent = data.accountHolder || '-';
            bankSection.classList.remove('hidden');
        } else {
            bankSection.classList.add('hidden');
        }

        // 4. Ẩn/hiện và điền Admin Notes
        const adminNotesSection = document.getElementById('adminNotesSection');
        // Kiểm tra data.adminNotes có tồn tại và không phải là chuỗi 'undefined'
        if (data.adminNotes && data.adminNotes !== 'undefined') {
            document.getElementById('modalAdminNotes').textContent = data.adminNotes;
            adminNotesSection.classList.remove('hidden');
        } else {
            adminNotesSection.classList.add('hidden');
        }

        // 5. Tạo các nút hành động (Approve/Reject)
        const actionButtonsDiv = document.getElementById('actionButtons');
        actionButtonsDiv.innerHTML = ''; // Xóa các nút cũ

        if (data.status === 'Pending') {
            // Nút Approve
            const approveButton = document.createElement('button');
            approveButton.innerHTML = '<i class="fas fa-check mr-2"></i>Duyệt (Đã hoàn tiền)';
            approveButton.className = 'px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700';
            approveButton.onclick = function() {
                // Gọi hàm approve (logic từ tệp JS cũ)
                approveRefundRequest(data.refundId);
            };
            actionButtonsDiv.appendChild(approveButton);

            // Nút Reject
            const rejectButton = document.createElement('button');
            rejectButton.innerHTML = '<i class="fas fa-times mr-2"></i>Từ chối';
            rejectButton.className = 'px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700';
            rejectButton.onclick = function() {
                // Gọi hàm reject (logic từ tệp JS cũ)
                rejectRefundRequest(data.refundId);
            };
            actionButtonsDiv.appendChild(rejectButton);
        }

        // 6. Hiển thị modal
        modal.classList.remove('hidden');
    };

    /**
     * Đóng modal chi tiết.
     * Hàm này được gọi bởi 'onclick="closeRefundDetailModal()"'
     */
    window.closeRefundDetailModal = function() {
        const modal = document.getElementById('refundDetailModal');
        if (modal) {
            modal.classList.add('hidden');
        }
    };

    /**
     * Gửi yêu cầu duyệt hoàn tiền (API call).
     * Logic này được lấy từ tệp admin-refund-requests-tab.js cũ.
     */
    window.approveRefundRequest = function(refundRequestId) {
        var notes = prompt('Nhập ghi chú (tùy chọn):', '');
        if (notes === null) return; // Người dùng hủy

        var url = '/admin/api/refund-requests/' + refundRequestId + '/approve';
        if (notes) {
            url += '?adminNotes=' + encodeURIComponent(notes);
        }

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
                // Thêm các headers khác nếu cần (ví dụ: CSRF token)
            }
        })
            .then(function(response) {
                if (!response.ok) {
                    return response.json().then(function(err) {
                        throw new Error(err.message || 'Failed to approve refund request');
                    });
                }
                return response.json();
            })
            .then(function(result) {
                if (result.status === 'success') {
                    alert('✅ Yêu cầu hoàn tiền đã được duyệt!');
                    closeRefundDetailModal();
                    // Tải lại trang để cập nhật dữ liệu từ server (thay vì gọi loadRefundRequests)
                    location.reload();
                } else {
                    throw new Error(result.message || 'Approval failed');
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('❌ Có lỗi xảy ra khi duyệt: ' + error.message);
            });
    };

    /**
     * Gửi yêu cầu từ chối hoàn tiền (API call).
     * Logic này được lấy từ tệp admin-refund-requests-tab.js cũ.
     */
    window.rejectRefundRequest = function(refundRequestId) {
        var notes = prompt('Nhập lý do từ chối (bắt buộc):', '');
        if (notes === null || notes.trim() === '') {
            alert('Lý do từ chối là bắt buộc');
            return;
        }

        var url = '/admin/api/refund-requests/' + refundRequestId + '/reject?adminNotes=' + encodeURIComponent(notes);

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
                // Thêm các headers khác nếu cần (ví dụ: CSRF token)
            }
        })
            .then(function(response) {
                if (!response.ok) {
                    return response.json().then(function(err) {
                        throw new Error(err.message || 'Failed to reject refund request');
                    });
                }
                return response.json();
            })
            .then(function(result) {
                if (result.status === 'success') {
                    alert('✅ Đã từ chối yêu cầu hoàn tiền!');
                    closeRefundDetailModal();
                    // Tải lại trang để cập nhật dữ liệu từ server (thay vì gọi loadRefundRequests)
                    location.reload();
                } else {
                    throw new Error(result.message || 'Rejection failed');
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('❌ Có lỗi xảy ra khi từ chối: ' + error.message);
            });
    };

})(); // IIFE (Immediately Invoked Function Expression)