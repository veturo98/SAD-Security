import socket
import subprocess
import time
import compose
import utility

from flask import jsonify, request



# AVVIO DEI CONTAINER
def start_container():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Dati JSON mancanti o non validi nel corpo della richiesta"}), 400

    classe = data.get('nomeClass')
    room = data.get('nomeLab')
    utente = data.get('utente')
    
    
    print(f"Ricevuta richiesta per avviare il container: {room}")
    print(f"Ricevuta richiesta per avviare la classe: {classe}")
    print(f"Ricevuta richiesta per avviare la classe: {utente}")

    #Seleziono la porta
    porta = str(utility.trova_prima_porta_libera(1024, 49151))
    if porta:
        print(f"Uso della porta {porta}")
    else:
        print("Nessuna porta libera trovata.")


    time.sleep(1)

    #Esecuzione dello script di avvio dei container
    try:
        # Lancio il compose
        result = compose.run_docker_compose(utente, classe, room, porta)
        print(result)
    except FileNotFoundError as e:
         print(f"Errore: {e}")
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")

    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container '{classe}' e '{room}' per l'utente' {utente}' avviati con successo", 200)


# FUNZIONE DI STOP DEI CONTAINER
def stop_container():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Dati JSON mancanti o non validi nel corpo della richiesta"}), 400

    utente = data.get('utente')
    

    
    print(f"Ricevuta richiesta per distruggere il container dell'utente: {utente}")

    time.sleep(1)

    #Esecuzione dello script di distruzione dei container
    try:
        # Lancio il compose
        result = compose.stop_docker_compose(utente)
        print(result)
    except FileNotFoundError as e:
         print(f"Errore: {e}")
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")

    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container per l'utente' {utente}' distrutto con successo", 200)


