SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::VerificarSecuenciasFoliosOrdenes";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que verifica la secuencia de folios. Al no existir ordenes para un criterio en la fecha del sistema
--	se debe reiniciar el tipo de folio correspondiente.
--  Autor: JGOL
--  Fecha Creacion: 20/12/2017
--   
"BSCL.Orden.BD.Servicios::VerificarSecuenciasFoliosOrdenes" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
	IN p_IdModulo                       NVARCHAR(3),
	OUT p_out_Mensaje 					NVARCHAR(200),
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 1;
	p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
 	p_out_Mensaje := '';

	BEGIN

		declare v_countRegistros1x BIGINT := 0;
		declare v_countRegistros2x BIGINT := 0;
		declare v_countRegistros3x BIGINT := 0;
		declare v_countRegistros4x BIGINT := 0;
		declare v_countRegistros5x BIGINT := 0;
		declare v_countRegistros6x BIGINT := 0;
		declare v_FechaHoraSistema SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		--obtiene fecha del sistema.
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
		
		--obtiene cantidad de registros por criterio:
		-- 3x falta por definir
		select COUNT("1") into v_countRegistros6x from "BSCL.Orden.BD::ORD.Orden" where "FechaIngresoRegistro" = :v_FechaHoraSistema
		and "IdTipoCanalIngreso" = 'TRA' AND "FormadorMercado" = 'S'; 
		
		select COUNT("1") into v_countRegistros5x from "BSCL.Orden.BD::ORD.Orden" where "FechaIngresoRegistro" = :v_FechaHoraSistema
		and "EsOrdenEspecial" = 'S'; 
		
		select COUNT("1") into v_countRegistros4x from "BSCL.Orden.BD::ORD.Orden" where "FechaIngresoRegistro" = :v_FechaHoraSistema
		and "EsOrdenEspecial" = 'N' AND "IdBolsa" IN ('BCS','BCV','BVM'); 
		
		select COUNT("1") into v_countRegistros1x from "BSCL.Orden.BD::ORD.Orden" where "FechaIngresoRegistro" = :v_FechaHoraSistema
		and "EsOrdenEspecial" = 'N' AND "IdTipoCanalDistribucion" = 'MAN'; 
		
		select COUNT("1") into v_countRegistros2x from "BSCL.Orden.BD::ORD.Orden" where "FechaIngresoRegistro" = :v_FechaHoraSistema
		AND 
			--omite 6x
			("IdTipoCanalIngreso" != 'TRA' or "FormadorMercado" != 'S')
		AND 
			--omite 5x
			"EsOrdenEspecial" = 'N' 
		AND 
			--omite 4x
			"IdBolsa" not IN ('BCS','BCV','BVM')
		AND 
			--omite 1x
			"IdTipoCanalDistribucion" != 'MAN'; 

		
		if(v_countRegistros1x=0)THEN
			SELECT 'se reinicia secuencia 1x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 1x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio1x" RESET BY 0;
		END IF;
		
		if(v_countRegistros2x=0)THEN
			SELECT 'se reinicia secuencia 2x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 2x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio2x" RESET BY 0;
		END IF;
		
		if(v_countRegistros3x=0)THEN
			SELECT 'se reinicia secuencia 3x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 3x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio3x" RESET BY 0;
		END IF;
		
		if(v_countRegistros4x=0)THEN
			SELECT 'se reinicia secuencia 4x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 4x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio4x" RESET BY 0;
		END IF;
		
		if(v_countRegistros5x=0)THEN
			SELECT 'se reinicia secuencia 5x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 5x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio5x" RESET BY 0;
		END IF;
		
		if(v_countRegistros6x=0)THEN
			SELECT 'se reinicia secuencia 6x' FROM dummy;
			p_out_Mensaje:=p_out_Mensaje || 'se reinicia secuencia 6x; ';
			ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::ORD.Folio6x" RESET BY 0;
		END IF;
		p_out_Mensaje:=p_out_Mensaje || 'Fin.'
		p_out_ErroresContador := 0;
	END;
END;
GO