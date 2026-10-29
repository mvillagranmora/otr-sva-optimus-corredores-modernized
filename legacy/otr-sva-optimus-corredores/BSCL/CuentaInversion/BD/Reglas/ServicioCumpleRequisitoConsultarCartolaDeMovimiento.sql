SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarCartolaDeMovimiento";

CREATE PROCEDURE
-- 
--  Proposito: Realizar el conjunto de validaciones asociadas al servicio
--  Autor: NRM - BCS 
--  Fecha Creacion: 29/01/2016
-- 
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarCartolaDeMovimiento" (
	IN	p_IdInterno					   BIGINT,
	IN  p_IdEmpresa                    BIGINT,
	IN  p_IdCuentaInversion            BIGINT,
	IN  p_IdCliente                    BIGINT,   
    IN  p_IdPortafolio                 BIGINT,
    IN  p_IdMoneda                     NVARCHAR(3), 
    IN  p_IdEstado                     NVARCHAR(3),
	IN  p_FechaEfectivaMovimientoDesde NVARCHAR(10),
    IN  p_FechaEfectivaMovimientoHasta NVARCHAR(10)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	v_MatrizValidaciones_v1 = 
	 SELECT  p_IdInterno AS "IdInterno"
	 		,p_IdEmpresa AS "IdEmpresa"
			,p_IdCuentaInversion AS "IdCuentaInversionInformado"
			,CINV."IdCuentaInversion"
			,p_IdCliente AS "IdCliente"
			,p_IdPortafolio AS "IdPortafolio"
			,p_IdMoneda AS "IdMoneda"
			,p_IdEstado AS "IdEstadoInformado"
			,EST."IdEstado"
			,p_FechaEfectivaMovimientoDesde AS "FechaDesde"
			,p_FechaEfectivaMovimientoHasta AS "FechaHasta"
			,1 as "CUENTA_INVERSION_INVALIDA"
			,1 as "CUENTA_INVERSION_NO_EXISTE"
			,1 as "ESTADO_NO_EXISTE"
			,1 as "FECHA_INVALIDA"
			,1 as "RANGO_FECHA_BUSQUEDA_INVALIDO"
	  FROM DUMMY
	  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CINV ON ("IdCuentaInversion" = :p_IdCuentaInversion OR ("IdPortafolio" = :p_IdPortafolio AND "IdMoneda" = :p_IdMoneda))
	  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.EstadoCuentaInversion" as EST ON (EST."IdEstado" = :p_IdEstado);
	
	
	v_Validaciones_V1 =
		SELECT
			 "IdInterno"
			,CASE WHEN "CUENTA_INVERSION_INVALIDA" = 1
					AND (COALESCE("IdPortafolio", 0) = 0 OR COALESCE("IdMoneda", '') = '' OR "IdPortafolio" < 1 OR length("IdMoneda") = 0)
					AND (COALESCE("IdCuentaInversion", 0) = 0 OR "IdCuentaInversion" < 1)
					AND (COALESCE("IdCliente", 0) = 0 OR "IdCliente" < 1)
				  THEN 'RECH'
				  ELSE 'OK' END AS "CUENTA_INVERSION_INVALIDA"
			,CASE WHEN "CUENTA_INVERSION_NO_EXISTE" = 1
					AND COALESCE("IdCuentaInversion",0) = 0
					AND COALESCE("IdCliente", 0) = 0
				  THEN 'RECH'
				  ELSE 'OK' END AS "CUENTA_INVERSION_NO_EXISTE"
			,CASE WHEN "ESTADO_NO_EXISTE" = 1 AND "IdEstadoInformado" IS NOT NULL AND COALESCE("IdEstado",'') = '' THEN 'RECH' ELSE 'OK' END AS "ESTADO_NO_EXISTE"
			,CASE WHEN "FECHA_INVALIDA" = 1 AND (COALESCE("FechaDesde",'0001-01-01') = '0001-01-01' OR COALESCE("FechaHasta",'0001-01-01') = '0001-01-01') THEN 'RECH' ELSE 'OK' END AS "FECHA_INVALIDA"						
			,CASE WHEN "RANGO_FECHA_BUSQUEDA_INVALIDO" = 1 AND TO_DATE("FechaHasta",'YYYY-MM-DD') < TO_DATE("FechaDesde",'YYYY-MM-DD') THEN 'RECH' ELSE 'OK' END AS "RANGO_FECHA_BUSQUEDA_INVALIDO"
		FROM :v_MatrizValidaciones_v1;
	
	INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoConsultarCartolaDeMovimiento_OUT_Resultado"
	 SELECT R."IdInterno",R."Codigo",R."Resultado",
	 		CASE WHEN R."Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN 'NombreCliente'
	 			 WHEN R."Codigo" = 'FECHA_INVALIDA' THEN 'FECHA'
	 			 ELSE ''
	 		 END as "CodigoDato1",
	 		CASE WHEN R."Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN ''
	 			 WHEN R."Codigo" = 'FECHA_INVALIDA' THEN M."FechaDesde" ||' o '|| "FechaHasta"
	 			 ELSE ''
	 		 END  as "ValorDato1",
	 		CASE WHEN R."Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN 'NombreMoneda'
	 			 ELSE ''
	 		 END  as "CodigoDato2",
	 		CASE WHEN R."Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN M."IdMoneda"
	 			 ELSE ''
	 		 END  as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		COALESCE(
			 			 "1" ||'CUENTA_INVERSION_INVALIDA'
						,"2" ||'CUENTA_INVERSION_NO_EXISTE'
						,"3" ||'ESTADO_NO_EXISTE'
						,"4" ||'FECHA_INVALIDA'
						,"5" ||'RANGO_FECHA_BUSQUEDA_INVALIDO'
			 		) AS "Codigo",
			 		COALESCE(
			 			 "1" ||"CUENTA_INVERSION_INVALIDA"
						,"2" ||"CUENTA_INVERSION_NO_EXISTE"
						,"3" ||"ESTADO_NO_EXISTE"
						,"4" ||"FECHA_INVALIDA"
						,"5" ||"RANGO_FECHA_BUSQUEDA_INVALIDO"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad") AS R
	  INNER JOIN :v_MatrizValidaciones_v1 as M ON M."IdInterno" = R."IdInterno"
	  WHERE "Resultado" IS NOT NULL;	
END;