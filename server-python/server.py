from flask import Flask, request, jsonify
import time
import subprocess
import esecuzioneCompose
import socket


app = Flask(__name__)

# Funzione per la scelta della porta
# def is_port_free(port, host='127.0.0.1'):
#     with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
#         try:
#             s.bind((host, port))
#             return port
#         except OSError:
#             return None
#     return None  # Nessuna porta libera trovata


# Trova la prima porta disponibile
def trova_prima_porta_libera(start_port, end_port, host='127.0.0.1'):
    for port in range(start_port, end_port + 1):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            try:
                s.bind((host, port))
                return port
            except OSError:
                continue
    return None  # Nessuna porta libera trovata


####    SOLUZIONE CON SCRIPT PYTHON     ####
@app.route('/start-container/', methods=['POST'])
def start_container():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Dati JSON mancanti o non validi nel corpo della richiesta"}), 400

    classe = data.get('nomeClass')
    room = data.get('nomeLab')
    utente = data.get('utente')
    


    # ###     SCELTA DELLA PORTA CON ID    ###
    # id = data.get('id')
    # # Imponiamo il limite massimo alla porta 4000
    # if (id > 4000):
    #     # Se la porta è maggiore allora si riparte quella più vicina
    #     porta = 1024

    #     # Controllo se la porta è disponibile altrimenti uso quella più vicino disponibile
    #     while (is_port_free(porta) == None):
    #         porta = porta + 1
    # else:     
    #     # Altrimenti la porta è quella corrispondente a quella dell'id dell'utente
    #     porta = 1023 + id

    #     # Controllo se la porta è disponibile altrimenti uso quella più vicino disponibile
    #     while (is_port_free(porta) == None):
    #         porta = porta + 1
    
    
    
    print(f"Ricevuta richiesta per avviare il container: {room}")
    print(f"Ricevuta richiesta per avviare la classe: {classe}")
    print(f"Ricevuta richiesta per avviare la classe: {utente}")

    #Seleziono la porta
    porta = str(trova_prima_porta_libera(1024, 49151))
    if porta:
        print(f"Uso della porta {porta}")
    else:
        print("Nessuna porta libera trovata.")


    time.sleep(1)

    #Esecuzione dello script di avvio dei container
    try:
        # Lancio il compose
        result = esecuzioneCompose.run_docker_compose(classe, room, porta)
        print(result)
    except FileNotFoundError as e:
         print(f"Errore: {e}")
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")

    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container '{classe}' e '{room}' per l'utente' {utente}' avviati con successo", 200)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=1234)
