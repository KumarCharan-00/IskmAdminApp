const allowedMethods = ["GET", "POST", "PATCH", "DELETE"];

export function setActive(buttonId) {
    const buttons = document.querySelectorAll(".btn-subtle-primary");
    buttons.forEach((btn) => btn.classList.remove("active"));
    document.getElementById(buttonId).classList.add("active");
}

export function showOutputPanel(action) {
    const output = document.querySelectorAll(".output");
    console.log("Output:", output);
    output.forEach((out) => out.classList.add("collapse"));
    console.log(action);
    const actionItem = document.getElementById(action);
    if (actionItem) {
        console.log("Action Item:", actionItem);
        actionItem.classList.remove("collapse");
    }
}

export async function api(method, path, options = {}) {
    if (!path) {
        throw new Error("Path is required");
    }
    if (!method || !allowedMethods.includes(method.toUpperCase())) {
        console.log("Method not passed or Invalid Method. Defaulting to GET");
        method = "GET";
    }
    method = method.toUpperCase();
    const defaultHeaders = {
        "Content-Type": "application/json",
        Accept: "application/json",
    };
    const { body, queryParams, headers = defaultHeaders } = options;

    console.log("Method: ", method);
    let fetchOptions = {
        method: method,
        headers: headers,
    };
    if (method === "GET" || method === "DELETE") {
        if (queryParams) {
            console.log("QueryParams: ", queryParams);
            path = queryParams
                ? `${path}?${new URLSearchParams(queryParams).toString()}`
                : path;
        }
    } else {
        if (body instanceof FormData) {
            console.log("Body: FormData object");
            fetchOptions.body = body;
        } else {
            console.log(
                "Body: ",
                body ? JSON.stringify(body) : "EMPTY_BODY_PASSED",
            );
            fetchOptions.body = body ? JSON.stringify(body) : "";
        }
    }
    return await fetch(path, fetchOptions);
}

export async function apiJson(method, path, options = {}) {
    const response = await api(method, path, options);

    let jsonResponse;
    console.log("Response Headers: ", response.headers);
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        jsonResponse = await response.json();
    } else {
        const text = await response.text();
        try {
            jsonResponse = JSON.parse(text);
        } catch (e) {
            jsonResponse = text;
        }
    }

    const failureResponse = {};
    if (response) {
        if (response.status && response.status.toString().startsWith("2")) {
            console.log("Response Status:", response.status);
            console.log("Response: ", response);
            return jsonResponse;
        } else {
            console.log("Response Status:", response.status);
            console.log(jsonResponse);
            failureResponse.status = response.status;
            failureResponse.errorMessage =
                typeof jsonResponse === "object" && jsonResponse.message
                    ? jsonResponse.message
                    : "01";
            failureResponse.errorDescription = "Internal Server Error";
            return failureResponse;
        }
    }
    return jsonResponse;
}

export function debounce(func, delayInMs = 200) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delayInMs);
    };
}

export function createLockHandler(func, lockTimeInMs = 200) {
    console.log("LockHandler Created for ", func.name);
    let locked = false;
    return (...args) => {
        console.log("LockHandler Invoked");
        if (locked) return;
        console.log("LockHandler timer expired");
        locked = true;
        func.apply(this, args);
        setTimeout(() => (locked = false), lockTimeInMs);
    };
}

export function processQueryParams(queryParams) {
    return Object.entries(queryParams)
        .filter(([, value]) => !value)
        .map(
            ([key, value]) =>
                `${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
        )
        .join("&");
}

export function dateRangePicker() {
    const input = document.querySelectorAll(".calendarInput");
    input.forEach((val) => {
        $(val).datepicker({
            format: "yyyy-mm-dd",
            autoclose: true,
            todayHighlight: true,
        });
    });
}

export function dateISOtoReadableFormat(isoDateStr) {
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
