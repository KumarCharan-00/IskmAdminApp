document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");
    const messageDiv = document.getElementById("message");
    const submitButton = form.querySelector("button[type='submit']");

    if (!form) {
        console.error("loginForm not found in DOM");
        return;
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        console.log("Inside JS");

        // Clear previous messages
        messageDiv.textContent = "";

        // Button responsiveness (loading state)
        const originalButtonText = submitButton.textContent;
        submitButton.textContent = "Logging in...";
        submitButton.disabled = true;

        const userName = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        try {
            const response = await fetch("../user/authenticate", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ userName, password }),
            });
            console.log("Response Status:", response.status);

            if (!response.ok) {
                console.log("Response not OK");
                messageDiv.textContent = "Invalid credentials";
                throw new Error("Authentication failed");
            }

            console.log("Response :", response);
            const data = await response.json();
            console.log("Response Data:", data);
            const token = data.token || data.jwt || null;

            if (token) {
                console.log("JWT Token:", token);
                window.location.href = "/view-content";
            } else {
                messageDiv.textContent = "Invalid credentials";
                throw new Error("Token not received");
            }
        } catch (error) {
            console.error("Error during authentication:", error);
            if (!messageDiv.textContent) {
                messageDiv.textContent = "Invalid credentials";
            }
        } finally {
            // Reset button state
            submitButton.textContent = originalButtonText;
            submitButton.disabled = false;
        }
    });
});
