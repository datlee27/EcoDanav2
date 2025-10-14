// Owner Cars Management JavaScript
// Includes: Form Validation, Confirm Dialog, Image Preview

// ============================================
// Form Validation
// ============================================

function validateCarForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    let isValid = true;
    const errors = [];

    // Model validation
    const model = form.querySelector('[name="model"]');
    if (!model || !model.value.trim()) {
        errors.push('Vehicle model is required');
        isValid = false;
        highlightError(model);
    }

    // License Plate validation
    const licensePlate = form.querySelector('[name="licensePlate"]');
    if (!licensePlate || !licensePlate.value.trim()) {
        errors.push('License plate is required');
        isValid = false;
        highlightError(licensePlate);
    }

    // Seats validation
    const seats = form.querySelector('[name="seats"]');
    if (seats && seats.value) {
        const seatsValue = parseInt(seats.value);
        if (seatsValue < 1 || seatsValue > 50) {
            errors.push('Seats must be between 1 and 50');
            isValid = false;
            highlightError(seats);
        }
    }

    // Odometer validation
    const odometer = form.querySelector('[name="odometer"]');
    if (odometer && odometer.value) {
        const odometerValue = parseInt(odometer.value);
        if (odometerValue < 0) {
            errors.push('Odometer cannot be negative');
            isValid = false;
            highlightError(odometer);
        }
    }

    // Rental rates validation
    const hourlyRate = form.querySelector('[name="hourlyRate"]');
    const dailyRate = form.querySelector('[name="dailyRate"]');
    const monthlyRate = form.querySelector('[name="monthlyRate"]');

    if (hourlyRate && hourlyRate.value && parseFloat(hourlyRate.value) < 0) {
        errors.push('Hourly rate cannot be negative');
        isValid = false;
        highlightError(hourlyRate);
    }

    if (dailyRate && dailyRate.value && parseFloat(dailyRate.value) < 0) {
        errors.push('Daily rate cannot be negative');
        isValid = false;
        highlightError(dailyRate);
    }

    if (monthlyRate && monthlyRate.value && parseFloat(monthlyRate.value) < 0) {
        errors.push('Monthly rate cannot be negative');
        isValid = false;
        highlightError(monthlyRate);
    }

    // Battery capacity validation
    const batteryCapacity = form.querySelector('[name="batteryCapacity"]');
    if (batteryCapacity && batteryCapacity.value) {
        const capacity = parseFloat(batteryCapacity.value);
        if (capacity < 0 || capacity > 1000) {
            errors.push('Battery capacity must be between 0 and 1000 kWh');
            isValid = false;
            highlightError(batteryCapacity);
        }
    }

    // Year validation
    const yearManufactured = form.querySelector('[name="yearManufactured"]');
    if (yearManufactured && yearManufactured.value) {
        const year = parseInt(yearManufactured.value);
        const currentYear = new Date().getFullYear();
        if (year < 1900 || year > currentYear + 1) {
            errors.push(`Year must be between 1900 and ${currentYear + 1}`);
            isValid = false;
            highlightError(yearManufactured);
        }
    }

    // Show errors
    if (!isValid) {
        showValidationErrors(errors);
    }

    return isValid;
}

function highlightError(element) {
    if (!element) return;
    element.classList.add('border-red-500', 'border-2');
    element.addEventListener('input', function() {
        element.classList.remove('border-red-500', 'border-2');
    }, { once: true });
}

function showValidationErrors(errors) {
    const errorHtml = `
        <div class="fixed top-20 right-4 bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded shadow-lg z-50" id="validation-errors">
            <div class="flex items-start">
                <i class="fas fa-exclamation-circle text-xl mr-3 mt-1"></i>
                <div>
                    <p class="font-bold mb-2">Please fix the following errors:</p>
                    <ul class="list-disc list-inside">
                        ${errors.map(err => `<li>${err}</li>`).join('')}
                    </ul>
                </div>
                <button onclick="closeValidationErrors()" class="ml-4 text-red-700 hover:text-red-900">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;
    
    // Remove existing errors
    const existing = document.getElementById('validation-errors');
    if (existing) existing.remove();
    
    // Add new errors
    document.body.insertAdjacentHTML('beforeend', errorHtml);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        closeValidationErrors();
    }, 5000);
}

function closeValidationErrors() {
    const errors = document.getElementById('validation-errors');
    if (errors) {
        errors.style.opacity = '0';
        errors.style.transform = 'translateX(100%)';
        setTimeout(() => errors.remove(), 300);
    }
}

// ============================================
// Confirm Dialog Before Delete
// ============================================

function confirmDeleteCar(vehicleId, vehicleName) {
    const modal = `
        <div id="delete-confirm-modal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
            <div class="bg-white rounded-xl shadow-2xl max-w-md w-full transform transition-all">
                <div class="p-6">
                    <div class="flex items-center justify-center w-12 h-12 mx-auto bg-red-100 rounded-full mb-4">
                        <i class="fas fa-trash-alt text-2xl text-red-600"></i>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 text-center mb-2">Delete Vehicle</h3>
                    <p class="text-gray-600 text-center mb-6">
                        Are you sure you want to delete <strong>${vehicleName || 'this vehicle'}</strong>?
                        <br><span class="text-sm text-red-600">This action cannot be undone.</span>
                    </p>
                    <div class="flex space-x-3">
                        <button onclick="closeDeleteModal()" 
                                class="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors">
                            Cancel
                        </button>
                        <button onclick="executeDeleteCar('${vehicleId}')" 
                                class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                            <i class="fas fa-trash-alt mr-2"></i>Delete
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modal);
}

function closeDeleteModal() {
    const modal = document.getElementById('delete-confirm-modal');
    if (modal) {
        modal.style.opacity = '0';
        setTimeout(() => modal.remove(), 200);
    }
}

function executeDeleteCar(vehicleId) {
    fetch(`/owner/cars/${vehicleId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        closeDeleteModal();
        if (data.status === 'success') {
            showSuccessMessage(data.message || 'Vehicle deleted successfully');
            setTimeout(() => {
                location.reload();
            }, 1000);
        } else {
            showErrorMessage(data.message || 'Failed to delete vehicle');
        }
    })
    .catch(error => {
        closeDeleteModal();
        showErrorMessage('An error occurred while deleting the vehicle');
        console.error('Delete error:', error);
    });
}

// ============================================
// Image Preview
// ============================================

function setupImagePreview(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    
    if (!input) return;
    
    input.addEventListener('change', function(e) {
        const file = e.target.files[0];
        
        if (!file) {
            clearImagePreview(previewId);
            return;
        }
        
        // Validate file type
        if (!file.type.startsWith('image/')) {
            showErrorMessage('Please select an image file');
            input.value = '';
            clearImagePreview(previewId);
            return;
        }
        
        // Validate file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            showErrorMessage('Image size must be less than 5MB');
            input.value = '';
            clearImagePreview(previewId);
            return;
        }
        
        // Show preview
        const reader = new FileReader();
        reader.onload = function(e) {
            showImagePreview(previewId, e.target.result, file.name);
        };
        reader.readAsDataURL(file);
    });
}

function showImagePreview(previewId, imageSrc, fileName) {
    const container = document.getElementById(previewId);
    if (!container) return;
    
    container.innerHTML = `
        <div class="relative inline-block">
            <img src="${imageSrc}" alt="Preview" class="max-w-full h-48 rounded-lg border-2 border-gray-300 object-cover">
            <div class="mt-2 text-sm text-gray-600">
                <i class="fas fa-image mr-1"></i>${fileName}
            </div>
            <button type="button" onclick="clearImagePreview('${previewId}')" 
                    class="absolute top-2 right-2 bg-red-500 text-white rounded-full w-8 h-8 flex items-center justify-center hover:bg-red-600 transition-colors">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    container.classList.remove('hidden');
}

function clearImagePreview(previewId) {
    const container = document.getElementById(previewId);
    if (container) {
        container.innerHTML = '';
        container.classList.add('hidden');
    }
}

// ============================================
// Helper Functions
// ============================================

function showSuccessMessage(message) {
    const html = `
        <div class="fixed top-20 right-4 bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded shadow-lg z-50 transition-all" id="success-message">
            <div class="flex items-center">
                <i class="fas fa-check-circle text-xl mr-3"></i>
                <p>${message}</p>
                <button onclick="closeSuccessMessage()" class="ml-4 text-green-700 hover:text-green-900">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;
    
    const existing = document.getElementById('success-message');
    if (existing) existing.remove();
    
    document.body.insertAdjacentHTML('beforeend', html);
    
    setTimeout(() => {
        closeSuccessMessage();
    }, 3000);
}

function closeSuccessMessage() {
    const msg = document.getElementById('success-message');
    if (msg) {
        msg.style.opacity = '0';
        msg.style.transform = 'translateX(100%)';
        setTimeout(() => msg.remove(), 300);
    }
}

function showErrorMessage(message) {
    const html = `
        <div class="fixed top-20 right-4 bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded shadow-lg z-50 transition-all" id="error-message">
            <div class="flex items-center">
                <i class="fas fa-exclamation-circle text-xl mr-3"></i>
                <p>${message}</p>
                <button onclick="closeErrorMessage()" class="ml-4 text-red-700 hover:text-red-900">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;
    
    const existing = document.getElementById('error-message');
    if (existing) existing.remove();
    
    document.body.insertAdjacentHTML('beforeend', html);
    
    setTimeout(() => {
        closeErrorMessage();
    }, 5000);
}

function closeErrorMessage() {
    const msg = document.getElementById('error-message');
    if (msg) {
        msg.style.opacity = '0';
        msg.style.transform = 'translateX(100%)';
        setTimeout(() => msg.remove(), 300);
    }
}

// ============================================
// Initialize on Page Load
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Setup image preview for add car form
    setupImagePreview('add-car-image', 'add-car-preview');
    
    // Setup image preview for edit car form
    setupImagePreview('edit-car-image', 'edit-car-preview');
    
    // Add form validation to add car form
    const addCarForm = document.getElementById('add-car-form');
    if (addCarForm) {
        addCarForm.addEventListener('submit', function(e) {
            if (!validateCarForm('add-car-form')) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    // Add form validation to edit car form
    const editCarForm = document.getElementById('edit-car-form');
    if (editCarForm) {
        editCarForm.addEventListener('submit', function(e) {
            if (!validateCarForm('edit-car-form')) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    console.log('Owner Cars Management JS initialized');
});
