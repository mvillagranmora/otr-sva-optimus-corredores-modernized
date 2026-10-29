SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos" (

	"ID",
	"IdDocumento",
	"FuenteDatos",
    "Estado",
    "EstadoSICAM",
    "SICAM",
    "SICAMDescripcion",
--camos layout    
	"TipoRegistro",
	"TipoRegistroDescripcion",
	"EventoReporte",
	"EventoReporteDescripcion",
	"TipoOperacion",
	"TipoOperacionDescripcion",
	"FechaNegociacion",
	"HoraNegociacion",
	"FechaValutaMonedaRecibida",
	"FechaValutaMonedaEntregada",
	"Identificador",
	"RUTContraparte1",
	"LEIContraparte1",
	"NombreContraparte1",
	"RUTContraparte2",
	"LEIContraparte2",
	"NombreContraparte2",
	"PaisContraparte2",
	"PaisContraparte2Descripcion",
	"RUTIntermediario",
	"PlataformaNegociacion",
	"PlataformaNegociacionDescripcion",
	"CodigoOperacionCambio",
	"CodigoOperacionCambioDescripcion",
	"PaisMonedaExtranjera",
	"PaisMonedaExtranjeraDescripcion",
	"MonedaCompradaRecibida",
	"MonedaCompradaRecibidaDescripcion",
	"MontoMonedaCompradaRecibida",
	"MonedaVendidaEntregada",
	"MonedaVendidaEntregadaDescripcion",
	"MontoMonedaVendidaEntregada",
	"ConvencionTipoCambio",
	"TipoCambio",
	"AfectaDerivados",
	"AfectaDerivadosDescripcion",
	"IdentificadorContratoDerivados",
	"FechaSuscripcionContrato",
	"HoraSuscripcionContrato",
	"OperacionEmpresaRelacionada",
	"OperacionEmpresaRelacionadaDescripcion",
	"NumeroInscripcionObligacionExterior",
	"NombreFinancistaAporante",
	"LEIFinancistaAporante",
	"NombreReceptorInversion",
	"LEIReceptorInversion",
	"ModalidadInversionCustodio",
	"ModalidadInversionCustodioDescripcion",
--campos de control interno especifico
    "FolioOperacion",
    "IdOperacion",
	"IdCartera",
	"FechaLiquidacion",
	"IdCliente",
	"IdTipoOperacion",
	"Nemo",
	"IdMonedaPrincipal",
	"IdMonedaSecundaria",
	"Observaciones",
	"MonedaPrimaria"


) AS


				
				SELECT 
					DAT."ID",
					DAT."IdDocumento",
					DAT."FuenteDatos",
				    DAT."Estado" ,
				    DAT."EstadoSICAM",
				    DAT."SICAM",
				    COALESCE(a."Descripcion",'') 			AS		"SICAMDescripcion",
					DAT."TipoRegistro",
					COALESCE(DTP."Descripcion",'') 			AS		"TipoRegistroDescripcion",
					DAT."EventoReporte",
					COALESCE(DER."Descripcion",'')			AS 		"EventoReporteDescripcion",
					DAT."TipoOperacion",
					COALESCE(DTO."Descripcion", '')			AS		"TipoOperacionDescripcion",
					DAT."FechaNegociacion",
					DAT."HoraNegociacion",
					DAT."FechaValutaMonedaRecibida",
					DAT."FechaValutaMonedaEntregada",
					DAT."Identificador",
					DAT."RUTContraparte1",
					DAT."LEIContraparte1",
					DAT."NombreContraparte1",
					DAT."RUTContraparte2",
					DAT."LEIContraparte2",
					DAT."NombreContraparte2",
					DAT."PaisContraparte2",
					COALESCE(DPC."Descripcion", '')			AS		"PaisContraparte2Descripcion",
					DAT."RUTIntermediario",
					DAT."PlataformaNegociacion",
					COALESCE(DPN."Descripcion",'')			AS 		"PlataformaNegociacionDescripcion",
					DAT."CodigoOperacionCambio",
					COALESCE(DCOP."Objetivo", '')			AS		"CodigoOperacionCambioDescripcion",
					DAT."PaisMonedaExtranjera",
					COALESCE(DPM."Descripcion", '')			AS		"PaisMonedaExtranjeraDescripcion",
					DAT."MonedaCompradaRecibida",
					COALESCE(c."Descripcion", '')			AS		"MonedaCompradaRecibidaDescripcion",
					DAT."MontoMonedaCompradaRecibida",
					DAT."MonedaVendidaEntregada",
					COALESCE(b."Descripcion", '')			AS		"MonedaVendidaEntregadaDescripcion",
					DAT."MontoMonedaVendidaEntregada",
					DAT."ConvencionTipoCambio",
					DAT."TipoCambio",
					DAT."AfectaDerivados",
					COALESCE(DAD."Descripcion", '')			AS		"AfectaDerivadosDescripcion",
					DAT."IdentificadorContratoDerivados",
					DAT."FechaSuscripcionContrato",
					DAT."HoraSuscripcionContrato",
					DAT."OperacionEmpresaRelacionada",
					COALESCE(DEM."Descripcion", '')			AS		"OperacionEmpresaRelacionadaDescripcion",
					DAT."NumeroInscripcionObligacionExterior",
					DAT."NombreFinancistaAporante",
					DAT."LEIFinancistaAporante",
					DAT."NombreReceptorInversion",
					DAT."LEIReceptorInversion",
					DAT."ModalidadInversionCustodio",
					COALESCE(DMC."Descripcion", '')			AS		"ModalidadInversionCustodioDescripcion",
					DAT."FolioOperacion",
				    DAT."IdOperacion",
					DAT."IdCartera",
					DAT."FechaLiquidacion",
					DAT."IdCliente",
					DAT."IdTipoOperacion",
					DAT."Nemo",
					DAT."IdMonedaPrincipal",
					DAT."IdMonedaSecundaria",
					COALESCE(VAL."CodigoValidacion", '')	AS 		"Observaciones",
					CASE DAT."TipoOperacion" 	 WHEN '01' THEN  DAT."MonedaCompradaRecibida"
			                    				 WHEN '03' THEN  DAT."MonedaCompradaRecibida"
			                    				 WHEN '02' THEN  DAT."MonedaVendidaEntregada"		 
			                    				 WHEN '04' THEN  DAT."MonedaVendidaEntregada" ELSE '' END AS "MonedaPrimaria"
					
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" DAT
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" a 	ON a."Tipo"		= 'REPO2P1RESULTADOWS' 				AND DAT."SICAM"							= a."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DTP 	ON DTP."Tipo"	= 'REPO2P1TIPOREGISTRO' 			AND DAT."TipoRegistro" 					= DTP."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DER 	ON DER."Tipo"	= 'REPO2P1EVENTOREPORTE'			AND DAT."EventoReporte" 				= DER."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DTO 	ON DTO."Tipo"	= 'REPO2P1TIPOOPERACION'			AND DAT."TipoOperacion" 				= DTO."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DCOP ON DCOP."Tipo"	= 'REPO2P1CODOPERACIONCAMBIO'		AND DAT."CodigoOperacionCambio" 		= DCOP."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DAD 	ON DAD."Tipo"	= 'REPO2P1AFECTADERIVADOS'			AND DAT."AfectaDerivados" 				= DAD."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DEM 	ON DEM."Tipo"	= 'REPO2P1OPEMPRESARELACIONADA'		AND DAT."OperacionEmpresaRelacionada"	= DEM."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DMC 	ON DMC."Tipo"	= 'REPO2P1INVERSIONCUSTODIO'		AND DAT."ModalidadInversionCustodio"	= DMC."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DPC 	ON DPC."Tipo"	= 'REPO2P1PAISISO'					AND DAT."PaisContraparte2"				= DPC."Codigo" 
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DPN 	ON DPN."Tipo"	= 'REPO2P1PLATAFORMANEGOCIACION'	AND DAT."PlataformaNegociacion" 		= DPN."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DPM 	ON DPM."Tipo"	= 'REPO2P1PAISMON'					AND DAT."PaisMonedaExtranjera" 			= DPM."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" c 	ON   c."Tipo"	= 'REPO2P1MONEDAISO'				AND DAT."MonedaCompradaRecibida" 		= c."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" b 	ON   b."Tipo"	= 'REPO2P1MONEDAISO'				AND DAT."MonedaVendidaEntregada" 		= b."Codigo"
				LEFT JOIN (
				  SELECT "CodigoValidacion", "IdDocumento" ,"ID"
				  FROM (
				  	SELECT 
				  			R."CodigoValidacion", DT."IdDocumento", DT."ID", 
							ROW_NUMBER() OVER (
								PARTITION BY DT."ID" 
								ORDER BY R."ID" ASC
							) AS rn
						FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
						INNER JOIN "BSCL.Normativa.BD::NOR.DatosREPO2P1" AS DT ON DT."IdDocumento" = R."IdDocumento"
						INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
					) AS t WHERE t.rn = 1
				) AS VAL ON  DAT."IdDocumento" = VAL."IdDocumento" AND DAT."ID" = VAL."ID"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos" TO bcs_desarrollador WITH GRANT OPTION;