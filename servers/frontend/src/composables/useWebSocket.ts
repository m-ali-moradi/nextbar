/**
 * ============================================================
 * useWebSocket Composable - Real-time Event Handling
 * ============================================================
 * 
 * This composable manages WebSocket connection to the gateway
 * and provides reactive event subscription for real-time updates.
 * 
 * Features:
 * - Automatic connection management with reconnection
 * - Event subscription by type or resource ID
 * - TypeScript support for event payloads
 * - Connection status tracking
 * 
 * Usage:
 * ```typescript
 * import { useWebSocket } from '@/composables/useWebSocket';
 * 
 * const { connected, subscribe, connect } = useWebSocket();
 * 
 * onMounted(() => {
 *   connect();
 *   subscribe('SUPPLY_REQUEST_UPDATED', (event) => {
 *     console.log('Supply request updated:', event);
 *     // Refresh data from store
 *   });
 * });
 * ```
 */

import { ref, onUnmounted, shallowRef } from 'vue';

// ============================================================
// Type Definitions
// ============================================================

/**
 * Event types that can be received from the WebSocket
 */
export type EventType = 
  | 'SUPPLY_REQUEST_UPDATED'
  | 'BAR_STOCK_UPDATED'
  | 'WAREHOUSE_STOCK_UPDATED'
  | 'DROPPOINT_STATUS_CHANGED'
  | 'EVENT_UPDATED'
  | 'HEARTBEAT';

/**
 * Structure of events received from the gateway WebSocket
 */
export interface NextBarEvent {
  type: EventType;
  source: string;
  resourceId: string | null;
  payload: Record<string, unknown>;
  timestamp: string;
}

/**
 * Callback function type for event handlers
 */
export type EventCallback = (event: NextBarEvent) => void;

// ============================================================
// WebSocket Configuration
// ============================================================

/** Gateway WebSocket endpoint */
const WS_URL = 'ws://localhost:8080/ws/events';

/** Reconnection delay in milliseconds */
const RECONNECT_DELAY = 5000;

/** Maximum reconnection attempts */
const MAX_RECONNECT_ATTEMPTS = 10;

// ============================================================
// Composable Implementation
// ============================================================

export function useWebSocket() {
  // Connection state
  const connected = ref(false);
  const connecting = ref(false);
  const error = ref<string | null>(null);
  
  // WebSocket instance (shallowRef to avoid deep reactivity)
  const socket = shallowRef<WebSocket | null>(null);
  
  // Reconnection tracking
  let reconnectAttempts = 0;
  let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;

  // Event listeners map: eventType -> callbacks[]
  const listeners = new Map<EventType | '*', EventCallback[]>();

  /**
   * Connect to the WebSocket server
   * 
   * Establishes connection to the gateway WebSocket endpoint
   * and sets up automatic reconnection on disconnect.
   */
  const connect = () => {
    // Don't connect if already connected or connecting
    if (socket.value?.readyState === WebSocket.OPEN || connecting.value) {
      console.log('[WebSocket] Already connected or connecting');
      return;
    }

    connecting.value = true;
    error.value = null;

    console.log('[WebSocket] Connecting to', WS_URL);
    
    try {
      socket.value = new WebSocket(WS_URL);
      
      // ======== Connection Opened ========
      socket.value.onopen = () => {
        console.log('[WebSocket] ✅ Connected');
        connected.value = true;
        connecting.value = false;
        reconnectAttempts = 0;
      };

      // ======== Message Received ========
      socket.value.onmessage = (event) => {
        try {
          const data: NextBarEvent = JSON.parse(event.data);
          
          // Skip heartbeat logging to reduce noise
          if (data.type !== 'HEARTBEAT') {
            console.log('[WebSocket] 📨 Event received:', data.type, data);
          }

          // Dispatch to registered listeners
          dispatchEvent(data);
        } catch (e) {
          console.error('[WebSocket] Failed to parse message:', e);
        }
      };

      // ======== Connection Closed ========
      socket.value.onclose = (event) => {
        console.log('[WebSocket] 🔌 Disconnected:', event.code, event.reason);
        connected.value = false;
        connecting.value = false;
        
        // Attempt reconnection if not a clean close
        if (!event.wasClean) {
          scheduleReconnect();
        }
      };

      // ======== Connection Error ========
      socket.value.onerror = (event) => {
        console.error('[WebSocket] ❌ Error:', event);
        error.value = 'WebSocket connection error';
        connected.value = false;
        connecting.value = false;
      };
      
    } catch (e) {
      console.error('[WebSocket] Failed to create connection:', e);
      error.value = 'Failed to connect';
      connecting.value = false;
      scheduleReconnect();
    }
  };

  /**
   * Schedule a reconnection attempt
   */
  const scheduleReconnect = () => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.log('[WebSocket] Max reconnection attempts reached');
      error.value = 'Unable to connect to server';
      return;
    }

    reconnectAttempts++;
    console.log(`[WebSocket] Reconnecting in ${RECONNECT_DELAY}ms (attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);

    reconnectTimeout = setTimeout(() => {
      connect();
    }, RECONNECT_DELAY);
  };

  /**
   * Dispatch event to all registered listeners
   */
  const dispatchEvent = (event: NextBarEvent) => {
    // Call listeners for specific event type
    const typeListeners = listeners.get(event.type);
    if (typeListeners) {
      typeListeners.forEach(callback => {
        try {
          callback(event);
        } catch (e) {
          console.error('[WebSocket] Listener error:', e);
        }
      });
    }

    // Call wildcard listeners (listen to all events)
    const wildcardListeners = listeners.get('*');
    if (wildcardListeners) {
      wildcardListeners.forEach(callback => {
        try {
          callback(event);
        } catch (e) {
          console.error('[WebSocket] Wildcard listener error:', e);
        }
      });
    }
  };

  /**
   * Subscribe to events of a specific type
   * 
   * @param eventType - The event type to listen for, or '*' for all events
   * @param callback - Function to call when event is received
   * @returns Unsubscribe function
   * 
   * @example
   * ```typescript
   * const unsubscribe = subscribe('SUPPLY_REQUEST_UPDATED', (event) => {
   *   if (event.resourceId === barId) {
   *     // Refresh bar data
   *     barStore.fetchBarDetails(barId);
   *   }
   * });
   * 
   * // Later: unsubscribe when no longer needed
   * onUnmounted(() => unsubscribe());
   * ```
   */
  const subscribe = (eventType: EventType | '*', callback: EventCallback): (() => void) => {
    if (!listeners.has(eventType)) {
      listeners.set(eventType, []);
    }
    
    listeners.get(eventType)!.push(callback);
    console.log(`[WebSocket] 📝 Subscribed to ${eventType}`);

    // Return unsubscribe function
    return () => {
      const callbacks = listeners.get(eventType);
      if (callbacks) {
        const index = callbacks.indexOf(callback);
        if (index > -1) {
          callbacks.splice(index, 1);
          console.log(`[WebSocket] 🗑️ Unsubscribed from ${eventType}`);
        }
      }
    };
  };

  /**
   * Disconnect from the WebSocket server
   */
  const disconnect = () => {
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
      reconnectTimeout = null;
    }

    if (socket.value) {
      socket.value.close(1000, 'Client disconnect');
      socket.value = null;
    }

    connected.value = false;
    connecting.value = false;
    console.log('[WebSocket] Disconnected by client');
  };

  // Cleanup on component unmount
  onUnmounted(() => {
    disconnect();
  });

  // ============================================================
  // Return Public API
  // ============================================================
  
  return {
    /** Whether WebSocket is currently connected */
    connected,
    
    /** Whether WebSocket is currently connecting */
    connecting,
    
    /** Last error message, if any */
    error,
    
    /** Connect to WebSocket server */
    connect,
    
    /** Disconnect from WebSocket server */
    disconnect,
    
    /** Subscribe to events of a specific type */
    subscribe,
  };
}
