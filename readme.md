# ReadingSync

**ReadingSync** is a real-time reading synchronization service that allows multiple users to read the same PDF document while tracking each other's progress within a shared room. Users can communicate through a chat system, and progress is displayed only for users in the same room.

## Features

- **Room-Based Reading**: The service supports **three rooms**, each with a maximum of **five users**.
- **Independent Reading Progress**: Each user reads at their own pace, but can see the progress of other users in the same room.
- **Book Selection**:
    - The **first user** to enter an empty room selects the PDF book for that session.
    - Subsequent users joining that room will read the same book.
    - When a room becomes **empty**, users can select a new book.
- **Unique Usernames**: No duplicate usernames are allowed within a room.
- **Real-Time Chat**: Users in the same room can chat, with messages limited to **25 characters**.
- **Persistent PDF Repository**: Books are stored in a **Git repository** containing PDF files and metadata.

## Technologies Used

- **Backend**: Spring Boot (WebSockets)
- **Frontend**: HTML, JavaScript, WebSockets
- **Database**: Git repository storing PDF files and metadata
- **Containerization**: Docker

## Port

- **ReadingSync Service**: `8080`

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/wafik97/readingSync.git
   cd readingSync
    ```

2. **Build and run the service**:
   ```bash
   docker build -t reading-sync-service .
   docker run -p 8080:8080 reading-sync-service
    ```

3. **Access the application**:
    - Open your browser and go to: [http://localhost:8080](http://localhost:8080)  

## Testing

The project includes **unit tests** that are triggered after each commit and push. These tests are executed via **GitHub Actions** to ensure the quality and correctness of the service.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For any questions or issues, please open an issue on [GitHub](https://github.com/wafik97/readingSync).
