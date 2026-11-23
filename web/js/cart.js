// ── Cart page & checkout ──────────────────────────────────────
import { $, $$, el, fmt, toast } from './ui.js';
import { state, removeFromCart, updateCartQty, clearCart, cartTotal, cartCount } from './state.js';
import { updateBadge } from './ui.js';

export function renderCart() {
  const list = $('.cart-list');
  const summary = $('.cart-summary');
  if (!list) return;

  if (!state.cart.length) {
    list.innerHTML = `
      <div style="text-align:center;padding:60px 20px">
        <div style="font-size:72px;margin-bottom:16px">🛒</div>
        <div style="font-size:18px;font-weight:800;color:#1a0050;margin-bottom:8px">კალათა ცარიელია</div>
        <div style="font-size:14px;color:#8a70c0;margin-bottom:24px">დაიწყე შოპინგი და დაამატე პროდუქტები!</div>
        <button id="cart-shop-btn" style="background:#5B21E6;color:#fff;padding:14px 32px;border-radius:40px;font-size:15px;font-weight:800">🛍️ პროდუქტები</button>
      </div>`;
    if (summary) summary.style.display = 'none';
    $('#cart-shop-btn')?.addEventListener('click', () => {
      import('./app.js').then(m => m.navigateTo('home'));
    });
    return;
  }
  if (summary) summary.style.display = '';
  list.innerHTML = '';

  state.cart.forEach(item => {
    const card = el('div', 'cart-card');
    card.innerHTML = `
      <div class="cart-emoji">${item.emoji}</div>
      <div class="cart-info">
        <div class="cart-name">${item.name}</div>
        <div class="cart-var">${item.variant}</div>
        <div class="cart-row">
          <div class="cart-price">${fmt(item.price * item.qty)}</div>
          <div class="qty-box">
            <button class="qty-btn qty-m" data-id="${item.id}">−</button>
            <span class="qty-n">${item.qty}</span>
            <button class="qty-btn qty-p" data-id="${item.id}">+</button>
          </div>
        </div>
      </div>
      <button class="cart-del" data-id="${item.id}">🗑️</button>`;

    card.querySelector('.qty-m').addEventListener('click', () => { updateCartQty(item.id, item.qty - 1); renderCart(); updateBadge(); });
    card.querySelector('.qty-p').addEventListener('click', () => { updateCartQty(item.id, item.qty + 1); renderCart(); updateBadge(); });
    card.querySelector('.cart-del').addEventListener('click', () => { removeFromCart(item.id); renderCart(); updateBadge(); toast('🗑️ ამოიღეს კალათიდან'); });
    list.appendChild(card);
  });

  renderSummary();
}

function renderSummary() {
  const sub = cartTotal();
  const delivery = sub >= 299 ? 0 : 10;
  const total = sub + delivery;

  const rows = $('.cart-summary');
  if (!rows) return;
  rows.querySelector('.sub-val').textContent = fmt(sub);
  rows.querySelector('.del-val').textContent = delivery === 0 ? 'უფასო 🎉' : fmt(delivery);
  rows.querySelector('.total-val').textContent = fmt(total);
}

export function initCart() {
  // Coupon apply
  document.addEventListener('click', e => {
    if (e.target.classList.contains('coupon-apply')) {
      const inp = document.querySelector('.coupon-inp');
      if (!inp) return;
      const code = inp.value.trim().toUpperCase();
      if (code === 'MYSHOP10') { toast('✅ 10% ფასდაკლება გამოიყენა!'); inp.value = ''; }
      else if (code === 'FREE') { toast('✅ უფასო მიწოდება!'); inp.value = ''; }
      else toast('❌ კუპონი არასწორია', 'error');
    }
    if (e.target.classList.contains('checkout-cta')) openCheckout();
  });
}

function openCheckout() {
  if (!state.cart.length) { toast('კალათა ცარიელია!', 'error'); return; }
  const modal = document.querySelector('.checkout-modal');
  if (modal) modal.classList.add('on');
  initCheckoutForm();
}

function initCheckoutForm() {
  // Radio opts
  document.querySelectorAll('.radio-opt').forEach(opt => {
    opt.addEventListener('click', () => {
      const group = opt.closest('.radio-g');
      group?.querySelectorAll('.radio-opt').forEach(o => o.classList.remove('sel'));
      opt.classList.add('sel');
    });
  });

  document.querySelector('.c-cancel')?.addEventListener('click', () => {
    document.querySelector('.checkout-modal')?.classList.remove('on');
  });

  document.querySelector('.c-confirm')?.addEventListener('click', confirmOrder);
}

function confirmOrder() {
  const name = document.querySelector('.f-name')?.value.trim();
  const phone = document.querySelector('.f-phone')?.value.trim();
  if (!name || !phone) { toast('შეავსე სახელი და ტელეფონი!', 'error'); return; }

  document.querySelector('.checkout-modal')?.classList.remove('on');
  const orderNum = 'MS' + Date.now().toString().slice(-6);
  document.querySelector('.success-num').textContent = `შეკვეთა #${orderNum}`;
  document.querySelector('.success-screen')?.classList.add('on');
  clearCart();
  updateBadge();

  setTimeout(() => {
    document.querySelector('.success-screen')?.classList.remove('on');
    import('./app.js').then(m => m.navigateTo('home'));
  }, 4000);
}
