import {
    setActive,
    showOutputPanel,
    apiJson,
    api,
    createLockHandler,
    dateRangePicker,
} from "./common-util.js";

// Event Listeners
document.addEventListener("DOMContentLoaded", dateRangePicker);
document.addEventListener("DOMContentLoaded", selectedStatus);

let contentTableDTInstance;
let loadedContentData = [];

function viewContent(status, startDate, endDate) {
    setActive("btn-show-uploaded");
    showOutputPanel("view-content");
    let filters = {};
    if (status && status.size > 0) filters.status = status.values().toArray();
    if (startDate) filters.from = startDate;
    if (endDate) filters.to = endDate;
    console.log("final ", filters);
    loadContent(filters).then(populateTable);
}
window.viewContent = viewContent;

function selectedStatus() {
    const statusList = document.querySelector(".status-options"); // Single UL element
    const badgeContainer = document.querySelector(".selected-item-list");

    if (!statusList || !badgeContainer) return;

    const status = statusList.querySelectorAll(".dropdown-item");
    console.log(status);
    status.forEach((item) => {
        console.log("Adding Click");
        item.addEventListener("click", () => {
            item.classList.toggle("selected");
            console.log("Listening Click on ", item.textContent);
            const label = item.textContent.trim().toUpperCase();
            const badgePresent = badgeContainer.querySelector(
                `.badge[data-status="${label}"]`,
            );

            if (item.classList.contains("selected") && !badgePresent) {
                const span = document.createElement("span");
                span.textContent = label;
                span.className =
                    "badge bg-secondary text-bg-secondary me-1 mt-2";
                span.setAttribute("data-status", label);
                badgeContainer.appendChild(span);
            } else if (!item.classList.contains("selected") && badgePresent) {
                badgePresent.remove();
            }
        });
    });
}

function clearStartDate() {
    document.getElementById("calendarStartDate").value = "";
}
window.clearStartDate = clearStartDate;

function clearEndDate() {
    document.getElementById("calendarEndDate").value = "";
}
window.clearEndDate = clearEndDate;

document.getElementById("ApplyFilters").addEventListener(
    "click",
    createLockHandler(() => {
        const status = document
            .querySelector(".selected-item-list")
            .querySelectorAll(".badge");
        const startDate = document.getElementById("calendarStartDate").value;
        const endDate = document.getElementById("calendarEndDate").value;
        const filters = {};
        if (status.length > 0) {
            filters.status = new Set();
            status.forEach((val) => {
                filters.status.add(val.getAttribute("data-status"));
            });
        }
        console.log(filters);
        viewContent(filters.status, startDate, endDate);
    }, 500),
);

function loadContent(filters) {
    return apiJson("GET", "/content", { queryParams: filters });
}

function populateTable(res) {
    try {
        console.log("fulfilled");
        const tableElement = document.getElementById("contentTable");
        const contentData = tableElement.querySelector(".content-data");
        if (contentTableDTInstance) {
            console.log("destroying table");
            contentTableDTInstance.clear().destroy();
            contentData.innerHTML = "";
            // contentTableDTInstance = null;
        }

        if (res.content && res.content.length > 0) {
            loadModalSkeleton();
            loadedContentData = res.content;
            res.content.forEach((val, idx) => {
                console.log(`${idx} and ${val}`);
                console.log(val);
                const tr = document.createElement("tr");
                tr.innerHTML = mapToRow(val, idx);
                contentData.appendChild(tr);
            });
        }

        console.log("Initializing DataTable");
        contentTableDTInstance = new DataTable(tableElement, {
            searchable: true,
            sortable: true,
            paging: true,
            responsive: true,
            columns: [
                { data: "id", className: "text-center" },
                { data: "title" },
                { data: "status" },
                { data: "type" },
                { data: "actions", className: "text-center" },
                { data: "images", className: "text-center" },
                { data: "createdAt" },
            ],
            columnDefs: [
                { orderable: false, targets: [4, 5] }, // Disable sorting on action buttons
            ],
            language: {
                search: "Search Content",
                lengthMenu: "_MENU_   records per page",
                info: "_START_ to _END_ of _TOTAL_ entries",
                infoEmpty: "No Entries Found",
                infoFiltered: "(filtered from _MAX_ total entries)",
                emptyTable: "No data available in table",
                zeroRecords: "No matching records found",
                loadingRecords: "Loading...",
                processing: "Processing...",
            },
            layout: {
                topStart: "pageLength",
                topEnd: "search",
                bottomStart: "info",
                bottomEnd: "paging",
            },
            lengthMenu: [5, 10, 25, 50],
            initComplete: function () {
                const searchComponent = document.querySelector(".dt-search");
                const searchInput = searchComponent.querySelector("input");
                const searchLabel = searchComponent.querySelector("label");
                if (searchComponent && searchInput && searchLabel) {
                    searchInput.classList.add("form-control");
                    searchInput.placeholder = "Search";
                    searchLabel.classList.add("visually-hidden");
                }
            },
        });

        // You can pass options here if needed
    } catch (ex) {
        console.log("error in populateTable ", ex);
    }
}

const contentModal = {
    idx: -1,
    id: "",
    title: "",
    previewText: "",
    fullText: "",
    quote: "",
    type: "",
    showDonation: false,
};

function mapToRow(val, idx) {
    if (val) {
        return `
         <th scope="row">${idx + 1}</th>
         <td>${val.title ? val.title.trim() : ""}</td>
         <td>${val.status}</td>
         <td>${val.type}</td>
         <td>
            <a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewContentModal" 
                onclick="loadContentInModal(${idx})">view</a>
         </td>
         <td>
            <a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewImagesModal" 
                onclick="loadImagesInModal(${idx})">view</a>
         </td>
         <td>${dateISOtoReadableFormat(val.createdAt)}</td>
        `;
    } else {
        console.log("Invalid value ", val);
        return "";
    }
}

function dateISOtoReadableFormat(isoDateStr) {
    const date = new Date(isoDateStr);

    const options = {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "numeric",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
    };
    return date.toLocaleString("en-US", options);
}

function loadModalSkeleton() {
    const footer = document.querySelector(".modal-footer");
    const closeBtn = footer.querySelector(".modalCloseBtn");
    const stateBtn = footer.querySelector(".modalStateBtn");
    const saveBtn = footer.querySelector(".saveModifiedContentBtn");
    if (closeBtn && stateBtn && saveBtn) {
        let stateBtnLockHandler = createLockHandler(switchContentMode, 100);
        let closeBtnLockHandler = createLockHandler(closeContentModal, 200);
        let saveBtnLockHandler = createLockHandler(saveContentById, 1000);
        closeBtn.addEventListener("click", () =>
            closeBtnLockHandler(contentModal.content),
        );
        stateBtn.addEventListener("click", () => stateBtnLockHandler());
        saveBtn.addEventListener("click", () =>
            saveBtnLockHandler(contentModal.idx, contentModal.id),
        );
    }
}

function loadContentInModal(idx) {
    const val = loadedContentData[idx];
    const viewContentModal = document.getElementById("viewContentModal");
    const label = viewContentModal.querySelector("#viewContentModalLabel");
    const bodyContainer = document.getElementById(
        "viewContentModalDynamicBody",
    );
    console.log("val", val);
    console.log("label", label);
    console.log("bodyContainer", bodyContainer);
    if (label && bodyContainer) {
        label.textContent = val.title || "";
        bodyContainer.innerHTML = "";

        let previewDiv = document.createElement("div");
        previewDiv.innerHTML = `
                <label for="viewContentModalPreview" class="form-label fw-bold">Short Text (Preview)</label>
                <textarea class="modal-body form-control" id="viewContentModalPreview" style="height: 4rem" disabled>${val.previewText || ""}</textarea>
            `;
        bodyContainer.appendChild(previewDiv);

        let quoteDiv = document.createElement("div");
        quoteDiv.innerHTML = `
                <label for="viewContentModalQuote" class="form-label fw-bold">Quote</label>
                <textarea class="modal-body form-control" id="viewContentModalQuote" style="height: 4rem" disabled>${val.quote || ""}</textarea>
            `;
        bodyContainer.appendChild(quoteDiv);

        let contentDiv = document.createElement("div");
        contentDiv.innerHTML = `
                <label for="viewContentModalFullText" class="form-label fw-bold">Long Text (Content)</label>
                <textarea class="modal-body form-control" id="viewContentModalFullText" style="height: 10rem" disabled>${val.fullText || ""}</textarea>
            `;
        bodyContainer.appendChild(contentDiv);

        let donationDiv = document.createElement("div");
        donationDiv.className = "form-check form-switch mt-3";
        donationDiv.innerHTML = `
            <input class="form-check-input" type="checkbox" role="switch" id="viewContentModalShowDonation" ${val.showDonation ? "checked" : ""} disabled>
            <label class="form-check-label fw-bold" for="viewContentModalShowDonation">Show donation option</label>
        `;
        bodyContainer.appendChild(donationDiv);

        if (
            val.type.toUpperCase() === "FESTIVAL" ||
            val.type.toUpperCase() === "SEVA" ||
            val.type.toUpperCase() === "BLOG"
        ) {
        }

        if (
            val.type.toUpperCase() === "FESTIVAL" ||
            val.type.toUpperCase() === "SEVA" ||
            val.type.toUpperCase() === "BLOG"
        ) {
        }

        if (
            val.type.toUpperCase() === "FESTIVAL" ||
            val.type.toUpperCase() === "SEVA" ||
            val.type.toUpperCase() === "BLOG"
        ) {
        }
    }

    contentModal.idx = idx;
    contentModal.id = val.id;
    contentModal.title = val.title || "";
    contentModal.previewText = val.previewText || "";
    contentModal.fullText = val.fullText || "";
    contentModal.quote = val.quote || "";
    contentModal.type = val.type;
    contentModal.showDonation = val.showDonation || false;

    console.log("contentModal", contentModal);
}
window.loadContentInModal = loadContentInModal;

let saveBtnEnabled = false;

function editContentModalTitle(edited) {
    const contentModal = document.getElementById("viewContentModal");
    const label = contentModal.querySelector("#viewContentModalLabel");
    const inputGrp = contentModal.querySelector("#viewContentModalInputGrp");
    const input = inputGrp.querySelector("#viewContentModalInput");
    const iconCont = contentModal.querySelector("#editContentModalTitleIcon");
    if (inputGrp && label && input && iconCont) {
        if (edited !== undefined && edited) {
            if (!saveBtnEnabled && label.textContent !== input.value) {
                contentModified(true);
                saveBtnEnabled = true;
            }
            label.textContent = input.value;
        }
        input.value = label.textContent;
        inputGrp.classList.toggle("visually-hidden");
        label.classList.toggle("visually-hidden");
        input.readOnly = !input.readOnly;
        iconCont.classList.toggle("visually-hidden");
    }
}
window.editContentModalTitle = editContentModalTitle;

function contentModified(flag) {
    if (typeof flag === "boolean") {
        const btn = document.querySelector(".saveModifiedContentBtn");
        btn.disabled = !flag;
        btn.classList.toggle("btn-outline-secondary");
        btn.classList.toggle("btn-outline-primary");
    }
}

let contentModifiedListener = () => {
    if (!saveBtnEnabled) {
        contentModified(true);
        saveBtnEnabled = true;
    }
};

let editMode = false;

function switchContentMode() {
    const disableableElements = document.querySelectorAll(
        "#viewContentModalDynamicBody textarea, #viewContentModalDynamicBody input",
    );
    const contentBodyEditMode = document.querySelector(".modalStateBtn");
    if (disableableElements.length > 0 && contentBodyEditMode) {
        let isDisabled = disableableElements[0].disabled;
        disableableElements.forEach((el) => (el.disabled = !isDisabled));

        contentBodyEditMode.textContent = isDisabled ? "Read-Only" : "Edit";

        editMode = isDisabled;
        if (editMode) {
            console.log("Adding input event listener");
            document.addEventListener("input", contentModifiedListener, {
                once: true,
            });
            document.addEventListener("change", contentModifiedListener, {
                once: true,
            });
        } else {
            console.log("Removing input event listener");
            document.removeEventListener("input", contentModifiedListener);
            document.removeEventListener("change", contentModifiedListener);
        }
    }
}
window.switchContentMode = switchContentMode;

function closeContentModal() {
    const previewArea = document.getElementById("viewContentModalPreview");
    const fullTextArea = document.getElementById("viewContentModalFullText");
    const quoteArea = document.getElementById("viewContentModalQuote");
    const donationSwitch = document.getElementById(
        "viewContentModalShowDonation",
    );

    let hasChanges = false;
    if (previewArea && contentModal.previewText !== previewArea.value)
        hasChanges = true;
    if (fullTextArea && contentModal.fullText !== fullTextArea.value)
        hasChanges = true;
    if (quoteArea && contentModal.quote !== quoteArea.value) hasChanges = true;
    if (donationSwitch && contentModal.showDonation !== donationSwitch.checked)
        hasChanges = true;

    if (hasChanges) {
        const confirmed = confirm(
            "Closing without Saving new changes will result in loss of changes.\nClick on OK if you are sure you want to close?",
        );
        if (!confirmed) {
            return;
        }
    }
    if (editMode) {
        switchContentMode();
    }
    const modal = document.getElementById("viewContentModal");
    const modalInstance = window.bootstrap.Modal.getInstance(modal);
    console.log(modalInstance);
    if (modalInstance) {
        modalInstance.hide();
    }
}
window.closeContentModal = closeContentModal;

function saveContentById(idx, contentId) {
    const titleNode = document.getElementById("viewContentModalLabel");
    const previewArea = document.getElementById("viewContentModalPreview");
    const fullTextArea = document.getElementById("viewContentModalFullText");
    const quoteArea = document.getElementById("viewContentModalQuote");
    const donationSwitch = document.getElementById(
        "viewContentModalShowDonation",
    );

    let body = {};
    if (titleNode && contentModal.title !== titleNode.textContent) {
        body.title = titleNode.textContent;
    }
    if (previewArea && contentModal.previewText !== previewArea.value) {
        body.previewText = previewArea.value;
    }
    if (fullTextArea && contentModal.fullText !== fullTextArea.value) {
        body.fullText = fullTextArea.value;
    }
    if (quoteArea && contentModal.quote !== quoteArea.value) {
        body.quote = quoteArea.value;
    }
    if (
        donationSwitch &&
        contentModal.showDonation !== donationSwitch.checked
    ) {
        body.showDonation = donationSwitch.checked;
    }

    const response = apiJson("PATCH", `/content/${contentId}`, { body });
    response.then((json) => popup(idx, json));
    // return response;
}
window.saveContentById = saveContentById;

function popup(idx, json) {
    console.log(json);
    if (json && !json.errorMessage) {
        contentModal.previewText = json.previewText;
        contentModal.fullText = json.fullText;
        contentModal.quote = json.quote;
        contentModal.title = json.title;
        if (json.showDonation !== undefined)
            contentModal.showDonation = json.showDonation;

        loadedContentData[idx] = Object.assign(
            {},
            loadedContentData[idx],
            json,
        );

        const newData = contentTableDTInstance.row(idx).data();
        newData.title = json.title;
        newData.actions = `<a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewContentModal"
                            onclick="loadContentInModal(${idx})">view</a>`;
        newData.images = `<a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewImagesModal"
                            onclick="loadImagesInModal(${idx})">images</a>`;
        contentTableDTInstance.row(idx).data(newData).draw(false);
        console.log(contentTableDTInstance.row(idx).data());
        console.log(contentTableDTInstance.row(idx).node());
        saveBtnEnabled = false;
        contentModified(false);
    }
}

// ----------------- image operations -----------------

let currentImageModalIdx = -1;

function loadImagesInModal(idx) {
    currentImageModalIdx = idx;
    const val = loadedContentData[idx];
    console.log("Images val: ", val);
    renderImagesTable(val.id, val.images || []);
    const btnUpload = document.getElementById("btnUploadNewImages");
    if (btnUpload) {
        btnUpload.onclick = () => uploadNewImages(val.id);
    }
}
window.loadImagesInModal = loadImagesInModal;

function renderImagesTable(contentId, images) {
    const bodyContainer = document.getElementById("viewImagesModalDynamicBody");
    if (!bodyContainer) return;

    if (!images || images.length === 0) {
        bodyContainer.innerHTML =
            "<p class='text-muted'>No images found for this content.</p>";
        return;
    }

    bodyContainer.innerHTML = images
        .map(
            (img) => `
            <div class="card" style="width: 12rem;">
                <img src="data:image/jpeg;base64,${img.imageData}" class="card-img-top" style="height: 10rem; object-fit: cover;" alt="Image">
                <div class="card-body text-center p-2">
                    <button class="btn btn-outline-danger btn-sm w-100" onclick="deleteExistingImage('${img.id}', '${contentId}')">Delete</button>
                </div>
            </div>`,
        )
        .join("");
}

function deleteExistingImage(imageId, contentId) {
    if (!confirm("Are you sure you want to delete this image?")) return;

    api("DELETE", `/images/${imageId}`, {
        method: "DELETE",
    })
        .then((res) => {
            console.log("Delete response: ", res);
            if (res?.status?.toString().startsWith("2")) {
                alert("Image deleted successfully!");
                // Refresh images list from API
                refreshImagesForCurrentContent(contentId);
            } else {
                alert("Failed to delete image.");
            }
        })
        .catch((e) => {
            console.error("Delete error", e);
            alert("Error while deleting image.");
        });
}
window.deleteExistingImage = deleteExistingImage;

function uploadNewImages(contentId) {
    const input = document.getElementById("newImagesUpload");
    if (!input || !input.files || input.files.length === 0) {
        alert("Please select images to upload first.");
        return;
    }

    const formData = new FormData();
    for (let i = 0; i < input.files.length; i++) {
        formData.append("images", input.files[i]);
    }

    console.log("Uploading images for content id: ", contentId);
    console.log("Form data: ", formData);
    api("POST", `/images/${contentId}`, {
        method: "POST",
        body: formData,
    })
        .then(async (response) => {
            if (response?.status?.toString().startsWith("2")) {
                alert("Images uploaded successfully!");
                input.value = ""; // Clear selection
                refreshImagesForCurrentContent(contentId);
            } else {
                alert("Failed to upload images: " + (await response.text()));
            }
        })
        .catch((e) => {
            console.error("Upload error", e);
            alert("Error while uploading images");
        });
}

function refreshImagesForCurrentContent(contentId) {
    console.log("Current Image Modal Id: ", currentImageModalIdx);
    if (currentImageModalIdx === -1) return;
    console.log("Refreshing images for content id: ", contentId);
    apiJson("GET", `/images/${contentId}`).then((images) => {
        // Handle no content (API returns null or error if empty sometimes, or empty array)
        if (images && images.errorMessage) {
            loadedContentData[currentImageModalIdx].images = [];
            renderImagesTable(contentId, []);
        } else {
            const safeImages = Array.isArray(images) ? images : [];
            loadedContentData[currentImageModalIdx].images = safeImages;
            renderImagesTable(contentId, safeImages);
        }
    });
}
