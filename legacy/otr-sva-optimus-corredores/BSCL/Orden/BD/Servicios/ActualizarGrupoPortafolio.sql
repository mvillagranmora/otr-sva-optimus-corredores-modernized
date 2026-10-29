SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Orden.BD.Servicios::ActualizarGrupoPortafolio";
-- 
--  Proposito: Servicio para la Actualizacion del grupo y la relacion grupoPortafolio.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 24/02/2016
--  
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::ActualizarGrupoPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	IN  p_IdGrupo					BIGINT,
	IN  p_Nombre					NVARCHAR(40) DEFAULT NULL,
	IN  p_Codigo					NVARCHAR(40) DEFAULT NULL,
	IN  p_IdEstado					NVARCHAR(3) DEFAULT NULL,
	IN	p_tablaClientesPortafolio	TABLE("IdPortafolio" BIGINT,"Porcentaje" NVARCHAR(30)),
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador INT,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion"	BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion"	BIGINT,"Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador INT,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion"	BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion",'' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :v_Errores,
		
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	

	BEGIN

	
		--------------------
		--Negocio Aqui
		--------------------

		v_Repetidos = SELECT "IdGrupo" FROM "BSCL.Orden.BD::ORD.Grupo" WHERE "Codigo" = :p_Codigo and "IdEmpresa" = :p_IdEmpresa AND "IdGrupo" != :p_IdGrupo AND :p_Codigo IS NOT NULL;
		IF (NOT IS_EMPTY(:v_Repetidos)) THEN
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_AL_ACTUALIZAR_GRUPO_CODIGO_REPETIDO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		
		END IF;
		
		v_Repetidos = SELECT "IdGrupo" FROM "BSCL.Orden.BD::ORD.Grupo" WHERE "Nombre" = :p_Nombre and "IdEmpresa" = :p_IdEmpresa AND "IdGrupo" != :p_IdGrupo AND :p_Nombre IS NOT NULL;
		IF (NOT IS_EMPTY(:v_Repetidos)) THEN
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_AL_ACTUALIZAR_GRUPO_NOMBRE_REPETIDO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		
		END IF;

		DELETE FROM "BSCL.Orden.BD::ORD.GrupoPortafolio" WHERE "IdGrupo" = :p_IdGrupo and "IdEmpresa" = :p_IdEmpresa;
		
		UPDATE "BSCL.Orden.BD::ORD.Grupo" 
		SET "Nombre" = COALESCE(:p_Nombre,"Nombre"),
		"IdEstado" = COALESCE(:p_IdEstado,"IdEstado"), 
		"Codigo" = COALESCE(:p_Codigo,"Codigo")
		WHERE "IdGrupo" = :p_IdGrupo and "IdEmpresa" = :p_IdEmpresa;
		
		INSERT INTO "BSCL.Orden.BD::ORD.GrupoPortafolio"("IdGrupoPortafolio", "IdEmpresa", "IdGrupo", "IdPortafolio","Porcentaje")
		SELECT "BSCL.Orden.BD.Secuencias::GrupoPortafolio".NEXTVAL as "IdGrupoPortafolio", :p_IdEmpresa as "IdEmpresa",:p_IdGrupo AS "IdGrupo","IdPortafolio",CAST("Porcentaje" AS DOUBLE)
		FROM :p_tablaClientesPortafolio;
		
		v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'INFORMACION' AS "Resultado",'GRUPO_ACTUALIZADO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
		SIGNAL SALIDA_CONTROLADA;
		
	END;
END;
