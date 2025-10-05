import { setActive, showOutputPanel, apiJson, createLockHandler } from "./common-util.js";

// Event Listeners
document.addEventListener('DOMContentLoaded', dateRangePicker);
document.addEventListener("DOMContentLoaded", selectedStatus);

let contentTableDTInstance;

function viewContent(status, startDate, endDate) {
    setActive('btn-show-uploaded');
    showOutputPanel("view-content");
    let filters = {};
    if (status && status !== 'all') filters.status = status;
    if (startDate) filters.from = startDate;
    if (endDate) filters.to = endDate;
    loadContent(filters).then(populateTable);
}
window.viewContent = viewContent;

function selectedStatus() {
    const statusList = document.querySelector(".status-options"); // Single UL element
    const badgeContainer = document.querySelector(".selected-item-list");

    if (!statusList || !badgeContainer) return;

    const status = statusList.querySelectorAll(".dropdown-item");
    console.log(status);
    status.forEach(item => {
        console.log("Adding Click");
        item.addEventListener("click", () => {
            item.classList.toggle("selected");
            console.log("Listening Click on ", item.textContent);
            const label = item.textContent.trim().toUpperCase();
            const badgePresent = badgeContainer.querySelector(`.badge[data-status="${label}"]`);

            if (item.classList.contains("selected") && !badgePresent) {
                const span = document.createElement("span");
                span.textContent = label;
                span.className = "badge bg-secondary text-bg-secondary me-1 mt-2";
                span.setAttribute("data-status", label);
                badgeContainer.appendChild(span);
            } else if (!item.classList.contains("selected") && badgePresent) {
                badgePresent.remove();
            }
        });
    });
}

function dateRangePicker() {
    const input = document.querySelectorAll('.calendarInput');
    input.forEach(val => {
        $(val).datepicker({
            format: 'yyyy-mm-dd',
            autoclose: true,
            todayHighlight: true
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

function loadContent(filters) {
    return apiJson("GET", "/content", { queryParams: filters });
}

let data = new Map();

function populateTable(res) {
    try {
        console.log("fulfilled");
        const contentData = document.querySelector(".content-data");
        contentData.innerHTML = "";
        if (res.content && res.content.length > 0) {
            loadModalSkeleton();
            res.content.forEach((val, idx) => {
                data.set(val.id, val);
                console.log(`${idx} and ${data.get(val.id)}`)
                console.log(data.get(val.id));
                const tr = document.createElement('tr');
                tr.innerHTML = mapToRow(data.get(val.id), idx);
                contentData.appendChild(tr);
            });
        }

        // Reinitialize DataTable
        const tableElement = document.getElementById('contentTable');

        if (contentTableDTInstance) {
            console.log("destroying table")
            contentTableDTInstance.destroy();
        }

        console.log("Initializing DataTable");
        contentTableDTInstance = new DataTable(tableElement, {
            searchable: true,
            sortable: true,
            paging: true,
            responsive: true,
            columnDefs: [
                { orderable: false, targets: [3, 4] } // Disable sorting on action buttons
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
                processing: "Processing..."
            },
            layout: {
                topStart: 'pageLength',
                topEnd: 'search',
                bottomStart: 'info',
                bottomEnd: 'paging'
            },
            lengthMenu: [5, 10, 25, 50],
            initComplete: function () {
                const searchComponent = document.querySelector('.dt-search');
                const searchInput = searchComponent.querySelector('input');
                const searchLabel = searchComponent.querySelector('label');
                if (searchComponent && searchInput && searchLabel) {
                    searchInput.classList.add('form-control');
                    searchInput.placeholder = "Search";
                    searchLabel.classList.add('visually-hidden');
                }
            }
        });

        // You can pass options here if needed
    } catch(ex) {
        console.log("error in populateTable ", ex);
    }
}

const contentModal = {
    id: "",
    content: "",
    title: ""
}

function mapToRow(val, idx) {
    if (val) {
        return `
         <th scope="row">${idx + 1}</th>
         <td>${val.pageTitle.trim()}</td>
         <td>${val.status}</td>
         <td>
            <a class="" href="#" data-bs-toggle="modal" data-bs-target="#viewContentModal" 
                onclick="loadContentInModal('${val.id}', '${val.pageTitle}', '${val.pageContent}')">view</a>
         </td>
         <td></td>
        `;
    } else {
        console.log("Invalid value ", val);
        return '';
    }
}

function loadModalSkeleton() {
    const footer = document.querySelector(".modal-footer");
    const closeBtn = footer.querySelector(".modalCloseBtn");
    const stateBtn = footer.querySelector(".modalStateBtn");
    const saveBtn = footer.querySelector(".saveModifiedContentBtn");
    if (closeBtn && stateBtn && saveBtn) {
        let stateBtnLockHandler = createLockHandler(enableContentEditMode, 100);
        let closeBtnLockHandler = createLockHandler(closeContentModal, 200);
        let saveBtnLockHandler = createLockHandler(saveContentById, 1000);
        closeBtn.addEventListener("click", () => closeBtnLockHandler(contentModal.content));
        stateBtn.addEventListener("click", () => stateBtnLockHandler());
        saveBtn.addEventListener("click", () => saveBtnLockHandler(contentModal.id, contentModal.content));
    }
}

function loadContentInModal(id, title, content) {
    const viewContentModal = document.getElementById("viewContentModal");
    const label = viewContentModal.querySelector("#viewContentModalLabel");
    const body = viewContentModal.querySelector("#viewContentModalBody");
    if (label && body) {
        label.textContent = title;
        body.value = content;
    }
    contentModal.id = id;
    contentModal.title = title;
    contentModal.content = content;
}
window.loadContentInModal = loadContentInModal;

let titleChanged = false;

function editContentModalTitle(edited) {
    const contentModal = document.getElementById("viewContentModal");
    const label = contentModal.querySelector("#viewContentModalLabel");
    const inputGrp = contentModal.querySelector("#viewContentModalInputGrp");
    const input = inputGrp.querySelector("#viewContentModalInput");
    const iconCont = contentModal.querySelector("#editContentModalTitleIcon");
    if (inputGrp && label && input && iconCont) {
        if (edited !== undefined && edited) {
            if (!titleChanged && (label.textContent !== input.value)) {
                contentModified(true);
                titleChanged = true;
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

let contentModifiedListener = () => { console.log("listener"); contentModified(true); }

function enableContentEditMode() {
    const textArea = document.getElementById("viewContentModalBody");
    const contentBodyEditMode = document.querySelector(".modalStateBtn");
    if (textArea && contentBodyEditMode) {
        textArea.disabled = !textArea.disabled;
        contentBodyEditMode.textContent = textArea.disabled? "Edit": "Read-Only";
        if (!textArea.disabled) {
            console.log("Adding input event listener");
            document.addEventListener("input", contentModifiedListener, { once: true });
        } else {
            console.log("Removing input event listener");
            document.removeEventListener("input", contentModifiedListener);
        }
    }
}
window.enableContentEditMode = enableContentEditMode;

function closeContentModal(content) {
    const contentBody = document.getElementById("viewContentModalBody");
    // content = contentBody.value; // for testing
    console.log("content ", content? content: "IS_EMPTY");
    if (content && contentBody && content !== contentBody.value) {
        const confirmed = confirm("Closing without Saving new changes will result in loss of changes.\nClick on OK if you are sure you want to close?");
        if (!confirmed) {
            return;
        }
    }
    /* console.log("Removing input event listener");
    document.removeEventListener("input", contentModifiedListener); */
    const modal = document.getElementById("viewContentModal");
    const modalInstance = window.bootstrap.Modal.getInstance(modal);
    console.log(modalInstance);
    if (modalInstance) {
        modalInstance.hide();
    }
}
window.closeContentModal = closeContentModal;

function saveContentById(contentId, content, title) {
    const newContent = document.getElementById("viewContentModalBody");
    const newTitle = document.getElementById("viewContentModalLabel").textContent;
    let body = {};
    if (content && newContent && content !== newContent.value) {
        body.pageContent = newContent.value;
    }
    if (title && newTitle && title !== newTitle) {
        body.pageTitle = newTitle;
    }
    const response = apiJson("PATCH", `/content/${contentId}`, { body })
    response.then(json => popup(json));
    // return response;
}
window.saveContentById = saveContentById

function popup(json) {
    console.log(json);
    if (json && !json.errorMessage) {
        data.set(json.id, json);
        contentModified(false);
    }
}
