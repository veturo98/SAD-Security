from flask import Flask

app = Flask(__name__)

@app.route('/start-container/<string:name>', methods=['POST'])
def start_container(name):
    print(f"Ricevuta richiesta per avviare il container: {name}")
    
    # Qui potresti aggiungere la logica per avviare il container Docker, ad esempio con docker-py
    return (f"Container '{name}' avviato con successo", 200)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
