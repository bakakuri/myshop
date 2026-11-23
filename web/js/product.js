// ── Product card & modal ──────────────────────────────────────
import { $, el, fmt, toast, openModal, closeModal, updateBadge } from './ui.js';
import { state, addToCart, toggleWishlist, inWishlist } from './state.js';

let currentProduct = null;

export function productCard(p) {
  const fav = inWishlist(p.id);
  const div = el('div', 'prod-card');
  div.innerHTML = `
    <div class="prod-img" data-id="${p.id}">${p.emoji}</div>
    ${p.discount ? `<span class="disc-badge">-${p.discount}%</span>` : ''}
    <button class="heart-btn ${fav ? 'on' : ''}" data-id="${p.id}">
      ${fav ? '❤️' : '🤍'}
    </button>
    <div class="prod-info">
      <div class="prod-name">${p.name}</div>
      <div class="prod-var">${p.variant}</div>
      <div class="prod-stars">★★★★★ <span class="prod-rc">(${p.reviews.toLocaleString()})</span></div>
      <div class="prod-price-row">
        <span class="prod-price">${fmt(p.price)}</span>
        ${p.oldPrice ? `<span class="prod-old">${fmt(p.oldPrice)}</span>` : ''}
      </div>
      <button class="prod-cart-btn" data-id="${p.id}">🛒 კალათაში</button>
    </div>`;

  div.querySelector('.prod-img').addEventListener('click', () => openProduct(p));
  div.querySelector('.prod-name').addEventListener('click', () => openProduct(p));
  div.querySelector('.heart-btn').addEventListener('click', e => { e.stopPropagation(); heartClick(p.id, e.currentTarget); });
  div.querySelector('.prod-cart-btn').addEventListener('click', e => { e.stopPropagation(); cartClick(p, e.currentTarget); });
  return div;
}

export function offerCard(o) {
  const div = el('div', 'offer-card');
  div.style.background = o.bg;
  div.innerHTML = `
    <div class="offer-emoji">${o.emoji}</div>
    <div>
      <div class="offer-title">${o.title}</div>
      <div class="offer-sub">${o.sub}</div>
      ${o.ends ? `<div class="offer-timer" data-ends="${o.ends}">⏰ იტვირთება...</div>` : ''}
    </div>`;
  return div;
}

function heartClick(id, btn) {
  toggleWishlist(id);
  const on = inWishlist(id);
  btn.classList.toggle('on', on);
  btn.textContent = on ? '❤️' : '🤍';
  toast(on ? '❤️ სიაში დაემატა' : 'სიიდან ამოიღეს');
}

function cartClick(p, btn) {
  addToCart(p);
  btn.textContent = '✓ დაემატა';
  btn.style.background = '#22c55e';
  setTimeout(() => { btn.textContent = '🛒 კალათაში'; btn.style.background = ''; }, 1500);
  toast(`🛒 ${p.name} კალათაში!`);
  updateBadge();
}

export function openProduct(p) {
  currentProduct = p;
  const fav = inWishlist(p.id);
  const modal = $('#modal-bg');
  if (!modal) return;
  modal.querySelector('.modal-img-box').textContent = p.emoji;
  modal.querySelector('.modal-name').textContent = p.name;
  modal.querySelector('.modal-var').textContent = p.variant;
  modal.querySelector('.modal-stars').textContent = '★★★★★';
  modal.querySelector('.modal-rnum').textContent = p.rating;
  modal.querySelector('.modal-rcnt').textContent = `(${p.reviews.toLocaleString()} შეფასება)`;
  modal.querySelector('.modal-price').textContent = fmt(p.price);
  modal.querySelector('.modal-old').textContent = p.oldPrice ? fmt(p.oldPrice) : '';
  modal.querySelector('.modal-disc').textContent = p.discount ? `-${p.discount}%` : '';
  modal.querySelector('.modal-disc').style.display = p.discount ? '' : 'none';
  modal.querySelector('.modal-desc').textContent = p.desc;
  const tags = modal.querySelector('.modal-tags');
  tags.innerHTML = p.tags.map(t => `<span class="modal-tag">${t}</span>`).join('');
  const fBtn = modal.querySelector('.modal-fav-btn');
  fBtn.classList.toggle('on', fav);
  fBtn.textContent = fav ? '❤️' : '🤍';
  modal.classList.add('on');
}

export function initModal() {
  const modal = $('#modal-bg');
  if (!modal) return;
  modal.querySelector('.modal-x').addEventListener('click', () => modal.classList.remove('on'));
  modal.addEventListener('click', e => { if (e.target === modal) modal.classList.remove('on'); });
  modal.querySelector('.modal-fav-btn').addEventListener('click', e => {
    if (!currentProduct) return;
    heartClick(currentProduct.id, e.currentTarget);
  });
  modal.querySelector('.modal-cart-btn').addEventListener('click', () => {
    if (!currentProduct) return;
    addToCart(currentProduct);
    toast(`🛒 ${currentProduct.name} კალათაში!`);
    updateBadge();
    modal.classList.remove('on');
  });
}
