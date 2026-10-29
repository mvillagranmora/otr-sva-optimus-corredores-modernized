SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarLogProgramas";
--DEPRECATED USAR "BSCL.Plataforma.BD.Servicios::BAT.AgregarLogPrograma"
CREATE PROCEDURE
-- 
--  Proposito: Agrega log por programa
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogProgramas"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									IN	p_IdTipoLog 			NVARCHAR(40),
 									IN 	p_EstadoRegistro		NVARCHAR(200),   
									IN	p_IdTipoOperacion		NVARCHAR(200),
									IN 	p_IdProceso				BIGINT,
									IN  p_IdTipoRegistro		NVARCHAR(200),
 									IN	p_IdRegistro			BIGINT,
 	 								IN  p_EstadoProceso			NVARCHAR(80),	
									IN  p_NombreProceso			NVARCHAR(40),

 									OUT msg_out  				NVARCHAR(520)
 	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
  		DECLARE p_out_refID BIGINT;
		declare p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		
		p_DetalleLog =
		SELECT null AS "SubRegistro",null AS "CampoRegistro",null AS "ValorAntiguo",null AS "ValorNuevo" FROM DUMMY;
		
		
 
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
 		p_IdTipoLog => p_IdTipoLog
 		,p_IdModulo => p_IdModulo
 		,p_IdUsuario => p_IdUsuario
 		,p_IdEmpresa => p_IdEmpresa
 		,p_FechaSistemaLog => CURRENT_UTCTIMESTAMP
 		,p_IdUsuarioAtendido => p_IdUsuario
 	--	,0
 	--	,''
 	--	,''
 	--	,''
 	--	,''
 	--	,0
 	--	,''
 		,p_IdTipoRegistro => p_IdTipoRegistro
 		,p_IdRegistro => p_IdRegistro
 		,p_EstadoRegistro => p_EstadoRegistro
 		,p_IdTipoOperacion => p_IdTipoOperacion
 	--	,0
 	--	,0
 	--	,0
 	--	,0
 	--	,''
 		,p_IdProceso => p_IdProceso			 
	 	,p_EstadoProceso => p_EstadoProceso			
		,p_NombreProceso => p_NombreProceso
		,p_DetalleLog => :p_DetalleLog
		);
		
	
 
	 
 END;


 
