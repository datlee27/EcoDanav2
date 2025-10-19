/**
 * =============================================
 * USER MANAGEMENT JAVASCRIPT
 * Modern, accessible user administration
 * =============================================
 */

(function() {
    'use strict';
    
    // =============================================
    // GLOBAL VARIABLES
    // =============================================
    var users = [];
    var roles = [];
    var currentEditingUserId = null;
    var csrfToken = '';
    var csrfHeader = '';
    
    // Debounce timer
    var searchDebounceTimer = null;
    var DEBOUNCE_DELAY = 300;
    
    // =============================================
    // INITIALIZATION
    // =============================================
    document.addEventListener('DOMContentLoaded', function() {
        initializeCSRF();
        initializeEventListeners();
        loadRoles();
        loadUsers();
    });
    
    /**
     * Initialize CSRF token from meta tags
     */
    function initializeCSRF() {
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        
        if (tokenMeta && headerMeta) {
            csrfToken = tokenMeta.getAttribute('content');
            csrfHeader = headerMeta.getAttribute('content');
        }
    }
    
    /**
     * Initialize all event listeners
     */
    function initializeEventListeners() {
        // Search input with debounce
        var searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', function() {
                clearTimeout(searchDebounceTimer);
                searchDebounceTimer = setTimeout(function() {
                    filterUsers();
                }, DEBOUNCE_DELAY);
            });
        }
        
        // Filter dropdowns
        var statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', filterUsers);
        }
        
        var roleFilter = document.getElementById('roleFilter');
        if (roleFilter) {
            roleFilter.addEventListener('change', filterUsers);
        }
        
        // Action buttons
        var addUserBtn = document.getElementById('addUserBtn');
        if (addUserBtn) {
            addUserBtn.addEventListener('click', openAddUserModal);
        }
        
        var refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function() {
                loadUsers();
                showToast('User list refreshed', 'success');
            });
        }
        
        // Form submission
        var userForm = document.getElementById('userForm');
        if (userForm) {
            userForm.addEventListener('submit', handleFormSubmit);
        }
        
        // Modal close on background click
        var modal = document.getElementById('userModal');
        if (modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === modal) {
                    closeUserModal();
                }
            });
        }
        
        // Keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            // ESC to close modal
            if (e.key === 'Escape') {
                closeUserModal();
                hideToast();
            }
            // Ctrl/Cmd + K to focus search
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                var searchInput = document.getElementById('searchInput');
                if (searchInput) {
                    searchInput.focus();
                }
            }
        });
    }
    
    // =============================================
    // DATA LOADING FUNCTIONS
    // =============================================
    
    /**
     * Load all users from API
     */
    function loadUsers() {
        showLoadingState();
        
        fetch('/admin/users/api/list', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('Failed to load users');
            }
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                users = data.users || [];
                renderUsers(users);
            } else {
                throw new Error(data.message || 'Failed to load users');
            }
        })
        .catch(function(error) {
            console.error('Error loading users:', error);
            showErrorState('Failed to load users. Please try again.');
            showToast('Error loading users: ' + error.message, 'error');
        });
    }
    
    /**
     * Load all roles from API
     */
    function loadRoles() {
        fetch('/admin/users/api/roles', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('Failed to load roles');
            }
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                roles = data.roles || [];
            }
        })
        .catch(function(error) {
            console.error('Error loading roles:', error);
        });
    }
    
    /**
     * Filter users based on search and filters (for Thymeleaf rendered table)
     */
    function filterUsers() {
        var searchInput = document.getElementById('searchInput') || document.getElementById('userSearchInput');
        var searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        var statusFilter = document.getElementById('statusFilter') ? document.getElementById('statusFilter').value : '';
        var roleFilterElement = document.getElementById('roleFilter');
        var roleFilter = roleFilterElement ? roleFilterElement.value : '';
        
        console.log('Filtering - Search:', searchTerm, 'Status:', statusFilter, 'Role:', roleFilter);
        
        // Try to find rows using multiple strategies
        var rows = document.querySelectorAll('.user-row');
        
        // Strategy 1: Look in users tab
        if (rows.length === 0) {
            var usersTab = document.getElementById('users');
            if (usersTab) {
                rows = usersTab.querySelectorAll('tr[data-role]');
                console.log('Strategy 1 - Found rows in users tab:', rows.length);
            }
        }
        
        // Strategy 2: Look in usersTableBody
        if (rows.length === 0) {
            var tableBody = document.getElementById('usersTableBody');
            if (tableBody) {
                rows = tableBody.querySelectorAll('tr[data-role]');
                console.log('Strategy 2 - Found rows in table body:', rows.length);
            }
        }
        
        // Strategy 3: Look anywhere for tr with data-role
        if (rows.length === 0) {
            rows = document.querySelectorAll('tr[data-role]');
            console.log('Strategy 3 - Found rows globally:', rows.length);
        }
        
        if (rows.length === 0) {
            console.log('No user rows found - table might be empty or not rendered yet');
            return;
        }
        
        var visibleCount = 0;
        
        rows.forEach(function(row) {
            var rowRole = row.getAttribute('data-role') || 'Customer';
            var rowStatus = row.getAttribute('data-status');
            var rowUsername = (row.getAttribute('data-username') || '').toLowerCase();
            var rowEmail = (row.getAttribute('data-email') || '').toLowerCase();
            
            var matchesSearch = !searchTerm || 
                rowUsername.indexOf(searchTerm) !== -1 ||
                rowEmail.indexOf(searchTerm) !== -1;
            
            var matchesStatus = !statusFilter || statusFilter === 'all' || statusFilter === '' || rowStatus === statusFilter;
            var matchesRole = !roleFilter || roleFilter === 'all' || roleFilter === '' || rowRole === roleFilter;
            
            console.log('Row:', rowUsername, 'Role:', rowRole, 'Filter:', roleFilter, 'Matches:', matchesRole && matchesSearch && matchesStatus);
            
            if (matchesSearch && matchesStatus && matchesRole) {
                row.style.display = '';
                visibleCount++;
            } else {
                row.style.display = 'none';
            }
        });
        
        console.log('Visible users count:', visibleCount, 'out of', rows.length);
    }
    
    /**
     * Filter by role when clicking on role cards
     */
    window.filterByRole = function(role) {
        console.log('=== filterByRole called with:', role, '===');
        
        // Check if we're in the users tab
        var usersTab = document.getElementById('users');
        if (usersTab) {
            var isActive = usersTab.classList.contains('active');
            console.log('Users tab active:', isActive);
        }
        
        var roleFilterElement = document.getElementById('roleFilter');
        if (roleFilterElement) {
            roleFilterElement.value = role;
            console.log('Role filter set to:', roleFilterElement.value);
            
            // Immediate attempt
            filterUsers();
            
            // Also try after delay in case DOM is still loading
            setTimeout(function() {
                console.log('=== Retry filter after delay ===');
                filterUsers();
            }, 100);
        } else {
            console.error('roleFilter element not found');
        }
    };
    
    // =============================================
    // RENDERING FUNCTIONS
    // =============================================
    
    /**
     * Render users table
     */
    function renderUsers(usersToRender) {
        var tbody = document.getElementById('usersTableBody');
        if (!tbody) return;
        
        if (!usersToRender || usersToRender.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-12 text-center text-gray-500">' +
                '<div class="empty-state">' +
                '<i class="fas fa-users empty-state-icon"></i>' +
                '<p class="empty-state-text">No users found</p>' +
                '</div></td></tr>';
            return;
        }
        
        var html = '';
        usersToRender.forEach(function(user) {
            html += renderUserRow(user);
        });
        
        tbody.innerHTML = html;
        attachRowEventListeners();
    }
    
    /**
     * Render a single user row
     */
    function renderUserRow(user) {
        var avatar = user.avatarUrl ? 
            '<img src="' + escapeHtml(user.avatarUrl) + '" alt="' + escapeHtml(user.username) + '" class="user-avatar">' :
            '<div class="user-avatar-placeholder">' + getInitials(user.firstName, user.lastName) + '</div>';
        
        var statusClass = 'status-' + (user.status || 'inactive').toLowerCase();
        var roleClass = 'role-' + (user.roleName || 'customer').toLowerCase();
        
        var createdDate = user.createdDate ? formatDate(user.createdDate) : '-';
        
        return '<tr class="fade-in" data-user-id="' + escapeHtml(user.id) + '">' +
            '<td class="px-6 py-4 whitespace-nowrap">' +
                '<div class="flex items-center">' +
                    '<div class="flex-shrink-0 h-10 w-10">' + avatar + '</div>' +
                    '<div class="ml-4">' +
                        '<div class="text-sm font-medium text-gray-900">' + escapeHtml(user.username || '-') + '</div>' +
                        '<div class="text-sm text-gray-500">' + escapeHtml(user.firstName || '') + ' ' + escapeHtml(user.lastName || '') + '</div>' +
                    '</div>' +
                '</div>' +
            '</td>' +
            '<td class="px-6 py-4 whitespace-nowrap">' +
                '<div class="text-sm text-gray-900">' + escapeHtml(user.email || '-') + '</div>' +
                '<div class="text-sm text-gray-500">' + escapeHtml(user.phoneNumber || '-') + '</div>' +
            '</td>' +
            '<td class="px-6 py-4 whitespace-nowrap">' +
                '<span class="role-badge ' + roleClass + '">' + escapeHtml(user.roleName || 'Customer') + '</span>' +
            '</td>' +
            '<td class="px-6 py-4 whitespace-nowrap">' +
                '<span class="status-badge ' + statusClass + '">' + escapeHtml(user.status || 'Inactive') + '</span>' +
            '</td>' +
            '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' + createdDate + '</td>' +
            '<td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">' +
                '<a href="/admin/users/detail/' + escapeHtml(user.id) + '" class="action-btn action-btn-view mr-2" title="View details" aria-label="View details">' +
                    '<i class="fas fa-eye"></i>' +
                '</a>' +
                '<button class="action-btn action-btn-edit mr-2" onclick="editUser(\'' + escapeHtml(user.id) + '\')" title="Edit user" aria-label="Edit user">' +
                    '<i class="fas fa-edit"></i>' +
                '</button>' +
                '<button class="action-btn action-btn-delete" onclick="deleteUser(\'' + escapeHtml(user.id) + '\')" title="Delete user" aria-label="Delete user">' +
                    '<i class="fas fa-trash"></i>' +
                '</button>' +
            '</td>' +
        '</tr>';
    }
    
    /**
     * Show loading state
     */
    function showLoadingState() {
        var tbody = document.getElementById('usersTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-12 text-center">' +
            '<div class="flex flex-col items-center justify-center">' +
            '<i class="fas fa-spinner fa-spin text-4xl text-gray-400 mb-4"></i>' +
            '<p class="text-gray-500">Loading users...</p>' +
            '</div></td></tr>';
    }
    
    /**
     * Show error state
     */
    function showErrorState(message) {
        var tbody = document.getElementById('usersTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-12 text-center text-red-500">' +
            '<div class="empty-state">' +
            '<i class="fas fa-exclamation-triangle empty-state-icon"></i>' +
            '<p class="empty-state-text">' + escapeHtml(message) + '</p>' +
            '</div></td></tr>';
    }
    
    /**
     * Attach event listeners to row buttons
     */
    function attachRowEventListeners() {
        // Event delegation is handled by onclick attributes in the HTML
        // This function is kept for potential future enhancements
    }
    
    // =============================================
    // MODAL FUNCTIONS
    // =============================================
    
    /**
     * Open modal for adding new user
     */
    function openAddUserModal() {
        currentEditingUserId = null;
        document.getElementById('modalTitle').textContent = 'Add New User';
        document.getElementById('passwordRequired').style.display = 'inline';
        document.getElementById('password').required = true;
        resetForm();
        showModal();
    }
    window.openAddUserModal = openAddUserModal;
    
    /**
     * Navigate to edit page
     */
    function editUser(userId) {
        window.location.href = '/admin/users/edit/' + userId;
        return;
        
        // OLD MODAL CODE (DEPRECATED)
        currentEditingUserId = userId;
        document.getElementById('modalTitle').textContent = 'Edit User';
        document.getElementById('passwordRequired').style.display = 'none';
        document.getElementById('password').required = false;
        
        // Find user data
        var user = users.find(function(u) { return u.id === userId; });
        if (!user) {
            showToast('User not found', 'error');
            return;
        }
        
        // Populate form
        document.getElementById('userId').value = user.id;
        document.getElementById('username').value = user.username || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('firstName').value = user.firstName || '';
        document.getElementById('lastName').value = user.lastName || '';
        document.getElementById('phoneNumber').value = user.phoneNumber || '';
        document.getElementById('userDOB').value = user.userDOB || '';
        document.getElementById('gender').value = user.gender || '';
        document.getElementById('roleId').value = user.roleId || '';
        document.getElementById('status').value = user.status || 'Active';
        document.getElementById('avatarUrl').value = user.avatarUrl || '';
        document.getElementById('emailVerified').checked = user.emailVerified || false;
        document.getElementById('twoFactorEnabled').checked = user.twoFactorEnabled || false;
        document.getElementById('lockoutEnabled').checked = user.lockoutEnabled || false;
        
        showModal();
    }
    window.editUser = editUser;
    
    /**
     * Show modal
     */
    function showModal() {
        var modal = document.getElementById('userModal');
        if (modal) {
            modal.classList.remove('hidden');
            modal.classList.add('show');
            document.body.style.overflow = 'hidden';
            
            // Focus first input
            setTimeout(function() {
                var firstInput = modal.querySelector('input:not([type="hidden"])');
                if (firstInput) {
                    firstInput.focus();
                }
            }, 100);
        }
    }
    
    /**
     * Close modal
     */
    function closeUserModal() {
        var modal = document.getElementById('userModal');
        if (modal) {
            modal.classList.add('hidden');
            modal.classList.remove('show');
            document.body.style.overflow = '';
            resetForm();
            currentEditingUserId = null;
        }
    }
    window.closeUserModal = closeUserModal;
    
    /**
     * Reset form
     */
    function resetForm() {
        var form = document.getElementById('userForm');
        if (form) {
            form.reset();
            document.getElementById('userId').value = '';
        }
    }
    
    // =============================================
    // FORM SUBMISSION
    // =============================================
    
    /**
     * Handle form submission
     */
    function handleFormSubmit(e) {
        e.preventDefault();
        
        var formData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            phoneNumber: document.getElementById('phoneNumber').value,
            userDOB: document.getElementById('userDOB').value || null,
            gender: document.getElementById('gender').value || null,
            roleId: document.getElementById('roleId').value,
            status: document.getElementById('status').value,
            avatarUrl: document.getElementById('avatarUrl').value || null,
            emailVerified: document.getElementById('emailVerified').checked,
            twoFactorEnabled: document.getElementById('twoFactorEnabled').checked,
            lockoutEnabled: document.getElementById('lockoutEnabled').checked,
            password: document.getElementById('password').value || null
        };
        
        if (currentEditingUserId) {
            updateUser(currentEditingUserId, formData);
        } else {
            createUser(formData);
        }
    }
    
    /**
     * Create new user
     */
    function createUser(userData) {
        var headers = {
            'Content-Type': 'application/json'
        };
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        
        fetch('/admin/users/api/create', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(userData),
            credentials: 'same-origin'
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                showToast('User created successfully', 'success');
                closeUserModal();
                loadUsers();
            } else {
                showToast(data.message || 'Failed to create user', 'error');
            }
        })
        .catch(function(error) {
            console.error('Error creating user:', error);
            showToast('Error creating user: ' + error.message, 'error');
        });
    }
    
    /**
     * Update existing user
     */
    function updateUser(userId, userData) {
        var headers = {
            'Content-Type': 'application/json'
        };
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        
        fetch('/admin/users/api/update/' + userId, {
            method: 'PUT',
            headers: headers,
            body: JSON.stringify(userData),
            credentials: 'same-origin'
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                showToast('User updated successfully', 'success');
                closeUserModal();
                loadUsers();
            } else {
                showToast(data.message || 'Failed to update user', 'error');
            }
        })
        .catch(function(error) {
            console.error('Error updating user:', error);
            showToast('Error updating user: ' + error.message, 'error');
        });
    }
    
    /**
     * Delete user
     */
    function deleteUser(userId) {
        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
            return;
        }
        
        var headers = {};
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        
        fetch('/admin/users/api/delete/' + userId, {
            method: 'DELETE',
            headers: headers,
            credentials: 'same-origin'
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                showToast('User deleted successfully', 'success');
                loadUsers();
            } else {
                showToast(data.message || 'Failed to delete user', 'error');
            }
        })
        .catch(function(error) {
            console.error('Error deleting user:', error);
            showToast('Error deleting user: ' + error.message, 'error');
        });
    }
    window.deleteUser = deleteUser;
    
    // =============================================
    // TOAST NOTIFICATION
    // =============================================
    
    /**
     * Show toast notification
     */
    function showToast(message, type) {
        type = type || 'info';
        
        var toast = document.getElementById('toast');
        var toastMessage = document.getElementById('toastMessage');
        var toastIcon = document.getElementById('toastIcon');
        
        if (!toast || !toastMessage || !toastIcon) return;
        
        // Set icon based on type
        var iconClass = '';
        var iconColor = '';
        switch(type) {
            case 'success':
                iconClass = 'fas fa-check-circle';
                iconColor = 'text-green-500';
                break;
            case 'error':
                iconClass = 'fas fa-times-circle';
                iconColor = 'text-red-500';
                break;
            case 'warning':
                iconClass = 'fas fa-exclamation-triangle';
                iconColor = 'text-yellow-500';
                break;
            default:
                iconClass = 'fas fa-info-circle';
                iconColor = 'text-blue-500';
        }
        
        toastIcon.innerHTML = '<i class="' + iconClass + ' text-2xl ' + iconColor + '"></i>';
        toastMessage.textContent = message;
        
        toast.classList.remove('hidden');
        toast.classList.add('show');
        
        // Auto hide after 5 seconds
        setTimeout(function() {
            hideToast();
        }, 5000);
    }
    
    /**
     * Hide toast notification
     */
    function hideToast() {
        var toast = document.getElementById('toast');
        if (toast) {
            toast.classList.add('hidden');
            toast.classList.remove('show');
        }
    }
    window.hideToast = hideToast;
    
    // =============================================
    // UTILITY FUNCTIONS
    // =============================================
    
    /**
     * Escape HTML to prevent XSS
     */
    function escapeHtml(text) {
        if (!text) return '';
        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, function(m) { return map[m]; });
    }
    
    /**
     * Get initials from name
     */
    function getInitials(firstName, lastName) {
        var initials = '';
        if (firstName) initials += firstName.charAt(0).toUpperCase();
        if (lastName) initials += lastName.charAt(0).toUpperCase();
        return initials || '?';
    }
    
    /**
     * Format date
     */
    function formatDate(dateString) {
        if (!dateString) return '-';
        try {
            var date = new Date(dateString);
            var day = ('0' + date.getDate()).slice(-2);
            var month = ('0' + (date.getMonth() + 1)).slice(-2);
            var year = date.getFullYear();
            return day + '/' + month + '/' + year;
        } catch (e) {
            return dateString;
        }
    }
    
})();
