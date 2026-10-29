SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,CASE CVIFRF."IdTipoOperacion"
		WHEN 'VENPAC' THEN 'PACTOVTA'
		WHEN 'COMPAC' THEN 'PACTOCOMP' 
		ELSE '????????????'			
	END	AS "IdTipoMovimiento" 
	,CVIFRF."IdEmpresa"								    
	,TO_BIGINT(CVIFRF."FolioOperacion") AS "Numero"
	,CVIFRF."IdValorizacion" AS "IdMovimiento"
	,TO_BIGINT(0) AS "Linea"
	,TO_VARCHAR(:p_FechaContable, 'YYYY-MM-DD') AS "Fecha"
	,DI."DocumentoIdentidad"
    ,CVIFRF."IdPortafolio"
	,TO_BIGINT(YEAR(CVIFRF."FechaOperacion")) AS "Ejercicio"
	,TO_NVARCHAR(TO_CHAR(CVIFRF."FechaOperacion", 'MM-YYYY')) AS "Periodo"
	,ABS(CVIFRF."MontoActual") AS "Monto"     
	,CONF."Valor" AS "IdMoneda"
	,'' AS "Estado"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CVIFRF."IdCliente" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = CLI."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.MonedaSistema'
WHERE CVIFRF."IdInforme" = :p_IdInforme
AND CVIFRF."IdEmpresa" = :p_IdEmpresa 
AND "IdTipoOperacion" in ('COMPAC' , 'VENPAC');