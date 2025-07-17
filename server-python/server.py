from flask import Flask, request, jsonify
import time
import subprocess
import compose
import socket
import container


app = Flask(__name__)

####    START CONTAINER     ####
@app.route('/start-container/', methods=['POST'])
def start():
    return container.start_container()


####    STOP CONTAINER     ####
@app.route('/stop-container/', methods=['POST'])
def stop():
    return container.stop_container()


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=1234)
