// Owner Management JavaScript

let currentBookingId = null;
let hiddenStatuses = new Set();
let timelineHiddenStatuses = new Set();
let currentGrouping = "month";
let bookingsData = [];

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  // Show success/error notifications
  showNotifications();

  // Load bookings data for timeline
  loadTimelineData();

  // Set default filter
  filterBookings();
});

// Tab Switching
function switchTab(tabName) {
  // Hide all tab contents
  document.querySelectorAll(".tab-content").forEach((content) => {
    content.classList.add("hidden");
  });

  // Remove active class from all tabs
  document.querySelectorAll(".tab-button").forEach((button) => {
    button.classList.remove("active", "border-primary", "text-primary");
    button.classList.add("border-transparent", "text-gray-600");
  });

  // Show selected tab content
  document.getElementById(tabName + "-section").classList.remove("hidden");

  // Add active class to selected tab
  const activeTab = document.getElementById(tabName + "-tab");
  activeTab.classList.add("active", "border-primary", "text-primary");
  activeTab.classList.remove("border-transparent", "text-gray-600");

  // Refresh calendar if switching to calendar tab
  if (tabName === "calendar" && calendar) {
    setTimeout(() => {
      calendar.updateSize();
      calendar.refetchEvents();
    }, 100);
  }
}

// Filter Bookings
function filterBookings() {
  const statusFilter = document.getElementById("status-filter").value.toLowerCase();
  const searchInput = document.getElementById("search-input").value.toLowerCase();
  const sortSelect = document.getElementById("sort-select").value;

  const rows = Array.from(document.querySelectorAll(".booking-row"));

  // Filter rows
  const visibleRows = rows.filter((row) => {
    const status = row.getAttribute("data-status").toLowerCase();
    const code = row.getAttribute("data-code").toLowerCase();
    const customer = row.getAttribute("data-customer").toLowerCase();

    const statusMatch = !statusFilter || status === statusFilter;
    const searchMatch = !searchInput || code.includes(searchInput) || customer.includes(searchInput);

    const isVisible = statusMatch && searchMatch;
    row.style.display = isVisible ? "" : "none";

    return isVisible;
  });

  // Sort rows
  visibleRows.sort((a, b) => {
    switch (sortSelect) {
      case "newest":
        return new Date(b.getAttribute("data-created")) - new Date(a.getAttribute("data-created"));
      case "oldest":
        return new Date(a.getAttribute("data-created")) - new Date(b.getAttribute("data-created"));
      case "amount-high":
        return parseFloat(b.getAttribute("data-amount")) - parseFloat(a.getAttribute("data-amount"));
      case "amount-low":
        return parseFloat(a.getAttribute("data-amount")) - parseFloat(b.getAttribute("data-amount"));
      default:
        return 0;
    }
  });

  // Reorder rows in table
  const tbody = document.getElementById("bookings-table-body");
  visibleRows.forEach((row) => tbody.appendChild(row));
}

// Load Timeline Data
function loadTimelineData() {
  fetch(contextPath + "owner/management/calendar-events")
    .then((response) => response.json())
    .then((events) => {
      bookingsData = events;
      renderTimeline();
    })
    .catch((error) => {
      console.error("Error loading timeline data:", error);
      document.getElementById("timeline-container").innerHTML = `
        <div class="text-center py-8">
          <i class="fas fa-exclamation-circle text-4xl text-red-500"></i>
          <p class="mt-2 text-gray-600">Failed to load booking data</p>
        </div>
      `;
    });
}

// Change Grouping (Day/Month/Year)
function changeGrouping(grouping) {
  currentGrouping = grouping;
  renderTimeline();
}

// Toggle Timeline Status Filter
function toggleTimelineStatusFilter(status) {
  if (timelineHiddenStatuses.has(status)) {
    timelineHiddenStatuses.delete(status);
  } else {
    timelineHiddenStatuses.add(status);
  }

  // Update button appearance
  const btn = document.querySelector(`[data-status="${status}"]`);
  if (btn) {
    btn.classList.toggle("opacity-50");
  }

  renderTimeline();
}

// Render Timeline Table
function renderTimeline() {
  const container = document.getElementById("timeline-container");

  // Filter bookings based on hidden statuses
  const filteredBookings = bookingsData.filter((booking) => !timelineHiddenStatuses.has(booking.status));

  if (filteredBookings.length === 0) {
    container.innerHTML = `
      <div class="text-center py-12">
        <i class="fas fa-calendar-times text-4xl text-gray-300"></i>
        <p class="mt-3 text-gray-500 text-sm">No bookings found</p>
      </div>
    `;
    return;
  }

  // Group bookings
  const grouped = groupBookings(filteredBookings, currentGrouping);

  // Generate HTML
  let html = '<div class="space-y-4">';

  Object.keys(grouped)
    .sort()
    .reverse()
    .forEach((groupKey) => {
      const bookings = grouped[groupKey];
      const groupLabel = formatGroupLabel(groupKey, currentGrouping);
      const totalBookings = bookings.length;
      const totalRevenue = bookings.reduce((sum, b) => sum + parseFloat(b.extendedProps?.amount || 0), 0);

      html += `
      <div class="border border-gray-200 rounded-lg overflow-hidden">
        <!-- Simple Group Header -->
        <div class="bg-gray-50 px-4 py-3 flex justify-between items-center cursor-pointer hover:bg-gray-100 transition-colors"
             onclick="toggleGroup('${groupKey}')">
          <div class="flex items-center space-x-3">
            <i class="fas fa-chevron-down text-gray-400 text-sm transition-transform" id="icon-${groupKey}"></i>
            <div>
              <h4 class="font-medium text-gray-800">${groupLabel}</h4>
               <p class="text-xs text-gray-500">${totalBookings} booking${
        totalBookings > 1 ? "s" : ""
      } • Total: ${totalRevenue.toLocaleString("vi-VN")} VND</p>
            </div>
          </div>
        </div>
        
        <!-- Group Content -->
        <div id="group-${groupKey}" class="group-content">
          <table class="min-w-full">
            <thead class="bg-gray-50 border-y border-gray-200">
              <tr>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Code</th>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Customer</th>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Vehicle</th>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Dates</th>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Amount</th>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-600">Status</th>
                <th class="px-4 py-2 text-center text-xs font-medium text-gray-600">Action</th>
              </tr>
            </thead>
            <tbody class="bg-white">
    `;

      bookings.forEach((booking) => {
        const statusClass = getStatusClass(booking.status);
        const amount = parseFloat(booking.extendedProps?.amount || 0);
        html += `
        <tr class="border-b border-gray-100 hover:bg-gray-50">
          <td class="px-4 py-3">
            <span class="text-sm font-medium text-primary">${booking.extendedProps?.bookingCode || "N/A"}</span>
          </td>
          <td class="px-4 py-3">
            <p class="text-sm text-gray-800">${booking.extendedProps?.customerName || "N/A"}</p>
          </td>
          <td class="px-4 py-3">
            <p class="text-sm text-gray-800">${booking.extendedProps?.vehicleModel || "N/A"}</p>
          </td>
          <td class="px-4 py-3">
            <p class="text-xs text-gray-600">${formatDate(booking.start)}</p>
            <p class="text-xs text-gray-500">${formatDate(booking.end)}</p>
          </td>
           <td class="px-4 py-3">
             <span class="text-sm font-medium text-gray-800">${amount.toLocaleString("vi-VN")} VND</span>
           </td>
          <td class="px-4 py-3">
            <span class="status-badge ${statusClass} text-xs">${booking.status}</span>
          </td>
          <td class="px-4 py-3 text-center">
            <button onclick="viewBookingDetailFromCalendar('${booking.id}')" 
                    class="text-primary hover:text-primary-dark text-sm"
                    title="View details">
              <i class="fas fa-eye"></i>
            </button>
          </td>
        </tr>
      `;
      });

      html += `
            </tbody>
          </table>
        </div>
      </div>
    `;
    });

  html += "</div>";
  container.innerHTML = html;
}

// Group Bookings by Period
function groupBookings(bookings, groupBy) {
  const grouped = {};

  bookings.forEach((booking) => {
    const date = new Date(booking.start);
    let key;

    if (groupBy === "day") {
      key = date.toISOString().split("T")[0]; // YYYY-MM-DD
    } else if (groupBy === "month") {
      key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`; // YYYY-MM
    } else if (groupBy === "year") {
      key = String(date.getFullYear()); // YYYY
    }

    if (!grouped[key]) {
      grouped[key] = [];
    }
    grouped[key].push(booking);
  });

  return grouped;
}

// Format Group Label
function formatGroupLabel(key, groupBy) {
  if (groupBy === "day") {
    const date = new Date(key);
    return date.toLocaleDateString("en-US", { weekday: "long", year: "numeric", month: "long", day: "numeric" });
  } else if (groupBy === "month") {
    const [year, month] = key.split("-");
    const date = new Date(year, parseInt(month) - 1);
    return date.toLocaleDateString("en-US", { year: "numeric", month: "long" });
  } else if (groupBy === "year") {
    return key;
  }
  return key;
}

// Toggle Group Collapse
function toggleGroup(groupKey) {
  const element = document.getElementById(`group-${groupKey}`);
  const icon = document.getElementById(`icon-${groupKey}`);
  if (element) {
    element.classList.toggle("collapsed");
    if (icon) {
      icon.classList.toggle("rotate-180");
    }
  }
}

// Get Status Class
function getStatusClass(status) {
  const statusMap = {
    Pending: "status-pending",
    Approved: "status-approved",
    Ongoing: "status-ongoing",
    Completed: "status-completed",
    Rejected: "status-rejected",
    Cancelled: "status-cancelled",
  };
  return statusMap[status] || "";
}

// Format Date
function formatDate(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" });
}

// Toggle Status Filter for Calendar
function toggleStatusFilter(status) {
  if (hiddenStatuses.has(status)) {
    hiddenStatuses.delete(status);
  } else {
    hiddenStatuses.add(status);
  }

  // Update button appearance
  const buttons = document.querySelectorAll(".filter-btn");
  buttons.forEach((btn) => {
    const btnText = btn.textContent.trim();
    if (btnText.includes(status)) {
      btn.classList.toggle("inactive");
    }
  });

  // Refresh calendar events
  if (calendar) {
    calendar.refetchEvents();
  }
}

// View Booking Detail
function viewBookingDetail(button) {
  const bookingId = button.getAttribute("data-booking-id");
  loadBookingDetail(bookingId);
}

// View Booking Detail from Calendar
function viewBookingDetailFromCalendar(bookingId) {
  loadBookingDetail(bookingId);
}

// Load Booking Detail
function loadBookingDetail(bookingId) {
  const modal = document.getElementById("booking-detail-modal");
  const content = document.getElementById("booking-detail-content");

  // Show modal with loading state
  modal.classList.remove("hidden");
  content.innerHTML = `
        <div class="text-center py-8">
            <i class="fas fa-spinner fa-spin text-4xl text-primary"></i>
            <p class="mt-2 text-gray-600">Loading...</p>
        </div>
    `;

  // Fetch booking details
  fetch(contextPath + "owner/management/bookings/" + bookingId)
    .then((response) => response.json())
    .then((booking) => {
      content.innerHTML = generateBookingDetailHTML(booking);
    })
    .catch((error) => {
      console.error("Error loading booking details:", error);
      content.innerHTML = `
                <div class="text-center py-8">
                    <i class="fas fa-exclamation-circle text-4xl text-red-500"></i>
                    <p class="mt-2 text-gray-600">Failed to load booking details</p>
                </div>
            `;
    });
}

// Generate Booking Detail HTML
function generateBookingDetailHTML(booking) {
  return `
        <div class="space-y-6">
            <!-- Booking Status -->
            <div class="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                <div>
                    <p class="text-sm text-gray-600">Booking Code</p>
                    <p class="text-xl font-bold text-primary">${booking.bookingCode}</p>
                </div>
                <span class="status-badge status-${booking.status.toLowerCase()}">${booking.status}</span>
            </div>
            
            <!-- Customer Information -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3 flex items-center">
                    <i class="fas fa-user mr-2 text-primary"></i>Customer Information
                </h4>
                <div class="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                    <div>
                        <p class="text-sm text-gray-600">Full Name</p>
                        <p class="font-semibold">${booking.customerName || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Email</p>
                        <p class="font-semibold">${booking.customerEmail || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Phone</p>
                        <p class="font-semibold">${booking.customerPhone || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Date of Birth</p>
                        <p class="font-semibold">${booking.customerDOB || "N/A"}</p>
                    </div>
                    <div class="col-span-2">
                        <button onclick="viewCustomerProfile('${booking.customerId}')" 
                                class="text-primary hover:text-primary-dark font-semibold">
                            <i class="fas fa-external-link-alt mr-1"></i>View Full Profile
                        </button>
                    </div>
                </div>
            </div>
            
            <!-- Vehicle Information -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3 flex items-center">
                    <i class="fas fa-car mr-2 text-primary"></i>Vehicle Information
                </h4>
                <div class="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                    <div>
                        <p class="text-sm text-gray-600">Model</p>
                        <p class="font-semibold">${booking.vehicleModel || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">License Plate</p>
                        <p class="font-semibold">${booking.licensePlate || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Category</p>
                        <p class="font-semibold">${booking.vehicleCategory || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Transmission</p>
                        <p class="font-semibold">${booking.transmission || "N/A"}</p>
                    </div>
                </div>
            </div>
            
            <!-- Booking Details -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3 flex items-center">
                    <i class="fas fa-calendar-check mr-2 text-primary"></i>Booking Details
                </h4>
                <div class="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                    <div>
                        <p class="text-sm text-gray-600">Pickup Date & Time</p>
                        <p class="font-semibold">${formatDateTime(booking.pickupDateTime)}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Return Date & Time</p>
                        <p class="font-semibold">${formatDateTime(booking.returnDateTime)}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Rental Type</p>
                        <p class="font-semibold">${booking.rentalType || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Created Date</p>
                        <p class="font-semibold">${formatDateTime(booking.createdDate)}</p>
                    </div>
                </div>
            </div>
            
            <!-- Financial Information -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3 flex items-center">
                    <i class="fas fa-money-bill-wave mr-2 text-primary"></i>Financial Information
                </h4>
                <div class="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                    <div>
                        <p class="text-sm text-gray-600">Total Amount</p>
                        <p class="text-xl font-bold text-primary">${formatCurrency(booking.totalAmount)} VND</p>
                    </div>
                    <div>
                        <p class="text-sm text-gray-600">Payment Method</p>
                        <p class="font-semibold">${booking.paymentMethod || "N/A"}</p>
                    </div>
                    ${
                      booking.discount
                        ? `
                    <div class="col-span-2">
                        <p class="text-sm text-gray-600">Discount Applied</p>
                        <p class="font-semibold text-green-600">${booking.discount}</p>
                    </div>
                    `
                        : ""
                    }
                </div>
            </div>
            
            ${
              booking.cancelReason
                ? `
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3 flex items-center">
                    <i class="fas fa-info-circle mr-2 text-red-500"></i>Cancellation Reason
                </h4>
                <div class="bg-red-50 p-4 rounded-lg">
                    <p class="text-gray-700">${booking.cancelReason}</p>
                </div>
            </div>
            `
                : ""
            }
            
            <!-- Actions -->
            <div class="flex justify-end space-x-3 pt-4 border-t">
                ${
                  booking.status === "Pending"
                    ? `
                    <button onclick="approveBookingFromDetail('${booking.bookingId}')" 
                            class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600">
                        <i class="fas fa-check mr-2"></i>Approve
                    </button>
                    <button onclick="rejectBookingFromDetail('${booking.bookingId}')" 
                            class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600">
                        <i class="fas fa-times mr-2"></i>Reject
                    </button>
                `
                    : ""
                }
                ${
                  booking.status === "Approved" || booking.status === "Ongoing"
                    ? `
                    <button onclick="completeBooking('${booking.bookingId}')" 
                            class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
                        <i class="fas fa-flag-checkered mr-2"></i>Mark as Completed
                    </button>
                    <button onclick="cancelBooking('${booking.bookingId}')" 
                            class="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600">
                        <i class="fas fa-ban mr-2"></i>Cancel Booking
                    </button>
                `
                    : ""
                }
            </div>
        </div>
    `;
}

// View Customer Profile
function viewCustomerProfile(customerId) {
  const modal = document.getElementById("customer-profile-modal");
  const content = document.getElementById("customer-profile-content");

  // Show modal with loading state
  modal.classList.remove("hidden");
  content.innerHTML = `
        <div class="text-center py-8">
            <i class="fas fa-spinner fa-spin text-4xl text-primary"></i>
            <p class="mt-2 text-gray-600">Loading...</p>
        </div>
    `;

  // Fetch customer profile
  fetch(contextPath + "owner/management/customer/" + customerId)
    .then((response) => response.json())
    .then((customer) => {
      content.innerHTML = generateCustomerProfileHTML(customer);
    })
    .catch((error) => {
      console.error("Error loading customer profile:", error);
      content.innerHTML = `
                <div class="text-center py-8">
                    <i class="fas fa-exclamation-circle text-4xl text-red-500"></i>
                    <p class="mt-2 text-gray-600">Failed to load customer profile</p>
                </div>
            `;
    });
}

// Generate Customer Profile HTML
function generateCustomerProfileHTML(customer) {
  return `
        <div class="space-y-6">
            <!-- Avatar and Basic Info -->
            <div class="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg">
                <div class="w-20 h-20 bg-primary rounded-full flex items-center justify-center">
                    ${
                      customer.avatarUrl
                        ? `<img src="${customer.avatarUrl}" alt="${customer.fullName}" class="w-20 h-20 rounded-full object-cover">`
                        : `<i class="fas fa-user text-white text-3xl"></i>`
                    }
                </div>
                <div>
                    <h3 class="text-2xl font-bold text-gray-800">${customer.fullName || "N/A"}</h3>
                    <p class="text-gray-600">${customer.email || "N/A"}</p>
                    <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      customer.status === "Active" ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
                    }">${customer.status || "Unknown"}</span>
                </div>
            </div>
            
            <!-- Personal Information -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3">Personal Information</h4>
                <div class="grid grid-cols-2 gap-4">
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">User ID</p>
                        <p class="font-semibold">${customer.userId || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Username</p>
                        <p class="font-semibold">${customer.username || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Email</p>
                        <p class="font-semibold">${customer.email || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Phone Number</p>
                        <p class="font-semibold">${customer.phoneNumber || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Date of Birth</p>
                        <p class="font-semibold">${customer.dateOfBirth || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Gender</p>
                        <p class="font-semibold">${customer.gender || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Role</p>
                        <p class="font-semibold">${customer.role || "N/A"}</p>
                    </div>
                    <div class="bg-gray-50 p-3 rounded">
                        <p class="text-sm text-gray-600">Member Since</p>
                        <p class="font-semibold">${formatDate(customer.createdDate)}</p>
                    </div>
                </div>
            </div>
            
            <!-- Account Statistics -->
            <div>
                <h4 class="text-lg font-bold text-gray-800 mb-3">Account Statistics</h4>
                <div class="grid grid-cols-3 gap-4">
                    <div class="bg-blue-50 p-4 rounded text-center">
                        <p class="text-2xl font-bold text-blue-600">${customer.totalBookings || 0}</p>
                        <p class="text-sm text-gray-600">Total Bookings</p>
                    </div>
                    <div class="bg-green-50 p-4 rounded text-center">
                        <p class="text-2xl font-bold text-green-600">${customer.completedBookings || 0}</p>
                        <p class="text-sm text-gray-600">Completed</p>
                    </div>
                    <div class="bg-yellow-50 p-4 rounded text-center">
                        <p class="text-2xl font-bold text-yellow-600">${customer.cancelledBookings || 0}</p>
                        <p class="text-sm text-gray-600">Cancelled</p>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Approve Booking
function approveBooking(button) {
  currentBookingId = button.getAttribute("data-booking-id");
  document.getElementById("approve-modal").classList.remove("hidden");
}

function approveBookingFromDetail(bookingId) {
  currentBookingId = bookingId;
  document.getElementById("approve-modal").classList.remove("hidden");
}

function confirmApprove() {
  if (!currentBookingId) return;

  fetch(contextPath + "owner/management/bookings/" + currentBookingId + "/approve", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        showSuccessMessage("Booking approved successfully!");
        closeModal("approve-modal");
        closeModal("booking-detail-modal");
        setTimeout(() => location.reload(), 1500);
      } else {
        showErrorMessage(data.message || "Failed to approve booking");
      }
    })
    .catch((error) => {
      console.error("Error approving booking:", error);
      showErrorMessage("An error occurred while approving the booking");
    });
}

// Reject Booking
function rejectBooking(button) {
  currentBookingId = button.getAttribute("data-booking-id");
  document.getElementById("reject-modal").classList.remove("hidden");
}

function rejectBookingFromDetail(bookingId) {
  currentBookingId = bookingId;
  document.getElementById("reject-modal").classList.remove("hidden");
}

function confirmReject() {
  if (!currentBookingId) return;

  const reason = document.getElementById("reject-reason").value.trim();
  if (!reason) {
    showErrorMessage("Please provide a rejection reason");
    return;
  }

  fetch(contextPath + "owner/management/bookings/" + currentBookingId + "/reject", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ reason: reason }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        showSuccessMessage("Booking rejected successfully!");
        closeModal("reject-modal");
        closeModal("booking-detail-modal");
        setTimeout(() => location.reload(), 1500);
      } else {
        showErrorMessage(data.message || "Failed to reject booking");
      }
    })
    .catch((error) => {
      console.error("Error rejecting booking:", error);
      showErrorMessage("An error occurred while rejecting the booking");
    });
}

// Complete Booking
function completeBooking(bookingId) {
  if (!confirm("Are you sure you want to mark this booking as completed?")) return;

  fetch(contextPath + "owner/management/bookings/" + bookingId + "/complete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        showSuccessMessage("Booking marked as completed!");
        closeModal("booking-detail-modal");
        setTimeout(() => location.reload(), 1500);
      } else {
        showErrorMessage(data.message || "Failed to complete booking");
      }
    })
    .catch((error) => {
      console.error("Error completing booking:", error);
      showErrorMessage("An error occurred while completing the booking");
    });
}

// Cancel Booking
function cancelBooking(bookingId) {
  const reason = prompt("Please provide a cancellation reason:");
  if (!reason || reason.trim() === "") return;

  fetch(contextPath + "owner/management/bookings/" + bookingId + "/cancel", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ reason: reason }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        showSuccessMessage("Booking cancelled successfully!");
        closeModal("booking-detail-modal");
        setTimeout(() => location.reload(), 1500);
      } else {
        showErrorMessage(data.message || "Failed to cancel booking");
      }
    })
    .catch((error) => {
      console.error("Error cancelling booking:", error);
      showErrorMessage("An error occurred while cancelling the booking");
    });
}

// Utility Functions
function closeModal(modalId) {
  document.getElementById(modalId).classList.add("hidden");
  if (modalId === "reject-modal") {
    document.getElementById("reject-reason").value = "";
  }
}

function formatDateTime(dateTimeString) {
  if (!dateTimeString) return "N/A";
  const date = new Date(dateTimeString);
  return date.toLocaleString("en-GB", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDate(dateString) {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function formatCurrency(amount) {
  if (!amount) return "0";
  return parseFloat(amount).toLocaleString("en-US", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  });
}

function showNotifications() {
  const successNotif = document.getElementById("success-notification");
  const errorNotif = document.getElementById("error-notification");

  if (successNotif) {
    setTimeout(() => {
      successNotif.style.transform = "translateX(0)";
    }, 100);
    setTimeout(() => {
      successNotif.style.transform = "translateX(100%)";
    }, 5000);
  }

  if (errorNotif) {
    setTimeout(() => {
      errorNotif.style.transform = "translateX(0)";
    }, 100);
    setTimeout(() => {
      errorNotif.style.transform = "translateX(100%)";
    }, 5000);
  }
}

function closeNotification(button) {
  button.closest('div[id$="-notification"]').style.transform = "translateX(100%)";
}

function showSuccessMessage(message) {
  const notification = document.createElement("div");
  notification.className = "fixed top-4 right-4 bg-green-500 text-white px-6 py-4 rounded-lg shadow-lg z-50";
  notification.innerHTML = `
        <div class="flex items-center">
            <i class="fas fa-check-circle mr-3"></i>
            <span>${message}</span>
        </div>
    `;
  document.body.appendChild(notification);
  setTimeout(() => notification.remove(), 3000);
}

function showErrorMessage(message) {
  const notification = document.createElement("div");
  notification.className = "fixed top-4 right-4 bg-red-500 text-white px-6 py-4 rounded-lg shadow-lg z-50";
  notification.innerHTML = `
        <div class="flex items-center">
            <i class="fas fa-exclamation-circle mr-3"></i>
            <span>${message}</span>
        </div>
    `;
  document.body.appendChild(notification);
  setTimeout(() => notification.remove(), 3000);
}
