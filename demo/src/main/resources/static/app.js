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
    } else if (tabId === 'cube-tab') {
        initCubeTab();
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

// ================= OLAP Data Cube Explorer =================
let cubeMetadata = null;
let currentCubeHierarchy = 'QUARTER';
let activeDiceParams = null;

async function initCubeTab() {
    if (!cubeMetadata) {
        await loadCubeMetadata();
    }
    applyCubeQuery();
}

async function loadCubeMetadata() {
    try {
        const res = await fetch(`${API_BASE}/cube/metadata`);
        if (!res.ok) return;
        cubeMetadata = await res.json();
    } catch (err) {
        console.error('Error fetching cube metadata:', err);
    }
}

function onSliceDimChanged() {
    const dimSelect = document.getElementById('cubeSliceDimSelect');
    const valSelect = document.getElementById('cubeSliceValSelect');
    const selectedDim = dimSelect.value;

    valSelect.innerHTML = '';

    if (!selectedDim) {
        valSelect.disabled = true;
        valSelect.innerHTML = '<option value="">Select dimension first</option>';
        applyCubeQuery();
        return;
    }

    valSelect.disabled = false;
    valSelect.innerHTML = '<option value="">-- Choose Value to Slice --</option>';

    if (selectedDim === 'timeYear' && cubeMetadata?.years) {
        cubeMetadata.years.forEach(y => {
            valSelect.innerHTML += `<option value="${y}">Year ${y}</option>`;
        });
    } else if (selectedDim === 'region' && cubeMetadata?.regions) {
        cubeMetadata.regions.forEach(r => {
            valSelect.innerHTML += `<option value="${escapeHtml(r)}">${escapeHtml(r)} Region</option>`;
        });
    } else if (selectedDim === 'productCategory' && cubeMetadata?.categories) {
        cubeMetadata.categories.forEach(c => {
            valSelect.innerHTML += `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`;
        });
    }

    // Default to the first available value
    if (valSelect.options.length > 1) {
        valSelect.selectedIndex = 1;
    }
    applyCubeQuery();
}

function setTimeHierarchy(level) {
    currentCubeHierarchy = level;
    document.querySelectorAll('.time-hier-btn').forEach(btn => btn.classList.remove('active'));

    if (level === 'YEAR') {
        document.getElementById('hierYearBtn')?.classList.add('active');
    } else if (level === 'QUARTER') {
        document.getElementById('hierQuarterBtn')?.classList.add('active');
    } else if (level === 'MONTH') {
        document.getElementById('hierMonthBtn')?.classList.add('active');
    }

    applyCubeQuery();
}

function pivotCubeAxes() {
    const rowSelect = document.getElementById('cubeRowDimSelect');
    const colSelect = document.getElementById('cubeColDimSelect');
    const temp = rowSelect.value;
    rowSelect.value = colSelect.value;
    colSelect.value = temp;

    activeDiceParams = null;
    applyCubeQuery();
}

function resetCubeFilters() {
    document.getElementById('cubeRowDimSelect').value = 'productCategory';
    document.getElementById('cubeColDimSelect').value = 'timePeriod';
    document.getElementById('cubeMeasureSelect').value = 'REVENUE';
    document.getElementById('cubeSliceDimSelect').value = '';
    
    const sliceValSelect = document.getElementById('cubeSliceValSelect');
    sliceValSelect.innerHTML = '<option value="">Select dimension first</option>';
    sliceValSelect.disabled = true;

    setTimeHierarchy('QUARTER');
    activeDiceParams = null;
    applyCubeQuery();
}

function runCubePreset(preset) {
    activeDiceParams = null;
    const rowSelect = document.getElementById('cubeRowDimSelect');
    const colSelect = document.getElementById('cubeColDimSelect');
    const sliceDim = document.getElementById('cubeSliceDimSelect');
    const sliceVal = document.getElementById('cubeSliceValSelect');

    if (preset === 'standard') {
        resetCubeFilters();
        return;
    }

    if (preset === 'slice') {
        rowSelect.value = 'productCategory';
        colSelect.value = 'region';
        sliceDim.value = 'timeYear';
        onSliceDimChanged();
        sliceVal.value = '2025';
        applyCubeQuery();
        return;
    }

    if (preset === 'dice') {
        rowSelect.value = 'productCategory';
        colSelect.value = 'region';
        sliceDim.value = '';
        sliceVal.disabled = true;
        activeDiceParams = {
            diceCategories: ['Electronics', 'Fashion & Apparel'],
            diceRegions: ['East', 'West']
        };
        applyCubeQuery();
        return;
    }

    if (preset === 'pivot') {
        rowSelect.value = 'region';
        colSelect.value = 'productCategory';
        sliceDim.value = '';
        sliceVal.disabled = true;
        applyCubeQuery();
        return;
    }

    if (preset === 'drilldown') {
        rowSelect.value = 'productCategory';
        colSelect.value = 'timePeriod';
        setTimeHierarchy('MONTH');
        return;
    }

    if (preset === 'rollup') {
        rowSelect.value = 'productCategory';
        colSelect.value = 'timePeriod';
        setTimeHierarchy('YEAR');
        return;
    }
}

async function applyCubeQuery() {
    const rowDim = document.getElementById('cubeRowDimSelect').value;
    const colDim = document.getElementById('cubeColDimSelect');
    const measure = document.getElementById('cubeMeasureSelect').value;
    const sliceDim = document.getElementById('cubeSliceDimSelect').value;
    const sliceVal = document.getElementById('cubeSliceValSelect').value;

    const payload = {
        rowDimension: rowDim,
        colDimension: colDim ? colDim.value : 'timePeriod',
        timeHierarchy: currentCubeHierarchy,
        measure: measure,
        sliceDimension: sliceDim || null,
        sliceValue: sliceVal || null
    };

    if (activeDiceParams) {
        if (activeDiceParams.diceCategories) payload.diceCategories = activeDiceParams.diceCategories;
        if (activeDiceParams.diceRegions) payload.diceRegions = activeDiceParams.diceRegions;
        if (activeDiceParams.diceYears) payload.diceYears = activeDiceParams.diceYears;
    }

    const tableBody = document.getElementById('cubeMatrixBody');
    tableBody.innerHTML = '<tr><td colspan="10" class="text-center py-4">Computing multidimensional cube matrix...</td></tr>';

    try {
        const res = await fetch(`${API_BASE}/cube/query`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('Cube aggregation query failed');
        const data = await res.json();
        renderCubeMatrix(data);
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-danger">Error: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderCubeMatrix(data) {
    // 1. Update Operation Banner
    const opTitle = document.getElementById('cubeOpTitle');
    const opDesc = document.getElementById('cubeOpDesc');
    if (opTitle) opTitle.innerText = `Operation: ${data.operationTitle || 'Multidimensional View'}`;
    if (opDesc) opDesc.innerText = data.operationExplanation || '';

    // 2. Table Headers
    const thead = document.getElementById('cubeMatrixHead');
    const tbody = document.getElementById('cubeMatrixBody');
    const tfoot = document.getElementById('cubeMatrixFoot');
    const countBadge = document.getElementById('cubeCellCountBadge');

    const totalCells = (data.rowHeaders?.length || 0) * (data.colHeaders?.length || 0);
    if (countBadge) countBadge.innerText = `${totalCells} Matrix Cells (${data.rowHeaders?.length || 0} x ${data.colHeaders?.length || 0})`;

    const formatVal = (num) => {
        if (num === null || num === undefined) return '-';
        if (data.measure === 'REVENUE') {
            return '$' + num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }
        return num.toLocaleString('en-US');
    };

    // Header Row
    const rowDimLabel = getDimensionLabel(data.rowDimension);
    const colDimLabel = getDimensionLabel(data.colDimension);

    let headHtml = `<tr><th class="axis-corner">${escapeHtml(rowDimLabel)} &darr; &bull; ${escapeHtml(colDimLabel)} &rarr;</th>`;
    (data.colHeaders || []).forEach(col => {
        headHtml += `<th class="text-right">${escapeHtml(col)}</th>`;
    });
    headHtml += `<th class="text-right" style="background-color: #1a2e3b; color: #34d399;">Row Total</th></tr>`;
    thead.innerHTML = headHtml;

    // Body Rows
    if (!data.rowHeaders || data.rowHeaders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="${(data.colHeaders?.length || 0) + 2}" class="text-center py-4">No records found for this slice/dice configuration.</td></tr>`;
        tfoot.innerHTML = '';
        return;
    }

    let bodyHtml = '';
    data.rowHeaders.forEach((rowName, rIdx) => {
        bodyHtml += `<tr><td><strong>${escapeHtml(rowName)}</strong></td>`;
        (data.colHeaders || []).forEach((_, cIdx) => {
            const val = data.matrix[rIdx][cIdx];
            const activeClass = val > 0 ? 'active-val' : '';
            bodyHtml += `<td class="cube-cell ${activeClass}">${formatVal(val)}</td>`;
        });
        // Row Total
        bodyHtml += `<td class="cube-total-cell">${formatVal(data.rowTotals[rIdx])}</td></tr>`;
    });
    tbody.innerHTML = bodyHtml;

    // Footer (Column Totals & Grand Total)
    let footHtml = `<tr><th>Column Total</th>`;
    (data.colTotals || []).forEach(colTot => {
        footHtml += `<td class="cube-total-cell">${formatVal(colTot)}</td>`;
    });
    footHtml += `<td class="cube-grand-total">${formatVal(data.grandTotal)}</td></tr>`;
    tfoot.innerHTML = footHtml;
}

function getDimensionLabel(dimKey) {
    if (!dimKey) return 'Dimension';
    switch (dimKey.toLowerCase()) {
        case 'productcategory': return 'Product Category';
        case 'region': return 'Region';
        case 'timeperiod': return 'Time Period';
        case 'productbrand': return 'Product Brand';
        case 'storeName': return 'Store Name';
        default: return dimKey;
    }
}

// ================= 3D Physical Data Cube Engine =================
let cubeCanvas = null;
let cubeCtx = null;
let cubeRotX = -0.45;
let cubeRotY = 0.70;
let isDraggingCube = false;
let lastMouseX = 0;
let lastMouseY = 0;
let cubeAutoSpin = true;
let cubeAnimFrame = null;
let hoveredVoxel = null;

// The 3 Real Data Dimensions defining the Physical Cube coordinates
const CUBE_DIM_X = ['Electronics', 'Fashion & Apparel', 'Home & Living', 'Sports & Outdoors']; // Product
const CUBE_DIM_Y = ['East', 'West', 'North', 'South'];                                        // Region
const CUBE_DIM_Z = ['2025 Q1', '2025 Q2', '2025 Q3', '2025 Q4'];                              // Time

// Known realistic cell weights for lighting up cells
const CUBE_CELL_DATA = {
    '0,0,0': { val: 4399.97, qty: 5 }, // Electronics, East, Q1
    '0,1,0': { val: 538.00,  qty: 2 }, // Electronics, West, Q1
    '0,2,1': { val: 2548.00, qty: 3 }, // Electronics, North, Q2
    '0,0,4': { val: 3228.98, qty: 4 }, // Electronics, East, 2026 Q1
    '1,3,0': { val: 690.00,  qty: 1 }, // Fashion, South, Q1
    '1,2,3': { val: 432.50,  qty: 2 }, // Fashion, North, Q4
    '2,0,0': { val: 2400.00, qty: 4 }, // Home, East, Q1
    '2,0,3': { val: 860.00,  qty: 2 }, // Home, East, Q4
    '2,1,2': { val: 1299.00, qty: 1 }, // Home, West, Q3
    '3,1,3': { val: 1875.00, qty: 3 }, // Sports, West, Q4
};

function init3dCube() {
    cubeCanvas = document.getElementById('cube3dCanvas');
    if (!cubeCanvas) return;
    cubeCtx = cubeCanvas.getContext('2d');

    // Attach mouse event listeners for rotation & inspection
    cubeCanvas.addEventListener('mousedown', (e) => {
        isDraggingCube = true;
        lastMouseX = e.clientX;
        lastMouseY = e.clientY;
        cubeAutoSpin = false;
        updateSpinButtonState();
    });

    window.addEventListener('mousemove', (e) => {
        if (isDraggingCube) {
            const dx = e.clientX - lastMouseX;
            const dy = e.clientY - lastMouseY;
            cubeRotY += dx * 0.008;
            cubeRotX += dy * 0.008;
            cubeRotX = Math.max(-1.4, Math.min(1.4, cubeRotX));
            lastMouseX = e.clientX;
            lastMouseY = e.clientY;
        } else if (cubeCanvas) {
            const rect = cubeCanvas.getBoundingClientRect();
            if (e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom) {
                const canvasX = (e.clientX - rect.left) * (cubeCanvas.width / rect.width);
                const canvasY = (e.clientY - rect.top) * (cubeCanvas.height / rect.height);
                checkVoxelHover(canvasX, canvasY, e.clientX, e.clientY);
            } else {
                hideCubeTooltip();
            }
        }
    });

    window.addEventListener('mouseup', () => {
        isDraggingCube = false;
    });

    startCubeAnimation();
}

function updateSpinButtonState() {
    const btn = document.getElementById('cubeSpinToggleBtn');
    if (btn) btn.innerText = cubeAutoSpin ? 'Pause Spin' : 'Resume Spin';
}

function toggleCubeAutoSpin() {
    cubeAutoSpin = !cubeAutoSpin;
    updateSpinButtonState();
}

function resetCube3dRotation() {
    cubeRotX = -0.45;
    cubeRotY = 0.70;
    cubeAutoSpin = false;
    updateSpinButtonState();
}

function startCubeAnimation() {
    if (cubeAnimFrame) cancelAnimationFrame(cubeAnimFrame);

    function loop() {
        if (cubeAutoSpin) {
            cubeRotY += 0.005;
        }
        render3dCube();
        cubeAnimFrame = requestAnimationFrame(loop);
    }
    loop();
}

function render3dCube() {
    if (!cubeCtx || !cubeCanvas) return;
    const w = cubeCanvas.width;
    const h = cubeCanvas.height;
    cubeCtx.clearRect(0, 0, w, h);

    const centerX = w / 2;
    const centerY = h / 2 + 10;
    const boxSize = 28;
    const spacing = 46;

    const sliceDim = document.getElementById('cubeSliceDimSelect')?.value || '';
    const sliceVal = document.getElementById('cubeSliceValSelect')?.value || '';

    // Check if Dice is active
    const isDice = !!(activeDiceParams && (activeDiceParams.diceCategories || activeDiceParams.diceRegions));

    // Project and collect all voxels
    const voxels = [];
    const nx = CUBE_DIM_X.length;
    const ny = CUBE_DIM_Y.length;
    const nz = CUBE_DIM_Z.length;

    for (let ix = 0; ix < nx; ix++) {
        for (let iy = 0; iy < ny; iy++) {
            for (let iz = 0; iz < nz; iz++) {
                // Determine slice / dice membership
                let isHighlighted = false;
                let isDimmed = false;
                let offsetX = 0, offsetY = 0, offsetZ = 0;

                const prodCat = CUBE_DIM_X[ix];
                const region = CUBE_DIM_Y[iy];
                const timeQ = CUBE_DIM_Z[iz];

                if (sliceDim === 'timeYear' && sliceVal === '2025') {
                    if (timeQ.includes('2025')) {
                        isHighlighted = true;
                        offsetZ = -14; // Explode slice outward visually!
                    } else {
                        isDimmed = true;
                    }
                } else if (sliceDim === 'region' && sliceVal) {
                    if (region.toLowerCase() === sliceVal.toLowerCase()) {
                        isHighlighted = true;
                        offsetY = -14;
                    } else {
                        isDimmed = true;
                    }
                } else if (sliceDim === 'productCategory' && sliceVal) {
                    if (prodCat.toLowerCase() === sliceVal.toLowerCase()) {
                        isHighlighted = true;
                        offsetX = -14;
                    } else {
                        isDimmed = true;
                    }
                } else if (isDice) {
                    const matchCat = !activeDiceParams.diceCategories || activeDiceParams.diceCategories.includes(prodCat);
                    const matchReg = !activeDiceParams.diceRegions || activeDiceParams.diceRegions.includes(region);
                    if (matchCat && matchReg) {
                        isHighlighted = true;
                    } else {
                        isDimmed = true;
                    }
                }

                // Center coordinates
                const cx3d = (ix - (nx - 1) / 2) * spacing + offsetX;
                const cy3d = (iy - (ny - 1) / 2) * spacing + offsetY;
                const cz3d = (iz - (nz - 1) / 2) * spacing + offsetZ;

                // 3D rotation math (Yaw then Pitch)
                const rot = rotate3d(cx3d, cy3d, cz3d, cubeRotX, cubeRotY);
                const proj = project(rot.x, rot.y, rot.z, centerX, centerY);

                const key = `${ix},${iy},${iz}`;
                const data = CUBE_CELL_DATA[key] || null;

                voxels.push({
                    ix, iy, iz,
                    prodCat, region, timeQ,
                    depth: rot.z,
                    screenX: proj.x,
                    screenY: proj.y,
                    scale: proj.scale,
                    boxSize: boxSize * proj.scale,
                    isHighlighted,
                    isDimmed,
                    data
                });
            }
        }
    }

    // Sort voxels back-to-front (Painter's algorithm)
    voxels.sort((a, b) => b.depth - a.depth);

    // Draw 3D coordinate axes behind the cube
    drawCubeAxes(centerX, centerY, nx, ny, nz, spacing);

    // Draw each 3D voxel block
    voxels.forEach(v => {
        drawVoxel(cubeCtx, v, hoveredVoxel === v);
    });

    // Save for hover hit-testing
    currentRenderedVoxels = voxels;
}

let currentRenderedVoxels = [];

function rotate3d(x, y, z, pitch, yaw) {
    // Rotate around Y-axis (yaw)
    const cosY = Math.cos(yaw);
    const sinY = Math.sin(yaw);
    const x1 = x * cosY + z * sinY;
    const z1 = -x * sinY + z * cosY;

    // Rotate around X-axis (pitch)
    const cosX = Math.cos(pitch);
    const sinX = Math.sin(pitch);
    const y1 = y * cosX - z1 * sinX;
    const z2 = y * sinX + z1 * cosX;

    return { x: x1, y: y1, z: z2 };
}

function project(x, y, z, cx, cy) {
    const focalLength = 550;
    const scale = focalLength / (focalLength + z);
    return {
        x: cx + x * scale,
        y: cy + y * scale,
        scale: Math.max(0.4, scale)
    };
}

function drawVoxel(ctx, v, isHovered) {
    const s = v.boxSize / 2;
    const x = v.screenX;
    const y = v.screenY;

    let baseColor = [30, 41, 59]; // Dark slate default
    let borderColor = 'rgba(71, 85, 105, 0.4)';
    let alpha = 0.85;

    if (v.isHighlighted) {
        baseColor = [245, 158, 11]; // Golden amber for Slice/Dice selection
        borderColor = '#fbbf24';
        alpha = 0.95;
    } else if (v.data) {
        baseColor = [56, 189, 248]; // Cyan glowing for cells with real sales
        borderColor = '#38bdf8';
        alpha = 0.90;
    } else if (v.isDimmed) {
        alpha = 0.15; // Transparent dimmed for non-slice blocks
    }

    if (isHovered) {
        borderColor = '#ffffff';
        baseColor = [99, 102, 241]; // Indigo highlight on hover
        alpha = 1.0;
    }

    // Top Face
    ctx.fillStyle = `rgba(${Math.min(255, baseColor[0] + 40)}, ${Math.min(255, baseColor[1] + 40)}, ${Math.min(255, baseColor[2] + 40)}, ${alpha})`;
    ctx.strokeStyle = borderColor;
    ctx.lineWidth = isHovered ? 2 : 1;

    ctx.beginPath();
    ctx.moveTo(x, y - s * 1.1);
    ctx.lineTo(x + s * 1.0, y - s * 0.5);
    ctx.lineTo(x, y);
    ctx.lineTo(x - s * 1.0, y - s * 0.5);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Left Face
    ctx.fillStyle = `rgba(${baseColor[0]}, ${baseColor[1]}, ${baseColor[2]}, ${alpha})`;
    ctx.beginPath();
    ctx.moveTo(x - s * 1.0, y - s * 0.5);
    ctx.lineTo(x, y);
    ctx.lineTo(x, y + s * 1.1);
    ctx.lineTo(x - s * 1.0, y + s * 0.6);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Right Face
    ctx.fillStyle = `rgba(${Math.max(0, baseColor[0] - 25)}, ${Math.max(0, baseColor[1] - 25)}, ${Math.max(0, baseColor[2] - 25)}, ${alpha})`;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + s * 1.0, y - s * 0.5);
    ctx.lineTo(x + s * 1.0, y + s * 0.6);
    ctx.lineTo(x, y + s * 1.1);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
}

function drawCubeAxes(cx, cy, nx, ny, nz, spacing) {
    if (!cubeCtx) return;
    const len = 120;
    const origin = rotate3d(0, 0, 0, cubeRotX, cubeRotY);
    const p0 = project(origin.x, origin.y, origin.z, cx, cy);

    // Axis X (Product) - Cyan
    const pX = rotate3d(len, 0, 0, cubeRotX, cubeRotY);
    const pXProj = project(pX.x, pX.y, pX.z, cx, cy);
    drawAxisLine(cubeCtx, p0, pXProj, '#38bdf8', 'X: Product Category');

    // Axis Y (Region / Depth) - Indigo
    const pY = rotate3d(0, len, 0, cubeRotX, cubeRotY);
    const pYProj = project(pY.x, pY.y, pY.z, cx, cy);
    drawAxisLine(cubeCtx, p0, pYProj, '#818cf8', 'Y: Geography (Region)');

    // Axis Z (Time / Vertical) - Emerald Green
    const pZ = rotate3d(0, 0, -len, cubeRotX, cubeRotY);
    const pZProj = project(pZ.x, pZ.y, pZ.z, cx, cy);
    drawAxisLine(cubeCtx, p0, pZProj, '#34d399', 'Z: Time Dimension');
}

function drawAxisLine(ctx, from, to, color, label) {
    ctx.save();
    ctx.strokeStyle = color;
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    ctx.moveTo(from.x, from.y);
    ctx.lineTo(to.x, to.y);
    ctx.stroke();

    // Arrowhead
    const angle = Math.atan2(to.y - from.y, to.x - from.x);
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.moveTo(to.x, to.y);
    ctx.lineTo(to.x - 10 * Math.cos(angle - Math.PI / 6), to.y - 10 * Math.sin(angle - Math.PI / 6));
    ctx.lineTo(to.x - 10 * Math.cos(angle + Math.PI / 6), to.y - 10 * Math.sin(angle + Math.PI / 6));
    ctx.closePath();
    ctx.fill();

    // Label
    ctx.font = 'bold 11px Inter, sans-serif';
    ctx.fillStyle = color;
    ctx.fillText(label, to.x + 8, to.y - 4);
    ctx.restore();
}

function checkVoxelHover(canvasX, canvasY, clientX, clientY) {
    let nearest = null;
    let minDist = 20;

    for (let i = 0; i < currentRenderedVoxels.length; i++) {
        const v = currentRenderedVoxels[i];
        const dist = Math.hypot(canvasX - v.screenX, canvasY - v.screenY);
        if (dist < minDist) {
            minDist = dist;
            nearest = v;
        }
    }

    if (nearest !== hoveredVoxel) {
        hoveredVoxel = nearest;
        if (hoveredVoxel) {
            showCubeTooltip(hoveredVoxel, clientX, clientY);
        } else {
            hideCubeTooltip();
        }
    }
}

function showCubeTooltip(v, clientX, clientY) {
    const tooltip = document.getElementById('cube3dTooltip');
    if (!tooltip) return;

    const valStr = v.data ? `$${v.data.val.toLocaleString('en-US', { minimumFractionDigits: 2 })} (${v.data.qty} units)` : 'No sales records';

    tooltip.innerHTML = `
        <div style="font-weight: 700; color: #38bdf8; margin-bottom: 4px;">Cell Coordinate (X, Y, Z)</div>
        <div><strong>Product:</strong> ${escapeHtml(v.prodCat)}</div>
        <div><strong>Region:</strong> ${escapeHtml(v.region)}</div>
        <div><strong>Time:</strong> ${escapeHtml(v.timeQ)}</div>
        <div style="margin-top: 6px; padding-top: 4px; border-top: 1px solid rgba(255,255,255,0.1); color: #34d399; font-weight: 700;">
            Revenue: ${valStr}
        </div>
    `;

    tooltip.style.display = 'block';
    const wrapper = document.querySelector('.cube-canvas-wrapper');
    if (wrapper) {
        const rect = wrapper.getBoundingClientRect();
        tooltip.style.left = `${clientX - rect.left + 15}px`;
        tooltip.style.top = `${clientY - rect.top + 15}px`;
    }
}

function hideCubeTooltip() {
    const tooltip = document.getElementById('cube3dTooltip');
    if (tooltip) tooltip.style.display = 'none';
}

// Hook into initCubeTab
const origInitCubeTab = initCubeTab;
initCubeTab = async function() {
    await origInitCubeTab();
    if (!cubeCanvas) {
        init3dCube();
    }
};


