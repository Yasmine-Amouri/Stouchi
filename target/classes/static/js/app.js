const API = '';
let currentMonth, currentYear, allTransactions = [], allCategories = [], pieChart = null;

const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

function init() {
  const now = new Date();
  currentMonth = now.getMonth() + 1;
  currentYear = now.getFullYear();

  document.getElementById('txnDate').value = now.toISOString().split('T')[0];

  bindNavigation();
  bindMonthNav();
  bindModal();
  bindBudget();
  bindCategories();
  bindFilters();

  loadCategories().then(() => loadDashboard());
}

function bindNavigation() {
  document.querySelectorAll('.nav-item, .link-more').forEach(el => {
    el.addEventListener('click', e => {
      e.preventDefault();
      const page = el.dataset.page;
      if (!page) return;
      navigateTo(page);
    });
  });
}

function navigateTo(page) {
  document.querySelectorAll('.nav-item').forEach(n => n.classList.toggle('active', n.dataset.page === page));
  document.querySelectorAll('.page').forEach(p => p.classList.toggle('active', p.id === page));
  const titles = { dashboard: 'Dashboard', transactions: 'Transactions', budget: 'Budget', categories: 'Categories' };
  document.getElementById('pageTitle').textContent = titles[page] || page;
  document.getElementById('addTxnBtn').style.display = page === 'categories' || page === 'budget' ? 'none' : '';
  if (page === 'dashboard') loadDashboard();
  if (page === 'transactions') loadTransactionsPage();
  if (page === 'budget') loadBudgetPage();
  if (page === 'categories') renderCategories();
}

function bindMonthNav() {
  document.getElementById('prevMonth').addEventListener('click', () => shiftMonth(-1));
  document.getElementById('nextMonth').addEventListener('click', () => shiftMonth(1));
}

function shiftMonth(delta) {
  currentMonth += delta;
  if (currentMonth > 12) { currentMonth = 1; currentYear++; }
  if (currentMonth < 1) { currentMonth = 12; currentYear--; }
  updateMonthLabel();
  const activePage = document.querySelector('.page.active')?.id;
  if (activePage === 'dashboard') loadDashboard();
  if (activePage === 'transactions') loadTransactionsPage();
  if (activePage === 'budget') loadBudgetPage();
}

function updateMonthLabel() {
  document.getElementById('monthLabel').textContent = `${MONTHS[currentMonth - 1]} ${currentYear}`;
}

async function get(url) {
  const res = await fetch(API + url);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
async function post(url, data) {
  const res = await fetch(API + url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
async function put(url, data) {
  const res = await fetch(API + url, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
async function del(url) {
  const res = await fetch(API + url, { method: 'DELETE' });
  if (!res.ok) throw new Error(await res.text());
}

async function loadCategories() {
  allCategories = await get('/api/categories');
  populateCategorySelects();
}

function populateCategorySelects() {
  const txnSel = document.getElementById('txnCategory');
  const type = document.getElementById('txnType').value;
  const filtered = allCategories.filter(c => c.type === type);
  txnSel.innerHTML = filtered.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

  const filterSel = document.getElementById('filterCategory');
  filterSel.innerHTML = `<option value="">All Categories</option>` +
    allCategories.map(c => `<option value="${c.name}">${c.name}</option>`).join('');
}

function renderCategories() {
  const grid = document.getElementById('categoriesGrid');
  grid.innerHTML = allCategories.map(c => `
    <div class="cat-card">
      <div class="cat-info">
        <div class="cat-icon" style="background:${c.color}22">${(c.name || '').slice(0, 1).toUpperCase()}</div>
        <div>
          <div class="cat-name">${c.name}</div>
          <div class="cat-type">${c.type === 'INCOME' ? '↑ Income' : '↓ Expense'}</div>
        </div>
      </div>
      <button class="btn-icon delete" onclick="deleteCategory(${c.id})">🗑️</button>
    </div>
  `).join('') || '<div class="empty-state">No categories yet</div>';
}

function bindCategories() {
  const addBtn = document.getElementById('addCategoryBtn');
  if (!addBtn) return;
  addBtn.addEventListener('click', async () => {
    const nameEl = document.getElementById('catName');
    const typeEl = document.getElementById('catType');
    const colorEl = document.getElementById('catColor');
    const name = nameEl ? nameEl.value.trim() : '';
    const type = typeEl ? typeEl.value : null;
    const color = colorEl ? colorEl.value : '';
    if (!name) return alert('Category name is required');
    if (!type) return alert('Category type is required');
    try {
      const payload = { name, type };
      if (color) payload.color = color;
      await post('/api/categories', payload);
      if (nameEl) nameEl.value = '';
      await loadCategories();
      renderCategories();
    } catch(e) { alert(e.message); }
  });
}

async function deleteCategory(id) {
  if (!confirm('Delete this category?')) return;
  try {
    await del(`/api/categories/${id}`);
    await loadCategories();
    renderCategories();
  } catch(e) { alert('Cannot delete: ' + e.message); }
}

async function loadDashboard() {
  updateMonthLabel();
  try {
    const [statusRes, txnsRes, expByCatRes] = await Promise.allSettled([
      get(`/api/budget/status?month=${currentMonth}&year=${currentYear}`),
      get(`/api/transactions?month=${currentMonth}&year=${currentYear}`),
      get(`/api/transactions/expenses-by-category?month=${currentMonth}&year=${currentYear}`)
    ]);

    const status = statusRes.status === 'fulfilled' ? statusRes.value : null;
    const txns = txnsRes.status === 'fulfilled' ? txnsRes.value : [];
    const expByCat = expByCatRes.status === 'fulfilled' ? expByCatRes.value : [];
    allTransactions = txns;

    const totals = status ? {
      income: status.totalIncome,
      expenses: status.totalExpenses,
      balance: status.balance
    } : txns.reduce((acc, t) => {
      if (t.type === 'INCOME') acc.income += t.amount || 0;
      if (t.type === 'EXPENSE') acc.expenses += t.amount || 0;
      acc.balance = acc.income - acc.expenses;
      return acc;
    }, { income: 0, expenses: 0, balance: 0 });

    document.getElementById('totalIncome').textContent = fmt(totals.income);
    document.getElementById('totalExpenses').textContent = fmt(totals.expenses);
    const bal = totals.balance;
    const balEl = document.getElementById('balance');
    balEl.textContent = fmt(bal);
    balEl.style.color = bal >= 0 ? 'var(--income)' : 'var(--expense)';

    if (status && status.hasBudget) {
      document.getElementById('budgetUsed').textContent = `${Math.round(status.percentageUsed)}%`;
      document.getElementById('budgetRemaining').textContent = `$${fmt(status.remaining)} remaining`;
      const prog = document.getElementById('budgetProgressSection');
      prog.style.display = 'block';
      document.getElementById('budgetProgressLabel').textContent = `${status.percentageUsed.toFixed(1)}% used of $${fmt(status.budgetLimit)}`;
      const bar = document.getElementById('budgetProgressBar');
      bar.style.width = status.percentageUsed + '%';
      bar.classList.toggle('danger', status.exceeded);
    } else {
      document.getElementById('budgetUsed').textContent = '—';
      document.getElementById('budgetRemaining').textContent = status ? 'No budget set' : 'Budget unavailable';
      document.getElementById('budgetProgressSection').style.display = 'none';
    }

    const banner = document.getElementById('alertBanner');
    if (status && status.alertMessage) {
      document.getElementById('alertText').textContent = status.alertMessage;
      banner.style.display = 'flex';
      banner.classList.toggle('danger', status.exceeded);
    } else {
      banner.style.display = 'none';
    }

    renderRecentTransactions(txns.slice(0, 6));
    renderPieChart(expByCat);
  } catch(e) { console.error(e); }
}

function renderRecentTransactions(txns) {
  const el = document.getElementById('recentTransactions');
  if (!txns.length) { el.innerHTML = '<div class="empty-state">No transactions this month</div>'; return; }
  el.innerHTML = txns.map(t => {
    const cat = allCategories.find(c => c.id === t.category?.id);
    const icon = cat?.name ? cat.name.slice(0, 1).toUpperCase() : (t.type === 'INCOME' ? 'I' : 'E');
    const color = cat?.color || (t.type === 'INCOME' ? '#22c55e' : '#ef4444');
    return `
      <div class="recent-item">
        <div class="recent-icon" style="background:${color}22">${icon}</div>
        <div class="recent-info">
          <div class="recent-desc">${esc(t.description)}</div>
          <div class="recent-cat">${t.category?.name || 'Uncategorized'}</div>
        </div>
        <div style="text-align:right">
          <div class="recent-amount ${t.type.toLowerCase()}">${t.type==='INCOME'?'+':'-'}${fmt(t.amount)}</div>
          <div class="recent-date">${formatDate(t.date)}</div>
        </div>
      </div>`;
  }).join('');
}

function renderPieChart(data) {
  const canvas = document.getElementById('pieChart');
  if (pieChart) { pieChart.destroy(); pieChart = null; }
  if (!data.length) {
    document.getElementById('chartLegend').innerHTML = '<div class="empty-state" style="padding:12px">No expense data</div>';
    return;
  }
  const labels = data.map(d => d[0]);
  const values = data.map(d => parseFloat(d[1]));
  const colors = data.map((_, i) => {
    const c = allCategories.find(cat => cat.name === _[0]);
    return c?.color || `hsl(${i * 40},65%,55%)`;
  });

  pieChart = new Chart(canvas, {
    type: 'doughnut',
    data: { labels, datasets: [{ data: values, backgroundColor: colors, borderWidth: 2, borderColor: '#fff' }] },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false }, tooltip: {
        callbacks: { label: ctx => ` ${ctx.label}: $${fmt(ctx.parsed)}` }
      }}
    }
  });

  document.getElementById('chartLegend').innerHTML = labels.map((l, i) =>
    `<div class="legend-item"><div class="legend-dot" style="background:${colors[i]}"></div>${l}</div>`
  ).join('');
}

async function loadTransactionsPage() {
  updateMonthLabel();
  try {
    allTransactions = await get(`/api/transactions?month=${currentMonth}&year=${currentYear}`);
    renderTransactionsTable(allTransactions);
  } catch(e) { console.error(e); }
}

function bindFilters() {
  ['filterType','filterCategory','filterSearch'].forEach(id => {
    document.getElementById(id).addEventListener('input', applyFilters);
  });
}

function applyFilters() {
  const type = document.getElementById('filterType').value;
  const cat = document.getElementById('filterCategory').value;
  const search = document.getElementById('filterSearch').value.toLowerCase();
  const filtered = allTransactions.filter(t =>
    (!type || t.type === type) &&
    (!cat || t.category?.name === cat) &&
    (!search || t.description.toLowerCase().includes(search))
  );
  renderTransactionsTable(filtered);
}

function renderTransactionsTable(txns) {
  const tbody = document.getElementById('transactionsBody');
  if (!txns.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No transactions found</td></tr>';
    return;
  }
  tbody.innerHTML = txns.map(t => `
    <tr>
      <td>${formatDate(t.date)}</td>
      <td>${esc(t.description)}${t.note ? `<br><small style="color:var(--text-sub)">${esc(t.note)}</small>` : ''}</td>
      <td>${t.category ? `<span>${t.category.name}</span>` : '—'}</td>
      <td><span class="badge ${t.type.toLowerCase()}">${t.type === 'INCOME' ? '↑ Income' : '↓ Expense'}</span></td>
      <td class="amount-col ${t.type.toLowerCase()}">${t.type==='INCOME'?'+':'-'}${fmt(t.amount)}</td>
      <td>
        <div class="action-btns">
          <button class="btn-icon" onclick="openEdit(${t.id})" title="Edit">✏️</button>
          <button class="btn-icon delete" onclick="deleteTransaction(${t.id})" title="Delete">🗑️</button>
        </div>
      </td>
    </tr>`).join('');
}

function bindModal() {
  document.getElementById('addTxnBtn').addEventListener('click', () => openModal());
  document.getElementById('closeModal').addEventListener('click', closeModal);
  document.getElementById('cancelModal').addEventListener('click', closeModal);
  document.getElementById('txnModal').addEventListener('click', e => { if (e.target.id === 'txnModal') closeModal(); });
  document.getElementById('saveTxnBtn').addEventListener('click', saveTxn);
  document.getElementById('txnType').addEventListener('change', populateCategorySelects);
}

function openModal() {
  document.getElementById('txnId').value = '';
  document.getElementById('modalTitle').textContent = 'Add Transaction';
  document.getElementById('txnDescription').value = '';
  document.getElementById('txnAmount').value = '';
  document.getElementById('txnNote').value = '';
  document.getElementById('txnType').value = 'EXPENSE';
  document.getElementById('txnDate').value = new Date().toISOString().split('T')[0];
  populateCategorySelects();
  document.getElementById('txnModal').classList.add('open');
}

function openEdit(id) {
  const t = allTransactions.find(x => x.id === id);
  if (!t) return;
  document.getElementById('txnId').value = t.id;
  document.getElementById('modalTitle').textContent = 'Edit Transaction';
  document.getElementById('txnDescription').value = t.description;
  document.getElementById('txnAmount').value = t.amount;
  document.getElementById('txnNote').value = t.note || '';
  document.getElementById('txnType').value = t.type;
  document.getElementById('txnDate').value = t.date;
  populateCategorySelects();
  if (t.category) document.getElementById('txnCategory').value = t.category.id;
  document.getElementById('txnModal').classList.add('open');
}

function closeModal() {
  document.getElementById('txnModal').classList.remove('open');
}

async function saveTxn() {
  const id = document.getElementById('txnId').value;
  const desc = document.getElementById('txnDescription').value.trim();
  const amount = parseFloat(document.getElementById('txnAmount').value);
  const type = document.getElementById('txnType').value;
  const date = document.getElementById('txnDate').value;
  const catId = document.getElementById('txnCategory').value;
  const note = document.getElementById('txnNote').value.trim();

  if (!desc) return alert('Description is required');
  if (!amount || amount <= 0) return alert('Enter a valid amount');
  if (!date) return alert('Date is required');

  const payload = { description: desc, amount, type, date, note, category: catId ? { id: parseInt(catId) } : null };
  try {
    if (id) { await put(`/api/transactions/${id}`, payload); }
    else { await post('/api/transactions', payload); }
    closeModal();
    const activePage = document.querySelector('.page.active')?.id;
    if (activePage === 'dashboard') await loadDashboard();
    else await loadTransactionsPage();
  } catch(e) { alert('Error: ' + e.message); }
}

async function deleteTransaction(id) {
  if (!confirm('Delete this transaction?')) return;
  try {
    await del(`/api/transactions/${id}`);
    const activePage = document.querySelector('.page.active')?.id;
    if (activePage === 'dashboard') await loadDashboard();
    else await loadTransactionsPage();
  } catch(e) { alert('Error: ' + e.message); }
}

async function loadBudgetPage() {
  updateMonthLabel();
  try {
    const status = await get(`/api/budget/status?month=${currentMonth}&year=${currentYear}`);
    if (status.hasBudget) {
      document.getElementById('budgetLimitInput').value = status.budgetLimit;
    } else {
      document.getElementById('budgetLimitInput').value = '';
    }
    renderBudgetStatus(status);
  } catch(e) { console.error(e); }
}

function renderBudgetStatus(s) {
  const el = document.getElementById('budgetStatusContent');
  if (!s.hasBudget) {
    el.innerHTML = '<div class="empty-state">No budget set for this month.<br>Set a limit on the left.</div>';
    return;
  }
  const pct = s.percentageUsed.toFixed(1);
  el.innerHTML = `
    <div class="stat-row"><span class="label">Budget Limit</span><span class="val">$${fmt(s.budgetLimit)}</span></div>
    <div class="stat-row"><span class="label">Total Spent</span><span class="val" style="color:var(--expense)">$${fmt(s.totalExpenses)}</span></div>
    <div class="stat-row"><span class="label">Total Income</span><span class="val" style="color:var(--income)">$${fmt(s.totalIncome)}</span></div>
    <div class="stat-row"><span class="label">Remaining Budget</span><span class="val" style="color:${s.exceeded?'var(--expense)':'var(--income)'}">$${fmt(s.remaining)}</span></div>
    <div class="stat-row"><span class="label">Balance</span><span class="val">$${fmt(s.balance)}</span></div>
    <div style="margin-top:16px">
      <div style="display:flex;justify-content:space-between;font-size:12px;color:var(--text-sub);margin-bottom:6px">
        <span>Budget used</span><span>${pct}%</span>
      </div>
      <div class="progress-bar-wrap">
        <div class="progress-bar ${s.exceeded?'danger':''}" style="width:${Math.min(s.percentageUsed,100)}%"></div>
      </div>
    </div>
    ${s.alertMessage ? `<div class="alert-banner ${s.exceeded?'danger':''}" style="margin-top:14px">
      <span class="alert-icon">!</span><span>${s.alertMessage}</span></div>` : ''}
  `;
}

function bindBudget() {
  document.getElementById('saveBudgetBtn').addEventListener('click', async () => {
    const limit = parseFloat(document.getElementById('budgetLimitInput').value);
    if (!limit || limit <= 0) return alert('Enter a valid budget limit');
    try {
      await post('/api/budget', { month: currentMonth, year: currentYear, budgetLimit: limit });
      await loadBudgetPage();
    } catch(e) { alert('Error: ' + e.message); }
  });
}

function fmt(n) { return (parseFloat(n)||0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function formatDate(d) {
  if (!d) return '';
  const parts = Array.isArray(d) ? d : d.split('-');
  return new Date(parts[0], parts[1]-1, parts[2]).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}
function esc(s) { const d = document.createElement('div'); d.textContent = s||''; return d.innerHTML; }

document.addEventListener('DOMContentLoaded', init);
