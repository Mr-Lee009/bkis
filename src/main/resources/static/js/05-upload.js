const CHUNK_SIZE = 1024 * 1024 * 10; // 10MB
const MAX_RETRY = 3;
const MAX_CONCURRENT = 4; // ⚡ số chunk upload song song
const uploadCommon = window.BkisCommon || {};
uploadCommon.sleep = uploadCommon.sleep || ((ms) => new Promise((resolve) => window.setTimeout(resolve, ms)));
uploadCommon.formatElapsedTime = uploadCommon.formatElapsedTime || ((ms) => `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`);
uploadCommon.runWithConcurrency = uploadCommon.runWithConcurrency || (async (tasks) => { await Promise.all(tasks.map((task) => task())); });

async function startUpload() {
    const fileInput = document.getElementById("fileInput");
    const file = fileInput.files[0];

    if (!file) {
        alert("Please select a file");
        return;
    }

    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    const startTime = Date.now();
    let uploadedCount = 0;
    let uploadId = "";
    let completed = false;

    setStatus("Initializing upload...");
    toggleButton(true);

    try {
        // INIT
        const initRes = await fetch("/upload/init", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                fileName: file.name,
                totalSize: file.size,
                totalChunks: totalChunks
            })
        });

        if (!initRes.ok) throw new Error("Init failed");

        uploadId = await initRes.text();

        setStatus("Uploading in parallel...");

        // 🔥 Tạo danh sách tasks
        const tasks = [];

        for (let i = 0; i < totalChunks; i++) {
            tasks.push(async () => {
                const start = i * CHUNK_SIZE;
                const end = Math.min(start + CHUNK_SIZE, file.size);
                const chunk = file.slice(start, end);

                const formData = new FormData();
                formData.append("uploadId", uploadId);
                formData.append("chunkIndex", i);
                formData.append("file", chunk);

                await uploadChunkWithRetry(formData, i);

                uploadedCount++;
                updateProgress(uploadedCount, totalChunks, startTime);
            });
        }

        // 🔥 chạy pool
        await uploadCommon.runWithConcurrency(tasks, MAX_CONCURRENT);

        // COMPLETE
        setStatus("Merging file on server...");

        const completeRes = await fetch(`/upload/complete?uploadId=${uploadId}`, {
            method: "POST"
        });

        if (!completeRes.ok) throw new Error("Merge failed");
        completed = true;

        const totalTime = uploadCommon.formatElapsedTime(Date.now() - startTime);

        setStatus(`✅ Upload completed in ${totalTime}`);
    } catch (err) {
        console.error(err);
        if (uploadId && !completed) {
            try {
                await abortUpload(uploadId);
            } catch (abortErr) {
                console.warn("Abort upload failed", abortErr);
            }
        }
        setStatus("❌ " + err.message);
    } finally {
        toggleButton(false);
    }
}

async function abortUpload(uploadId) {
    const res = await fetch("/upload/abort", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ uploadId })
    });

    if (!res.ok) {
        throw new Error("Abort upload failed");
    }
}

async function uploadChunkWithRetry(formData, chunkIndex) {
    for (let attempt = 1; attempt <= MAX_RETRY; attempt++) {
        try {
            const res = await fetch("/upload/chunk", {
                method: "POST",
                body: formData
            });

            if (!res.ok) throw new Error();

            return;
        } catch (err) {
            if (attempt === MAX_RETRY) {
                throw new Error(`Chunk ${chunkIndex} failed after ${MAX_RETRY} retries`);
            }

            console.warn(`Retry chunk ${chunkIndex} - attempt ${attempt}`);
            await uploadCommon.sleep(500 * attempt); // backoff nhẹ
        }
    }
}

function updateProgress(done, total, startTime) {
    const percent = Math.floor((done / total) * 100);

    const progressBar = document.getElementById("progressBar");
    progressBar.style.width = percent + "%";
    progressBar.innerText = percent + "%";

    // ⏱️ update thời gian
    const elapsed = Date.now() - startTime;
    document.getElementById("time").innerText = uploadCommon.formatElapsedTime(elapsed);
}

function setStatus(message) {
    document.getElementById("status").innerText = message;
}

function toggleButton(disabled) {
    document.querySelector("button").disabled = disabled;
}

