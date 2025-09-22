// src/main/resources/static/js/owner-dashboard.js
function showSection(sectionId) {
    document.querySelectorAll('.dashboard-section').forEach(section => {
        section.classList.add('hidden');
    });
    document.getElementById(`${sectionId}-section`).classList.remove('hidden');
}

function showAddCarModal() {
    // Giả lập hiển thị modal thêm xe
    alert('Add new car modal opened');
}

function editCar(carId) {
    fetch(`/owner/cars/${carId}/edit`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            // Giả lập hiển thị modal chỉnh sửa với dữ liệu xe
            alert(`Edit car ID: ${data.id}, Model: ${data.brand} ${data.model}`);
        });
}

function deleteCar(carId) {
    if (confirm('Are you sure you want to delete this car?')) {
        fetch(`/owner/cars/${carId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(response => {
                if (response.ok) {
                    alert('Car deleted successfully');
                    location.reload();
                }
            });
    }
}

function acceptBooking(bookingId) {
    fetch(`/owner/bookings/${bookingId}/accept`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                alert('Booking accepted');
                location.reload();
            }
        });
}

function declineBooking(bookingId) {
    if (confirm('Are you sure you want to decline this booking?')) {
        fetch(`/owner/bookings/${bookingId}/decline`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(response => {
                if (response.ok) {
                    alert('Booking declined');
                    location.reload();
                }
            });
    }
}

function viewBooking(bookingId) {
    // Giả lập xem chi tiết booking
    alert(`View booking ID: ${bookingId}`);
}

// Filter bookings
document.querySelectorAll('.filter-btn').forEach(button => {
    button.addEventListener('click', () => {
        const status = button.getAttribute('data-status');
        document.querySelectorAll('tbody tr').forEach(row => {
            if (status === 'all' || row.getAttribute('data-status') === status) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
        document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.replace('bg-primary', 'bg-gray-100'));
        button.classList.replace('bg-gray-100', 'bg-primary');
    });
});