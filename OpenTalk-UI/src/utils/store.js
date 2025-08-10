// 本地存储 + 用户/好友/消息 API（所有数据仅存于 localStorage）
const store = {
    get(key, def = null) { try { const v = localStorage.getItem(key); return v ? JSON.parse(v) : def; } catch { return def; } },
    set(key, val) { localStorage.setItem(key, JSON.stringify(val)); },
    del(key) { localStorage.removeItem(key); }
};

const text = {
    time(ts = Date.now()) {
        const d = new Date(ts); const p = n => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
    }
};

const uuid = () =>
    (crypto?.randomUUID?.() ??
        'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
            const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        }));

async function sha256(str) {
    const enc = new TextEncoder();
    const buf = await crypto.subtle.digest('SHA-256', enc.encode(str));
    return [...new Uint8Array(buf)].map(b => b.toString(16).padStart(2, '0')).join('');
}

const KEYS = {
    users: 'ot_users',
    current: 'ot_current',
    friends: u => `ot_friends:${u}`,
    profile: u => `ot_profile:${u}`,
    messages: pair => `ot_messages:${pair}`
};

function loadUsers() { return store.get(KEYS.users, {}); }
function saveUsers(obj) { store.set(KEYS.users, obj); }
function ensureArray(key) { const a = store.get(key, null); if (Array.isArray(a)) return a; store.set(key, []); return []; }
function sortPair(a, b) { return [a, b].sort().join('::'); }

const API = {
    async register(username, password, nickname = '') {
        username = (username || '').trim();
        const users = loadUsers();
        if (!/^[a-z0-9_]{3,16}$/.test(username)) throw new Error('用户名需为 3-16 位小写字母/数字/下划线');
        if (users[username]) throw new Error('该用户名已被注册');
        if (!password || password.length < 6) throw new Error('密码至少 6 位');
        const passHash = await sha256(password);
        const now = Date.now();
        users[username] = { username, passHash, createdAt: now, profile: { displayName: nickname || username, bio: '', color: '#0a84ff' } };
        saveUsers(users);
        store.set(KEYS.profile(username), users[username].profile);
        store.set(KEYS.friends(username), []);
        store.set(KEYS.current, { username });
        return users[username];
    },
    async login(username, password) {
        const users = loadUsers();
        if (!users[username]) throw new Error('用户不存在');
        const passHash = await sha256(password);
        if (users[username].passHash !== passHash) throw new Error('密码错误');
        store.set(KEYS.current, { username });
        return users[username];
    },
    logout() { store.del(KEYS.current); },
    getCurrent() { return store.get(KEYS.current, null); },

    getProfile(u) {
        const users = loadUsers();
        const base = users[u]?.profile || { displayName: u, bio: '', color: '#0a84ff' };
        const custom = store.get(KEYS.profile(u), null);
        return { ...base, ...(custom || {}) };
    },
    setProfile(u, data) {
        const users = loadUsers();
        if (users[u]) {
            users[u].profile = { ...users[u].profile, ...data };
            saveUsers(users);
        }
        store.set(KEYS.profile(u), { ...API.getProfile(u), ...data });
    },

    listUsers() { return Object.keys(loadUsers()); },
    exists(u) { return !!loadUsers()[u]; },

    listFriends(u) { return ensureArray(KEYS.friends(u)); },
    addFriend(u, v) {
        if (u === v) throw new Error('不能添加自己为好友');
        if (!API.exists(v)) throw new Error('用户不存在');
        const ua = ensureArray(KEYS.friends(u));
        const vb = ensureArray(KEYS.friends(v));
        if (!ua.includes(v)) ua.push(v);
        if (!vb.includes(u)) vb.push(u);
        store.set(KEYS.friends(u), ua);
        store.set(KEYS.friends(v), vb);
    },

    pairKey(a, b) { return sortPair(a, b); },
    getMessages(a, b) { return ensureArray(KEYS.messages(sortPair(a, b))); },
    pushMessage(a, b, msg) {
        const key = KEYS.messages(sortPair(a, b));
        const list = ensureArray(key);
        list.push(msg);
        store.set(key, list);
    }
};

export { store, text, uuid, sha256, KEYS, API };