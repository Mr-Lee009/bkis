const CHUNK_SIZE = 1024 * 1024 * 10; // 10MB
const MAX_RETRY = 3;

async function startUpload() {
    const fileInput = document.getElementById("fileInput");
    const file = fileInput.files[0];

    if (!file) {
        alert("Please select a file");
        return;
    }

    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    const startTime = Date.now();

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

        const uploadId = await initRes.text();

        setStatus("Uploading...");

        // UPLOAD CHUNKS
        for (let i = 0; i < totalChunks; i++) {
            const start = i * CHUNK_SIZE;
            const end = Math.min(start + CHUNK_SIZE, file.size);
            const chunk = file.slice(start, end);

            const formData = new FormData();
            formData.append("uploadId", uploadId);
            formData.append("chunkIndex", i);
            formData.append("file", chunk);

            await uploadChunkWithRetry(formData, i);

            updateProgress(i + 1, totalChunks, startTime);
        }

        // COMPLETE (merge file)
        setStatus("Merging file on server...");

        const completeRes = await fetch(`/upload/complete?uploadId=${uploadId}`, {
            method: "POST"
        });

        if (!completeRes.ok) throw new Error("Merge failed");

        const totalTime = formatTime(Date.now() - startTime);

        updateProgress(totalChunks, totalChunks, startTime);
        setStatus(`✅ Upload completed in ${totalTime}`);
    } catch (err) {
        console.error(err);
        setStatus("❌ " + err.message);
    } finally {
        toggleButton(false);
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

            return; // success
        } catch (err) {
            if (attempt === MAX_RETRY) {
                throw new Error(`Chunk ${chunkIndex} failed after ${MAX_RETRY} retries`);
            }
            console.warn(`Retry chunk ${chunkIndex} - attempt ${attempt}`);
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
    document.getElementById("time").innerText = formatTime(elapsed);
}

function setStatus(message) {
    document.getElementById("status").innerText = message;
}

function toggleButton(disabled) {
    document.querySelector("button").disabled = disabled;
}

function formatTime(ms) {
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}m ${seconds}s`;
}
