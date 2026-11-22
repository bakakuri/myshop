// MYSHOP Admin — Products Management Page
import { formatPrice, formatDate, renderPagination, confirmDialog, createModal, closeModal, getImageUrl } from '../js/utils.js';

const PER_PAGE = 20;
let state = { products: [], total: 0, page: 1, search: '', categoryId: '', status: '', categories: [] };

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  await Promise.all([loadCategories(supabase), loadProducts(supabase)]);
  bindEvents(supabase);
}

function renderShell() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">პროდუქტები</h1>
        <p class="page-subtitle" id="prodCount">იტვირთება…</p>
      </div>
      <div class="flex gap-3">
        <button class="btn btn-secondary" id="bulkDeleteBtn" style="display:none">🗑 წაშლა</button>
        <button class="btn btn-primary" id="addProductBtn">+ პროდუქტის დამატება</button>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="search-input-wrap">
          <span class="search-icon">🔍</span>
          <input type="text" class="form-control" id="prodSearch" placeholder="პროდუქტის ძებნა…">
        </div>
        <select class="form-control" id="catFilter" style="width:180px">
          <option value="">ყველა კატეგ.</option>
        </select>
        <select class="form-control" id="statusFilter" style="width:160px">
          <option value="">ყველა სტატ.</option>
          <option value="active">გამოქვეყნებ.</option>
          <option value="draft">დრაფტი</option>
          <option value="inactive">გაუქმებული</option>
        </select>
        <button class="btn btn-secondary btn-sm" id="clearFilters">გასუფთავება</button>
      </div>

      <div class="table-container" style="border:none;border-radius:0">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width:40px"><input type="checkbox" id="selectAll"></th>
              <th>პროდუქტი</th>
              <th>კატეგ.</th>
              <th>ფასი</th>
              <th>მარ.</th>
              <th>სტ.</th>
              <th>თარ.</th>
              <th class="col-actions">მოქმ.</th>
            </tr>
          </thead>
          <tbody id="prodBody">
            <tr><td colspan="8"><div class="skeleton" style="height:200px;margin:8px;border-radius:8px"></div></td></tr>
          </tbody>
        </table>
      </div>
      <div id="prodPagination"></div>
    </div>
  `;
}

async function loadCategories(supabase) {
  const { data: cats } = await supabase.from('categories').select('id, name').order('name');
  state.categories = cats || [];
  const sel = document.getElementById('catFilter');
  if (sel) sel.innerHTML = `<option value="">ყველა კატეგ.</option>` + (cats || []).map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

async function loadProducts(supabase) {
  const tbody = document.getElementById('prodBody');
  tbody.innerHTML = `<tr><td colspan="8"><div class="skeleton" style="height:200px;margin:8px;border-radius:8px"></div></td></tr>`;

  let query = supabase
    .from('products')
    .select(`id, name, price, compare_price, stock_quantity, status, images, created_at,
             categories:category_id(name)`, { count: 'exact' });

  if (state.search) query = query.ilike('name', `%${state.search}%`);
  if (state.categoryId) query = query.eq('category_id', state.categoryId);
  if (state.status) query = query.eq('status', state.status);

  const { data, count } = await query
    .order('created_at', { ascending: false })
    .range((state.page - 1) * PER_PAGE, state.page * PER_PAGE - 1);

  state.products = data || [];
  state.total = count || 0;

  document.getElementById('prodCount').textContent = `სულ ${state.total} პროდუქტი`;

  if (!state.products.length) {
    tbody.innerHTML = `<tr><td colspan="8"><div class="empty-state"><div class="empty-state-icon">🛍</div><div class="empty-state-desc">პროდუქტი არ მოიძებნა</div></div></td></tr>`;
    return;
  }

  tbody.innerHTML = state.products.map(p => renderProductRow(p, supabase)).join('');
  tbody.querySelectorAll('.btn-edit').forEach(btn => btn.addEventListener('click', () => openProductForm(supabase, btn.dataset.id)));
  tbody.querySelectorAll('.btn-del').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog(`„${btn.dataset.name}" წაიშლება. ეს ვერ გაუქმდება.`)) {
      await supabase.from('products').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('პროდუქტი წაიშალა');
      loadProducts(supabase);
    }
  }));
  tbody.querySelectorAll('.status-toggle').forEach(tog => {
    tog.addEventListener('change', async e => {
      const newStatus = e.target.checked ? 'active' : 'draft';
      await supabase.from('products').update({ status: newStatus }).eq('id', tog.dataset.id);
      window.__APP__.toast.success('სტატუსი განახლდა');
    });
  });

  document.getElementById('prodPagination').innerHTML = renderPagination({
    total: state.total, page: state.page, perPage: PER_PAGE,
    onPage: (p) => { state.page = p; loadProducts(supabase); }
  });
}

function renderProductRow(p, supabase) {
  const imgUrl = p.images?.[0] ? getImageUrl(supabase, 'product-images', p.images[0]) : null;
  const statusBadge = { active: `<span class="badge badge-active">გამოქვ.</span>`, draft: `<span class="badge badge-draft">დრაფტი</span>`, inactive: `<span class="badge badge-cancelled">გაუქ.</span>` }[p.status] || '';

  return `
    <tr>
      <td><input type="checkbox" class="prod-check" value="${p.id}"></td>
      <td>
        <div class="flex items-center gap-3">
          <div style="width:44px;height:44px;border-radius:8px;overflow:hidden;flex-shrink:0;background:var(--color-gray-100);display:flex;align-items:center;justify-content:center">
            ${imgUrl ? `<img src="${imgUrl}" style="width:100%;height:100%;object-fit:cover" loading="lazy" alt="">` : `<span style="font-size:18px">📷</span>`}
          </div>
          <div>
            <div class="font-semibold text-sm truncate" style="max-width:200px">${p.name}</div>
            ${p.compare_price ? `<div class="text-xs text-gray" style="text-decoration:line-through">${formatPrice(p.compare_price)}</div>` : ''}
          </div>
        </div>
      </td>
      <td class="text-sm text-gray">${p.categories?.name || '—'}</td>
      <td class="font-semibold">${formatPrice(p.price)}</td>
      <td class="text-sm ${p.stock_quantity < 5 ? 'text-danger' : 'text-gray'}">${p.stock_quantity ?? '∞'}</td>
      <td>${statusBadge}</td>
      <td class="text-xs text-gray">${formatDate(p.created_at)}</td>
      <td class="col-actions">
        <div class="flex gap-2 justify-end">
          <button class="btn btn-sm btn-secondary btn-edit" data-id="${p.id}">✏️</button>
          <button class="btn btn-sm btn-danger btn-del" data-id="${p.id}" data-name="${p.name}">🗑</button>
        </div>
      </td>
    </tr>
  `;
}

function bindEvents(supabase) {
  let timer;
  document.getElementById('prodSearch').addEventListener('input', e => {
    clearTimeout(timer);
    timer = setTimeout(() => { state.search = e.target.value.trim(); state.page = 1; loadProducts(supabase); }, 400);
  });
  document.getElementById('catFilter').addEventListener('change', e => { state.categoryId = e.target.value; state.page = 1; loadProducts(supabase); });
  document.getElementById('statusFilter').addEventListener('change', e => { state.status = e.target.value; state.page = 1; loadProducts(supabase); });
  document.getElementById('clearFilters').addEventListener('click', () => {
    document.getElementById('prodSearch').value = '';
    document.getElementById('catFilter').value = '';
    document.getElementById('statusFilter').value = '';
    state.search = ''; state.categoryId = ''; state.status = ''; state.page = 1;
    loadProducts(supabase);
  });
  document.getElementById('addProductBtn').addEventListener('click', () => openProductForm(supabase, null));
}

async function openProductForm(supabase, productId) {
  let product = null;
  if (productId) {
    const { data } = await supabase.from('products').select('*').eq('id', productId).single();
    product = data;
  }

  const catOptions = state.categories.map(c => `<option value="${c.id}" ${product?.category_id === c.id ? 'selected' : ''}>${c.name}</option>`).join('');

  createModal({
    id: 'productModal',
    title: product ? 'პროდუქტის რედ.' : 'ახალი პროდუქტი',
    size: 'modal-xl',
    body: `
      <form id="productForm">
        <div class="form-row cols-2">
          <div class="form-group">
            <label class="form-label required">სახელი</label>
            <input class="form-control" name="name" value="${product?.name || ''}" required placeholder="პროდუქტის სახელი">
          </div>
          <div class="form-group">
            <label class="form-label">კატეგ.</label>
            <select class="form-control" name="category_id">
              <option value="">— კატ. —</option>
              ${catOptions}
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">აღწერა</label>
          <textarea class="form-control" name="description" rows="4">${product?.description || ''}</textarea>
        </div>
        <div class="form-row cols-3">
          <div class="form-group">
            <label class="form-label required">ფასი (₾)</label>
            <input class="form-control" name="price" type="number" step="0.01" min="0" value="${product?.price || ''}" required>
          </div>
          <div class="form-group">
            <label class="form-label">ფასდაკლ. ფასი (₾)</label>
            <input class="form-control" name="compare_price" type="number" step="0.01" min="0" value="${product?.compare_price || ''}">
          </div>
          <div class="form-group">
            <label class="form-label">მარაგი</label>
            <input class="form-control" name="stock_quantity" type="number" min="0" value="${product?.stock_quantity ?? ''}">
          </div>
        </div>
        <div class="form-row cols-2">
          <div class="form-group">
            <label class="form-label">სტ.</label>
            <select class="form-control" name="status">
              <option value="draft" ${product?.status === 'draft' ? 'selected' : ''}>დრაფტი</option>
              <option value="active" ${product?.status === 'active' ? 'selected' : ''}>გამოქვ.</option>
              <option value="inactive" ${product?.status === 'inactive' ? 'selected' : ''}>გაუქ.</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">SKU</label>
            <input class="form-control" name="sku" value="${product?.sku || ''}" placeholder="შეასვ. SKU">
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">სურათის URL (ერთი სტრ. - ერთი URL)</label>
          <textarea class="form-control" name="images" rows="3" placeholder="https://…&#10;https://…">${(product?.images || []).join('\n')}</textarea>
          <span class="form-hint">ჩამოწერეთ სურათების URL-ები, თითო ხაზზე ერთი.</span>
        </div>
      </form>
    `,
    footer: `
      <button class="btn btn-secondary modal-close">გაუქ.</button>
      <button class="btn btn-primary" id="saveProductBtn">${product ? 'შენახვა' : 'შექმნა'}</button>
    `,
  });

  document.getElementById('saveProductBtn').addEventListener('click', async () => {
    const form = document.getElementById('productForm');
    const data = Object.fromEntries(new FormData(form).entries());
    data.price = parseFloat(data.price) || 0;
    if (data.compare_price) data.compare_price = parseFloat(data.compare_price);
    else delete data.compare_price;
    if (data.stock_quantity) data.stock_quantity = parseInt(data.stock_quantity);
    else delete data.stock_quantity;
    data.images = data.images.split('\n').map(s => s.trim()).filter(Boolean);
    if (!data.category_id) delete data.category_id;

    if (!data.name) { window.__APP__.toast.error('სახელი სავალდებულოა'); return; }

    const btn = document.getElementById('saveProductBtn');
    btn.disabled = true; btn.textContent = 'ინახება…';

    const { error } = product
      ? await supabase.from('products').update(data).eq('id', product.id)
      : await supabase.from('products').insert(data);

    if (error) {
      window.__APP__.toast.error('შეცდომა: ' + error.message);
      btn.disabled = false; btn.textContent = product ? 'შენახვა' : 'შექმნა';
    } else {
      window.__APP__.toast.success(product ? 'პროდ. განახლდა' : 'პროდ. შეიქმნა');
      closeModal('productModal');
      loadProducts(supabase);
    }
  });
}
