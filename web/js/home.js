// ── Home page (slider, categories, products) ──────────────────
import { $, $$, el, fmt, toast, showPage } from './ui.js';
import { PRODUCTS, CATEGORIES, SLIDES, OFFERS } from './data.js';
import { productCard, offerCard } from './product.js';

let slideIdx = 0;
let slideTimer = null;

export function renderHome() {
  renderSlider();
  renderCategories();
  renderFeatureBanners();
  renderProducts();
  startOfferTimers();
}

// ── Slider ────────────────────────────────────────────────────
function renderSlider() {
  const wrap = $('.slides-wrap');
  const dots = $('.slide-dots');
  if (!wrap) return;
  wrap.innerHTML = '';
  dots.innerHTML = '';
  SLIDES.forEach((s, i) => {
    const slide = el('div', 'slide');
    slide.style.background = s.bg;
    slide.innerHTML = `
      <div class="slide-emoji">${s.emoji}</div>
      <div class="slide-text">
        <h2 class="slide-title">${s.title.replace('\n', '<br>')}</h2>
        <p class="slide-sub">${s.sub}</p>
        <button class="slide-cta">${s.cta}</button>
      </div>`;
    wrap.appendChild(slide);
    const dot = el('button', `dot ${i === 0 ? 'on' : ''}`);
    dot.addEventListener('click', () => goSlide(i));
    dots.appendChild(dot);
  });
  goSlide(0);
  startSlider();

  // swipe
  let sx = 0;
  wrap.addEventListener('touchstart', e => { sx = e.touches[0].clientX; }, { passive: true });
  wrap.addEventListener('touchend', e => {
    const dx = e.changedTouches[0].clientX - sx;
    if (dx < -40) goSlide((slideIdx + 1) % SLIDES.length);
    else if (dx > 40) goSlide((slideIdx - 1 + SLIDES.length) % SLIDES.length);
  });
}

function goSlide(i) {
  slideIdx = i;
  const wrap = $('.slides-wrap');
  if (!wrap) return;
  wrap.style.transform = `translateX(-${i * 100}%)`;
  $$('.dot').forEach((d, j) => d.classList.toggle('on', j === i));
}

function startSlider() {
  if (slideTimer) clearInterval(slideTimer);
  slideTimer = setInterval(() => goSlide((slideIdx + 1) % SLIDES.length), 4500);
}

// ── Categories ────────────────────────────────────────────────
function renderCategories() {
  const row = $('.cat-row');
  if (!row) return;
  row.innerHTML = '';
  CATEGORIES.forEach(c => {
    const btn = el('button', 'cat-card');
    btn.innerHTML = `<span class="cat-emoji">${c.emoji}</span><span class="cat-name">${c.name}</span>`;
    btn.addEventListener('click', () => {
      import('./search.js').then(m => { m.filterByCategory(c.id); showPage('search'); });
    });
    row.appendChild(btn);
  });
}

// ── Feature Banners ───────────────────────────────────────────
function renderFeatureBanners() {
  const grid = $('.feat-grid');
  if (!grid) return;
  grid.innerHTML = `
    <div class="feat-banner feat-delivery">
      <span class="feat-icon">🚚</span>
      <div>
        <div class="feat-title">სწრაფი მიწოდება</div>
        <div class="feat-sub">1-2 დღეში თქვენამდე</div>
      </div>
    </div>
    <div class="feat-banner feat-secure">
      <span class="feat-icon">🔒</span>
      <div>
        <div class="feat-title">უსაფრთხო გადახდა</div>
        <div class="feat-sub">100% დაცული</div>
      </div>
    </div>`;
}

// ── Products Grid ─────────────────────────────────────────────
function renderProducts() {
  const grid = $('.prods-grid');
  if (!grid) return;
  grid.innerHTML = '';
  PRODUCTS.forEach(p => grid.appendChild(productCard(p)));
}

// ── Offer Timers ──────────────────────────────────────────────
function startOfferTimers() {
  const timers = $$('.offer-timer');
  if (!timers.length) return;
  function tick() {
    timers.forEach(t => {
      const ends = +t.dataset.ends;
      const left = ends - Date.now();
      if (left <= 0) { t.textContent = '⏰ დამთავრდა'; return; }
      const h = Math.floor(left / 3600000);
      const m = Math.floor((left % 3600000) / 60000);
      const s = Math.floor((left % 60000) / 1000);
      t.textContent = `⏰ ${h}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
    });
  }
  tick();
  setInterval(tick, 1000);
}
