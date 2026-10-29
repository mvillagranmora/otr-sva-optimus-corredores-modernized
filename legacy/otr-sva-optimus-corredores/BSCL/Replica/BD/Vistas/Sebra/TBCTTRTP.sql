SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP" AS

SELECT
		CAR."FolioCartera" AS "FolioCartera",
		(SELECT CAST("Valor" AS VARCHAR(7)) FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema') AS COD_PER, -- TIPO :char
		COALESCE (CAST(TO_VARCHAR (CAR."FechaVencimientoOperacion",'YYYY-MM-DD') AS VARCHAR(100)),''   )  AS FEC_VCTO_TP, -- TIPO :char
		COALESCE (CAST(SUB."Nemotecnico" AS VARCHAR(16)),''   )  AS NEMO, -- TIPO :char
		COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),'0-0'   )  AS RUT_CLI, -- TIPO :char
		COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '0')  AS SEC_RUT_CLI, -- TIPO :decimal
		COALESCE (CARFIRST."IdCartera", 0    )  AS SEC_MOVTO, -- TIPO :decimal
		COALESCE (CAR."INGCantidad", 0    )  AS CANTIDAD, -- TIPO :decimal
		0  AS CANTIDAD_ACUM_VC, -- TIPO :decimal
		COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(100)),''   )  AS COD_AGENTE, -- TIPO :char
		COALESCE (CORR."CodigoCorredora" , 0    )  AS COD_CORR_CONTRA, -- TIPO :decimal
		COALESCE (CAST(SUC."CodigoSucursal" AS VARCHAR(100)),''   )  AS COD_SUC, -- TIPO :char
		'0001-01-01'  AS FEC_LIQ_ANTICIP, -- TIPO :char 
		COALESCE (CAST(CAR."FechaLiquidacion" AS VARCHAR(100)),''   )  AS FEC_LIQUID, -- TIPO :char
		COALESCE (CAST(CAR."FechaOperacion" AS VARCHAR(10)),''   )  AS FEC_TRANS, -- TIPO :char 
		COALESCE (CAR."FolioComprobante" , 0    )  AS FOLIO_COMP_ADJ, -- TIPO :decimal
		COALESCE (CAST(LPAD(CAR."FolioExternoReferencia",8) AS VARCHAR(100)),''   )  AS FOLIO_TRANS, -- TIPO :char
		COALESCE (CAST(CAR."IdBolsa" AS VARCHAR(100)),''   )  AS IND_BOLSA, -- TIPO :char
		CASE WHEN COM."IdFormaCustodia" = 'NOM' THEN 'N' ELSE 'C' END AS IND_TIT_CUS,
		0  AS INTERES_DIA, -- TIPO :decimal
		RANK() OVER (PARTITION BY CAR."FolioComprobante" ORDER BY CAR."FolioComprobante" , CAR."IdCartera" ASC)    AS  LIN_COMP_ADJ,
		CAST(coalesce(CAR."INGMontoAPlazo",0) AS DECIMAL(17,2)  )  AS MONTO, -- TIPO :decimal
		0  AS MONTO_ACUM_VC, -- TIPO :decimal
		CAST(COALESCE (CAR."PrecioAPlazo" , 0 ) AS decimal(15,4))  AS PRECIO, -- TIPO :decimal
		COALESCE (0 , 0    )  AS SPREAD, -- TIPO :decimal -- 
		COALESCE (CAR."TasaCompra" , 0    )  AS TASA, -- TIPO :decimal
		'FD'   AS TIPO_COMP_ADJ, -- TIPO :char
		CASE WHEN substr(TOPE."AccionOperacion",1,1) = 'C' THEN 'V' ELSE 'C' END  AS TIPO_OPERAC, -- TIPO :char
		CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE COALESCE(CL."Codigo",'') END AS IND_LIQUID, -- TIPO :char
		COALESCE (CAR."PrecioCompra" , 0    )  AS PRECIO_CONTADO, -- TIPO :decimal
		CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TS' ELSE '' END  AS IND_SIMUL, -- TIPO :char
		COALESCE (CAR."IdCartera", 0    )  AS SEC_MOVTO_CTDO, -- TIPO :decimal -- validar
		CASE SUB."IdTipoInstrumento" WHEN 'CFI' THEN 'CF'
		WHEN 'CFX' THEN 'CO'
		WHEN 'ACN' THEN 'AC'
		WHEN 'ACX' THEN 'AO'
		WHEN 'ETN' THEN 'EN'
		WHEN 'MIL' THEN 'ST'
		WHEN 'ETX' THEN 'ET'
		ELSE ''
		END AS MERCADO, -- TIPO :char
		CAR."FolioExternoReferencia",
		CAR."FechaOperacion"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARFIRST ON CAR."FolioCartera" = CARFIRST."FolioCartera" and CARFIRST."EsPrimeraCartera" = 'S'
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CAR."IdCliente" = CLI."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CAR."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON SUC."IdSucursal" = CAR."IdSucursal"
	LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
	LEFT JOIN (
		SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
			SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
		) WHERE RANK < 10
	) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COM ON COM."IdComprobante" = CAR."FolioComprobante"
	LEFT JOIN (SELECT "FolioCarteraRelacionada", "IdTipoInstrumento", "Nemotecnico", SUM("MontoSubyacente") AS "MontoSubyacente" 
				FROM "BSCL.Custodia.BD::CUS.Subyacente" WHERE "EsPrimeraCartera" = 'S'
				GROUP BY "FolioCarteraRelacionada", "IdTipoInstrumento", "Nemotecnico") SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"
	LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CAR."FolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CAR."FechaOperacion"
			AND TRA."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON  TOPE."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion"
	WHERE CAR."IdEstado" = 'VIG' AND CAR."IdClasificacionTipoInstrumento" = 'RV' AND CAR."IdTipoInstrumento" = 'SIM'-- and TRA."IdTransaccion" is not NULL

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP" TO  BCS_DESARROLLADOR WITH GRANT OPTION;