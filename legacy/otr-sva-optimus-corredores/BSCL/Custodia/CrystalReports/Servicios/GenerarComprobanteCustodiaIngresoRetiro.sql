SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::GenerarComprobanteCustodiaIngresoRetiro"; 
GO
CREATE PROCEDURE
--
--  Proposito: Servicio para generar Comprobante de ingreso retiro titulos custodia.
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 09/02/2017

-- ************************************************************************************************************************************
-- Para poder probar el rpt en Crystal Report, comentar las líneas que llamen a la funcion BSCL.Plataforma.BD.Utilitarios::CamelCase
-- ************************************************************************************************************************************
"BSCL.Custodia.CrystalReports.Servicios::GenerarComprobanteCustodiaIngresoRetiro" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
    IN p_IdCliente							BIGINT,
	IN p_IdPortafolio						BIGINT,
	IN p_IdMovimiento						BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_cantidad BIGINT;
		DECLARE v_moneda NVARCHAR(5);
		DECLARE v_cantidad_texto NVARCHAR(1000);
		
		v_MovimientoCustodia = SELECT TOP 1
										M."IdMovimientoCarteraEnTransito",
										M."NombrePersona",
										M."DocumentoIdentidad",
										M."IdClasificacionTipoInstrumento" AS "Mercado",
										M."CodigoPortafolio",
										CASE WHEN M."IdClasificacionTipoInstrumento"='IF' THEN M."ValorRescate" ELSE M."Cantidad" END AS "Cantidad",
										TO_NVARCHAR(M."FechaIngresoRegistro",'DD-MM-YYYY HH24:MI:SS') AS "FechaIngresoRegistro",
										0 AS "IdCartera",
										M."PrecioCompra",
										CASE WHEN M."IdClasificacionTipoInstrumento"='IF' THEN M."NemoSVS" ELSE M."Nemotecnico" END AS "Nemotecnico",
										M."IdEmisor",
										M."NombreEmisor",
										COALESCE (M."NombreBoveda" ,'DCV') AS "NombreBoveda",
										TO_NVARCHAR(M."FechaOperacion",'DD-MM-YYYY HH:MM') AS "FechaOperacion",
										TO_NVARCHAR(M."FechaVencimientoInstrumento",'DD-MM-YYYY') AS "FechaVencimientoInstrumento",
										M."IdConcepto",
										M."NombreConcepto",
										COALESCE(C."GeneraTransferenciaBoveda",'N') AS "GeneraTransferenciaBoveda",
										COALESCE(C."EsTransferenciaPortafolio",'N') AS "EsTransferenciaPortafolio",
										COALESCE(C."EsTransferenciaCliente",'N') AS "EsTransferenciaCliente",
										IFNULL(CONT."NombreCompleto",'') AS "NombreEntidadDestino",
										M."IndicadorIngEgrAbr",
										M."Glosa",
										CPDF."NombreLargo" AS "DireccionFisica",
										CPT."Anexo" || ' ' || CPT."Numero" AS "Telefono",
										M."IdAgente",
										CE1."NombreCompleto" AS "Agente",
										M."IdUsuario",
										CE2."NombreCompleto" AS "AtendidoPor",
										M."IdMonedaInstrumento",
										M."MontoCompraMonedaInstrumento", M."IdCliente",M."IdPortafolio"
									   ,'Comprobante ' ||
									    CASE WHEN conf."IdAccionOperacion" = 'ING' THEN 'Ingreso de Títulos ' 
									   		 WHEN conf."IdAccionOperacion" = 'RET' AND C."GeneraTransferenciaBoveda"<>'S' AND C."EsTransferenciaPortafolio"<>'S' AND C."EsTransferenciaCliente"<>'S'  THEN 'Egreso de Títulos '
									   		 WHEN conf."IdAccionOperacion" = 'RET' AND (C."GeneraTransferenciaBoveda"='S' OR C."EsTransferenciaPortafolio"='S' OR C."EsTransferenciaCliente"='S')  THEN 'Traspaso de Títulos '
									   		 ELSE '' 
									   	END 
									   	|| LTRIM("BSCL.Plataforma.BD.Utilitarios::CamelCase"(mercado."Descripcion"))
									   	|| CASE WHEN M."RazonSocialContraparteGarantia" <> '' THEN ' en Garantía ' ELSE '' END 
									   	AS "Titulo"
									   ,IFNULL(formato."CantidadDecimales",CASE M."IdMonedaInstrumento" WHEN 'CLP' THEN 0 ELSE 2 END) AS "CantidadDecimales" 
									   ,IFNULL(formato."PrecioDecimales",CASE M."IdMonedaInstrumento" WHEN 'CLP' THEN 0 ELSE 2 END) AS "PrecioDecimales"
									   ,IFNULL(formato."TasaDecimales",CASE M."IdMonedaInstrumento" WHEN 'CLP' THEN 0 ELSE 2 END) AS "TasaDecimales"
									   ,IFNULL(formato."MontoDecimales",CASE M."IdMonedaInstrumento" WHEN 'CLP' THEN 0 ELSE 2 END) AS "MontoDecimales"
									   ,M."IdTipoOperacion"
									   ,mda."Simbolo"
									   ,IFNULL(cli2."NombreCompleto",'') AS "NombreClienteDestino"
									   ,IFNULL(cli2."DocumentoIdentidad",'') AS "RutClienteDestino"
									   ,IFNULL(otroMov."IdBoveda",'') AS "IdBovedaDestino"
									   ,IFNULL(por2."CodigoPortafolio",'') AS "CodigoPortafolioDestino"
									   ,IFNULL(bov2."Nombre",'') AS "BovedaDestino"
									   ,M."Accion"
									   

		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" M
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia" C ON M."IdConcepto" = C."IdConcepto" AND C."NombreEntidad" <> 'CUS.MOVIMIENTOCUSTODIA'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" CONT ON C."IdContraparte" = CONT."IdContraparte" AND CONT."IdEstado" = 'ACT'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPDF ON CPDF."IdPersona" =  M."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona" =  M."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U1 ON U1."IdUsuario" = M."IdAgente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE1 ON U1."IdEntidad" = CE1."IdEmpleado" AND U1."TipoEntidad" = 'EMP'
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U2 ON U2."IdUsuario" = M."IdUsuario"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON U2."IdEntidad" = CE2."IdEmpleado" AND U2."TipoEntidad" = 'EMP'
		LEFT JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS conf ON M."IdTipoOperacion" = conf."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS formato ON formato."IdMoneda" = M."IdMonedaInstrumento" AND formato."IdUsoMonedaFormato" = 'GEN' AND formato."IdTipoInstrumento" = M."IdTipoInstrumento" AND IFNULL(formato."IdTipoOperacion",'') = ''
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS mercado ON mercado."IdClasificacionTipoInstrumento" = M."IdClasificacionTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS mda ON mda."IdMoneda" = M."IdMonedaInstrumento"
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS otroMov ON otroMov."IdMovimientoCarteraEnTransitoRelacionado" = M."IdMovimientoCarteraEnTransito" AND otroMov."IdEmpresa" = M."IdEmpresa"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cli2 ON cli2."IdCliente" = otroMov."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS por2 ON por2."IdPortafolio" = otroMov."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS bov2 ON bov2."IdBoveda" = otroMov."IdBoveda"
		WHERE M."IdCliente" = :p_IdCliente AND M."IdPortafolio" = :p_IdPortafolio AND M."IdMovimientoCarteraEnTransito" = :p_IdMovimiento;
		

		--Obtener cantidad en texto.
		SELECT "Cantidad", "IdMonedaInstrumento" INTO v_cantidad, v_moneda FROM :v_MovimientoCustodia;
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(v_cantidad,'CL', v_moneda, :v_cantidad_texto);
		v_cantidad_t = select ('(' || "BSCL.Plataforma.BD.Utilitarios::CamelCase"(v_cantidad_texto) || ')') AS "CantidadTexto" FROM DUMMY;
		--v_cantidad_t = select ('(' || v_cantidad_texto || ')') AS "CantidadTexto" FROM DUMMY;

		
		--Obtener datos generales y logo.
		v_DatosGenerales = SELECT
		"RazonSocial" AS "RazonCorredora", 
		"Direccion" AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora" 
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		v_Logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		--------------------
		--Lógica de Negocio 
		--------------------
		SELECT * FROM :v_MovimientoCustodia
		CROSS JOIN :v_cantidad_t
		CROSS JOIN :v_DatosGenerales
		CROSS JOIN :v_logo;

		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO