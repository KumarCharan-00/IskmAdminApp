import {
    setActive,
    showOutputPanel,
    apiJson,
    createLockHandler,
    dateRangePicker,
} from "./common-util.js";

// Logging configuration
const LOG_LEVELS = {
    DEBUG: "DEBUG",
    INFO: "INFO",
    WARN: "WARN",
    ERROR: "ERROR",
};

const LOG_PREFIX = "[UploadContent]";

// Simple logging utility
function log(level, message, data = null) {
    const timestamp = new Date().toISOString();
    const logMessage = `${timestamp} ${LOG_PREFIX} [${level}] ${message}`;

    switch (level) {
        case LOG_LEVELS.DEBUG:
            console.debug(logMessage, data || "");
            break;
        case LOG_LEVELS.INFO:
            console.info(logMessage, data || "");
            break;
        case LOG_LEVELS.WARN:
            console.warn(logMessage, data || "");
            break;
        case LOG_LEVELS.ERROR:
            console.error(logMessage, data || "");
            break;
        default:
            console.log(logMessage, data || "");
    }
}

function addContent() {
    log(LOG_LEVELS.INFO, "addContent function called");
    setActive("btn-upload");
    showOutputPanel("add-content");
}
window.addContent = addContent;

document.addEventListener("DOMContentLoaded", () => {
    selectedType();
    dateRangePicker();

    // Seva options listeners
    const neverExpiryOption = document.getElementById("sevaNeverExpiryOption");

    if (neverExpiryOption) {
        neverExpiryOption.addEventListener("change", function () {
            this.setAttribute("aria-checked", this.checked);
            const dateContainer = document.getElementById("dateContainer");
            if (dateContainer) {
                if (this.checked) {
                    dateContainer.classList.add("d-none");
                    dateContainer
                        .querySelectorAll("input, textarea")
                        .forEach((i) => {
                            i.disabled = true;
                            i.value = "";
                        });
                } else {
                    dateContainer.classList.remove("d-none");
                    dateContainer
                        .querySelectorAll("input, textarea")
                        .forEach((i) => (i.disabled = false));
                }
            }
        });
    }

    // Initialize with default type
    updateFormFields("Festival");

    // Donation checkbox listener
    const donationCheckbox = document.getElementById("contentDonationOption");
    if (donationCheckbox) {
        donationCheckbox.addEventListener("change", function () {
            this.setAttribute("aria-checked", this.checked);
        });
    }

    // Seva UI logic
    initSevaUI();
});

function initSevaUI() {
    const sevaSelect = document.getElementById("sevaSelect");
    const newSevaName = document.getElementById("newSevaName");
    const subTypeSelect = document.getElementById("sevaSubTypeSelect");
    const newSubTypeContainer = document.getElementById("newSubTypeContainer");
    const generalDonationCheck = document.getElementById("newSubTypeGeneralDonation");
    const amountContainer = document.getElementById("newSubTypeAmountContainer");
    const amountInput = document.getElementById("newSubTypeAmount");

    if (!sevaSelect) return;

    // Load initial sevas
    fetchSevas();

    sevaSelect.addEventListener("change", function() {
        if (this.value === "new") {
            newSevaName.classList.remove("d-none");
            subTypeSelect.disabled = true;
            subTypeSelect.innerHTML = '<option value="new" selected>+ Add New Sub Type</option>';
            newSubTypeContainer.classList.remove("d-none");
        } else {
            newSevaName.classList.add("d-none");
            subTypeSelect.disabled = false;
            fetchSubTypes(this.value);
            newSubTypeContainer.classList.add("d-none");
        }
    });

    subTypeSelect.addEventListener("change", function() {
        if (this.value === "new") {
            newSubTypeContainer.classList.remove("d-none");
        } else {
            newSubTypeContainer.classList.add("d-none");
        }
    });

    generalDonationCheck.addEventListener("change", function() {
        if (this.checked) {
            amountContainer.classList.add("d-none");
            amountInput.value = "";
        } else {
            amountContainer.classList.remove("d-none");
        }
    });
}

function fetchSevas() {
    apiJson("GET", "/api/sevas").then(response => {
        const select = document.getElementById("sevaSelect");
        if (select) {
            select.innerHTML = '<option value="" selected disabled>Select Seva</option>';
            if (response && !response.errorMessage && Array.isArray(response)) {
                response.forEach(seva => {
                    select.innerHTML += `<option value="${seva.id}">${seva.name}</option>`;
                });
            }
            select.innerHTML += '<option value="new">+ Add New Seva</option>';
        }
    }).catch(e => log(LOG_LEVELS.ERROR, "Failed to fetch sevas", e));
}

function fetchSubTypes(sevaId) {
    apiJson("GET", `/api/sevas/${sevaId}/subtypes`).then(response => {
        const select = document.getElementById("sevaSubTypeSelect");
        if (select) {
            select.innerHTML = '<option value="" selected disabled>Select Sub Type</option>';
            if (response && !response.errorMessage && Array.isArray(response)) {
                response.forEach(st => {
                    const amountText = st.isGeneralDonation ? "(General)" : `(₹${st.amount})`;
                    select.innerHTML += `<option value="${st.id}">${st.name} ${amountText}</option>`;
                });
            }
            select.innerHTML += '<option value="new">+ Add New Sub Type</option>';
        }
    }).catch(e => log(LOG_LEVELS.ERROR, "Failed to fetch sub types", e));
}

function selectedType() {
    const typeList = document.querySelector(".type-options");
    const dropdownBtn = document.getElementById("typeDropdownBtn");

    if (!typeList || !dropdownBtn) return;

    const types = typeList.querySelectorAll(".dropdown-item");

    types.forEach((item) => {
        item.addEventListener("click", () => {
            types.forEach((t) => t.classList.remove("selected"));
            item.classList.add("selected");
            const typeName = item.textContent;
            dropdownBtn.textContent = typeName;
            updateFormFields(typeName);
        });
    });
}

function updateFormFields(type) {
    log(LOG_LEVELS.DEBUG, `Updating form fields for type: ${type}`);

    const fields = {
        location: document.getElementById("locationContainer"),
        dates: document.getElementById("dateContainer"),
        donation: document.getElementById("donationContainer"),
        quote: document.getElementById("quoteContainer"),
        preview: document.getElementById("previewContainer"),
        sevaOptions: document.getElementById("sevaOptionsContainer"),
    };

    const toggle = (el, show) => {
        if (!el) return;
        if (show) {
            el.classList.remove("d-none");
            el.querySelectorAll("input, textarea, select").forEach(
                (i) => (i.disabled = false),
            );
        } else {
            el.classList.add("d-none");
            el.querySelectorAll("input, textarea, select").forEach(
                (i) => (i.disabled = true),
            );
        }
    };

    const dateHelpText = document.getElementById("dateHelpText");

    if (type === "Festival") {
        toggle(fields.sevaOptions, false);
        toggle(fields.location, true);
        toggle(fields.dates, true);
        toggle(fields.donation, true);
        toggle(fields.quote, true);
        toggle(fields.preview, true);
        if (dateHelpText) {
            dateHelpText.textContent =
                "These are the days this event is expected to start and stay valid until date. Once event starts even if it is in draft date it will be moved to published state and after end date it will be moved to expired state. For Festival User can see these dates";
        }
    } else if (type === "Seva") {
        toggle(fields.sevaOptions, true);
        toggle(fields.location, false);

        const neverExpiry = document.getElementById("sevaNeverExpiryOption");
        const neverExpiryChecked = neverExpiry ? neverExpiry.checked : false;

        toggle(fields.dates, !neverExpiryChecked);

        toggle(fields.donation, true);
        toggle(fields.quote, true);
        toggle(fields.preview, true);
        if (dateHelpText) {
            dateHelpText.textContent =
                "This start date this event automatically moves to published state if it is in draft and after end date it moves to expired state. User Cannot see these dates";
        }
    } else if (type === "Blog") {
        toggle(fields.sevaOptions, false);
        toggle(fields.location, false);
        toggle(fields.dates, false);
        toggle(fields.donation, true);
        toggle(fields.quote, true);
        toggle(fields.preview, true);
        if (dateHelpText) {
            dateHelpText.textContent = "";
        }
    }
}

function clearContentForm(userConfirmed = false) {
    log(LOG_LEVELS.INFO, "clearContentForm function called");
    const confirmed = userConfirmed
        ? true
        : confirm("Are you sure you want to clear all fields?");
    if (!confirmed) {
        log(LOG_LEVELS.INFO, "User cancelled form clearing");
        return;
    }

    log(LOG_LEVELS.DEBUG, "User confirmed form clearing");
    const form = document.getElementById("contentUploadForm");
    if (form) {
        log(LOG_LEVELS.DEBUG, "Resetting form and clearing images");
        form.reset();
        const imageList = document.getElementById("imageList");
        if (imageList) {
            imageList.innerHTML = "";
            log(LOG_LEVELS.DEBUG, "Cleared image list");
        }
        const imagePreview = document.getElementById("imagePreview");
        if (imagePreview) {
            const carouselInner = imagePreview.querySelector(".carousel-inner");
            if (carouselInner) {
                carouselInner.innerHTML = "";
                log(LOG_LEVELS.DEBUG, "Cleared image preview carousel");
            }
            imagePreview.classList.add("d-none");
        }
        // Reset aria-checked
        const donationCheckbox = document.getElementById(
            "contentDonationOption",
        );
        if (donationCheckbox) {
            donationCheckbox.setAttribute("aria-checked", "false");
        }

        // Reset Seva options aria-checked
        const neverExpiry = document.getElementById("sevaNeverExpiryOption");
        if (neverExpiry) neverExpiry.setAttribute("aria-checked", "false");

        // Reset Seva Options UI
        const sevaSelect = document.getElementById("sevaSelect");
        if (sevaSelect) {
            sevaSelect.value = "";
            document.getElementById("newSevaName").classList.add("d-none");
            document.getElementById("newSevaName").value = "";
            const subTypeSelect = document.getElementById("sevaSubTypeSelect");
            subTypeSelect.innerHTML = '<option value="" selected disabled>Select Sub Type</option><option value="new">+ Add New Sub Type</option>';
            subTypeSelect.disabled = true;
            document.getElementById("newSubTypeContainer").classList.add("d-none");
            document.getElementById("newSubTypeName").value = "";
            document.getElementById("newSubTypeAmount").value = "";
            document.getElementById("newSubTypeGeneralDonation").checked = false;
            document.getElementById("newSubTypeAmountContainer").classList.remove("d-none");
        }

        // Sync DOM states with reset toggles
        const typeBtn = document.getElementById("typeDropdownBtn");
        const type = typeBtn ? typeBtn.textContent.trim() : "Festival";
        updateFormFields(type);

        log(LOG_LEVELS.INFO, "Form cleared successfully");
    } else {
        log(LOG_LEVELS.WARN, "Content upload form not found");
    }
}

function displayImageNames() {
    log(LOG_LEVELS.INFO, "displayImageNames function called");
    const input = document.getElementById("imageUpload");
    const imagePreview = document.getElementById("imagePreview");
    const list = document.getElementById("imageList");

    if (!input) {
        log(LOG_LEVELS.ERROR, "Image upload input not found");
        return;
    }

    log(LOG_LEVELS.DEBUG, "Clearing previous image entries");
    list.innerHTML = ""; // Clear previous entries
    if (imagePreview) {
        const carouselInner = imagePreview.querySelector(".carousel-inner");
        if (carouselInner) {
            carouselInner.innerHTML = "";
            log(LOG_LEVELS.DEBUG, "Cleared image preview carousel");
        }
    }

    Array.from(input.files).forEach((file, idx) => {
        log(LOG_LEVELS.DEBUG, `Processing file ${idx + 1}: ${file.name}`, {
            size: file.size,
            type: file.type,
        });

        const listItem = document.createElement("li");
        listItem.className = "list-group-item";
        listItem.textContent = file.name;
        list.appendChild(listItem);

        const carouselInner = imagePreview.querySelector(".carousel-inner");
        if (carouselInner) {
            const carouselItem = document.createElement("div");
            carouselItem.className = "carousel-item";
            if (idx === 0) carouselItem.classList.add("active");

            const img = document.createElement("img");
            const imageUrl = URL.createObjectURL(file);
            img.src = imageUrl;
            img.className = "d-block center";
            img.alt = file.name;
            img.onload = () => {
                URL.revokeObjectURL(imageUrl);
                log(
                    LOG_LEVELS.DEBUG,
                    `Image loaded and URL revoked for: ${file.name}`,
                );
            };
            img.onerror = () => {
                log(LOG_LEVELS.ERROR, `Failed to load image: ${file.name}`);
                URL.revokeObjectURL(imageUrl);
            };

            img.classList.add("object-fit-scale");
            carouselItem.appendChild(img);
            carouselInner.appendChild(carouselItem);
        }
    });

    if (imagePreview) {
        if (input.files.length > 0) {
            imagePreview.classList.remove("d-none");
        } else {
            imagePreview.classList.add("d-none");
        }
    }

    log(LOG_LEVELS.INFO, `Successfully processed ${input.files.length} images`);
}

// Save as Draft function with lock handler
// Generic save function
async function saveContent(status) {
    const actionName = status === "draft" ? "Save as Draft" : "Save and Upload";
    log(LOG_LEVELS.INFO, `${actionName} function called`);

    const saveHandler = createLockHandler(async () => {
        log(LOG_LEVELS.INFO, `${actionName} clicked - starting process`);

        // Handle Seva / SubType creation first if applicable
        const typeBtn = document.getElementById("typeDropdownBtn");
        const type = typeBtn ? typeBtn.textContent.trim() : "Festival";
        
        let finalSevaId = "";
        let finalSubTypeId = "";

        if (type === "Seva") {
            const sevaSelect = document.getElementById("sevaSelect");
            const subTypeSelect = document.getElementById("sevaSubTypeSelect");
            
            if (!sevaSelect || !sevaSelect.value) {
                showAlert("Please select or create a Seva.", "danger");
                return;
            }

            if (sevaSelect.value === "new") {
                const newName = document.getElementById("newSevaName").value.trim();
                if (!newName) {
                    showAlert("Please enter a name for the new Seva.", "danger");
                    return;
                }
                try {
                    const sevaRes = await apiJson("POST", "/api/sevas", { body: { name: newName } });
                    if (sevaRes && sevaRes.errorMessage) throw new Error(sevaRes.errorMessage);
                    finalSevaId = sevaRes.id;
                } catch (e) {
                    showAlert("Failed to create new Seva.", "danger");
                    return;
                }
            } else {
                finalSevaId = sevaSelect.value;
            }

            if (subTypeSelect.value === "new" || sevaSelect.value === "new") {
                const newSubName = document.getElementById("newSubTypeName").value.trim();
                if (!newSubName) {
                    showAlert("Please enter a name for the new Sub Type.", "danger");
                    return;
                }
                const isGen = document.getElementById("newSubTypeGeneralDonation").checked;
                const amt = document.getElementById("newSubTypeAmount").value;
                if (!isGen && !amt) {
                    showAlert("Please enter an amount or select General Donation.", "danger");
                    return;
                }
                try {
                    const subRes = await apiJson("POST", `/api/sevas/${finalSevaId}/subtypes`, {
                        body: {
                            name: newSubName,
                            amount: isGen ? null : parseFloat(amt),
                            isGeneralDonation: isGen
                        }
                    });
                    if (subRes && subRes.errorMessage) throw new Error(subRes.errorMessage);
                    finalSubTypeId = subRes.id;
                } catch (e) {
                    showAlert("Failed to create new Sub Type.", "danger");
                    return;
                }
            } else {
                if (!subTypeSelect.value) {
                    showAlert("Please select or create a Sub Type.", "danger");
                    return;
                }
                finalSubTypeId = subTypeSelect.value;
            }
        }

        // Validate form data before sending
        log(LOG_LEVELS.DEBUG, `Collecting form data for ${status}`);
        const formData = collectFormData();

        if (type === "Seva") {
            formData.append("sevaId", finalSevaId);
            formData.append("sevaSubTypeId", finalSubTypeId);
        }

        if (!validateFormData(formData)) {
            log(LOG_LEVELS.WARN, `Form validation failed for ${status}`);
            return;
        }

        log(LOG_LEVELS.DEBUG, `Form validation passed for ${status}`);
        formData.append("status", status);

        const headers = {
            Channel: "web", // Assuming web channel for now
        };

        log(LOG_LEVELS.DEBUG, `Preparing API request for ${status}`, {
            headers,
        });

        log(LOG_LEVELS.INFO, `Making API call to save content as ${status}`);
        apiJson("POST", "/content", {
            body: formData,
            headers: headers,
        })
            .then((response) => {
                log(
                    LOG_LEVELS.INFO,
                    `${actionName} API response received`,
                    response,
                );
                if (response && !response.errorMessage) {
                    log(LOG_LEVELS.INFO, `${actionName} successful`);
                    showAlert(
                        `Content ${
                            status === "draft"
                                ? "saved as draft"
                                : "saved and uploaded"
                        } successfully!`,
                        "success",
                    );
                    clearContentForm(true);
                    if (type === "Seva") {
                        fetchSevas(); // Refresh sevas dropdown
                    }
                } else {
                    const errorMsg = response?.errorMessage || "Unknown error";
                    log(LOG_LEVELS.ERROR, `${actionName} failed`, {
                        errorMessage: errorMsg,
                    });
                    showAlert(`Failed to save content: ${errorMsg}`, "danger");
                }
            })
            .catch((error) => {
                log(LOG_LEVELS.ERROR, `${actionName} API call failed`, error);
                showAlert(
                    `Failed to save content. Please try again.`,
                    "danger",
                );
            });
    }, 1000); // 1 second lock to prevent double clicks

    saveHandler();
}

function saveAsDraft() {
    saveContent("draft");
}
window.saveAsDraft = saveAsDraft;

function saveAndUpload() {
    saveContent("published");
}
window.saveAndUpload = saveAndUpload;

// Helper function to collect form data
function collectFormData() {
    log(LOG_LEVELS.DEBUG, "collectFormData function called");
    const form = document.getElementById("contentUploadForm");
    const formData = new FormData();

    if (!form) {
        log(LOG_LEVELS.ERROR, "Content upload form not found");
        return formData;
    }

    // Get Type
    const typeBtn = document.getElementById("typeDropdownBtn");
    const type = typeBtn ? typeBtn.textContent.trim() : "Festival";
    formData.append("type", type);

    // Get title
    const titleInput = document.getElementById("contentTitle");
    if (titleInput && titleInput.value.trim()) {
        formData.append("title", titleInput.value.trim());
    }

    // Get content
    const contentTextarea = document.getElementById("contentBody");
    if (contentTextarea && contentTextarea.value.trim()) {
        formData.append("fullText", contentTextarea.value.trim());
    }

    // Get images
    const imageInput = document.getElementById("imageUpload");
    if (imageInput && imageInput.files.length > 0) {
        Array.from(imageInput.files).forEach((file) => {
            formData.append("images", file);
        });
    }

    // Get dates (if enabled)
    const startDate = document.getElementById("contentStartDate");
    const endDate = document.getElementById("contentEndDate");
    if (startDate && !startDate.disabled && startDate.value) {
        formData.append("showFromDate", startDate.value);
    }
    if (endDate && !endDate.disabled && endDate.value) {
        formData.append("showToDate", endDate.value);
    }

    // Get Location (if enabled)
    const location = document.getElementById("contentLocation");
    if (location && !location.disabled && location.value.trim()) {
        formData.append("location", location.value.trim());
    }

    // Get Donation (if enabled)
    const donation = document.getElementById("contentDonationOption");
    if (donation && !donation.disabled) {
        formData.append("showDonation", donation.checked);
    }

    // Get Quote (if enabled)
    const quote = document.getElementById("contentQuote");
    if (quote && !quote.disabled && quote.value.trim()) {
        formData.append("quote", quote.value.trim());
    }

    // Get Preview (if enabled)
    const preview = document.getElementById("contentPreview");
    if (preview && !preview.disabled && preview.value.trim()) {
        formData.append("shortText", preview.value.trim());
    }

    log(LOG_LEVELS.DEBUG, "Form data collection completed");
    return formData;
}

// Helper function to validate form data
function validateFormData(formData) {
    log(LOG_LEVELS.DEBUG, "validateFormData function called");

    const type = formData.get("type");

    // Check if title is provided
    if (!formData.get("title")) {
        log(LOG_LEVELS.WARN, "Validation failed: Missing page title");
        showAlert("Please enter a Title.", "danger");
        return false;
    }

    // Check if content is provided
    if (!formData.get("fullText")) {
        log(LOG_LEVELS.WARN, "Validation failed: Missing page content");
        showAlert("Please enter Content.", "danger");
        return false;
    }

    if (type === "Festival" || type === "Seva") {
        const datesShown = !document
            .getElementById("dateContainer")
            .classList.contains("d-none");

        if (datesShown) {
            if (!formData.get("showFromDate")) {
                log(LOG_LEVELS.WARN, "Validation failed: Missing start date");
                showAlert("Please select a Start Date.", "danger");
                return false;
            }

            const neverExpiryOption = document.getElementById(
                "sevaNeverExpiryOption",
            );
            const neverExpiryChecked =
                neverExpiryOption && neverExpiryOption.checked;

            if (!neverExpiryChecked && !formData.get("showToDate")) {
                log(LOG_LEVELS.WARN, "Validation failed: Missing end date");
                showAlert("Please select an End Date.", "danger");
                return false;
            }
        }
    }

    // Word count validations
    let isValid = true;
    if (!validateWordCount("contentTitle", 7)) isValid = false;
    if (!validateWordCount("contentQuote", 15)) isValid = false;
    if (!validateWordCount("contentPreview", 20)) isValid = false;

    // Content limit depends on type
    const contentLimit = type === "Blog" ? 200 : 45;
    if (!validateWordCount("contentBody", contentLimit)) isValid = false;

    if (!isValid) {
        log(LOG_LEVELS.WARN, "Validation failed: Word count limit exceeded");
        return false;
    }

    log(LOG_LEVELS.DEBUG, "Form validation passed");
    return true;
}

function validateWordCount(elementId, maxWords) {
    const element = document.getElementById(elementId);
    if (!element || element.disabled) return true;

    const text = element.value.trim();
    if (!text) {
        element.classList.remove("is-invalid");
        return true;
    }

    const wordCount = text.split(/\s+/).filter((w) => w.length > 0).length;

    if (wordCount > maxWords) {
        element.classList.add("is-invalid");
        let feedback = element.parentElement.querySelector(".invalid-feedback");
        if (!feedback) {
            feedback = document.createElement("div");
            feedback.className = "invalid-feedback";
            element.parentElement.appendChild(feedback);
        }
        feedback.textContent = `Max number of words allowed ${maxWords}`;
        return false;
    } else {
        element.classList.remove("is-invalid");
        let feedback = element.parentElement.querySelector(".invalid-feedback");
        if (feedback) {
            feedback.remove();
        }
        return true;
    }
}

function showAlert(message, type = "success") {
    const alertContainer = document.getElementById("alertContainer");
    if (!alertContainer) return;

    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.role = "alert";
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    alertContainer.appendChild(alertDiv);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        const alert = bootstrap.Alert.getOrCreateInstance(alertDiv);
        alert.close();
    }, 5000);
}
