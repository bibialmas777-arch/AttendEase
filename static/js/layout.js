/**
 * AttendEase — Unified Layout & Application Shell Engine
 * Manages responsive navigation, session verification, ambient visuals, and notifications.
 */

const AppLayout = {
  currentUser: null,

  init: async function(activeNavKey, expectedRole) {
    this.injectAmbientBackground();
    this.initToastContainer();
    
    // Authenticate and load current user session
    try {
      const response = await fetch('/api/auth/me');
      if (!response.ok) {
        window.location.href = '/login.html';
        return;
      }
      this.currentUser = await response.json();

      // Check role enforcement
      if (expectedRole && this.currentUser.role !== expectedRole) {
        alert(`Access denied. You are signed in as ${this.currentUser.role}, but this page requires ${expectedRole}.`);
        this.redirectToRoleHome(this.currentUser.role);
        return;
      }

      this.renderSidebar(activeNavKey);
      this.renderTopNavbar();
      this.initSidebarToggle();
      this.loadNotifications();
    } catch (e) {
      console.error('Session verification failed:', e);
      window.location.href = '/login.html';
    }
  },

  redirectToRoleHome: function(role) {
    switch (role) {
      case 'STUDENT': window.location.href = '/pages/student/dashboard.html'; break;
      case 'FACULTY': window.location.href = '/pages/faculty/dashboard.html'; break;
      case 'ADMIN': window.location.href = '/pages/admin/dashboard.html'; break;
      default: window.location.href = '/login.html';
    }
  },

  injectAmbientBackground: function() {
    if (document.querySelector('.ambient')) return;
    const ambient = document.createElement('div');
    ambient.className = 'ambient';
    ambient.innerHTML = `
      <div class="ambient-blob blob-1"></div>
      <div class="ambient-blob blob-2"></div>
      <div class="ambient-blob blob-3"></div>
    `;
    document.body.prepend(ambient);
  },

  initToastContainer: function() {
    if (document.getElementById('toastContainer')) return;
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container-custom';
    document.body.appendChild(container);
  },

  renderSidebar: function(activeNavKey) {
    const sidebarEl = document.getElementById('appSidebar');
    if (!sidebarEl) return;

    const role = this.currentUser.role;
    let navItems = [];

    if (role === 'STUDENT') {
      navItems = [
        { key: 'dashboard', label: 'Dashboard', icon: 'fa-gauge-high', url: '/pages/student/dashboard.html' },
        { key: 'history', label: 'Attendance History', icon: 'fa-calendar-days', url: '/pages/student/history.html' },
        { key: 'recovery', label: 'Recovery Planner', icon: 'fa-calculator', url: '/pages/student/recovery.html' },
        { key: 'complaints', label: 'Grievance Portal', icon: 'fa-file-shield', url: '/pages/student/complaints.html' },
        { key: 'reports', label: 'Attendance Reports', icon: 'fa-file-pdf', url: '/pages/student/reports.html' }
      ];
    } else if (role === 'FACULTY') {
      navItems = [
        { key: 'dashboard', label: 'Dashboard', icon: 'fa-gauge-high', url: '/pages/faculty/dashboard.html' },
        { key: 'mark', label: 'Mark Attendance', icon: 'fa-clipboard-user', url: '/pages/faculty/mark-attendance.html' },
        { key: 'history', label: 'Class History', icon: 'fa-clock-rotate-left', url: '/pages/faculty/history.html' },
        { key: 'complaints', label: 'Review Grievances', icon: 'fa-file-signature', url: '/pages/faculty/complaints.html' },
        { key: 'reports', label: 'Reports & Export', icon: 'fa-chart-pie', url: '/pages/faculty/reports.html' }
      ];
    } else if (role === 'ADMIN') {
      navItems = [
        { key: 'dashboard', label: 'Dashboard', icon: 'fa-gauge-high', url: '/pages/admin/dashboard.html' },
        { key: 'students', label: 'Students', icon: 'fa-user-graduate', url: '/pages/admin/students.html' },
        { key: 'faculty', label: 'Faculty', icon: 'fa-chalkboard-user', url: '/pages/admin/faculty.html' },
        { key: 'academics', label: 'Classes & Subjects', icon: 'fa-book-open', url: '/pages/admin/academics.html' },
        { key: 'assignments', label: 'Assignments', icon: 'fa-link', url: '/pages/admin/assignments.html' },
        { key: 'attendance', label: 'All Attendance', icon: 'fa-list-check', url: '/pages/admin/attendance.html' },
        { key: 'complaints', label: 'All Grievances', icon: 'fa-scale-balanced', url: '/pages/admin/complaints.html' },
        { key: 'analytics', label: 'Analytics', icon: 'fa-chart-line', url: '/pages/admin/analytics.html' },
        { key: 'settings', label: 'Threshold Settings', icon: 'fa-sliders', url: '/pages/admin/settings.html' },
        { key: 'reports', label: 'Reports & Export', icon: 'fa-file-arrow-down', url: '/pages/admin/reports.html' }
      ];
    }

    let navHtml = `
      <div class="sidebar-header">
        <a href="#" class="brand-wrapper">
          <svg class="brand-logo-svg" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="url(#brandGrad)"/>
            <path d="M20 10L9 16L20 22L31 16L20 10Z" stroke="#FFF7F8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13 18.2V25.5C13 25.5 16 28 20 28C24 28 27 25.5 27 25.5V18.2" stroke="#FFF7F8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M29 18V24" stroke="#FFF7F8" stroke-width="2" stroke-linecap="round"/>
            <defs>
              <linearGradient id="brandGrad" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
                <stop stop-color="#8F304F"/>
                <stop offset="1" stop-color="#3B1022"/>
              </linearGradient>
            </defs>
          </svg>
          <span class="brand-wordmark">Attend<span>Ease</span></span>
        </a>
        <button class="sidebar-toggle-btn d-none d-lg-flex" id="sidebarCollapseBtn" title="Toggle Sidebar">
          <i class="fa-solid fa-bars-staggered"></i>
        </button>
      </div>

      <div class="sidebar-nav">
        <div class="nav-section-title">Navigation</div>
    `;

    navItems.forEach(item => {
      const isActive = item.key === activeNavKey ? 'active' : '';
      navHtml += `
        <a href="${item.url}" class="nav-item ${isActive}">
          <i class="fa-solid ${item.icon}"></i>
          <span>${item.label}</span>
        </a>
      `;
    });

    navHtml += `
      </div>
      <div class="sidebar-footer">
        <a href="javascript:void(0)" onclick="AppLayout.logout()" class="nav-item text-danger">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>
          <span>Sign Out</span>
        </a>
      </div>
    `;

    sidebarEl.innerHTML = navHtml;

    // Restore collapsed state
    if (localStorage.getItem('attendease_sidebar_collapsed') === 'true') {
      sidebarEl.classList.add('collapsed');
    }
  },

  renderTopNavbar: function() {
    const navbarEl = document.getElementById('topNavbar');
    if (!navbarEl) return;

    const u = this.currentUser;
    const initials = u.name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
    const roleChipClass = `chip-${u.role.toLowerCase()}`;

    navbarEl.innerHTML = `
      <div class="nav-left">
        <button class="mobile-nav-toggle d-lg-none" id="mobileMenuBtn">
          <i class="fa-solid fa-bars"></i>
        </button>
        <span class="role-chip ${roleChipClass}">${u.role}</span>
        <span class="text-muted d-none d-md-inline" style="font-size: 0.88rem;">
          ${u.rollNo ? `Roll: ${u.rollNo} • ${u.className || ''}` : (u.department ? u.department : 'College Administrator')}
        </span>
      </div>

      <div class="nav-right">
        <!-- Notification Dropdown -->
        <div class="dropdown">
          <button class="notification-btn" id="notificationBtn" data-bs-toggle="dropdown" aria-expanded="false" title="Notifications">
            <i class="fa-regular fa-bell"></i>
            <span class="notification-badge d-none" id="notifBadge">0</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end glass-dropdown-menu" id="notifDropdownList" style="width: 340px; max-height: 400px; overflow-y: auto;">
            <li class="px-3 py-2 border-bottom border-light border-opacity-10 d-flex justify-content-between align-items-center">
              <span class="fw-bold" style="font-size: 0.85rem;">Notifications</span>
              <button class="btn btn-link p-0 text-decoration-none" style="font-size: 0.78rem; color: var(--blush);" onclick="AppLayout.markNotificationsRead()">Mark read</button>
            </li>
            <div id="notifItemsContainer">
              <li class="text-center py-4 text-muted" style="font-size: 0.85rem;">No new notifications</li>
            </div>
          </ul>
        </div>

        <!-- User Profile Pill -->
        <div class="user-menu-btn">
          <div class="user-avatar">${initials}</div>
          <div class="user-details-mini">
            <span class="user-name-mini">${u.name}</span>
            <span class="user-role-mini">${u.email}</span>
          </div>
        </div>
      </div>
    `;

    // Hook mobile drawer toggle
    const mobileBtn = document.getElementById('mobileMenuBtn');
    if (mobileBtn) {
      mobileBtn.addEventListener('click', () => {
        document.getElementById('appSidebar').classList.toggle('mobile-open');
      });
    }
  },

  initSidebarToggle: function() {
    const btn = document.getElementById('sidebarCollapseBtn');
    const sidebar = document.getElementById('appSidebar');
    if (!btn || !sidebar) return;

    btn.addEventListener('click', () => {
      sidebar.classList.toggle('collapsed');
      const isCollapsed = sidebar.classList.contains('collapsed');
      localStorage.setItem('attendease_sidebar_collapsed', isCollapsed ? 'true' : 'false');
      
      // Trigger resize for Chart.js instances
      window.dispatchEvent(new Event('resize'));
    });
  },

  loadNotifications: async function() {
    if (!this.currentUser) return;
    const role = this.currentUser.role.toLowerCase();
    try {
      const res = await fetch(`/api/${role}/notifications`);
      if (!res.ok) return;
      const notifs = await res.json();
      
      const badge = document.getElementById('notifBadge');
      const container = document.getElementById('notifItemsContainer');
      const unread = notifs.filter(n => !n.isRead).length;

      if (badge) {
        if (unread > 0) {
          badge.textContent = unread;
          badge.classList.remove('d-none');
        } else {
          badge.classList.add('d-none');
        }
      }

      if (container && notifs.length > 0) {
        container.innerHTML = notifs.slice(0, 8).map(n => `
          <li class="px-3 py-2 border-bottom border-light border-opacity-10 ${n.isRead ? 'opacity-75' : 'bg-white bg-opacity-10'}">
            <div style="font-size: 0.84rem; line-height: 1.3;">${n.message}</div>
            <small class="text-muted" style="font-size: 0.72rem;">${new Date(n.createdAt).toLocaleDateString()} ${new Date(n.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</small>
          </li>
        `).join('');
      }
    } catch (e) {
      console.warn('Could not load notifications', e);
    }
  },

  markNotificationsRead: async function() {
    const role = this.currentUser.role.toLowerCase();
    try {
      await fetch(`/api/${role}/notifications/read-all`, { method: 'POST' });
      this.loadNotifications();
      this.showToast('All notifications marked as read', 'success');
    } catch (e) {
      console.error(e);
    }
  },

  logout: async function() {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
    } finally {
      window.location.href = '/login.html';
    }
  },

  showToast: function(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    let icon = 'fa-circle-info';
    let borderColor = 'var(--glass-border)';
    if (type === 'success') {
      icon = 'fa-circle-check';
      borderColor = 'var(--success)';
    } else if (type === 'error' || type === 'danger') {
      icon = 'fa-circle-exclamation';
      borderColor = 'var(--danger)';
    } else if (type === 'warning') {
      icon = 'fa-triangle-exclamation';
      borderColor = 'var(--warning)';
    }

    const toast = document.createElement('div');
    toast.className = 'toast-glass';
    toast.style.borderColor = borderColor;
    toast.innerHTML = `
      <i class="fa-solid ${icon}" style="font-size: 1.25rem; color: ${borderColor};"></i>
      <div style="flex: 1; font-size: 0.88rem;">${message}</div>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease-out';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  renderCircleProgress: function(containerId, percentage, statusColor) {
    const el = document.getElementById(containerId);
    if (!el) return;

    const radius = 52;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (percentage / 100) * circumference;

    let strokeColor = '#75C9A3'; // Green
    if (statusColor === 'YELLOW') strokeColor = '#E9B96E';
    if (statusColor === 'RED') strokeColor = '#F07889';

    el.innerHTML = `
      <div class="circle-progress-wrapper">
        <svg class="circle-progress-svg" viewBox="0 0 130 130">
          <circle class="circle-bg" cx="65" cy="65" r="${radius}"></circle>
          <circle class="circle-fill" cx="65" cy="65" r="${radius}" 
            stroke="${strokeColor}" 
            stroke-dasharray="${circumference}" 
            stroke-dashoffset="${circumference}" 
            id="${containerId}_stroke"></circle>
        </svg>
        <div class="circle-text">
          <div class="circle-percent">${percentage.toFixed(1)}%</div>
          <div class="circle-status-label" style="color: ${strokeColor};">${statusColor}</div>
        </div>
      </div>
    `;

    setTimeout(() => {
      const stroke = document.getElementById(`${containerId}_stroke`);
      if (stroke) stroke.style.strokeDashoffset = offset;
    }, 50);
  }
};

