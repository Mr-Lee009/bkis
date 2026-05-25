(() => {
    const CHUNK_SIZE = 10 * 1024 * 1024;
    const MAX_RETRY = 3;
    const MAX_CONCURRENT = 4;

    // Hien thi thong bao trang thai trong widget upload.
    function setWidgetStatus(widget, message, type = "muted") {
        const status = widget.querySelector("[data-upload-status]");
        if (!status) {
            return;
        }
        status.className = `small text-${type}`;
        status.textContent = message;
    }

    // Cap nhat thanh tien do upload theo so chunk da gui thanh cong.
    function updateWidgetProgress(widget, uploadedCount, totalChunks) {
        const percent = Math.floor((uploadedCount / totalChunks) * 100);
        const progressBar = widget.querySelector("[data-upload-progress]");
        if (!progressBar) {
            return;
        }
        progressBar.style.width = `${percent}%`;
        progressBar.textContent = `${percent}%`;
        progressBar.setAttribute("aria-valuenow", String(percent));
    }

    // Chay danh sach task voi gioi han song song de tranh qua tai server.
    async function runWithConcurrency(tasks, limit) {
        const executing = new Set();
        for (const task of tasks) {
            const promise = task().finally(() => executing.delete(promise));
            executing.add(promise);
            if (executing.size >= limit) {
                await Promise.race(executing);
            }
        }
        await Promise.all(executing);
    }

    // Doc message loi tu response JSON hoac text cua server.
    async function readErrorMessage(response, fallback) {
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            const body = await response.json().catch(() => ({}));
            return body.message || fallback;
        }
        const text = await response.text().catch(() => "");
        return text || fallback;
    }

    // Goi API init va lay uploadId cho phien upload hien tai.
    async function initUpload(file, totalChunks, folder) {
        const response = await fetch("/upload/api/init", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                fileName: file.name,
                totalSize: file.size,
                totalChunks,
                folder
            })
        });

        if (!response.ok) {
            throw new Error(await readErrorMessage(response, "Khong the khoi tao upload."));
        }

        const body = await response.json();
        if (!body.uploadId) {
            throw new Error("Server khong tra ve uploadId.");
        }
        return body.uploadId;
    }

    // Upload mot chunk co retry de bat loi mang tam thoi.
    async function uploadChunkWithRetry(uploadId, chunkIndex, chunk) {
        for (let attempt = 1; attempt <= MAX_RETRY; attempt++) {
            const formData = new FormData();
            formData.append("uploadId", uploadId);
            formData.append("chunkIndex", chunkIndex);
            formData.append("file", chunk, `chunk_${chunkIndex}`);

            try {
                const response = await fetch("/upload/api/chunk", {
                    method: "POST",
                    body: formData
                });
                if (!response.ok) {
                    throw new Error(await readErrorMessage(response, `Chunk ${chunkIndex + 1} upload that bai.`));
                }
                return;
            } catch (error) {
                if (attempt === MAX_RETRY) {
                    throw error;
                }
                await new Promise((resolve) => window.setTimeout(resolve, 500 * attempt));
            }
        }
    }

    // Goi API complete de ghep file va lay URL public.
    async function completeUpload(uploadId) {
        const response = await fetch(`/upload/api/complete?uploadId=${encodeURIComponent(uploadId)}`, {
            method: "POST"
        });
        if (!response.ok) {
            throw new Error(await readErrorMessage(response, "Khong the hoan tat upload."));
        }

        const body = await response.json();
        if (!body.url) {
            throw new Error("Server khong tra ve URL file.");
        }
        return body.url;
    }

    // Upload file cua mot widget va dien URL vao input dich.
    async function uploadWidgetFile(widget) {
        const fileInput = widget.querySelector("[data-upload-file]");
        const uploadButton = widget.querySelector("[data-upload-button]");
        const targetInput = document.querySelector(widget.dataset.targetInput || "");
        const folder = widget.dataset.uploadFolder || "/source";
        const file = fileInput?.files?.[0];

        if (!file) {
            setWidgetStatus(widget, "Vui long chon file truoc khi upload.", "danger");
            return;
        }
        if (!targetInput) {
            setWidgetStatus(widget, "Khong tim thay o nhap URL dich.", "danger");
            return;
        }

        const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
        let uploadedCount = 0;

        uploadButton.disabled = true;
        fileInput.disabled = true;
        updateWidgetProgress(widget, 0, 1);
        setWidgetStatus(widget, "Dang khoi tao upload...", "muted");

        try {
            const uploadId = await initUpload(file, totalChunks, folder);
            const tasks = [];
            for (let index = 0; index < totalChunks; index++) {
                tasks.push(async () => {
                    const start = index * CHUNK_SIZE;
                    const end = Math.min(start + CHUNK_SIZE, file.size);
                    await uploadChunkWithRetry(uploadId, index, file.slice(start, end));
                    uploadedCount++;
                    updateWidgetProgress(widget, uploadedCount, totalChunks);
                });
            }

            setWidgetStatus(widget, "Dang upload file...", "muted");
            await runWithConcurrency(tasks, MAX_CONCURRENT);

            setWidgetStatus(widget, "Dang ghep file tren server...", "muted");
            const fileUrl = await completeUpload(uploadId);
            targetInput.value = fileUrl;
            targetInput.dispatchEvent(new Event("input", { bubbles: true }));
            setWidgetStatus(widget, "Upload thanh cong. URL da duoc dien vao o Video URL.", "success");
        } catch (error) {
            console.error(error);
            setWidgetStatus(widget, error.message || "Upload that bai.", "danger");
        } finally {
            uploadButton.disabled = false;
            fileInput.disabled = false;
        }
    }

    // Khoi tao tat ca upload widget tren trang hien tai.
    function initUploadWidgets() {
        document.querySelectorAll("[data-upload-widget]").forEach((widget) => {
            const uploadButton = widget.querySelector("[data-upload-button]");
            uploadButton?.addEventListener("click", () => uploadWidgetFile(widget));
        });
    }

    document.addEventListener("DOMContentLoaded", initUploadWidgets);
})();
