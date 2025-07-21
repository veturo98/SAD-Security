import os
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
    print(f"Ricevuta richiesta per avviare dall'utente: {utente}")

    #Seleziono la porta
    porta = utility.scelta_random_porta(1024, 49151, utente, room, classe)

    time.sleep(1)

    #Esecuzione dello script di avvio dei container
    try:

        # Faccio un compose down nella cartella running per evitare sovrascritture
        print(compose.stop_docker_compose(utente))

        # Lancio il compose
        result = compose.run_docker_compose(utente, classe, room, porta)

        if (result != 0):
            return jsonify({"msg" : "Errore durante l'avvio del laboratorio, riprovare più tardi", "type" : "error"}), 200
    except FileNotFoundError as e:
         print(f"Errore: {e}")
         return jsonify({"msg" : "File non trovato", "type" : "success"}), 200
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")
        return jsonify({"msg" : e.stderr, "type" : "success"}), 200

    #########   #########   #########   #########   #########   #########   #########   #########
    # ATTENZIONE - il campo IP deve contenere l'indirizzo del nodo che esegue il server.py 
    ip = "localhost"
    return jsonify({"msg" : f"Room {room} avviata con successo per l'utente {utente}! Per il collegamento usa il comando nella box.", "command" : f"ssh -p {porta} root@{ip}", "type" : "success"}), 200
    #########   #########   #########   #########   #########   #########   #########   #########


# FUNZIONE DI STOP DEI CONTAINER
def stop_container():

    data = request.get_json()
    if not data:
        return None, 400

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
         return jsonify({"msg" : "File non trovato", "type" : "success"}), 200
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")
        return jsonify({"msg" : "Errore nell'esecuzione del comando", "type" : "success"}), 200

    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return jsonify({"msg" : "Container distrutto con successo", "type" : "success"}), 200


# CREAZIONE ROOM
def crea_room():
    
    # Ricevo i dati dal form
    nomeRoom = request.form.get('nomeLab')
    yaml_file = request.files.get('yamlFile')

    if not nomeRoom or not yaml_file:
        return jsonify({"error": "Parametri mancanti"}), 400

    print(f"Ricevuto nome room: {nomeRoom}")
    print(f"Nome file: {yaml_file.filename}")

    # Costruisco i path
    base_path = os.getcwd()
    room_path = os.path.join(base_path, "Room", nomeRoom)
    os.makedirs(room_path, exist_ok=True)

    file_path = os.path.join(room_path, 'docker-compose.yml')
    yaml_file.save(file_path)

    print(f"File YAML salvato in: {file_path}")
    return jsonify({"message": "Room creata con successo!"}), 200


