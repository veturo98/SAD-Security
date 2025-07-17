

# FUNZIONI DI UTILITY
# Trova la prima porta disponibile
import socket


def trova_prima_porta_libera(start_port, end_port, host='127.0.0.1'):
    for port in range(start_port, end_port + 1):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            try:
                s.bind((host, port))
                return port
            except OSError:
                continue
    return None  # Nessuna porta libera trovata
