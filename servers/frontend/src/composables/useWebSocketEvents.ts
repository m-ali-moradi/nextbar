/**
 * ============================================================
 * useWebSocketEvents Composable - Reactive WebSocket for Vue.js
 * ============================================================
 * 
 * This composable provides a Vue-native reactive WebSocket connection.
 * It uses Vue's ref() and reactive() to ensure UI updates automatically
 * when WebSocket events arrive.
 * 
 * Key difference from plain class: This is integrated with Vue's reactivity!
 */

import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';

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

// ============================================================
// Global Reactive State (shared across all uses)
// ============================================================

/** Reactive connection state */
const connected = ref(false);
const connecting = ref(false);
const error = ref<string | null>(null);

/** 
 * Reactive event stream - components can watch this!
 * Each new event overwrites this ref, triggering Vue reactivity
 */
const lastEvent = ref<NextBarEvent | null>(null);

/** Event counter - useful for triggering watchers */
const eventCounter = ref(0);

/** Events by type - reactive object that stores the last event per type */
const eventsByType = reactive<Record<EventType, NextBarEvent | null>>({
    'SUPPLY_REQUEST_UPDATED': null,
    'BAR_STOCK_UPDATED': null,
    'WAREHOUSE_STOCK_UPDATED': null,
    'DROPPOINT_STATUS_CHANGED': null,
    'EVENT_UPDATED': null,
    'HEARTBEAT': null,
});

// ============================================================
// WebSocket Connection (singleton)
// ============================================================

let socket: WebSocket | null = null;
let reconnectAttempts = 0;
let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;
const wsUrl = 'ws://localhost:8080/ws/events';
const maxReconnectAttempts = 10;
const reconnectDelay = 5000;

function connect() {
    if (socket?.readyState === WebSocket.OPEN || connecting.value) {
        console.log('[useWebSocketEvents] Already connected or connecting');
        return;
    }

    connecting.value = true;
    error.value = null;
    console.log('[useWebSocketEvents] Connecting to', wsUrl);

    try {
        socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            console.log('[useWebSocketEvents] ✅ Connected!');
            connected.value = true;
            connecting.value = false;
            reconnectAttempts = 0;
        };

        socket.onmessage = (event) => {
            try {
                const data: NextBarEvent = JSON.parse(event.data);

                if (data.type !== 'HEARTBEAT') {
                    console.log('[useWebSocketEvents] 📨 Event received:', data.type, data);
                }

                // ====== TRIGGER VUE REACTIVITY ======
                // Update the reactive refs - this triggers Vue watchers!
                lastEvent.value = data;
                eventCounter.value++;
                eventsByType[data.type] = data;

            } catch (e) {
                console.error('[useWebSocketEvents] Parse error:', e);
            }
        };

        socket.onclose = (event) => {
            console.log('[useWebSocketEvents] 🔌 Disconnected:', event.code);
            connected.value = false;
            connecting.value = false;

            if (!event.wasClean) {
                scheduleReconnect();
            }
        };

        socket.onerror = () => {
            console.error('[useWebSocketEvents] ❌ Connection error');
            error.value = 'WebSocket connection error';
            connected.value = false;
            connecting.value = false;
        };

    } catch (e) {
        console.error('[useWebSocketEvents] Failed to connect:', e);
        error.value = 'Failed to connect';
        connecting.value = false;
        scheduleReconnect();
    }
}

function scheduleReconnect() {
    if (reconnectAttempts >= maxReconnectAttempts) {
        console.log('[useWebSocketEvents] Max reconnect attempts reached');
        error.value = 'Unable to connect to server';
        return;
    }

    reconnectAttempts++;
    console.log(`[useWebSocketEvents] Reconnecting in ${reconnectDelay}ms (attempt ${reconnectAttempts})`);

    reconnectTimeout = setTimeout(() => {
        connect();
    }, reconnectDelay);
}

function disconnect() {
    if (reconnectTimeout) {
        clearTimeout(reconnectTimeout);
        reconnectTimeout = null;
    }
    if (socket) {
        socket.close(1000, 'Client disconnect');
        socket = null;
    }
    connected.value = false;
    connecting.value = false;
}

// ============================================================
// Composable Export
// ============================================================

/**
 * Composable for reactive WebSocket events
 * 
 * @example
 * ```typescript
 * const { connected, lastEvent, onEvent } = useWebSocketEvents();
 * 
 * // Watch for specific event types
 * onEvent('SUPPLY_REQUEST_UPDATED', (event) => {
 *   console.log('Supply request updated!', event);
 *   refreshData();
 * });
 * ```
 */
export function useWebSocketEvents() {

    /**
     * Register a callback for a specific event type.
     * Uses Vue's watch() under the hood for reactivity.
     */
    function onEvent(eventType: EventType, callback: (event: NextBarEvent) => void) {
        // Watch the specific event type in the reactive object
        watch(
            () => eventsByType[eventType],
            (newEvent) => {
                if (newEvent) {
                    console.log(`[useWebSocketEvents] 🔔 Triggering callback for ${eventType}`);
                    callback(newEvent);
                }
            },
            { immediate: false }
        );
    }

    /**
     * Watch all events (except heartbeat)
     */
    function onAnyEvent(callback: (event: NextBarEvent) => void) {
        watch(lastEvent, (newEvent) => {
            if (newEvent && newEvent.type !== 'HEARTBEAT') {
                callback(newEvent);
            }
        });
    }

    return {
        // Reactive state
        connected,
        connecting,
        error,
        lastEvent,
        eventCounter,
        eventsByType,

        // Methods
        connect,
        disconnect,
        onEvent,
        onAnyEvent,
    };
}

// Auto-connect on module load (for singleton behavior)
// This ensures connection happens when the module is first imported
console.log('[useWebSocketEvents] Module loaded, connecting...');
connect();
