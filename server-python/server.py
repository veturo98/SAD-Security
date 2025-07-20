from flask import Flask
import container


app = Flask(__name__)


# MAPPING DELLE ROTTE

####    START CONTAINER     ####
@app.route('/start-container/', methods=['POST'])
def start():
    return container.start_container()


####    STOP CONTAINER     ####
@app.route('/stop-container/', methods=['POST'])
def stop():
    return container.stop_container()


####    CREA ROOM          ####
@app.route('/crea-room/', methods=['POST'])
def crea():
    return container.crea_room()


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=1234)
