document
    .getElementById("registerForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        const name = document.getElementById("name").value;
        const lastname = document.getElementById("lastname").value;
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        const registerRequest = {
            name: name,
            lastname: lastname,
            username: username,
            password: password
        };

        //Springboot responsible later for converting the js obj (json) to java obj
        registerUser(registerRequest);
    });


function registerUser(registerRequest) {

    fetch("/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(registerRequest)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Registration failed");
        }
        return response.text();
    })
    .then(message => {
        alert("Registration successful!");
        window.location.href = "/login.html";
    })
    .catch(error => {
        alert(error.message);
    });
}