--*********************************************************************************************
--***************************************  NEGOCIO  *******************************************
--*********************************************************************************************	
WITH requisitos1 AS ( 
SELECT  2 AS "Prioridad",
		CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 0
		 	 WHEN (NT."IdEstado" = 'RCP') THEN 0
			 WHEN (NT."IdEstado" = 'ANU') THEN 0
			 ELSE  1
		END AS "Cumple",
		CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 'NOMINA_TRANSFERENCIA_NO_EXISTE'
		 	 WHEN (NT."IdEstado" = 'RCP') THEN 'NOMINA_TRANSFERENCIA_RECEPCIONADO'
			 WHEN (NT."IdEstado" = 'ANU') THEN 'NOMINA_TRANSFERENCIA_ANULADO'
			 ELSE  ''
		END AS "CodigoError",
		'IdNominaTransferencia' AS "CodigoDato",
       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
       	RP."Id",
       	'' as "Entidad"
 FROM #AnularNominasTransferencias_p_RegistrosAProcesar as RP
 LEFT JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = RP."Id"
WHERE RP."Entidad" = 'NominaTransferencia'

), requisitos2 as(
SELECT  2 AS "Prioridad",
		CASE WHEN COALESCE(NT."IdNominaTransferenciaDetalle",0) = 0 THEN 0
		 	 WHEN (NT."IdEstado" = 'RCP') THEN 0
			 WHEN (NT."IdEstado" = 'ANU') THEN 0
			 ELSE  1
		END AS "Cumple",
		CASE WHEN COALESCE(NT."IdNominaTransferenciaDetalle",0) = 0 THEN 'NOMINA_TRANSFERENCIA_DETALLE_NO_EXISTE'
		 	 WHEN (NT."IdEstado" = 'RCP') THEN 'NOMINA_TRANSFERENCIA_DETALLE_RECEPCIONADO'
			 WHEN (NT."IdEstado" = 'ANU') THEN 'NOMINA_TRANSFERENCIA_DETALLE_ANULADO'
			 ELSE  ''
		END AS "CodigoError",
		'IdNominaTransferenciaDetalle' AS "CodigoDato",
       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
       	RP."Id",
       	'' as "Entidad"
 FROM #AnularNominasTransferencias_p_RegistrosAProcesar as RP
 LEFT JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NT ON NT."IdNominaTransferenciaDetalle" = RP."Id"
WHERE RP."Entidad" = 'NominaTransferenciaDetalle'
UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad" FROM requisitos1
) 

SELECT * FROM requisitos2 WHERE "Cumple" = 0;

