// ── App entry point ───────────────────────────────────────────
import { $, $$, showPage, updateBadge, toast } from './ui.js';
import { state, setPage } from './state.js';
import { renderHome } from './home.js';
import { renderCart, initCart } from './cart.js';
import { renderSearch, initSearch } from './search.js';
import { renderChat } from './chat.js';
import { renderProfile } from './profile.js';
import { initModal } from './product.js';

// ── Navigation ────────────────────────────────────────────────
export function navigateTo(page) {
  setPage(page);
  showPage(page);
  switch (page) {
    case 'home':     renderHome(); break;
    case 'search':   renderSearch(); break;
    case 'cart':     renderCart(); break;
    case 'chat':     renderChat(); break;
    case 'profile':  renderProfile(); break;
  }
  window.scrollTo(0, 0);
}

// ── Init ──────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  // Nav tabs
  $$('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => navigateTo(btn.dataset.page));
  });

  // Header search
  const searchBarInp = $('#hdr-search');
  if (searchBarInp) {
    searchBarInp.addEventListener('keydown', e => {
      if (e.key === 'Enter') {
        state.search = e.target.value;
        navigateTo('search');
      }
    });
    searchBarInp.addEventListener('focus', () => navigateTo('search'));
  }

  // Hamburger / side menu
  const burger = $('.burger-btn');
  const sideMenu = $('.side-menu');
  const sideClose = $('.side-close');
  const sideOverlay = $('.side-overlay');
  burger?.addEventListener('click', () => {
    sideMenu?.classList.add('open');
    sideOverlay?.classList.add('open');
  });
  const closeMenu = () => { sideMenu?.classList.remove('open'); sideOverlay?.classList.remove('open'); };
  sideClose?.addEventListener('click', closeMenu);
  sideOverlay?.addEventListener('click', closeMenu);
  $$('.side-link').forEach(link => {
    link.addEventListener('click', () => {
      closeMenu();
      const pg = link.dataset.page;
      if (pg) navigateTo(pg);
    });
  });

  // Bell notification
  $('.bell-btn')?.addEventListener('click', () => toast('🔔 ახალი შეთავაზებები მოდის!'));

  // Notification popup close
  $('.notif-close')?.addEventListener('click', () => document.querySelector('.notif-popup')?.remove());

  // Product modal
  initModal();

  // Cart init
  initCart();

  // Search init
  initSearch();

  // Checkout modal close on bg click
  document.querySelector('.checkout-modal')?.addEventListener('click', e => {
    if (e.target === e.currentTarget) e.target.classList.remove('on');
  });

  // Success screen tap to dismiss
  document.querySelector('.success-screen')?.addEventListener('click', () => {
    document.querySelector('.success-screen')?.classList.remove('on');
  });

  // Boot
  updateBadge();
  navigateTo('home');

  // Show notification popup after 3s
  setTimeout(showNotifPopup, 3000);
});

function showNotifPopup() {
  const popup = document.querySelector('.notif-popup');
  if (popup) {
    popup.style.display = 'flex';
    setTimeout(() => popup.remove(), 6000);
  }
}
