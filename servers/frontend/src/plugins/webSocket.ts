/**
 * ============================================================
 * WebSocket Plugin - Global Event Subscription Service
 * ============================================================
 * 
 * This plugin provides a global singleton WebSocket connection
 * that can be used by any store to subscribe to real-time events.
 * 
 * The connection is established on app mount and maintained 
 * throughout the application lifecycle.
 * 
 * Usage in stores:
 * ```typescript
 * import { webSocketService } from '@/plugins/webSocket';
 * 
 * // Subscribe when store is created
 * webSocketService.subscribe('SUPPLY_REQUEST_UPDATED', (event) => {
 *   refreshData();
 * });
 * ```
 */

// ============================================================
// Type Definitions
// ============================================================

export type EventType =
    | 'SUPPLY_REQUEST_UPDATED'
    | 'BAR_STOCK_UPDATED'
    | 'WAREHOUSE_STOCK_UPDATED'
    | 'DROPPOINT_STATUS_CHANGED'
    | 'EVENT_UPDATED'
    | 'HEARTBEAT';

export interface NextBarEvent {
    type: EventType;
    source: string;
    resourceId: string | null;
    payload: Record<string, unknown>;
    timestamp: string;
}

export type EventCallback = (event: NextBarEvent) => void;

// ============================================================
// WebSocket Service Singleton
// ============================================================

class WebSocketService {
    private socket: WebSocket | null = null;
    private connected = false;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 10;
    private reconnectDelay = 5000;
    private reconnectTimeout: ReturnType<typeof setTimeout> | null = null;
    private listeners = new Map<EventType | '*', EventCallback[]>();
    private wsUrl = 'ws://localhost:8080/ws/events';

    /**
     * Initialize the WebSocket connection
     * Called once when the app mounts
     */
    connect(): void {
        if (this.socket?.readyState === WebSocket.OPEN) {
            console.log('[WebSocketService] Already connected');
            return;
        }

        console.log('[WebSocketService] Connecting to', this.wsUrl);

        try {
            this.socket = new WebSocket(this.wsUrl);

            this.socket.onopen = () => {
                console.log('[WebSocketService] ✅ Connected');
                this.connected = true;
                this.reconnectAttempts = 0;
            };

            this.socket.onmessage = (event) => {
                try {
                    const data: NextBarEvent = JSON.parse(event.data);
                    if (data.type !== 'HEARTBEAT') {
                        console.log('[WebSocketService] 📨 Event:', data.type);
                    }
                    this.dispatchEvent(data);
                } catch (e) {
                    console.error('[WebSocketService] Parse error:', e);
                }
            };

            this.socket.onclose = (event) => {
                console.log('[WebSocketService] 🔌 Disconnected:', event.code);
                this.connected = false;
                if (!event.wasClean) {
                    this.scheduleReconnect();
                }
            };

            this.socket.onerror = () => {
                console.error('[WebSocketService] ❌ Connection error');
                this.connected = false;
            };

        } catch (e) {
            console.error('[WebSocketService] Failed to connect:', e);
            this.scheduleReconnect();
        }
    }

    /**
     * Schedule automatic reconnection
     */
    private scheduleReconnect(): void {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.log('[WebSocketService] Max reconnect attempts reached');
            return;
        }

        this.reconnectAttempts++;
        console.log(`[WebSocketService] Reconnecting in ${this.reconnectDelay}ms...`);

        this.reconnectTimeout = setTimeout(() => {
            this.connect();
        }, this.reconnectDelay);
    }

    /**
     * Dispatch event to registered listeners
     */
    private dispatchEvent(event: NextBarEvent): void {
        console.log(`[WebSocketService] 🔄 Dispatching event: ${event.type} to ${this.listeners.size} listener types`);

        // Type-specific listeners
        const typeListeners = this.listeners.get(event.type);
        console.log(`[WebSocketService] Found ${typeListeners?.length || 0} listeners for ${event.type}`);

        typeListeners?.forEach(cb => {
            try {
                console.log(`[WebSocketService] Calling listener for ${event.type}`);
                cb(event);
            } catch (e) {
                console.error('[WebSocketService] Listener error:', e);
            }
        });

        // Wildcard listeners
        const wildcardListeners = this.listeners.get('*');
        wildcardListeners?.forEach(cb => {
            try { cb(event); } catch (e) { console.error('[WebSocketService] Wildcard error:', e); }
        });
    }

    /**
     * Subscribe to events of a specific type
     * @returns Unsubscribe function
     */
    subscribe(eventType: EventType | '*', callback: EventCallback): () => void {
        if (!this.listeners.has(eventType)) {
            this.listeners.set(eventType, []);
        }
        this.listeners.get(eventType)!.push(callback);
        console.log(`[WebSocketService] 📝 Subscribed to ${eventType}`);

        // Return unsubscribe function
        return () => {
            const callbacks = this.listeners.get(eventType);
            if (callbacks) {
                const index = callbacks.indexOf(callback);
                if (index > -1) {
                    callbacks.splice(index, 1);
                }
            }
        };
    }

    /**
     * Check if WebSocket is connected
     */
    isConnected(): boolean {
        return this.connected;
    }

    /**
     * Disconnect the WebSocket
     */
    disconnect(): void {
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
        }
        if (this.socket) {
            this.socket.close(1000, 'Client disconnect');
            this.socket = null;
        }
        this.connected = false;
    }
}

// Export singleton instance
export const webSocketService = new WebSocketService();
