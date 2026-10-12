/* ShopSphere storefront: a dependency-free single-page app that talks only to the API gateway. */
(() => {
  'use strict';

  const SERVICES = [
    { id: 'gateway', name: 'API Gateway', path: '/actuator/health', detail: 'Spring Cloud Gateway · JWT · rate limiting' },
    { id: 'auth', name: 'Auth service', path: '/status/auth', detail: 'Users & JWT · PostgreSQL' },
    { id: 'product', name: 'Product service', path: '/status/product', detail: 'Catalogue · MongoDB' },
    { id: 'inventory', name: 'Inventory service', path: '/status/inventory', detail: 'Stock reservations · RabbitMQ' },
    { id: 'cart', name: 'Cart service', path: '/status/cart', detail: 'Shopping carts · Redis' },
    { id: 'order', name: 'Order service', path: '/status/order', detail: 'Saga orchestrator · RabbitMQ' },
    { id: 'payment', name: 'Payment service', path: '/status/payment', detail: 'Mock payments · Resilience4j' },
    { id: 'notification', name: 'Notification service', path: '/status/notification', detail: 'Order emails · RabbitMQ' },
  ];
  const SAGA = [
    { status: 'PENDING', label: 'Order placed', service: 'order-service' },
    { status: 'STOCK_RESERVED', label: 'Stock reserved', service: 'inventory-service' },
    { status: 'PAID', label: 'Payment captured', service: 'payment-service' },
    { status: 'CONFIRMED', label: 'Confirmed', service: 'notification-service' },
  ];

  const state = {
    session: load('shopsphere.session'),
    products: [],
    stock: {},
    category: 'All',
    query: '',
    cart: { items: [], itemCount: 0, subtotal: 0 },
    orders: [],
    notifications: [],
    seenNotifications: Number(load('shopsphere.seen') || 0),
  };
  let pollTimer = null;
  let authMode = 'login';

  // ---------- helpers ----------
  function load(key) { try { return JSON.parse(localStorage.getItem(key)); } catch { return null; } }
  function save(key, value) { try { value == null ? localStorage.removeItem(key) : localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage unavailable */ } }
  const $ = (selector) => document.querySelector(selector);
  const money = (value) => '₹' + Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
  const shortId = (id) => '#' + String(id).slice(0, 8);
  const timeAgo = (iso) => {
    const seconds = Math.max(1, Math.round((Date.now() - new Date(iso).getTime()) / 1000));
    if (seconds < 60) return `${seconds}s ago`;
    if (seconds < 3600) return `${Math.round(seconds / 60)}m ago`;
    if (seconds < 86400) return `${Math.round(seconds / 3600)}h ago`;
    return new Date(iso).toLocaleDateString();
  };

  /** Builds DOM nodes with textContent only, so product names or messages can never inject HTML. */
  function el(tag, props = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(props)) {
      if (value == null || value === false) continue;
      if (key === 'class') node.className = value;
      else if (key === 'text') node.textContent = value;
      else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
      else node.setAttribute(key, value === true ? '' : value);
    }
    for (const child of children.flat()) if (child != null) node.append(child.nodeType ? child : document.createTextNode(String(child)));
    return node;
  }

  function toast(message, isError = false) {
    const node = el('div', { class: 'toast' + (isError ? ' error' : ''), text: message });
    $('#toasts').append(node);
    setTimeout(() => node.remove(), 4000);
  }

  async function api(path, { method = 'GET', body, auth = false } = {}) {
    const headers = { Accept: 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (state.session?.accessToken) headers.Authorization = 'Bearer ' + state.session.accessToken;
    const response = await fetch(path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
    if (response.status === 401 && auth) {
      setSession(null);
      openAuth('login');
      throw new Error('Your session expired, please sign in again');
    }
    const text = await response.text();
    const data = text ? safeJson(text) : null;
    if (!response.ok) {
      const fields = data?.fieldErrors ? Object.entries(data.fieldErrors).map(([f, m]) => `${f} ${m}`).join(', ') : '';
      throw new Error(fields || data?.message || `Request failed (${response.status})`);
    }
    return data;
  }
  function safeJson(text) { try { return JSON.parse(text); } catch { return null; } }

  // ---------- session ----------
  function setSession(session) {
    state.session = session;
    save('shopsphere.session', session);
    renderUser();
    if (session) { refreshCart(); refreshNotifications(); }
    else { state.cart = { items: [], itemCount: 0, subtotal: 0 }; state.orders = []; renderCart(); }
  }

  function renderUser() {
    const user = state.session?.user;
    $('#signin-btn').hidden = !!user;
    $('#user-chip').hidden = !user;
    $('#bell').hidden = !user;
    document.querySelectorAll('.admin-only').forEach((node) => { node.hidden = user?.role !== 'ADMIN'; });
    if (user) {
      $('#user-name').textContent = user.name;
      $('#user-avatar').textContent = (user.name || user.email).charAt(0).toUpperCase();
    }
  }

  function openAuth(mode) {
    authMode = mode;
    document.querySelectorAll('.tab').forEach((tab) => tab.classList.toggle('active', tab.dataset.mode === mode));
    document.querySelectorAll('.register-only').forEach((node) => { node.hidden = mode !== 'register'; });
    $('#auth-form').name.required = mode === 'register';
    $('#auth-form').password.autocomplete = mode === 'register' ? 'new-password' : 'current-password';
    $('#auth-submit').textContent = mode === 'register' ? 'Create account' : 'Sign in';
    $('#auth-error').hidden = true;
    const dialog = $('#auth-dialog');
    if (!dialog.open) dialog.showModal();
  }

  async function submitAuth(event) {
    event.preventDefault();
    const form = event.target;
    const payload = { email: form.email.value.trim(), password: form.password.value };
    if (authMode === 'register') payload.name = form.name.value.trim();
    $('#auth-submit').disabled = true;
    try {
      const session = await api(`/api/auth/${authMode}`, { method: 'POST', body: payload });
      setSession(session);
      $('#auth-dialog').close();
      form.reset();
      toast(`Welcome, ${session.user.name}!`);
      route();
    } catch (error) {
      $('#auth-error').textContent = error.message;
      $('#auth-error').hidden = false;
    } finally {
      $('#auth-submit').disabled = false;
    }
  }

  function requireSignIn() {
    if (state.session) return true;
    toast('Please sign in first');
    openAuth('login');
    return false;
  }

  // ---------- catalogue ----------
  async function loadCatalogue() {
    $('#products').replaceChildren(...Array.from({ length: 8 }, () => el('div', { class: 'skeleton' })));
    try {
      const [products, stock] = await Promise.all([api('/api/products'), api('/api/inventory').catch(() => ({}))]);
      state.products = products || [];
      state.stock = stock || {};
      renderCategories();
      renderProducts();
      renderAdmin();
    } catch (error) {
      $('#products').replaceChildren(el('div', { class: 'empty card' }, el('h3', { text: 'The catalogue is unavailable' }),
        el('p', { text: `${error.message}. The product service may still be starting; this page retries automatically.` })));
      setTimeout(loadCatalogue, 5000);
    }
  }

  function renderCategories() {
    const categories = ['All', ...new Set(state.products.map((p) => p.category))];
    $('#categories').replaceChildren(...categories.map((name) => el('button', {
      class: 'chip' + (name === state.category ? ' active' : ''), role: 'tab', text: name,
      onclick: () => { state.category = name; renderCategories(); renderProducts(); },
    })));
    $('#category-list').replaceChildren(...categories.slice(1).map((name) => el('option', { value: name })));
  }

  function stockPill(sku) {
    const available = state.stock[sku];
    if (available === undefined) return el('span', { class: 'pill info', text: 'Stock unknown' });
    if (available <= 0) return el('span', { class: 'pill bad', text: 'Out of stock' });
    if (available <= 5) return el('span', { class: 'pill warn', text: `Only ${available} left` });
    return el('span', { class: 'pill ok', text: `${available} in stock` });
  }

  function renderProducts() {
    const query = state.query.toLowerCase();
    const visible = state.products.filter((p) => (state.category === 'All' || p.category === state.category)
      && (!query || p.name.toLowerCase().includes(query) || (p.description || '').toLowerCase().includes(query)));
    if (!visible.length) {
      $('#products').replaceChildren(el('div', { class: 'empty', text: 'No products match your search.' }));
      return;
    }
    $('#products').replaceChildren(...visible.map((product) => {
      const soldOut = (state.stock[product.id] ?? 1) <= 0;
      return el('article', { class: 'card product' },
        el('div', { class: 'art', 'aria-hidden': 'true', text: product.image || '📦' }),
        el('div', { class: 'body' },
          el('span', { class: 'pill info', text: product.category }),
          el('h3', { text: product.name }),
          el('p', { text: product.description }),
          el('div', { class: 'meta' }, el('span', { class: 'price', text: money(product.price) }), stockPill(product.id)),
          el('button', { class: 'btn', disabled: soldOut, text: soldOut ? 'Sold out' : 'Add to cart', onclick: () => addToCart(product) })));
    }));
  }

  // ---------- cart ----------
  async function refreshCart() {
    if (!state.session) return renderCart();
    try { state.cart = await api('/api/carts', { auth: true }); } catch (error) { toast(error.message, true); }
    renderCart();
  }

  async function addToCart(product) {
    if (!requireSignIn()) return;
    try {
      state.cart = await api('/api/carts/items', { method: 'POST', auth: true,
        body: { productId: product.id, name: product.name, price: product.price, image: product.image, quantity: 1 } });
      renderCart();
      toast(`${product.name} added to cart`);
    } catch (error) { toast(error.message, true); }
  }

  async function setQuantity(item, quantity) {
    try {
      state.cart = await api(`/api/carts/items/${encodeURIComponent(item.productId)}?quantity=${quantity}`, { method: 'PUT', auth: true });
      renderCart();
    } catch (error) { toast(error.message, true); }
  }

  function renderCart() {
    const cart = state.cart;
    $('#cart-count').hidden = !cart.itemCount;
    $('#cart-count').textContent = cart.itemCount;
    $('#cart-subtotal').textContent = money(cart.subtotal);
    $('#checkout-btn').disabled = !cart.items.length;
    if (!cart.items.length) {
      $('#cart-items').replaceChildren(el('div', { class: 'empty' }, el('div', { class: 'big-icon', text: '🛒' }),
        el('p', { text: state.session ? 'Your cart is empty.' : 'Sign in to start a cart.' })));
      return;
    }
    $('#cart-items').replaceChildren(...cart.items.map((item) => el('div', { class: 'line' },
      el('div', { class: 'art', text: item.image || '📦' }),
      el('div', {}, el('div', { class: 'name', text: item.name }), el('small', { class: 'muted', text: `${money(item.price)} each` })),
      el('div', { class: 'qty' },
        el('button', { 'aria-label': 'Decrease quantity', text: '−', onclick: () => setQuantity(item, item.quantity - 1) }),
        el('span', { text: item.quantity }),
        el('button', { 'aria-label': 'Increase quantity', text: '+', onclick: () => setQuantity(item, item.quantity + 1) })))));
  }

  function toggleCart(open) {
    $('#cart-drawer').hidden = !open;
    $('#scrim').hidden = !open;
    if (open) refreshCart();
  }

  async function checkout() {
    if (!requireSignIn() || !state.cart.items.length) return;
    $('#checkout-btn').disabled = true;
    try {
      const order = await api('/api/orders', { method: 'POST', auth: true,
        body: { items: state.cart.items.map((item) => ({ productId: item.productId, quantity: item.quantity })) } });
      await api('/api/carts', { method: 'DELETE', auth: true });
      state.cart = { items: [], itemCount: 0, subtotal: 0 };
      renderCart();
      toggleCart(false);
      toast(`Order ${shortId(order.id)} placed. Watch the saga run!`);
      location.hash = '#/orders';
    } catch (error) {
      toast(error.message, true);
      $('#checkout-btn').disabled = false;
    }
  }

  // ---------- orders ----------
  async function loadOrders() {
    if (!state.session) {
      $('#orders').replaceChildren(el('div', { class: 'empty card' }, el('p', { text: 'Sign in to see your orders.' }),
        el('button', { class: 'btn', text: 'Sign in', onclick: () => openAuth('login') })));
      return;
    }
    try {
      state.orders = await api('/api/orders', { auth: true });
      renderOrders();
      const running = state.orders.some((o) => o.status !== 'CONFIRMED' && o.status !== 'CANCELLED');
      clearTimeout(pollTimer);
      loadStock();
      if (running && currentRoute() === 'orders') pollTimer = setTimeout(() => { loadOrders(); refreshNotifications(); }, 1000);
    } catch (error) { toast(error.message, true); }
  }

  function sagaSteps(order) {
    const reached = new Set(order.history.map((h) => h.status));
    if (order.status === 'CONFIRMED') reached.add('PAID');
    const cancelled = order.status === 'CANCELLED';
    let failedAt = -1;
    if (cancelled) failedAt = reached.has('STOCK_RESERVED') ? 2 : 1;
    return SAGA.map((step, index) => {
      const lookup = step.status === 'PAID' ? 'CONFIRMED' : step.status;
      const at = order.history.find((h) => h.status === lookup)?.at;
      let cls = 'step';
      let note = step.service;
      if (reached.has(step.status)) cls += ' done';
      else if (index === failedAt) { cls += ' failed'; note = 'Failed · compensated'; }
      else if (!cancelled && index === [...reached].filter((s) => SAGA.some((x) => x.status === s)).length) cls += ' current';
      return el('div', { class: cls }, el('b', { text: step.label }), el('span', { text: at ? timeAgo(at) : note }));
    });
  }

  function renderOrders() {
    if (!state.orders.length) {
      $('#orders').replaceChildren(el('div', { class: 'empty card' }, el('p', { text: 'No orders yet.' }),
        el('a', { class: 'btn', href: '#/shop', text: 'Start shopping' })));
      return;
    }
    $('#orders').replaceChildren(...state.orders.map((order) => {
      const pill = order.status === 'CONFIRMED' ? 'ok' : order.status === 'CANCELLED' ? 'bad' : 'warn';
      return el('article', { class: 'card order' },
        el('div', { class: 'order-head' },
          el('h3', { text: `Order ${shortId(order.id)}` }),
          el('div', {}, el('span', { class: `pill ${pill}`, text: order.status.replace('_', ' ') }), ' ',
            el('b', { text: money(order.total) }), ' ', el('small', { class: 'muted', text: timeAgo(order.createdAt) }))),
        el('ul', { class: 'order-lines' }, order.items.map((item) => el('li', { text: `${item.image || '📦'} ${item.name} × ${item.quantity}` }))),
        el('div', { class: 'timeline' }, sagaSteps(order)),
        order.reason ? el('p', { class: 'reason', text: order.history.some((h) => h.status === 'STOCK_RESERVED')
          ? `Reason: ${order.reason}. The saga compensated by releasing the reserved stock.` : `Reason: ${order.reason}.` }) : null);
    }));
  }

  // Stock is shared by every shopper, so it is re-read after each order and polled while the page is visible.
  async function loadStock() {
    try {
      const stock = await api('/api/inventory') || {};
      if (JSON.stringify(stock) === JSON.stringify(state.stock)) return;
      state.stock = stock;
      renderProducts();
    } catch { /* keep last known stock */ }
  }

  // ---------- notifications ----------
  async function refreshNotifications() {
    if (!state.session) return;
    try {
      state.notifications = await api('/api/notifications', { auth: true }) || [];
      const unseen = state.notifications.filter((n) => new Date(n.at).getTime() > state.seenNotifications).length;
      $('#bell-count').hidden = !unseen;
      $('#bell-count').textContent = unseen;
      $('#notif-list').replaceChildren(...(state.notifications.length ? state.notifications.slice(0, 20).map((n) => el('li', {},
        el('b', { text: `${n.level === 'SUCCESS' ? '✅' : '⚠️'} ${n.title}` }), el('span', { text: n.message }), el('br'),
        el('time', { text: timeAgo(n.at) }))) : [el('li', { class: 'muted', text: 'Nothing yet. Place an order!' })]));
    } catch { /* non-critical */ }
  }

  function toggleNotifications() {
    const panel = $('#notif-panel');
    panel.hidden = !panel.hidden;
    if (!panel.hidden) {
      state.seenNotifications = Date.now();
      save('shopsphere.seen', state.seenNotifications);
      $('#bell-count').hidden = true;
    }
  }

  // ---------- platform ----------
  async function loadHealth() {
    $('#health').replaceChildren(...SERVICES.map((service) => el('div', { class: 'card health', id: `health-${service.id}` },
      el('div', { class: 'top' }, el('h3', { text: service.name }), el('span', {}, el('span', { class: 'dot pending' }), 'checking')),
      el('small', { text: service.detail }))));
    await Promise.all(SERVICES.map(async (service) => {
      const started = performance.now();
      let up = false;
      try {
        const response = await fetch(service.path, { headers: { Accept: 'application/json' } });
        up = response.ok && (await response.json()).status === 'UP';
      } catch { up = false; }
      const ms = Math.round(performance.now() - started);
      const card = document.getElementById(`health-${service.id}`);
      if (!card) return;
      card.querySelector('.top span').replaceChildren(el('span', { class: `dot ${up ? 'up' : 'down'}` }), up ? `UP · ${ms} ms` : 'DOWN');
    }));
  }

  // ---------- admin ----------
  function renderAdmin() {
    if (state.session?.user?.role !== 'ADMIN') return;
    $('#stock-rows').replaceChildren(...state.products.map((product) => {
      const input = el('input', { type: 'number', min: '0', value: state.stock[product.id] ?? 0, 'aria-label': `Stock for ${product.name}` });
      return el('tr', {},
        el('td', { text: `${product.image || '📦'} ${product.name}` }), el('td', {}, el('code', { text: product.id })), el('td', {}, input),
        el('td', {}, el('button', { class: 'btn btn-small btn-ghost', text: 'Save', onclick: () => saveStock(product, input.value) })));
    }));
  }

  async function saveStock(product, quantity) {
    try {
      await api(`/api/inventory/${encodeURIComponent(product.id)}?quantity=${Number(quantity)}`, { method: 'PUT', auth: true });
      state.stock[product.id] = Number(quantity);
      toast(`Stock for ${product.name} set to ${quantity}`);
      renderProducts();
    } catch (error) { toast(error.message, true); }
  }

  async function createProduct(event) {
    event.preventDefault();
    const form = event.target;
    try {
      const product = await api('/api/products', { method: 'POST', auth: true, body: {
        name: form.name.value, description: form.description.value, price: Number(form.price.value),
        category: form.category.value, image: form.image.value } });
      await api(`/api/inventory/${encodeURIComponent(product.id)}?quantity=${Number(form.stock.value || 0)}`, { method: 'PUT', auth: true });
      form.reset();
      toast(`${product.name} is now in the catalogue`);
      loadCatalogue();
    } catch (error) { toast(error.message, true); }
  }

  // ---------- routing ----------
  function currentRoute() { return (location.hash.replace('#/', '') || 'shop').split('?')[0]; }

  function route() {
    let name = currentRoute();
    if (!['shop', 'orders', 'platform', 'admin'].includes(name)) name = 'shop';
    if (name === 'admin' && state.session?.user?.role !== 'ADMIN') name = 'shop';
    document.querySelectorAll('.view').forEach((view) => { view.hidden = view.id !== `view-${name}`; });
    document.querySelectorAll('.nav a').forEach((link) => link.classList.toggle('active', link.dataset.route === name));
    clearTimeout(pollTimer);
    if (name === 'shop') loadStock();
    if (name === 'orders') loadOrders();
    if (name === 'platform') loadHealth();
    if (name === 'admin') renderAdmin();
  }

  // ---------- wiring ----------
  $('#signin-btn').addEventListener('click', () => openAuth('login'));
  $('#signout-btn').addEventListener('click', () => { setSession(null); toast('Signed out'); location.hash = '#/shop'; });
  $('#auth-cancel').addEventListener('click', () => $('#auth-dialog').close());
  $('#auth-form').addEventListener('submit', submitAuth);
  document.querySelectorAll('.tab').forEach((tab) => tab.addEventListener('click', () => openAuth(tab.dataset.mode)));
  $('#cart-btn').addEventListener('click', () => toggleCart(true));
  $('#cart-close').addEventListener('click', () => toggleCart(false));
  $('#scrim').addEventListener('click', () => toggleCart(false));
  $('#checkout-btn').addEventListener('click', checkout);
  $('#bell').addEventListener('click', toggleNotifications);
  $('#product-form').addEventListener('submit', createProduct);
  $('#search').addEventListener('input', (event) => { state.query = event.target.value; renderProducts(); });
  document.addEventListener('keydown', (event) => { if (event.key === 'Escape') { toggleCart(false); $('#notif-panel').hidden = true; } });
  window.addEventListener('hashchange', route);
  $('#footer-env').textContent = location.host;

  renderUser();
  renderCart();
  route();
  loadCatalogue();
  if (state.session) { refreshCart(); refreshNotifications(); }
  setInterval(refreshNotifications, 15000);
  setInterval(() => { if (document.visibilityState === 'visible') loadStock(); }, 5000);
  document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'visible') loadStock(); });
})();
