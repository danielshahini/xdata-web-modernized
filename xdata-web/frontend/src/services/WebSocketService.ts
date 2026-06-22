import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BASE_URL } from '../api';

class WebSocketService {
    private client: Client | null = null;
    // Desired subscriptions keyed by topic. Kept independently of the live
    // STOMP subscriptions so they can be (re)applied on every (re)connect.
    private handlers: Map<string, (message: any) => void> = new Map();
    private active: Map<string, StompSubscription> = new Map();

    connect() {
        return new Promise((resolve, reject) => {
            // Reuse an already-connected client (the service is a singleton and
            // several components may call connect()).
            if (this.client && this.client.active) {
                if (this.client.connected) resolve(true);
                return;
            }

            this.client = new Client({
                // Must create a NEW SockJS per attempt — reusing one instance
                // breaks automatic reconnects (the closed socket gets reused).
                webSocketFactory: () => new SockJS(`${BASE_URL}/ws-grading`),
                debug: () => {},
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            this.client.onConnect = () => {
                console.log('Connected to WebSocket');
                // (Re)apply every desired subscription — also covers reconnects.
                this.active.clear();
                this.handlers.forEach((cb, topic) => this.applySubscription(topic, cb));
                resolve(true);
            };

            this.client.onStompError = (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                reject(frame);
            };

            this.client.activate();
        });
    }

    private applySubscription(topic: string, callback: (message: any) => void) {
        if (!this.client || !this.client.connected) return;
        const subscription = this.client.subscribe(topic, (message) => {
            try {
                callback(JSON.parse(message.body));
            } catch (e) {
                console.error('WebSocket message handler failed:', e);
            }
        });
        this.active.set(topic, subscription);
    }

    subscribe(topic: string, callback: (message: any) => void) {
        // Remember the intent so it survives a not-yet-connected client and reconnects.
        this.handlers.set(topic, callback);
        if (this.client && this.client.connected) {
            this.applySubscription(topic, callback);
        }
    }

    unsubscribe(topic: string) {
        this.handlers.delete(topic);
        const sub = this.active.get(topic);
        if (sub) {
            sub.unsubscribe();
            this.active.delete(topic);
        }
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.active.clear();
            console.log('Disconnected from WebSocket');
        }
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;
