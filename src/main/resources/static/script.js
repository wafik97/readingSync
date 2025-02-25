let currentPage = 1;
let pdfDoc = null;
let socket = null;  // Initially, no socket connection

// Store the users and their current pages
let users = {};

// Function to update the user list in the sidebar
function updateUserList() {
    const userList = document.getElementById("userList");
    userList.innerHTML = "";  // Clear current list

    for (const userId in users) {

        if (userId.length > 10) {
                    continue;
                }

        const li = document.createElement("li");
        li.classList.add("user-item");
        li.innerHTML = `<span>${userId}:</span> Page ${users[userId]}`;
        userList.appendChild(li);
    }
}




// Function to handle the WebSocket connection after username is chosen
function connectWebSocket(username,room) {
    socket = new WebSocket("ws://localhost:8080/sync");

    // Open WebSocket connection
    socket.addEventListener("open", function(event) {
        console.log("Connected to WebSocket");

        // Send the user join message with the username as userId
        socket.send(JSON.stringify({ type: "user_join", userId: username, page: currentPage , room: room }));
    });



    // Handle messages from the server
    socket.addEventListener("message", function(event) {
        const message = JSON.parse(event.data);  // Parse JSON message
        console.log("Received message:", message);

        // Handle user list update
        if (message.type === "user_list") {
            users = {};  // Clear current users
            message.users.forEach(function(user) {
                users[user.userId] = user.page;
            });
            updateUserList();  // Update the UI with the new user list
        }

        // Handle page updates
        if (message.type === "page_update") {
            const userId = message.userId;
            const page = message.page;

            // Update the user's page if they are in the user list
            if (users[userId] !== undefined) {
                users[userId] = page;
                updateUserList();  // Update the UI with the new page for the user
            }
        }

        // Handle user join and leave
        if (message.type === "user_join" || message.type === "user_leave") {
            const userId = message.userId;
            if (message.type === "user_join") {
                users[userId] = message.page;
            } else {
                delete users[userId];
            }
            updateUserList();  // Update the UI after the user list changes
        }
    });
}

// Function to send the current page update to the WebSocket server
function sendPageUpdate() {
    const message = JSON.stringify({
        type: "page_update",
        userId: userId, // Use the entered username as userId here
        page: currentPage
    });
    socket.send(message);
}

// Function to load PDF file from the input
function loadPdf(file) {
    const reader = new FileReader();

    reader.onload = function(e) {
        const pdfData = new Uint8Array(e.target.result);
        pdfjsLib.getDocument(pdfData).promise.then(function(pdf) {
            pdfDoc = pdf;
            renderPages();
        });
    };

    reader.readAsArrayBuffer(file);
}

// Render all pages of the PDF file
function renderPages() {
    const container = document.getElementById('pdf-container');
    container.innerHTML = '';  // Clear previous pages

    for (let i = 1; i <= pdfDoc.numPages; i++) {
        renderPage(i);
    }
}

// Render a specific page
function renderPage(pageNum) {
    pdfDoc.getPage(pageNum).then(function(page) {
        const scale = 1.5;
        const viewport = page.getViewport({ scale: scale });
        const canvas = document.createElement('canvas');
        canvas.classList.add('pdf-page');
        const context = canvas.getContext('2d');

        canvas.height = viewport.height;
        canvas.width = viewport.width;

        page.render({
            canvasContext: context,
            viewport: viewport
        });

        document.getElementById('pdf-container').appendChild(canvas);
    });
}

// Detect page scroll and send page update
document.getElementById('pdf-container').addEventListener('scroll', function () {
    const container = document.getElementById('pdf-container');
    const pageHeight = container.firstChild ? container.firstChild.clientHeight : 0;

    const currentScrollPosition = container.scrollTop;
    const newPage = Math.ceil(currentScrollPosition / pageHeight) + 1;

    if (newPage !== currentPage) {
        currentPage = newPage;
        sendPageUpdate();
    }
});

// Listen for file input and load the selected PDF
document.getElementById('pdfFile').addEventListener('change', function(event) {
    const file = event.target.files[0];
    if (file) {
        loadPdf(file);
    }
});

// Set up the start button for the username prompt
document.getElementById('submitBtn').addEventListener('click', function() {
    let username = document.getElementById('usernameInput').value.trim();
    let room = document.getElementById('roomSelect').value.trim();

    // Check if the username is empty or too long
    if (username === "") {
        alert("Please enter a valid username!");
    } else if (username.length > 10) {
        alert("Username cannot be longer than 10 characters!");
    } else {

        userId = username;

        connectWebSocket(username,room);

        socket.addEventListener("message", function(event) {
               const message = JSON.parse(event.data);
               if (message.type != "error") {

               document.getElementById('usernameModal').style.display = "none";

               }
               else{
                window.location.reload();
               alert(message.message);



               }

        });



    }
});

