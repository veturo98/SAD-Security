console.log("Script loaded and DOMContentLoaded event listener attached.");


document.addEventListener("DOMContentLoaded", function () {
    const items = document.querySelectorAll(".sidebar-item");
    const titleEl = document.getElementById("Section-title");
    const descEl = document.getElementById("section-description");





    // ottiene le classi dal db
    function caricaClassiNelSelect(selectId) {
        const select = document.getElementById(selectId);
        if (!select) {
            console.warn(`Elemento select con ID '${selectId}' non trovato.`);
            return;
        }

        // Recupera i token CSRF da Spring Security (meta tag)
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        fetch('/classe/getClassi', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Errore nella risposta del server");
                }
                return response.json(); // solo UNA volta
            })
            .then(classNames => {
                select.innerHTML = '';

                if (!Array.isArray(classNames) || classNames.length === 0) {
                    const option = document.createElement('option');
                    option.disabled = true;
                    option.selected = true;
                    option.textContent = 'Nessuna classe disponibile';
                    select.appendChild(option);
                    return;
                }

                const defaultOption = document.createElement('option');
                defaultOption.disabled = true;
                defaultOption.selected = true;
                defaultOption.textContent = 'Seleziona una classe';
                select.appendChild(defaultOption);

                classNames.forEach(nome => {
                    const option = document.createElement('option');
                    option.value = nome;
                    option.textContent = nome;
                    select.appendChild(option);
                });
            })
            .catch(error => {
                console.error("Errore durante il caricamento delle classi:", error);
                select.innerHTML = '';
                const option = document.createElement('option');
                option.disabled = true;
                option.selected = true;
                option.textContent = 'Errore nel caricamento';
                select.appendChild(option);
            });
    }


    //crea una nuova sezione nel menu con la classe selezionata
    function iscriviStudente(nomeClasse) {
    const params = new URLSearchParams();
    params.append("nomeClasse", nomeClasse);


    fetch('/classe/iscriviti', {
        method: 'POST',
        body: params,
        credentials: 'same-origin', // per cookies/sessione
        headers: {
            'X-CSRF-TOKEN': csrfToken // se usi Spring Security
        }
    })
    .then(async response => {
        const text = await response.text();

        if (!response.ok) {
            // Gestione messaggio d'errore personalizzato restituito dal backend
            throw new Error(text || "Errore durante l'iscrizione");
        }

        console.log("Risposta dal server:", text);
        alert(text); // Mostra messaggio positivo dal server
        caricaClassiIscritte(); // Aggiorna la lista delle classi iscritte
    })
    .catch(error => {
        console.error("Errore:", error);
        alert(error.message); // Mostra messaggio di errore ricevuto dal server
    });
}


 // funzione per caricare le classi iscritte dall'utente e popolare la sidebar
    function caricaClassiIscritte() {
        
        fetch('/classe/getClassiIscritte', { credentials: 'same-origin' })
            .then(res => {
                if (!res.ok) throw new Error('Errore nel recupero classi iscritte');
                return res.json();
            })
            .then(classiIscritte => {
                if (Array.isArray(classiIscritte)) {
                    classiIscritte.forEach(nomeClasse => {
                        aggiungiClasseAllaSidebar(nomeClasse);
                    });
                }
            })
            .catch(err => {
                console.error('Errore caricamento classi iscritte:', err);
            });
    }


function aggiungiClasseAllaSidebar(nomeClasse) {
        const sidebar = document.getElementById("student-sidebar-options");

        if (!sidebar) return;

        // Controlla se la classe è già presente nella sidebar
        const esiste = Array.from(sidebar.querySelectorAll("a.sidebar-item"))
            .some(link => link.textContent.trim() === nomeClasse);

        if (esiste) return; // evita duplicati

        const li = document.createElement("li");
        const a = document.createElement("a");
        a.href = "#";
        a.classList.add("sidebar-item");
        a.dataset.option = nomeClasse;
        a.textContent = nomeClasse;

        li.appendChild(a);
        sidebar.appendChild(li);

        // (Opzionale) Gestione click
        a.addEventListener("click", () => {
            titleEl.textContent = `Classe: ${nomeClasse}`;
descEl.innerHTML = `
    <p>Hai selezionato la classe <strong>${nomeClasse}</strong>.</p>
    <button id="start-room-btn" class="btn">Avvia Room</button>
    <button id="stop-room-btn" class="btn" style="margin-left: 10px;">Ferma Room</button>
    <div id="room-status-message" style="margin-top: 10px; font-weight: bold;"></div>
`;

        });
    }

    // La funzione è la prima ad essere chiamata subito dopo il caricamento del DOM così inserisce subito le classi nella sidebar
    caricaClassiIscritte();



    // verifica che la password vecchia sia corretta
    function oldPasswordCheck() {
        const oldpasswordInput = document.getElementById("oldPassword");
        const msgOldPassword = document.getElementById("old-password-message");

        if (!oldpasswordInput) return;

        oldpasswordInput.addEventListener("blur", function () {

            const oldPassword = oldpasswordInput.value.trim();

            if (!oldPassword) {
                msgOldPassword.textContent = "Il campo password non può essere vuoto.";
                msgOldPassword.style.color = "red";
                return;
            }

            fetch(`/account/studente/checkOldPassword?oldPassword=${encodeURIComponent(oldPassword)}`, {
                credentials: 'same-origin'
            })
                .then(res => res.json())
                .then(data => {
                    msgOldPassword.textContent = data.message;
                    msgOldPassword.style.color = data.type === "error" ? "red" : "green";
                })
                .catch(err => {
                    console.error("Errore nella verifica della vecchia password:", err);
                    msgOldPassword.textContent = "Errore durante la verifica.";
                    msgOldPassword.style.color = "red";
                });
        });
    }

    //verifica che la nuova passord e quella di conferma sono uguali 
    function confermaPasswordRealtime() {
        // const oldPasswordInput = document.getElementById("oldPassword");
        const newPasswordInput = document.getElementById("newPassword");
        const confirmPasswordInput = document.getElementById("confirmPassword");
        const msgConfirmPassword = document.getElementById("confirm-password-message");

        if (!newPasswordInput || !confirmPasswordInput || !msgConfirmPassword) return;

        confirmPasswordInput.addEventListener("input", function () {
            const newPassword = newPasswordInput.value.trim();
            const confirmPassword = confirmPasswordInput.value.trim();

            if (!confirmPassword) {
                msgConfirmPassword.textContent = "";
                return;
            }

            if (newPassword !== confirmPassword) {
                msgConfirmPassword.textContent = "Le password non corrispondono.";
                msgConfirmPassword.style.color = "red";
            } else {
                msgConfirmPassword.textContent = "Le password corrispondono.";
                msgConfirmPassword.style.color = "green";
            }
        });
    }

    // Cambia password
    function cambiaPassword() {
        const form = document.getElementById("change-password-form");
        if (!form) return;

        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const oldPassword = document.getElementById("oldPassword").value.trim();
            const newPassword = document.getElementById("newPassword").value.trim();

            const formData = new FormData();
            formData.append("oldPassword", oldPassword);
            formData.append("newPassword", newPassword);

            const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
            const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;

            fetch(form.action, {
                method: "POST",
                headers: csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {},
                body: formData,
                credentials: "same-origin"
            })
                .then(res => res.json())
                .then(data => {
                    const messageElement = document.getElementById("submit-form-message");
                    if (!messageElement) return;

                    if (data.type === "success") {
                        messageElement.textContent = "Password cambiata con successo!";
                        messageElement.style.color = "green";
                        form.reset();
                        setTimeout(() => {
                            window.location.href = "/login";  // Metti qui l'URL della tua pagina di login
                        }, 1000); // aspetta 2 secondi per far vedere il messaggio
                    } else {
                        messageElement.textContent = data.message || "Errore nel cambio password";
                        messageElement.style.color = "red";
                    }
                })
                .catch(err => {
                    console.error("Errore nel cambio password:", err);
                    const messageElement = document.getElementById("submit-form-message");
                    if (!messageElement) return;
                    messageElement.textContent = "Errore nel cambio password, riprova.";
                    messageElement.style.color = "red";
                });
        });
    }


    function logoutUser() {
        const descEl = document.getElementById("section-description");
        descEl.innerHTML = "<p>Effettuando logout...</p>";

        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;

        fetch("/account/logout", {
            method: "POST",
            headers: {
                "X-CSRF-TOKEN": csrfToken,
                "Content-Type": "application/x-www-form-urlencoded"
            },
            credentials: "same-origin"
        })
            .then(response => {
                if (response.ok) {
                    window.location.href = "/login";
                } else {
                    throw new Error("Logout fallito");
                }
            })
            .catch(err => {
                console.error("Errore durante il logout:", err);
                descEl.innerHTML = "<p style='color:red'>Errore durante il logout, riprova.</p>";
            });
    }


    




    const content = {
        IscrivitiClasse: {
            title: "Iscriviti ad una classe",
            desc: `
                <p>Seleziona una classe a cui iscriverti</p>
                <form id="create-lab-form" action="/classe/iscriviti" method="post">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                <div class="form-group">
                    <label for="classSelect"> Nome classe:</label>
                    <select id="classSelect" name="nomeClasse">
                        <option value="">Selezione classe...</option>
                    </select>
                </div>
                 <button type="submit">Iscriviti a Classe</button>
                </form>
                <div id="form-lab-message" style="margin-top: 15px; font-weight: bold;"></div>
            `
        },
        "Cambia Password": {
            title: "Cambia Password",
            desc: `
                <p>Cambia la password dell'utente</p>
                <form id="change-password-form" action="/account/studente/change-password" method="post">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                    <div>
                        <label for="oldPassword">Vecchia Password:</label>
                        <input type="password" id="oldPassword" name="oldPassword" required>
                        <div id="old-password-message" style="margin-top: 15px; font-weight: bold;"></div>
                    </div>
                    <div>
                        <label for="newPassword">Nuova Password:</label>
                        <input type="password" id="newPassword" name="newPassword" required>
                        <div id="new-password-message" style="margin-top: 15px; font-weight: bold;"></div>
                    </div>
                    <div>
                        <label for="confirmPassword">Conferma Nuova Password:</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required>
                        <div id="confirm-password-message" style="margin-top: 15px; font-weight: bold;"></div>
                    </div>
                    <button type="submit">Cambia Password</button>
                    <div id="submit-form-message" style="margin-top: 15px; font-weight: bold;"></div>
                </form>
                `

        },
        Logout: {
            title: "Logout applicazione ",
            desc: `Esci dalla webapp</p>
                `

        }
    };

    items.forEach(item => {
        item.addEventListener("click", function (e) {
            e.preventDefault();
            const key = item.dataset.option;

            if (content[key]) {
                titleEl.textContent = content[key].title;

                if (key === "IscrivitiClasse") {
                    descEl.innerHTML = content[key].desc;
                    caricaClassiNelSelect("classSelect");//  listener per l'iscrizione a classi
                   
                    setTimeout(() => {
                        iscriviStudente();
                    }, 0);

                } else if (key === "Cambia Password") {

                    descEl.innerHTML = content[key].desc;
                    setTimeout(() => {
                        oldPasswordCheck();// controlla old passwrod
                        cambiaPassword();//crea nuova password
                        confermaPasswordRealtime(); //controlla le nuove password
                    }, 0);
                } else if (key === "Logout") {
                    console
                    descEl.innerHTML = content[key].desc;
                    logoutUser();
                }

                else {
                    descEl.innerHTML = `<p>${content[key].desc}</p>`;
                }

                items.forEach(i => i.classList.remove('active'));
                item.classList.add('active');
            } else {
                titleEl.textContent = "Selezione non valida";
                descEl.innerHTML = "<p>Questa opzione non è ancora disponibile.</p>";
            }
        });
    });


});




