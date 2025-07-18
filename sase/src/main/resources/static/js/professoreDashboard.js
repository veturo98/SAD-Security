console.log("Script loaded and DOMContentLoaded event listener attached.");


document.addEventListener("DOMContentLoaded", function () {
    const items = document.querySelectorAll(".sidebar-item");
    const titleEl = document.getElementById("Section-title");
    const descEl = document.getElementById("section-description");



    // funzione per la creazione della classe
    function attachFormListener() {
        const form = document.getElementById("create-class-form");
        if (!form) return;

        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const formData = new FormData(form);

            fetch(form.action, {
                method: "POST",
                headers: {
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    const msgEl = document.getElementById("form-message");
                    msgEl.textContent = data.message;
                    msgEl.style.color = data.type === "success" ? "green" : "red";
                })
                .catch(error => {
                    console.error("Errore:", error);
                });
        });
    }



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



    // verifica che il laboratorio non esista già
    function controllaLab() {
        const input = document.getElementById("roomName");
        const msg = document.getElementById("form-roomName-message");

        if (!input || !msg) return;

        input.addEventListener("blur", function () {

            const roomName = input.value.trim();

            if (!roomName) {
                msg.textContent = "Il nome del laboratorio non può essere vuoto.";
                msg.style.color = "red";
                return;
            }

            fetch(`/room/professore/checkroom?roomName=${encodeURIComponent(roomName)}`)
                .then(res => res.json())
                .then(data => {
                    msg.textContent = data.message;
                    msg.style.color = data.type === "error" ? "red" : "green";
                })
                .catch(err => {
                    console.error("Errore nella verifica del laboratorio:", err);
                    msg.textContent = "Errore durante la verifica.";
                    msg.style.color = "red";
                });
        });
    }



    // Crea laboratorio
    function CreaLaboratorio() {
        const form = document.getElementById("create-lab-form");

        if (!form) return;

        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const formData = new FormData(form);

            fetch(form.action, {
                method: "POST",
                headers: {
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content')

                },
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    const msgEl = document.getElementById("form-lab-message");
                    msgEl.textContent = data.message;
                    msgEl.style.color = data.type === "success" ? "green" : "red";
                })
                .catch(error => {
                    console.error("Errore nella creazione del laboratorio:", error);
                    const msgEl = document.getElementById("form-lab-message");
                    msgEl.textContent = "Errore durante l'invio del modulo.";
                    msgEl.style.color = "red";
                });
        });
    }



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

            fetch(`/account/professore/checkOldPassword?oldPassword=${encodeURIComponent(oldPassword)}`, {
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
        CreaClasse: {
            title: "Crea Nuova Classe",
            desc: `
                <p>Utilizza il modulo sottostante per creare una nuova classe per l'organizzazione dei tuoi studenti.</p>
                <form id="create-class-form" action="/classe/crea" method="post">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                    <div class="form-group">
                        <label for="className">Nome Classe:</label>
                        <input type="text" id="className" name="classe" required>
                    </div>
                    <button type="submit">Crea Classe</button>
                </form>
                <div id="form-message" style="margin-top: 15px; font-weight: bold;"></div>
            `
        },
        CreaLaboratorio: {
            title: "Crea Laboratorio",
            desc: `
                <p>Imposta un nuovo laboratorio, specifica la durata, il contenuto e gli studenti assegnati.</p>
                <form id="create-lab-form" action="/room/professore/creaRoom" method="post">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                <div class="form-group">
                    <label for="classSelect"> Nome classe:</label>
                    <select id="classSelect" name="classeId">
                        <option value="">Caricamento classi...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="roomName">Nome Laboratorio:</label>
                    <input type="text" id="roomName" name="room" required>
                    <div id="form-roomName-message" style="margin-top: 15px; font-weight: bold;"></div>
                </div>
                <div class="form-group">
                <label for="roomDescrizione">Descrizione Laboratorio:</label>
                    <input type="text" id="roomDescrizione" name="descrizione" required>
                    <div id="form-roomDescrizione-message" style="margin-top: 15px; font-weight: bold;"></div>
                </div>
                <div class="form-group">
                <label for="roomFlag">Flag:</label>
                    <input type="text" id="roomFlag" name="flag" required>
                    <div id="form-roomFlag-message" style="margin-top: 15px; font-weight: bold;"></div>
                </div>
                <div class="form-group">    
                    <label for="yamlFile">File YAML:</label>
                    <input type="file" id="yamlFile" name="yamlFile" accept=".yaml,.yml" required>
                </div>
                   <button type="submit">Crea Laboratorio</button>
                </form>

                <div id="form-lab-message" style="margin-top: 15px; font-weight: bold;"></div>
                `
        },
        "Aggiungi studente": {
            title: "Aggiungi Studente",
            desc: `
                <p>Aggiungi uno studente ad una classe esistente, inserendo nome, matricola e classe di appartenenza.</p>
                `
        },
        "Conferma Richieste": {
            title: "Conferma Richieste",
            desc: `
                <p>Visualizza e approva/rifiuta le richieste di iscrizione o partecipazione agli esami/laboratori.</p>
                `
        },
        "Pubblica risultati": {
            title: "Pubblica Risultati",
            desc: `
                <p>Inserisci e pubblica i risultati degli esami o laboratori per la visione degli studenti.</p>
                `

        },
        "Cambia Password": {
            title: "Cambia Password",
            desc: `
                <p>Cambia la password del professore</p>
                <form id="change-password-form" action="/account/professore/change-password" method="post">
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
            desc: `<p>Esci dalla webapp</p>
                `

        }
    };

    items.forEach(item => {
        item.addEventListener("click", function (e) {
            e.preventDefault();
            const key = item.dataset.option;

            if (content[key]) {
                titleEl.textContent = content[key].title;

                if (key === "CreaClasse") {
                    descEl.innerHTML = content[key].desc;
                    attachFormListener();  //  listener della creazione classi

                } else if (key === "CreaLaboratorio") {

                    //quando clicco sull'opzione della sidebar ne carica il contenuto nella parte centrale della pagina 
                    descEl.innerHTML = content[key].desc;
                    setTimeout(() => {
                        caricaClassiNelSelect("classSelect");
                        controllaLab();
                        CreaLaboratorio();
                    }, 0);
                } else if (key === "Aggiungi studente") {
                    descEl.innerHTML = content[key].desc;

                } else if (key === "Conferma Richieste") {
                    descEl.innerHTML = content[key].desc;
                } else if (key === "Pubblica risultati") {
                    descEl.innerHTML = content[key].desc;
                } else if (key === "Cambia Password") {

                    descEl.innerHTML = content[key].desc;
                    setTimeout(() => {
                        oldPasswordCheck();// controlla old passwrod
                        cambiaPassword();//crea nuova password
                        confermaPasswordRealtime(); //controlla le nuove password
                    }, 0);
                } else if (key === "Logout") {

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




