<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <title>NimbusLink Admin Dashboard</title>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #f4f4f9;
                margin: 0;
                padding: 20px;
            }

            .container {
                max-width: 1200px;
                margin: 0 auto;
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 20px;
            }

            .card {
                background: white;
                padding: 20px;
                border-radius: 8px;
                box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
            }

            h1 {
                text-align: center;
                color: #333;
            }

            h2 {
                border-bottom: 2px solid #eee;
                padding-bottom: 10px;
                margin-top: 0;
            }

            ul {
                list-style: none;
                padding: 0;
            }

            li {
                padding: 8px;
                border-bottom: 1px solid #eee;
            }

            #log-container {
                height: 300px;
                overflow-y: auto;
                background: #222;
                color: #0f0;
                padding: 10px;
                font-family: monospace;
                border-radius: 4px;
            }

            .log-entry {
                margin: 2px 0;
            }

            .status-badge {
                display: inline-block;
                padding: 4px 8px;
                border-radius: 4px;
                background: #ddd;
                font-size: 0.8em;
            }

            .status-connected {
                background: #d4edda;
                color: #155724;
            }

            .status-disconnected {
                background: #f8d7da;
                color: #721c24;
            }
        </style>
    </head>

    <body>
        <h1>NimbusLink Admin Dashboard</h1>

        <div class="container">
            <div class="card">
                <h2>Server Status</h2>
                <p><strong>TCP Port:</strong> 8888</p>
                <p><strong>WebSocket Status:</strong> <span id="ws-status" class="status-badge">Connecting...</span></p>
                <p><strong>Active TCP Clients:</strong> <span id="client-count">${clientCount}</span></p>
            </div>

            <div class="card">
                <h2>Active Rooms</h2>
                <ul id="room-list">
                    <c:forEach items="${rooms}" var="room">
                        <li>${room}</li>
                    </c:forEach>
                </ul>
            </div>

            <div class="card">
                <h2>Connected Clients</h2>
                <ul id="client-list">
                </ul>
            </div>

            <div class="card">
                <h2>Actions</h2>
                <div style="margin-bottom: 10px;">
                    <input type="text" id="broadcast-msg" placeholder="Broadcast Message">
                    <button onclick="broadcast()">Send Broadcast</button>
                </div>
                <div>
                    <input type="text" id="kick-id" placeholder="Client ID to Kick">
                    <button onclick="kick()">Kick Client</button>
                </div>
            </div>

            <div class="card" style="grid-column: 1 / -1;">
                <h2>Live Server Logs</h2>
                <div id="log-container"></div>
                <button onclick="clearLogs()" style="margin-top: 10px; padding: 5px 10px;">Clear Logs</button>
            </div>
        </div>

        <script>
            var stompClient = null;

            function connect() {
                var socket = new SockJS('/ws');
                stompClient = Stomp.over(socket);
                stompClient.debug = null; // Disable debug logs
                stompClient.connect({}, function (frame) {
                    document.getElementById('ws-status').textContent = 'Connected';
                    document.getElementById('ws-status').className = 'status-badge status-connected';

                    stompClient.subscribe('/topic/events', function (message) {
                        handleEvent(JSON.parse(message.body));
                    });

                    stompClient.subscribe('/topic/logs', function (message) {
                        addLog(message.body);
                    });
                }, function (error) {
                    document.getElementById('ws-status').textContent = 'Disconnected';
                    document.getElementById('ws-status').className = 'status-badge status-disconnected';
                    setTimeout(connect, 5000);
                });
            }

            function handleEvent(event) {
                console.log("Event:", event);
                if (event.type === 'CLIENT_CONNECT') {
                    updateClientCount(1);
                    loadClients();
                } else if (event.type === 'CLIENT_DISCONNECT') {
                    updateClientCount(-1);
                    loadClients();
                } else if (event.type === 'ROOM_JOIN') {
                    addRoom(event.data.room);
                } else if (event.type === 'ROOM_LEAVE') {
                    // We might want to check if room is empty, but for now just UI update
                } else if (event.type === 'NICKNAME_SET') {
                    loadClients();
                }
            }

            function updateClientCount(delta) {
                var el = document.getElementById('client-count');
                var count = parseInt(el.textContent) || 0;
                el.textContent = Math.max(0, count + delta);
            }

            function addRoom(room) {
                var list = document.getElementById('room-list');
                var items = list.getElementsByTagName('li');
                for (var i = 0; i < items.length; ++i) {
                    if (items[i].textContent === room) return;
                }
                var li = document.createElement('li');
                li.textContent = room;
                list.appendChild(li);
            }

            function addLog(message) {
                var container = document.getElementById('log-container');
                var div = document.createElement('div');
                div.className = 'log-entry';
                div.textContent = new Date().toLocaleTimeString() + " - " + message;
                container.appendChild(div);
                container.scrollTop = container.scrollHeight;
            }

            function clearLogs() {
                document.getElementById('log-container').innerHTML = '';
            }

            function broadcast() {
                var msg = document.getElementById('broadcast-msg').value;
                if (!msg) return;
                fetch('/api/admin/broadcast', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: msg })
                }).then(() => {
                    document.getElementById('broadcast-msg').value = '';
                    alert('Broadcast sent');
                });
            }

            function kick() {
                var id = document.getElementById('kick-id').value;
                if (!id) return;
                fetch('/api/admin/kick', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ clientId: id })
                }).then(res => {
                    if (res.ok) {
                        document.getElementById('kick-id').value = '';
                        alert('Client kicked');
                    } else {
                        alert('Client not found');
                    }
                });
            }

            function loadClients() {
                fetch('/api/admin/clients')
                    .then(res => res.json())
                    .then(clients => {
                        var list = document.getElementById('client-list');
                        list.innerHTML = '';
                        clients.forEach(client => {
                            var li = document.createElement('li');
                            li.textContent = client.displayName;
                            if (client.displayName !== client.clientId) {
                                li.textContent += ' (' + client.clientId + ')';
                            }
                            list.appendChild(li);
                        });
                    });
            }

            connect();
            loadClients();
        </script>
    </body>

    </html>