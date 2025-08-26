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

            const res = await response.json();
            console.log("Risposta JSON:", res);

            if (res.type === "error") {
                if (errorDiv) {
                    errorDiv.textContent = res.message || "Errore generico";
                } else {
                    alert(res.message || "Errore generico");
                }
            } else if (res.type === "success" && res.data) {
                alert(res.message);
                window.location.href = res.data;
            } else {
                console.log("Risposta inattesa:", res.message);
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
