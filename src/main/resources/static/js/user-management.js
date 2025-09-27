// User Management JavaScript - Clean Implementation

let allUsers = [];
let filteredUsers = [];

// Initialize User Management
document.addEventListener('DOMContentLoaded', function() {
    console.log('User Management initialized');
    
    // Load users when users tab is active
    const usersTab = document.getElementById('users');
    if (usersTab) {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    if (usersTab.classList.contains('active')) {
                        loadUsers();
                    }
                }
            });
        });
        observer.observe(usersTab, { attributes: true });
    }
    
    // Setup search functionality
    const searchInput = document.getElementById('userSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            filterUsers(e.target.value);
        });
    }
});

// Load users from API
async function loadUsers() {
    try {
        console.log('Loading users...');
        const response = await fetch('/admin/api/users');
        const data = await response.json();
        
        if (data.success) {
            allUsers = data.users;
            filteredUsers = [...allUsers];
            renderUsers();
            console.log('Loaded', allUsers.length, 'users');
        } else {
            console.error('Failed to load users:', data.error);
            showNotification('Failed to load users: ' + data.error, 'error');
        }
    } catch (error) {
        console.error('Error loading users:', error);
        showNotification('Error loading users: ' + error.message, 'error');
    }
}

// Filter users based on search term
function filterUsers(searchTerm) {
    if (!searchTerm.trim()) {
        filteredUsers = [...allUsers];
    } else {
        const term = searchTerm.toLowerCase();
        filteredUsers = allUsers.filter(user => 
            user.username.toLowerCase().includes(term) ||
            user.email.toLowerCase().includes(term) ||
            (user.firstName && user.firstName.toLowerCase().includes(term)) ||
            (user.lastName && user.lastName.toLowerCase().includes(term)) ||
            (user.phoneNumber && user.phoneNumber.includes(term)) ||
            (user.roleName && user.roleName.toLowerCase().includes(term))
        );
    }
    renderUsers();
}

// Render users in table
function renderUsers() {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    if (filteredUsers.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="px-6 py-4 text-center text-gray-500">
                    <div class="text-center">
                        <i class="fas fa-users text-4xl text-gray-300 mb-2"></i>
                        <p class="text-gray-500">No users found</p>
                        <p class="text-sm text-gray-400">Click "Add User" to create a new user</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = filteredUsers.map((user, index) => `
        <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${index + 1}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                        <i class="fas fa-user-circle text-2xl text-gray-400"></i>
                    </div>
                    <div class="ml-4">
                        <div class="text-sm font-medium text-gray-900">${user.username || 'N/A'}</div>
                        <div class="text-sm text-gray-500">${getFullName(user)}</div>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${user.email || 'N/A'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${user.phoneNumber || 'N/A'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${user.roleName || 'N/A'}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusClass(user.status)}">
                    ${user.status || 'N/A'}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${user.createdDate ? formatDate(user.createdDate) : 'N/A'}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button onclick="editUser('${user.id}')" class="text-indigo-600 hover:text-indigo-900 mr-3">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button onclick="deleteUser('${user.id}', '${user.username}')" class="text-red-600 hover:text-red-900">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        </tr>
    `).join('');
}

// Helper functions
function getFullName(user) {
    if (user.firstName && user.lastName) {
        return `${user.firstName} ${user.lastName}`;
    } else if (user.firstName) {
        return user.firstName;
    } else if (user.lastName) {
        return user.lastName;
    }
    return 'Not set';
}

function getStatusClass(status) {
    switch (status) {
        case 'Active':
            return 'bg-green-100 text-green-800';
        case 'Pending':
            return 'bg-yellow-100 text-yellow-800';
        case 'Suspended':
        case 'Inactive':
            return 'bg-red-100 text-red-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

// Show Add User Modal
function showAddUserModal() {
    const modal = document.getElementById('addUserModal');
    if (modal) {
        modal.classList.remove('hidden', 'modal-hidden');
        modal.classList.add('modal-visible');
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        
        // Reset form
        document.getElementById('addUserForm').reset();
        
        // Focus on first input
        setTimeout(() => {
            const firstNameInput = document.getElementById('firstName');
            if (firstNameInput) {
                firstNameInput.focus();
            }
        }, 100);
    }
}

// Close Add User Modal
function closeAddUserModal() {
    const modal = document.getElementById('addUserModal');
    if (modal) {
        modal.classList.add('hidden', 'modal-hidden');
        modal.classList.remove('modal-visible');
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// Show Edit User Modal
function editUser(userId) {
    const user = allUsers.find(u => u.id === userId);
    if (!user) {
        showNotification('User not found', 'error');
        return;
    }
    
    const modal = document.getElementById('editUserModal');
    if (modal) {
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
        modal.classList.remove('hidden', 'modal-hidden');
        modal.classList.add('modal-visible');
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

// Close Edit User Modal
function closeEditUserModal() {
    const modal = document.getElementById('editUserModal');
    if (modal) {
        modal.classList.add('hidden', 'modal-hidden');
        modal.classList.remove('modal-visible');
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// Submit Add User
async function submitAddUser() {
    const form = document.getElementById('addUserForm');
    const formData = new FormData(form);
    
    // Validate form
    if (!validateAddUserForm()) {
        return;
    }
    
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
    
    try {
        const response = await fetch('/admin/api/users/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams(userData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('User added successfully!', 'success');
            closeAddUserModal();
            loadUsers(); // Reload users
        } else {
            showNotification('Error: ' + data.error, 'error');
        }
    } catch (error) {
        showNotification('Error: ' + error.message, 'error');
    }
}

// Submit Edit User
async function submitEditUser() {
    const form = document.getElementById('editUserForm');
    const formData = new FormData(form);
    
    // Validate form
    if (!validateEditUserForm()) {
        return;
    }
    
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
    
    try {
        const response = await fetch('/admin/api/users/edit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams(userData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('User updated successfully!', 'success');
            closeEditUserModal();
            loadUsers(); // Reload users
        } else {
            showNotification('Error: ' + data.error, 'error');
        }
    } catch (error) {
        showNotification('Error: ' + error.message, 'error');
    }
}

// Delete User
async function deleteUser(userId, username) {
    if (!confirm(`Are you sure you want to delete user "${username}"?`)) {
        return;
    }
    
    try {
        const response = await fetch('/admin/api/users/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({ userId })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('User deleted successfully!', 'success');
            loadUsers(); // Reload users
        } else {
            showNotification('Error: ' + data.error, 'error');
        }
    } catch (error) {
        showNotification('Error: ' + error.message, 'error');
    }
}

// Form validation
function validateAddUserForm() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    if (password !== confirmPassword) {
        showNotification('Passwords do not match!', 'error');
        return false;
    }
    
    if (password.length < 6) {
        showNotification('Password must be at least 6 characters!', 'error');
        return false;
    }
    
    return true;
}

function validateEditUserForm() {
    const password = document.getElementById('editPassword').value;
    const confirmPassword = document.getElementById('editConfirmPassword').value;
    
    // If password is provided, validate it
    if (password && password.length < 6) {
        showNotification('Password must be at least 6 characters!', 'error');
        return false;
    }
    
    if (password && password !== confirmPassword) {
        showNotification('Passwords do not match!', 'error');
        return false;
    }
    
    return true;
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

// Export functions for global access
window.showAddUserModal = showAddUserModal;
window.closeAddUserModal = closeAddUserModal;
window.submitAddUser = submitAddUser;
window.editUser = editUser;
window.closeEditUserModal = closeEditUserModal;
window.submitEditUser = submitEditUser;
window.deleteUser = deleteUser;
