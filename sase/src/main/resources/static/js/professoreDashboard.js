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

    async function getLabsPerClasse(nomeClasse) {
        try {
            const response = await fetch(`/room/professore/getRoomsPerClasse?nomeClasse=${encodeURIComponent(nomeClasse)}`, {
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
            const response = await fetch('/classe/professore/getClassi', {
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

            fetch(`/room/professore/checkroom?roomName=${encodeURIComponent(roomName)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').getAttribute('content'),
                }
            })
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


    //verifica che la nuova passord e quella di conferma sono uguali 
    function confermaPasswordRealtime() {
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
        const form = document.getElementById("changePassword-form");
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
                            window.location.href = "/professore/login";
                        }, 1000); // aspetta 2 secondi per far vedere il messaggio
                    } else {
                        messageElement.textContent = data.msg || "Errore nel cambio password";
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


    // Funzione di visualizzazione dei risultati
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
            const response = await fetch('/classe/professore/getClassi', {
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


    // submit form dei risultati
    document.addEventListener("submit", async (e) => {
        const form = e.target;

        if (form.id === "form-risultati") {
            e.preventDefault();

            const classeId = form.querySelector('[name="classeId"]').value;
            const roomId = form.querySelector('[name="roomId"]').value;
            const csrfToken = form.querySelector('[name="_csrf"]').value;

            const formData = new URLSearchParams();
            formData.append("classeId", classeId);
            formData.append("roomId", roomId);

            try {
                const response = await fetch("/room/professore/risultati/visualizza", {
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



    // FUNZIONI DI VISUALIZZAZIONE DEGLI STUDENTI
    // Carica le classi nel select specificato e popola dinamicamente le room al cambio classe
    async function caricaStudenti(classSelectId, roomSelectId) {
        const classSelect = document.getElementById(classSelectId);

        if (!classSelect) {
            console.warn("Elementi select non trovati.");
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        try {
            const response = await fetch('/classe/professore/getClassi', {
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


    document.addEventListener("submit", async (e) => {
        const form = e.target;

        if (form.id === "form-studenti") {
            e.preventDefault();

            const classeId = form.querySelector('[name="classeId"]').value;
            const csrfToken = form.querySelector('[name="_csrf"]').value;

            const formData = new URLSearchParams();
            formData.append("classeId", classeId);

            try {
                const response = await fetch("/classe/professore/listaIscritti", {
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
                    renderizzaTabellaStudenti(data); // chiama la tua funzione già definita
                } else {
                    console.error("Risposta non valida dal server", data);
                }
            } catch (err) {
                console.error("Errore durante la fetch dei risultati:", err);
            }
        }
    });


    function renderizzaTabellaStudenti(dati) {
        const container = document.getElementById('form-studenti');
        if (!container) return;

        const table = document.createElement('table');
        table.classList.add('tabella-studenti');

        const thead = `
    <thead>
      <tr>
        <th>Studente</th>
      </tr>
    </thead>
  `;
        table.innerHTML = thead;

        const tbody = document.createElement('tbody');
        dati.forEach(el => {
            const row = document.createElement('tr');
            row.innerHTML = `
      <td>${el.studente}</td>
    `;
            tbody.appendChild(row);
        });

        table.appendChild(tbody);

        container.innerHTML = ''; // svuota il form
        container.appendChild(table); // inserisce la tabella
    }


    // FUNZIONI PER LA VISUALIZZAZIONE DEI LABORATORI
    // Carica le classi nel select specificato e popola dinamicamente le room al cambio classe
    async function caricaLaboratori(classSelectId) {
        const classSelect = document.getElementById(classSelectId);

        if (!classSelect) {
            console.warn("Elementi select non trovati.");
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        try {
            const response = await fetch('/classe/professore/getClassi', {
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


    document.addEventListener("submit", async (e) => {
        const form = e.target;

        if (form.id === "form-lab") {
            e.preventDefault();

            const classeId = form.querySelector('[name="classeId"]').value;
            const csrfToken = form.querySelector('[name="_csrf"]').value;

            const formData = new URLSearchParams();
            formData.append("classeId", classeId);

            try {
                const response = await fetch("/room/professore/laboratori", {
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
                    renderizzaTabellaRoom(data); // chiama la tua funzione già definita
                } else {
                    console.error("Risposta non valida dal server", data);
                }
            } catch (err) {
                console.error("Errore durante la fetch dei risultati:", err);
            }
        }
    });


    function renderizzaTabellaRoom(dati) {
        const container = document.getElementById('form-lab');
        if (!container) return;

        const table = document.createElement('table');
        table.classList.add('tabella-lab');

        const thead = `
    <thead>
      <tr>
        <th>Room</th>
      </tr>
    </thead>
  `;
        table.innerHTML = thead;

        const tbody = document.createElement('tbody');
        dati.forEach(el => {
            const row = document.createElement('tr');
            row.innerHTML = `
      <td>${el.room}</td>
    `;
            tbody.appendChild(row);
        });

        table.appendChild(tbody);

        container.innerHTML = ''; // svuota il form
        container.appendChild(table); // inserisce la tabella
    }




    const content = {
        CreaClasse: {
            title: "Crea Nuova Classe",
            desc: `
                <p>Utilizza il modulo sottostante per creare una nuova classe per l'organizzazione dei tuoi studenti.</p>
                <form id="create-class-form" action="/classe/professore/crea" method="post">
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
                <p>Crea un nuovo laboratorio per una classe inserendo il nome, una breve descrizione (255 caratteri), la flag di completamento ed il docker compose file.</p>
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
        "Visualizza Studenti": {
            title: "Visualizza Studenti",
            desc: `
                <p>Visualizza la lista di studenti per classe</p>
                <form id="form-studenti">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                    <div class="form-group">
                        <label for="classSelect">Nome classe:</label>
                        <select id="classSelect" name="classeId">
                            <option value="">Caricamento classi...</option>
                        </select>
                    </div>
                    <button type="submit">Visualizza</button>
                </form>
                `
        },
        "Visualizza Laboratori": {
            title: "Visualizza Laboratori",
            desc: `
                <p>Visualizza la lista di laboratori per classe</p>
                <form id="form-lab">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
                    <div class="form-group">
                        <label for="classSelect">Nome classe:</label>
                        <select id="classSelect" name="classeId">
                            <option value="">Caricamento classi...</option>
                        </select>
                    </div>
                    <button type="submit">Visualizza</button>
                </form>
                `
        },
        "Visualizza Risultati": {
            title: "Visualizza risultati",
            desc: `
                <p>Visualizza risultati per classe e laboratorio</p>
                <form id="form-risultati">
                    <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').getAttribute('content')}" />
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
        },
        "Cambia Password": {
            title: "Cambia Password",
            desc: `
                <p>Cambia la password del professore</p>
                <form id="changePassword-form" action="/account/professore/changePassword" method="post">
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
                } else if (key === "Visualizza Risultati") {
                    descEl.innerHTML = content[key].desc;
                    caricaRisultati("classSelect", "roomSelect");

                } else if (key === "Visualizza Studenti") {
                    descEl.innerHTML = content[key].desc;
                    caricaStudenti("classSelect", "roomSelect")

                } else if (key === "Visualizza Laboratori") {
                    descEl.innerHTML = content[key].desc;
                    caricaLaboratori("classSelect", "roomSelect")
                } else if (key === "Cambia Password") {

                    descEl.innerHTML = content[key].desc;
                    setTimeout(() => {
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




