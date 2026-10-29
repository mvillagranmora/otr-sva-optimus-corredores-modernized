SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoAnularCargoAbono";

CREATE PROCEDURE 
-- 
--  Proposito: Validar si el movimiento de cargo o abono pueden ser anulado
--  Autor: NRM - BCS 
--  Fecha Creacion: 20/08/2015
-- 
--  Codigo de prueba servicio:
--  DROP TABLE #rules_in;
--  CREATE LOCAL TEMPORARY TABLE #rules_in ("IdCargoAbonoMovimiento" BIGINT);
--  INSERT INTO #rules_in VALUES (4);
--  CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoDeAnulacion"(#rules_in,?,?)
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoAnularCargoAbono" (
	IN  p_IdEmpresa					BIGINT,
	IN  p_IdModulo                  NVARCHAR(3),
	IN  p_FechaMovimiento        	NVARCHAR(40)
	/*IN  p_CargoAbonoAAnular			TABLE ("IdCargoAbonoMovimiento" BIGINT,"GlosaAnulacion" NVARCHAR(260)),
	
	-- Salidas para mensajes de errores
	OUT p_out_MovimientosRech		TABLE ("IdCargoAbonoMovimiento" BIGINT, "Accion" NVARCHAR(40)),
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
    OUT p_out_ResultadoRegla		NVARCHAR(40)  	--OK=Cumple todos los requisitos,		
											  		--ADVE=Alguna de lAS reglAS posee advertencias, no detiene la operacion
											  		--RAUT=Alguna de las reglas requiere autorizacion
                                              		--RECH=Alguna de lAS reglAS detienen la operacion*/
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"  AS
BEGIN
	
	v_MatrizValidaciones_v1 = 
	SELECT  CAA."IdInterno"
		   ,CAA."IdCargoAbonoMovimiento"
		   ,CAA."IdCuentaInversion"
		   ,CAA."IdEstado"
		   ,CAA."IdEvento"
		   ,EVT."IdEstado" as "IdEstadoEvento"
		   ,CAA."GlosaAnulacion"
		   ,CAA."PERMITEANULARMOVIMIENTO"
		   ,CAA."ANULACIONREQUIEREAUTORIZACION"
		   ,CAA."EsCargoAbonoPorTraspaso"
		   ,CAA."IdCargoAbonoRelacionadoTraspaso"
		   ,CAA."Informado"
		   ,CAA."IdTipoMovimiento"
		   ,1 as "MOVIMIENTO_CARGO_ABONO_NO_EXISTE"
		   ,1 as "MOVIMIENTO_CARGO_ABONO_ANULADO"
		   ,1 as "NO_PERMITIDO_ANULAR_MOVIMIENTO"
		   ,1 as "ANULACION_REQUIERE_AUTORIZACION"
		   ,1 as "AUTORIZACION_PENDIENTE_APROBACION"
		   ,CASE WHEN CAA."IdTipoMovimiento" = 'MOVABN' THEN 1 ELSE 0 END as "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
	  FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoAnularCargoAbono_IN_CargoAbonoAAnular" AS CAA
	  LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVT ON EVT."IdEvento" = CAA."IdEvento";
	
	v_Validaciones_V1 =
	 SELECT  "IdInterno"
	 		,CASE WHEN "MOVIMIENTO_CARGO_ABONO_NO_EXISTE" = 1 AND COALESCE("IdCargoAbonoMovimiento",0) = 0 THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_CARGO_ABONO_NO_EXISTE"	 		
	 		,CASE WHEN "MOVIMIENTO_CARGO_ABONO_ANULADO" = 1 AND "IdEstado" = 'ANU' THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_CARGO_ABONO_ANULADO"
	 		,CASE WHEN "NO_PERMITIDO_ANULAR_MOVIMIENTO" = 1 AND "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'N' THEN 'RECH' ELSE 'OK' END AS "NO_PERMITIDO_ANULAR_MOVIMIENTO"	 		
	 		,CASE WHEN "ANULACION_REQUIERE_AUTORIZACION" = 1 AND ("EsCargoAbonoPorTraspaso" = 'N' OR ("EsCargoAbonoPorTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR')) THEN 
	 				( CASE WHEN "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'S' AND "ANULACIONREQUIEREAUTORIZACION" = 'S' AND "IdEvento" < 1 THEN 'RAUT'
						   WHEN "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'S' AND "ANULACIONREQUIEREAUTORIZACION" = 'S' AND "IdEvento" > 1 AND "IdEstadoEvento" = 'VCD' THEN 'RAUT'
						   WHEN "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'S' AND "ANULACIONREQUIEREAUTORIZACION" = 'S' AND "IdEvento" > 1 AND "IdEstadoEvento" = 'RZD' THEN 'RAUT'
						   WHEN "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'S' AND "ANULACIONREQUIEREAUTORIZACION" = 'S' AND "IdEvento" > 1 AND "IdEstadoEvento" = 'ANU' THEN 'RAUT'
						   ELSE 'OK'
					   END
					)
	 		  END AS "ANULACION_REQUIERE_AUTORIZACION"
	 		,CASE WHEN "AUTORIZACION_PENDIENTE_APROBACION" = 1 AND "IdEstado" = 'CRT' AND "PERMITEANULARMOVIMIENTO" = 'S' AND "ANULACIONREQUIEREAUTORIZACION" = 'S' AND "IdEvento" > 1 AND "IdEstadoEvento" = 'VIG' THEN 'RECH' ELSE 'OK' END AS "AUTORIZACION_PENDIENTE_APROBACION"
	 		,CASE WHEN "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA" = 1 AND "EsCargoAbonoPorTraspaso" = 'S' AND "Informado" = 'S' THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
	  FROM :v_MatrizValidaciones_v1;
	
	
	------RESULTADO	
	v_Resultado = 
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		COALESCE(			 			
			 			 "1" ||'MOVIMIENTO_CARGO_ABONO_NO_EXISTE'
						,"2" ||'MOVIMIENTO_CARGO_ABONO_ANULADO'
						,"3" ||'NO_PERMITIDO_ANULAR_MOVIMIENTO'
						,"4" ||'ANULACION_REQUIERE_AUTORIZACION'
						,"5" ||'AUTORIZACION_PENDIENTE_APROBACION'
						,"6" ||'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1" ||"MOVIMIENTO_CARGO_ABONO_NO_EXISTE"						 
						,"2" ||"MOVIMIENTO_CARGO_ABONO_ANULADO"
						,"3" ||"NO_PERMITIDO_ANULAR_MOVIMIENTO"
						,"4" ||"ANULACION_REQUIERE_AUTORIZACION"
						,"5" ||"AUTORIZACION_PENDIENTE_APROBACION"
						,"6" ||"MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;
	
	
	INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoAnularCargoAbono_OUT_Resultado"
	 SELECT  DISTINCT R."IdInterno"
		   	,R."Codigo"
	 		,R."Resultado"
	 		,CASE WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_NO_EXISTE' THEN 'IdCargoAbonoMovimiento'
	 		      WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_ANULADO' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'NO_PERMITIDO_ANULAR_MOVIMIENTO' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'ANULACION_REQUIERE_AUTORIZACION' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'AUTORIZACION_PENDIENTE_APROBACION' THEN 'Id'
	 			  WHEN "Codigo" = 'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA' THEN 'IdCargoAbonoMovimiento'
	 			  ELSE ''
	 		  END "CodigoDato1"
	 		,CASE WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_NO_EXISTE' THEN CAST(M."IdInterno" as NVARCHAR)
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_ANULADO' THEN CAST(M."IdInterno" as NVARCHAR)
	 			  WHEN "Codigo" = 'NO_PERMITIDO_ANULAR_MOVIMIENTO' THEN CAST(M."IdInterno" as NVARCHAR)
	 			  WHEN "Codigo" = 'ANULACION_REQUIERE_AUTORIZACION' THEN CAST(M."IdInterno" as NVARCHAR)
	 			  WHEN "Codigo" = 'AUTORIZACION_PENDIENTE_APROBACION' THEN CAST(M."IdEvento" as NVARCHAR)
	 			  WHEN "Codigo" = 'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA' THEN CAST(M."IdEvento" as NVARCHAR)
	 			  ELSE ''
	 		  END "ValorDato1"
	 		,"CodigoDato2"
	 		,"ValorDato2"
	   FROM :v_Resultado as R
	  INNER JOIN :v_MatrizValidaciones_v1 as M ON M."IdInterno" = R."IdInterno";	
END;
