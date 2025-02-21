class UserDatabase {
    constructor() {
        this.users = {};  // Stores user progress { userId: pageNumber }
    }

    // Add or update a user's page
    updateUser(userId, page) {
        this.users[userId] = page;
    }

    // Remove a user from the database
    removeUser(userId) {
        delete this.users[userId];
    }

    // Get all users' progress
    getAllUsers() {
        return this.users;
    }
}

module.exports = UserDatabase;
