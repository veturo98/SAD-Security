from flask import Flask, request, jsonify
import time
import subprocess
import esecuzioneCompose

app = Flask(__name__)

####    AVVIO DEL LABORATORIO   ####
# @app.route('/start-container/<string:classe>/<string:room>', methods=['POST'])
# def start_container(classe,room):
    
#     print(f"Ricevuta richiesta per avviare il container: {room}")
#     print(f"Ricevuta richiesta per avviare la classe: {classe}")
#     time.sleep(1)

#     # Esecuzione dello script di avvio dei container
#     try:
#         result = subprocess.run(['./Esecuzionecompose.sh', classe, room], capture_output=True, text=True, check=True)
#         print(result.stdout)
#     except subprocess.CalledProcessError as e:
#         print(f"Errore durante l'esecuzione del comando: {e}")
#         print(f"Errore standard: {e.stderr}")

#     # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
#     return (f"Container '{classe}' e '{room}' avviati con successo", 200)



####    SOLUZIONE CON SCRIPT PYTHON #####
@app.route('/start-container/', methods=['POST'])
def start_container():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Dati JSON mancanti o non validi nel corpo della richiesta"}), 400

    classe = data.get('nomeClass')
    room = data.get('nomeLab')
    
    print(f"Ricevuta richiesta per avviare il container: {room}")
    print(f"Ricevuta richiesta per avviare la classe: {classe}")
    time.sleep(1)

    #Esecuzione dello script di avvio dei container
    try:
        result = esecuzioneCompose.run_docker_compose(classe, room)
        print(result)
    except FileNotFoundError as e:
         print(f"Errore: {e}")
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")

    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container '{classe}' e '{room}' avviati con successo", 200)





if __name__ == '__main__':
    app.run(host='0.0.0.0', port=1234)





