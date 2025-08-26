console.log("Script loaded and DOMContentLoaded event listener attached.");

document.addEventListener("DOMContentLoaded", function () {
    const items = document.querySelectorAll(".sidebar-item");
    const titleEl = document.getElementById("Section-title");
    const descEl = document.getElementById("section-description");


    // stampa descrizione
    async function getDescrizione(nomeLab) {

        try {
            const response = await fetch(`/room/studente/getDescrizioneRoom?nomeRoom=${encodeURIComponent(nomeLab)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content'),

                }
            });

            if (!response.ok) throw new Error("Errore nel recupero della descrizione della room");

            return await response.json();
        } catch (error) {
            console.error("Errore durante il caricamento della descrizione:", error);
            return [];
        }
    }

    // stampa laboratori per classe
    async function getLabsPerClasse(nomeClasse) {
        try {
            const response = await fetch(`/room/studente/getRoomsPerClasse?nomeClasse=${encodeURIComponent(nomeClasse)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content'),
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
            messageEl.textContent = "";
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
    async function startRoomContainer(nomeClasse, nomeLab, utente, token, header, areaRisposta, commandBox) {
        areaRisposta.textContent = 'Avvio in corso...';

        try {
            const response = await fetch('/room/studente/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [header]: token
                },
                body: JSON.stringify({
                    'nomeClass': nomeClasse,
                    'nomeLab': nomeLab,
                    'utente': utente
                })
            });

            const data = await response.json();

            areaRisposta.textContent = 'Risposta: ' + data.msg;
            commandBox.textContent = data.command;

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

            const data = await response.json();
            areaRisposta.textContent = 'Risposta: ' + data.msg;

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
            const response = await fetch('/classe/studente/getClassi', {
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


    // Carica le classi nel select specificato e popola dinamicamente le room al cambio classe
    async function caricaRisultati(classSelectId, roomSelectId) {
        const classSelect = document.getElementById(classSelectId);
        const roomSelect = document.getElementById(roomSelectId);

        if (!classSelect || !roomSelect) {
            console.warn("Elementi select non trovati.");
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        try {
            const response = await fetch('/classe/studente/getClassi', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                }
            });

            if (!response.ok) throw new Error("Errore nella risposta del server");

            const classNames = await response.json();

            classSelect.innerHTML = '';

            // Opzione predefinita
            const defaultOption = document.createElement('option');
            defaultOption.disabled = true;
            defaultOption.selected = true;
            defaultOption.value = "";
            defaultOption.textContent = 'Seleziona una classe';
            classSelect.appendChild(defaultOption);

            classNames.forEach(nome => {
                const option = document.createElement('option');
                option.value = nome;
                option.textContent = nome;
                classSelect.appendChild(option);
            });

            // Aggiungi il listener per il cambio selezione
            classSelect.addEventListener('change', async () => {
                const classeSelezionata = classSelect.value;

                // Pulisce il select delle room
                roomSelect.innerHTML = '';

                try {

                    const roomNames = await getLabsPerClasse(classeSelezionata);

                    if (!Array.isArray(roomNames) || roomNames.length === 0) {
                        const option = document.createElement('option');
                        option.disabled = true;
                        option.selected = true;
                        option.value = "";
                        option.textContent = 'Nessuna room disponibile';
                        roomSelect.appendChild(option);
                        renderRisultatiTabella(roomNames);
                        return;
                    }

                    // Opzione predefinita per le room
                    const roomDefault = document.createElement('option');
                    roomDefault.disabled = true;
                    roomDefault.selected = true;
                    roomDefault.value = "";
                    roomDefault.textContent = 'Seleziona una room';
                    roomSelect.appendChild(roomDefault);

                    roomNames.forEach(room => {
                        const option = document.createElement('option');
                        option.value = room;
                        option.textContent = room;
                        roomSelect.appendChild(option);
                    });

                } catch (error) {
                    console.error("Errore durante il caricamento delle room:", error);
                    const option = document.createElement('option');
                    option.disabled = true;
                    option.selected = true;
                    option.value = "";
                    option.textContent = 'Errore nel caricamento delle room';
                    roomSelect.appendChild(option);
                }
            });

        } catch (error) {
            console.error("Errore durante il caricamento delle classi:", error);
            classSelect.innerHTML = '';
            const option = document.createElement('option');
            option.disabled = true;
            option.selected = true;
            option.value = "";
            option.textContent = 'Errore nel caricamento';
            classSelect.appendChild(option);
        }
    }



    // Iscrive lo studente a una classe tramite il form
    function iscriviStudente(nomeClasse, messageEl) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        fetch('/classe/studente/iscriviti', {
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
            const response = await fetch('/classe/studente/getClassiIscritte', { credentials: 'same-origin' });
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

    // submit form per ottenere i risultati
    document.addEventListener("submit", async (e) => {
        const form = e.target;

        if (form.id === "form-risultati") {
            e.preventDefault();

            const classeId = form.querySelector('[name="classeId"]').value;
            const roomId = form.querySelector('[name="roomId"]').value;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");

            const formData = new URLSearchParams();
            formData.append("classeId", classeId);
            formData.append("roomId", roomId);

            try {
                const response = await fetch("/room/studente/risultati/visualizza", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "X-CSRF-TOKEN": csrfToken
                    },
                    body: formData,
                    credentials: "same-origin"
                });

                const data = await response.json();

                if (Array.isArray(data)) {
                    renderizzaTabellaRisultati(data); // chiama la tua funzione già definita
                } else {
                    console.error("Risposta non valida dal server", data);
                }
            } catch (err) {
                console.error("Errore durante la fetch dei risultati:", err);
            }
        }
    });


    // rendering della tabella dei risultati
    function renderizzaTabellaRisultati(dati) {
        const container = document.getElementById('form-risultati');
        if (!container) return;

        const table = document.createElement('table');
        table.classList.add('tabella-risultati');

        const thead = `
        <thead>
            <tr>
                <th>Studente</th>
                <th>Score</th>
            </tr>
            </thead>
        `;
        table.innerHTML = thead;

        const tbody = document.createElement('tbody');
        dati.forEach(el => {
            const row = document.createElement('tr');
            row.innerHTML = `
      <td>${el.studente}</td>
      <td>${el.score}</td>
    `;
            tbody.appendChild(row);
        });

        table.appendChild(tbody);

        container.innerHTML = ''; // svuota il form
        container.appendChild(table); // inserisce la tabella
    }

    // rendering dei ri
    function renderRisultatiTabella(data) {
        const formContainer = document.getElementById('form-risultati');

        if (!formContainer) {
            console.error("Elemento #form-risultati non trovato");
            return;
        }

        // Crea la tabella
        const table = document.createElement('table');
        table.classList.add('risultati-table');
        table.style.width = "100%";
        table.style.borderCollapse = "collapse";

        // Crea l'intestazione
        const headerRow = document.createElement('tr');
        ['Studente', 'Classe', 'Room', 'Data', 'Score'].forEach(headerText => {
            const th = document.createElement('th');
            th.textContent = headerText;
            th.style.border = "1px solid #ccc";
            th.style.padding = "8px";
            th.style.backgroundColor = "#f2f2f2";
            headerRow.appendChild(th);
        });
        table.appendChild(headerRow);

        // Crea le righe dei dati
        data.forEach(riga => {
            const tr = document.createElement('tr');

            const dataFormat = new Date(riga.timestamp).toLocaleString();

            [riga.studente, riga.classe, riga.room, dataFormat, riga.score].forEach(val => {
                const td = document.createElement('td');
                td.textContent = val;
                td.style.border = "1px solid #ccc";
                td.style.padding = "8px";
                tr.appendChild(td);
            });

            table.appendChild(tr);
        });

        // Sostituisci il form con la tabella
        formContainer.innerHTML = ""; // Svuota il contenuto
        formContainer.appendChild(table);
    }




    async function renderRoomDetail(nomeClasse, nomeLab, utente, csrfToken, csrfHeader) {

        const descrizione = await getDescrizione(nomeLab);

        descEl.innerHTML = `
        <h2>Room: ${nomeLab}</h3>
        <p>${descrizione.descrizione}.</p>
        <div style="margin-top: 10px;">
            <button class="btn start-room-btn">Avvia Room</button>
            <button class="btn stop-room-btn" style="margin-left: 10px;">Ferma Room</button>
        </div>
        <div>
        <h3 id="insFlag" hidden>Inserisci la flag</h3>
        <form id="submit-flag" action="/room/studente/flag" method="post">
            <input type="hidden" name="room" value="${nomeLab}" />
            <input type="hidden" name="studente" value="${utente}" />
            <input type="text" name="flag" id="flagField" hidden>
            <input type="submit" id="flagSubmit" hidden></input>
        </div>
        <p id="flag-result-msg" style="display: none;"></p>
        <div class="room-status" style="margin-top: 10px; font-weight: bold;"></div>
        <div id="command-box" class="command-box" hidden></div>
        <button class="btn" style="margin-top: 20px;" id="back-to-list">⬅ Torna alle Room</button>
    `;

        const startBtn = descEl.querySelector(".start-room-btn");
        const stopBtn = descEl.querySelector(".stop-room-btn");
        const areaRisposta = descEl.querySelector(".room-status");
        const commandBox = descEl.querySelector(".command-box")
        const backBtn = descEl.querySelector("#back-to-list");

        startBtn.addEventListener("click", () => {
            startRoomContainer(nomeClasse, nomeLab, utente, csrfToken, csrfHeader, areaRisposta, commandBox);
            flagField.hidden = false;
            flagSubmit.hidden = false;
            commandBox.hidden = false;
            insFlag.hidden = false;
        });

        stopBtn.addEventListener("click", () => {
            stopRoomContainer(utente, csrfToken, csrfHeader, areaRisposta);
            flagField.hidden = true;
            flagSubmit.hidden = true;
            commandBox.hidden = true;
            insFlag.hidden = true;
            commandBox.textContent = "";
        });


        const form = document.getElementById("submit-flag");
        const resultMsg = document.getElementById("flag-result-msg");

        form.addEventListener("submit", async function (e) {
            e.preventDefault();

            const formData = new FormData(form);

            const res = await fetch("/room/studente/flag", {
                headers: {
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content'),
                },
                method: "POST",
                body: formData,
            });

            const json = await res.json();

            resultMsg.style.display = "block";
            resultMsg.innerText = json.esito;

            if (json.type === "success") {
                resultMsg.style.color = "green";
            } else if (json.type === "error") {
                resultMsg.style.color = "red";
            }
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
        const form = document.getElementById("changePassword-form");
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
                    messageElement.textContent = data.msg || "Errore nel cambio password";
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

            if (!response.ok) {
                throw new Error("Logout fallito");
            }

            const data = await response.json();

            console.log(data.redirectUrl)

            if (data.redirectUrl) {
                window.location.href = data.redirectUrl;
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
                <form id="create-lab-form" action="/classe/studente/iscriviti" method="post">
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
                <form id="changePassword-form" action="/account/studente/changePassword" method="post">
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
        },
        Risultati: {
            title: "Risultati Room",
            desc: `
                <p>Visualizza i risultati delle room completate!.</p>
                <form id="form-risultati">
                    <div class="form-group">
                        <label for="classSelect">Nome classe:</label>
                        <select id="classSelect" name="classeId">
                            <option value="">Caricamento classi...</option>
                        </select>
                        <label for="roomSelect">Nome room:</label>
                        <select id="roomSelect" name="roomId">
                            <option value="">Caricamento room...</option>
                        </select>
                    </div>
                    <button type="submit">Visualizza</button>
                </form>


            `
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
                confermaPasswordRealtime();
                cambiaPassword();
            } else if (key === "Logout") {
                descEl.innerHTML = content[key].desc;
                logoutUser();
            } else if (key === "Risultati") {
                descEl.innerHTML = content[key].desc;
                caricaRisultati("classSelect", "roomSelect");

            }
        });
    });

    // Carica classi iscritte all'avvio per sidebar studente
    caricaClassiIscritte();
});
