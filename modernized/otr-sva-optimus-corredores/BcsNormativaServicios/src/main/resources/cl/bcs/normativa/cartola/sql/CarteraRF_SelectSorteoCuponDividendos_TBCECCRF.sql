WITH
	Cartera AS
	(
		SELECT 	 
			CAR."FolioCartera"
			,CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."INGCantidad"AS "Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
		
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE 
		   CAR."IdEmpresa" = :IdEmpresa
	     AND CAR."IdEstado" = 'VIG'
	     
		UNION ALL
				
		SELECT 	 
			CAR."FolioCartera"
			,CAR."IdCliente"
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
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE 
		   CAR."IdEmpresa" = :IdEmpresa
		  AND GAR."IdEstado" = 'VIG'
)
 SELECT 	
	 CAR."FolioCartera" || '_' || CAR."IdCliente" || '_' || CAR."IdPortafolio" || '_' || CAR."IdInstrumento" || '_' || CAR."IdMonedaInstrumento" AS "Id"
	,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdInstrumento"
	,SUM(
		CASE WHEN CAR."Cantidad" > CCRF."CANTIDAD" THEN CCRF."VALOR_UNIT"
		     ELSE CAR."Cantidad"*(CCRF."VALOR_UNIT"/CCRF."CANTIDAD")
		     END
		)	AS "Disponible"		
	,SUM(
		CASE WHEN CAR."Cantidad" > CCRF."CANTIDAD" THEN CCRF."MONTO_INTERES"
		     ELSE CAR."Cantidad"*(CCRF."MONTO_INTERES"/CCRF."CANTIDAD")
		     END
		)	AS "InteresesPagados"	
	,CAR."IdMonedaInstrumento" AS "IdMoneda"
FROM Cartera CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON CLI."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
	ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
	ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT
	ON PORT."IdPortafolio" = CAR."IdPortafolio"
INNER JOIN BSCL.TBCECCRF CCRF
	ON TRIM(CCRF."RUT_CLI") = DOC."DocumentoIdentidad"
	AND TRIM(CCRF."SEC_RUT_CLI") = PORT."CodigoPortafolio"
	AND TRIM(CCRF."NEMO") = CAR."Nemotecnico"
	AND CAR."INGFechaOperacion" <= CCRF."FECHA" 
WHERE 
 CAR."IdClasificacionTipoInstrumento" = 'RF'
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."INGFechaOperacion" <= :FechaGoLive
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')

GROUP BY 
CAR."FolioCartera",CAR."IdCliente",CAR."IdPortafolio",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento";