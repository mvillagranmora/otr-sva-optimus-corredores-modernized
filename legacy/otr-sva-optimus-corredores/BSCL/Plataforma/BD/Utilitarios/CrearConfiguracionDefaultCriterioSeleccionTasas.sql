SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::CrearConfiguracionDefaultCriterioSeleccionTasas";
CREATE PROCEDURE 
-- 
--  Proposito		: Establece de manera predeterminada criterios de selección 
--					  de tasa/precio para un IdClasificacionTipoInstrumento dado
--  Autor			: JG - BCS
--  Fecha Creacion	: 01/03/2017
--   

/*rollback;
call "BSCL.Plataforma.BD.Utilitarios::CrearConfiguracionDefaultCriterioSeleccionTasas"(
	p_IdEmpresa => 1,
	p_IdUsuario => 37,
	p_IdModulo => 'PLA',
	p_UsaReglasNegocio => 'N',
	p_IdClasificacionTipoInstrumento => 'RV',
	p_BorrarCriteriosAnteriores => 'S'
);
commit;*/

"BSCL.Plataforma.BD.Utilitarios::CrearConfiguracionDefaultCriterioSeleccionTasas" (
	IN p_IdEmpresa			bigint,
	IN p_IdUsuario			bigint,
	IN p_IdModulo			nvarchar(3),
	IN p_UsaReglasNegocio	nvarchar(1),
	IN p_IdClasificacionTipoInstrumento nvarchar(3) default '',
	IN p_IdUso 				nvarchar(40) default '',
	IN p_BorrarCriteriosAnteriores nvarchar(1) default 'N'
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--ERRORES
	BEGIN -- NEGOCIO
		--DECLARE
		declare v_out_RefId	BIGINT := 0;
		declare v_out_AdvertenciasContador INT := 0;
		declare v_out_ErroresContador INT := 0;
		declare v_IdClasificacionTipoInstrumento nvarchar(3) := '';
		/****************************
			 	CONFIGURACIONES
		*****************************/
		v_IdClasificacionTipoInstrumento = :p_IdClasificacionTipoInstrumento;
		v_Antes = select 'ANTES',* from "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" where "IdClasificacionTipoInstrumento" = :v_IdClasificacionTipoInstrumento and "IdUso" = :p_IdUso;
		if(p_BorrarCriteriosAnteriores = 'S') then
			delete from "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" where "IdClasificacionTipoInstrumento" = :v_IdClasificacionTipoInstrumento and "IdUso" = :p_IdUso;
		end if;
		
		CALL "BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas" (
			p_IdEmpresa							=> :p_IdEmpresa,
			p_IdUsuario							=> :p_IdUsuario,
			p_IdModulo							=> :p_IdModulo,
			p_UsaReglasNegocio					=> :p_UsaReglasNegocio,
			p_Prioridad							=> 1,
			p_TipoCartera						=> '0',
			p_IdUso								=> :p_IdUso,
			p_IdClasificacionTipoInstrumento	=> :v_IdClasificacionTipoInstrumento,
			p_IdTipoInstrumento					=> '0',
			p_DiasAntiguedad					=> 0,
			p_IdTipoCriterioTasa				=> '0',
			p_CodigoFuente						=> 'PRECIOSBCS',
			p_IdTipoTasa						=> 'TASA',
			p_IdTipoPrecio						=> 'M',
			p_Interpolar						=> 'N',
		    p_out_RefId							=> v_out_RefId,
			p_out_AdvertenciasContador			=> v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle			=> v_out_AdvertenciasDetalle,	
			p_out_ErroresContador				=> v_out_ErroresContador,
			p_out_ErroresDetalle				=> v_out_ErroresDetalle
		);
		
		CALL "BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas" (
			p_IdEmpresa							=> :p_IdEmpresa,
			p_IdUsuario							=> :p_IdUsuario,
			p_IdModulo							=> :p_IdModulo,
			p_UsaReglasNegocio					=> :p_UsaReglasNegocio,
			p_Prioridad							=> 2,
			p_TipoCartera						=> '0',
			p_IdUso								=> :p_IdUso,
			p_IdClasificacionTipoInstrumento	=> :v_IdClasificacionTipoInstrumento,
			p_IdTipoInstrumento					=> '0',
			p_DiasAntiguedad					=> 0,
			p_IdTipoCriterioTasa				=> '0',
			p_CodigoFuente						=> 'INSTRUMENTO',
			p_IdTipoTasa						=> 'TASA',
			p_IdTipoPrecio						=> 'M',
			p_Interpolar						=> 'N',
		    p_out_RefId							=> v_out_RefId,
			p_out_AdvertenciasContador			=> v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle			=> v_out_AdvertenciasDetalle,	
			p_out_ErroresContador				=> v_out_ErroresContador,
			p_out_ErroresDetalle				=> v_out_ErroresDetalle
		);
		
		CALL "BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas" (
			p_IdEmpresa							=> :p_IdEmpresa,
			p_IdUsuario							=> :p_IdUsuario,
			p_IdModulo							=> :p_IdModulo,
			p_UsaReglasNegocio					=> :p_UsaReglasNegocio,
			p_Prioridad							=> 3,
			p_TipoCartera						=> '0',
			p_IdUso								=> :p_IdUso,
			p_IdClasificacionTipoInstrumento	=> :v_IdClasificacionTipoInstrumento,
			p_IdTipoInstrumento					=> '0',
			p_DiasAntiguedad					=> 0,
			p_IdTipoCriterioTasa				=> '0',
			p_CodigoFuente						=> 'TIPOINSTRUMENTO',
			p_IdTipoTasa						=> 'TASA',
			p_IdTipoPrecio						=> 'M',
			p_Interpolar						=> 'N',
		    p_out_RefId							=> v_out_RefId,
			p_out_AdvertenciasContador			=> v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle			=> v_out_AdvertenciasDetalle,	
			p_out_ErroresContador				=> v_out_ErroresContador,
			p_out_ErroresDetalle				=> v_out_ErroresDetalle
		);
		
		select * from :v_Antes union all select 'DESPUES',* from "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" where "IdClasificacionTipoInstrumento" = :v_IdClasificacionTipoInstrumento and "IdUso" = :p_IdUso;
	END; -- NEGOCIO
END;
