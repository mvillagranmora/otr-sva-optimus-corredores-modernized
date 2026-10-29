SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ObtenerDatosInformeRentabilidadFFMMExtranjera";	
CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ObtenerDatosInformeRentabilidadFFMMExtranjera"
(
	IN p_IdEmpresa 		BIGINT,
	IN p_IdTrabajo 		BIGINT,
	IN p_EsMasivo 		NVARCHAR(1),
	IN p_AgnoComercial 	NVARCHAR(4),
	
	--Parametros Salida
	OUT p_salida TABLE(
		"IdCliente"					BIGINT,
		"IdInstrumento"				BIGINT,
		"IdRegimenTributario"		NVARCHAR(40),
		"NombreCompleto"			NVARCHAR(302),
		"Nombres"					NVARCHAR(100),
		"ApellidoPaterno"			NVARCHAR(100),
		"ApellidoMaterno"			NVARCHAR(100),
		"RazonSocial"				NVARCHAR(100),
		"DocumentoIdentidadCliente"	NVARCHAR(40),
		"IdDocumentoIdentidad"		BIGINT,
		"NombreFondo"				NVARCHAR(100),
		"CodigoSerie"				NVARCHAR(10),
		"CodigoPais"				NVARCHAR(40),
		"AgnoComercial"				NVARCHAR(4),
		"NombreEmisor"				NVARCHAR(302),
		"TotalRescatadoEnero"		BIGINT,
		"TotalRescatadoFebrero"		BIGINT,
		"TotalRescatadoMarzo"		BIGINT,
		"TotalRescatadoAbril"		BIGINT,
		"TotalRescatadoMayo"		BIGINT,
		"TotalRescatadoJunio"		BIGINT,
		"TotalRescatadoJulio"		BIGINT,
		"TotalRescatadoAgosto"		BIGINT,
		"TotalRescatadoSeptiembre"	BIGINT,
		"TotalRescatadoOctubre"		BIGINT,
		"TotalRescatadoNoviembre"	BIGINT,
		"TotalRescatadoDiciembre"	BIGINT,
		"TotalAportadoEnero"		BIGINT,
		"TotalAportadoFebrero"		BIGINT,
		"TotalAportadoMarzo"		BIGINT,
		"TotalAportadoAbril"		BIGINT,
		"TotalAportadoMayo"			BIGINT,
		"TotalAportadoJunio"		BIGINT,
		"TotalAportadoJulio"		BIGINT,
		"TotalAportadoAgosto"		BIGINT,
		"TotalAportadoSeptiembre"	BIGINT,
		"TotalAportadoOctubre"		BIGINT,
		"TotalAportadoNoviembre"	BIGINT,
		"TotalAportadoDiciembre"	BIGINT,
		"DiferenciaEnero"			BIGINT,
		"DiferenciaFebrero"			BIGINT,
		"DiferenciaMarzo"			BIGINT,
		"DiferenciaAbril"			BIGINT,
		"DiferenciaMayo"			BIGINT,
		"DiferenciaJunio"			BIGINT,
		"DiferenciaJulio"			BIGINT,
		"DiferenciaAgosto"			BIGINT,
		"DiferenciaSeptiembre"		BIGINT,
		"DiferenciaOctubre"			BIGINT,
		"DiferenciaNoviembre"		BIGINT,
		"DiferenciaDiciembre"		BIGINT,
		"LogoCorredora"					BLOB,
	  	"FechaSistema"					NVARCHAR(10),
	  	"DocumentoIdentidadCorredora"	NVARCHAR(40),
	  	"RazonSocialCorredora"			NVARCHAR(100),
	  	"CiudadCorredora" 				NVARCHAR(100),
	  	"IdTemplate"  					VARCHAR(50),
		"DJProcesada"					NVARCHAR(1)
	)
	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN
		
		BEGIN
			DECLARE v_logo TABLE("LogoCorredora" BLOB);
			DECLARE v_cliente TABLE (
								  "IdCliente" 			BIGINT
								, "NombreCompleto" 		NVARCHAR(100)
								, "RutCliente" 			NVARCHAR(20)
								, "EGRIdOperacion" 		BIGINT);
			
			DECLARE v_DatosCorredora TABLE( "IdPersona" 			BIGINT,
										"DocumentoIdentidad" 	NVARCHAR(20),
										"RazonSocial" 			NVARCHAR(100),
										"DireccionFisica" 		NVARCHAR(200),
										"Telefono" 				NVARCHAR(40),
										"CiudadCorredora"		NVARCHAR(100)
										); 
			
			DECLARE v_validacionDJ NVARCHAR(1);
			
			v_logo = 	SELECT TOP 1 "Binario" AS "LogoCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'LOGO_CORREDOR' 
						AND "IdEmpresa" = :p_IdEmpresa 
						ORDER BY "Version" DESC;
					
			v_DatosCorredora = SELECT Persona."IdPersona"
							     , Persona."DocumentoIdentidad"
							     , Persona."RazonSocial"
							     , COALESCE(DireccionFisica."Calle",'')	|| ' ' || COALESCE(DireccionFisica."Numero",'')				AS "DireccionFisica"
							     , COALESCE(Telefono."Numero",'') 						AS "Telefono"
							     , COALESCE(DireccionFisica."NombreCiudad", '') AS "CiudadCorredora"
							FROM "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa
							INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocioNegocio
									ON SocioNegocio."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Persona
									ON Persona."IdPersona" = SocioNegocio."IdPersona"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DireccionFisica 
								   ON DireccionFisica."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS Telefono 
							       ON Telefono."IdTelefono" = Persona."IdTelefonoPrincipal"; 
		  				
                    SELECT (CASE WHEN COALESCE(val."CodigoValidacion", 'N') = 'OK' THEN 'S' ELSE 'N' END) INTO v_validacionDJ
                    FROM (
                                  SELECT DISTINCT "Esquema", "Version"
                                  FROM "BSCL.Normativa.BD::DOC.Validacion"
                                  WHERE "Esquema"= 'DJ1932'
                                  UNION ALL
                                  SELECT "Esquema", max("Version") +1 AS "Version" FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"= 'DJ1932' GROUP BY "Esquema"
                           ORDER BY "Version"
                           )ver
                    LEFT JOIN "BSCL.Normativa.BD::DOC.Documento" AS doc ON doc."Esquema"=ver."Esquema" AND doc."Version"= ver."Version"
                    LEFT JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" AS val ON val."IdDocumento" = doc."IdDocumento"
                    WHERE ver."Esquema"= 'DJ1932' AND ver."Version" =  :p_AgnoComercial;


						
			v_resultados = 	WITH 
									CTE_INVERSION ("IdCliente", "IdMes", "IdInstrumento", "IdRegimenTributario", "IdSolicitudAporteRescate","TotalRescatado", "Diferencia", "NombreFondo" , "CodigoSerie","CodigoPais","IdEmisor") AS (
									SELECT
										  solRescate."IdCliente"
										, MONTH(solRescate."FechaValorCuota") AS "IdMes"
										, car."IdInstrumento" AS "IdInstrumento"
										, solRescate."IdRegimenTributario"
										, solRescate."IdSolicitudAporteRescate"
										, SUM(solRescate."MontoMonedaSistema") AS "TotalRescatado"
										, TO_BIGINT(sum(coalesce (ROUND((((((car."EGRMonto" ))/ uf_movRes."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_movRes."Valor" ),0) ,0))) AS "Diferencia"
										, Fondo."NombreFondo" AS "NombreFondo"
										, SerieFondo."CodigoSerie" AS "CodigoSerie"
										, Pais."CodigoPais" AS "CodigoPais"
										, car."IdEmisor" AS "IdEmisor"
									FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS solRescate
									INNER JOIN ( SELECT "EGRIdOperacion", SUM("EGRMonto") AS "EGRMonto" ,SUM("EGRCostoInversionEnFactorAjuste") AS "EGRCostoInversionEnFactorAjuste" , "IdInstrumento","IdEmisor"
												 FROM "BSCL.Custodia.BD::CUS.Cartera"
												 GROUP BY "EGRIdOperacion", "IdInstrumento","IdEmisor" ) AS car ON car."EGRIdOperacion" = solRescate."IdSolicitudAporteRescate" --AND car."IdClasificacionTipoInstrumento" = 'FM' AND car."IdEstado" IN ('LIQ','MOD','RPA')
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS solAporte ON solAporte."IdSolicitudAporteRescate" = car."EGRIdOperacion"
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SerieFondo ON (SerieFondo."IdInstrumento" = car."IdInstrumento")
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" Fondo ON (Fondo."IdFondo" = SerieFondo."IdFondo")
									LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV ON ( (SUBSTRING(ADD_MONTHS(TO_DATE(solRescate."FechaValorCuota", 'YYYY-MM-DD'), -1), 0, 7) = IPCINV."AgnoMes") AND IPCINV."IdParametroFinanciero" = 'IPC' )
									LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT ON ( (CONCAT(:p_AgnoComercial ,'-11') = IPCACT."AgnoMes") AND IPCACT."IdParametroFinanciero" = 'IPC' )
									INNER JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais ON Pais."IdPais" = Fondo."IdPaisEmisor"
									LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_movRes ON (uf_movRes."Fecha"= solRescate."FechaValorCuota" )  AND uf_movRes."IdParametroFinanciero" = 'UF'
									WHERE
									YEAR(solRescate."FechaValorCuota") = :p_AgnoComercial
									AND solRescate."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV','RESCDF')
									AND solRescate."IdRegimenTributario" IN ('18Q','18T')
									AND solRescate."Estado" = 'CRT'
									AND Fondo."FondoExtranjero" ='S'
									AND Fondo."IdMoneda" !='CLP'
									GROUP BY solRescate."IdCliente", car."IdInstrumento", solRescate."IdRegimenTributario", MONTH(solRescate."FechaValorCuota"), solRescate."IdSolicitudAporteRescate", IPCACT."Valor", IPCINV."Valor", solRescate."FechaValorCuota", Fondo."NombreFondo", SerieFondo."CodigoSerie", Pais."CodigoPais", car."IdEmisor", car."EGRMonto", car."EGRCostoInversionEnFactorAjuste", uf_movRes."Valor"
									)
									, CTE_RESUMEN_MENSUAL_CUSTODIA ("IdCliente", "IdInstrumento", "IdRegimenTributario", "IdMes", "TotalRescatado", "Diferencia" , "TotalAportado", "NombreFondo", "CodigoSerie","CodigoPais","IdEmisor") AS (
									SELECT
										  Inversion."IdCliente"
										, Inversion."IdInstrumento"
										, Inversion."IdRegimenTributario"
										, Inversion."IdMes"
										, Inversion."TotalRescatado"
										, SUM(TO_BIGINT(Inversion."Diferencia")) AS "Diferencia"
										, ROUND( (SUM(Rescate."MontoMonedaSistema") - Inversion."Diferencia") , 0 ) AS "TotalAportado"
										, Inversion."NombreFondo"
										, Inversion."CodigoSerie"
										, Inversion."CodigoPais"
										, Inversion."IdEmisor"
									FROM CTE_INVERSION AS Inversion
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" Rescate ON (Rescate."IdSolicitudAporteRescate" = Inversion."IdSolicitudAporteRescate")
									GROUP BY Inversion."Diferencia", Inversion."IdCliente", Inversion."IdInstrumento", Inversion."IdRegimenTributario", Inversion."IdMes", Inversion."TotalRescatado", Inversion."NombreFondo", Inversion."CodigoSerie", Inversion."CodigoPais", Inversion."IdEmisor"
									)
								SELECT 
						  		  rescates."IdCliente" AS "IdCliente"
							 	, rescates."IdInstrumento"
								, rescates."IdRegimenTributario"
								, CASE persona."Tipo"
									WHEN 'N' THEN persona."ApellidoPaterno" || ' ' ||persona."ApellidoMaterno" ||' '|| persona."Nombres" 
									ELSE persona."RazonSocial" END AS "NombreCompleto"
								, persona."Nombres" AS "Nombres"
								, persona."ApellidoPaterno" 
								, persona."ApellidoMaterno"
								, persona."RazonSocial"
								, doci."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
								, doci."IdDocumentoIdentidad" AS "IdDocumentoIdentidad"
								, rescates."NombreFondo"
								, rescates."CodigoSerie"
								, rescates."CodigoPais" AS "CodigoPais"
								, :p_AgnoComercial AS "AgnoComercial"
								, emisor."NombreCompleto" AS "NombreEmisor"
								, sum(CASE WHEN  rescates."IdMes"  = 1 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoEnero"
								, sum(CASE WHEN  rescates."IdMes"  = 2 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoFebrero"
								, sum(CASE WHEN  rescates."IdMes"  = 3 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoMarzo"
								, sum(CASE WHEN  rescates."IdMes"  = 4 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoAbril"
								, sum(CASE WHEN  rescates."IdMes"  = 5 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoMayo"
								, sum(CASE WHEN  rescates."IdMes"  = 6 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoJunio"
								, sum(CASE WHEN  rescates."IdMes"  = 7 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoJulio"
								, sum(CASE WHEN  rescates."IdMes"  = 8 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoAgosto"
								, sum(CASE WHEN  rescates."IdMes"  = 9 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoSeptiembre"
								, sum(CASE WHEN  rescates."IdMes"  = 10 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoOctubre"
								, sum(CASE WHEN  rescates."IdMes"  = 11 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoNoviembre"
								, sum(CASE WHEN  rescates."IdMes"  = 12 THEN TO_BIGINT(rescates."TotalRescatado") ELSE 0 END) AS "TotalRescatadoDiciembre"
								, sum(CASE WHEN  rescates."IdMes"  = 1 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoEnero"
								, sum(CASE WHEN  rescates."IdMes"  = 2 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoFebrero"
								, sum(CASE WHEN  rescates."IdMes"  = 3 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoMarzo"
								, sum(CASE WHEN  rescates."IdMes"  = 4 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoAbril"
								, sum(CASE WHEN  rescates."IdMes"  = 5 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoMayo"
								, sum(CASE WHEN  rescates."IdMes"  = 6 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoJunio"
								, sum(CASE WHEN  rescates."IdMes"  = 7 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoJulio"
								, sum(CASE WHEN  rescates."IdMes"  = 8 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoAgosto"
								, sum(CASE WHEN  rescates."IdMes"  = 9 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoSeptiembre"
								, sum(CASE WHEN  rescates."IdMes"  = 10 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoOctubre"
								, sum(CASE WHEN  rescates."IdMes"  = 11 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoNoviembre"
								, sum(CASE WHEN  rescates."IdMes"  = 12 THEN TO_BIGINT(rescates."TotalAportado") ELSE 0 END) AS "TotalAportadoDiciembre"
								, sum(CASE WHEN  rescates."IdMes"  = 1 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaEnero"
								, sum(CASE WHEN  rescates."IdMes"  = 2 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaFebrero"
								, sum(CASE WHEN  rescates."IdMes"  = 3 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaMarzo"
								, sum(CASE WHEN  rescates."IdMes"  = 4 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaAbril"
								, sum(CASE WHEN  rescates."IdMes"  = 5 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaMayo"
								, sum(CASE WHEN  rescates."IdMes"  = 6 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaJunio"
								, sum(CASE WHEN  rescates."IdMes"  = 7 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaJulio"
								, sum(CASE WHEN  rescates."IdMes"  = 8 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaAgosto"
								, sum(CASE WHEN  rescates."IdMes"  = 9 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaSeptiembre"
								, sum(CASE WHEN  rescates."IdMes"  = 10 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaOctubre"
								, sum(CASE WHEN  rescates."IdMes"  = 11 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaNoviembre"
								, sum(CASE WHEN  rescates."IdMes"  = 12 THEN TO_BIGINT(rescates."Diferencia") ELSE 0 END) AS "DiferenciaDiciembre"
							FROM (
								
								SELECT
									  ResumenMensualCustodia."IdCliente"
								    , ResumenMensualCustodia."IdInstrumento"
									, ResumenMensualCustodia."IdRegimenTributario"
									, ResumenMensualCustodia."IdMes"
									, ResumenMensualCustodia."TotalRescatado"
									, ResumenMensualCustodia."TotalAportado"
									, TO_BIGINT(ResumenMensualCustodia."Diferencia") AS "Diferencia"
									, ResumenMensualCustodia."NombreFondo"
									, ResumenMensualCustodia."CodigoSerie"
									, ResumenMensualCustodia."CodigoPais"
									, ResumenMensualCustodia."IdEmisor"
								FROM CTE_RESUMEN_MENSUAL_CUSTODIA ResumenMensualCustodia
							)  AS rescates
							INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS cliente ON cliente."IdCliente" = rescates."IdCliente"
							INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS persona ON cliente."IdPersona" = persona."IdPersona"
							INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS doci ON doci."IdDocumentoIdentidad" = persona."IdDocumentoIdentidad" AND doci."Predeterminado" = 'S'
							INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emisor ON emisor."IdEmisor" = rescates."IdEmisor"
							WHERE :p_EsMasivo = 'S' OR rescates."IdCliente" IN (SELECT "IdCliente" FROM "BSCL.FondoMutuo.BD::GBL.InformeRentabilidadFFMMExtranjero_Cliente" WHERE "IdTrabajo" = :p_IdTrabajo)
							GROUP BY rescates."IdCliente",rescates."IdInstrumento", rescates."IdRegimenTributario", persona."Tipo"
									, persona."Nombres",persona."ApellidoPaterno" , persona."ApellidoMaterno"
									, persona."RazonSocial", doci."DocumentoIdentidad",rescates."NombreFondo",rescates."CodigoSerie", rescates."CodigoPais", doci."IdDocumentoIdentidad",emisor."NombreCompleto";
			
			p_salida = SELECT	  
					  DAT."IdCliente"
					, DAT."IdInstrumento"
					, DAT."IdRegimenTributario"
					, DAT."NombreCompleto"
					, DAT."Nombres"
					, DAT."ApellidoPaterno" 
					, DAT."ApellidoMaterno"
					, DAT."RazonSocial"
					, DAT."DocumentoIdentidadCliente"
					, DAT."IdDocumentoIdentidad"
					, DAT."NombreFondo"
					, DAT."CodigoSerie"
					, DAT."CodigoPais"
					, DAT."AgnoComercial"
					, DAT."NombreEmisor"
					, DAT."TotalRescatadoEnero"
					, DAT."TotalRescatadoFebrero"
					, DAT."TotalRescatadoMarzo"
					, DAT."TotalRescatadoAbril"
					, DAT."TotalRescatadoMayo"
					, DAT."TotalRescatadoJunio"
					, DAT."TotalRescatadoJulio"
					, DAT."TotalRescatadoAgosto"
					, DAT."TotalRescatadoSeptiembre"
					, DAT."TotalRescatadoOctubre"
					, DAT."TotalRescatadoNoviembre"
					, DAT."TotalRescatadoDiciembre"
					, DAT."TotalAportadoEnero"
					, DAT."TotalAportadoFebrero"
					, DAT."TotalAportadoMarzo"
					, DAT."TotalAportadoAbril"
					, DAT."TotalAportadoMayo"
					, DAT."TotalAportadoJunio"
					, DAT."TotalAportadoJulio"
					, DAT."TotalAportadoAgosto"
					, DAT."TotalAportadoSeptiembre"
					, DAT."TotalAportadoOctubre"
					, DAT."TotalAportadoNoviembre"
					, DAT."TotalAportadoDiciembre"
					, DAT."DiferenciaEnero"
					, DAT."DiferenciaFebrero"
					, DAT."DiferenciaMarzo"
					, DAT."DiferenciaAbril"
					, DAT."DiferenciaMayo"
					, DAT."DiferenciaJunio"
					, DAT."DiferenciaJulio"
					, DAT."DiferenciaAgosto"
					, DAT."DiferenciaSeptiembre"
					, DAT."DiferenciaOctubre"
					, DAT."DiferenciaNoviembre"
					, DAT."DiferenciaDiciembre"
					, IMG."LogoCorredora"
				  	, TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY') AS "FechaSistema"
				  	, CORR."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
				  	, CORR."RazonSocial" AS "RazonSocialCorredora"
				  	, CORR."CiudadCorredora"
				  	, template."Id" AS "IdTemplate"
				  	, v_validacionDJ AS "DJProcesada"
				FROM :v_resultados AS DAT
				INNER JOIN "BSCL.Report::Template" AS template ON template."ReportName" = 'INFORME RENTABILIDAD FFMM EXTRANJEROS'
				CROSS JOIN :v_logo AS IMG
				CROSS JOIN :v_DatosCorredora AS CORR;
		END;
	END;
END;