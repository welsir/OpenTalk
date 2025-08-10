// BroadcastChannel 封装（storage 事件作为后备）
class Channel {
    constructor(name) {
        this.name = name;
        this.bc = (window.BroadcastChannel) ? new BroadcastChannel(name) : null;
        this.listeners = new Set();
        if (this.bc) {
            this.bc.onmessage = e => this.listeners.forEach(fn => fn(e.data));
        } else {
            window.addEventListener('storage', (e) => {
                if (e.key === '__bc__' && e.newValue) {
                    try {
                        const msg = JSON.parse(e.newValue);
                        if (msg.name === this.name) this.listeners.forEach(fn => fn(msg.data));
                    } catch {}
                }
            });
        }
    }
    post(data) {
        if (this.bc) this.bc.postMessage(data);
        else localStorage.setItem('__bc__', JSON.stringify({ name: this.name, data, t: Date.now() }));
    }
    on(fn) { this.listeners.add(fn); }
    off(fn) { this.listeners.delete(fn); }
    close() { if (this.bc) this.bc.close(); this.listeners.clear(); }
}
export { Channel };