window.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById("registration-form");
    const errorDiv = document.getElementById("registration-error");
    console.log("Form trovato?", form);
    console.log("Div errore trovata?", errorDiv);

    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        const formData = new FormData(form);
        const params = new URLSearchParams();
        for (const pair of formData) {
            params.append(pair[0], pair[1]);
        }

        try {
            const response = await fetch(form.action, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: params,
            });

            const data = await response.json();
            console.log("Risposta JSON:", data);

            if (data.type === "error") {
                if (errorDiv) {
                    errorDiv.textContent = data.msg || "Errore generico";
                } else {
                    alert(data.msg || "Errore generico");
                }
            } else if (data.type === "success" && data.redirect) {
                alert(data.msg);
                window.location.href = data.redirect;
            } else {
                console.log("Risposta inattesa:", data);
            }

        } catch (err) {
            if (errorDiv) {
                errorDiv.textContent = "Errore di rete: riprova.";
            } else {
                alert("Errore di rete: riprova.");
            }
            console.error(err);
        }
    });
});
