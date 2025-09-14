/**
 * @param buttonId
 * Sets the state of the specified button to active and removes the active state from other buttons
 */
function setActive(buttonId) {
    const buttons = document.querySelectorAll('.btn-subtle-primary');
    buttons.forEach(btn => btn.classList.remove('active'));
    document.getElementById(buttonId).classList.add('active');
}

function uploadContent() {
    setActive('btn-upload');
    showMessage("add-content", "Upload form or modal would appear here.");
    uploadContentModal();
}

function showUploaded() {
    setActive('btn-show-uploaded');
    showMessage("view-content", "List of uploaded content would be displayed here.");
}

function showDrafts() {
    setActive('btn-show-drafts');
    showMessage("view-content", "Draft items would be shown here.");
}

function showMessage(action, message) {
    const output = document.querySelectorAll(".output");
    console.log("Output:", output);
    output.forEach(out =>  out.classList.add('collapse'));
    console.log(action);
    const actionItem = document.getElementById(action);
    if (actionItem) {
        console.log("Action Item:", actionItem);
        actionItem.classList.remove('collapse');
        // actionItem.textContent = message;
    }
    output.textContent = message;
}

function uploadContentModal() {
    const modal = document.getElementById("output");

}

function clearContentForm() {
    const confirmed = confirm("Are you sure you want to clear all fields?");
    if (!confirmed) return;

    const form = document.getElementById("contentUploadForm");
    if (form) {
        form.reset();
        const imageList = document.getElementById("imageList");
        if (imageList) imageList.innerHTML = "";
        const imagePreview = document.getElementById("imagePreview");
        if (imagePreview) {
            const carouselInner = imagePreview.querySelector(".carousel-inner");
            if (carouselInner) carouselInner.innerHTML = "";
        }
    }
}

function displayImageNames() {
    const input = document.getElementById("imageUpload");
    const imagePreview = document.getElementById("imagePreview");
    const list = document.getElementById("imageList");
    list.innerHTML = ""; // Clear previous entries
    if (imagePreview) {
        const carouselInner = imagePreview.querySelector(".carousel-inner");
        if (carouselInner) {
            carouselInner.innerHTML = "";
        }
    }

    Array.from(input.files).forEach((file, idx) => {
        const listItem = document.createElement("li");
        listItem.className = "list-group-item";
        listItem.textContent = file.name;
        list.appendChild(listItem);

        const carouselInner = imagePreview.querySelector(".carousel-inner");

        const carouselItem = document.createElement("div");
        carouselItem.className = "carousel-item";
        if (idx === 0) carouselItem.classList.add("active");
        const img = document.createElement("img");
        const imageUrl = URL.createObjectURL(file);
        img.src = imageUrl
        img.className = "d-block center";
        img.alt = file.name;
        img.onload = () => URL.revokeObjectURL(imageUrl);

        img.classList.add("object-fit-contain");
        carouselItem.appendChild(img);
        carouselInner.appendChild(carouselItem);
    });
}