SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.APV.BD.Servicios::ConfirmarAutorizacionIngresoContratoSAAPV";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la orden.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-06-2016
--  
"BSCL.APV.BD.Servicios::ConfirmarAutorizacionIngresoContratoSAAPV" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
		  
	IN 	p_Contratos						TABLE ("FolioContratoSAAPV"	BIGINT)	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_FechaOperacionSecondate SECONDDATE;
	DECLARE v_countContratos BIGINT;
	--DECLARE v_ContratosAnterior TABLE ("IdContratoSAAPV"	NVARCHAR(50))DEFAULT SELECT '' as "IdContratoSAAPV" FROM DUMMY WHERE 1=2 ;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);
	
	v_Contratos = SELECT row_number() over (order by "FolioContratoSAAPV") AS "IdInterno","FolioContratoSAAPV" FROM :p_Contratos;
	CALL "BSCL.APV.BD.Servicios::CambiarEstadoContratoSAAAPV" (
 		p_Contratos  			=> :v_Contratos,
    	p_NuevoEstado  		    => 'VIG',
    	p_NuevoEstadoSolicitud 	=> 'APR'
	);
	
	v_ContratosAnterior =
	SELECT "IdInterno","Folio" AS "FolioContratoSAAPV" FROM "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV
	INNER JOIN :v_Contratos Con on SAAPV."FolioContratoSAAPV" = TO_NVARCHAR(Con."FolioContratoSAAPV")
	WHERE SAAPV."Folio" != '';
	
    SELECT count("IdInterno")INTO v_countContratos FROM :v_ContratosAnterior;
    
    IF v_countContratos > 0 THEN
		CALL "BSCL.APV.BD.Servicios::CambiarEstadoContratoSAAAPV" (
	 		p_Contratos  			=> :v_ContratosAnterior,
	    	p_NuevoEstado  		    => 'ANU',
	    	p_NuevoEstadoSolicitud 	=> ''
		);
    
    END IF;
	
	/*
	p_DetalleLog = SELECT p_ord."IdOrden" AS "IdCabeceraLog",'' as "SubRegistro", 'IdEstadoOrden' as "CampoRegistro", ord."IdEstadoOrden" as "ValorAntiguo", 'ING' as "ValorNuevo" 
					FROM :p_Ordenes p_ord
					INNER JOIN :v_out_SalidaResumen res on p_ord."IdOrden" = res."IdOperacion" and "Resultado" = 'OK'
					INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on p_ord."IdOrden" = ord."IdOrden";
	
	v_log = SELECT	p_ord."IdOrden" as "IdCabeceraLog",
						'INGRESO' AS "IdTipoLog",
						'ORD' as "IdModulo",
				 		:p_IdUsuario as "IdUsuario",
				 		:p_IdEmpresa as 	"IdEmpresa",
				 		TO_NVARCHAR("FechaOperacion", 'YYYY-MM-DD') as	"FechaSistemaLog",
				 		:p_IdUsuario as "IdUsuarioAtendido",
				 		cli."IdCliente",
						cli."IdEstado" as "EstadoCliente",
						"NombreCompleto" as "NombreCliente",
						"TipoDocumentoIdentidad" as "IdTipoDocumentoIdentidad",
						"DocumentoIdentidad" as "IdDocumentoIdentidad",
						"IdPortafolioCliente" as "IdPortafolio",
						port."IdEstado" as "EstadoPortafolio",
						'ORDEN' as "IdTipoRegistro", 
						p_ord."IdOrden" as "IdRegistro",
						"IdEstadoOrden" as "EstadoRegistro",
						"IdTipoOperacion",
						p_ord."IdOrden" as "Folio",
						"Cantidad",
						"PrecioOrden" as "PrecioTasa",
						"MontoOrden" as "Monto",
						"IdMonedaOrden" as "IdMonedaOrigen",
						"IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",
						"TipoCambio",
						"Nemotecnico" as "Nemo",
						0 as "IdProceso",
						'' as "EstadoProceso",
						'' as "NombreProceso"
					FROM :p_Ordenes p_ord
					INNER JOIN :v_out_SalidaResumen res on p_ord."IdOrden" = res."IdOperacion" and "Resultado" = 'OK'
					INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on p_ord."IdOrden" = ord."IdOrden"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = ord."IdCliente"
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on port."IdPortafolio" = ord."IdPortafolioCliente";

	CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :p_DetalleLog);
	*/
END;