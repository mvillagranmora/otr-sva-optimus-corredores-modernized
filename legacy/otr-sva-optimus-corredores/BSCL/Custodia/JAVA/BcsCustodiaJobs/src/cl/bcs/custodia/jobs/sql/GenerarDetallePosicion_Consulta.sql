SELECT 
    ConsultaTarjeta."FechaProceso", 
    ConsultaTarjeta."TipoUI",
    ConsultaTarjeta."IdClasificacionTipoInstrumento",
    ConsultaTarjeta."Nemotecnico", 
    ConsultaTarjeta."CantidadIngreso", 
    ConsultaTarjeta."CantidadEgreso", 
    ConsultaTarjeta."SaldoCantidad", 
    ConsultaTarjeta."MontoIngreso", 
    ConsultaTarjeta."MontoEgreso",
    ConsultaTarjeta."SaldoMontoPrecioAdquisicion",
    CASE WHEN ConsultaTarjeta."TipoUI" = 'VENTA' THEN
    	Cartera."EGRFolioComprobante" 
    ELSE Cartera."FolioComprobante"
    END AS "NumeroPoliza",
    Cartera."IdBolsa",
    Instrumento."IdMoneda",
	CASE 
		WHEN Instrumento."OrigenInstrumento" = 'P' THEN 'PERUANO'
		WHEN Instrumento."OrigenInstrumento" = 'E' THEN 'EXTRANJERO'
		ELSE ''
	END AS "OrigenInstrumento",
	CASE 
		WHEN Instrumento."PresenciaBursatilAjustada" > 0 THEN 'SI' 
		ELSE 'NO' 
	END AS "PresenciaBursatil",
	CASE 
		WHEN Instrumento."TipoRenta" = 'A' THEN 'AFECTOS'
		WHEN Instrumento."TipoRenta" = 'E' THEN 'EXONERADOS'
		WHEN Instrumento."TipoRenta" = 'I' THEN 'INAFECTOS'
		ELSE ''
	END AS "TipoRenta",
	CASE 
		WHEN Instrumento."PactoAndino" = 'S' THEN 'SI'
		WHEN Instrumento."PactoAndino" = 'N' THEN 'NO'
		ELSE 'NO APLICA'
	END AS "PactoAndino"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia" AS ConsultaTarjeta
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento ON Instrumento."IdInstrumento" = ConsultaTarjeta."IdInstrumento" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS Cartera ON Cartera."FolioCartera" = ConsultaTarjeta."FolioCartera"
WHERE ConsultaTarjeta."IdEstado" = 'VIG' AND ConsultaTarjeta."IdEmpresa" = :p_IdEmpresa
    AND ConsultaTarjeta."FechaProceso" BETWEEN :p_FechaConsultaDesde AND :p_FechaConsultaHasta
    AND (:p_IdInstrumento IS NULL OR ConsultaTarjeta."IdInstrumento" = :p_IdInstrumento)
    AND (:p_IdConcepto IS NULL OR ConsultaTarjeta."IdConcepto" = :p_IdConcepto)
ORDER BY "Nemotecnico", "FechaProceso", "IdTarjetaExistencia"  ASC