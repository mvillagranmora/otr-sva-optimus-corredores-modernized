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
			,CAR."INGCantidad"  AS "Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion" AS "INGFechaOperacion"
			,CAR."Nemotecnico"			
			,CAR."IdMonedaInstrumento"
			,CAR."FechaOperacion"
		
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		WHERE 
		 CAR."FechaRespaldo" = :p_FechaConsulta
	     AND CAR."IdEstado" = 'VIG'
	     AND CAR."FolioCartera" IN (:p_FolioCarteras)
	    
)
 SELECT 	
	 CAR."FolioCartera"
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

GROUP BY 
CAR."FolioCartera";