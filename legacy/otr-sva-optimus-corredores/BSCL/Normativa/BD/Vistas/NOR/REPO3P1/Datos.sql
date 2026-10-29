SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos" (

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
    "RUTReportante",
    "FechaOperacion",
    "HoraOperacion",
    "TipoOperacion",
	"TipoOperacionDescripcion",
	"EventoReporte",
	"EventoReporteDescripcion",
	"Identificador",
	"RUTContraparte",
	"LEIContraparte",
	"NombreContraparte",
	"PlataformaNegociacion",
	"PlataformaNegociacionDescripcion",
	"MontoMonedaComprada",
	"MontoMonedaVendida",
	"TipoCambio",
--campos de control interno especifico
	"IdCliente",
	"IdOperacion",
	"IdTipoOperacion",
	"Nemo",
	"IdMonedaPrincipal",
	"IdMonedaSecundaria",
	"Observaciones"

) AS


				
				SELECT 
					DAT."ID",
					DAT."IdDocumento",
					DAT."FuenteDatos",
				    CASE WHEN TO_DATE(DAT."FechaOperacion") =TO_DATE(FRE."FechaReporte") THEN DAT."Estado" ELSE 'INA' END AS "Estado" ,
				    DAT."EstadoSICAM",
				    DAT."SICAM",
				    COALESCE(a."Descripcion",'') 				AS	"SICAMDescripcion",
					DAT."TipoRegistro",
					COALESCE(DTP."Descripcion",'') 			AS		"TipoRegistroDescripcion",
				    DAT."RUTReportante",
				    DAT."FechaOperacion",
				    DAT."HoraOperacion",
					DAT."TipoOperacion",
					COALESCE(DTO."Descripcion", '')			AS		"TipoOperacionDescripcion",
					DAT."EventoReporte",
					COALESCE(DER."Descripcion",'')			AS 		"EventoReporteDescripcion",
					DAT."Identificador",
					DAT."RUTContraparte",
					DAT."LEIContraparte",
					DAT."NombreContraparte",
					DAT."PlataformaNegociacion",
					COALESCE(DPN."Descripcion",'')			AS 		"PlataformaNegociacionDescripcion",
					DAT."MontoMonedaComprada",
					DAT."MontoMonedaVendida",
					DAT."TipoCambio",
					DAT."IdCliente",
					DAT."IdOperacion",
					DAT."IdTipoOperacion",
					DAT."Nemo",
					DAT."IdMonedaPrincipal",
					DAT."IdMonedaSecundaria",
					COALESCE(VAL."CodigoValidacion", '')	AS 		"Observaciones"

				FROM "BSCL.Normativa.BD::NOR.DatosREPO3P1" DAT
				LEFT JOIN "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte" 	AS FRE	 ON (TO_DATE(DAT."FechaOperacion") =TO_DATE(FRE."FechaReporte"))
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" a 	ON a."Tipo"		= 'REPO3P1RESULTADOWS' 				AND DAT."SICAM"							= a."Codigo" 
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DTP 	ON DTP."Tipo"	= 'REPO3P1TIPOREGISTRO' 			AND DAT."TipoRegistro" 					= DTP."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DER 	ON DER."Tipo"	= 'REPO3P1EVENTOREPORTE'			AND DAT."EventoReporte" 				= DER."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DTO 	ON DTO."Tipo"	= 'REPO3P1TIPOOPERACION'			AND DAT."TipoOperacion" 				= DTO."Codigo"
				LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" DPN 	ON DPN."Tipo"	= 'REPO3P1PLATAFORMANEGOCIACION'	AND DAT."PlataformaNegociacion" 		= DPN."Codigo"
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
						INNER JOIN "BSCL.Normativa.BD::NOR.DatosREPO3P1" AS DT ON DT."IdDocumento" = R."IdDocumento"
						INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
					) AS t WHERE t.rn = 1
				) AS VAL ON  DAT."IdDocumento" = VAL."IdDocumento" AND DAT."ID" = VAL."ID"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos" TO bcs_desarrollador WITH GRANT OPTION;