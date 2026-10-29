SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarRegistroGravamenesProhibiciones";

CREATE PROCEDURE
--  Proposito: Crear reporte Registro de Gravamenes y Prohibiciones.
--  Autor: Gat-Blac 
--  Fecha Creacion: 09/03/2018
"BSCL.FondoMutuo.BD.Servicios::ConsultarRegistroGravamenesProhibiciones" (
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
				:v_HoraSistema AS "HoraGeneracionReporte", 
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
				"ConsultarCliente"."NombreCompleto" AS "NombreParticipe", 
				TO_CHAR("Garantia"."FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaSolicitud", 
				TO_TIME ("Garantia"."FechaIngresoRegistro") AS "HoraSolicitud", 
				'INGRESO DE GARANTIAS' AS "TipoSolicitud",
				"Garantia"."NombreConcepto" AS "MotivoRetencion",
				TO_CHAR("Garantia"."FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaCurse",
				TO_TIME ("Garantia"."FechaIngresoRegistro") AS "HoraCurse", 
				"Garantia"."Cantidad" AS "NumeroCuotas"  
					
				FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" AS "Garantia" 
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate" ON ("SolicitudAporteRescate"."IdSolicitudAporteRescate" = "Garantia"."FolioComprobanteOperacionOrigen") 
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" ) 				
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "ConsultarCliente" ON "ConsultarCliente"."IdCliente" = "Garantia"."IdCliente" 
				
				WHERE TO_DATE ("Garantia"."FechaOperacion") = TO_DATE(:p_registroDate, 'YYYY-MM-DD') --TODO: Validar si el filtro es FechaIngresoRegistro
				AND "Garantia"."IdTipoComprobanteOperacionOrigen" = 'FFMM' 
				AND (:p_IdFondo = '' OR "Fondo"."IdFondo" = :p_IdFondo) 
				AND ("SolicitudAporteRescate"."Estado" = 'ING' OR "SolicitudAporteRescate"."Estado" = 'CRT') 
				AND (:p_IdCliente = 0 OR "Garantia"."IdCliente" = :p_IdCliente) --Valida si es que viene valor en el filtro, solo si viene valor se aplica 
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
