import { setActive, showOutputPanel } from "./common-util.js";

function addContent() {
    setActive('btn-upload');
    showOutputPanel("add-content");
}
window.addContent = addContent;

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

        img.classList.add("object-fit-scale");
        carouselItem.appendChild(img);
        carouselInner.appendChild(carouselItem);
    });
}