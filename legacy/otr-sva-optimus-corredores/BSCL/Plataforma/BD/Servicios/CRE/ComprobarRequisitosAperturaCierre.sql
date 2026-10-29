SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CRE.ComprobarRequisitosAperturaCierre";

CREATE PROCEDURE
-- 
--  Proposito: Obtiene el libro de un cliente
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 12/10/2016
--     
"BSCL.Plataforma.BD.Servicios::CRE.ComprobarRequisitosAperturaCierre" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN  p_IdCREModuloEmpresa						BIGINT,
	IN  p_Tipo										NVARCHAR(10), --CIEMOD: Cierre , APEMOD --Apertura
	IN  p_fechaSistema 								NVARCHAR(10),
	
	OUT p_SalidaDetalle	"BSCL.Orden.BD.Tipos::SalidaDetalle"
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL 
	READS SQL DATA AS
BEGIN
	/*DECLARACION DE VARIABLES*/
	DECLARE v_cantidadFuncionesRequeridas BIGINT;
	DECLARE v_cantidadFuncionesCumplidas BIGINT;
	DECLARE v_tipoEjecucion NVARCHAR(1);	
	p_SalidaDetalle = SELECT 0 as "IdInterno",0 as "IdOperacion",'' AS "Resultado",'' AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	---------------------------------------------
	/*LOGICA DE NEGOCIO*/
	--------------------------------------------

	--Filtro para buscar funciones de apertura o cierre segun corresponda
	IF p_Tipo = 'CIEMOD' THEN
		v_tipoEjecucion = 'A';
	ELSEIF p_Tipo = 'APEMOD' THEN
		v_tipoEjecucion = 'C';
	END IF;
	
	--Buscar funciones requeridas
	t_funcionesRequeridas = select "NombreFuncionProceso" from "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" where "IdCREModuloEmpresa" = p_IdCREModuloEmpresa AND "PermiteEjecucion" = v_tipoEjecucion AND "IdEstado" = 'VIG';

	--buscar funciones cumplidas en el log.
	t_funcionesCumplidas = 
		SELECT DISTINCT 
			FR."NombreFuncionProceso",
			CLCM."TipoEvento",
			CASE WHEN COALESCE(CLCM."TipoEvento",'') = '' THEN 'N' ELSE 'S' END AS "Existe"
		FROM :t_funcionesRequeridas FR
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarLogCierreModulos" CLCM  ON CLCM."TipoEvento" = FR."NombreFuncionProceso" AND "Tipo" = p_Tipo AND "IdEmpresa" = p_IdEmpresa 
		-- AND "FechaProceso" BETWEEN (p_fechaSistema||' 00:00:00') AND (p_fechaSistema||' 23:59:59');
		AND "FechaLog"= :p_fechaSistema;

	--Contar cuantos eventos son requeridos, y cuantos fueron cumplidos
	select COUNT("NombreFuncionProceso") into v_cantidadFuncionesRequeridas from :t_funcionesRequeridas;
	select COUNT("TipoEvento") into v_cantidadFuncionesCumplidas from :t_funcionesCumplidas;

	--Evaluar si se cumplieron todas las funciones requeridas
	IF v_cantidadFuncionesRequeridas != v_cantidadFuncionesCumplidas THEN
		p_SalidaDetalle = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", 'UI_NO_EJECUTADO_'||"NombreFuncionProceso" AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
		FROM :t_funcionesCumplidas where "Existe" = 'N' 
		UNION ALL SELECT * FROM :p_SalidaDetalle;
	END IF;
  
END;
 
