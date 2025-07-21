# FUNZIONI DI UTILITY
import socket
import random

# Trova la prima porta libera sull'host (non si può usare nel caso dell'applicazione containerizzata)
def trova_prima_porta_libera(start_port, end_port, host='127.0.0.1'):
    for port in range(start_port, end_port + 1):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            try:
                s.bind((host, port))
                return port
            except OSError:
                continue
    return None  # Nessuna porta libera trovata

# Sanificazione input nome utente
def sostituisci_spazi_con_hyphen(s):
    if ' ' in s:
        return s.replace(' ', '-')
    return s

# Sceglie in maniera casuale la porta da utilizzare
def scelta_random_porta (start_port, end_port, utente, room, classe):

    seed = utente + room + classe

    # Setto come seed il nome dell'utente
    random.seed(seed)
    
    # Scelgo in maniera casuale la porta in un intervallo 
    porta_selezionata = random.randint(start_port, end_port)

    return porta_selezionata