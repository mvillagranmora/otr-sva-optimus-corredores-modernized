SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarRegistroParticipes";

CREATE PROCEDURE
--  Proposito: Crear reporte Registro de Participes.
--  Autor: Gat-Blac 
--  Fecha Creacion: 07/03/2018
"BSCL.FondoMutuo.BD.Servicios::ConsultarRegistroParticipes" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT, 
    IN p_registroDate							NVARCHAR(10), 
    IN p_IdFondo								NVARCHAR(40), 
    IN p_IdSerie	 							NVARCHAR(40) DEFAULT '', 
    IN p_IdCliente								BIGINT DEFAULT 0
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN	
	BEGIN	
		BEGIN		
			---------------------
			--    Variables    --
			---------------------
			DECLARE v_FechaHoraSistema SECONDDATE;
			DECLARE v_HoraSistema TIME;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa, v_FechaHoraSistema);
			
			v_HoraSistema = TO_TIME (v_FechaHoraSistema);
			
			v_resultado = SELECT 
				TO_CHAR(v_FechaHoraSistema,'DD-MM-YYYY') AS "FechaGeneracionReporte",
				add_seconds(:v_HoraSistema,-10800) AS "HoraGeneracionReporte",
				
				CASE 
					WHEN :p_IdFondo != '' 
						THEN "Fondo"."NombreFondo" 
						ELSE 'TODAS' 
				END AS "NombreFondo", 
				CASE 
					WHEN :p_IdSerie != '' 
						THEN "SerieDeFondo"."NombreSerieFondo" 
						ELSE 'TODAS' 
				END AS "NombreSerieFondo", 
				CASE 
					WHEN "ConsultarCliente"."NombreCompleto" != '' 
						THEN "ConsultarCliente"."NombreCompleto" 
						ELSE "ConsultarCliente"."RazonSocial" 
				END AS "Participe",
				"ConsultarCliente"."DocumentoIdentidad" AS "RUT",
				"SolicitudAporteRescate"."NumeroCuotas" AS "CantidadCuotas",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOREINV','APODIV','APOOVER','APORESPRO','INGTIT') 
						THEN TO_CHAR("SolicitudAporteRescate"."FechaAceptacion",'DD-MM-YYYY')
						ELSE '' 
				END AS "FechaAporte",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOREINV','APODIV','APOOVER','APORESPRO','INGTIT') 
						THEN TO_TIME ("SolicitudAporteRescate"."FechaAceptacionRegistro") 
						ELSE '' 
				END AS "HoraAporte",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOREINV','APODIV','APOOVER','APORESPRO','INGTIT')  
						THEN "MedioDePago"."Nombre" 
						ELSE '' 
				END AS "FormaAporte",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('RESCAM','RESNORM','RESREINV','RESCOM','RETTIT','RESTRASP')
						THEN TO_CHAR("SolicitudAporteRescate"."FechaAceptacion",'DD-MM-YYYY') 
						ELSE '' 
				END AS "FechaRescate",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('RESCAM','RESNORM','RESREINV','RESCOM','RETTIT','RESTRASP')
						THEN TO_TIME ("SolicitudAporteRescate"."FechaAceptacionRegistro") 
						ELSE '' 
				END AS "HoraRescate",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('RESCAM','RESNORM','RESREINV','RESCOM','RETTIT','RESTRASP')
						THEN "MedioDePago"."Nombre" 
						ELSE '' 
				END AS "FormaRescate",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('RESCAM','RESNORM','RESREINV','RESCOM','RETTIT','RESTRASP') 
						THEN TO_CHAR("SolicitudAporteRescate"."FechaLiquidacion",'DD-MM-YYYY') 
						ELSE '' 
				END AS "FechaDePago",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" = 'RESTRANSF' 
						THEN TO_CHAR("SolicitudAporteRescate"."FechaCertificacion",'DD-MM-YYYY') 
						ELSE '' 
				END AS "FechaTransferencia",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" = 'RESTRANSF' 
						THEN TO_TIME ("SolicitudAporteRescate"."FechaCertificacionRegistro") 
						ELSE '' 
				END AS "HoraTransferencia",
				CASE 
					WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" = 'RESTRANSF' 
						THEN "MedioDePago"."Nombre" --TODO Validar que quieren que se muestre en esta columna
						ELSE '' 
				END AS "FormaTransferencia" 
	
				FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate" 
				LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
				LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )						
				LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio" ) 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" ) 
				LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo") 
				LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "SolicitudAporteRescate"."IdMedioDePago") 
				
				WHERE "SolicitudAporteRescate"."FechaAceptacion" = TO_DATE(:p_registroDate, 'YYYY-MM-DD') 
				AND (:p_IdFondo = '' OR "Fondo"."IdFondo" = :p_IdFondo) 
				AND ("SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOREINV','APODIV','APOOVER','RESCAM','RESNORM','RESREINV','RESTRANSF','RESTRASP','RESCOM','INGTIT','RETTIT','APORESPRO'))
				AND ("SolicitudAporteRescate"."Estado" = 'ING' OR "SolicitudAporteRescate"."Estado" = 'CRT') 
				AND (:p_IdCliente = 0 OR "SolicitudAporteRescate"."IdCliente" = :p_IdCliente) --Valida si es que viene valor en el filtro, solo si viene valor se aplica 
				AND (:p_IdSerie = '' OR "SolicitudAporteRescate"."IdSerieFondo" = :p_IdSerie); --Valida si es que viene valor en el filtro, solo si viene valor se aplica 
	    
		    v_datosGenerales = select 
	        "RazonSocial" AS "RazonCorredora", 
	        "RUT" AS "RutCorredora" 
	        from "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
	        WHERE "IdEmpresa" = :p_IdEmpresa;
	        
		    v_logo = select TOP 1
	        "Binario" AS "LogoCorredora"
	        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = :p_IdEmpresa ORDER BY "Version" DESC;
			
		    SELECT *
			FROM :v_resultado
			CROSS JOIN :v_logo
			CROSS JOIN :v_datosGenerales;
		END; 
	END;
END;
