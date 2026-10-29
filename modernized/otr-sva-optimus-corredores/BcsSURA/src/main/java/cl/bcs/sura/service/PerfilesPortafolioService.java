package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilesPortafolioService {

    // B-SURA-01: portfolios APV cannot have their forma-cobro updated
    public static List<IdsEntidadYPerfiles> validarIndicadorFormaCobroPortafolioApv(
            List<IdsEntidadYPerfiles> perfiles) {
        for (IdsEntidadYPerfiles p : perfiles) {
            if ("PROPOSITOAPV".equals(p.proposito)) {
                p.actualizarFormaCobro = "N";
            }
        }
        return perfiles;
    }

    // B-SURA-02: entries needing INSERT (new indicators, IdIndicador == 0)
    public static List<IdsEntidadYPerfiles> filtrarParaIngresarIndicadores(
            List<IdsEntidadYPerfiles> perfiles) {
        return perfiles.stream()
                .filter(p -> p.idIndicador == 0)
                .collect(Collectors.toList());
    }

    // B-SURA-03: entries needing UPDATE (existing indicators, ActualizarFormaCobro == "S")
    public static List<IdsEntidadYPerfiles> filtrarParaModificarIndicadores(
            List<IdsEntidadYPerfiles> perfiles) {
        return perfiles.stream()
                .filter(p -> p.idIndicador != 0 && "S".equals(p.actualizarFormaCobro))
                .collect(Collectors.toList());
    }

    // B-SURA-04: merge IdFormaCobro from input into DB result (empty → fill from input)
    public static List<IdsEntidadYPerfiles> fusionarFormaCobro(
            List<IdsEntidadYPerfiles> entidadesDB,
            List<IdsEntidadYPerfiles> inputConFormaCobro) {
        entidadesDB.forEach(entidad -> {
            inputConFormaCobro.stream()
                    .filter(id -> id.idPortafolio.equals(entidad.idPortafolio))
                    .findFirst()
                    .ifPresent(match -> {
                        if (entidad.idFormaCobro.isEmpty()) {
                            entidad.idFormaCobro = match.idFormaCobro;
                        }
                    });
        });
        return entidadesDB;
    }

    // B-SURA-05: remove entries with no forma-cobro assigned
    public static List<IdsEntidadYPerfiles> filtrarConIdFormaCobro(
            List<IdsEntidadYPerfiles> perfiles) {
        return perfiles.stream()
                .filter(p -> !p.idFormaCobro.isEmpty())
                .collect(Collectors.toList());
    }

    // B-SURA-06: set IdFormaCobro from COBROCUST indicator on all matching perfiles
    public static void aplicarFormaCobro(List<IdsEntidadYPerfiles> perfiles, String valorIndicador) {
        perfiles.forEach(p -> p.idFormaCobro = valorIndicador);
    }
}
