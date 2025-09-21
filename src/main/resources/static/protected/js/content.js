/**
 * @param buttonId
 * Sets the state of the specified button to active and removes the active state from other buttons
 */
function setActive(buttonId) {
    const buttons = document.querySelectorAll('.btn-subtle-primary');
    buttons.forEach(btn => btn.classList.remove('active'));
    document.getElementById(buttonId).classList.add('active');
}

/**
 * Sets the state of the "Upload New Content" button to active
 * @function
 * @name uploadContent
 */
function uploadContent() {
    setActive('btn-upload');
    showMessage("add-content");
}


function showUploadedContent(status, startDate, endDate) {
    setActive('btn-show-uploaded');
    showMessage("view-content");
    let filters = {};
    if (status && status !== 'all') filters.status = status;
    if (startDate) filters.from = startDate;
    if (endDate) filters.to = endDate;
    loadContent(filters).then(contentDetailsInTable);
}

function showMessage(action) {
    const output = document.querySelectorAll(".output");
    console.log("Output:", output);
    output.forEach(out =>  out.classList.add('collapse'));
    console.log(action);
    const actionItem = document.getElementById(action);
    if (actionItem) {
        console.log("Action Item:", actionItem);
        actionItem.classList.remove('collapse');
    }
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

        img.classList.add("object-fit-scale");
        carouselItem.appendChild(img);
        carouselInner.appendChild(carouselItem);
    });
}

async function loadContent(filters) {
    const queryParams = processFiltersAsQueryParams(filters);
    console.log(queryParams);
    const url = "/content" + (queryParams ? `?${queryParams}` : "");
    const response = await fetch(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    });
    console.log("Response Status:", response.status);
    console.log(response);
    return await response.json();
}

function processFiltersAsQueryParams(filters) {
    return Object.entries(filters)
        .filter(([, value]) => !value)
        .map(
        ([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
    ).join("&");
}


let contentTableDTInstance;

function contentDetailsInTable(res) {
    try {

        console.log("fulfilled");
        const contentData = document.querySelector(".content-data");
        contentData.innerHTML = "";

        res.content.forEach((val, idx) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <th scope="row">${idx + 1}</th>
                <td>${val.pageTitle}</td>
                <td>${val.status}</td>
                <td></td>
                <td></td>
            `;
            contentData.appendChild(tr);
        });

        // Reinitialize DataTable
        const tableElement = document.getElementById('contentTable');

        if (this.contentTableDTInstance) {
            console.log("destroying table")
            this.contentTableDTInstance.destroy();
        }

        console.log("Initializing DataTable");
        this.contentTableDTInstance = new DataTable(tableElement, {
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
        console.log("error in contentDetailsInTable ", ex);
    }
}

document.addEventListener("DOMContentLoaded", selectedStatus);

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


document.addEventListener('DOMContentLoaded', dateRangePicker);

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

function clearEndDate() {
    document.getElementById("calendarEndDate").value = "";
}
