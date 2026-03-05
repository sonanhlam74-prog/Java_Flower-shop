const CATEGORIES = ["Hoa tết", "Hoa Chia buồn", "Hoa Chúc Mừng", "Hoa Sinh Nhật", "Hoa Cưới"];
const TAX_RATE = 0.08;
const DELIVERY_ZONE_BASE_FEE = {
  "Nội thành": 20000,
  "Cận thành": 35000,
  "Liên tỉnh": 55000
};
const DELIVERY_SPEED_MULTIPLIER = {
  "Tiêu chuẩn (24h)": 1.0,
  "Nhanh (4h)": 1.6,
  "Hỏa tốc (2h)": 2.2
};
const COUPONS = {
  GIAM10: 0.10,
  GIAM20: 0.20,
  FLOWER50: 0.50,
  WELCOME: 0.15
};
const TIERS = [
  { key: "BRONZE", name: "🥉 Đồng", threshold: 0, discount: 0 },
  { key: "SILVER", name: "🥈 Bạc", threshold: 500000, discount: 0.05 },
  { key: "GOLD", name: "🥇 Vàng", threshold: 1500000, discount: 0.10 },
  { key: "DIAMOND", name: "💎 Kim Cương", threshold: 3000000, discount: 0.15 },
  { key: "VIP", name: "👑 VIP", threshold: 5000000, discount: 0.20 }
];

const STORAGE_KEY = "flower_web_state_v1";

function seedInventory() {
  return [
    ["Hoa Mai Vàng", "Hoa tết", 280000, 30, "🌼"],
    ["Hoa Đào Nhật Tân", "Hoa tết", 320000, 28, "🌺"],
    ["Hoa Cúc Mâm Xôi", "Hoa tết", 260000, 24, "🌻"],
    ["Hoa Lan Hồ Điệp Tết", "Hoa tết", 450000, 16, "🌷"],
    ["Hoa Cúc Trắng", "Hoa Chia buồn", 190000, 25, "🤍"],
    ["Hoa Huệ Trắng", "Hoa Chia buồn", 210000, 22, "⚪"],
    ["Hoa Ly Trắng", "Hoa Chia buồn", 240000, 18, "🕊"],
    ["Hoa Hồng Trắng", "Hoa Chia buồn", 220000, 20, "🥀"],
    ["Hoa Hướng Dương", "Hoa Chúc Mừng", 230000, 26, "🌻"],
    ["Hoa Hồng Đỏ", "Hoa Chúc Mừng", 250000, 32, "🌹"],
    ["Hoa Tulip", "Hoa Chúc Mừng", 310000, 15, "🌷"],
    ["Hoa Cẩm Chướng", "Hoa Chúc Mừng", 205000, 27, "🌸"],
    ["Hoa Hồng Phấn", "Hoa Sinh Nhật", 275000, 20, "🌷"],
    ["Hoa Đồng Tiền", "Hoa Sinh Nhật", 195000, 30, "🌼"],
    ["Hoa Baby Trắng", "Hoa Sinh Nhật", 180000, 35, "🤍"],
    ["Hoa Lan Mokara", "Hoa Sinh Nhật", 350000, 18, "💜"],
    ["Hoa Hồng Pastel", "Hoa Cưới", 380000, 14, "💐"],
    ["Hoa Cát Tường", "Hoa Cưới", 290000, 22, "🌿"],
    ["Hoa Mẫu Đơn", "Hoa Cưới", 420000, 12, "🌺"],
    ["Hoa Phi Yến", "Hoa Cưới", 340000, 16, "💠"]
  ].map((row, idx) => ({ id: 1001 + idx, name: row[0], category: row[1], price: row[2], stock: row[3], emoji: row[4] }));
}

function defaultState() {
  return {
    users: {
      admin: { password: "123456", fullName: "Nguyễn Admin", email: "admin@flowershop.vn" }
    },
    currentUser: null,
    inventory: seedInventory(),
    cart: {},
    orders: [],
    membershipSpent: 0,
    nextOrderId: 1
  };
}

let state = loadState();
let activeView = "shop";
let appliedCouponRate = 0;
let appliedCouponCode = null;

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return defaultState();
    return { ...defaultState(), ...JSON.parse(raw) };
  } catch {
    return defaultState();
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function money(v) {
  return `${Math.round(v).toLocaleString("vi-VN")}₫`;
}

function toast(message) {
  const t = document.getElementById("toast");
  t.textContent = message;
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 1600);
}

function getCurrentTier() {
  let current = TIERS[0];
  for (const t of TIERS) {
    if (state.membershipSpent >= t.threshold) current = t;
  }
  return current;
}

function getNextTier() {
  const idx = TIERS.findIndex(t => t.key === getCurrentTier().key);
  return idx >= TIERS.length - 1 ? null : TIERS[idx + 1];
}

function isAdmin() {
  return state.currentUser === "admin";
}

function fullNameOfCurrent() {
  if (!state.currentUser) return "Guest";
  return state.users[state.currentUser]?.fullName || state.currentUser;
}

function cartEntries() {
  return Object.entries(state.cart).filter(([, qty]) => qty > 0);
}

function cartCount() {
  return cartEntries().reduce((sum, [, qty]) => sum + qty, 0);
}

function findFlower(name) {
  return state.inventory.find(f => f.name === name);
}

function subtotal() {
  return cartEntries().reduce((sum, [name, qty]) => {
    const f = findFlower(name);
    return sum + (f ? f.price * qty : 0);
  }, 0);
}

function setView(view) {
  activeView = view;
  document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
  document.getElementById(`view-${view}`)?.classList.add("active");
  renderNav();
  if (view === "shop") renderShop();
  if (view === "checkout") renderCheckout();
  if (view === "history") renderHistory();
  if (view === "profile") renderProfile();
  if (view === "admin") renderAdmin();
}

function renderNav() {
  const nav = document.getElementById("mainNav");
  const items = [
    ["shop", "Shop"],
    ["checkout", "Checkout"],
    ["history", "History"],
    ["profile", "Profile"]
  ];
  if (isAdmin()) items.push(["admin", "Admin"]);
  if (!state.currentUser) {
    items.push(["login", "Login"]);
    items.push(["register", "Register"]);
  } else {
    items.push(["logout", `Logout (${fullNameOfCurrent()})`]);
  }

  nav.innerHTML = "";
  for (const [key, label] of items) {
    const b = document.createElement("button");
    b.className = `btn ${activeView === key ? "primary" : ""}`;
    b.textContent = label;
    b.onclick = () => {
      if (key === "logout") {
        state.currentUser = null;
        saveState();
        toast("Đã đăng xuất");
        setView("shop");
        return;
      }
      setView(key);
    };
    nav.appendChild(b);
  }
}

function renderShop() {
  const search = document.getElementById("searchInput").value.trim().toLowerCase();
  const maxPrice = Number(document.getElementById("priceRange").value);
  document.getElementById("priceValue").textContent = money(maxPrice);

  const grid = document.getElementById("productGrid");
  grid.innerHTML = "";
  const filtered = state.inventory.filter(f => f.name.toLowerCase().includes(search) && f.price <= maxPrice);

  for (const f of filtered) {
    const card = document.createElement("div");
    card.className = "product";
    card.innerHTML = `
      <h4>${f.emoji} ${f.name}</h4>
      <p class="muted">${f.category}</p>
      <p class="price">${money(f.price)}</p>
      <p>Tồn kho: ${f.stock}</p>
      <button class="btn primary" ${f.stock <= 0 ? "disabled" : ""}>Thêm vào giỏ</button>
    `;
    card.querySelector("button").onclick = () => {
      if (f.stock <= 0) {
        toast("Sản phẩm đã hết hàng");
        return;
      }
      f.stock -= 1;
      state.cart[f.name] = (state.cart[f.name] || 0) + 1;
      saveState();
      renderShop();
      toast(`Đã thêm ${f.name}`);
    };
    grid.appendChild(card);
  }

  document.getElementById("memberSummary").textContent = `${getCurrentTier().name} • Giảm ${Math.round(getCurrentTier().discount * 100)}% • Đã chi: ${money(state.membershipSpent)}`;
  renderCartPreview();
}

function renderCartPreview() {
  const box = document.getElementById("cartPreview");
  box.innerHTML = "";
  const entries = cartEntries();
  if (!entries.length) {
    box.innerHTML = `<p class="muted">Giỏ hàng trống</p>`;
  } else {
    for (const [name, qty] of entries) {
      const f = findFlower(name);
      const row = document.createElement("div");
      row.className = "cart-row";
      row.innerHTML = `<span>${name} x${qty}</span><span>${money((f?.price || 0) * qty)}</span>`;
      box.appendChild(row);
    }
  }
  document.getElementById("cartCount").textContent = String(cartCount());
  document.getElementById("cartSubtotal").textContent = money(subtotal());
}

function shippingFeeBy(sub) {
  const zone = document.getElementById("coZone").value;
  const speed = document.getElementById("coSpeed").value;
  const base = DELIVERY_ZONE_BASE_FEE[zone] || 20000;
  const mul = DELIVERY_SPEED_MULTIPLIER[speed] || 1;
  if (sub <= 0) return 0;
  return base * mul;
}

function renderCheckout() {
  if (!cartEntries().length) {
    toast("Giỏ hàng đang trống");
    setView("shop");
    return;
  }

  const zoneSelect = document.getElementById("coZone");
  const speedSelect = document.getElementById("coSpeed");

  if (!zoneSelect.options.length) {
    Object.keys(DELIVERY_ZONE_BASE_FEE).forEach(k => zoneSelect.add(new Option(k, k)));
    zoneSelect.value = "Nội thành";
  }
  if (!speedSelect.options.length) {
    Object.keys(DELIVERY_SPEED_MULTIPLIER).forEach(k => speedSelect.add(new Option(k, k)));
    speedSelect.value = "Tiêu chuẩn (24h)";
  }

  if (state.currentUser) {
    document.getElementById("coFullName").value = fullNameOfCurrent();
    document.getElementById("coEmail").value = state.users[state.currentUser].email;
  }

  const items = document.getElementById("checkoutItems");
  items.innerHTML = "";
  for (const [name, qty] of cartEntries()) {
    const f = findFlower(name);
    const div = document.createElement("div");
    div.className = "cart-row";
    div.innerHTML = `<span>${name} x${qty}</span><span>${money((f?.price || 0) * qty)}</span>`;
    items.appendChild(div);
  }

  updateCheckoutSummary();
}

function updateCheckoutSummary() {
  const sub = subtotal();
  const tax = sub * TAX_RATE;
  const memberRate = getCurrentTier().discount;
  const memberDiscount = (sub + tax) * memberRate;
  const afterMember = sub + tax - memberDiscount;
  const couponDiscount = afterMember * appliedCouponRate;
  const shipping = shippingFeeBy(sub);
  const total = afterMember - couponDiscount + shipping;

  document.getElementById("sumSubtotal").textContent = money(sub);
  document.getElementById("sumTax").textContent = money(tax);
  document.getElementById("sumMemberDiscount").textContent = `- ${money(memberDiscount)}`;
  document.getElementById("sumCouponDiscount").textContent = `- ${money(couponDiscount)}`;
  document.getElementById("sumShipping").textContent = money(shipping);
  document.getElementById("sumTotal").textContent = money(total);

  return { sub, tax, memberDiscount, couponDiscount, shipping, total };
}

function renderHistory() {
  const stats = document.getElementById("historyStats");
  const completedRevenue = state.orders.filter(o => o.status !== "Đang giao").reduce((s, o) => s + o.total, 0);
  stats.textContent = `Số đơn: ${state.orders.length} • Doanh thu: ${money(completedRevenue)} • Hạng hiện tại: ${getCurrentTier().name}`;

  const list = document.getElementById("historyList");
  list.innerHTML = "";
  if (!state.orders.length) {
    list.innerHTML = `<div class="history-card"><p class="muted">Chưa có đơn hàng nào.</p></div>`;
    return;
  }

  for (const order of state.orders) {
    const card = document.createElement("div");
    card.className = "history-card";
    const itemRows = order.items.map(i => `<li>${i.name} x${i.quantity} - ${money(i.subtotal)}</li>`).join("");
    card.innerHTML = `
      <p><strong>Đơn #${order.orderId}</strong> • ${order.dateTime} • ${order.status}</p>
      <p>Khách: ${order.customerName} • Hạng lúc mua: ${order.tierAtPurchase}</p>
      <ul>${itemRows}</ul>
      <p>Tổng: <strong>${money(order.total)}</strong></p>
    `;
    list.appendChild(card);
  }
}

function renderProfile() {
  if (!state.currentUser) {
    toast("Vui lòng đăng nhập");
    setView("login");
    return;
  }
  const user = state.users[state.currentUser];
  const tier = getCurrentTier();
  const next = getNextTier();
  document.getElementById("pfUsername").textContent = state.currentUser;
  document.getElementById("pfEmail").textContent = user.email;
  document.getElementById("pfFullName").value = user.fullName;
  document.getElementById("pfTier").textContent = tier.name;
  document.getElementById("pfDiscount").textContent = `${Math.round(tier.discount * 100)}%`;
  document.getElementById("pfSpent").textContent = money(state.membershipSpent);
  document.getElementById("pfNextTier").textContent = next ? `${next.name} (còn ${money(next.threshold - state.membershipSpent)})` : "Đã tối đa";
}

function renderAdmin() {
  if (!isAdmin()) {
    toast("Chỉ admin mới truy cập được");
    setView("shop");
    return;
  }
  const adCategory = document.getElementById("adCategory");
  if (!adCategory.options.length) {
    CATEGORIES.forEach(c => adCategory.add(new Option(c, c)));
  }

  const tbody = document.querySelector("#adminTable tbody");
  tbody.innerHTML = "";
  state.inventory.forEach(f => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${f.id}</td><td>${f.name}</td><td>${f.category}</td><td>${money(f.price)}</td><td>${f.stock}</td>`;
    tr.onclick = () => {
      document.getElementById("adId").value = f.id;
      document.getElementById("adName").value = f.name;
      document.getElementById("adCategory").value = f.category;
      document.getElementById("adPrice").value = f.price;
      document.getElementById("adStock").value = f.stock;
    };
    tbody.appendChild(tr);
  });
}

function bindEvents() {
  document.getElementById("searchInput").addEventListener("input", renderShop);
  document.getElementById("priceRange").addEventListener("input", renderShop);
  document.getElementById("goCheckoutBtn").addEventListener("click", () => setView("checkout"));

  document.querySelectorAll("[data-nav]").forEach(btn => {
    btn.addEventListener("click", () => setView(btn.dataset.nav));
  });

  document.getElementById("loginForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const u = document.getElementById("loginUsername").value.trim();
    const p = document.getElementById("loginPassword").value;
    if (!u || !p) return toast("Vui lòng nhập đủ tài khoản và mật khẩu");
    if (!state.users[u] || state.users[u].password !== p) return toast("Sai tài khoản hoặc mật khẩu");
    state.currentUser = u;
    saveState();
    toast("Đăng nhập thành công");
    setView("shop");
  });

  document.getElementById("registerForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const fullName = document.getElementById("regFullName").value.trim();
    const email = document.getElementById("regEmail").value.trim();
    const username = document.getElementById("regUsername").value.trim();
    const password = document.getElementById("regPassword").value;
    const confirm = document.getElementById("regConfirmPassword").value;

    if (!fullName || !email || !username || !password) return toast("Vui lòng nhập đầy đủ thông tin");
    if (!/^[-\w.+]+@[-\w.]+\.[a-zA-Z]{2,}$/.test(email)) return toast("Email không đúng định dạng");
    if (password.length < 6) return toast("Mật khẩu phải có ít nhất 6 ký tự");
    if (password !== confirm) return toast("Mật khẩu xác nhận không khớp");
    if (state.users[username]) return toast("Tài khoản đã tồn tại");

    state.users[username] = { fullName, email, password };
    saveState();
    toast("Đăng ký thành công, vui lòng đăng nhập");
    setView("login");
  });

  document.getElementById("forgotForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const username = document.getElementById("forgotUsername").value.trim();
    const email = document.getElementById("forgotEmail").value.trim();
    const newPassword = document.getElementById("forgotNewPassword").value;
    const confirm = document.getElementById("forgotConfirmPassword").value;

    const user = state.users[username];
    if (!user || user.email.toLowerCase() !== email.toLowerCase()) return toast("Sai tài khoản hoặc email");
    if (newPassword.length < 6) return toast("Mật khẩu mới phải >= 6 ký tự");
    if (newPassword !== confirm) return toast("Xác nhận mật khẩu không khớp");

    user.password = newPassword;
    saveState();
    toast("Đặt lại mật khẩu thành công");
    setView("login");
  });

  document.getElementById("coZone").addEventListener("change", updateCheckoutSummary);
  document.getElementById("coSpeed").addEventListener("change", updateCheckoutSummary);

  document.getElementById("applyCouponBtn").addEventListener("click", () => {
    const code = document.getElementById("couponInput").value.trim().toUpperCase();
    const rate = COUPONS[code];
    const status = document.getElementById("couponStatus");
    if (!code) {
      status.textContent = "Vui lòng nhập mã giảm giá.";
      appliedCouponRate = 0;
      appliedCouponCode = null;
    } else if (!rate) {
      status.textContent = `Mã ${code} không hợp lệ.`;
      appliedCouponRate = 0;
      appliedCouponCode = null;
    } else {
      status.textContent = `Áp dụng mã ${code} thành công (-${Math.round(rate * 100)}%).`;
      appliedCouponRate = rate;
      appliedCouponCode = code;
    }
    updateCheckoutSummary();
  });

  document.getElementById("confirmOrderBtn").addEventListener("click", () => {
    const form = document.getElementById("checkoutForm");
    const fullName = document.getElementById("coFullName").value.trim();
    const email = document.getElementById("coEmail").value.trim();
    const phone = document.getElementById("coPhone").value.trim();
    const address = document.getElementById("coAddress").value.trim();
    const payment = document.getElementById("coPayment").value;
    const terms = document.getElementById("coTerms").checked;

    if (!form.reportValidity()) return;
    if (!/^[-\w.+]+@[-\w.]+\.[a-zA-Z]{2,}$/.test(email)) return toast("Email không đúng định dạng");
    if (!/^\d{9,11}$/.test(phone)) return toast("Số điện thoại không hợp lệ");
    if (!payment) return toast("Vui lòng chọn phương thức thanh toán");
    if (!terms) return toast("Bạn cần đồng ý điều khoản");
    if (!cartEntries().length) return toast("Giỏ hàng đang trống");

    const summary = updateCheckoutSummary();
    const tier = getCurrentTier();

    const orderItems = cartEntries().map(([name, quantity]) => {
      const f = findFlower(name);
      return {
        name,
        quantity,
        unitPrice: f?.price || 0,
        subtotal: (f?.price || 0) * quantity
      };
    });

    const now = new Date();
    const order = {
      orderId: state.nextOrderId++,
      dateTime: now.toLocaleString("vi-VN"),
      status: "Đang giao",
      customerName: fullName || "Khách vãng lai",
      tierAtPurchase: tier.name,
      couponCode: appliedCouponCode,
      delivery: {
        zone: document.getElementById("coZone").value,
        speed: document.getElementById("coSpeed").value,
        address,
        phone,
        email,
        payment
      },
      items: orderItems,
      subtotal: summary.sub,
      tax: summary.tax,
      discount: summary.memberDiscount + summary.couponDiscount,
      total: summary.total
    };

    state.orders.unshift(order);
    state.membershipSpent += summary.total;

    state.cart = {};
    appliedCouponRate = 0;
    appliedCouponCode = null;

    saveState();
    toast(`Đặt hàng thành công! Mã đơn #${order.orderId}`);
    setView("history");
  });

  document.getElementById("saveProfileBtn").addEventListener("click", () => {
    if (!state.currentUser) return;
    const fullName = document.getElementById("pfFullName").value.trim();
    if (!fullName) return toast("Tên không được để trống");
    state.users[state.currentUser].fullName = fullName;
    saveState();
    toast("Cập nhật hồ sơ thành công");
    renderNav();
  });

  document.getElementById("adAddBtn").addEventListener("click", () => {
    if (!isAdmin()) return;
    const name = document.getElementById("adName").value.trim();
    const category = document.getElementById("adCategory").value;
    const price = Number(document.getElementById("adPrice").value);
    const stock = Number(document.getElementById("adStock").value);
    if (!name || !category || price < 0 || stock < 0) return toast("Thông tin sản phẩm không hợp lệ");

    const maxId = Math.max(...state.inventory.map(i => i.id), 1000);
    state.inventory.push({ id: maxId + 1, name, category, price, stock, emoji: "🌸" });
    saveState();
    renderAdmin();
    renderShop();
    toast("Đã thêm sản phẩm");
  });

  document.getElementById("adUpdateBtn").addEventListener("click", () => {
    if (!isAdmin()) return;
    const id = Number(document.getElementById("adId").value);
    const flower = state.inventory.find(f => f.id === id);
    if (!flower) return toast("Không tìm thấy sản phẩm theo ID");

    flower.name = document.getElementById("adName").value.trim();
    flower.category = document.getElementById("adCategory").value;
    flower.price = Number(document.getElementById("adPrice").value);
    flower.stock = Number(document.getElementById("adStock").value);

    saveState();
    renderAdmin();
    renderShop();
    toast("Đã cập nhật sản phẩm");
  });

  document.getElementById("adDeleteBtn").addEventListener("click", () => {
    if (!isAdmin()) return;
    const id = Number(document.getElementById("adId").value);
    const idx = state.inventory.findIndex(f => f.id === id);
    if (idx < 0) return toast("Không tìm thấy sản phẩm để xóa");

    const [deleted] = state.inventory.splice(idx, 1);
    delete state.cart[deleted.name];

    saveState();
    renderAdmin();
    renderShop();
    toast("Đã xóa sản phẩm");
  });
}

function bootstrap() {
  bindEvents();
  renderNav();
  renderShop();
  setView("shop");
}

bootstrap();
