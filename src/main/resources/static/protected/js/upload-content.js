import { setActive, showOutputPanel, apiJson, createLockHandler } from "./common-util.js";

// Logging configuration
const LOG_LEVELS = {
    DEBUG: 'DEBUG',
    INFO: 'INFO',
    WARN: 'WARN',
    ERROR: 'ERROR'
};

const LOG_PREFIX = '[UploadContent]';

// Simple logging utility
function log(level, message, data = null) {
    const timestamp = new Date().toISOString();
    const logMessage = `${timestamp} ${LOG_PREFIX} [${level}] ${message}`;
    
    switch (level) {
        case LOG_LEVELS.DEBUG:
            console.debug(logMessage, data || '');
            break;
        case LOG_LEVELS.INFO:
            console.info(logMessage, data || '');
            break;
        case LOG_LEVELS.WARN:
            console.warn(logMessage, data || '');
            break;
        case LOG_LEVELS.ERROR:
            console.error(logMessage, data || '');
            break;
        default:
            console.log(logMessage, data || '');
    }
}

function addContent() {
    log(LOG_LEVELS.INFO, 'addContent function called');
    setActive('btn-upload');
    showOutputPanel("add-content");
}
window.addContent = addContent;

function clearContentForm() {
    log(LOG_LEVELS.INFO, 'clearContentForm function called');
    const confirmed = confirm("Are you sure you want to clear all fields?");
    if (!confirmed) {
        log(LOG_LEVELS.INFO, 'User cancelled form clearing');
        return;
    }
    
    log(LOG_LEVELS.DEBUG, 'User confirmed form clearing');
    const form = document.getElementById("contentUploadForm");
    if (form) {
        log(LOG_LEVELS.DEBUG, 'Resetting form and clearing images');
        form.reset();
        const imageList = document.getElementById("imageList");
        if (imageList) {
            imageList.innerHTML = "";
            log(LOG_LEVELS.DEBUG, 'Cleared image list');
        }
        const imagePreview = document.getElementById("imagePreview");
        if (imagePreview) {
            const carouselInner = imagePreview.querySelector(".carousel-inner");
            if (carouselInner) {
                carouselInner.innerHTML = "";
                log(LOG_LEVELS.DEBUG, 'Cleared image preview carousel');
            }
        }
        log(LOG_LEVELS.INFO, 'Form cleared successfully');
    } else {
        log(LOG_LEVELS.WARN, 'Content upload form not found');
    }
}

function displayImageNames() {
    log(LOG_LEVELS.INFO, 'displayImageNames function called');
    const input = document.getElementById("imageUpload");
    const imagePreview = document.getElementById("imagePreview");
    const list = document.getElementById("imageList");
    
    if (!input) {
        log(LOG_LEVELS.ERROR, 'Image upload input not found');
        return;
    }
    
    log(LOG_LEVELS.DEBUG, 'Clearing previous image entries');
    list.innerHTML = ""; // Clear previous entries
    if (imagePreview) {
        const carouselInner = imagePreview.querySelector(".carousel-inner");
        if (carouselInner) {
            carouselInner.innerHTML = "";
            log(LOG_LEVELS.DEBUG, 'Cleared image preview carousel');
        }
    }

    Array.from(input.files).forEach((file, idx) => {
        log(LOG_LEVELS.DEBUG, `Processing file ${idx + 1}: ${file.name}`, { size: file.size, type: file.type });
        
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
                log(LOG_LEVELS.DEBUG, `Image loaded and URL revoked for: ${file.name}`);
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
    
    log(LOG_LEVELS.INFO, `Successfully processed ${files.length} images`);
}

// Save as Draft function with lock handler
function saveAsDraft() {
    log(LOG_LEVELS.INFO, 'saveAsDraft function called');
    const saveAsDraftHandler = createLockHandler(() => {
        log(LOG_LEVELS.INFO, "Save as Draft clicked - starting process");
        
        // Validate form data before sending
        log(LOG_LEVELS.DEBUG, 'Collecting form data for draft save');
        const formData = collectFormData();
        log(LOG_LEVELS.DEBUG, 'Form data collected', {
            hasTitle: !!formData.get('pageTitle'),
            hasContent: !!formData.get('pageContent'),
            imageCount: formData.getAll('webImages').length,
            hasStartDate: !!formData.get('startDate'),
            hasEndDate: !!formData.get('endDate')
        });
        
        if (!validateFormData(formData)) {
            log(LOG_LEVELS.WARN, 'Form validation failed for draft save');
            return;
        }
        
        log(LOG_LEVELS.DEBUG, 'Form validation passed for draft save');
        formData.append('status', 'draft');
        
        const headers = {
            'Channel': 'web' // Assuming web channel for now
        };
        
        log(LOG_LEVELS.DEBUG, 'Preparing API request for draft save', { headers });
        
        log(LOG_LEVELS.INFO, 'Making API call to save content as draft');
        apiJson("POST", "/content", {
            body: formData,
            headers: headers
        }).then(response => {
            log(LOG_LEVELS.INFO, "Save as Draft API response received", response);
            if (response && !response.errorMessage) {
                log(LOG_LEVELS.INFO, 'Draft save successful');
                alert("Content saved as draft successfully!");
                clearContentForm();
            } else {
                const errorMsg = response?.errorMessage || "Unknown error";
                log(LOG_LEVELS.ERROR, 'Draft save failed', { errorMessage: errorMsg });
                alert("Failed to save content: " + errorMsg);
            }
        }).catch(error => {
            log(LOG_LEVELS.ERROR, "Save as Draft API call failed", error);
            alert("Failed to save content as draft. Please try again.");
        });
    }, 1000); // 1 second lock to prevent double clicks
    
    saveAsDraftHandler();
}
window.saveAsDraft = saveAsDraft;

// Save and Upload function with lock handler
function saveAndUpload() {
    log(LOG_LEVELS.INFO, 'saveAndUpload function called');
    const saveAndUploadHandler = createLockHandler(() => {
        log(LOG_LEVELS.INFO, "Save and Upload clicked - starting process");
        
        // Validate form data before sending
        log(LOG_LEVELS.DEBUG, 'Collecting form data for publish');
        const formData = collectFormData();
        log(LOG_LEVELS.DEBUG, 'Form data collected', {
            hasTitle: !!formData.get('pageTitle'),
            hasContent: !!formData.get('pageContent'),
            imageCount: formData.getAll('webImages').length,
            hasStartDate: !!formData.get('startDate'),
            hasEndDate: !!formData.get('endDate')
        });
        
        if (!validateFormData(formData)) {
            log(LOG_LEVELS.WARN, 'Form validation failed for publish');
            return;
        }
        
        log(LOG_LEVELS.DEBUG, 'Form validation passed for publish');
        formData.append('status', 'published');
        
        const headers = {
            'Channel': 'web' // Assuming web channel for now
        };
        
        log(LOG_LEVELS.DEBUG, 'Preparing API request for publish', { headers });
        
        log(LOG_LEVELS.INFO, 'Making API call to save and publish content');
        apiJson("POST", "/content", {
            body: formData,
            headers: headers
        }).then(response => {
            log(LOG_LEVELS.INFO, "Save and Upload API response received", response);
            if (response && !response.errorMessage) {
                log(LOG_LEVELS.INFO, 'Publish successful');
                alert("Content saved and uploaded successfully!");
                clearContentForm();
            } else {
                const errorMsg = response?.errorMessage || "Unknown error";
                log(LOG_LEVELS.ERROR, 'Publish failed', { errorMessage: errorMsg });
                alert("Failed to save and upload content: " + errorMsg);
            }
        }).catch(error => {
            log(LOG_LEVELS.ERROR, "Save and Upload API call failed", error);
            alert("Failed to save and upload content. Please try again.");
        });
    }, 1000); // 1 second lock to prevent double clicks
    
    saveAndUploadHandler();
}
window.saveAndUpload = saveAndUpload;

// Helper function to collect form data
function collectFormData() {
    log(LOG_LEVELS.DEBUG, 'collectFormData function called');
    const form = document.getElementById("contentUploadForm");
    const formData = new FormData();
    
    if (!form) {
        log(LOG_LEVELS.ERROR, 'Content upload form not found');
        return formData;
    }
    
    // Get title
    const titleInput = document.getElementById("floatingTitleLabel");
    if (titleInput && titleInput.value.trim()) {
        const title = titleInput.value.trim();
        formData.append('pageTitle', title);
        log(LOG_LEVELS.DEBUG, 'Title collected', { title });
    } else {
        log(LOG_LEVELS.DEBUG, 'No title provided');
    }
    
    // Get content
    const contentTextarea = document.getElementById("floatingTextArea");
    if (contentTextarea && contentTextarea.value.trim()) {
        const content = contentTextarea.value.trim();
        formData.append('pageContent', content);
        log(LOG_LEVELS.DEBUG, 'Content collected', { contentLength: content.length });
    } else {
        log(LOG_LEVELS.DEBUG, 'No content provided');
    }
    
    // Get images
    const imageInput = document.getElementById("imageUpload");
    if (imageInput && imageInput.files.length > 0) {
        const files = Array.from(imageInput.files);
        log(LOG_LEVELS.DEBUG, `Processing ${files.length} image files`, files.map(f => ({ name: f.name, size: f.size, type: f.type })));
        // Add images as webImages (you can modify this based on your needs)
        files.forEach(file => {
            formData.append('webImages', file);
        });
    } else {
        log(LOG_LEVELS.DEBUG, 'No images provided');
    }
    
    // Get dates
    const startDate = document.getElementById("calendarStartDate");
    const endDate = document.getElementById("calendarEndDate");
    if (startDate && startDate.value) {
        formData.append('startDate', startDate.value);
        log(LOG_LEVELS.DEBUG, 'Start date collected', { startDate: startDate.value });
    }
    if (endDate && endDate.value) {
        formData.append('endDate', endDate.value);
        log(LOG_LEVELS.DEBUG, 'End date collected', { endDate: endDate.value });
    }
    
    log(LOG_LEVELS.DEBUG, 'Form data collection completed');
    return formData;
}

// Helper function to validate form data
function validateFormData(formData) {
    log(LOG_LEVELS.DEBUG, 'validateFormData function called');
    
    // Check if title is provided
    if (!formData.get('pageTitle')) {
        log(LOG_LEVELS.WARN, 'Validation failed: Missing page title');
        alert("Please enter a page title.");
        return false;
    }
    
    // Check if content is provided
    if (!formData.get('pageContent')) {
        log(LOG_LEVELS.WARN, 'Validation failed: Missing page content');
        alert("Please enter page content.");
        return false;
    }
    
    log(LOG_LEVELS.DEBUG, 'Form validation passed');
    return true;
}