package com.apiGleyson.controleestacionamento.controlles;

import com.apiGleyson.controleestacionamento.dto.EstacionamentoDto;
import com.apiGleyson.controleestacionamento.model.EstacionamentoModel;
import com.apiGleyson.controleestacionamento.services.EstacionamentoService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController/* Classe de Controle - Criação dos Endpoints - Padrão MVC*/
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/estacionamento")
public class EstacionamentoController {

    final EstacionamentoService estacionamentoService;

    public EstacionamentoController(EstacionamentoService estacionamentoService) {
        this.estacionamentoService = estacionamentoService;
    }

    @PostMapping /*Serviço de salvamento do registro*/
    public ResponseEntity<Object> salvarEstacionamento(@RequestBody @Valid EstacionamentoDto estacionamentoDto){

        if(estacionamentoService.existsByLicensePlateCar(estacionamentoDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("já existe a placa cadastrada no estacionamento");
        }

        if(estacionamentoService.existsByParkingSpotNumber(estacionamentoDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("O estacionamento já está em uso");
        }

        if(estacionamentoService.existsByBlockAndApartment(estacionamentoDto.getBlock(), estacionamentoDto.getApartment())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("O Bloco e apartamento já estão cadastrados");
        }

        var estacionamentoModelo = new EstacionamentoModel();
        BeanUtils.copyProperties(estacionamentoDto, estacionamentoModelo);
        estacionamentoModelo.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(estacionamentoService.save(estacionamentoModelo));
    }

    @GetMapping("/apartment") //lista todas as vagas por apartamento
    public ResponseEntity<Object>  getAllVagasEstacionamentoPorApartamento(@RequestParam String apartment){

        List<EstacionamentoModel> litaVagasPorApartamento = estacionamentoService.findParkingSpotNumberByApartment(apartment);

        if(litaVagasPorApartamento.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não existe vagas pra esse apartamento");
        }

        return ResponseEntity.status(HttpStatus.OK).body(litaVagasPorApartamento);
    }

    @GetMapping
    public ResponseEntity<List<EstacionamentoModel>> getAllVagasEstacionamento(){
         return ResponseEntity.status(HttpStatus.OK).body(estacionamentoService.findAll());
    }

    @GetMapping("/{id}") /*Serviço de listagem dos registros*/
    public ResponseEntity<Object> getOneVagasEstacionamento(@PathVariable(value= "id") UUID id){
        Optional<EstacionamentoModel> estacionamentoModelOptional = estacionamentoService.findById(id);

        if(estacionamentoModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A vaga de estacionamento não existe.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(estacionamentoModelOptional.get());
    }

    @DeleteMapping("/{id}") /*Serviço de deleção*/
    public ResponseEntity<Object> deleteVagaEstacionamentoId(@PathVariable(value= "id") UUID id){
        Optional<EstacionamentoModel> estacionamentoModelOptional = estacionamentoService.findById(id);

        if(estacionamentoModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A vaga de estacionamento não existe.");
    }
        estacionamentoService.delete(estacionamentoModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Vaga removida com sucesso!!");
    }

    @PutMapping("/{id}")/*Serviço de atualização*/
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value= "id") UUID id,
                                                    @RequestBody @Valid EstacionamentoDto estacionamentoDto){
        Optional<EstacionamentoModel> estacionamentoModelOptional = estacionamentoService.findById(id);

        if(estacionamentoModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A vaga de estacionamento não encontrada.");
        }
        var estacionamentoModelo = new EstacionamentoModel();
        BeanUtils.copyProperties(estacionamentoDto, estacionamentoModelo);
        estacionamentoModelo.setId(estacionamentoModelOptional.get().getId());
        estacionamentoModelo.setRegistrationDate(estacionamentoModelOptional.get().getRegistrationDate());
        return ResponseEntity.status(HttpStatus.OK).body(estacionamentoService.save(estacionamentoModelo));
    }
}
