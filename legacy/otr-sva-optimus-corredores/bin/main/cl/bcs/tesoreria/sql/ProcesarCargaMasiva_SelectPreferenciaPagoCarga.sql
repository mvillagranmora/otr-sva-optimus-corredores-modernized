
-- Toma los parametros de entrada(preferencias de pago editada en complemento). 
-- Si estos vienen en null, toma la preferencia de pago configurada por carga

SELECT
	PROC."IdCargaMasiva",
	COALESCE(PPC."CodigoTipoCargaMasiva",'') AS "CodigoTipoCargaMasiva",
	COALESCE(PPC."IdPersonaEntidadRecaudadora",0) AS "IdPersonaEntidadRecaudadora",			
	COALESCE(PER."NombreCompleto",'') AS "NombreCompletoEntidadRecaudadora",
	COALESCE(PER."DocumentoIdentidad",'') AS "DocumentoIdentidadEntidadRecaudadora",
	COALESCE(PER."TipoDocumentoIdentidad",'') AS "TipoDocumentoIdentidadEntidadRecaudadora",
	COALESCE(PROC."IdMoneda",PPC."IdMoneda",'') AS "IdMoneda",
	COALESCE(PROC."IdConcepto",PPC."IdConcepto",'') AS "IdConcepto",
	COALESCE(PROC."IdMedioDePago",PPC."IdMedioDePago",'') AS "IdMedioDePago",
	COALESCE(MODPROC."IdModalidadDePago",MODPPC."IdModalidadDePago",'') AS "IdModalidadDePago",	
	COALESCE(PROC."IdCuentaBanco",PPC."IdCuentaBanco",0) AS "IdCuentaBanco",
	COALESCE(PPC."ProfundidadProceso",'') AS "ProfundidadProceso",
	TCM."GeneraPartida"    -- indicador si se genera partida de ingreso/egreso
			
FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC
INNER JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = PROC."CodigoTipoCargaMasiva")
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS PPC ON (
				    PPC."CodigoTipoCargaMasiva" = PROC."CodigoTipoCargaMasiva"						    
				AND PPC."NombreEntidad" 	= 'TES.CargaMasiva'
				AND PPC."IdEstado" 			= 'ACT'				
				AND PPC."IdEmpresa" 		= :IdEmpresa)						
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = PPC."IdPersonaEntidadRecaudadora")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MODPROC ON (MODPROC."IdMedioDePago" = PROC."IdMedioDePago")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MODPPC  ON (MODPPC."IdMedioDePago" = PPC."IdMedioDePago")
WHERE PROC."IdCargaMasiva" IN (:IdCargaMasiva);
