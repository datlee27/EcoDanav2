// Admin Vehicles Management JavaScript
(function() {
    'use strict';

    // State management
    var state = {
        vehicles: [],
        filteredVehicles: [],
        searchTerm: '',
        typeFilter: '',
        statusFilter: '',
        deleteVehicleId: null
    };

    // DOM Elements
    var elements = {
        searchInput: document.getElementById('searchInput'),
        typeFilter: document.getElementById('typeFilter'),
        statusFilter: document.getElementById('statusFilter'),
        clearFilters: document.getElementById('clearFilters'),
        vehiclesContainer: document.getElementById('vehiclesContainer'),
        loadingSpinner: document.getElementById('loadingSpinner'),
        noResults: document.getElementById('noResults'),
        deleteModal: document.getElementById('deleteModal'),
        confirmDelete: document.getElementById('confirmDelete'),
        cancelDelete: document.getElementById('cancelDelete'),
        availableCount: document.getElementById('availableCount'),
        rentedCount: document.getElementById('rentedCount'),
        maintenanceCount: document.getElementById('maintenanceCount')
    };

    // Utility: Debounce function
    function debounce(func, wait) {
        var timeout;
        return function executedFunction() {
            var context = this;
            var args = arguments;
            var later = function() {
                timeout = null;
                func.apply(context, args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Utility: Format currency
    function formatCurrency(amount) {
        if (!amount) return '0 ₫';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    // Utility: Get status badge HTML
    function getStatusBadge(status) {
        var badges = {
            'Available': '<span class="status-badge available"><i class="fas fa-check-circle"></i> Sẵn Sàng</span>',
            'Rented': '<span class="status-badge rented"><i class="fas fa-key"></i> Đang Thuê</span>',
            'Maintenance': '<span class="status-badge maintenance"><i class="fas fa-wrench"></i> Bảo Trì</span>',
            'Unavailable': '<span class="status-badge unavailable"><i class="fas fa-ban"></i> Không Khả Dụng</span>'
        };
        return badges[status] || badges['Unavailable'];
    }

    // Utility: Get vehicle type label
    function getVehicleTypeLabel(type) {
        return type === 'ElectricCar' ? 'Xe Ô Tô Điện' : 'Xe Máy Điện';
    }

    // Fetch vehicles from API
    function fetchVehicles() {
        showLoading();
        
        fetch('/admin/vehicles/api/list')
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    state.vehicles = data.vehicles;
                    state.filteredVehicles = data.vehicles;
                    updateStatistics();
                    renderVehicles();
                } else {
                    showError('Không thể tải danh sách xe');
                }
            })
            .catch(function(error) {
                console.error('Error fetching vehicles:', error);
                showError('Có lỗi xảy ra khi tải dữ liệu');
            })
            .finally(function() {
                hideLoading();
            });
    }

    // Update statistics
    function updateStatistics() {
        var available = state.vehicles.filter(function(v) { return v.status === 'Available'; }).length;
        var rented = state.vehicles.filter(function(v) { return v.status === 'Rented'; }).length;
        var maintenance = state.vehicles.filter(function(v) { return v.status === 'Maintenance'; }).length;

        if (elements.availableCount) elements.availableCount.textContent = available;
        if (elements.rentedCount) elements.rentedCount.textContent = rented;
        if (elements.maintenanceCount) elements.maintenanceCount.textContent = maintenance;
    }

    // Filter vehicles
    function filterVehicles() {
        state.filteredVehicles = state.vehicles.filter(function(vehicle) {
            var matchesSearch = true;
            var matchesType = true;
            var matchesStatus = true;

            // Search filter
            if (state.searchTerm) {
                var searchLower = state.searchTerm.toLowerCase();
                matchesSearch = 
                    vehicle.vehicleModel.toLowerCase().indexOf(searchLower) !== -1 ||
                    vehicle.licensePlate.toLowerCase().indexOf(searchLower) !== -1;
            }

            // Type filter
            if (state.typeFilter) {
                matchesType = vehicle.vehicleType === state.typeFilter;
            }

            // Status filter
            if (state.statusFilter) {
                matchesStatus = vehicle.status === state.statusFilter;
            }

            return matchesSearch && matchesType && matchesStatus;
        });

        renderVehicles();
    }

    // Render vehicles
    function renderVehicles() {
        if (!elements.vehiclesContainer) return;

        if (state.filteredVehicles.length === 0) {
            elements.vehiclesContainer.innerHTML = '';
            elements.noResults.classList.remove('hidden');
            return;
        }

        elements.noResults.classList.add('hidden');
        
        var html = state.filteredVehicles.map(function(vehicle) {
            var imageUrl = vehicle.mainImageUrl || 'https://via.placeholder.com/400x300?text=No+Image';
            
            return '<div class="vehicle-card">' +
                '<img src="' + imageUrl + '" alt="' + vehicle.vehicleModel + '" class="vehicle-card-image" ' +
                'onerror="this.src=\'https://via.placeholder.com/400x300?text=No+Image\'">' +
                '<div class="vehicle-card-body">' +
                '<div class="flex justify-between items-start mb-2">' +
                '<h3 class="vehicle-card-title">' + vehicle.vehicleModel + '</h3>' +
                getStatusBadge(vehicle.status) +
                '</div>' +
                '<p class="vehicle-card-subtitle">' +
                '<i class="fas fa-id-card mr-1"></i>' + vehicle.licensePlate +
                '</p>' +
                '<div class="vehicle-card-info">' +
                '<span class="vehicle-card-info-item">' +
                '<i class="fas fa-car"></i>' + getVehicleTypeLabel(vehicle.vehicleType) +
                '</span>' +
                '<span class="vehicle-card-info-item">' +
                '<i class="fas fa-users"></i>' + vehicle.seats + ' chỗ' +
                '</span>' +
                '<span class="vehicle-card-info-item">' +
                '<i class="fas fa-tachometer-alt"></i>' + formatNumber(vehicle.odometer) + ' km' +
                '</span>' +
                '</div>' +
                '<div class="vehicle-card-price">' +
                formatCurrency(vehicle.dailyPrice) +
                '<span class="vehicle-card-price-label">/ngày</span>' +
                '</div>' +
                '<div class="vehicle-card-actions">' +
                '<a href="/admin/vehicles/detail/' + vehicle.vehicleId + '" class="btn btn-outline btn-sm flex-1">' +
                '<i class="fas fa-eye"></i>Chi Tiết' +
                '</a>' +
                '<a href="/admin/vehicles/edit/' + vehicle.vehicleId + '" class="btn btn-secondary btn-sm flex-1">' +
                '<i class="fas fa-edit"></i>Sửa' +
                '</a>' +
                '<button onclick="showDeleteModal(\'' + vehicle.vehicleId + '\')" class="btn btn-danger btn-sm flex-1">' +
                '<i class="fas fa-trash"></i>Xóa' +
                '</button>' +
                '</div>' +
                '</div>' +
                '</div>';
        }).join('');

        elements.vehiclesContainer.innerHTML = html;
    }

    // Format number with commas
    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    // Show loading spinner
    function showLoading() {
        if (elements.loadingSpinner) {
            elements.loadingSpinner.classList.remove('hidden');
        }
        if (elements.vehiclesContainer) {
            elements.vehiclesContainer.classList.add('hidden');
        }
    }

    // Hide loading spinner
    function hideLoading() {
        if (elements.loadingSpinner) {
            elements.loadingSpinner.classList.add('hidden');
        }
        if (elements.vehiclesContainer) {
            elements.vehiclesContainer.classList.remove('hidden');
        }
    }

    // Show error message
    function showError(message) {
        alert(message);
    }

    // Show delete modal
    window.showDeleteModal = function(vehicleId) {
        state.deleteVehicleId = vehicleId;
        if (elements.deleteModal) {
            elements.deleteModal.classList.remove('hidden');
            elements.deleteModal.classList.add('flex');
        }
    };

    // Hide delete modal
    function hideDeleteModal() {
        state.deleteVehicleId = null;
        if (elements.deleteModal) {
            elements.deleteModal.classList.add('hidden');
            elements.deleteModal.classList.remove('flex');
        }
    }

    // Delete vehicle
    function deleteVehicle() {
        if (!state.deleteVehicleId) return;

        fetch('/admin/vehicles/api/delete/' + state.deleteVehicleId, {
            method: 'DELETE'
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                hideDeleteModal();
                fetchVehicles(); // Reload vehicles
                alert('Xóa xe thành công!');
            } else {
                alert('Lỗi: ' + data.message);
            }
        })
        .catch(function(error) {
            console.error('Error deleting vehicle:', error);
            alert('Có lỗi xảy ra khi xóa xe');
        });
    }

    // Clear all filters
    function clearFilters() {
        state.searchTerm = '';
        state.typeFilter = '';
        state.statusFilter = '';
        
        if (elements.searchInput) elements.searchInput.value = '';
        if (elements.typeFilter) elements.typeFilter.value = '';
        if (elements.statusFilter) elements.statusFilter.value = '';
        
        filterVehicles();
    }

    // Event listeners
    function initEventListeners() {
        // Search input with debounce
        if (elements.searchInput) {
            elements.searchInput.addEventListener('input', debounce(function(e) {
                state.searchTerm = e.target.value;
                filterVehicles();
            }, 300));
        }

        // Type filter
        if (elements.typeFilter) {
            elements.typeFilter.addEventListener('change', function(e) {
                state.typeFilter = e.target.value;
                filterVehicles();
            });
        }

        // Status filter
        if (elements.statusFilter) {
            elements.statusFilter.addEventListener('change', function(e) {
                state.statusFilter = e.target.value;
                filterVehicles();
            });
        }

        // Clear filters button
        if (elements.clearFilters) {
            elements.clearFilters.addEventListener('click', clearFilters);
        }

        // Delete modal buttons
        if (elements.confirmDelete) {
            elements.confirmDelete.addEventListener('click', deleteVehicle);
        }

        if (elements.cancelDelete) {
            elements.cancelDelete.addEventListener('click', hideDeleteModal);
        }

        // Close modal on backdrop click
        if (elements.deleteModal) {
            elements.deleteModal.addEventListener('click', function(e) {
                if (e.target === elements.deleteModal) {
                    hideDeleteModal();
                }
            });
        }

        // Keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            // ESC to close modal
            if (e.key === 'Escape' && elements.deleteModal && !elements.deleteModal.classList.contains('hidden')) {
                hideDeleteModal();
            }
            
            // Ctrl/Cmd + K to focus search
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                if (elements.searchInput) {
                    elements.searchInput.focus();
                }
            }
        });
    }

    // Initialize
    function init() {
        initEventListeners();
        fetchVehicles();
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
