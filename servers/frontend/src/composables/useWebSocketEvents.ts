import { ref, reactive, watch } from 'vue';
import ApiService from '@/api';

// ============================================================
// Type Definitions
// ============================================================

export type EventType =
    | 'SUPPLY_REQUEST_CREATED'
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
    'SUPPLY_REQUEST_CREATED': null,
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
const WS_BASE_URL = 'ws://localhost:8080/ws/events';
const maxReconnectAttempts = 10;
const reconnectDelay = 5000;

function getAuthToken(): string | null {
    // Read from short-lived session storage only; tokens must not be in localStorage.
    try {
        const token = sessionStorage.getItem('authToken');
        return token && token.trim() ? token : null;
    } catch {
        return null;
    }
}

/**
 * Build WebSocket URL with JWT token for authentication.
 * Token is read from localStorage where the auth store persists it.
 */
function buildWsUrl(): string {
    // Do not send tokens in URL. Use cookie-based auth or Authorization header in future.
    return WS_BASE_URL;
}

function connect() {
    void connectInternal();
}

async function connectInternal() {
    if (socket?.readyState === WebSocket.OPEN || connecting.value) {
        return;
    }

    connecting.value = true;
    error.value = null;
    const authToken = getAuthToken();
    if (!authToken) {
        // No short-lived token available; do not open authenticated websocket.
        connecting.value = false;
        connected.value = false;
        return;
    }

    const wsUrl = buildWsUrl();
    let wsTicket: string;

    try {
        const response = await ApiService.get('/api/v1/users/ws-ticket');
        wsTicket = response?.data?.ticket;
        if (!wsTicket || typeof wsTicket !== 'string') {
            throw new Error('Missing websocket ticket');
        }
    } catch {
        error.value = 'Unable to authenticate websocket';
        connecting.value = false;
        connected.value = false;
        return;
    }

    try {
        // Do not include token in URL. Pass one-time ticket via subprotocol header.
        socket = new WebSocket(wsUrl, ['nextbar.v1', `ticket.${wsTicket}`]);

        socket.onopen = () => {
            connected.value = true;
            connecting.value = false;
            reconnectAttempts = 0;
        };

        socket.onmessage = (event) => {
            try {
                const data: NextBarEvent = JSON.parse(event.data);

                // ====== TRIGGER VUE REACTIVITY ======
                // Update the reactive refs - this triggers Vue watchers!
                lastEvent.value = data;
                eventCounter.value++;
                eventsByType[data.type] = data;

            } catch {
            }
        };

        socket.onclose = (event) => {
            connected.value = false;
            connecting.value = false;

            if (event.code === 4401) {
                error.value = 'Unauthorized';
                return;
            }

            if (!event.wasClean) {
                scheduleReconnect();
            }
        };

        socket.onerror = () => {
            error.value = 'WebSocket connection error';
            connected.value = false;
            connecting.value = false;
        };

    } catch {
        error.value = 'Failed to connect';
        connecting.value = false;
        scheduleReconnect();
    }
}

function scheduleReconnect() {
    if (!getAuthToken()) {
        return;
    }

    if (reconnectAttempts >= maxReconnectAttempts) {
        error.value = 'Unable to connect to server';
        return;
    }

    reconnectAttempts++;

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
connect();
