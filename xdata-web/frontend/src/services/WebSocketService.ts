import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BASE_URL } from '../api';

class WebSocketService {
    private client: Client | null = null;
    private subscribers: Map<string, any> = new Map();

    connect() {
        return new Promise((resolve, reject) => {
            const socket = new SockJS(`${BASE_URL}/ws-grading`);
            this.client = new Client({
                webSocketFactory: () => socket,
                debug: (str) => {
                    // console.log(str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            this.client.onConnect = (frame) => {
                console.log('Connected to WebSocket');
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

    subscribe(topic: string, callback: (message: any) => void) {
        if (!this.client || !this.client.connected) {
            console.warn('WebSocket not connected. Subscription delayed.');
            return;
        }
        
        const subscription = this.client.subscribe(topic, (message) => {
            callback(JSON.parse(message.body));
        });
        
        this.subscribers.set(topic, subscription);
        return subscription;
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            console.log('Disconnected from WebSocket');
        }
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;
