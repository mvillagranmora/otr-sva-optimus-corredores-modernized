SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarProgramaSinParametros";

CREATE PROCEDURE
-- 
--  Proposito: Agrega Programa sin parametros, excepto el idempresa
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarProgramaSinParametros"
 (
	IN 	p_IdEmpresa          		BIGINT,
	IN 	p_IdUsuario          		BIGINT,
	IN 	p_IdModulo           		NVARCHAR(3),
	IN	p_IdTipoTarea				NVARCHAR(40),   
	OUT p_out_IdPrograma			TABLE("IdPrograma" BIGINT),
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))				
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

	p_TablaParametros = SELECT 'IdEmpresa' as "NombreParametro",:p_IdEmpresa as "Valor" FROM DUMMY;

	CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(	
	   		p_IdEmpresa 				=> :p_IdEmpresa,
   			p_IdUsuario					=> :p_IdUsuario ,
	   		p_IdModulo					=> :p_IdModulo,
   			p_IdAgenda 					=> 0,
   			p_IdTipoTarea 				=> :p_IdTipoTarea,
   			p_TablaParametros			=> :p_TablaParametros,
   			p_Segundos 					=> 0,
   			p_CreaParam 				=> 'S',
   			p_out_IdPrograma 			=> p_out_IdPrograma,
   			p_out_AdvertenciasContador 	=> p_out_AdvertenciasContador,
   			p_out_AdvertenciasDetalle 	=> p_out_AdvertenciasDetalle,
   			p_out_ErroresContador 		=> p_out_ErroresContador,
   			p_out_ErroresDetalle 		=> p_out_ErroresDetalle
   		);
	 

END;
