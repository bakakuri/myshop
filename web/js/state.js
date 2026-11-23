// ── App State ─────────────────────────────────────────────────
const load = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) ?? d; } catch { return d; } };
const save = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)); } catch {} };

export const state = {
  page: 'home',
  cart:     load('ms_cart', []),
  wishlist: load('ms_wish', []),
  search:   '',
  filter:   { cat: '', priceMin: 0, priceMax: 99999, brands: [] },
  sort:     'popular',
  chatMsgs: load('ms_chat', []),
  user:     load('ms_user', { name: 'გიორგი', email: 'giorgi@example.com', orders: 12, reviews: 7, wishCount: 0 }),
};

export function setPage(p) { state.page = p; }

// Cart
export function getCartItem(id) { return state.cart.find(i => i.id === id); }
export function addToCart(product, qty = 1) {
  const ex = getCartItem(product.id);
  if (ex) ex.qty += qty;
  else state.cart.push({ ...product, qty });
  save('ms_cart', state.cart);
}
export function removeFromCart(id) {
  state.cart = state.cart.filter(i => i.id !== id);
  save('ms_cart', state.cart);
}
export function updateCartQty(id, qty) {
  const item = getCartItem(id);
  if (!item) return;
  if (qty <= 0) removeFromCart(id);
  else { item.qty = qty; save('ms_cart', state.cart); }
}
export function clearCart() { state.cart = []; save('ms_cart', state.cart); }
export function cartTotal() { return state.cart.reduce((s, i) => s + i.price * i.qty, 0); }
export function cartCount() { return state.cart.reduce((s, i) => s + i.qty, 0); }

// Wishlist
export function inWishlist(id) { return state.wishlist.includes(id); }
export function toggleWishlist(id) {
  if (inWishlist(id)) state.wishlist = state.wishlist.filter(x => x !== id);
  else state.wishlist.push(id);
  save('ms_wish', state.wishlist);
}

// Chat
export function addChatMsg(msg) {
  state.chatMsgs.push(msg);
  if (state.chatMsgs.length > 100) state.chatMsgs = state.chatMsgs.slice(-100);
  save('ms_chat', state.chatMsgs);
}
