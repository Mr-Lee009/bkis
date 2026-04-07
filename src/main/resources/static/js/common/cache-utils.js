// session-storage-util.js
// Utility for CRUD operations on sessionStorage
const CacheUtils = {
    /**
     * Create or update an item in sessionStorage
     * @param {string} key - The key to store the value under
     * @param {*} value - The value to store (will be JSON-stringified)
     */
    setItem: function (key, value) {
        sessionStorage.setItem(key, JSON.stringify(value));
    },

    /**
     * Read an item from sessionStorage
     * @param {string} key - The key to retrieve
     * @returns {*} The parsed value, or null if not found
     */
    getItem: function (key) {
        const value = sessionStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    },

    /**
     * Delete an item from sessionStorage
     * @param {string} key - The key to remove
     */
    removeItem: function (key) {
        sessionStorage.removeItem(key);
    },

    /**
     * Clear all items from sessionStorage
     */
    clear: function () {
        sessionStorage.clear();
    },

    /**
     * Check if an item exists in sessionStorage
     * @param {string} key - The key to check
     * @returns {boolean} True if exists, false otherwise
     */
    checkExit: function (key) {
        return sessionStorage.getItem(key) !== null;
    }
};

