--VISTA PARA EL BUSCADOR DE INSTRUMENTOS, NO USAR EN SPS!!
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumentoOSA"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumentoOSA"  
AS WITH ClasificadoraRiesgo AS (
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
	),
CantidadTramite AS (
		SELECT  "IdPortafolio",
		IFNULL(SUM(CASE WHEN "IdEstado" IN ('VIG', 'CRT') THEN "CantidadNueva" ELSE 0 END), 0) AS "CantidadTramite",
        IFNULL(SUM(CASE WHEN "IdEstado" IN ('VIG', 'CRT', 'ENV') THEN "CantidadNueva" ELSE 0 END), 0) AS "CantidadTotalTramitada",
		"Secuencia", 
    	"IdSocioNegocio", 
    	"IdInstrumentoNuevo"
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	
		WHERE 
			"IdTipoEventoCapital" 	= 'SU'	AND
			"IdEstado"				IN ('VIG','CRT','ENV')
		GROUP BY "IdPortafolio", 
			    "Secuencia", 
			    "IdSocioNegocio", 
			    "IdInstrumentoNuevo"
)
SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado",
		"IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdMonedaEmision","IdTipoInstrumento","IdEstado","DecimalesPrecio","NombreTipoInstrumento",
		"IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento","Tamano","IndicadorBolson","Tasa",
		"Precio","ValorizaPor","TieneCupon","Cantidad","PorcentajeCastigo","FrecuenciaCupon","PrecioStrike","InstrumentoAcreditado","IdCliente","IdEventoCapital","IdEmpresa","IdSocioDeNegocio","IdPortafolio",
		"NumeroDividendo","IdBoveda","Secuencia","Descripcion","AfectoArt107", "CantidadTramite","IdInstrumentoEvento", "CantidadTotalTramitada"
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
	 		 inst."Tamano" AS "Tamano",
	 		 inst."IndicadorBolson",
	 		 inst."Tasa",
			 EVC."FactorNuevo" AS "Precio",
			 inst."ValorizaPor",
			 inst."TieneCupon",
			 SUM(CAR."Cantidad") as "Cantidad",
			 IFNULL(CT."CantidadTramite", 0) AS "CantidadTramite",
			 IFNULL(CT."CantidadTotalTramitada",0) AS "CantidadTotalTramitada",
			 inst."PorcentajeCastigo",
			 inst."FrecuenciaCupon",
	 		 inst."PrecioStrike",
			 inst."InstrumentoAcreditado",
			 CAR."IdCliente",
			 EVC."IdEventoCapital",
			 EVC."IdEmpresa",
			 SOC."IdSocioDeNegocio",
			 CAR."IdPortafolio",
			 EVC."NumeroDividendo",
			 CAR."IdBoveda",
			 EVC."Secuencia",
			 EVC."Descripcion",
			 IFNULL(EVC."IdInstrumento", 0) AS  "IdInstrumentoEvento",
			 inst."AfectoArt107"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst	
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS clasi ON clasi."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
		LEFT JOIN ClasificadoraRiesgo AS cr ON cr."IdInstrumento" = inst."IdInstrumento" AND cr.RANK = 1
		LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" bon on inst."IdInstrumento" = bon."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi on inst."IdEmisor" = emi."IdEmisor"
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdInstrumento" = inst."IdInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.RelacionInstrumento" AS REL ON inst."IdInstrumento" = REL."IdInstrumentoRelacionado" AND REL."IdEstado" = 'VIG'
		INNER JOIN (Select "Descripcion","Secuencia","FactorNuevo","IdEventoCapital","IdInstrumento","IdEmpresa","NumeroDividendo" FROM "BSCL.Custodia.BD::CUS.EventoCapital" Where "IdEstado" IN ('ACT') AND "IdTipoEventoCapital" = 'EP'
			GROUP BY "FactorNuevo","IdEventoCapital","IdInstrumento","IdEmpresa","NumeroDividendo","Secuencia","Descripcion") AS EVC ON EVC."IdInstrumento" = REL."IdInstrumento"
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON CAR."IdCliente" = SOC."IdCliente"
		LEFT JOIN CantidadTramite AS CT ON CT."IdPortafolio" = CAR."IdPortafolio" AND CT."Secuencia" = EVC."Secuencia" AND CT."IdSocioNegocio" = SOC."IdSocioDeNegocio" AND CT."IdInstrumentoNuevo" = inst."IdInstrumento"
		WHERE inst."IdTipoInstrumento"  in ('OSA') AND CAR."IdEstado" = 'VIG' 
		GROUP BY inst."IdInstrumento",inst."Nemotecnico",inst."ISIN", inst."CorteMinimo",inst."EstaInscrito",inst."EsDivisible",inst."RazonSocial",inst."FechaEmision",
			 inst."FechaVencimiento", inst."IdInstrumentoRelacionado",inst."IdEmisor",inst."IdTipoReajuste",inst."Reajustable", inst."IdMoneda", inst."IdMonedaEmision",
			 inst."IdTipoInstrumento", inst."IdEstado", inst."DecimalesPrecio", tipoins."Nombre",tipoins."IdClasificacionTipoInstrumento",clasi."Nombre",
	 		 inst."Tamano", inst."IndicadorBolson",inst."Tasa",EVC."FactorNuevo",inst."ValorizaPor",inst."TieneCupon",inst."PorcentajeCastigo",
			 inst."FrecuenciaCupon",inst."PrecioStrike", inst."InstrumentoAcreditado",CAR."IdCliente", EVC."IdEventoCapital",EVC."IdEmpresa",SOC."IdSocioDeNegocio",CAR."IdPortafolio",
			 EVC."NumeroDividendo", CAR."IdBoveda",EVC."Secuencia",EVC."Descripcion",EVC."IdInstrumento", inst."AfectoArt107", CT."CantidadTramite", CT."CantidadTotalTramitada");
	
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumentoOSA" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
