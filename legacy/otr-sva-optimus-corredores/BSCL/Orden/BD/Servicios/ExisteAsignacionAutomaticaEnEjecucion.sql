SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ExisteAsignacionAutomaticaEnEjecucion";
CREATE PROCEDURE
-- 
--  Proposito: Crear asignaciones de forma masiva
--  Autor: 
--  Fecha Creacion: 20/07/2015 
--  
"BSCL.Orden.BD.Servicios::ExisteAsignacionAutomaticaEnEjecucion" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
	
	IN p_IdLibroOrdenes 				  BIGINT,
	IN p_IdTipoPrograma					  NVARCHAR(40),
 
	OUT salida							TABLE ( contador BIGINT ),
	OUT salida_alt						BIGINT ,
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ( "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_asignaciones 			= SELECT  0 as "IdAsignacion" from dummy where 1=2; 
	
	BEGIN
	salida = 
	select count(1) as contador from "BSCL.Plataforma.BD::BAT.Programa" programm
			LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosPrograma" param
			on programm."IdTipoPrograma" = p_IdTipoPrograma
			where  param."Valor" = p_IdLibroOrdenes and param."NombreParametro" = 'IdLibroOrdenes' and programm."idEstado" = 'PEN' or programm."idEstado" = 'ASI';

		
	select count(1) into salida_alt  from "BSCL.Plataforma.BD::BAT.Programa" programm
			LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosPrograma" param
			on programm."IdTipoPrograma" = p_IdTipoPrograma
			where  param."Valor" = p_IdLibroOrdenes and param."NombreParametro" = 'IdLibroOrdenes' and programm."idEstado" = 'PEN' or programm."idEstado" = 'ASI';

		
	END;
END;
