window.BkisApi = window.BkisApi || {};

$(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajaxSetup({
        beforeSend: function (xhr) {
            if (header && token) {
                xhr.setRequestHeader(header, token);
            }
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
});

/**
 * Show loading and handle logic for get method.
 * @param url url
 * @param successFunction success callback
 * @param errorFunction error callback
 * @param anotherParams extra params for success callback
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
    });
}

/**
 * Show loading and handle logic for post method.
 * @param url url
 * @param requestData request data
 * @param successFunction success callback
 * @param errorFunction error callback
 * @param anotherParams extra params for success callback
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
    });
}

/**
 * Common function to upload a file by ajax.
 * @param steelType steel type
 * @param fileType file type
 * @param file input file
 * @param onSuccess callback when upload succeeds
 * @param onError callback when upload fails
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
        error: function (error) {
            onError(error);
        }
    });
}

/**
 * Validate upload file, including server-side file-name encoding validation.
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
        errors.push(err.message);
        return errors;
    }

    const fileExtension = fileName.split('.').pop();
    errors.push(...await validateFileNameCode([fileName]));

    const bytes = Encoding.convert(fileName, {
        to: 'SJIS',
        from: 'UNICODE',
        type: 'array'
    });
    if (bytes.length > FILE_NAME_MAX_LENGTH) {
        errors.push(format(MESSAGE_LIST.MSGV0007, fileName, FILE_NAME_MAX_LENGTH));
    }

    if (fileType !== fileExtension) {
        errors.push(format(MESSAGE_LIST.MSGV0022, fileType));
    }

    const size = fileInput.files[0].size;
    const maxFileSize = sizeUnit === SIZE_UNIT.KB ? fileSize * 1024 : fileSize * 1024 * 1024;
    if (size > maxFileSize) {
        errors.push(format(MESSAGE_LIST.MSGV0023, fileSize + sizeUnit));
    }

    return errors;
}

/**
 * Call ajax to validate file name character code.
 * @param fileNames file names
 */
async function validateFileNameCode(fileNames) {
    let errors = [];

    function onSuccess(data) {
        errors.push(...data);
    }

    function onError() {
        errors.push(MESSAGE_LIST.MSG0001E);
    }

    await ajaxPost("/common/upload/validate-file-name", fileNames, onSuccess, onError);
    return errors;
}

/**
 * Show loading and handle logic for get method.
 * @param url url
 * @param successFunction success callback
 * @param errorFunction error callback
 * @param anotherParams extra params for success callback
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
 * Build uri from base uri and params.
 * @param baseUri baseUri
 * @param params params
 * @returns {string} url
 */
function buildUri(baseUri, params = {}) {
    const query = Object.entries(params)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v ? v : '')}`)
        .join("&");

    return query ? `${baseUri}?${query}` : baseUri;
}

window.BkisApi.ajaxGet = ajaxGet;
window.BkisApi.ajaxPost = ajaxPost;
window.BkisApi.ajaxGetData = ajaxGetData;
window.BkisApi.uploadFile = uploadFile;
window.BkisApi.validateUploadFile = validateUploadFile;
window.BkisApi.validateFileNameCode = validateFileNameCode;
window.BkisApi.buildUri = buildUri;
