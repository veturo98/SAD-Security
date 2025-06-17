#!/bin/bash

# --- Inizio configurazione per lo script Bash ---

# Definisci CLASSE e ROOM.
# Opzione 1: Definisci qui i valori fissi per il test
CLASSE="$1"
ROOM="$2"

# Opzione 2: Se vuoi passare CLASSE e ROOM come argomenti al tuo script Bash:
# (ad es. ./start_compose.sh Scienze Laboratorio1)
# CLASSE="$1"
# ROOM="$2"
# if [ -z "$CLASSE" ] || [ -z "$ROOM" ]; then
#     echo "Uso: $0 <classe> <room>"
#     exit 1
# fi

# --- Fine configurazione ---


# Corrisponde a 'original_cwd = os.getcwd()'
ORIGINAL_CWD=$(pwd)
echo "Directory di lavoro originale: $ORIGINAL_CWD"

# Corrisponde a 'base_path = os.path.dirname(os.path.abspath(__file__))'
# In bash, se lo script è nella directory di base, $(pwd) è già la base_path.
# Se lo script bash si trova in una sottocartella e vuoi risalire, usa:
# BASE_PATH=$(dirname "$(readlink -f "$0")") # Directory dove si trova lo script Bash stesso
BASE_PATH=$(pwd) # Assumiamo che il tuo script Flask (e questo Bash) siano nella directory radice del progetto

# Corrisponde a 'compose_dir = os.path.join(base_path, classe, room)'
COMPOSE_DIR="${BASE_PATH}/${CLASSE}/${ROOM}"

# Verifica se la directory esiste prima di provare a entrarci
if [ ! -d "$COMPOSE_DIR" ]; then
    echo "Errore: La directory Docker Compose '$COMPOSE_DIR' non esiste."
    exit 1 # Esce dallo script con un codice di errore
fi

# Corrisponde a 'os.chdir(compose_dir)'
cd "$COMPOSE_DIR"
echo "Directory di lavoro cambiata in: $(pwd)"

echo "Esecuzione di 'docker compose up' in $(pwd)"

# Corrisponde a 'try...except subprocess.CalledProcessError as e:'
# 'docker compose up' senza '-d' esegue in foreground.
# 'set -e' farebbe uscire lo script immediatamente in caso di errore.
# Per gestire l'errore manualmente e tornare alla directory originale:
docker compose up -d --wait
COMMAND_EXIT_CODE=$? # Cattura il codice di uscita dell'ultimo comando

if [ "$COMMAND_EXIT_CODE" -eq 0 ]; then
    echo "Comando Docker Compose eseguito con successo."
else
    echo "Errore durante l'esecuzione del comando Docker Compose."
    echo "Codice di uscita: $COMMAND_EXIT_CODE"
    # Puoi aggiungere qui l'output di stderr se il comando lo stampa automaticamente
fi

# Corrisponde a 'finally: os.chdir(original_cwd)'
# Questo blocco verrà sempre eseguito, garantendo il ritorno alla directory originale
# indipendentemente dall'esito del comando docker compose.
cd "$ORIGINAL_CWD"
echo "Tornato alla directory originale: $(pwd)"

# Puoi aggiungere un 'exit $COMMAND_EXIT_CODE' qui se vuoi che lo script Bash
# restituisca il codice di uscita del comando docker compose.
exit "$COMMAND_EXIT_CODE"