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
        if (!select) return;

        // chiama il controller che interroga il DB
        fetch('/classe/getClassi')
            .then(response => response.json())
            .then(classNames => {
                const select = document.getElementById('classSelect');
                select.innerHTML = ''; // svuota se necessario

                classNames.forEach(nome => {
                    const option = document.createElement('option');
                    option.value = nome;
                    option.textContent = nome;
                    select.appendChild(option);
                });
            })
            .catch(console.error);
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

            fetch(`/room/checkroom?roomName=${encodeURIComponent(roomName)}`)
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
    //bisogna controllare che il nome del laboratorio non esista già
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
                <form id="create-lab-form" action="/room/creaLaboratorio" method="post">
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
            desc:`
                <p>Aggiungi uno studente ad una classe esistente, inserendo nome, matricola e classe di appartenenza.</p>
                `
        },
        "Conferma Richieste": {
            title: "Conferma Richieste",
            desc:`
                <p>Visualizza e approva/rifiuta le richieste di iscrizione o partecipazione agli esami/laboratori.</p>
                `
        },
        "Pubblica risultati": {
            title: "Pubblica Risultati",
            desc:`
                <p>Inserisci e pubblica i risultati degli esami o laboratori per la visione degli studenti.</p>
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
