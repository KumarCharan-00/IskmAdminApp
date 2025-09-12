document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("loginForm");
  if (!form) {
    console.error("loginForm not found in DOM");
    return;
  }

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    console.log("Inside JS");

    const userName = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    try {
      const response = await fetch("/authenticate/user", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ userName, password })
      });
      console.log("Response Status:", response.status);
      if (!response.ok) {
        console.log("Response not OK");
        throw new Error("Authentication failed");
      }

      console.log("Response :", response);
      const data = await response.json();
      console.log("Response Data:", data);
      const token = data.token || data.jwt || null;

      if (token) {
        console.log("JWT Token:", token);
        window.location.href = "/dashboard";
      } else {
        throw new Error("Token not received");
      }
    } catch (error) {
      console.error("Error during authentication:", error);
    }
  });
});
