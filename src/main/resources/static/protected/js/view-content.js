import {
    setActive,
    showOutputPanel,
    apiJson,
    createLockHandler,
    dateRangePicker,
} from "./common-util.js";

// Event Listeners
document.addEventListener("DOMContentLoaded", dateRangePicker);
document.addEventListener("DOMContentLoaded", selectedStatus);

let contentTableDTInstance;

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
                `.badge[data-status="${label}"]`
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
    }, 500)
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
                { data: "pageTitle" },
                { data: "status" },
                { data: "actions", className: "text-center" },
                { data: "images", className: "text-center" },
                { data: "createdAt" },
            ],
            columnDefs: [
                { orderable: false, targets: [3, 4] }, // Disable sorting on action buttons
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
    content: "",
    title: "",
};

function mapToRow(val, idx) {
    if (val) {
        return `
         <th scope="row">${idx + 1}</th>
         <td>${val.pageTitle.trim()}</td>
         <td>${val.status}</td>
         <td>
            <a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewContentModal" 
                onclick=" loadContentInModal(${idx}, '${val.id}', '${
            val.pageTitle
        }', '${val.pageContent}')">view</a>
         </td>
         <td></td>
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
            closeBtnLockHandler(contentModal.content)
        );
        stateBtn.addEventListener("click", () => stateBtnLockHandler());
        saveBtn.addEventListener("click", () =>
            saveBtnLockHandler(
                contentModal.idx,
                contentModal.id,
                contentModal.title,
                contentModal.content
            )
        );
    }
}

function loadContentInModal(idx, id, title, content) {
    const viewContentModal = document.getElementById("viewContentModal");
    const label = viewContentModal.querySelector("#viewContentModalLabel");
    const body = viewContentModal.querySelector("#viewContentModalBody");
    if (label && body) {
        label.textContent = title;
        body.value = content;
    }
    contentModal.idx = idx;
    contentModal.id = id;
    contentModal.title = title;
    contentModal.content = content;
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
    const textArea = document.getElementById("viewContentModalBody");
    const contentBodyEditMode = document.querySelector(".modalStateBtn");
    if (textArea && contentBodyEditMode) {
        textArea.disabled = !textArea.disabled;
        contentBodyEditMode.textContent = textArea.disabled
            ? "Edit"
            : "Read-Only";
        editMode = !textArea.disabled;
        if (editMode) {
            console.log("Adding input event listener");
            document.addEventListener("input", contentModifiedListener, {
                once: true,
            });
        } else {
            console.log("Removing input event listener");
            document.removeEventListener("input", contentModifiedListener);
        }
    }
}
window.switchContentMode = switchContentMode;

function closeContentModal(content) {
    const contentBody = document.getElementById("viewContentModalBody");
    // content = contentBody.value; // for testing
    console.log("content ", content ? content : "IS_EMPTY");
    if (content && contentBody && content !== contentBody.value) {
        const confirmed = confirm(
            "Closing without Saving new changes will result in loss of changes.\nClick on OK if you are sure you want to close?"
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

function saveContentById(idx, contentId, title, content) {
    const bodyNode = document.getElementById("viewContentModalBody");
    const titleNode = document.getElementById("viewContentModalLabel");
    let body = {};
    console.log(title, " == ", titleNode.textContent);
    console.log(content, " == ", bodyNode.value);
    if (content && bodyNode && content !== bodyNode.value) {
        body.pageContent = bodyNode.value;
    }
    if (title && titleNode && title !== titleNode.textContent) {
        body.pageTitle = titleNode.textContent;
    }
    const response = apiJson("PATCH", `/content/${contentId}`, { body });
    response.then((json) => popup(idx, json));
    // return response;
}
window.saveContentById = saveContentById;

function popup(idx, json) {
    console.log(json);
    if (json && !json.errorMessage) {
        contentModal.content = json.pageContent;
        contentModal.title = json.pageTitle;
        const newData = contentTableDTInstance.row(idx).data();
        newData.pageTitle = json.pageTitle;
        newData.actions = `<a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewContentModal"
                            onclick=" loadContentInModal(${idx}, '${
            json.id
        }', '${json.pageTitle.trim()}', '${json.pageContent}')">view</a>`;
        contentTableDTInstance.row(idx).data(newData).draw(false);
        console.log(contentTableDTInstance.row(idx).data());
        console.log(contentTableDTInstance.row(idx).node());
        saveBtnEnabled = false;
        contentModified(false);
    }
}
