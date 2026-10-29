WITH
	CarteraPortafolioPaso AS (
		SELECT
			CAR."IdCartera"
			,CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
		WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
		  AND CAR."IdEmpresa" = :IdEmpresa
	),
	Cartera AS
	(
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
		
		FROM CarteraPortafolioPaso CAR
	    WHERE CAR."IdEstado" = 'VIG'
	     
		UNION ALL
				
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,GAR."INGCantidad"AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
		
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN CarteraPortafolioPaso CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE GAR."IdEstado" = 'VIG'
)
 SELECT 	
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"	AS "Nemotecnico"
	,SUM(CAR."Cantidad"*(DVRV."MONTO"/DVRV."CANTIDAD"))	AS "Disponible"			
	,CAR."IdMonedaInstrumento" AS "IdMonedaInstrumento"
FROM Cartera CAR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON CLI."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
	ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
	ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT
	ON PORT."IdPortafolio" = CAR."IdPortafolio"
INNER JOIN BSCL.TBCEDVRV DVRV
	ON TRIM(DVRV."RUT_CLI") = DOC."DocumentoIdentidad"
	AND TRIM(DVRV."SEC_RUT_CLI") = PORT."CodigoPortafolio"
	AND TRIM(DVRV."NEMO") = CAR."Nemotecnico"
	AND CAR."INGFechaOperacion" <= DVRV."FECHA" 
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."INGFechaOperacion" <= :FechaGoLive
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')

GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento";