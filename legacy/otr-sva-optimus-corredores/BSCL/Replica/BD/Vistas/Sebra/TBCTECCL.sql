SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTECCL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTECCL" AS


	  SELECT
	  			--SE AGRUPAN LOS REGISTROS YA QUE OPTIMUS PERMITE REPETIR EVCC
	  			CASE EC."IdTipoEventoCapital"
					WHEN 'CC' THEN 'CC'
					WHEN 'SL' THEN 'SL'
					ELSE 'VC'
				END AS TIPO_EVE_CAP, 
				CASE WHEN EC."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN EC."IdClasificacionTipoInstrumento"
            			WHEN EC."IdTipoInstrumento" IN ('OSA','SIM','ACN') THEN 'AC'
 	           			WHEN EC."IdTipoInstrumento" IN ('ETN') THEN 'EN'
 	           			WHEN EC."IdTipoInstrumento" IN ('ETX') THEN 'ET'
 	           			WHEN EC."IdTipoInstrumento" IN ('MIL') THEN 'ST'
 	           			WHEN EC."IdTipoInstrumento" IN ('ACX') THEN 'AO'
	           			WHEN EC."IdTipoInstrumento" IN ('CFX') THEN 'CO'
	           			WHEN EC."IdTipoInstrumento" IN ('CFM') THEN 'CM'
            			WHEN EC."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN EC."IdTipoInstrumento" = 'FWD' THEN 'FW'
            			WHEN EC."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			ELSE 'XX'
            			END	 AS MERCADO 
				, LEFT(EC."Nemo",16) AS NEMO
				, EC."Secuencia" AS SEC_EVE
				, CASE "TipoCartera" WHEN 'CUS' THEN '1' WHEN 'GAR' THEN '2' WHEN 'TRC' THEN '3' WHEN 'TRV' THEN '3' WHEN 'SIC' THEN '4' ELSE '0' END AS TIPO_CARTERA
				, CAST(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( ECC."DocumentoIdentidad" AS VARCHAR(11))), '') AS Varchar)   AS RUT_CLI
				, COALESCE(P."CodigoPortafolio",'-1') AS SEC_RUT_CLI
				, TO_DECIMAL(SUM(ECC."CantidadDerecho"),14,2) AS CAN_DERECHO
				, TO_DECIMAL(SUM(ECC."CantidadNueva"),14,2) AS CAN_NUEVA
				, CAST(coalesce(MAX(COD_EMP_TRUN4."CodigoEmpleado"),'') AS VARCHAR(4)) AS COD_AGENTE
				, COALESCE(MAX(SUC."CodigoSucursal"),'') AS COD_SUC
				, LEFT(EC."Descripcion",40) DESCRIP_MOVTO
				, TO_DECIMAL(EC."Factor",15,10) AS FAC_AJUSTE
				, TO_NVARCHAR(MAX(ECC."FechaCierre"), 'YYYY-MM-DD') AS FEC_CIERRE
				, TO_NVARCHAR(EC."FechaPago", 'YYYY-MM-DD') AS FEC_MVTO
				, TO_NVARCHAR(MAX(ECC."FechaPago"), 'YYYY-MM-DD') AS FEC_PAGO
				, TO_NVARCHAR(EC."FechaLimite", 'YYYY-MM-DD')  AS FEC_VCTO --?
				, CASE WHEN LOCATE(EC."Descripcion", 'SUSC', -1)  = 0 THEN 'N' ELSE 'S' END    AS IND_SUSCRIP
				, '' AS IND_TRASPASO
				, ROUND(SUM(ECC."Total"),2) AS MONTO
				, SUM(ECC."MontoInteres") AS MONTO_INTERES
				, '' AS NEMO_SUSCRIP
				, CASE WHEN EC."IdMoneda" = 'CLP' THEN '$' ELSE '' END  AS TIPO_MON
				, LEFT(EC."IdTipoEventoCapital",2) AS TIPO_VC
				, ROUND(EC."Factor",4) AS VALOR_UNIT_CAP
				, '' AS FORMA_PAGO
				, '' AS IND_EMI_CHE
				, '' AS ESTADO
				, EC."IdMonedaLiquidacion" AS MONEDA
				, 0 AS CORRELATIVO
				, MAX(ECC.ISFUT) AS IND_ISFUT
				, EC."TasaEfectiva" AS TASA_ISFUT
				, SUM(ECC."MontoRetencionISFUT") AS MONTO_RET_ISFUT
				, MAX(ECC."RetencionISFUT") AS IND_RET_ISFUT
			
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" EC
		INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC ON ECC."IdEventoCapital" = EC."IdEventoCapital"
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = ECC."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP on EMP."IdEmpleado" = ECC."IdEmpleadoEjecutivoPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
	    WHERE EC."IdEstado" = 'CER' AND ECC."Origen" != 'MAN' AND ECC."IdBoveda" = 'DCV'
	    AND  "BSCL.Replica.BD.Servicios::func_IsNumeric"(p."CodigoPortafolio") != 0
	    GROUP BY EC."IdTipoEventoCapital",EC."IdClasificacionTipoInstrumento",EC."IdTipoInstrumento",EC."Nemo",EC."FechaPago",EC."Secuencia","TipoCartera",ECC."DocumentoIdentidad",P."CodigoPortafolio",
	    EC."Descripcion",EC."Factor",EC."FechaLimite",EC."IdMoneda",EC."IdMonedaLiquidacion",EC."TasaEfectiva"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTECCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
