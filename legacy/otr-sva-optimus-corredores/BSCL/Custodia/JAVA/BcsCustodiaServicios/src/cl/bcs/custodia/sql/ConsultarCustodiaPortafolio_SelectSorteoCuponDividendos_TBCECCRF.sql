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
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
			,CAR."INGIdMonedaPagoCupon"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE 
		CAR."IdEstado" = 'VIG'
	    AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
	     
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
			,CAR."INGIdMonedaPagoCupon"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE 
		GAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
)
 SELECT 	
	 CAR."IdCliente"
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
	,CAR."INGIdMonedaPagoCupon"
FROM Cartera CAR
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
AND CAR."FechaOperacion" <= :p_FechaConsulta
AND CAR."INGFechaOperacion" <= :FechaGoLive
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdInstrumento",CAR."INGIdMonedaPagoCupon";