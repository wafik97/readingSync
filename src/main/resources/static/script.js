let currentPage = 1;
let pdfDoc = null;
let socket = new WebSocket("ws://localhost:8080/sync");

// Store the users and their current pages
let users = {};

// Open WebSocket connection
socket.addEventListener("open", function(event) {
    console.log("Connected to WebSocket");
});

// Function to update the user list in the sidebar
function updateUserList() {
    const userList = document.getElementById("userList");
    userList.innerHTML = "";  // Clear current list

    for (const userId in users) {
        const li = document.createElement("li");
        li.classList.add("user-item");
        li.innerHTML = `<span>User ${userId}:</span> Page ${users[userId]}`;
        userList.appendChild(li);
    }
}

// Handle messages from the server
socket.addEventListener("message", function(event) {
    const message = JSON.parse(event.data);  // Parse JSON message
    console.log("Received message:", message);

    try {
        if (message.type === "user_join") {
            // Handle user joining
            users[message.userId] = message.page;
            updateUserList();
        }
        else if (message.type === "user_list") {
            // Handle user list update
            message.users.forEach(user => {
                users[user.userId] = user.page;
            });
            updateUserList();
        }
        else if (message.type === "page_update") {
            // Handle page updates
            users[message.userId] = message.page;
            updateUserList();
        }
        else {
            console.log("Unexpected message format:", message);
        }
    } catch (error) {
        console.error("Error processing WebSocket message:", error);
    }
});

// Function to send the current page update to the WebSocket server
function sendPageUpdate() {
    const message = JSON.stringify({
        type: "page_update",
        userId: "someUserId", // You should set the actual userId here
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
