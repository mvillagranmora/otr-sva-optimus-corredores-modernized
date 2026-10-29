SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearGrupo";
-- 
--  Proposito: Servicio para la Actualizacion del grupo y la relacion grupoPortafolio.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 24/02/2016
--  
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CrearGrupo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	IN  p_Nombre					NVARCHAR(40),  		
	IN  p_Codigo					NVARCHAR(40),  		
	IN  p_IdEstado					NVARCHAR(3),
	IN	p_tablaClientesPortafolio	TABLE("IdPortafolio" BIGINT,"Porcentaje" DECIMAL(26,8)),
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador INT,
	OUT p_out_IdGrupo					BIGINT,
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
		DECLARE v_IdGrupo BIGINT;
		
		v_Repetidos = SELECT "IdGrupo" FROM "BSCL.Orden.BD::ORD.Grupo" WHERE "Codigo" = :p_Codigo and "IdEmpresa" = :p_IdEmpresa;
		IF (NOT IS_EMPTY(:v_Repetidos)) THEN
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_AL_CREAR_GRUPO_CODIGO_REPETIDO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		
		END IF;
		
		v_Repetidos = SELECT "IdGrupo" FROM "BSCL.Orden.BD::ORD.Grupo" WHERE "Nombre" = :p_Nombre and "IdEmpresa" = :p_IdEmpresa;
		IF (NOT IS_EMPTY(:v_Repetidos)) THEN
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_AL_CREAR_GRUPO_NOMBRE_REPETIDO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		
		END IF;
		
		
		
		SELECT "BSCL.Orden.BD.Secuencias::Grupo".NEXTVAL into v_IdGrupo  from dummy;
		
		INSERT INTO "BSCL.Orden.BD::ORD.Grupo" ("IdGrupo","IdEmpresa","Nombre","IdEstado","Codigo")  
		VALUES(v_IdGrupo,p_IdEmpresa,p_Nombre,p_IdEstado,p_Codigo);
		
		INSERT INTO "BSCL.Orden.BD::ORD.GrupoPortafolio"("IdGrupoPortafolio", "IdEmpresa", "IdGrupo", "IdPortafolio","Porcentaje")
		SELECT "BSCL.Orden.BD.Secuencias::GrupoPortafolio".NEXTVAL as "IdGrupoPortafolio", :p_IdEmpresa as "IdEmpresa",v_IdGrupo AS "IdGrupo","IdPortafolio","Porcentaje"
		FROM :p_tablaClientesPortafolio;
		
		v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'INFORMACION' AS "Resultado",'GRUPO_CREADO' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" from dummy;
		p_out_IdGrupo = v_IdGrupo;
		SIGNAL SALIDA_CONTROLADA;
	END;
END;
