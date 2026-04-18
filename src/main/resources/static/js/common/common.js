$(function () {
    // off autocomplete for all input and textarea
    const whiteListTypes = ['text', 'password', 'number'];
    const inputSelector = whiteListTypes.map(type => `input[type="${type}"]`).join(', ');
    $(`${inputSelector}, textarea`).prop('autocomplete', 'off');
});

window.BkisCommon = window.BkisCommon || {};

window.BkisCommon.escapeHtml = function (value = '') {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
};

window.BkisCommon.escapeAttr = function (value = '') {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
};

window.BkisCommon.showAlert = function ($element, message) {
    if (!$element || !$element.length) {
        return;
    }
    $element.text(message || '').removeClass('d-none');
};

window.BkisCommon.hideAlert = function ($element) {
    if (!$element || !$element.length) {
        return;
    }
    $element.addClass('d-none').text('');
};

window.BkisCommon.showAlertTemporarily = function ($element, message, delay = 2600) {
    window.BkisCommon.showAlert($element, message);
    window.setTimeout(function () {
        window.BkisCommon.hideAlert($element);
    }, delay);
};

window.BkisCommon.getBootstrapModal = function (elementOrSelector) {
    const element = typeof elementOrSelector === 'string'
        ? document.querySelector(elementOrSelector)
        : elementOrSelector;
    return element && window.bootstrap ? new bootstrap.Modal(element) : null;
};

window.BkisCommon.initialsOf = function (name) {
    return (name || '')
        .split(' ')
        .filter(Boolean)
        .map((chunk) => chunk.charAt(0))
        .join('')
        .slice(0, 2)
        .toUpperCase();
};

window.BkisCommon.formatDateTime = function (value, locale = 'vi-VN', fallback = '--') {
    if (!value) {
        return fallback;
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleDateString(locale);
};

window.BkisCommon.sleep = function (ms) {
    return new Promise((resolve) => window.setTimeout(resolve, ms));
};

window.BkisCommon.runWithConcurrency = async function (tasks, limit) {
    const executing = [];

    for (const task of tasks) {
        const promise = task().then(() => {
            executing.splice(executing.indexOf(promise), 1);
        });

        executing.push(promise);

        if (executing.length >= limit) {
            await Promise.race(executing);
        }
    }

    await Promise.all(executing);
};

window.BkisCommon.formatElapsedTime = function (ms) {
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}m ${seconds}s`;
};

/**
 * format string
 * @param str str
 * @param args args
 * @returns {*}
 */
function format(str, ...args) {
    return str.replace(/{(\d+)}/g, (match, index) =>
        typeof args[index] !== 'undefined' ? args[index] : match
    );
}

/**
 * Display error when call ajax
 * @param id id
 * @param messages list of message or message text
 */
function displayError(id, messages) {
    let html = '<ul class="mb-0">';
    if (Array.isArray(messages)) {
        messages.forEach(message => {
            html += `<li>${message}</li>`;
        });

    } else if (messages) {
        html += `<li>${messages}</li>`;
    }
    html += '</ul>';
    document.getElementById(id).classList.add('alert', 'alert-danger');
    document.getElementById(id).innerHTML = html;
}

/**
 * Formats a date object or date string to yyyy/MM/dd format.
 *
 * @param {Date|string|number} dateValue - The date to format (Date object, ISO string, or timestamp)
 * @returns {string} Formatted date string in yyyy/MM/dd format, or empty string if invalid
 */
function formatDate(dateValue) {
    if (!dateValue) {
        return '';
    }

    try {
        const date = new Date(dateValue);

        // Check if date is valid
        if (isNaN(date.getTime())) {
            return '';
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}/${month}/${day}`;
    } catch (error) {
        return '';
    }
}

/**
 * try to read the find to detect existence
 *
 * @param file file input
 */
function readFileAsText(file) {
    const fileName = file.name;
    return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = () => resolve(reader.result);
        reader.onerror = () => {
            let msg;
            switch (reader.error.name) {
                case "NotFoundError":
                    msg = format(MESSAGE_LIST.MSGV0017, fileName);
                    break;
                default:
                    msg = MESSAGE_LIST.MSG0001E;
                    break;
            }
            reject(new Error(msg));
        }
        try {
            reader.readAsText(file);
        } catch (e) {
            reject(new Error(MESSAGE_LIST.MSG0001E));
        }
    });
}

/**
 * Show error message in modal
 * @param message
 * @param errorList
 */
function showErrorMessage(message, errorList) {
    $("#error-list").hide();
    $("#error-message").hide();

    if (message) {
        $("#error-message").text(message).show();
    }
    if (errorList) {
        $("#error-list").empty().show();
        errorList.forEach(function (error) {
            const li = $("<li></li>").text(error);
            $("#error-list").append(li);
        });
    }
    $("#error-message-box").show();
}

/**
 * Normalize value by converting blank/empty-like inputs to null.
 *
 * This function treats the following values as "empty":
 * - null
 * - undefined
 * - an empty string ("")
 * - a string containing only whitespace characters ("   ")
 *
 * If the input matches any of the above cases, the function returns null.
 * Otherwise, it returns the original value unchanged.
 *
 * @param {*} value - The input value to normalize.
 * @returns {*|null} - The original value if valid, otherwise null.
 */
function normalizeValue(value) {
    if (value == null || String(value).trim() === '') {
        return null;
    }
    return value;
}

/**
 * Redirect to common error page
 */
function redirectToErrorPage() {
    window.location.href = '/common/error';
}
