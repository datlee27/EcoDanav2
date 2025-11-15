// Admin Withdrawal Requests Tab JavaScript

let currentWithdrawalData = {};

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// Function to get CSRF token from meta tags
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    return { token, header };
}

function showWithdrawalDetailModal(button) {
    const data = {
        requestId: button.dataset.requestId,
        amount: parseFloat(button.dataset.amount),
        ownerId: button.dataset.ownerId,
        ownerName: button.dataset.ownerName,
        ownerEmail: button.dataset.ownerEmail,
        ownerPhone: button.dataset.ownerPhone,
        ownerBalance: parseFloat(button.dataset.ownerBalance),
        status: button.dataset.status,
        adminNotes: button.dataset.adminNotes,
        requestDate: button.dataset.requestDate
    };

    currentWithdrawalData = data;

    console.log('Withdrawal data:', data);
    console.log('Status value:', data.status);
    console.log('Status type:', typeof data.status);

    // Set modal content
    document.getElementById('modalRequestId').textContent = 'ID: ' + data.requestId;
    document.getElementById('modalAmount').textContent = formatCurrency(data.amount);
    document.getElementById('modalOwnerBalance').textContent = formatCurrency(data.ownerBalance);
    document.getElementById('modalRequestDate').textContent = data.requestDate;
    document.getElementById('modalOwnerName').textContent = data.ownerName;
    document.getElementById('modalOwnerEmail').textContent = data.ownerEmail;
    document.getElementById('modalOwnerPhone').textContent = data.ownerPhone;

    // Set status badge - normalize status to uppercase
    const statusBadge = document.getElementById('statusBadge');
    const normalizedStatus = data.status ? data.status.toString().toUpperCase() : '';
    
    if (normalizedStatus === 'PENDING') {
        statusBadge.textContent = 'Chờ duyệt';
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold bg-yellow-100 text-yellow-800';
    } else if (normalizedStatus === 'APPROVED') {
        statusBadge.textContent = 'Đã duyệt';
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold bg-green-100 text-green-800';
    } else if (normalizedStatus === 'REJECTED') {
        statusBadge.textContent = 'Từ chối';
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold bg-red-100 text-red-800';
    } else {
        statusBadge.textContent = 'Không xác định (' + data.status + ')';
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-semibold bg-gray-100 text-gray-800';
    }

    // Show/hide sections based on status
    const adminNotesSection = document.getElementById('adminNotesSection');
    const rejectionReasonSection = document.getElementById('rejectionReasonSection');
    const approvalNotesSection = document.getElementById('approvalNotesSection');
    const actionButtons = document.getElementById('actionButtons');

    if (data.adminNotes && data.adminNotes !== 'null') {
        document.getElementById('modalAdminNotes').textContent = data.adminNotes;
        adminNotesSection.classList.remove('hidden');
    } else {
        adminNotesSection.classList.add('hidden');
    }

    // Clear action buttons
    actionButtons.innerHTML = '';

    console.log('Checking status for buttons. Normalized:', normalizedStatus);

    if (normalizedStatus === 'PENDING') {
        // Show approve and reject buttons
        rejectionReasonSection.classList.add('hidden');
        approvalNotesSection.classList.add('hidden');
        
        console.log('Status is PENDING - showing action buttons');
        actionButtons.innerHTML = `
            <button onclick="prepareApproval()" class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
                <i class="fas fa-check mr-2"></i>Duyệt
            </button>
            <button onclick="prepareRejection()" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
                <i class="fas fa-times mr-2"></i>Từ chối
            </button>
        `;
    } else {
        console.log('Status is NOT PENDING - no action buttons shown');
    }

    // Show modal
    document.getElementById('withdrawalDetailModal').classList.remove('hidden');
}

function closeWithdrawalDetailModal() {
    document.getElementById('withdrawalDetailModal').classList.add('hidden');
}

function prepareApproval() {
    document.getElementById('rejectionReasonSection').classList.add('hidden');
    document.getElementById('approvalNotesSection').classList.remove('hidden');
    
    const actionButtons = document.getElementById('actionButtons');
    actionButtons.innerHTML = `
        <button onclick="confirmApproval()" class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
            <i class="fas fa-check mr-2"></i>Xác nhận duyệt
        </button>
        <button onclick="cancelApproval()" class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400">
            <i class="fas fa-arrow-left mr-2"></i>Quay lại
        </button>
    `;
}

function prepareRejection() {
    document.getElementById('approvalNotesSection').classList.add('hidden');
    document.getElementById('rejectionReasonSection').classList.remove('hidden');
    
    const actionButtons = document.getElementById('actionButtons');
    actionButtons.innerHTML = `
        <button onclick="confirmRejection()" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
            <i class="fas fa-times mr-2"></i>Xác nhận từ chối
        </button>
        <button onclick="cancelRejection()" class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400">
            <i class="fas fa-arrow-left mr-2"></i>Quay lại
        </button>
    `;
}

function cancelApproval() {
    // Re-show the modal with original buttons
    const button = document.querySelector(`[data-request-id="${currentWithdrawalData.requestId}"]`);
    if (button) {
        showWithdrawalDetailModal(button);
    }
}

function cancelRejection() {
    // Re-show the modal with original buttons
    const button = document.querySelector(`[data-request-id="${currentWithdrawalData.requestId}"]`);
    if (button) {
        showWithdrawalDetailModal(button);
    }
}

function confirmApproval() {
    const adminNotes = document.getElementById('approvalNotes').value || 'Approved';
    const { token, header } = getCsrfToken();

    fetch(`/admin/withdrawals/${currentWithdrawalData.requestId}/approve`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ adminNotes: adminNotes })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeWithdrawalDetailModal();
            location.reload();
        } else {
            alert('Lỗi: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error approving withdrawal request:', error);
        alert('Đã xảy ra lỗi khi duyệt yêu cầu rút tiền.');
    });
}

function confirmRejection() {
    const rejectionReason = document.getElementById('rejectionReason').value;
    
    if (!rejectionReason || rejectionReason.trim() === '') {
        alert('Lý do từ chối không được để trống.');
        return;
    }

    const { token, header } = getCsrfToken();

    fetch(`/admin/withdrawals/${currentWithdrawalData.requestId}/reject`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ adminNotes: rejectionReason })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeWithdrawalDetailModal();
            location.reload();
        } else {
            alert('Lỗi: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error rejecting withdrawal request:', error);
        alert('Đã xảy ra lỗi khi từ chối yêu cầu rút tiền.');
    });
}

function showOwnerBankAccountModal() {
    const ownerId = currentWithdrawalData.ownerId;
    
    // Show modal
    document.getElementById('ownerBankAccountModal').classList.remove('hidden');
    
    // Load bank accounts
    fetch(`/customer/bank-accounts/api/list?userId=${ownerId}`)
        .then(response => response.json())
        .then(data => {
            const bankAccountsList = document.getElementById('ownerBankAccountsList');
            
            if (data.success && data.bankAccounts && data.bankAccounts.length > 0) {
                bankAccountsList.innerHTML = '';
                
                data.bankAccounts.forEach(account => {
                    const accountHtml = `
                        <div class="border border-gray-200 rounded-lg p-4 ${account.default ? 'border-blue-500 bg-blue-50' : ''}">
                            <div class="flex justify-between items-start mb-3">
                                <div>
                                    <h5 class="font-semibold text-gray-900">${account.bankName}</h5>
                                    ${account.default ? '<span class="text-xs bg-blue-500 text-white px-2 py-1 rounded">Mặc định</span>' : ''}
                                </div>
                            </div>
                            <div class="space-y-2">
                                <div class="flex justify-between">
                                    <span class="text-sm text-gray-600">Số tài khoản:</span>
                                    <span class="text-sm font-semibold text-gray-900">${account.accountNumber}</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-sm text-gray-600">Chủ tài khoản:</span>
                                    <span class="text-sm font-semibold text-gray-900">${account.accountHolderName}</span>
                                </div>
                                ${account.qrCodeImagePath ? `
                                    <div class="mt-3">
                                        <p class="text-sm text-gray-600 mb-2">Mã QR:</p>
                                        <img src="${account.qrCodeImagePath}" alt="QR Code" class="w-40 h-40 object-contain border border-gray-300 rounded">
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                    `;
                    bankAccountsList.innerHTML += accountHtml;
                });
            } else {
                bankAccountsList.innerHTML = `
                    <div class="text-center text-gray-500 py-8">
                        <i class="fas fa-exclamation-circle text-3xl mb-3"></i>
                        <p>Chủ xe chưa có tài khoản ngân hàng nào.</p>
                    </div>
                `;
            }
        })
        .catch(error => {
            console.error('Error loading bank accounts:', error);
            document.getElementById('ownerBankAccountsList').innerHTML = `
                <div class="text-center text-red-500 py-8">
                    <i class="fas fa-exclamation-triangle text-3xl mb-3"></i>
                    <p>Lỗi khi tải thông tin ngân hàng.</p>
                </div>
            `;
        });
}

function closeOwnerBankAccountModal() {
    document.getElementById('ownerBankAccountModal').classList.add('hidden');
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin withdrawal requests tab JavaScript loaded');
});
