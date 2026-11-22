// MYSHOP Admin Panel — Toast Notification System
export function initToast() {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  function show(message, type = 'default', duration = 3500) {
    const icons = { success: '✓', error: '✕', warning: '⚠', default: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || icons.default}</span>
      <span class="toast-message">${message}</span>
    `;
    container.appendChild(toast);

    // Auto dismiss
    setTimeout(() => {
      toast.style.animation = 'slideIn 0.25s ease reverse';
      setTimeout(() => toast.remove(), 250);
    }, duration);

    // Click to dismiss
    toast.addEventListener('click', () => toast.remove());
  }

  return {
    success: (msg, dur) => show(msg, 'success', dur),
    error:   (msg, dur) => show(msg, 'error',   dur),
    warning: (msg, dur) => show(msg, 'warning', dur),
    info:    (msg, dur) => show(msg, 'default', dur),
    show,
  };
}
