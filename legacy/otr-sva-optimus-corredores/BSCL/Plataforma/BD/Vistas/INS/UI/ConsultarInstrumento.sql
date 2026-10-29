--VISTA PARA EL BUSCADOR DE INSTRUMENTOS, NO USAR EN SPS!!
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento"  
AS WITH 
	ClasificadoraRiesgo AS (
		SELECT  i."IdInstrumento",
				RANK() over (PARTITION BY i."IdInstrumento" ORDER BY CR."Nivel" ASC) AS RANK,
				CR."ClasificacionRiesgo"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS i
		INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = i."IdInstrumento" 
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = i."IdTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS CR ON CR."IdTipoClasificacionRiesgo" = TICR."IdTipoClasificacionRiesgo" 
																		AND CR."ClasificacionRiesgo" = ICR."ValorClasificacionRiesgo" 
																		AND CR."IdEntidadClasificadoraRiesgo" = ICR."IdEntidadClasificadoraRiesgo"
		GROUP BY i."IdInstrumento",CR."Nivel",CR."ClasificacionRiesgo"
	)
SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado",
		"IdEmisor","DocumentoIdentidad","IdTipoReajuste","Reajustable","IdMoneda","IdMonedaEmision","IdTipoInstrumento","IdEstado","DecimalesPrecio","NombreTipoInstrumento",
		"IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento","NombreCompletoEmisor","Tamano","TipoValorizacion","IndicadorBolson","Tasa",
		"Precio","ValorizaPor","TieneCupon","Cantidad","PorcentajeCastigo","FrecuenciaCupon","PrecioStrike","InstrumentoAcreditado","CallConfirmado",
 		"TasaEmision","MercadoLocal","ValorClasificacionRiesgo","EsPutEsCall","IdTipoInstrumentoOrigen","AfectoArt107","AfectoArt104", "EsAutorizado", 
 		"CobroComisionesCustodia","IdInstrumentoRaiz", "EsInstrumentoCalificado"
FROM (
	SELECT   inst."IdInstrumento",
			 inst."Nemotecnico",
			 inst."ISIN",
			 inst."CorteMinimo",
			 inst."EstaInscrito",
			 inst."EsDivisible",
			 inst."RazonSocial",
			 inst."FechaEmision",
			 inst."FechaVencimiento",
			 inst."IdInstrumentoRelacionado",
			 inst."IdEmisor",
			 emi."DocumentoIdentidad",
			 inst."IdTipoReajuste",
			 inst."Reajustable",
			 inst."IdMoneda",
			 inst."IdMonedaEmision",
			 inst."IdTipoInstrumento",
			 inst."IdEstado",
			 inst."DecimalesPrecio",
			 tipoins."Nombre" AS "NombreTipoInstrumento",
			 tipoins."IdClasificacionTipoInstrumento",
			 clasi."Nombre" AS "NombreClasificacionTipoInstrumento",
			 emi."NombreCompleto" AS "NombreCompletoEmisor",
	 		 inst."Tamano" AS "Tamano",
	 		 inst."TipoValorizacion",
	 		 inst."IndicadorBolson",
	 		 inst."Tasa",
			 inst."Precio",
			 inst."ValorizaPor",
			 inst."TieneCupon",
			 0 as "Cantidad",
			 inst."PorcentajeCastigo",
			 inst."FrecuenciaCupon",
	 		 inst."PrecioStrike",
			 inst."InstrumentoAcreditado",
			 bon."CallConfirmado",
			 bon."TasaEmision",	
			 bon."MercadoLocal",
			 cr."ClasificacionRiesgo" AS "ValorClasificacionRiesgo",
			 inst."EsPutEsCall",
			 inst."IdTipoInstrumentoOrigen",
			 inst."AfectoArt107",
			 inst."AfectoArt104",
			 inst."EsAutorizado",
			 inst."CobroComisionesCustodia",
			 inst."IdInstrumentoRaiz",
			 inst."EsInstrumentoCalificado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst	
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS clasi ON clasi."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
		LEFT JOIN ClasificadoraRiesgo AS cr ON cr."IdInstrumento" = inst."IdInstrumento" AND cr.RANK = 1
		LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" bon on inst."IdInstrumento" = bon."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi on inst."IdEmisor" = emi."IdEmisor"
		WHERE inst."IdTipoInstrumento" not in ('SIM','RRV','RRF','PAC','PRT')
		AND inst."EsAutorizado" =  	CASE inst."IdTipoInstrumento" 
										WHEN 'CFI' THEN  'S' 
										WHEN 'CFX' THEN  'S'
										ELSE inst."EsAutorizado" 
									END
		UNION
		SELECT   inst."IdInstrumento",
			 inst."Nemotecnico",
			 inst."ISIN",
			 inst."CorteMinimo",
			 inst."EstaInscrito",
			 inst."EsDivisible",
			 inst."RazonSocial",
			 inst."FechaEmision",
			 inst."FechaVencimiento",
			 inst."IdInstrumentoRelacionado",
			 inst."IdEmisor",
			 emi."DocumentoIdentidad",
			 inst."IdTipoReajuste",
			 inst."Reajustable",
			 inst."IdMoneda",
			 inst."IdMonedaEmision",
			 sub."IdTipoInstrumento",
			 inst."IdEstado",
			 inst."DecimalesPrecio",
			 tipoins."Nombre" AS "NombreTipoInstrumento",
			 tipoins."IdClasificacionTipoInstrumento",
			 clasi."Nombre" AS "NombreClasificacionTipoInstrumento",
			 emi."NombreCompleto" AS "NombreCompletoEmisor",
	 		 inst."Tamano" AS "Tamano",
	 		 inst."TipoValorizacion",
	 		 inst."IndicadorBolson",
	 		 inst."Tasa",
			 inst."Precio",
			 inst."ValorizaPor",
			 inst."TieneCupon",
			 0 as "Cantidad",
			 inst."PorcentajeCastigo",
			 inst."FrecuenciaCupon",
			 inst."PrecioStrike",
			 inst."InstrumentoAcreditado",
			 bon."CallConfirmado",
	 		 bon."TasaEmision",
	 		 bon."MercadoLocal",
	 		 cr."ClasificacionRiesgo" AS "ValorClasificacionRiesgo",
			 inst."EsPutEsCall",
			 inst."IdTipoInstrumento" AS "IdTipoInstrumentoOrigen",
			 inst."AfectoArt107",
			 inst."AfectoArt104",
			 inst."EsAutorizado",
			 inst."CobroComisionesCustodia",
			 inst."IdInstrumentoRaiz",
			 inst."EsInstrumentoCalificado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst
		INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" AS sub ON sub."IdInstrumento" = inst."IdInstrumento" and sub."IdTipoInstrumento" IN ('SIM','RRV','RRF')	
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = sub."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS clasi ON clasi."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
		LEFT JOIN ClasificadoraRiesgo AS cr ON cr."IdInstrumento" = inst."IdInstrumento" AND cr.RANK = 1
		LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" bon on inst."IdInstrumento" = bon."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi on inst."IdEmisor" = emi."IdEmisor"
		WHERE inst."IdTipoInstrumento" in ('SIM','RRV','RRF')
		AND inst."EsAutorizado" =  	CASE inst."IdTipoInstrumento" 
										WHEN 'CFI' THEN  'S' 
										WHEN 'CFX' THEN  'S'
										ELSE inst."EsAutorizado" 
									END		
	);
	
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
