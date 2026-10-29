SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas";
CREATE PROCEDURE 
-- 
--  Proposito: Simplificar la creación de pautas del sistema. (utilitario)
--  Autor: - JG - BCS
--  Fecha Creacion: 15/02/2017
--   
"BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	IN  p_IdEmpresa				BIGINT,
	IN  p_IdTipoPrograma		NVARCHAR(40),
	IN  p_Descripcion			NVARCHAR(255),
	IN	p_NombreClasePrograma	NVARCHAR(255),
	IN	p_Nodo					NVARCHAR(1) default '',
	IN	p_IdModulo				NVARCHAR(10) default 'PLA',
	IN	p_Hora					NVARCHAR(2) default '**',
	IN	p_Minuto				NVARCHAR(2) default '90',
	IN	p_DependenciaAnterior	NVARCHAR(40) default '',
	IN	p_DependenciaProximo	NVARCHAR(40) default '',
	IN  p_Parametros			TABLE("NombreParametro" nvarchar(100) , "Valor" nvarchar(100))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
--READS SQL DATA
AS
BEGIN
	declare v_IdAgenda 		bigint := 0;
	declare v_IdPrograma 	bigint := 0;
	declare v_out_AdvertenciasContador bigint := 0;
	declare v_out_ErroresContador 		bigint := 0;
	
	/***********************
		BORRAR EXISTENTE
	***********************/
	delete from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" where "IdPrograma" in (
		select "IdPrograma" from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = :p_IdTipoPrograma
	);
	delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" 
		where "IdAgenda" in (select "IdAgenda" from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"=:p_IdTipoPrograma);
	delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"=:p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::PAU.Dependencia" where "IdTipoPrograma" = :p_IdTipoPrograma and "Ejecucion" = 'PROGRAMADA';
	/***********************
		CREAR NUEVO
	***********************/	
	--TIPO PROGRAMA
	INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma", "Nodo","EsPauta")
	SELECT 
	:p_IdTipoPrograma as "IdTipoPrograma", 
	:p_Descripcion as "Descripcion", 
	1 as "MaximoIntentos",
	:p_IdModulo as "Modulo",
	:p_NombreClasePrograma as "NombreClasePrograma", 
	'JAVA' as "TipoClasePrograma",
	:p_Nodo as "Nodo",
	'S' as "EsPauta"
	from dummy;
	--AGENDA
	call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
	(
		p_IdEmpresa			 		=> :p_IdEmpresa,
		p_IdUsuario 				=> 0,
		p_IdModulo 					=> :p_IdModulo,                         
		p_IdTipoTarea 				=> :p_IdTipoPrograma,
		p_EsDemonio 				=> 'N',   
		p_DiaDeMes 					=> '**',
		p_DiaDeSemana 				=> '*',
		p_Mes 						=> '**',
		p_Hora 						=> :p_Hora,
		p_Minuto 					=> :p_Minuto,
		p_SoloDiasHabiales 			=> 'S',
		p_SoloPrincipioMes 			=> 'N',
		p_SoloFinDeMes 				=> 'N',
		p_habilitado 				=> 'S', 
		p_TablaParametros 			=> :p_Parametros,                
		p_IdAgenda 					=> :v_IdAgenda,
		p_IdPrograma 				=> :v_IdPrograma,
		p_out_AdvertenciasContador 	=> :v_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle 	=> :v_out_AdvertenciasDetalle,
		p_out_ErroresContador 		=> :v_out_ErroresContador,
		p_out_ErroresDetalle 		=> :v_out_ErroresDetalle
	);
	-- DEPENDENCIA
	insert into "BSCL.Plataforma.BD::PAU.Dependencia" select :p_IdTipoPrograma,'PROGRAMADA',:p_DependenciaAnterior,:p_DependenciaProximo from dummy;
	/***************
		EVAL RES
	***************/
	if(v_out_ErroresContador=0 and :v_IdAgenda>0) then
		commit;
		select 'COMMIT REALIZADO' from dummy;
	else 
		rollback;
		select 'SE HIZO ROLLBACK' from dummy;
	end if;
	/***************
		SALIDA
	***************/	
	select 'v_out_ErroresDetalle',* from :v_out_ErroresDetalle; 
	select 'v_out_AdvertenciasDetalle',* from :v_out_AdvertenciasDetalle; 	
	select :v_IdAgenda as IDAGENDA,:v_IdPrograma as IDPROGRAMA, :v_out_ErroresContador as ERRORESCONTADOR,:v_out_AdvertenciasContador as ADVERTENCIASCONTADOR from dummy;	 	
END;






