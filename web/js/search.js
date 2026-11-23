// ── Search / browse page ──────────────────────────────────────
import { $, $$, el, fmt } from './ui.js';
import { PRODUCTS, CATEGORIES } from './data.js';
import { state } from './state.js';
import { productCard } from './product.js';

let activeCategory = '';
let sortMode = 'popular';

export function filterByCategory(catId) {
  activeCategory = catId;
  state.filter.cat = catId;
  renderSearch();
}

export function renderSearch() {
  const q = state.search.toLowerCase();
  let results = [...PRODUCTS];

  if (state.filter.cat) results = results.filter(p => p.cat === state.filter.cat);
  if (q) results = results.filter(p => p.name.toLowerCase().includes(q) || p.tags.some(t => t.toLowerCase().includes(q)));
  if (state.filter.priceMin > 0) results = results.filter(p => p.price >= state.filter.priceMin);
  if (state.filter.priceMax < 99999) results = results.filter(p => p.price <= state.filter.priceMax);

  switch (sortMode) {
    case 'price-asc':  results.sort((a,b) => a.price - b.price); break;
    case 'price-desc': results.sort((a,b) => b.price - a.price); break;
    case 'rating':     results.sort((a,b) => b.rating - a.rating); break;
    case 'discount':   results.sort((a,b) => b.discount - a.discount); break;
    default:           results.sort((a,b) => b.reviews - a.reviews);
  }

  // Update category chips
  $$('.schip').forEach(chip => {
    chip.classList.toggle('active', chip.dataset.cat === (state.filter.cat || 'all'));
  });

  // Result count
  const cnt = $('.res-cnt');
  if (cnt) cnt.innerHTML = `ნაპოვნია: <span>${results.length}</span> პროდუქტი`;

  // Grid
  const grid = $('.search-prods-grid');
  if (!grid) return;
  grid.innerHTML = '';
  if (!results.length) {
    grid.innerHTML = `<div style="grid-column:1/-1;text-align:center;padding:40px 20px">
      <div style="font-size:48px;margin-bottom:12px">🔍</div>
      <div style="font-size:16px;font-weight:800;color:#1a0050">ვერ ვიპოვეთ</div>
      <div style="font-size:13px;color:#8a70c0;margin-top:6px">სცადე სხვა საკვანძო სიტყვა</div>
    </div>`;
    return;
  }
  results.forEach(p => grid.appendChild(productCard(p)));
}

export function initSearch() {
  // Category chips
  const chipsWrap = $('.search-chips');
  if (chipsWrap) {
    chipsWrap.innerHTML = `<button class="schip active" data-cat="all">ყველა</button>` +
      CATEGORIES.map(c => `<button class="schip" data-cat="${c.id}">${c.emoji} ${c.name}</button>`).join('');
    chipsWrap.addEventListener('click', e => {
      const chip = e.target.closest('.schip');
      if (!chip) return;
      state.filter.cat = chip.dataset.cat === 'all' ? '' : chip.dataset.cat;
      renderSearch();
    });
  }

  // Sort buttons
  $$('.sort-b').forEach(btn => {
    btn.addEventListener('click', () => {
      sortMode = btn.dataset.sort;
      $$('.sort-b').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderSearch();
    });
  });

  // Search input (search page)
  const inp = $('#search-inp');
  if (inp) {
    inp.value = state.search;
    inp.addEventListener('input', e => {
      state.search = e.target.value;
      renderSearch();
    });
  }

  // Filter sheet
  initFilters();
}

function initFilters() {
  const sheet = $('.filter-sheet');
  const bg = sheet?.querySelector('.filter-bg');
  bg?.addEventListener('click', () => sheet.classList.remove('on'));

  // Filter chips
  $$('.f-chip').forEach(chip => {
    chip.addEventListener('click', () => chip.classList.toggle('on'));
  });
  $$('.f-check').forEach(ch => {
    ch.addEventListener('click', () => ch.classList.toggle('on'));
  });

  $('.f-reset')?.addEventListener('click', () => {
    $$('.f-chip, .f-check').forEach(el => el.classList.remove('on'));
    const mn = document.querySelector('.f-min');
    const mx = document.querySelector('.f-max');
    if (mn) mn.value = '';
    if (mx) mx.value = '';
    state.filter = { cat: '', priceMin: 0, priceMax: 99999, brands: [] };
    renderSearch();
    sheet?.classList.remove('on');
  });

  $('.f-apply')?.addEventListener('click', () => {
    const mn = +(document.querySelector('.f-min')?.value || 0);
    const mx = +(document.querySelector('.f-max')?.value || 99999);
    state.filter.priceMin = mn;
    state.filter.priceMax = mx || 99999;
    renderSearch();
    sheet?.classList.remove('on');
  });

  // Global search bar filter button
  document.querySelector('.search-filter-btn')?.addEventListener('click', () => {
    sheet?.classList.add('on');
  });
}
