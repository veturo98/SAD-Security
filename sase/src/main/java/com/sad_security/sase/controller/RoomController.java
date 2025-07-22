package com.sad_security.sase.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sad_security.sase.model.RoomAvviata;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * Controller per le richieste di room management
 */
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * Gestione della richiesta di avvio della room.
     *
     * @param startRoom corpo della richiesta con nome classe, nome laboratorio e utente
     * @return mappa contenente messaggio, comando e tipo di esito (success/error)
     * @throws InterruptedException in caso di interruzione dell'esecuzione asincrona
     * @throws ExecutionException in caso di errore durante l'esecuzione asincrona
     */
    @PostMapping("/studente/start")
    public Map<String, String> startRoom(@RequestBody startRoomBody startRoom)
            throws InterruptedException, ExecutionException {

        String classe = startRoom.getNomeClass();
        String lab = startRoom.getNomeLab();
        String utente = startRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + startRoom);

        boolean isPresent = roomService.VerificaRoomAvviata(classe, lab, utente);

        CompletableFuture<startRoomResponse> esito_avvio = roomService.startContainerAsync(classe, lab, utente);

        Map<String, String> res = new HashMap<>();

        if ("success".equals(esito_avvio.get().type)) {

            if (!isPresent) {
                boolean esito_salvataggio = roomService.SalvaRoomAvviata(classe, lab, utente);

                if (!esito_salvataggio) {
                    System.out.println("Errore nel salvataggio della room");
                }
            }

            res.put("msg", esito_avvio.get().msg);
            res.put("command", esito_avvio.get().command);
            res.put("type", "success");
        } else {
            res.put("msg", esito_avvio.get().msg);
            res.put("type", "error");
        }

        return res;
    }

    /**
     * Gestione della richiesta di stop della room.
     *
     * @param stopRoom corpo della richiesta con utente
     * @return mappa contenente messaggio e tipo di esito (success/error)
     * @throws InterruptedException in caso di interruzione dell'esecuzione asincrona
     * @throws ExecutionException in caso di errore durante l'esecuzione asincrona
     * @throws JsonMappingException in caso di errore nella mappatura JSON
     * @throws JsonProcessingException in caso di errore di processing JSON
     */
    @PostMapping("/studente/stop")
    public Map<String, String> stopRoom(@RequestBody stopRoomBody stopRoom)
            throws InterruptedException, ExecutionException, JsonMappingException, JsonProcessingException {

        String utente = stopRoom.getUtente();
        System.out.println("Sono il controller ed ho ricevuto questo " + stopRoom);

        CompletableFuture<String> response = roomService.stopContainer(utente);
        String result = response.get();
        System.out.println("result" + result);

        Map<String, String> res = new HashMap<>();

        if (result == null || result.isEmpty()) {
            res.put("msg", "Impossibile stoppare il container");
            res.put("type", "error");
        } else {
            ObjectMapper mapper = new ObjectMapper();
            res = mapper.readValue(result, new TypeReference<Map<String, String>>() {
            });
        }

        return res;
    }

    /**
     * Gestione della richiesta di creazione della room.
     *
     * @param classeNome nome della classe da associare
     * @param roomName nome della room da creare
     * @param descrizione descrizione del laboratorio
     * @param flag flag di configurazione
     * @param yamlFile file YAML di configurazione
     * @return mappa contenente messaggio e tipo di esito (success/error)
     */
    @PostMapping("/professore/creaRoom")
    @ResponseBody
    public Map<String, String> creaRoom(
            @RequestParam("classeId") String classeNome,
            @RequestParam("room") String roomName,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("flag") String flag,
            @RequestParam("yamlFile") MultipartFile yamlFile) {

        Map<String, String> response = new HashMap<>();

        try {
            String originalFilename = yamlFile.getOriginalFilename();

            if (yamlFile.isEmpty() ||
                    originalFilename == null ||
                    (!originalFilename.endsWith(".yaml") &&
                            !originalFilename.endsWith(".yml"))) {
                response.put("message", "File non valido. Deve essere un file .yaml o .yml");
                response.put("type", "error");
                return response;
            }

            String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Contenuto YAML:\n" + yamlContent);

            if (roomService.isPresent(roomName)) {
                response.put("message", "Nome room già esistente.");
                response.put("type", "error");
                return response;
            }

            roomService.salvaNuovaRoom(roomName, descrizione, flag);

            roomService.aggiungiRisorsaServer(roomName, yamlFile);
            
            boolean associazione = roomService.aggiungiassociazione(classeNome, roomName);
            if (associazione) {
                response.put("message", "L'associazione tra questa classe e room è già stata fatta");
                response.put("type", "error");
                return response;
            }

            response.put("message", "Laboratorio creato con successo.");
            response.put("type", "success");
        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "Errore durante il caricamento del file.");
            response.put("type", "error");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Errore nella creazione del laboratorio.");
            response.put("type", "error");
        }

        return response;
    }

    /**
     * Gestione della richiesta di controllo dell'esistenza della room.
     *
     * @param roomName nome della room da verificare
     * @return mappa contenente messaggio e tipo di esito (success/error)
     */
    @GetMapping({ "/professore/checkroom", "/studente/checkroom" })
    @ResponseBody
    public Map<String, String> checkroomRoomName(@RequestParam String roomName) {

        boolean exists = roomService.checkRoom(roomName);
        Map<String, String> response = new HashMap<>();
        if (exists) {
            response.put("message", "La room esiste già");
            response.put("type", "error");
        } else {
            response.put("message", "Nome room disponibile");
            response.put("type", "success");
        }

        return response;
    }

    /**
     * Gestione della richiesta della descrizione della room.
     *
     * @param nomeRoom nome della room
     * @return mappa contenente descrizione e tipo di esito (success/error)
     */
    @GetMapping("/studente/getDescrizioneRoom")
    public Map<String, String> getDescrizioneRoom(@RequestParam String nomeRoom) {

        String descrizione = roomService.getDescrizione(nomeRoom);

        Map<String, String> res = new HashMap<>();

        if (descrizione == null || descrizione.isEmpty()) {
            res.put("msg", "Impossibile stoppare il container");
            res.put("type", "error");
        } else {
            res.put("descrizione", descrizione);
            res.put("type", "success");
        }

        return res;
    }

    /**
     * Gestione della richiesta delle room associate ad una classe.
     *
     * @param nomeClasse nome della classe
     * @return lista di nomi delle room associate
     */
    @GetMapping({ "studente/getRoomsPerClasse", "professore/getRoomsPerClasse" })
    public ResponseEntity<List<String>> getRoomsPerClasse(@RequestParam String nomeClasse) {
        List<String> rooms = roomService.getRoomListbyClasse(nomeClasse);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Gestione della richiesta di inserimento della flag.
     *
     * @param room nome della room
     * @param studente nome dello studente
     * @param flag flag inserita dallo studente
     * @return mappa contenente esito e tipo di esito (success/error)
     */
    @PostMapping("/studente/flag")
    public Map<String, String> InserimentoFlag(@RequestParam String room, @RequestParam String studente,
            @RequestParam String flag) {

        LocalDateTime submitTime = LocalDateTime.now();

        LocalDateTime startTime = roomService.flagCorretta(studente, room, flag);

        Map<String, String> response = new HashMap<>();

        if (startTime != null) {

            roomService.calcoloScore(room, studente, submitTime, startTime);

            response.put("esito", "Flag corretta!");
            response.put("type", "success");

        } else {
            response.put("esito", "Flag errata");
            response.put("type", "error");
        }

        return response;
    }

    /**
     * Gestione della richiesta di visualizzazione dei risultati.
     *
     * @param classe nome della classe
     * @param room nome della room
     * @return lista di mappe con studente e score
     */
    @PostMapping({ "/professore/risultati/visualizza", "/studente/risultati/visualizza" })
    @ResponseBody
    public List<Map<String, Object>> visualizzazioneRisultati(
            @RequestParam("classeId") String classe,
            @RequestParam("roomId") String room) {

        List<RoomAvviata> risultati = roomService.getRoombyClasseAndRoom(classe, room);

        if (risultati == null || risultati.isEmpty()) {
            return Collections.emptyList();
        }

        return risultati.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studente", r.getStudente());
                    map.put("score", r.getScore());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gestione della richiesta dei laboratori di una classe.
     *
     * @param classe nome della classe
     * @return lista di mappe con i nomi delle room
     */
    @PostMapping("/professore/laboratori")
    @ResponseBody
    public List<Map<String, Object>> getLaboratori(@RequestParam("classeId") String classe) {

        List<RoomClasse> laboratori = roomService.trovaLaboratori(classe);

        return laboratori.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("room", r.getRoom());
                    return map;
                })
                .collect(Collectors.toList());

    }

    /* DICHIARAZIONE DEI TIPI DELLE RICHIESTE */
    @Data
    @AllArgsConstructor
    public static class startRoomBody {

        private String nomeClass;
        private String nomeLab;
        private String utente;

    }

    @Data
    @AllArgsConstructor
    public static class stopRoomBody {
        private String utente;

    }

    @Data
    @AllArgsConstructor
    public static class createRoomBody {

        private String nomeLab;
        private MultipartFile yamlFile;

    }

    @Data
    @AllArgsConstructor
    public static class patternRisultato {
        private String studente;
        private String score;
    }

    @Data
    @AllArgsConstructor
    public static class startRoomResponse {
        private String msg;
        private String command;
        private String type;
    }

}
