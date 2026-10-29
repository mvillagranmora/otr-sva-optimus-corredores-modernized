SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarComprobante";

CREATE PROCEDURE
--  Proposito: Crear Comprobante Aporte Rescate.
--  Autor: Gat-Blac 
--  Fecha Creacion: 16/09/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarComprobante" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
	IN p_IdSolicitudAporteRescate  				BIGINT
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
			DECLARE v_SegundosZonaHoraria BIGINT;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.SegundosZonaHoraria', :v_SegundosZonaHoraria);
			
			
			
			v_resultado = 	SELECT "SolicitudAporteRescate"."IdSolicitudAporteRescate"
								, "SolicitudAporteRescate"."TipoSolicitud"
								, "SerieDeFondo"."IdSerieFondo"
								, "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo"
								, "SolicitudAporteRescate"."IndicadorMLT"
								, CASE "SolicitudAporteRescate"."IndicadorMLT" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsIndicadorMLTUI"
								, "SolicitudAporteRescate"."Overnight"
								, CASE "SolicitudAporteRescate"."Overnight" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsOvernightUI"
								, "SolicitudAporteRescate"."CalculoPorMontoCuota"
								, CASE "SolicitudAporteRescate"."CalculoPorMontoCuota" WHEN 'C' THEN 'UI_NUMERO_CUOTAS' WHEN 'T' THEN 'UI_TOTAL' ELSE 'UI_MONTO' END AS "CalculoPorMontoCuotaUI"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaEmision", 'DD-MM-YYYY') AS "FechaEmision"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaAceptacion", 'DD-MM-YYYY') AS "FechaAceptacion"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaCertificacion", 'DD-MM-YYYY') AS "FechaCertificacion"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaLiquidacion", 'DD-MM-YYYY') AS "FechaLiquidacion"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaAnulacion", 'DD-MM-YYYY') AS "FechaAnulacion"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaOriginalInversion", 'DD-MM-YYYY') AS "FechaOriginalInversion"
								,  TO_NVARCHAR(ADD_SECONDS("SolicitudAporteRescate"."FechaIngresoRegistro",:v_SegundosZonaHoraria), 'DD-MM-YYYY HH24:MI:SS') as "FechaIngresoRegistro"								   
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaAceptacionRegistro", 'DD-MM-YYYY') AS "FechaAceptacionRegistro"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaCertificacionRegistro", 'DD-MM-YYYY') AS "FechaCertificacionRegistro"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaLiquidacionRegistro", 'DD-MM-YYYY') AS "FechaLiquidacionRegistro"
								,  TO_VARCHAR("SolicitudAporteRescate"."FechaAnulacionRegistro", 'DD-MM-YYYY') AS "FechaAnulacionRegistro"
								,  TO_NVARCHAR(ADD_SECONDS("SolicitudAporteRescate"."FechaIngresoRegistro",:v_SegundosZonaHoraria), 'HH24:MI:SS') as "HoraIngresoRegistro"
								, "SolicitudAporteRescate"."Monto"
								, "SolicitudAporteRescate"."NumeroCuotas"
								, "SolicitudAporteRescate"."ValorCuota"
								, "SolicitudAporteRescate"."IdMoneda"
								, "SolicitudAporteRescate"."MontoPago"
								, "SolicitudAporteRescate"."IdEmpresa"
								, "SolicitudAporteRescate"."Estado" AS "EstadoSolicitud"
								, CASE WHEN "SolicitudAporteRescate"."IdMoneda" != "Fondo"."IdMoneda" THEN 'S' ELSE 'N' END AS "EsDiferido"
								, CASE WHEN "SolicitudAporteRescate"."IdMoneda" != "Fondo"."IdMoneda" THEN 'UI_SI' ELSE 'UI_NO' END AS "EsDiferidoUI"
								, "SerieDeFondo"."IdInstrumento"
								, "SerieDeFondo"."NombreSerieFondo"
								, "SerieDeFondo"."CodigoSerie"
								, "SolicitudAporteRescate"."IdRegimenTributario" AS "PropositoSerie"
								, "SerieDeFondo"."Alias"
								, "SerieDeFondo"."ExcluyeGarantia"
								, "SerieDeFondo"."IdSerieFondoOrigen"
								, "SerieDeFondo"."CobraComision"
								, "SerieDeFondo"."Diferido"
								, "SerieDeFondo"."DecimalesValorCuota"
								, "SerieDeFondo"."DecimalesCuota"
								, "Fondo"."IdFondo"
								, "Fondo"."IdEmisor"
								, "Fondo"."Nemotecnico"
								, "Fondo"."NombreFondo"
								, "Fondo"."IdTipoFondo"
								, "Fondo"."IdPaisOrigen"
								, "Fondo"."CodigoSVS"
								, "Fondo"."MoneyMarket"
								, "Fondo"."IdCategoriaPerfilFondoMutuo"
								,  TO_VARCHAR("Fondo"."FechaCreacion", 'DD-MM-YYYY') AS "FechaCreacion"
								,  TO_VARCHAR("Fondo"."FechaConstitucion", 'DD-MM-YYYY') AS "FechaConstitucion"
								,  TO_VARCHAR("Fondo"."FechaCierre", 'DD-MM-YYYY') AS "FechaCierre"
								, "Fondo"."Vigencia"
								, "Fondo"."IdTipoInstrumento"
								, "Fondo"."IdMoneda" AS "IdMonedaFondo"
								, "Portafolio"."CodigoPortafolio"
								, "Portafolio"."DescripcionPortafolio"
								, "ConsultarCliente"."RazonSocial"
								, "ConsultarCliente"."NombreCompleto"
								, "ConsultarCliente"."DocumentoIdentidad"
								, "ConsultarCliente"."IdPersona"
								, TO_VARCHAR("ConsultarCliente"."FechaNacimiento", 'DD-MM-YYYY') AS "FechaNacimiento"
								, "PortafolioDestino"."CodigoPortafolio" AS "CodigoPortafolioDestino"
								, "PortafolioDestino"."DescripcionPortafolio" AS "DescripcionPortafolioDestino"
								, "ConsultarClienteDestino"."RazonSocial" 	 AS "RazonSocialDestino"
								, "ConsultarClienteDestino"."NombreCompleto" AS "NombreCompletoDestino"
								, "ConsultarClienteDestino"."DocumentoIdentidad" AS "DocumentoIdentidadDestino"
								, "TipoFondo"."NombreCorto"
								, "TipoFondo"."Descripcion"
								, "Instrumento"."Nemotecnico" AS "Instrumento"
								, "TipoMovimientoFondoMutuo"."Descripcion" AS "DescripcionTipoMovimiento"
								, "Estado"."Nombre" AS "NombreEstado"
								, "ConsultarEmpleado"."NombreCompleto" AS "NombreEmpleado"
								, "MedioDePago"."Nombre" AS "TextoPreferenciaPago"
								, "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoFondoMutuoRelacionado"
								, "TipoMovimientoFondoMutuoRelacionado"."Descripcion" AS "DescripcionTipoMovimientoRelacionado"
								, "ConsultarDireccionFisica"."NombreLargo" AS "NombreLargoDireccion"
								, "ConsultarPersonaTelefono"."Numero" AS "NumeroTelefono"
								, "SolicitudAporteRescateRelacionado"."NumeroCuotas" AS "NumeroCuotasRelacionado"
								, TO_VARCHAR("SolicitudAporteRescateRelacionado"."FechaLiquidacion", 'DD-MM-YYYY') AS "FechaLiquidacionRelacionado"
								, "SerieDeFondoRelacionado"."IdSerieFondo" AS "IdSerieFondoRelacionado"
								, "SerieDeFondoRelacionado"."CodigoSerie" AS "CodigoSerieRelacionado"
								, "FondoRelacionado"."IdEmisor" AS "IdEmisorRelacionado"
								, COALESCE("SolicitudAporteRescateRelacionado"."IdMoneda","SolicitudAporteRescate"."IdMoneda") AS "IdMonedaRelacionado"
								, COALESCE("SolicitudAporteRescateRelacionado"."Monto","SolicitudAporteRescate"."Monto") AS "MontoRelacionado"
								, "SerieDeFondoReinversion"."CodigoSerie" AS "CodigoSerieReinversion"
								, CASE "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" WHEN 'APOREINV' THEN COALESCE("SerieDeFondoReinversion"."IdSerieFondo","SolicitudAporteRescate"."IdSerieFondoOrigen") ELSE "SerieDeFondoReinversion"."IdSerieFondo" END AS "IdSerieFondoReinversion"
								, CASE "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" WHEN 'APOREINV' THEN COALESCE("FondoReinversion"."IdEmisor", "SolicitudAporteRescate"."IdEmisorOrigen") ELSE "FondoReinversion"."IdEmisor" END AS "IdEmisorReinversion"
								, "SolicitudAporteRescate"."MontoOrigen"
								, "SolicitudAporteRescate"."CostoUF"
								, "SerieDeFondoDestino"."IdSerieFondo" AS "IdSerieFondoDestino"
								, "SerieDeFondoDestino"."NombreSerieFondo" AS "NombreSerieFondoDestino"
								, "ConsultarPersonaEjecutivo"."NombreCompleto" AS "Agente"
								, "ConsultarUsuario"."NombreCompleto" AS "AtendidoPor"
								, "SolicitudAporteRescate"."IdRegimenTributario"
								FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )
								LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "Portafolio" ON  ( "Portafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio" )
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "Portafolio"."IdCliente" )
								LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PortafolioDestino" ON  ( "PortafolioDestino"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolioDestino" )
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarClienteDestino" ON  ( "ConsultarClienteDestino"."IdCliente" = "PortafolioDestino"."IdCliente" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo") 
								LEFT OUTER  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "SerieDeFondo"."IdInstrumento")
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo")
								LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "ConsultarEmpleado" ON ("ConsultarEmpleado"."IdEmpleado" = "SolicitudAporteRescate"."IdUsuario")
								LEFT OUTER  JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "SolicitudAporteRescate"."IdMedioDePago")
								LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PortafolioAGF" ON ("PortafolioAGF"."IdPortafolio" = "SerieDeFondo"."IdPortafolio")
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "ClienteAGF" ON ("SerieDeFondo"."IdCliente" = "ClienteAGF"."IdCliente")
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescateRelacionado" ON ("SolicitudAporteRescateRelacionado"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado")
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuoRelacionado" ON ("TipoMovimientoFondoMutuoRelacionado"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo")
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "ConsultarDireccionFisica" ON  ( "ConsultarCliente"."IdDireccionFisicaPrincipal" = "ConsultarDireccionFisica"."IdDireccionFisica" )
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS "ConsultarPersonaTelefono" ON  ( "ConsultarCliente"."IdTelefonoPrincipal" = "ConsultarPersonaTelefono"."IdTelefono" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondoRelacionado" ON ("SerieDeFondoRelacionado"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondoDestino" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FondoRelacionado" ON ("FondoRelacionado"."IdFondo" = "SerieDeFondoRelacionado"."IdFondo" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondoReinversion" ON ("SerieDeFondoReinversion"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondoOrigen" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FondoReinversion" ON ("FondoReinversion"."IdFondo" = "SerieDeFondoReinversion"."IdFondo" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondoDestino" ON ("SerieDeFondoDestino"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondoDestino" )
								LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FondoDestino" ON ("FondoDestino"."IdFondo" = "SerieDeFondoDestino"."IdFondo" )
								LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "ConsultarPersonaEjecutivo" ON ( "ConsultarPersonaEjecutivo"."IdPersona" = "ConsultarCliente"."IdPersonaEjecutivo")
								LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS "ConsultarUsuario" ON ("ConsultarUsuario"."IdUsuario" = "SolicitudAporteRescate"."IdUsuario")
								WHERE "SolicitudAporteRescate"."IdSolicitudAporteRescate" = :p_IdSolicitudAporteRescate;
	    
	    v_datosGenerales = select 
        "RazonSocial" AS "RazonCorredora", 
        "Direccion" AS "DireccionCorredora", 
        "RUT" AS "RutCorredora", 
        "Telefono" AS "TelefonoCorredora" 
        from "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
        WHERE "IdEmpresa" = p_IdEmpresa;
        
	    v_logo = select TOP 1
        "Binario" AS "LogoCorredora"
        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		
	    SELECT *
		FROM :v_resultado
		CROSS JOIN :v_logo
		CROSS JOIN :v_datosGenerales;
		END; 
	END;
END;
