// DEPRECATED: This file has been replaced with server-side rendering
// See: /admin/refund-requests (RefundRequestController.java)
// The refund requests page now uses Thymeleaf templates instead of JavaScript
// 
// New implementation:
// - RefundRequestController.java (server-side controller)
// - refund-requests-list.html (server-side rendered list page)
// - refund-request-detail.html (server-side rendered detail page)
//
// This file is kept for backward compatibility but is no longer used.
        // Try to load from /pending first, if empty try /all
        fetch('/admin/api/refund-requests')
            .then(function(response) {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    console.error('Response not ok:', response.status, response.statusText);
                    throw new Error('Failed to load refund requests: ' + response.status);
                }
                return response.json();
            })
            .then(function(data) {
                console.log('Received refund requests:', data);
                console.log('Data length:', data ? data.length : 0);
                refundRequestsData = data || [];
                filteredData = refundRequestsData;
                renderRefundRequests();
                updateStatistics();
            })
            .catch(function(error) {
                console.error('Error loading refund requests:', error);
                console.error('Error stack:', error.stack);
                showError();
            });
    }
    
    function loadStatistics() {
        fetch('/admin/api/refund-requests/statistics')
            .then(function(response) {
                if (!response.ok) throw new Error('Failed to load statistics');
                return response.json();
            })
            .then(function(stats) {
                updateStatisticsCards(stats);
            })
            .catch(function(error) {
                console.error('Error loading statistics:', error);
            });
    }
    
    function updateStatisticsCards(stats) {
        var totalEl = document.getElementById('refundsTotalCount');
        var pendingEl = document.getElementById('refundsPendingCount');
        var approvedEl = document.getElementById('refundsApprovedCount');
        var rejectedEl = document.getElementById('refundsRejectedCount');
        var urgentEl = document.getElementById('refundsUrgentCount');
        var amountEl = document.getElementById('refundsTotalAmount');
        
        if (totalEl) totalEl.textContent = stats.total || 0;
        if (pendingEl) pendingEl.textContent = stats.pending || 0;
        if (approvedEl) approvedEl.textContent = stats.approved || 0;
        if (rejectedEl) rejectedEl.textContent = stats.rejected || 0;
        if (urgentEl) urgentEl.textContent = stats.urgent || 0;
        if (amountEl) amountEl.textContent = (stats.totalPendingAmount || 0).toLocaleString('vi-VN') + ' VND';
    }
    
    function renderRefundRequests() {
        var tbody = document.getElementById('refundRequestsTableBody');
        if (!tbody) {
            console.error('Refund requests table body not found!');
            return;
        }
        
        if (filteredData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" class="px-6 py-12 text-center text-gray-500">No refund requests found</td></tr>';
            return;
        }
        
        var html = '';
        filteredData.forEach(function(refund) {
            html += createRefundRequestRow(refund);
        });
        tbody.innerHTML = html;
    }
    
    function createRefundRequestRow(refund) {
        var statusClass = getStatusClass(refund.status);
        var urgencyClass = refund.isWithinTwoHours ? 'bg-red-100 text-red-800' : '';
        var html = '<tr class="hover:bg-gray-50">';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-medium text-gray-900">' + escapeHtml(refund.refundRequestId.substring(0, 8) + '...') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-900">' + escapeHtml(refund.bookingCode || '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        html += '<div class="text-sm font-medium text-gray-900">' + escapeHtml(refund.customerName || '-') + '</div>';
        html += '<div class="text-xs text-gray-500">' + escapeHtml(refund.customerEmail || '-') + '</div>';
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-600">' + escapeHtml(refund.customerPhone || '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-semibold text-red-600">' + (refund.refundAmount ? refund.refundAmount.toLocaleString('vi-VN') + ' VND' : '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        html += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ' + statusClass + '">' + escapeHtml(refund.status) + '</span>';
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        if (refund.isWithinTwoHours) {
            html += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800"><i class="fas fa-exclamation-circle mr-1"></i>Urgent (< 2h)</span>';
        } else {
            html += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">Normal</span>';
        }
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">' + (refund.createdDate ? formatDateTime(refund.createdDate) : '-') + '</td>';
        
        html += '<td class="sticky right-0 bg-white px-6 py-4 whitespace-nowrap text-right text-sm font-medium shadow-lg">';
        html += '<button class="text-indigo-600 hover:text-indigo-900 mr-2" onclick="viewRefundRequestDetail(\'' + refund.refundRequestId + '\')" title="View Details"><i class="fas fa-eye"></i></button>';
        html += '<button class="text-blue-600 hover:text-blue-900 mr-2" onclick="viewBankInfo(\'' + refund.refundRequestId + '\')" title="Bank Info"><i class="fas fa-university"></i></button>';
        
        if (refund.status === 'Pending') {
            html += '<button class="text-green-600 hover:text-green-900 mr-2" onclick="approveRefundRequest(\'' + refund.refundRequestId + '\')" title="Approve"><i class="fas fa-check"></i></button>';
            html += '<button class="text-red-600 hover:text-red-900" onclick="rejectRefundRequest(\'' + refund.refundRequestId + '\')" title="Reject"><i class="fas fa-times"></i></button>';
        }
        html += '</td>';
        
        html += '</tr>';
        return html;
    }
    
    function updateStatistics() {
        var total = refundRequestsData.length;
        var pending = refundRequestsData.filter(function(r) { return r.status === 'Pending'; }).length;
        var approved = refundRequestsData.filter(function(r) { return r.status === 'Approved'; }).length;
        var rejected = refundRequestsData.filter(function(r) { return r.status === 'Rejected'; }).length;
        var urgent = refundRequestsData.filter(function(r) { return r.isWithinTwoHours && r.status === 'Pending'; }).length;
        
        var totalAmount = refundRequestsData
            .filter(function(r) { return r.status === 'Pending'; })
            .reduce(function(sum, r) { return sum + (r.refundAmount || 0); }, 0);
        
        updateStatisticsCards({
            total: total,
            pending: pending,
            approved: approved,
            rejected: rejected,
            urgent: urgent,
            totalPendingAmount: totalAmount
        });
    }
    
    function getStatusClass(status) {
        var classes = {
            'Pending': 'bg-yellow-100 text-yellow-800',
            'Approved': 'bg-green-100 text-green-800',
            'Rejected': 'bg-red-100 text-red-800',
            'Completed': 'bg-blue-100 text-blue-800'
        };
        return classes[status] || 'bg-gray-100 text-gray-800';
    }
    
    function formatDateTime(dateString) {
        if (!dateString) return '-';
        var date = new Date(dateString);
        return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }
    
    function escapeHtml(text) {
        if (!text) return '';
        var map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'};
        return text.toString().replace(/[&<>"']/g, function(m) { return map[m]; });
    }
    
    function showError() {
        var tbody = document.getElementById('refundRequestsTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="10" class="px-6 py-12 text-center text-red-500">Error loading refund requests. Please try again.</td></tr>';
        }
    }
    
    // Event listeners
    var statusFilter = document.getElementById('refundsStatusFilter');
    var searchInput = document.getElementById('refundsSearchInput');
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
    
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            applyFilters();
        });
    }
    
    function applyFilters() {
        var status = statusFilter ? statusFilter.value : '';
        var search = searchInput ? searchInput.value.toLowerCase() : '';
        
        filteredData = refundRequestsData.filter(function(r) {
            var matchStatus = !status || r.status === status;
            var matchSearch = !search || 
                (r.refundRequestId && r.refundRequestId.toLowerCase().includes(search)) ||
                (r.bookingCode && r.bookingCode.toLowerCase().includes(search)) ||
                (r.customerName && r.customerName.toLowerCase().includes(search)) ||
                (r.customerEmail && r.customerEmail.toLowerCase().includes(search));
            
            return matchStatus && matchSearch;
        });
        
        renderRefundRequests();
    }
    
    // Load refund requests when tab is shown
    window.addEventListener('tabChanged', function(e) {
        if (e.detail === 'refund-requests') {
            console.log('Refund Requests tab activated, loading...');
            loadRefundRequests();
            loadStatistics();
        }
    });
    
    // Global functions for buttons
    window.viewRefundRequestDetail = function(refundRequestId) {
        fetch('/admin/api/refund-requests/' + refundRequestId)
            .then(function(response) {
                if (!response.ok) throw new Error('Failed to load refund request');
                return response.json();
            })
            .then(function(refund) {
                showRefundRequestDetailModal(refund);
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('Failed to load refund request details');
            });
    };
    
    function showRefundRequestDetailModal(refund) {
        var modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center p-4';
        modal.id = 'refundDetailModal';
        modal.onclick = function(e) {
            if (e.target === modal) closeRefundDetailModal();
        };
        
        var content = '<div class="relative bg-white rounded-xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">';
        
        // Header
        content += '<div class="bg-gradient-to-r from-orange-600 to-red-600 p-6 rounded-t-xl">';
        content += '<div class="flex justify-between items-center">';
        content += '<div>';
        content += '<h3 class="text-2xl font-bold text-white">Refund Request Details</h3>';
        content += '<p class="text-orange-100 text-sm mt-1">ID: ' + escapeHtml(refund.refundRequestId) + '</p>';
        content += '</div>';
        content += '<button onclick="closeRefundDetailModal()" class="text-white hover:text-gray-200 transition-colors">';
        content += '<i class="fas fa-times text-2xl"></i></button>';
        content += '</div></div>';
        
        // Status badges
        content += '<div class="px-6 py-4 border-b border-gray-200 bg-gray-50 flex space-x-3">';
        content += '<span class="px-4 py-2 rounded-full text-sm font-semibold ' + getStatusClass(refund.status) + '">';
        content += '<i class="fas fa-circle text-xs mr-2"></i>' + refund.status + '</span>';
        if (refund.isWithinTwoHours) {
            content += '<span class="px-4 py-2 rounded-full text-sm font-semibold bg-red-100 text-red-800">';
            content += '<i class="fas fa-exclamation-circle text-xs mr-2"></i>Urgent (< 2 hours)</span>';
        }
        content += '</div>';
        
        // Content
        content += '<div class="p-6 grid grid-cols-2 gap-4">';
        
        // Refund Amount
        content += createDetailCard('Refund Amount', '<span class="text-red-600 font-bold text-lg">' + (refund.refundAmount ? refund.refundAmount.toLocaleString('vi-VN') + ' VND' : '-') + '</span>', 'fa-money-bill-wave');
        
        // Booking Code
        content += createDetailCard('Booking Code', refund.bookingCode || '-', 'fa-ticket-alt');
        
        // Customer Name
        content += createDetailCard('Customer Name', refund.customerName || '-', 'fa-user');
        
        // Customer Email
        content += createDetailCard('Email', refund.customerEmail || '-', 'fa-envelope');
        
        // Customer Phone
        content += createDetailCard('Phone', refund.customerPhone || '-', 'fa-phone');
        
        // Created Date
        content += createDetailCard('Request Date', refund.createdDate ? formatDateTime(refund.createdDate) : '-', 'fa-calendar');
        
        // Bank Account Info
        content += '<div class="col-span-2 bg-blue-50 border-l-4 border-blue-500 p-4 rounded">';
        content += '<h5 class="font-semibold text-blue-900 mb-3"><i class="fas fa-university mr-2"></i>Bank Account Information</h5>';
        content += '<div class="grid grid-cols-2 gap-3 text-sm">';
        content += '<div><span class="text-blue-700 font-medium">Bank:</span> <span class="text-blue-900">' + escapeHtml(refund.bankName || '-') + '</span></div>';
        content += '<div><span class="text-blue-700 font-medium">Account Number:</span> <span class="text-blue-900">' + escapeHtml(refund.accountNumber || '-') + '</span></div>';
        content += '<div class="col-span-2"><span class="text-blue-700 font-medium">Account Holder:</span> <span class="text-blue-900">' + escapeHtml(refund.accountHolder || '-') + '</span></div>';
        content += '</div></div>';
        
        // Cancel Reason
        if (refund.cancelReason) {
            content += '<div class="col-span-2 bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded">';
            content += '<h5 class="font-semibold text-yellow-900 mb-2"><i class="fas fa-comment mr-2"></i>Cancellation Reason</h5>';
            content += '<p class="text-yellow-800">' + escapeHtml(refund.cancelReason) + '</p></div>';
        }
        
        // Admin Notes
        if (refund.adminNotes) {
            content += '<div class="col-span-2 bg-green-50 border-l-4 border-green-500 p-4 rounded">';
            content += '<h5 class="font-semibold text-green-900 mb-2"><i class="fas fa-sticky-note mr-2"></i>Admin Notes</h5>';
            content += '<p class="text-green-800">' + escapeHtml(refund.adminNotes) + '</p></div>';
        }
        
        content += '</div>';
        
        // Footer with Action Buttons
        content += '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-between">';
        
        if (refund.status === 'Pending') {
            content += '<div class="flex space-x-3">';
            content += '<button onclick="approveRefundRequest(\'' + refund.refundRequestId + '\')" class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">';
            content += '<i class="fas fa-check mr-2"></i>Approve</button>';
            content += '<button onclick="rejectRefundRequest(\'' + refund.refundRequestId + '\')" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">';
            content += '<i class="fas fa-times mr-2"></i>Reject</button>';
            content += '</div>';
        }
        
        content += '<button onclick="closeRefundDetailModal()" class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors">';
        content += '<i class="fas fa-times mr-2"></i>Close</button>';
        content += '</div></div>';
        
        modal.innerHTML = content;
        document.body.appendChild(modal);
    }
    
    function createDetailCard(label, value, icon) {
        return '<div class="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">' +
               '<div class="flex items-start">' +
               '<div class="flex-shrink-0 w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center mr-3">' +
               '<i class="fas ' + icon + ' text-gray-600"></i></div>' +
               '<div class="flex-1 min-w-0">' +
               '<p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">' + escapeHtml(label) + '</p>' +
               '<p class="text-sm font-semibold text-gray-900 break-words">' + value + '</p>' +
               '</div></div></div>';
    }
    
    window.closeRefundDetailModal = function() {
        var modal = document.getElementById('refundDetailModal');
        if (modal) modal.remove();
    };
    
    window.viewBankInfo = function(refundRequestId) {
        fetch('/admin/api/refund-requests/' + refundRequestId)
            .then(function(response) {
                if (!response.ok) throw new Error('Failed to load refund request');
                return response.json();
            })
            .then(function(refund) {
                showBankInfoModal(refund);
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('Failed to load bank information');
            });
    };
    
    function showBankInfoModal(refund) {
        var modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center p-4';
        modal.id = 'bankInfoModal';
        modal.onclick = function(e) {
            if (e.target === modal) closeBankInfoModal();
        };
        
        var content = '<div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl" onclick="event.stopPropagation()">';
        
        // Header
        content += '<div class="bg-gradient-to-r from-blue-600 to-blue-700 p-6 rounded-t-xl">';
        content += '<div class="flex justify-between items-center">';
        content += '<div>';
        content += '<h3 class="text-2xl font-bold text-white"><i class="fas fa-university mr-2"></i>Bank Account Information</h3>';
        content += '<p class="text-blue-100 text-sm mt-1">Booking: ' + escapeHtml(refund.bookingCode) + ' | Customer: ' + escapeHtml(refund.customerName) + '</p>';
        content += '</div>';
        content += '<button onclick="closeBankInfoModal()" class="text-white hover:text-gray-200 transition-colors">';
        content += '<i class="fas fa-times text-2xl"></i></button>';
        content += '</div></div>';
        
        // Content
        content += '<div class="p-8">';
        
        // Bank Details with QR Code
        content += '<div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">';
        
        // Bank Info
        content += '<div class="bg-gradient-to-br from-blue-50 to-blue-100 border-2 border-blue-300 rounded-lg p-6">';
        content += '<h5 class="font-semibold text-blue-900 mb-4"><i class="fas fa-university mr-2"></i>Thông Tin Ngân Hàng</h5>';
        content += '<div class="space-y-3">';
        
        content += '<div>';
        content += '<p class="text-xs font-medium text-blue-600 uppercase tracking-wider mb-1">Ngân Hàng</p>';
        content += '<p class="text-lg font-bold text-blue-900">' + escapeHtml(refund.bankName || '-') + '</p>';
        content += '</div>';
        
        content += '<div>';
        content += '<p class="text-xs font-medium text-blue-600 uppercase tracking-wider mb-1">Mã Ngân Hàng</p>';
        content += '<p class="text-lg font-bold text-blue-900">' + escapeHtml(refund.bankCode || '-') + '</p>';
        content += '</div>';
        
        content += '<div>';
        content += '<p class="text-xs font-medium text-blue-600 uppercase tracking-wider mb-1">Số Tài Khoản</p>';
        content += '<p class="text-xl font-bold text-blue-900 font-mono break-all">' + escapeHtml(refund.accountNumber || '-') + '</p>';
        content += '</div>';
        
        content += '<div>';
        content += '<p class="text-xs font-medium text-blue-600 uppercase tracking-wider mb-1">Chủ Tài Khoản</p>';
        content += '<p class="text-lg font-bold text-blue-900">' + escapeHtml(refund.accountHolder || '-') + '</p>';
        content += '</div>';
        
        content += '</div></div>';
        
        // QR Code
        content += '<div class="bg-white border-2 border-blue-300 rounded-lg p-6 flex flex-col items-center justify-center">';
        content += '<h5 class="font-semibold text-blue-900 mb-4 w-full text-center"><i class="fas fa-qrcode mr-2"></i>Mã QR Ngân Hàng</h5>';
        if (refund.qrCodeImagePath) {
            content += '<img src="' + escapeHtml(refund.qrCodeImagePath) + '" alt="QR Code" class="w-48 h-48 object-contain rounded-lg border border-gray-300">';
        } else {
            content += '<div class="w-48 h-48 bg-gray-100 rounded-lg flex items-center justify-center border-2 border-dashed border-gray-300">';
            content += '<p class="text-gray-500 text-center"><i class="fas fa-image text-4xl mb-2 block"></i>Không có ảnh QR</p>';
            content += '</div>';
        }
        content += '</div>';
        
        content += '</div>';
        
        // Refund Amount
        content += '<div class="bg-gradient-to-br from-red-50 to-red-100 border-2 border-red-300 rounded-lg p-6 mb-6">';
        content += '<p class="text-sm font-medium text-red-600 uppercase tracking-wider mb-2">Refund Amount</p>';
        content += '<p class="text-3xl font-bold text-red-600">' + (refund.refundAmount ? refund.refundAmount.toLocaleString('vi-VN') + ' VND' : '-') + '</p>';
        content += '</div>';
        
        // Customer Info
        content += '<div class="bg-gray-50 border border-gray-300 rounded-lg p-6 mb-6">';
        content += '<h4 class="font-semibold text-gray-900 mb-4"><i class="fas fa-user mr-2"></i>Customer Information</h4>';
        content += '<div class="grid grid-cols-2 gap-4 text-sm">';
        content += '<div><span class="font-medium text-gray-700">Name:</span> <span class="text-gray-900">' + escapeHtml(refund.customerName || '-') + '</span></div>';
        content += '<div><span class="font-medium text-gray-700">Email:</span> <span class="text-gray-900">' + escapeHtml(refund.customerEmail || '-') + '</span></div>';
        content += '<div><span class="font-medium text-gray-700">Phone:</span> <span class="text-gray-900">' + escapeHtml(refund.customerPhone || '-') + '</span></div>';
        content += '<div><span class="font-medium text-gray-700">Booking Code:</span> <span class="text-gray-900">' + escapeHtml(refund.bookingCode || '-') + '</span></div>';
        content += '</div></div>';
        
        // Status
        content += '<div class="bg-yellow-50 border border-yellow-300 rounded-lg p-6 mb-6">';
        content += '<h4 class="font-semibold text-gray-900 mb-3"><i class="fas fa-info-circle mr-2"></i>Request Status</h4>';
        content += '<div class="flex items-center space-x-3">';
        content += '<span class="px-4 py-2 rounded-full text-sm font-semibold ' + getStatusClass(refund.status) + '">';
        content += '<i class="fas fa-circle text-xs mr-2"></i>' + refund.status + '</span>';
        if (refund.isWithinTwoHours) {
            content += '<span class="px-4 py-2 rounded-full text-sm font-semibold bg-red-100 text-red-800">';
            content += '<i class="fas fa-exclamation-circle text-xs mr-2"></i>Urgent (< 2 hours)</span>';
        }
        content += '</div></div>';
        
        content += '</div>';
        
        // Footer with Action Buttons
        content += '<div class="px-8 py-6 bg-gray-50 rounded-b-xl flex justify-between border-t border-gray-200">';
        
        if (refund.status === 'Pending') {
            content += '<div class="flex space-x-3">';
            content += '<button onclick="approveRefundRequest(\'' + refund.refundRequestId + '\')" class="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-semibold flex items-center">';
            content += '<i class="fas fa-check mr-2"></i>Đã hoàn tiền</button>';
            content += '<button onclick="rejectRefundRequest(\'' + refund.refundRequestId + '\')" class="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-semibold flex items-center">';
            content += '<i class="fas fa-times mr-2"></i>Từ chối</button>';
            content += '</div>';
        } else {
            content += '<div class="flex-1"></div>';
        }
        
        content += '<button onclick="closeBankInfoModal()" class="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors font-semibold flex items-center">';
        content += '<i class="fas fa-times mr-2"></i>Đóng</button>';
        content += '</div></div>';
        
        modal.innerHTML = content;
        document.body.appendChild(modal);
    }
    
    window.closeBankInfoModal = function() {
        var modal = document.getElementById('bankInfoModal');
        if (modal) modal.remove();
    };
    
    window.approveRefundRequest = function(refundRequestId) {
        var notes = prompt('Enter admin notes (optional):', '');
        if (notes === null) return; // User cancelled
        
        var url = '/admin/api/refund-requests/' + refundRequestId + '/approve';
        if (notes) {
            url += '?adminNotes=' + encodeURIComponent(notes);
        }
        
        fetch(url, {
            method: 'POST'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to approve refund request');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Refund request approved successfully!');
                closeRefundDetailModal();
                loadRefundRequests();
                loadStatistics();
            } else {
                throw new Error(result.message || 'Approval failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed to approve refund request: ' + error.message);
        });
    };
    
    window.rejectRefundRequest = function(refundRequestId) {
        var notes = prompt('Enter rejection reason (required):', '');
        if (notes === null || notes.trim() === '') {
            alert('Rejection reason is required');
            return;
        }
        
        var url = '/admin/api/refund-requests/' + refundRequestId + '/reject?adminNotes=' + encodeURIComponent(notes);
        
        fetch(url, {
            method: 'POST'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to reject refund request');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Refund request rejected successfully!');
                closeRefundDetailModal();
                loadRefundRequests();
                loadStatistics();
            } else {
                throw new Error(result.message || 'Rejection failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed to reject refund request: ' + error.message);
        });
    };
    
    // Initial load if refund requests tab is active
    if (document.getElementById('refund-requests') && document.getElementById('refund-requests').classList.contains('active')) {
        loadRefundRequests();
        loadStatistics();
    }
})();
