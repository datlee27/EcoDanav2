(function() {
    'use strict';
    console.log('Payments tab script loaded');
    
    var paymentsData = [];
    var filteredData = [];
    
    function loadPayments() {
        console.log('Loading payments...');
        fetch('/admin/api/payments')
            .then(function(response) {
                console.log('Response status:', response.status);
                if (!response.ok) throw new Error('Failed to load payments');
                return response.json();
            })
            .then(function(data) {
                console.log('Received payments:', data);
                paymentsData = data || [];
                filteredData = paymentsData;
                renderPayments();
                updateStatistics();
            })
            .catch(function(error) {
                console.error('Error loading payments:', error);
                showError();
            });
    }
    
    function loadStatistics() {
        fetch('/admin/api/payments/statistics')
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
        var totalEl = document.getElementById('paymentsTotalCount');
        var pendingEl = document.getElementById('paymentsPendingCount');
        var completedEl = document.getElementById('paymentsCompletedCount');
        var failedEl = document.getElementById('paymentsFailedCount');
        var refundedEl = document.getElementById('paymentsRefundedCount');
        var revenueEl = document.getElementById('paymentsTotalRevenue');
        
        if (totalEl) totalEl.textContent = stats.total || 0;
        if (pendingEl) pendingEl.textContent = stats.pending || 0;
        if (completedEl) completedEl.textContent = stats.completed || 0;
        if (failedEl) failedEl.textContent = stats.failed || 0;
        if (refundedEl) refundedEl.textContent = stats.refunded || 0;
        if (revenueEl) revenueEl.textContent = (stats.totalRevenue || 0).toLocaleString('vi-VN') + ' VND';
    }
    
    function renderPayments() {
        var tbody = document.getElementById('paymentsTableBody');
        if (!tbody) {
            console.error('Payments table body not found!');
            return;
        }
        
        if (filteredData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="px-6 py-12 text-center text-gray-500">No payments found</td></tr>';
            return;
        }
        
        var html = '';
        filteredData.forEach(function(payment) {
            html += createPaymentRow(payment);
        });
        tbody.innerHTML = html;
    }
    
    function createPaymentRow(payment) {
        var statusClass = getStatusClass(payment.paymentStatus);
        var typeClass = getTypeClass(payment.paymentType);
        var html = '<tr class="hover:bg-gray-50">';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-medium text-gray-900">' + escapeHtml(payment.paymentId.substring(0, 8) + '...') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-900">' + escapeHtml(payment.bookingCode || '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        html += '<div class="text-sm font-medium text-gray-900">' + escapeHtml(payment.userName || '-') + '</div>';
        html += '<div class="text-xs text-gray-500">' + escapeHtml(payment.userEmail || '-') + '</div>';
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-semibold text-green-600">' + (payment.amount ? payment.amount.toLocaleString('vi-VN') + ' VND' : '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-900">' + escapeHtml(payment.paymentMethod || '-') + '</div></td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        html += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ' + typeClass + '">' + escapeHtml(payment.paymentType) + '</span>';
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap">';
        html += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ' + statusClass + '">' + escapeHtml(payment.paymentStatus) + '</span>';
        html += '</td>';
        
        html += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">' + (payment.paymentDate ? formatDateTime(payment.paymentDate) : 'Not paid') + '</td>';
        
        html += '<td class="sticky right-0 bg-white px-6 py-4 whitespace-nowrap text-right text-sm font-medium shadow-lg">';
        html += '<button class="text-indigo-600 hover:text-indigo-900 mr-2" onclick="viewPaymentDetail(\'' + payment.paymentId + '\')" title="View"><i class="fas fa-eye"></i></button>';
        html += '<button class="text-blue-600 hover:text-blue-900 mr-2" onclick="editPayment(\'' + payment.paymentId + '\')" title="Edit"><i class="fas fa-edit"></i></button>';
        
        if (payment.paymentStatus === 'Pending') {
            html += '<button class="text-green-600 hover:text-green-900 mr-2" onclick="markAsCompleted(\'' + payment.paymentId + '\')" title="Mark Completed"><i class="fas fa-check"></i></button>';
            html += '<button class="text-red-600 hover:text-red-900 mr-2" onclick="markAsFailed(\'' + payment.paymentId + '\')" title="Mark Failed"><i class="fas fa-times"></i></button>';
        }
        if (payment.paymentStatus === 'Completed') {
            html += '<button class="text-purple-600 hover:text-purple-900 mr-2" onclick="processRefund(\'' + payment.paymentId + '\')" title="Refund"><i class="fas fa-undo"></i></button>';
        }
        
        html += '<button class="text-red-600 hover:text-red-900" onclick="deletePayment(\'' + payment.paymentId + '\')" title="Delete"><i class="fas fa-trash"></i></button>';
        html += '</td>';
        
        html += '</tr>';
        return html;
    }
    
    function updateStatistics() {
        var total = paymentsData.length;
        var pending = paymentsData.filter(function(p) { return p.paymentStatus === 'Pending'; }).length;
        var completed = paymentsData.filter(function(p) { return p.paymentStatus === 'Completed'; }).length;
        var failed = paymentsData.filter(function(p) { return p.paymentStatus === 'Failed'; }).length;
        var refunded = paymentsData.filter(function(p) { return p.paymentStatus === 'Refunded'; }).length;
        
        var totalRevenue = paymentsData
            .filter(function(p) { return p.paymentStatus === 'Completed'; })
            .reduce(function(sum, p) { return sum + (p.amount || 0); }, 0);
        
        updateStatisticsCards({
            total: total,
            pending: pending,
            completed: completed,
            failed: failed,
            refunded: refunded,
            totalRevenue: totalRevenue
        });
    }
    
    function getStatusClass(status) {
        var classes = {
            'Pending': 'bg-yellow-100 text-yellow-800',
            'Completed': 'bg-green-100 text-green-800',
            'Failed': 'bg-red-100 text-red-800',
            'Refunded': 'bg-purple-100 text-purple-800'
        };
        return classes[status] || 'bg-gray-100 text-gray-800';
    }
    
    function getTypeClass(type) {
        var classes = {
            'Deposit': 'bg-blue-100 text-blue-800',
            'FinalPayment': 'bg-indigo-100 text-indigo-800',
            'Surcharge': 'bg-orange-100 text-orange-800',
            'Refund': 'bg-purple-100 text-purple-800'
        };
        return classes[type] || 'bg-gray-100 text-gray-800';
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
        var tbody = document.getElementById('paymentsTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="9" class="px-6 py-12 text-center text-red-500">Error loading payments. Please try again.</td></tr>';
        }
    }
    
    // Event listeners
    var statusFilter = document.getElementById('paymentsStatusFilter');
    var typeFilter = document.getElementById('paymentsTypeFilter');
    var searchInput = document.getElementById('paymentsSearchInput');
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
    
    if (typeFilter) {
        typeFilter.addEventListener('change', function() {
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
        var type = typeFilter ? typeFilter.value : '';
        var search = searchInput ? searchInput.value.toLowerCase() : '';
        
        filteredData = paymentsData.filter(function(p) {
            var matchStatus = !status || p.paymentStatus === status;
            var matchType = !type || p.paymentType === type;
            var matchSearch = !search || 
                (p.paymentId && p.paymentId.toLowerCase().includes(search)) ||
                (p.bookingCode && p.bookingCode.toLowerCase().includes(search)) ||
                (p.userName && p.userName.toLowerCase().includes(search)) ||
                (p.transactionId && p.transactionId.toLowerCase().includes(search));
            
            return matchStatus && matchType && matchSearch;
        });
        
        renderPayments();
    }
    
    // Load payments when tab is shown
    window.addEventListener('tabChanged', function(e) {
        if (e.detail === 'payments') {
            console.log('Payments tab activated, loading...');
            loadPayments();
            loadStatistics();
        }
    });
    
    // Global functions for buttons
    window.viewPaymentDetail = function(paymentId) {
        fetch('/admin/api/payments/' + paymentId)
            .then(function(response) {
                if (!response.ok) throw new Error('Failed to load payment');
                return response.json();
            })
            .then(function(payment) {
                showPaymentDetailModal(payment);
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('Failed to load payment details');
            });
    };
    
    function showPaymentDetailModal(payment) {
        var modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center p-4';
        modal.id = 'paymentDetailModal';
        modal.onclick = function(e) {
            if (e.target === modal) closePaymentDetailModal();
        };
        
        var content = '<div class="relative bg-white rounded-xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">';
        
        // Header
        content += '<div class="bg-gradient-to-r from-green-600 to-emerald-600 p-6 rounded-t-xl">';
        content += '<div class="flex justify-between items-center">';
        content += '<div>';
        content += '<h3 class="text-2xl font-bold text-white">Payment Details</h3>';
        content += '<p class="text-green-100 text-sm mt-1">ID: ' + escapeHtml(payment.paymentId) + '</p>';
        content += '</div>';
        content += '<button onclick="closePaymentDetailModal()" class="text-white hover:text-gray-200 transition-colors">';
        content += '<i class="fas fa-times text-2xl"></i></button>';
        content += '</div></div>';
        
        // Status badges
        content += '<div class="px-6 py-4 border-b border-gray-200 bg-gray-50 flex space-x-3">';
        content += '<span class="px-4 py-2 rounded-full text-sm font-semibold ' + getStatusClass(payment.paymentStatus) + '">';
        content += '<i class="fas fa-circle text-xs mr-2"></i>' + payment.paymentStatus + '</span>';
        content += '<span class="px-4 py-2 rounded-full text-sm font-semibold ' + getTypeClass(payment.paymentType) + '">';
        content += '<i class="fas fa-tag text-xs mr-2"></i>' + payment.paymentType + '</span>';
        content += '</div>';
        
        // Content
        content += '<div class="p-6 grid grid-cols-2 gap-4">';
        content += createDetailCard('Amount', '<span class="text-green-600 font-bold text-lg">' + (payment.amount ? payment.amount.toLocaleString('vi-VN') + ' VND' : '-') + '</span>', 'fa-money-bill-wave');
        content += createDetailCard('Payment Method', payment.paymentMethod || '-', 'fa-credit-card');
        content += createDetailCard('Transaction ID', payment.transactionId || '-', 'fa-receipt');
        content += createDetailCard('Booking Code', payment.bookingCode || '-', 'fa-ticket-alt');
        content += createDetailCard('Customer', payment.userName || '-', 'fa-user');
        content += createDetailCard('Email', payment.userEmail || '-', 'fa-envelope');
        content += createDetailCard('Phone', payment.userPhone || '-', 'fa-phone');
        content += createDetailCard('Contract', payment.contractCode || '-', 'fa-file-contract');
        content += createDetailCard('Payment Date', payment.paymentDate ? formatDateTime(payment.paymentDate) : 'Not paid', 'fa-calendar');
        content += createDetailCard('Created Date', formatDateTime(payment.createdDate), 'fa-calendar-plus');
        
        if (payment.notes) {
            content += '<div class="col-span-2 bg-blue-50 border-l-4 border-blue-500 p-4 rounded">';
            content += '<h5 class="font-semibold text-blue-900 mb-2"><i class="fas fa-sticky-note mr-2"></i>Notes</h5>';
            content += '<p class="text-blue-800">' + escapeHtml(payment.notes) + '</p></div>';
        }
        
        content += '</div>';
        
        // Footer
        content += '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end">';
        content += '<button onclick="closePaymentDetailModal()" class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors">';
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
    
    window.closePaymentDetailModal = function() {
        var modal = document.getElementById('paymentDetailModal');
        if (modal) modal.remove();
    };
    
    window.editPayment = function(paymentId) {
        fetch('/admin/api/payments/' + paymentId)
            .then(function(response) {
                if (!response.ok) throw new Error('Failed to load payment');
                return response.json();
            })
            .then(function(payment) {
                showEditPaymentModal(payment);
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('Failed to load payment for editing');
            });
    };
    
    function showEditPaymentModal(payment) {
        var modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center p-4';
        modal.id = 'editPaymentModal';
        modal.onclick = function(e) {
            if (e.target === modal) closeEditPaymentModal();
        };
        
        var content = '<div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl" onclick="event.stopPropagation()">';
        
        // Header
        content += '<div class="bg-gradient-to-r from-green-600 to-emerald-600 p-6 rounded-t-xl">';
        content += '<div class="flex justify-between items-center">';
        content += '<div>';
        content += '<h3 class="text-2xl font-bold text-white">Edit Payment</h3>';
        content += '<p class="text-green-100 text-sm mt-1">ID: ' + escapeHtml(payment.paymentId) + '</p>';
        content += '</div>';
        content += '<button onclick="closeEditPaymentModal()" class="text-white hover:text-gray-200 transition-colors">';
        content += '<i class="fas fa-times text-2xl"></i></button>';
        content += '</div></div>';
        
        // Form
        content += '<form id="editPaymentForm" class="p-6">';
        
        // Read-only info
        content += '<div class="mb-6 bg-gray-50 p-4 rounded-lg">';
        content += '<h4 class="font-semibold text-gray-900 mb-3">Payment Information</h4>';
        content += '<div class="grid grid-cols-2 gap-4 text-sm">';
        content += '<div><span class="text-gray-600">Booking:</span> <span class="font-medium">' + escapeHtml(payment.bookingCode || '-') + '</span></div>';
        content += '<div><span class="text-gray-600">Customer:</span> <span class="font-medium">' + escapeHtml(payment.userName || '-') + '</span></div>';
        content += '</div></div>';
        
        // Editable fields
        content += '<div class="space-y-4">';
        
        // Amount
        content += '<div>';
        content += '<label for="editAmount" class="block text-sm font-medium text-gray-700 mb-2">';
        content += '<i class="fas fa-money-bill-wave mr-2 text-green-600"></i>Amount (VND)</label>';
        content += '<input type="number" id="editAmount" name="amount" value="' + (payment.amount || 0) + '" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500" required>';
        content += '</div>';
        
        // Payment Method
        content += '<div>';
        content += '<label for="editPaymentMethod" class="block text-sm font-medium text-gray-700 mb-2">';
        content += '<i class="fas fa-credit-card mr-2 text-blue-600"></i>Payment Method</label>';
        content += '<select id="editPaymentMethod" name="paymentMethod" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500">';
        content += '<option value="Credit Card" ' + (payment.paymentMethod === 'Credit Card' ? 'selected' : '') + '>Credit Card</option>';
        content += '<option value="Bank Transfer" ' + (payment.paymentMethod === 'Bank Transfer' ? 'selected' : '') + '>Bank Transfer</option>';
        content += '<option value="Cash" ' + (payment.paymentMethod === 'Cash' ? 'selected' : '') + '>Cash</option>';
        content += '<option value="VNPay" ' + (payment.paymentMethod === 'VNPay' ? 'selected' : '') + '>VNPay</option>';
        content += '<option value="MoMo" ' + (payment.paymentMethod === 'MoMo' ? 'selected' : '') + '>MoMo</option>';
        content += '<option value="E-Wallet" ' + (payment.paymentMethod === 'E-Wallet' ? 'selected' : '') + '>E-Wallet</option>';
        content += '</select>';
        content += '</div>';
        
        // Status
        content += '<div>';
        content += '<label for="editPaymentStatus" class="block text-sm font-medium text-gray-700 mb-2">';
        content += '<i class="fas fa-info-circle mr-2 text-indigo-600"></i>Status</label>';
        content += '<select id="editPaymentStatus" name="paymentStatus" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500">';
        content += '<option value="Pending" ' + (payment.paymentStatus === 'Pending' ? 'selected' : '') + '>Pending</option>';
        content += '<option value="Completed" ' + (payment.paymentStatus === 'Completed' ? 'selected' : '') + '>Completed</option>';
        content += '<option value="Failed" ' + (payment.paymentStatus === 'Failed' ? 'selected' : '') + '>Failed</option>';
        content += '<option value="Refunded" ' + (payment.paymentStatus === 'Refunded' ? 'selected' : '') + '>Refunded</option>';
        content += '</select>';
        content += '</div>';
        
        // Transaction ID
        content += '<div>';
        content += '<label for="editTransactionId" class="block text-sm font-medium text-gray-700 mb-2">';
        content += '<i class="fas fa-receipt mr-2 text-purple-600"></i>Transaction ID</label>';
        content += '<input type="text" id="editTransactionId" name="transactionId" value="' + escapeHtml(payment.transactionId || '') + '" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500">';
        content += '</div>';
        
        // Notes
        content += '<div>';
        content += '<label for="editPaymentNotes" class="block text-sm font-medium text-gray-700 mb-2">';
        content += '<i class="fas fa-sticky-note mr-2 text-blue-600"></i>Notes</label>';
        content += '<textarea id="editPaymentNotes" name="notes" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500" placeholder="Add notes...">' + escapeHtml(payment.notes || '') + '</textarea>';
        content += '</div>';
        
        content += '</div>';
        content += '</form>';
        
        // Footer
        content += '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">';
        content += '<button onclick="closeEditPaymentModal()" class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors">';
        content += '<i class="fas fa-times mr-2"></i>Cancel</button>';
        content += '<button onclick="savePaymentChanges(\'' + payment.paymentId + '\')" class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">';
        content += '<i class="fas fa-save mr-2"></i>Save Changes</button>';
        content += '</div>';
        
        content += '</div>';
        
        modal.innerHTML = content;
        document.body.appendChild(modal);
    }
    
    window.savePaymentChanges = function(paymentId) {
        var data = {
            amount: parseFloat(document.getElementById('editAmount').value),
            paymentMethod: document.getElementById('editPaymentMethod').value,
            paymentStatus: document.getElementById('editPaymentStatus').value,
            transactionId: document.getElementById('editTransactionId').value,
            notes: document.getElementById('editPaymentNotes').value
        };
        
        console.log('Saving payment:', paymentId, data);
        
        fetch('/admin/api/payments/' + paymentId + '/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to update payment');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Payment updated successfully!');
                closeEditPaymentModal();
                loadPayments();
            } else {
                throw new Error(result.message || 'Update failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed to update payment: ' + error.message);
        });
    };
    
    window.closeEditPaymentModal = function() {
        var modal = document.getElementById('editPaymentModal');
        if (modal) modal.remove();
    };
    
    window.deletePayment = function(paymentId) {
        if (!confirm('Are you sure you want to delete this payment? This action cannot be undone.')) return;
        
        fetch('/admin/api/payments/' + paymentId, {
            method: 'DELETE'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to delete payment');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Payment deleted successfully!');
                loadPayments();
            } else {
                throw new Error(result.message || 'Delete failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed to delete payment: ' + error.message);
        });
    };
    
    window.markAsCompleted = function(paymentId) {
        if (!confirm('Mark this payment as completed?')) return;
        
        fetch('/admin/api/payments/' + paymentId + '/mark-completed', {
            method: 'POST'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to mark as completed');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Payment marked as completed!');
                loadPayments();
            } else {
                throw new Error(result.message || 'Operation failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed: ' + error.message);
        });
    };
    
    window.markAsFailed = function(paymentId) {
        if (!confirm('Mark this payment as failed?')) return;
        
        fetch('/admin/api/payments/' + paymentId + '/mark-failed', {
            method: 'POST'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to mark as failed');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Payment marked as failed!');
                loadPayments();
            } else {
                throw new Error(result.message || 'Operation failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed: ' + error.message);
        });
    };
    
    window.processRefund = function(paymentId) {
        if (!confirm('Process refund for this payment? This will change the status to Refunded.')) return;
        
        fetch('/admin/api/payments/' + paymentId + '/refund', {
            method: 'POST'
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Failed to process refund');
            return response.json();
        })
        .then(function(result) {
            if (result.status === 'success') {
                alert('✅ Refund processed successfully!');
                loadPayments();
            } else {
                throw new Error(result.message || 'Operation failed');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('❌ Failed: ' + error.message);
        });
    };
    
    // Initial load if payments tab is active
    if (document.getElementById('payments') && document.getElementById('payments').classList.contains('active')) {
        loadPayments();
        loadStatistics();
    }
})();
