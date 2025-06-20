# run_compose.py
import os
import sys
import subprocess

def run_docker_compose(classe, room, porta):
    """
    Esegue il comando 'docker compose up' in una sottodirectory specifica.

    Args:
        classe (str): Il nome della classe (prima parte del percorso).
        room (str): Il nome della stanza (seconda parte del percorso).
    """
    # ... tutta la logica del tuo script ...
    original_cwd = os.getcwd()
    print(f"Directory di lavoro originale: {original_cwd}")

    base_path = original_cwd
    compose_dir = os.path.join(base_path, classe, room)

    if not os.path.isdir(compose_dir):
        print(f"Errore: La directory Docker Compose '{compose_dir}' non esiste.")
        # Non usare sys.exit(1) qui se vuoi che il chiamante possa gestire l'errore
        # Potresti invece sollevare un'eccezione o restituire False
        raise FileNotFoundError(f"La directory '{compose_dir}' non esiste.")

    os.chdir(compose_dir)
    print(f"Directory di lavoro cambiata in: {os.getcwd()}")

    command_exit_code = 0

    # Setto la variabile relativa alla porta
    os.environ["PORTA"] = str(porta)

    try:
        subprocess.run(["docker", "compose", "up", "-d", "--wait"], check=True, capture_output=False)
        print("Comando Docker Compose eseguito con successo.")
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
        os.chdir(original_cwd)
        print(f"Tornato alla directory originale: {os.getcwd()}")

    return command_exit_code # Restituisce il codice di uscita

if __name__ == "__main__":
    # Questa parte viene eseguita SOLO quando lo script è lanciato direttamente
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