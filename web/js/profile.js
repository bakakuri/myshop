// ── Profile page ──────────────────────────────────────────────
import { $, toast } from './ui.js';
import { state } from './state.js';
import { PRODUCTS } from './data.js';
import { productCard } from './product.js';

export function renderProfile() {
  const u = state.user;
  const nameEl = $('.p-pname');
  const emailEl = $('.p-email');
  if (nameEl) nameEl.textContent = u.name;
  if (emailEl) emailEl.textContent = u.email;

  // Stats
  const stats = document.querySelectorAll('.p-stat-num');
  if (stats[0]) stats[0].textContent = u.orders;
  if (stats[1]) stats[1].textContent = state.wishlist.length;
  if (stats[2]) stats[2].textContent = u.reviews;

  // Wishlist in profile
  renderWishlist();

  // Menu items
  document.querySelectorAll('.p-menu-item').forEach(item => {
    item.addEventListener('click', () => {
      const action = item.dataset.action;
      handleProfileAction(action);
    });
  });
}

function handleProfileAction(action) {
  switch (action) {
    case 'orders': toast('📦 შეკვეთების ისტორია მალე!'); break;
    case 'wishlist': scrollToWishlist(); break;
    case 'notifications': toast('🔔 შეტყობინებები ჩართულია!'); break;
    case 'settings': toast('⚙️ პარამეტრები მალე!'); break;
    case 'help': toast('💬 ჩატი გახსენი დახმარებისთვის'); break;
    case 'logout': toast('👋 გამოსვლა... სამინახოდ!'); break;
  }
}

function scrollToWishlist() {
  const wl = document.querySelector('.wishlist-section');
  wl?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function renderWishlist() {
  const grid = document.querySelector('.wishlist-grid');
  if (!grid) return;
  const items = PRODUCTS.filter(p => state.wishlist.includes(p.id));
  if (!items.length) {
    grid.innerHTML = `<div style="grid-column:1/-1;text-align:center;padding:32px 20px">
      <div style="font-size:48px;margin-bottom:10px">❤️</div>
      <div style="font-size:15px;font-weight:800;color:#1a0050">სია ცარიელია</div>
      <div style="font-size:13px;color:#8a70c0;margin-top:6px">დაამატე პროდუქტები გულის ღილაკით</div>
    </div>`;
    return;
  }
  grid.innerHTML = '';
  items.forEach(p => grid.appendChild(productCard(p)));
}
