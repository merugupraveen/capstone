const quoteTextEl = document.getElementById('quoteText');
const quoteAuthorEl = document.getElementById('quoteAuthor');
const errorEl = document.getElementById('error');
const toastEl = document.getElementById('toast');

const categorySelectEl = document.getElementById('categorySelect');
const btnShow = document.getElementById('btnShow');
const btnCopy = document.getElementById('btnCopy');
const btnFavorite = document.getElementById('btnFavorite');

const historyListEl = document.getElementById('historyList');
const favoritesListEl = document.getElementById('favoritesList');
const historyEmptyEl = document.getElementById('historyEmpty');
const favoritesEmptyEl = document.getElementById('favoritesEmpty');

const STORAGE = {
  history: 'rq.history',
  favorites: 'rq.favorites',
  lastQuoteKey: 'rq.lastQuoteKey',
  selectedCategory: 'rq.selectedCategory'
};

const HISTORY_MAX = 50;

let state = {
  currentQuote: null,
  history: loadJson(STORAGE.history, []),
  favorites: loadJson(STORAGE.favorites, {}),
  lastQuoteKey: localStorage.getItem(STORAGE.lastQuoteKey) || null,
  selectedCategory: localStorage.getItem(STORAGE.selectedCategory) || 'all'
};

function loadJson(key, defaultValue) {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return defaultValue;
    return JSON.parse(raw);
  } catch (_) {
    return defaultValue;
  }
}

function saveJson(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (_) {
    // ignore (storage may be blocked)
  }
}

function showToast(message) {
  toastEl.textContent = message || '';
  if (!message) return;
  window.clearTimeout(showToast._t);
  showToast._t = window.setTimeout(() => {
    toastEl.textContent = '';
  }, 2500);
}

function formatQuoteForClipboard(q) {
  const author = q.author ? q.author : 'Unknown';
  return `"${q.text}" — ${author}`;
}

function renderQuote(q) {
  if (!q) {
    quoteTextEl.textContent = 'Click "Show Quote" to get a quote.';
    quoteAuthorEl.textContent = '';
    btnCopy.disabled = true;
    btnFavorite.disabled = true;
    btnFavorite.textContent = 'Favorite';
    return;
  }

  quoteTextEl.textContent = `"${q.text}"`;
  const author = q.author ? q.author : 'Unknown';
  quoteAuthorEl.textContent = `— ${author}`;

  btnCopy.disabled = false;
  btnFavorite.disabled = false;
  btnFavorite.textContent = isFavorited(q.key) ? 'Unfavorite' : 'Favorite';
}

function isFavorited(quoteKey) {
  return !!state.favorites?.[quoteKey];
}

function addToHistory(q) {
  if (!q) return;

  const next = [q, ...state.history];
  // avoid immediate duplicates in history
  const deduped = next.filter((item, idx) => idx === 0 || item.key !== next[idx - 1].key);
  state.history = deduped.slice(0, HISTORY_MAX);
  saveJson(STORAGE.history, state.history);
}

function renderHistory() {
  historyListEl.innerHTML = '';
  const items = state.history || [];
  historyEmptyEl.style.display = items.length ? 'none' : 'block';

  items.slice(0, 10).forEach((q) => {
    const li = document.createElement('li');
    li.className = 'listItem';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'listItemButton';
    btn.textContent = `"${q.text}"`;
    btn.addEventListener('click', () => {
      state.currentQuote = q;
      renderQuote(q);
    });

    const meta = document.createElement('div');
    meta.className = 'listItemMeta';
    meta.innerHTML = `<span>— ${q.author ? q.author : 'Unknown'}</span><span>${q.category ? q.category : ''}</span>`;

    li.appendChild(btn);
    li.appendChild(meta);
    historyListEl.appendChild(li);
  });
}

function renderFavorites() {
  favoritesListEl.innerHTML = '';
  const map = state.favorites || {};
  const items = Object.values(map);
  favoritesEmptyEl.style.display = items.length ? 'none' : 'block';

  items.forEach((q) => {
    const li = document.createElement('li');
    li.className = 'listItem';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'listItemButton';
    btn.textContent = `"${q.text}"`;
    btn.addEventListener('click', () => {
      state.currentQuote = q;
      renderQuote(q);
    });

    const meta = document.createElement('div');
    meta.className = 'listItemMeta';

    const left = document.createElement('span');
    left.textContent = `— ${q.author ? q.author : 'Unknown'}`;

    const unfav = document.createElement('button');
    unfav.type = 'button';
    unfav.className = 'btn';
    unfav.textContent = 'Unfavorite';
    unfav.addEventListener('click', (e) => {
      e.stopPropagation();
      delete state.favorites[q.key];
      saveJson(STORAGE.favorites, state.favorites);
      renderFavorites();
      renderQuote(state.currentQuote);
    });

    meta.appendChild(left);
    meta.appendChild(unfav);

    li.appendChild(btn);
    li.appendChild(meta);
    favoritesListEl.appendChild(li);
  });
}

async function loadCategories() {
  try {
    const res = await fetch('/api/quote/categories', { headers: { 'Accept': 'application/json' } });
    if (!res.ok) return;
    const categories = await res.json();

    const existing = new Set(Array.from(categorySelectEl.options).map(o => o.value));
    categories.forEach((c) => {
      const v = String(c);
      if (existing.has(v)) return;
      const opt = document.createElement('option');
      opt.value = v;
      opt.textContent = v;
      categorySelectEl.appendChild(opt);
    });

    categorySelectEl.value = state.selectedCategory || 'all';
  } catch (_) {
    // ignore
  }
}

async function loadQuote() {
  errorEl.textContent = '';
  btnShow.disabled = true;

  const category = categorySelectEl.value;

  try {
    const url = new URL('/api/quote/random', window.location.origin);
    if (category && category !== 'all') {
      url.searchParams.set('category', category);
    }
    if (state.lastQuoteKey) {
      url.searchParams.set('avoidQuoteKey', state.lastQuoteKey);
    }

    const res = await fetch(url.toString(), { headers: { 'Accept': 'application/json' } });
    if (!res.ok) {
      throw new Error(`Request failed: ${res.status}`);
    }
    const data = await res.json();

    const q = data.quote;
    if (!q) {
      showToast('No quotes available');
      return;
    }

    state.currentQuote = q;
    state.lastQuoteKey = q.key;
    localStorage.setItem(STORAGE.lastQuoteKey, q.key);

    renderQuote(q);
    addToHistory(q);
    renderHistory();

    if (data.usedFallback) {
      showToast('Online quotes are temporarily unavailable');
    }
  } catch (e) {
    errorEl.textContent = 'Could not load quote. Please try again.';
  } finally {
    btnShow.disabled = false;
  }
}

btnShow.addEventListener('click', loadQuote);

btnCopy.addEventListener('click', async () => {
  if (!state.currentQuote) return;
  try {
    await navigator.clipboard.writeText(formatQuoteForClipboard(state.currentQuote));
    showToast('Copied to clipboard');
  } catch (_) {
    showToast('Copy failed');
  }
});

btnFavorite.addEventListener('click', () => {
  const q = state.currentQuote;
  if (!q) return;

  if (isFavorited(q.key)) {
    delete state.favorites[q.key];
  } else {
    state.favorites[q.key] = q;
  }
  saveJson(STORAGE.favorites, state.favorites);

  renderFavorites();
  renderQuote(q);
});

categorySelectEl.addEventListener('change', () => {
  state.selectedCategory = categorySelectEl.value;
  try { localStorage.setItem(STORAGE.selectedCategory, state.selectedCategory); } catch (_) {}
});

// initial render
categorySelectEl.value = state.selectedCategory || 'all';
renderQuote(state.currentQuote);
renderHistory();
renderFavorites();
loadCategories();
