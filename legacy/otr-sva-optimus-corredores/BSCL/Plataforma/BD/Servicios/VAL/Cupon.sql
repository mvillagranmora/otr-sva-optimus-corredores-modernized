SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::VAL.Cupon";
GO

CREATE PROCEDURE   
--ALTER PROCEDURE
----------------------------------------------------------------------------------------------------- 
--  Proposito	: Obtener cupon. Servicio publico para la obtencion de cupon.
--  Autor		: JGarces
--  Fecha 		: 10/11/2016  

"BSCL.Plataforma.BD.Servicios::VAL.Cupon" (
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),   
	-- OPCIONES DE VALORIZACION
	IN p_RutinaValorizador  NVARCHAR(04) default 'BCS', -- Rutina de valorización desde parametro de sistema
	-- IN p_in_ListaInstrumentoXXs 	-> "BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos",
	-- OUT p_out_ListaCuponeXXs 	-> "BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones" 	
	OUT p_out_Mensaje 		nvarchar(520), --CONTROL DE SALIDA
	OUT p_out_TicketProcesamiento	bigint
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	/******************************************
	* SECCION - DECLARACION VARIABLES
	******************************************/
	declare v_in_ListaInstrumentosIds table(
		"IdInterno" 			BIGINT, 	
		"Nemo"					NVARCHAR(100),-- DATOS ENTRADA CUPON
		"FechaIni"				DATE,
		"FechaFin"				DATE,
		"CodTipoMod"			NVARCHAR(1),
		"Plazo"					BIGINT,
		"IdTarea" 				BIGINT		
	);

	declare v_out_Mensaje 		nvarchar(520):='';
	
	declare v_TicketProcesamiento bigint:=0;
	
	/******************************************
	* SECCION - LOGICA SP
	******************************************/
	SELECT "BSCL.Plataforma.BD.Secuencias::TicketProcesamientoCupon".NEXTVAL INTO v_TicketProcesamiento FROM DUMMY;
	
	insert into "BSCL.Plataforma.BD::VAL.ProcesamientoCupon"
		("IdProcesamientoCupon","TicketProcesamiento","IdTarea","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo")
	select "BSCL.Plataforma.BD.Secuencias::IdProcesamientoCupon".NEXTVAL,:v_TicketProcesamiento,
		"IdTarea","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo" from "BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos";
		
	DELETE FROM "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos";
		
	insert into "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos"
--	select "BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion".NEXTVAL as "IdTarea","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo" 
	select "IdTarea","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo"
	from "BSCL.Plataforma.BD::VAL.ProcesamientoCupon" where "TicketProcesamiento" = :v_TicketProcesamiento  
	group by "IdTarea","Nemo","FechaIni","FechaFin","CodTipoMod","Plazo" ;
	
/*	update pc set pc."IdTarea" = li."IdTarea" 
		from "BSCL.Plataforma.BD::VAL.ProcesamientoCupon" pc 
			join "BSCL.Plataforma.BD::VAL.CuponMasivo_Entrada_Instrumentos" li on (
					pc."Nemo"=li."Nemo" and 
					pc."FechaIni"=li."FechaIni" and 
					pc."FechaFin"=li."FechaFin" and 
					pc."CodTipoMod"=li."CodTipoMod" and 
					pc."Plazo"=li."Plazo")
	where pc."TicketProcesamiento" = :v_TicketProcesamiento;
*/
	call "BSCL.Plataforma.BD.Servicios::VAL.CuponMasivo"(
	    p_IdEmpresa 			=> :p_IdEmpresa,
	    p_IdUsuario 			=> :p_IdUsuario,
	    p_IdModulo				=> :p_IdModulo,  
		p_RutinaValorizador 	=> :p_RutinaValorizador,				 									 									  										
		p_out_Mensaje 			=> :v_out_Mensaje);
		
	update proc set 
		"OUT_GrupoValorizacion" 	= lc."GrupoValorizacion",
		"OUT_FechaSolicitud" 		= lc."FechaSolicitud",
	 	"OUT_IdEstado" 				= lc."IdEstado",
		"OUT_Nemo" 					= lc."Nemo",
		"OUT_FechaIni" 				= lc."FechaIni",
		"OUT_FechaFin" 				= lc."FechaFin",
		"OUT_CodTipoMod" 			= lc."CodTipoMod",
		"OUT_Plazo" 				= lc."Plazo",
		"OUT_Glosa" 				= lc."Glosa",
		"OUT_CodigoEmisor" 			= lc."CodigoEmisor",
		"OUT_FechaEmision" 			= lc."FechaEmision",
		"OUT_FechaVencimiento" 		= lc."FechaVencimiento",
		"OUT_TipoMod" 				= lc."TipoMod",
		"OUT_TipoUnidad" 			= lc."TipoUnidad",
		"OUT_ValorReferencial" 		= lc."ValorReferencial",
		"OUT_CuponAmortizacion" 	= lc."CuponAmortizacion",
		"OUT_CuponFactorAjuste" 	= lc."CuponFactorAjuste",
		"OUT_CuponFechaEfectiva" 	= lc."CuponFechaEfectiva",
		"OUT_CuponFechaHabil" 		= lc."CuponFechaHabil",
		"OUT_CuponFlujo" 			= lc."CuponFlujo",
		"OUT_CuponIntereses" 		= lc."CuponIntereses",
		"OUT_CuponSecuencia" 		= lc."CuponSecuencia",
		"OUT_WsCallerCodSalida" 	= lc."WsCallerCodSalida",
		"OUT_WsCallerDescSalida" 	= lc."WsCallerDescSalida" 
	from "BSCL.Plataforma.BD::VAL.ProcesamientoCupon" proc
	left join "BSCL.Plataforma.BD::VAL.CuponMasivo_Salida_Cupones" lc on(lc."IdTarea" = proc."IdTarea")
	where "TicketProcesamiento" = :v_TicketProcesamiento;
	
	/******************************************
	* SECCION - TRATAMIENTO SALIDA
	******************************************/	
	insert into "BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones"
	(	"IdTarea",
		"GrupoValorizacion",
		"FechaSolicitud",
		"IdEstado",
		"Nemo",
		"FechaIni",
		"FechaFin",
		"CodTipoMod",
		"Plazo",
		"Glosa",
		"CodigoEmisor",
		"FechaEmision",
		"FechaVencimiento",
		"TipoMod",
		"TipoUnidad",
		"ValorReferencial",
		"CuponAmortizacion",
		"CuponFactorAjuste",
		"CuponFechaEfectiva",
		"CuponFechaHabil",
		"CuponFlujo",
		"CuponIntereses",
		"CuponSecuencia",
		"WsCallerCodSalida",
		"WsCallerDescSalida"
	) select 
		proc."IdTarea"					, -- "IdTarea"
		proc."OUT_GrupoValorizacion" 	, -- "GrupoValorizacion"
		proc."OUT_FechaSolicitud" 		, -- "FechaSolicitud"
	 	proc."OUT_IdEstado" 			, -- "IdEstado"
		proc."OUT_Nemo" 				, -- "Nemo"
		proc."OUT_FechaIni" 			, -- "FechaIni"
		proc."OUT_FechaFin" 			, -- "FechaFin"
		proc."OUT_CodTipoMod" 			, -- "CodTipoMod"
		proc."OUT_Plazo" 				, -- "Plazo"
		proc."OUT_Glosa" 				, -- "Glosa"
		proc."OUT_CodigoEmisor" 		, -- "CodigoEmisor"
		proc."OUT_FechaEmision" 		, -- "FechaEmision"
		proc."OUT_FechaVencimiento" 	, -- "FechaVencimiento"
		proc."OUT_TipoMod"	 			, -- "TipoMod"
		proc."OUT_TipoUnidad" 			, -- "TipoUnidad"
		proc."OUT_ValorReferencial" 	, -- "ValorReferencial"
		proc."OUT_CuponAmortizacion" 	, -- "CuponAmortizacion"
		proc."OUT_CuponFactorAjuste" 	, -- "CuponFactorAjuste"
		proc."OUT_CuponFechaEfectiva" 	, -- "CuponFechaEfectiva"
		proc."OUT_CuponFechaHabil" 		, -- "CuponFechaHabil"
		proc."OUT_CuponFlujo" 			, -- "CuponFlujo"
		proc."OUT_CuponIntereses" 		, -- "CuponIntereses"
		proc."OUT_CuponSecuencia" 		, -- "CuponSecuencia"
		proc."OUT_WsCallerCodSalida" 	, -- "WsCallerCodSalida"
		proc."OUT_WsCallerDescSalida" 	  -- "WsCallerDescSalida" 
	from "BSCL.Plataforma.BD::VAL.ProcesamientoCupon" as proc
	where "TicketProcesamiento" = :v_TicketProcesamiento;
	
	p_out_Mensaje=:v_out_Mensaje;
	p_out_TicketProcesamiento := v_TicketProcesamiento;
	
end;
GO