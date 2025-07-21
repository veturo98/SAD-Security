# run_compose.py
import os
import shutil
import sys
import subprocess
import utility

def run_docker_compose(utente, classe, room, porta):
    """
    Esegue il comando 'docker compose up' in una sottodirectory specifica.

    Args:
        classe (str): Il nome della classe (prima parte del percorso).
        room (str): Il nome della stanza (seconda parte del percorso).
    """

    #room = utility.sostituisci_spazi_con_hyphen(room)

    # Lavoro per ottenere i path
    base_path = os.getcwd()
    print(f"Directory di lavoro : {base_path}")

    # Costruisco il path del laboratorio da copiare e della cartella utente
    lab_path = os.path.join(base_path, "Room", room, "docker-compose.yml")
    utente_dir = os.path.join(base_path, "Running", utente)

    # Creo la cartella dove mettere il docker compose file, se esiste già non è un problema
    os.makedirs(utente_dir, exist_ok=True)

    # Sposto il file nella cartella giusta
    try:
    # Se il file di destinazione esiste lo sovrascrivo.
        shutil.copy(lab_path, utente_dir)
        print(f"File '{lab_path}' copiato con successo in '{utente_dir}'")

    except FileNotFoundError:
        print(f"Errore: Il file sorgente '{lab_path}' non è stato trovato.")
        return 1
    except PermissionError:
        print(f"Errore: Permesso negato per copiare il file in '{utente_dir}'.")
        return 1
    except Exception as e:
        print(f"Si è verificato un errore durante la copia del file: {e}")
        return 1



    # Se la cartella non esiste alzo un eccezione
    if not os.path.isdir(utente_dir):
        print(f"Errore: La directory Docker Compose '{utente_dir}' non esiste.")
        # Non usare sys.exit(1) qui se vuoi che il chiamante possa gestire l'errore
        # Potresti invece sollevare un'eccezione o restituire False
        raise FileNotFoundError(f"La directory '{utente_dir}' non esiste.")

    # Mi sposto nella cartella
    os.chdir(utente_dir)
    print(f"Directory di lavoro cambiata in: {os.getcwd()}")

    # Setto la variabile relativa alla porta
    os.environ["PORTA"] = str(porta)

    # Lancio il docker compose up
    try:
        esito = subprocess.run(["docker", "compose", "up", "-d", "--wait"], check=True, capture_output=False)
        print("Comando Docker Compose eseguito con successo.")
        command_exit_code = esito.returncode
    except subprocess.CalledProcessError as e:
        print("Errore durante l'esecuzione del comando Docker Compose.")
        print(f"Codice di uscita: {e.returncode}")
        command_exit_code = e.returncode
        raise # Rilancia l'eccezione per il chiamante
    except FileNotFoundError:
        print("Errore: Il comando 'docker' o 'docker compose' non è stato trovato.")
        print("Assicurati che Docker sia installato e nel tuo PATH.")
        command_exit_code = 127
        raise # Rilancia l'eccezione
    finally:
        os.chdir(base_path)
        print(f"Tornato alla directory originale: {os.getcwd()}")
        return command_exit_code # Restituisce il codice di uscita

    

def stop_docker_compose(utente):
    """
    Esegue il comando 'docker compose down' in una sottodirectory specifica.

    Args:
        utente (str): Il nome dell'utente (definisce la posizione dell'unico compose running per l'utente).
    """

    # Costruisco i path
    base_path = os.getcwd()
    print(f"Directory di lavoro originale: {base_path}")

    utente_dir = os.path.join(base_path, "Running", utente)


    if not os.path.isdir(utente_dir):
        #print(f"Errore: La directory Docker Compose '{utente_dir}' non esiste.")
        # Non usare sys.exit(1) qui se vuoi che il chiamante possa gestire l'errore
        # Potresti invece sollevare un'eccezione o restituire False
       # raise FileNotFoundError(f"La directory '{utente_dir}' non esiste.")
       print(f"Nuovo utente {utente}. Procedo alla creazione della directory.")
       return 0

    # Mi sposto nella cartella utente
    os.chdir(utente_dir)
    print(f"Directory di lavoro cambiata in: {os.getcwd()}")


    # Eseguo il compose down
    try:
        esito = subprocess.run(["docker", "compose", "down"], check=True, capture_output=False)
        print("Comando Docker Compose down eseguito con successo.")
        command_exit_code = esito.returncode
    except subprocess.CalledProcessError as e:
        print("Errore durante l'esecuzione del comando Docker Compose down.")
        print(f"Codice di uscita: {e.returncode}")
        command_exit_code = e.returncode
        raise # Rilancia l'eccezione per il chiamante
    except FileNotFoundError:
        print("Errore: Il comando 'docker' o 'docker compose' non è stato trovato.")
        print("Assicurati che Docker sia installato e nel tuo PATH.")
        command_exit_code = 127
        raise # Rilancia l'eccezione
    finally:
        os.chdir(base_path)
        print(f"Tornato alla directory originale: {os.getcwd()}")

    return command_exit_code # Restituisce il codice di uscita


# MAIN
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Uso: python script.py <CLASSE> <ROOM>")
        print("Esempio: python script.py MyProject WebApp")
        sys.exit(1)

    classe_arg = sys.argv[1]
    room_arg = sys.argv[2]

    try:
        run_docker_compose(classe_arg, room_arg)
    except Exception as e:
        print(f"Un errore non gestito ha interrotto l'esecuzione: {e}")
        sys.exit(1)
