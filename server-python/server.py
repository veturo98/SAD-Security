from flask import Flask
import time
import subprocess
import os


app = Flask(__name__)

@app.route('/start-container/<string:classe>/<string:room>', methods=['POST'])
def start_container(classe,room):
    
    print(f"Ricevuta richiesta per avviare il container: {room}")
    print(f"Ricevuta richiesta per avviare la classe: {classe}")
    time.sleep(1)

    try:
        result = subprocess.run(['./Esecuzionecompose.sh', classe, room], capture_output=True, text=True, check=True)
        print(result.stdout)
    except subprocess.CalledProcessError as e:
        print(f"Errore durante l'esecuzione del comando: {e}")
        print(f"Errore standard: {e.stderr}")
    
    # # 1. Salva la directory di lavoro corrente all'inizio della funzione
    # original_cwd = os.getcwd()
    # print(f"Directory di lavoro originale: {original_cwd}")

    # base_path = os.path.dirname(os.path.abspath(__file__)) # Directory dello script Flask
    # compose_dir = os.path.join(base_path, classe, room)

    # # 2. Cambia la directory di lavoro del processo Flask
    # os.chdir(compose_dir)
    # print(f"Directory di lavoro cambiata in: {os.getcwd()}")

    # # Esegui il comando docker compose
    # print(f"Esecuzione di 'docker compose up -d' in {compose_dir}")

    # ## Avvio comando docker 
    # try:
    #     result = subprocess.run(['docker', 'compose', 'up'], capture_output=True, text=True, check=True)
    #     print(result.stdout)
    # except subprocess.CalledProcessError as e:
    #     print(f"Errore durante l'esecuzione del comando: {e}")
    #     print(f"Errore standard: {e.stderr}")

    # finally:
    #     os.chdir(original_cwd)
    #     print(f"Tornato alla directory originale: {os.getcwd()}")


    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container '{classe}' e '{room}' avviati con successo", 200)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=1234)





