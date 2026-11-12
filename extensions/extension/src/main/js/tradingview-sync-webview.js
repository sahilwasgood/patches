/**
 * TradingView Morphe Android Webview Sync Engine & Feature Suite
 * Seamless bidirectional sync with Cloudflare Worker & Desktop Extension
 */
(function () {
  if (window.__tvSyncExtensionInjected) return;
  window.__tvSyncExtensionInjected = true;

  const DEFAULT_WORKER_URL = "https://tradingview-sync-worker.pubgnewstatevpn99.workers.dev";
  const STORAGE_KEY_CONFIG = "morphe_tv_sync_config_v1";
  const STORAGE_KEY_WATCHLISTS = "morphe_tv_custom_watchlists_v1";

  // Load persistent configuration
  function loadConfig() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY_CONFIG);
      if (raw) return JSON.parse(raw);
    } catch (e) {
      console.warn("[TVSync] Failed to load config:", e);
    }
    return {
      workerUrl: DEFAULT_WORKER_URL,
      syncToken: "",
      autoSync: true,
      lastSyncTime: 0
    };
  }

  function saveConfig(cfg) {
    try {
      localStorage.setItem(STORAGE_KEY_CONFIG, JSON.stringify(cfg));
    } catch (e) {
      console.warn("[TVSync] Failed to save config:", e);
    }
  }

  function loadWatchlists() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY_WATCHLISTS);
      if (raw) return JSON.parse(raw);
    } catch (e) {
      console.warn("[TVSync] Failed to load watchlists:", e);
    }
    return [];
  }

  function saveWatchlists(list) {
    try {
      localStorage.setItem(STORAGE_KEY_WATCHLISTS, JSON.stringify(list));
    } catch (e) {
      console.warn("[TVSync] Failed to save watchlists:", e);
    }
  }

  let config = loadConfig();
  let watchlists = loadWatchlists();
  let isSyncing = false;

  // --- Multi-Chart Layouts Unlock Hook ---
  (function unlockMultiChart() {
    try {
      const origParse = JSON.parse;
      JSON.parse = function () {
        const plans = origParse.apply(this, arguments);
        if (plans && plans.free && plans.free.MULTIPLE_CHARTS) {
          let maxLimit = 8;
          for (const k in plans) {
            if (plans[k] && plans[k].MULTIPLE_CHARTS && plans[k].MULTIPLE_CHARTS.limit > maxLimit) {
              maxLimit = plans[k].MULTIPLE_CHARTS.limit;
            }
          }
          for (const k in plans) {
            if (plans[k] && plans[k].MULTIPLE_CHARTS) {
              plans[k].MULTIPLE_CHARTS.limit = maxLimit;
            }
          }
        }
        return plans;
      };
    } catch (e) {
      console.warn("[TVSync] Chart layout patch error:", e);
    }
  })();

  // --- Network Sync with Cloudflare Worker ---
  async function performSync(direction = "bidirectional") {
    if (!config.syncToken) {
      showToast("Please enter a Sync Token in settings");
      return;
    }

    if (isSyncing) return;
    isSyncing = true;
    updateSyncButtonState(true);

    const baseUrl = (config.workerUrl || DEFAULT_WORKER_URL).replace(/\/+$/, "");

    try {
      // 1. Fetch remote data
      const getRes = await fetch(`${baseUrl}/api/sync`, {
        headers: { "x-sync-token": config.syncToken }
      });

      if (!getRes.ok) throw new Error(`HTTP ${getRes.status}`);
      const remote = await getRes.json();
      const remoteTime = Number(remote.timestamp) || 0;

      // 2. Decide merge strategy
      if (
        (direction === "pull" || remoteTime > config.lastSyncTime) &&
        remote.watchlists &&
        Array.isArray(remote.watchlists) &&
        !remote.empty
      ) {
        watchlists = remote.watchlists;
        saveWatchlists(watchlists);
        config.lastSyncTime = remoteTime;
        saveConfig(config);
        renderDrawerWatchlists();
        showToast(`Pulled ${watchlists.length} watchlists from cloud!`);
      } else if (direction === "push" || direction === "bidirectional") {
        // Push local to remote
        const now = Date.now();
        const payload = {
          timestamp: now,
          watchlists: watchlists,
          client: "android-morphe-app"
        };

        const postRes = await fetch(`${baseUrl}/api/sync`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "x-sync-token": config.syncToken
          },
          body: JSON.stringify(payload)
        });

        if (!postRes.ok) throw new Error(`HTTP ${postRes.status}`);
        config.lastSyncTime = now;
        saveConfig(config);
        showToast("Watchlists synced to cloud!");
      }
    } catch (err) {
      console.error("[TVSync] Sync error:", err);
      showToast("Sync failed: " + (err.message || "Network error"));
    } finally {
      isSyncing = false;
      updateSyncButtonState(false);
      updateDrawerStatus();
    }
  }

  // --- Chart Symbol Dispatcher ---
  function loadSymbolOnChart(symbol) {
    try {
      // 1. If window.TradingView chart instance is exposed
      if (window.tvWidget && typeof window.tvWidget.chart === "function") {
        window.tvWidget.chart().setSymbol(symbol);
        showToast(`Loaded ${symbol}`);
        closeDrawer();
        return;
      }
      // 2. Try window.ChartApiInstance
      if (window.ChartApiInstance && typeof window.ChartApiInstance.setSymbol === "function") {
        window.ChartApiInstance.setSymbol(symbol);
        showToast(`Loaded ${symbol}`);
        closeDrawer();
        return;
      }
      // 3. Fallback: simulate symbol search input or dispatch custom event
      window.dispatchEvent(new CustomEvent("tv-morphe-symbol-select", { detail: { symbol } }));
      showToast(`Selected ${symbol}`);
      closeDrawer();
    } catch (e) {
      console.warn("[TVSync] Failed to load symbol:", e);
    }
  }

  // --- UI Injection ---
  function injectStyles() {
    if (document.getElementById("tv-morphe-sync-style")) return;
    const style = document.createElement("style");
    style.id = "tv-morphe-sync-style";
    style.textContent = `
      #tv-morphe-sync-fab {
        position: fixed;
        bottom: 80px;
        right: 16px;
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background: #1e222d;
        border: 2px solid #2962ff;
        color: #2962ff;
        box-shadow: 0 4px 16px rgba(0,0,0,0.6);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        z-index: 9999999;
        transition: transform 0.2s, background 0.2s, border-color 0.2s;
        user-select: none;
        -webkit-tap-highlight-color: transparent;
      }
      #tv-morphe-sync-fab:active {
        transform: scale(0.92);
      }
      #tv-morphe-sync-fab svg {
        width: 22px;
        height: 22px;
        fill: currentColor;
      }
      #tv-morphe-sync-fab.syncing svg {
        animation: tv-spin 1s linear infinite;
      }
      @keyframes tv-spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
      }
      #tv-morphe-sync-drawer-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(0,0,0,0.7);
        backdrop-filter: blur(4px);
        z-index: 99999998;
        display: none;
        opacity: 0;
        transition: opacity 0.25s ease;
      }
      #tv-morphe-sync-drawer {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        max-height: 82vh;
        background: #131722;
        border-top: 1px solid #2a2e39;
        border-radius: 16px 16px 0 0;
        box-shadow: 0 -8px 32px rgba(0,0,0,0.8);
        z-index: 99999999;
        display: flex;
        flex-direction: column;
        transform: translateY(100%);
        transition: transform 0.28s cubic-bezier(0.1, 0.9, 0.2, 1);
        color: #d1d4dc;
        font-family: -apple-system, BlinkMacSystemFont, 'Trebuchet MS', Roboto, Ubuntu, sans-serif;
      }
      #tv-morphe-sync-drawer.open {
        transform: translateY(0);
      }
      .tv-sync-header {
        padding: 16px 20px;
        border-bottom: 1px solid #2a2e39;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .tv-sync-title {
        font-size: 16px;
        font-weight: 700;
        color: #f0f3fa;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .tv-sync-close-btn {
        background: none;
        border: none;
        color: #787b86;
        font-size: 20px;
        cursor: pointer;
        padding: 4px 8px;
      }
      .tv-sync-body {
        padding: 16px 20px;
        overflow-y: auto;
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      .tv-sync-card {
        background: #1e222d;
        border-radius: 10px;
        padding: 12px 14px;
        border: 1px solid #2a2e39;
      }
      .tv-sync-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
      }
      .tv-sync-input {
        background: #131722;
        border: 1px solid #363a45;
        border-radius: 6px;
        color: #f0f3fa;
        padding: 8px 12px;
        font-size: 13px;
        width: 100%;
        box-sizing: border-box;
      }
      .tv-sync-input:focus {
        border-color: #2962ff;
        outline: none;
      }
      .tv-sync-btn {
        background: #2962ff;
        color: #ffffff;
        border: none;
        border-radius: 6px;
        padding: 9px 16px;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
      }
      .tv-sync-btn:active {
        opacity: 0.85;
      }
      .tv-sync-btn.secondary {
        background: #2a2e39;
        color: #d1d4dc;
      }
      .tv-sync-wl-item {
        margin-bottom: 12px;
        padding-bottom: 12px;
        border-bottom: 1px solid #2a2e39;
      }
      .tv-sync-wl-name {
        font-weight: 600;
        font-size: 14px;
        color: #f0f3fa;
        margin-bottom: 6px;
        display: flex;
        justify-content: space-between;
      }
      .tv-sync-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
      .tv-sync-chip {
        background: #2a2e39;
        color: #2962ff;
        font-weight: 600;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 12px;
        cursor: pointer;
      }
      .tv-sync-chip:active {
        background: #2962ff;
        color: #fff;
      }
      #tv-morphe-toast {
        position: fixed;
        bottom: 136px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(30, 34, 45, 0.95);
        color: #f0f3fa;
        border: 1px solid #2962ff;
        border-radius: 20px;
        padding: 8px 18px;
        font-size: 12px;
        font-weight: 600;
        box-shadow: 0 4px 20px rgba(0,0,0,0.7);
        z-index: 100000000;
        display: none;
        opacity: 0;
        transition: opacity 0.2s ease;
        pointer-events: none;
      }
    `;
    document.head.appendChild(style);
  }

  function showToast(text) {
    let toast = document.getElementById("tv-morphe-toast");
    if (!toast) {
      toast = document.createElement("div");
      toast.id = "tv-morphe-toast";
      document.body.appendChild(toast);
    }
    toast.textContent = text;
    toast.style.display = "block";
    setTimeout(() => { toast.style.opacity = "1"; }, 10);
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
      toast.style.opacity = "0";
      setTimeout(() => { toast.style.display = "none"; }, 200);
    }, 2400);
  }

  function updateSyncButtonState(syncing) {
    const fab = document.getElementById("tv-morphe-sync-fab");
    if (!fab) return;
    if (syncing) {
      fab.classList.add("syncing");
      fab.style.borderColor = "#00e5ff";
      fab.style.color = "#00e5ff";
    } else {
      fab.classList.remove("syncing");
      fab.style.borderColor = "#2962ff";
      fab.style.color = "#2962ff";
    }
  }

  function updateDrawerStatus() {
    const statusEl = document.getElementById("tv-sync-status-text");
    if (!statusEl) return;
    if (!config.syncToken) {
      statusEl.textContent = "Status: No Passkey configured";
      statusEl.style.color = "#787b86";
    } else if (config.lastSyncTime > 0) {
      const dt = new Date(config.lastSyncTime);
      statusEl.textContent = `Status: Synced at ${dt.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
      statusEl.style.color = "#00e5ff";
    } else {
      statusEl.textContent = "Status: Ready to sync";
      statusEl.style.color = "#26a69a";
    }
  }

  function renderDrawerWatchlists() {
    const container = document.getElementById("tv-sync-watchlists-container");
    if (!container) return;

    if (!watchlists || watchlists.length === 0) {
      container.innerHTML = `<div style="color: #787b86; font-size: 13px; text-align: center; padding: 12px 0;">No synced watchlists found. Tap 'Sync Now' or add on PC.</div>`;
      return;
    }

    container.innerHTML = watchlists.map((wl) => {
      const symbolsHtml = (wl.symbols || [])
        .map((s) => `<span class="tv-sync-chip" data-symbol="${s}">${s}</span>`)
        .join("");
      return `
        <div class="tv-sync-wl-item">
          <div class="tv-sync-wl-name">
            <span>${wl.name || "Watchlist"}</span>
            <span style="font-size: 11px; color: #787b86;">${(wl.symbols || []).length} items</span>
          </div>
          <div class="tv-sync-chips">${symbolsHtml || '<span style="color:#787b86;font-size:11px;">Empty</span>'}</div>
        </div>
      `;
    }).join("");

    container.querySelectorAll(".tv-sync-chip").forEach((chip) => {
      chip.addEventListener("click", () => {
        const sym = chip.getAttribute("data-symbol");
        if (sym) loadSymbolOnChart(sym);
      });
    });
  }

  function openDrawer() {
    const backdrop = document.getElementById("tv-morphe-sync-drawer-backdrop");
    const drawer = document.getElementById("tv-morphe-sync-drawer");
    if (!drawer || !backdrop) return;
    backdrop.style.display = "block";
    setTimeout(() => {
      backdrop.style.opacity = "1";
      drawer.classList.add("open");
    }, 10);
    updateDrawerStatus();
    renderDrawerWatchlists();
  }

  function closeDrawer() {
    const backdrop = document.getElementById("tv-morphe-sync-drawer-backdrop");
    const drawer = document.getElementById("tv-morphe-sync-drawer");
    if (!drawer || !backdrop) return;
    backdrop.style.opacity = "0";
    drawer.classList.remove("open");
    setTimeout(() => { backdrop.style.display = "none"; }, 250);
  }

  function buildUI() {
    injectStyles();

    // 1. Floating Action Button
    let fab = document.getElementById("tv-morphe-sync-fab");
    if (!fab) {
      fab = document.createElement("div");
      fab.id = "tv-morphe-sync-fab";
      fab.title = "Cloud Watchlist Sync";
      fab.innerHTML = `
        <svg viewBox="0 0 24 24">
          <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"/>
        </svg>
      `;
      document.body.appendChild(fab);
      fab.addEventListener("click", openDrawer);
    }

    // 2. Drawer Backdrop
    let backdrop = document.getElementById("tv-morphe-sync-drawer-backdrop");
    if (!backdrop) {
      backdrop = document.createElement("div");
      backdrop.id = "tv-morphe-sync-drawer-backdrop";
      document.body.appendChild(backdrop);
      backdrop.addEventListener("click", closeDrawer);
    }

    // 3. Slide-up Drawer
    let drawer = document.getElementById("tv-morphe-sync-drawer");
    if (!drawer) {
      drawer = document.createElement("div");
      drawer.id = "tv-morphe-sync-drawer";
      drawer.innerHTML = `
        <div class="tv-sync-header">
          <div class="tv-sync-title">
            <svg style="width:20px;height:20px;fill:#2962ff;" viewBox="0 0 24 24">
              <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM12 18l-4-4h2.5V10h3v4H16l-4 4z"/>
            </svg>
            Watchlist Cloud Sync
          </div>
          <button class="tv-sync-close-btn" id="tv-sync-close-btn">&times;</button>
        </div>
        <div class="tv-sync-body">
          <div class="tv-sync-card">
            <div id="tv-sync-status-text" style="font-size:12px; font-weight:600; margin-bottom:10px;">Status: Ready</div>
            <div class="tv-sync-row" style="margin-bottom:8px;">
              <input type="text" id="tv-sync-token-input" class="tv-sync-input" placeholder="Enter Sync Passkey (e.g. key_xxx)" value="${config.syncToken || ''}"/>
              <button class="tv-sync-btn" id="tv-sync-save-token-btn">Save</button>
            </div>
            <div class="tv-sync-row">
              <button class="tv-sync-btn" id="tv-sync-now-btn" style="flex:1;">Sync Now</button>
              <button class="tv-sync-btn secondary" id="tv-sync-pull-btn">Pull</button>
              <button class="tv-sync-btn secondary" id="tv-sync-push-btn">Push</button>
            </div>
          </div>

          <div class="tv-sync-card" style="flex:1;">
            <div style="font-size:13px; font-weight:700; color:#f0f3fa; margin-bottom:12px; display:flex; justify-content:space-between; align-items:center;">
              <span>Synced Watchlists</span>
              <span style="font-size:11px; color:#787b86;">Tap symbol to chart</span>
            </div>
            <div id="tv-sync-watchlists-container"></div>
          </div>
        </div>
      `;
      document.body.appendChild(drawer);

      // Event Listeners
      drawer.querySelector("#tv-sync-close-btn").addEventListener("click", closeDrawer);
      drawer.querySelector("#tv-sync-save-token-btn").addEventListener("click", () => {
        const input = drawer.querySelector("#tv-sync-token-input");
        config.syncToken = (input.value || "").trim();
        saveConfig(config);
        updateDrawerStatus();
        showToast("Passkey saved!");
        performSync("bidirectional");
      });
      drawer.querySelector("#tv-sync-now-btn").addEventListener("click", () => performSync("bidirectional"));
      drawer.querySelector("#tv-sync-pull-btn").addEventListener("click", () => performSync("pull"));
      drawer.querySelector("#tv-sync-push-btn").addEventListener("click", () => performSync("push"));
    }
  }

  // --- Auto-Sync and Initialization ---
  function init() {
    if (document.body) {
      buildUI();
      // Auto-pull on startup if token is set
      if (config.syncToken && config.autoSync) {
        setTimeout(() => performSync("pull"), 1200);
      }
    } else {
      document.addEventListener("DOMContentLoaded", init);
    }
  }

  init();

  // Periodic background pull (every 60s)
  setInterval(() => {
    if (config.syncToken && config.autoSync && !isSyncing) {
      performSync("pull");
    }
  }, 60000);

})();
