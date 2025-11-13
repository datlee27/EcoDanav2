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

        // Debug: Log all data attributes
        console.log('=== REFUND DETAIL MODAL DATA ===');
        console.log('Bank Name:', data.bankName);
        console.log('Account Number:', data.accountNumber);
        console.log('Account Holder:', data.accountHolder);
        console.log('QR Code Image:', data.qrCodeImage);
        console.log('Customer ID:', data.customerId);
        console.log('All data:', data);

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

        // 3. Lưu refund ID để sử dụng sau
        window.currentRefundRequestId = data.refundId;
        window.currentCustomerId = data.customerId;

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
                    // Cập nhật status badge trong modal
                    var statusBadge = document.getElementById('statusBadge');
                    statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold inline-block bg-green-100 text-green-800';
                    statusBadge.innerHTML = '<i class="fas fa-check mr-1"></i> Đã duyệt';
                    
                    // Cập nhật admin notes nếu có
                    if (notes) {
                        var adminNotesSection = document.getElementById('adminNotesSection');
                        document.getElementById('modalAdminNotes').textContent = notes;
                        adminNotesSection.classList.remove('hidden');
                    }
                    
                    // Ẩn các nút hành động (Approve/Reject)
                    var actionButtonsDiv = document.getElementById('actionButtons');
                    actionButtonsDiv.innerHTML = '';
                    
                    // Hiển thị thông báo thành công
                    var successMessage = document.createElement('div');
                    successMessage.className = 'bg-green-50 border-l-4 border-green-500 p-4 rounded mb-4';
                    successMessage.innerHTML = '<p class="text-green-900"><i class="fas fa-check-circle mr-2 text-green-600"></i><strong>✅ Yêu cầu hoàn tiền đã được duyệt thành công!</strong></p>';
                    
                    // Chèn thông báo vào đầu nội dung modal
                    var modalContent = document.querySelector('#refundDetailModal .p-6');
                    modalContent.insertBefore(successMessage, modalContent.firstChild);
                    
                    // Tự động ẩn thông báo sau 3 giây
                    setTimeout(function() {
                        successMessage.style.transition = 'opacity 0.3s ease-out';
                        successMessage.style.opacity = '0';
                        setTimeout(function() {
                            successMessage.remove();
                        }, 300);
                    }, 3000);
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
                    // Cập nhật status badge trong modal
                    var statusBadge = document.getElementById('statusBadge');
                    statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold inline-block bg-red-100 text-red-800';
                    statusBadge.innerHTML = '<i class="fas fa-times mr-1"></i> Từ chối';
                    
                    // Cập nhật admin notes
                    var adminNotesSection = document.getElementById('adminNotesSection');
                    document.getElementById('modalAdminNotes').textContent = notes;
                    adminNotesSection.classList.remove('hidden');
                    
                    // Ẩn các nút hành động (Approve/Reject)
                    var actionButtonsDiv = document.getElementById('actionButtons');
                    actionButtonsDiv.innerHTML = '';
                    
                    // Hiển thị thông báo từ chối
                    var rejectMessage = document.createElement('div');
                    rejectMessage.className = 'bg-red-50 border-l-4 border-red-500 p-4 rounded mb-4';
                    rejectMessage.innerHTML = '<p class="text-red-900"><i class="fas fa-times-circle mr-2 text-red-600"></i><strong>✅ Yêu cầu hoàn tiền đã bị từ chối!</strong></p>';
                    
                    // Chèn thông báo vào đầu nội dung modal
                    var modalContent = document.querySelector('#refundDetailModal .p-6');
                    modalContent.insertBefore(rejectMessage, modalContent.firstChild);
                    
                    // Tự động ẩn thông báo sau 3 giây
                    setTimeout(function() {
                        rejectMessage.style.transition = 'opacity 0.3s ease-out';
                        rejectMessage.style.opacity = '0';
                        setTimeout(function() {
                            rejectMessage.remove();
                        }, 300);
                    }, 3000);
                } else {
                    throw new Error(result.message || 'Rejection failed');
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('❌ Có lỗi xảy ra khi từ chối: ' + error.message);
            });
    };

    /**
     * Hiển thị modal để xem tài khoản ngân hàng mặc định
     */
    window.showBankAccountModal = function() {
        const bankAccountModal = document.getElementById('bankAccountModal');
        if (!bankAccountModal) {
            console.error('Bank account modal not found!');
            return;
        }

        // Load default bank account của customer
        if (window.currentCustomerId) {
            console.log('Loading default bank account for customer:', window.currentCustomerId);
            loadDefaultBankAccountForModal(window.currentCustomerId);
        } else {
            console.error('Customer ID not available');
            alert('Không tìm thấy thông tin khách hàng');
            return;
        }

        // Hiển thị modal
        bankAccountModal.classList.remove('hidden');
    };

    /**
     * Đóng modal thông tin tài khoản ngân hàng
     */
    window.closeBankAccountModal = function() {
        const bankAccountModal = document.getElementById('bankAccountModal');
        if (bankAccountModal) {
            bankAccountModal.classList.add('hidden');
        }
    };

    /**
     * Mở lightbox để xem ảnh QR code to
     */
    window.openQRCodeLightbox = function(imageSrc) {
        const lightbox = document.getElementById('qrCodeLightbox');
        const lightboxImage = document.getElementById('qrCodeLightboxImage');
        if (lightbox && lightboxImage) {
            lightboxImage.src = imageSrc;
            lightbox.classList.remove('hidden');
        }
    };

    /**
     * Đóng lightbox QR code
     */
    window.closeQRCodeLightbox = function() {
        const lightbox = document.getElementById('qrCodeLightbox');
        if (lightbox) {
            lightbox.classList.add('hidden');
        }
    };

    // Close lightbox when clicking outside the image
    document.addEventListener('DOMContentLoaded', function() {
        const lightbox = document.getElementById('qrCodeLightbox');
        if (lightbox) {
            lightbox.addEventListener('click', function(e) {
                if (e.target === this) {
                    window.closeQRCodeLightbox();
                }
            });
        }
    });

    /**
     * Load default bank account của customer cho modal
     */
    window.loadDefaultBankAccountForModal = function(customerId) {
        if (!customerId) {
            console.error('Customer ID not provided');
            return;
        }

        console.log('Loading default bank account for modal, customer:', customerId);
        
        fetch('/customer/bank-accounts/api/list-by-user/' + customerId)
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error('HTTP error, status=' + response.status);
                }
                return response.json();
            })
            .then(bankAccounts => {
                console.log('Bank accounts loaded:', bankAccounts);
                const container = document.getElementById('bankAccountsList');
                
                if (!container) {
                    console.error('Bank accounts list container not found');
                    return;
                }
                
                container.innerHTML = '';
                
                // Find default account
                let defaultAccount = null;
                if (bankAccounts && bankAccounts.length > 0) {
                    defaultAccount = bankAccounts.find(acc => acc.isDefault === true);
                    // If no default, use first account
                    if (!defaultAccount) {
                        defaultAccount = bankAccounts[0];
                    }
                }
                
                if (defaultAccount) {
                    const accountDiv = document.createElement('div');
                    accountDiv.className = 'bg-blue-50 p-6 rounded';
                    
                    let qrHtml = '';
                    if (defaultAccount.qrCodeImagePath && defaultAccount.qrCodeImagePath !== 'null') {
                        qrHtml = `<div class="mt-4 text-center"><img src="${defaultAccount.qrCodeImagePath}" alt="QR" class="w-48 h-48 border border-gray-300 rounded inline-block cursor-pointer hover:opacity-80 transition" onclick="openQRCodeLightbox('${defaultAccount.qrCodeImagePath}')"></div>`;
                    }
                    
                    accountDiv.innerHTML = `
                        <div class="grid grid-cols-2 gap-4 mb-4">
                            <div>
                                <p class="text-sm text-gray-600">Ngân hàng</p>
                                <p class="text-lg font-semibold text-gray-900">${defaultAccount.bankName}</p>
                            </div>
                            <div>
                                <p class="text-sm text-gray-600">Số tài khoản</p>
                                <p class="text-lg font-semibold text-gray-900">${defaultAccount.accountNumber}</p>
                            </div>
                            <div class="col-span-2">
                                <p class="text-sm text-gray-600">Chủ tài khoản</p>
                                <p class="text-lg font-semibold text-gray-900">${defaultAccount.accountHolderName}</p>
                            </div>
                        </div>
                        ${qrHtml}
                    `;
                    container.appendChild(accountDiv);
                } else {
                    container.innerHTML = '<p class="text-center text-gray-500 py-8"><i class="fas fa-inbox text-3xl mb-2 block"></i>Khách hàng chưa có tài khoản ngân hàng nào</p>';
                }
            })
            .catch(error => {
                console.error('Error loading bank account:', error);
                const container = document.getElementById('bankAccountsList');
                if (container) {
                    container.innerHTML = '<p class="text-center text-red-500 py-4">Lỗi khi tải tài khoản: ' + error.message + '</p>';
                }
            });
    };


})(); // IIFE (Immediately Invoked Function Expression)