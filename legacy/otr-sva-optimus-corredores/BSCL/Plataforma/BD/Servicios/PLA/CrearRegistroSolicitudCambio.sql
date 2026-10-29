SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio";

CREATE PROCEDURE  
-- 
--  Proposito: Servicio Interno que crea solicitudes de cambio segun una estructura de valores 
--  enviados desde las entidades del Negocio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/12/2015
--  
"BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio" (
	-- Parametros entrada comunes en todos los servicios publicos

	
	
	IN p_RegistroSolicitudCambio  TABLE("IdSolicitudCambio"		BIGINT,	
										"IdTipoEntidad"			NVARCHAR(40),
										"IdEntidadOrigen"		NVARCHAR(40),
										"CampoModificado"		NVARCHAR(255),
										"TipoDato"				NVARCHAR(255),
										"ValorAntiguo"			NVARCHAR(255),
										"ValorNuevo"			NVARCHAR(255),
										"IdEstadoAutorizacion"	NVARCHAR(3)),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId bigint
	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
		
		/*DECLARE v_TablaCambios 	TABLE ("IdSolicitudCambio"		BIGINT,
								  "IdSecuenciaCambio"		BIGINT,	
								  "IdTipoEntidad"			NVARCHAR(40),
								  "IdEntidadOrigen"			BIGINT,
								  "CampoModificado"			NVARCHAR(255),
								  "TipoDato"				NVARCHAR(255),
								  "ValorAntiguo"			NVARCHAR(255),
								  "ValorNuevo"				NVARCHAR(255),
								  "IdEstadoAutorizacion"	NVARCHAR(3));
*/

	BEGIN	 
	
--				"BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL) as "IdSolicitudCambio",	

		v_TablaCambios= SELECT	
				("BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio".NEXTVAL) as "IdSecuenciaCambio",
				"IdSolicitudCambio",
				"IdTipoEntidad",
				"IdEntidadOrigen",
				"CampoModificado",
				"TipoDato",
				"ValorAntiguo",
				"ValorNuevo",
				"IdEstadoAutorizacion"
		FROM :p_RegistroSolicitudCambio;


		
		
			
		
		SELECT "IdSolicitudCambio",
			"IdSecuenciaCambio",
			"IdTipoEntidad",
			"IdEntidadOrigen",
			"CampoModificado",
			"TipoDato",
			"ValorAntiguo",
			"ValorNuevo",
			CURRENT_UTCTIMESTAMP as "FechaSolicitud",
			'0001-01-01' as "FechaRevision",
			"IdEstadoAutorizacion"
 		FROM :v_TablaCambios		
		INTO "BSCL.Plataforma.BD::PLA.SolicitudCambio"(
			"IdSolicitudCambio",
			"IdSecuenciaCambio",
			"IdTipoEntidad",
			"IdEntidadOrigen",
			"CampoModificado",
			"TipoDato",
			"ValorAntiguo",
			"ValorNuevo",
			"FechaSolicitud",
			"FechaRevision",
			"IdEstadoAutorizacion");
		
		
		 p_out_IdSecuencias =   SELECT "IdSecuenciaCambio" as "IdSecuencia" FROM :v_TablaCambios;
 
 
 
	END;	
END;