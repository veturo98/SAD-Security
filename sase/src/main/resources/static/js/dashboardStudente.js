console.log("Script loaded and DOMContentLoaded event listener attached.");

document.addEventListener("DOMContentLoaded", function () {
    const items = document.querySelectorAll(".sidebar-item");
    const titleEl = document.getElementById("Section-title");
    const descEl = document.getElementById("section-description");




    async function getLabsPerClasse(nomeClasse) {
        try {
            const response = await fetch(`/room/getRoomsPerClasse?nomeClasse=${encodeURIComponent(nomeClasse)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error("Errore nel recupero dei lab della classe");

            return await response.json(); // supponiamo sia un array di nomi di lab
        } catch (error) {
            console.error("Errore durante il caricamento dei lab:", error);
            return [];
        }
    }



    function setupIscrizioneClasse() {
        const form = document.getElementById("create-lab-form");
        const select = document.getElementById("classSelect");
        const messageEl = document.getElementById("form-lab-message");

        if (!form || !select || !messageEl) return;

        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const nomeClasse = select.value;
            messageEl.textContent = ""; // pulizia precedente
            messageEl.style.color = "black";

            if (!nomeClasse) {
                messageEl.textContent = "Seleziona una classe valida.";
                messageEl.style.color = "red";
                return;
            }

            iscriviStudente(nomeClasse, messageEl);
        });
    }





    // Funzione per avviare il container di una room
    async function startRoomContainer(nomeClasse, nomeLab, utente, token, header, areaRisposta) {
        areaRisposta.textContent = 'Avvio in corso...';

        try {
            const response = await fetch('/room/studente/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    //[header]: token
                },
                body: JSON.stringify({
                    'nomeClass': nomeClasse,
                    'nomeLab': nomeLab,
                    'utente': utente
                })
            });

            const data = await response.text();
            areaRisposta.textContent = 'Risposta: ' + data;

        } catch (error) {
            console.error('Errore durante la richiesta:', error);
            areaRisposta.textContent = 'Errore durante la richiesta.';
        }
    }

    // Funzione per fermare il container di una room
    async function stopRoomContainer(utente, token, header, areaRisposta) {
        areaRisposta.textContent = 'Stop container in corso...';

        try {
            const response = await fetch('/room/studente/stop', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [header]: token
                },
                body: JSON.stringify({ 'utente': utente })
            });

            const data = await response.text();
            areaRisposta.textContent = 'Risposta: ' + data;

        } catch (error) {
            console.error('Errore durante la richiesta:', error);
            areaRisposta.textContent = 'Errore durante la richiesta.';
        }
    }

    // Carica le classi nel select specificato
    async function caricaClassiNelSelect(selectId) {
        const select = document.getElementById(selectId);
        if (!select) {
            console.warn(`Elemento select con ID '${selectId}' non trovato.`);
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        try {
            const response = await fetch('/classe/getClassi', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                }
            });

            if (!response.ok) throw new Error("Errore nella risposta del server");

            const classNames = await response.json();

            select.innerHTML = '';

            // Se nessuna classe è disponibile
            if (!Array.isArray(classNames) || classNames.length === 0) {
                const option = document.createElement('option');
                option.disabled = true;
                option.selected = true;
                option.value = ""; // Assicura che non possa essere selezionata per iscrizione
                option.textContent = 'Nessuna classe disponibile';
                select.appendChild(option);
                return;
            }

            // Aggiungi opzione predefinita
            const defaultOption = document.createElement('option');
            defaultOption.disabled = true;
            defaultOption.selected = true;
            defaultOption.value = ""; // Impedisce l'iscrizione con questa voce
            defaultOption.textContent = 'Seleziona una classe';
            select.appendChild(defaultOption);

            // Aggiungi opzioni reali
            classNames.forEach(nome => {
                const option = document.createElement('option');
                option.value = nome;
                option.textContent = nome;
                select.appendChild(option);
            });

        } catch (error) {
            console.error("Errore durante il caricamento delle classi:", error);
            select.innerHTML = '';
            const option = document.createElement('option');
            option.disabled = true;
            option.selected = true;
            option.value = ""; // Anche qui, per sicurezza
            option.textContent = 'Errore nel caricamento';
            select.appendChild(option);
        }
    }


    // Iscrive lo studente a una classe tramite il form
    function iscriviStudente(nomeClasse, messageEl) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        fetch('/classe/iscriviti', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': csrfToken || ''
            },
            body: new URLSearchParams({ nomeClasse })
        })
            .then(response => response.json())
            .then(data => {
                if (!data || !data.message) {
                    messageEl.textContent = "Errore imprevisto nella risposta del server.";
                    messageEl.style.color = "red";
                    return;
                }

                messageEl.textContent = data.message;
                messageEl.style.color = data.type === "success" ? "green" : "red";

                if (data.type === "success") {
                    caricaClassiIscritte(); // aggiorna la sidebar
                }
            })
            .catch(error => {
                console.error("Errore:", error);
                messageEl.textContent = "Errore durante l'iscrizione.";
                messageEl.style.color = "red";
            });
    }



    // Carica le classi iscritte e popola la sidebar
    async function caricaClassiIscritte() {
        const sidebar = document.getElementById("student-sidebar-options");
        if (!sidebar) return;
        sidebar.innerHTML = ''; // pulisce prima di aggiungere

        try {
            const response = await fetch('/classe/getClassiIscritte', { credentials: 'same-origin' });
            if (!response.ok) throw new Error('Errore nel recupero classi iscritte');

            const classiIscritte = await response.json();

            if (Array.isArray(classiIscritte)) {
                classiIscritte.forEach(nomeClasse => {
                    aggiungiClasseAllaSidebar(nomeClasse);
                });
            }
        } catch (error) {
            console.error('Errore caricamento classi iscritte:', error);
        }
    }

    // Aggiunge una classe alla sidebar con relativo listener
function aggiungiClasseAllaSidebar(nomeClasse) {
    const sidebar = document.getElementById("student-sidebar-options");
    if (!sidebar) return;

    const exists = Array.from(sidebar.querySelectorAll("a.sidebar-item"))
        .some(link => link.textContent.trim() === nomeClasse);
    if (exists) return;

    const li = document.createElement("li");
    const a = document.createElement("a");
    a.href = "#";
    a.classList.add("sidebar-item");
    a.dataset.option = nomeClasse;
    a.textContent = nomeClasse;

    li.appendChild(a);
    sidebar.appendChild(li);

    a.addEventListener("click", async () => {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
        const utente = document.getElementById("user")?.textContent || "unknown";

        titleEl.textContent = `Classe: ${nomeClasse}`;
        descEl.innerHTML = `<p>Seleziona una room per la classe <strong>${nomeClasse}</strong>:</p>`;

        const labs = await getLabsPerClasse(nomeClasse);

        if (!Array.isArray(labs) || labs.length === 0) {
            descEl.innerHTML += "<p>Nessuna room disponibile per questa classe.</p>";
            return;
        }

        const listContainer = document.createElement("ul");
        listContainer.classList.add("room-list");

        labs.forEach(nomeLab => {
            const roomItem = document.createElement("li");
            roomItem.style.cursor = "pointer";
            roomItem.style.padding = "8px";
            roomItem.style.borderBottom = "1px solid #ccc";
            roomItem.textContent = nomeLab;

            roomItem.addEventListener("click", () => {
                renderRoomDetail(nomeClasse, nomeLab, utente, csrfToken, csrfHeader);
            });

            listContainer.appendChild(roomItem);
        });

        descEl.appendChild(listContainer);
    });
}


function renderRoomDetail(nomeClasse, nomeLab, utente, csrfToken, csrfHeader) {
    descEl.innerHTML = `
        <h3>Room: ${nomeLab}</h3>
        <p>Stai visualizzando la room <strong>${nomeLab}</strong> della classe <strong>${nomeClasse}</strong>.</p>
        <div style="margin-top: 10px;">
            <button class="btn start-room-btn">Avvia Room</button>
            <button class="btn stop-room-btn" style="margin-left: 10px;">Ferma Room</button>
        </div>
        <div class="room-status" style="margin-top: 10px; font-weight: bold;"></div>
        <button class="btn" style="margin-top: 20px;" id="back-to-list">⬅ Torna alle Room</button>
    `;

    const startBtn = descEl.querySelector(".start-room-btn");
    const stopBtn = descEl.querySelector(".stop-room-btn");
    const areaRisposta = descEl.querySelector(".room-status");
    const backBtn = descEl.querySelector("#back-to-list");

    startBtn.addEventListener("click", () => {
        startRoomContainer(nomeClasse, nomeLab, utente, csrfToken, csrfHeader, areaRisposta);
    });

    stopBtn.addEventListener("click", () => {
        stopRoomContainer(utente, csrfToken, csrfHeader, areaRisposta);
    });

    backBtn.addEventListener("click", async () => {
        // Ricarica la lista delle room per la classe
        titleEl.textContent = `Classe: ${nomeClasse}`;
        descEl.innerHTML = `<p>Seleziona una room per la classe <strong>${nomeClasse}</strong>:</p>`;

        const labs = await getLabsPerClasse(nomeClasse);

        if (!Array.isArray(labs) || labs.length === 0) {
            descEl.innerHTML += "<p>Nessuna room disponibile per questa classe.</p>";
            return;
        }

        const listContainer = document.createElement("ul");
        listContainer.classList.add("room-list");

        labs.forEach(nomeLab => {
            const roomItem = document.createElement("li");
            roomItem.style.cursor = "pointer";
            roomItem.style.padding = "8px";
            roomItem.style.borderBottom = "1px solid #ccc";
            roomItem.textContent = nomeLab;

            roomItem.addEventListener("click", () => {
                renderRoomDetail(nomeClasse, nomeLab, utente, csrfToken, csrfHeader);
            });

            listContainer.appendChild(roomItem);
        });

        descEl.appendChild(listContainer);
    });
}


    // Verifica vecchia password
    function oldPasswordCheck() {
        const oldpasswordInput = document.getElementById("oldPassword");
        const msgOldPassword = document.getElementById("old-password-message");
        if (!oldpasswordInput || !msgOldPassword) return;

        oldpasswordInput.addEventListener("blur", async () => {
            const oldPassword = oldpasswordInput.value.trim();

            if (!oldPassword) {
                msgOldPassword.textContent = "Il campo password non può essere vuoto.";
                msgOldPassword.style.color = "red";
                return;
            }

            try {
                const res = await fetch(`/account/studente/checkOldPassword?oldPassword=${encodeURIComponent(oldPassword)}`, { credentials: 'same-origin' });
                const data = await res.json();

                msgOldPassword.textContent = data.message;
                msgOldPassword.style.color = data.type === "error" ? "red" : "green";
            } catch (error) {
                console.error("Errore nella verifica della vecchia password:", error);
                msgOldPassword.textContent = "Errore durante la verifica.";
                msgOldPassword.style.color = "red";
            }
        });
    }

    // Conferma nuova password in tempo reale
    function confermaPasswordRealtime() {
        const newPasswordInput = document.getElementById("newPassword");
        const confirmPasswordInput = document.getElementById("confirmPassword");
        const msgConfirmPassword = document.getElementById("confirm-password-message");
        if (!newPasswordInput || !confirmPasswordInput || !msgConfirmPassword) return;

        confirmPasswordInput.addEventListener("input", () => {
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

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const oldPassword = document.getElementById("oldPassword").value.trim();
            const newPassword = document.getElementById("newPassword").value.trim();

            const formData = new FormData();
            formData.append("oldPassword", oldPassword);
            formData.append("newPassword", newPassword);

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const messageElement = document.getElementById("submit-form-message");
            if (!messageElement) return;

            try {
                const response = await fetch(form.action, {
                    method: "POST",
                    headers: csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {},
                    body: formData,
                    credentials: "same-origin"
                });

                const data = await response.json();

                if (data.type === "success") {
                    messageElement.textContent = "Password cambiata con successo!";
                    messageElement.style.color = "green";
                    form.reset();
                    setTimeout(() => {
                        window.location.href = "/login";
                    }, 1000);
                } else {
                    messageElement.textContent = data.message || "Errore nel cambio password";
                    messageElement.style.color = "red";
                }
            } catch (error) {
                console.error("Errore nel cambio password:", error);
                messageElement.textContent = "Errore nel cambio password, riprova.";
                messageElement.style.color = "red";
            }
        });
    }

    // Logout utente
    async function logoutUser() {

        descEl.innerHTML = "<p>Effettuando logout...</p>";

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        try {
            const response = await fetch("/account/logout", {
                method: "POST",
                headers: {
                    "X-CSRF-TOKEN": csrfToken || '',
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                credentials: "same-origin"
            });

            if (response.ok) {
                window.location.href = "/login";
            } else {
                throw new Error("Logout fallito");
            }
        } catch (error) {
            console.error("Errore durante il logout:", error);
            descEl.innerHTML = "<p style='color:red'>Errore durante il logout, riprova.</p>";
        }
    }

    // Contenuti per la sidebar principale
    const content = {
        "IscrivitiClasse": {
            title: "Iscriviti ad una classe",
            desc: `
                <p>Seleziona una classe a cui iscriverti</p>
                <form id="create-lab-form" action="/classe/iscriviti" method="post">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''}" />
                    <div class="form-group">
                        <label for="classSelect">Nome classe:</label>
                        <select id="classSelect" name="nomeClasse">
                            <option value="">Seleziona classe...</option>
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
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''}" />
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
            title: "Logout",
            desc: "<p>Effettua il logout dall'account</p>"
        }
    };

    // Event listener per i link della sidebar principale
    items.forEach((item) => {
        item.addEventListener("click", () => {
            const key = item.dataset.option || item.textContent.trim();
            if (!content[key]) return;

            titleEl.textContent = content[key].title || key;
            descEl.innerHTML = content[key].desc || "";

            if (key === "IscrivitiClasse") {
                caricaClassiNelSelect("classSelect").then(() => {
                    setupIscrizioneClasse();
                });
            } else if (key === "Cambia Password") {
                oldPasswordCheck();
                confermaPasswordRealtime();
                cambiaPassword();
            } else if (key === "Logout") {
                descEl.innerHTML = content[key].desc;
                logoutUser();
            }
        });
    });

    // Carica classi iscritte all'avvio per sidebar studente
    caricaClassiIscritte();
});
