package com.utfpr.estagio.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utfpr.estagio.dto.EmpresaComSupervisoresDto;
import com.utfpr.estagio.dto.EstagiosPorSemestreDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoEstagioDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoEstagio;
import com.utfpr.estagio.dto.QtdeEstagiosOrientadorDto;
import com.utfpr.estagio.dto.TiposEstagiosPorSemestreDto;
import com.utfpr.estagio.dto.UnidadeCedenteDto;
import com.utfpr.estagio.service.PraeService;

@RestController
@RequestMapping("/prae")
public class PraeController {

    @Autowired
    private PraeService praeService;

    /**
     * Retorna estatísticas de orientações por orientador e semestre
     * @return
     */
    @GetMapping("/estagios-por-orientador")
    public ResponseEntity<List<QtdeEstagiosOrientadorDto>> getEstatisticasOrientacoes() {
        try {
            List<QtdeEstagiosOrientadorDto> estatisticas = praeService.getEstatisticasOrientacoesPorSemestre();

            if (estatisticas == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     *
     * @return
     */
    @GetMapping("/estagios-por-semestre")
    public ResponseEntity<List<EstagiosPorSemestreDto>> getEstagiosPorSemestre() {
        try {
            List<EstagiosPorSemestreDto> estatisticas = praeService.getEstagiosIniciadosFinalizadosPorSemestre();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna estágios iniciados e finalizados por mês
     * @return
     */
    @GetMapping("/estagios-por-mes")
    public ResponseEntity<List<EstagiosPorSemestreDto>> getEstagiosPorMes() {
        try {
            List<EstagiosPorSemestreDto> estatisticas = praeService.getEstagiosIniciadosFinalizadosPorMes();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 
     * @return
     */
    @GetMapping("/tipos-estagios-por-semestre")
    public ResponseEntity<List<TiposEstagiosPorSemestreDto>> getTiposEstagiosPorSemestre() {
        try {
            List<TiposEstagiosPorSemestreDto> estatisticas = praeService.getTiposEstagiosPorSemestre();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/unidades-cedentes")
    public ResponseEntity<List<UnidadeCedenteDto>> getUnidadesCedentes() {
        try {
            List<UnidadeCedenteDto> unidades = praeService.getUnidadesCedentes();
            return ResponseEntity.ok(unidades);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna lista de empresas com supervisores agrupados hierarquicamente
     * @return
     */
    @GetMapping("/supervisores")
    public ResponseEntity<List<EmpresaComSupervisoresDto>> getSupervisores() {
        try {
            List<EmpresaComSupervisoresDto> empresasComSupervisores = praeService.getSupervisores();

            if (empresasComSupervisores == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            return ResponseEntity.ok(empresasComSupervisores);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna estatísticas de duração dos estágios
     * Inclui: média, mínima, máxima, primeiro quartil, mediana e terceiro quartil
     * @return
     */
    @GetMapping("/estatisticas-duracao")
    public ResponseEntity<EstatisticasDuracaoEstagioDto> getEstatisticasDuracao() {
        try {
            EstatisticasDuracaoEstagioDto estatisticas = praeService.getEstatisticasDuracaoEstagios();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna estatísticas de duração de estágios separadas por tipo (obrigatório/não obrigatório)
     * @return
     */
    @GetMapping("/estatisticas-duracao-por-tipo")
    public ResponseEntity<EstatisticasDuracaoPorTipoDto> getEstatisticasDuracaoPorTipo() {
        try {
            EstatisticasDuracaoPorTipoDto estatisticas = praeService.getEstatisticasDuracaoPorTipo();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna estatísticas de duração de estágios separadas por tipo de estágio e depois por tipo (obrigatório/não obrigatório)
     * @return
     */
    @GetMapping("/estatisticas-duracao-por-tipo-estagio")
    public ResponseEntity<EstatisticasDuracaoPorTipoEstagio> getEstatisticasDuracaoPorTipoEstagio() {
        try {
            EstatisticasDuracaoPorTipoEstagio estatisticas = praeService.getEstatisticasDuracaoPorTipoEstagio();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}