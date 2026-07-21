document
    .getElementById("loginForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        const loginRequest = {
            username: username,
            password: password
        };

        loginUser(loginRequest);
    });


function loginUser(loginRequest) {

    fetch("/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(loginRequest)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Invalid username or password");
        }
        return response.json();
    })
    .then(authResponse => {

        localStorage.setItem("token", authResponse.token);
        localStorage.setItem("username", authResponse.username);
        localStorage.setItem("name", authResponse.name);
        localStorage.setItem("lastname", authResponse.lastname);

        alert("Login successful!");

        window.location.href = "/index.html";

    })
    .catch(error => {
        alert(error.message);
    });
}