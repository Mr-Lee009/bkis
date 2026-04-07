$(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        complete: function (xhr) {
            $('#loading').addClass('d-none');
            const contentType = xhr.getResponseHeader("content-type");
            if (contentType && contentType.includes("text/html")) {
                const responseText = xhr.responseText;
                const parser = new DOMParser();
                const doc = parser.parseFromString(responseText, "text/html");

                const redirectUrl = doc.querySelector('meta[name="pageUrl"]')?.content;

                if (redirectUrl) {
                    window.location.href = redirectUrl;
                }
            }
        }
    });

    // off autocomplete for all input and textarea
    const whiteListTypes = ['text', 'password', 'number'];
    const inputSelector = whiteListTypes.map(type => `input[type="${type}"]`).join(', ');
    $(`${inputSelector}, textarea`).prop('autocomplete', 'off');
});

/**
 * Show loading and hanle logic for get method
 * @param url url
 * @param successFunction success function hanlde data
 * @param errorFunction error function handle error
 * @param anotherParams another params for success function
 */
function ajaxGet(url, successFunction, errorFunction, ...anotherParams) {
    $('#loading').removeClass('d-none');
    return $.ajax({
        url: url,
        type: "GET",
        contentType: "application/json",
        cache: false,
        dataType: "json",
        success: (data) => {
            $('#loading').addClass('d-none');
            successFunction(data, ...anotherParams);
        },
        error: function (error) {
            $('#loading').addClass('d-none');
            if (errorFunction) {
                errorFunction(error);
            }
        }
    })
}

/**
 * Show loading and hanle logic for get method
 * @param url url
 * @param requestData request data
 * @param successFunction success function hanlde data
 * @param errorFunction error function handle error
 * @param anotherParams another params for success function
 */
async function ajaxPost(url, requestData, successFunction, errorFunction, ...anotherParams) {
    $('#loading').removeClass('d-none');
    await $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(requestData),
        cache: false,
        dataType: "json",
        success: (data) => {
            $('#loading').addClass('d-none');
            successFunction(data, ...anotherParams);
        },
        error: function (error) {
            $('#loading').addClass('d-none');
            if (errorFunction) {
                errorFunction(error);
            }
        }
    })
}

/**
 * common function to uploading file by ajax
 *
 * @param steelType steel type
 * @param fileType file type
 * @param file input file
 * @param onSuccess callback when upload successfully
 * @param onError callback when upload failed
 */
function uploadFile(steelType, fileType, file, onSuccess, onError) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("steelType", steelType);
    formData.append("fileType", fileType);
    $.ajax({
        url: "/common/upload",
        type: "POST",
        processData: false,
        contentType: false,
        dataType: "json",
        data: formData,
        cache: false,
        success: (data) => {
            onSuccess(data);
        },
        onerror: function (error) {
            onError(error);
        }
    });
}

/**
 * validate upload file
 *
 * @param fileInput input file
 * @param fileType file extension
 * @param fileSize file size
 * @param sizeUnit size unit
 */
async function validateUploadFile(fileInput, fileType, fileSize, sizeUnit) {
    let errors = [];
    const filePath = fileInput.value;
    const fileName = filePath.split('\\').pop().toLowerCase();
    try {
        await readFileAsText(fileInput.files[0]);
    } catch (err) {
        // read file error
        errors.push(err.message);
        return errors;
    }

    const fileExtension = fileName.split('.').pop();
    // check character code
    errors.push(...await validateFileNameCode([fileName]));

    // check file name length
    const bytes = Encoding.convert(fileName, {
        to: 'SJIS',
        from: 'UNICODE',
        type: 'array'
    });
    if (bytes.length > FILE_NAME_MAX_LENGTH) {
        errors.push(format(MESSAGE_LIST.MSGV0007, fileName, FILE_NAME_MAX_LENGTH));
    }
    // check file extension
    if (fileType !== fileExtension) {
        errors.push(format(MESSAGE_LIST.MSGV0022, fileType));
    }

    // check file size
    const size = fileInput.files[0].size;
    const maxFileSize = sizeUnit === SIZE_UNIT.KB ? fileSize * 1024 : fileSize * 1024 * 1024;
    if (size > maxFileSize) {
        errors.push(format(MESSAGE_LIST.MSGV0023, fileSize + sizeUnit));
    }

    return errors;
}

/**
 * call ajax to validate file name character code
 *
 * @param fileNames file name
 */
async function validateFileNameCode(fileNames) {
    let errors = [];

    function onSuccess(data) {
        errors.push(...data);
    }

    function onError() {
        errors.push(MESSAGE_LIST.MSG0001E);
    }

    // check character code
    await ajaxPost("/common/upload/validate-file-name", fileNames, onSuccess, onError)
    return errors;
}

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
 * Show loading and handle logic for get method
 * @param url url
 * @param successFunction success function hanldde data
 * @param anotherParams another params for success function
 */
function ajaxGetData(url, successFunction, errorFunction, ...anotherParams) {
    $('#loading').removeClass('d-none');
    $.ajax({
        url: url,
        type: "GET",
        contentType: "application/json",
        cache: false,
        success: (data) => {
            successFunction(data, ...anotherParams);
        },
        error: function (error) {
            $('#loading').addClass('d-none');
            if (errorFunction) {
                errorFunction(error);
            }
        }
    });
}

/**
 * Build uri from base uri and param
 * @param baseUri baseUri
 * @param params params
 * @returns {string|*} url
 */
function buildUri(baseUri, params = {}) {
    const query = Object.entries(params)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v ? v : '')}`)
        .join("&");

    return query ? `${baseUri}?${query}` : baseUri;
}

/**
 * Redirect to common error page
 */
function redirectToErrorPage() {
    window.location.href = '/common/error';
}
