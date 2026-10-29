WITH
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
		
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		WHERE CAR."FechaRespaldo" = :p_FechaConsulta 
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE CAR."Nemotecnico" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
	     
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
		
		FROM "BSCL.Custodia.BD::HIS.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE GAR."FechaRespaldo" = :p_FechaConsulta 
		AND GAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE CAR."Nemotecnico" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
)
 SELECT 	
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdInstrumento"
	,SUM(CAR."Cantidad"*(DVRV."MONTO"/DVRV."CANTIDAD"))	AS "Disponible"
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
WHERE
CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :p_FechaConsulta
AND CAR."INGFechaOperacion" <= :FechaGoLive
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
AND CAR."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE CAR."Nemotecnico" END
AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
GROUP BY CAR."IdCliente",CAR."IdPortafolio",CAR."IdInstrumento";