// Temporal & Multidimensional Sales DB - Frontend JavaScript

const API_BASE = '/api';
let allSalesCache = [];
let currentDbTable = 'fact_sales';

document.addEventListener('DOMContentLoaded', () => {
    loadAllSales();
    loadAnalytics();
    loadDimensionSuggestions();
    loadDbTable('fact_sales');
    setDefaultDates();
});

function setDefaultDates() {
    const today = new Date().toISOString().split('T')[0];
    const nowIso = new Date().toISOString().slice(0, 16);
    
    const saleDateEl = document.getElementById('formSaleDate');
    const validFromEl = document.getElementById('formValidFrom');
    if (saleDateEl) saleDateEl.value = today;
    if (validFromEl) validFromEl.value = nowIso;
}

// ================= Tab Navigation =================
function switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    const activeBtn = Array.from(document.querySelectorAll('.tab-btn'))
        .find(b => b.getAttribute('onclick')?.includes(tabId));
    if (activeBtn) activeBtn.classList.add('active');

    const activeTab = document.getElementById(tabId);
    if (activeTab) activeTab.classList.add('active');

    if (tabId === 'analytics-tab') {
        loadAnalytics();
    } else if (tabId === 'crud-tab') {
        loadAllSales();
    } else if (tabId === 'dbviewer-tab') {
        loadDbTable(currentDbTable);
    }
}

// ================= Sales CRUD Operations =================
async function loadAllSales() {
    const tbody = document.getElementById('salesTableBody');
    tbody.innerHTML = '<tr><td colspan="12" class="text-center py-4">Loading sales facts...</td></tr>';

    try {
        const res = await fetch(`${API_BASE}/sales`);
        if (!res.ok) throw new Error('Failed to load sales');
        const sales = await res.json();
        allSalesCache = sales;
        renderSalesTable(sales);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="12" class="text-center py-4 text-danger">Error loading sales: ${err.message}</td></tr>`;
        showToast('Error loading sales data', 'error');
    }
}

function renderSalesTable(sales) {
    const tbody = document.getElementById('salesTableBody');
    const countEl = document.getElementById('totalRecordsCount');
    if (countEl) countEl.innerText = `Total Records: ${sales.length}`;

    if (!sales || sales.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" class="text-center py-4">No sales records found. Click "+ Add New Sale" to create one.</td></tr>';
        return;
    }

    tbody.innerHTML = sales.map(s => {
        const statusBadge = getStatusBadge(s.status);
        const validFromStr = formatDateTime(s.validFrom);
        const validToStr = s.validTo ? formatDateTime(s.validTo) : 'Current (Active)';
        
        return `
            <tr>
                <td><strong>#${s.id}</strong></td>
                <td><strong>${escapeHtml(s.customerName || 'N/A')}</strong></td>
                <td>
                    <div><strong>${escapeHtml(s.productName || 'N/A')}</strong></div>
                    <small class="text-secondary">${escapeHtml(s.productCategory || '')} &bull; ${escapeHtml(s.productBrand || '')}</small>
                </td>
                <td>
                    <div>${escapeHtml(s.storeName || 'N/A')}</div>
                    <small class="text-secondary">${escapeHtml(s.city || '')}, ${escapeHtml(s.state || '')} (${escapeHtml(s.region || '')})</small>
                </td>
                <td>
                    <div>${s.saleDate || 'N/A'}</div>
                    <small class="text-secondary">Q${s.quarter || ''} ${s.year || ''} &bull; ${s.season || ''}</small>
                </td>
                <td>${s.quantity}</td>
                <td>$${(s.unitPrice || 0).toFixed(2)}</td>
                <td><strong style="color: #38bdf8;">$${(s.totalAmount || 0).toFixed(2)}</strong></td>
                <td><span class="temporal-date">${validFromStr}</span></td>
                <td><span class="temporal-date" style="color: ${s.validTo ? '#f59e0b' : '#34d399'};">${validToStr}</span></td>
                <td>${statusBadge}</td>
                <td class="text-right">
                    <div class="action-btns">
                        <button class="btn btn-secondary btn-sm" onclick="openEditSaleModal(${s.id}, false)" title="In-place Edit">Edit</button>
                        <button class="btn btn-outline btn-sm" onclick="openEditSaleModal(${s.id}, true)" title="Create New Temporal Revision">Revise</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteSale(${s.id})" title="Soft Cancel Sale">Delete</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function filterSalesTable() {
    const query = (document.getElementById('salesSearchInput').value || '').toLowerCase().trim();
    if (!query) {
        renderSalesTable(allSalesCache);
        return;
    }

    const filtered = allSalesCache.filter(s => {
        return (s.customerName && s.customerName.toLowerCase().includes(query)) ||
               (s.productName && s.productName.toLowerCase().includes(query)) ||
               (s.productCategory && s.productCategory.toLowerCase().includes(query)) ||
               (s.storeName && s.storeName.toLowerCase().includes(query)) ||
               (s.region && s.region.toLowerCase().includes(query)) ||
               (s.status && s.status.toLowerCase().includes(query)) ||
               (s.id && s.id.toString().includes(query));
    });

    renderSalesTable(filtered);
}

// ================= Modal Actions =================
let isRevisionMode = false;

function openNewSaleModal() {
    isRevisionMode = false;
    document.getElementById('modalTitle').innerText = 'Add New Sale';
    document.getElementById('saleForm').reset();
    document.getElementById('formSaleId').value = '';
    document.getElementById('saveSaleBtn').innerText = 'Save Sale';
    
    setDefaultDates();
    calculateFormTotal();
    document.getElementById('saleModal').classList.add('active');
}

async function openEditSaleModal(id, revision = false) {
    isRevisionMode = revision;
    try {
        const res = await fetch(`${API_BASE}/sales/${id}`);
        if (!res.ok) throw new Error('Could not fetch sale details');
        const s = await res.json();

        document.getElementById('formSaleId').value = s.id;
        document.getElementById('formCustomerName').value = s.customerName || '';
        document.getElementById('formProductName').value = s.productName || '';
        document.getElementById('formProductCategory').value = s.productCategory || 'Electronics';
        document.getElementById('formProductBrand').value = s.productBrand || '';
        document.getElementById('formStoreName').value = s.storeName || '';
        document.getElementById('formRegion').value = s.region || 'East';
        document.getElementById('formCity').value = s.city || '';
        document.getElementById('formUnitPrice').value = s.unitPrice || 0;
        document.getElementById('formQuantity').value = s.quantity || 1;
        document.getElementById('formDiscount').value = s.discount || 0;
        document.getElementById('formSaleDate').value = s.saleDate || '';
        
        if (s.validFrom) {
            document.getElementById('formValidFrom').value = s.validFrom.slice(0, 16);
        }
        if (s.validTo) {
            document.getElementById('formValidTo').value = s.validTo.slice(0, 16);
        } else {
            document.getElementById('formValidTo').value = '';
        }

        document.getElementById('formStatus').value = s.status || 'ACTIVE';

        if (revision) {
            document.getElementById('modalTitle').innerText = `Create Temporal Revision for Sale #${s.id}`;
            document.getElementById('saveSaleBtn').innerText = 'Save Revision';
        } else {
            document.getElementById('modalTitle').innerText = `Edit Sale #${s.id}`;
            document.getElementById('saveSaleBtn').innerText = 'Update Sale';
        }

        calculateFormTotal();
        document.getElementById('saleModal').classList.add('active');
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

function closeSaleModal() {
    document.getElementById('saleModal').classList.remove('active');
}

function calculateFormTotal() {
    const price = parseFloat(document.getElementById('formUnitPrice').value) || 0;
    const qty = parseInt(document.getElementById('formQuantity').value) || 1;
    const discount = parseFloat(document.getElementById('formDiscount').value) || 0;
    const total = Math.max(0, (price * qty) - discount);
    document.getElementById('formCalculatedTotal').innerText = `$${total.toFixed(2)}`;
}

async function handleSaleFormSubmit(e) {
    e.preventDefault();

    const saleId = document.getElementById('formSaleId').value;
    const payload = {
        customerName: document.getElementById('formCustomerName').value,
        productName: document.getElementById('formProductName').value,
        productCategory: document.getElementById('formProductCategory').value,
        productBrand: document.getElementById('formProductBrand').value,
        storeName: document.getElementById('formStoreName').value,
        region: document.getElementById('formRegion').value,
        city: document.getElementById('formCity').value,
        unitPrice: parseFloat(document.getElementById('formUnitPrice').value),
        quantity: parseInt(document.getElementById('formQuantity').value),
        discount: parseFloat(document.getElementById('formDiscount').value),
        saleDate: document.getElementById('formSaleDate').value || null,
        validFrom: document.getElementById('formValidFrom').value || null,
        validTo: document.getElementById('formValidTo').value || null,
        status: document.getElementById('formStatus').value
    };

    try {
        let res;
        if (saleId) {
            const url = `${API_BASE}/sales/${saleId}?createRevision=${isRevisionMode}`;
            res = await fetch(url, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            res = await fetch(`${API_BASE}/sales`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (!res.ok) throw new Error('Server returned error while saving sale');

        showToast(saleId ? (isRevisionMode ? 'Temporal revision created.' : 'Sale updated.') : 'Sale created successfully.', 'success');
        closeSaleModal();
        loadAllSales();
        loadAnalytics();
        loadDimensionSuggestions();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteSale(id) {
    if (!confirm(`Are you sure you want to mark Sale #${id} as Cancelled (Temporal soft delete)?`)) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/sales/${id}?soft=true`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete sale');
        showToast(`Sale #${id} cancelled.`, 'success');
        loadAllSales();
        loadAnalytics();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ================= Multidimensional Analytics =================
async function loadAnalytics() {
    try {
        const res = await fetch(`${API_BASE}/sales/summary`);
        if (!res.ok) return;
        const data = await res.json();

        // Metrics
        document.getElementById('metricTotalRevenue').innerText = `$${(data.totalRevenue || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('metricTotalUnits').innerText = (data.totalUnitsSold || 0).toLocaleString();
        document.getElementById('metricTotalTransactions').innerText = (data.totalSalesCount || 0).toLocaleString();

        // 1. Category Breakdown
        const catContainer = document.getElementById('categoryBreakdownContainer');
        const maxCatRev = Math.max(...(data.categoryBreakdown || []).map(c => c.totalRevenue), 1);
        catContainer.innerHTML = (data.categoryBreakdown || []).map(c => {
            const pct = Math.round((c.totalRevenue / maxCatRev) * 100);
            return `
                <div class="stat-row">
                    <div class="stat-label-flex">
                        <span><strong>${escapeHtml(c.category)}</strong> (${c.salesCount} sales &bull; ${c.totalUnits} units)</span>
                        <strong style="color: #38bdf8;">$${c.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: ${pct}%"></div>
                    </div>
                </div>
            `;
        }).join('') || '<p class="text-secondary">No category data.</p>';

        // 2. Region Breakdown
        const regContainer = document.getElementById('regionBreakdownContainer');
        const maxRegRev = Math.max(...(data.regionBreakdown || []).map(r => r.totalRevenue), 1);
        regContainer.innerHTML = (data.regionBreakdown || []).map(r => {
            const pct = Math.round((r.totalRevenue / maxRegRev) * 100);
            return `
                <div class="stat-row">
                    <div class="stat-label-flex">
                        <span><strong>${escapeHtml(r.region)} Region</strong> (${r.salesCount} sales &bull; ${r.totalUnits} units)</span>
                        <strong style="color: #34d399;">$${r.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: ${pct}%; background: linear-gradient(90deg, #34d399, #059669);"></div>
                    </div>
                </div>
            `;
        }).join('') || '<p class="text-secondary">No region data.</p>';

        // 3. Time Quarter Breakdown
        const tqContainer = document.getElementById('timeQuarterBreakdownContainer');
        const maxTqRev = Math.max(...(data.timeQuarterBreakdown || []).map(t => t.totalRevenue), 1);
        tqContainer.innerHTML = (data.timeQuarterBreakdown || []).map(t => {
            const pct = Math.round((t.totalRevenue / maxTqRev) * 100);
            return `
                <div class="stat-row">
                    <div class="stat-label-flex">
                        <span><strong>${t.year} Q${t.quarter}</strong> (${t.salesCount} sales)</span>
                        <strong style="color: #f59e0b;">$${t.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: ${pct}%; background: linear-gradient(90deg, #f59e0b, #d97706);"></div>
                    </div>
                </div>
            `;
        }).join('') || '<p class="text-secondary">No quarterly data.</p>';

        // 4. Month Breakdown
        const mContainer = document.getElementById('monthBreakdownContainer');
        const maxMRev = Math.max(...(data.monthBreakdown || []).map(m => m.totalRevenue), 1);
        mContainer.innerHTML = (data.monthBreakdown || []).map(m => {
            const pct = Math.round((m.totalRevenue / maxMRev) * 100);
            return `
                <div class="stat-row">
                    <div class="stat-label-flex">
                        <span><strong>${escapeHtml(m.monthName || '')} ${m.year}</strong> (${m.salesCount} txns)</span>
                        <strong style="color: #a78bfa;">$${m.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill" style="width: ${pct}%; background: linear-gradient(90deg, #a78bfa, #7c3aed);"></div>
                    </div>
                </div>
            `;
        }).join('') || '<p class="text-secondary">No monthly data.</p>';

    } catch (err) {
        console.error('Error fetching analytics:', err);
    }
}

// ================= Temporal Time Travel =================
async function querySalesAsOf() {
    const targetDate = document.getElementById('asOfDateInput').value;
    if (!targetDate) {
        showToast('Please select a date', 'error');
        return;
    }

    const tbody = document.getElementById('temporalTableBody');
    tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4">Reconstructing database state...</td></tr>';

    try {
        const res = await fetch(`${API_BASE}/sales/temporal/as-of?date=${targetDate}`);
        if (!res.ok) throw new Error('Query failed');
        const sales = await res.json();

        document.getElementById('temporalQueryTitle').innerText = `Active Sales as of Snapshot: ${targetDate}`;
        document.getElementById('temporalCountBadge').innerText = `${sales.length} Records Found`;

        if (!sales || sales.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4">No records were active at this point in time.</td></tr>';
            return;
        }

        tbody.innerHTML = sales.map(s => `
            <tr>
                <td><strong>#${s.id}</strong></td>
                <td><strong>${escapeHtml(s.customerName || 'N/A')}</strong></td>
                <td>${escapeHtml(s.productName || 'N/A')}</td>
                <td>${escapeHtml(s.region || 'N/A')}</td>
                <td><strong style="color: #38bdf8;">$${(s.totalAmount || 0).toFixed(2)}</strong></td>
                <td><span class="temporal-date">${formatDateTime(s.validFrom)}</span></td>
                <td><span class="temporal-date">${s.validTo ? formatDateTime(s.validTo) : 'Current'}</span></td>
                <td><span class="temporal-date">${formatDateTime(s.transactionTime)}</span></td>
                <td>${getStatusBadge(s.status)}</td>
            </tr>
        `).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Error: ${err.message}</td></tr>`;
    }
}

function resetToCurrentTime() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('asOfDateInput').value = today;
    querySalesAsOf();
}

// ================= Database Table Explorer =================
async function loadDbTable(tableName) {
    currentDbTable = tableName;
    
    // Update button states
    document.querySelectorAll('.table-tab-btn').forEach(btn => {
        if (btn.getAttribute('onclick')?.includes(tableName)) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    document.getElementById('currentTableName').innerText = `Table: ${tableName}`;
    const thead = document.getElementById('rawDbTableHead');
    const tbody = document.getElementById('rawDbTableBody');
    thead.innerHTML = '';
    tbody.innerHTML = '<tr><td class="text-center py-4">Fetching table rows...</td></tr>';

    try {
        const res = await fetch(`${API_BASE}/db-viewer/table/${tableName}`);
        if (!res.ok) throw new Error('Could not fetch table rows');
        const rows = await res.json();

        document.getElementById('tableRowCountBadge').innerText = `${rows.length} Rows`;

        if (!rows || rows.length === 0) {
            tbody.innerHTML = '<tr><td class="text-center py-4">Table is currently empty.</td></tr>';
            return;
        }

        // Generate columns dynamically
        const sample = rows[0];
        const keys = Object.keys(sample);

        thead.innerHTML = `<tr>${keys.map(k => `<th>${escapeHtml(k)}</th>`).join('')}</tr>`;

        tbody.innerHTML = rows.map(row => {
            return `<tr>${keys.map(k => {
                let val = row[k];
                if (val !== null && typeof val === 'object') {
                    val = JSON.stringify(val);
                }
                return `<td>${val !== null && val !== undefined ? escapeHtml(String(val)) : '<em style="color:#64748b">null</em>'}</td>`;
            }).join('')}</tr>`;
        }).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td class="text-center py-4 text-danger">Error: ${err.message}</td></tr>`;
    }
}

function reloadCurrentDbTable() {
    loadDbTable(currentDbTable);
}

// ================= Autocomplete & Lookups =================
async function loadDimensionSuggestions() {
    try {
        const [prodRes, storeRes] = await Promise.all([
            fetch(`${API_BASE}/dimensions/products`),
            fetch(`${API_BASE}/dimensions/stores`)
        ]);

        if (prodRes.ok) {
            const products = await prodRes.json();
            const prodDataList = document.getElementById('productSuggestions');
            if (prodDataList) {
                prodDataList.innerHTML = products.map(p => `<option value="${escapeHtml(p.productName)}">${p.category} - $${p.unitCost || 0}</option>`).join('');
            }
        }

        if (storeRes.ok) {
            const stores = await storeRes.json();
            const storeDataList = document.getElementById('storeSuggestions');
            if (storeDataList) {
                storeDataList.innerHTML = stores.map(s => `<option value="${escapeHtml(s.storeName)}">${s.city}, ${s.region}</option>`).join('');
            }
        }
    } catch (e) {
        console.warn('Could not load dimension suggestions', e);
    }
}

// ================= Utility Helpers =================
function getStatusBadge(status) {
    if (!status) return '<span class="badge badge-secondary">UNKNOWN</span>';
    const s = status.toUpperCase();
    if (s === 'ACTIVE') return '<span class="badge badge-active">ACTIVE</span>';
    if (s === 'REVISED') return '<span class="badge badge-revised">REVISED</span>';
    if (s === 'CANCELLED') return '<span class="badge badge-cancelled">CANCELLED</span>';
    return `<span class="badge badge-secondary">${escapeHtml(s)}</span>`;
}

function formatDateTime(dtStr) {
    if (!dtStr) return 'N/A';
    try {
        const d = new Date(dtStr);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
        return dtStr;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showToast(msg, type = 'info') {
    const toast = document.getElementById('toast');
    toast.innerText = msg;
    toast.style.borderColor = type === 'error' ? 'var(--danger)' : 'var(--accent)';
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
