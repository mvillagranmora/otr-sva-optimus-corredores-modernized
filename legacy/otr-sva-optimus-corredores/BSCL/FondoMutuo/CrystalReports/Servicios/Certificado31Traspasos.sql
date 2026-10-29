SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.FondoMutuo.CrystalReports.Servicios::Certificado31Traspasos";
GO
CREATE PROCEDURE "BSCL.FondoMutuo.CrystalReports.Servicios::Certificado31Traspasos" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
	IN p_Traspaso           	TABLE ( "IdTraspaso" BIGINT ),
	
	OUT p_Salida   TABLE (
		"FolioTraspaso" 						BIGINT,	
		"IdTraspaso" 							BIGINT,	
		"FechaEncabezado"                       NVARCHAR(10),
		"Lugar"                                 NVARCHAR(100),
		"NombreCompleto"                        NVARCHAR(302),
		"RutCliente"                            NVARCHAR(10),
		"NumeroCuotas"                          DECIMAL(26,8),
		"ValorCuota"                            DECIMAL(26,8),
		"IdSerieFondo"                          NVARCHAR(100),
		"RutFondo"                              NVARCHAR(10),
		"NombreSerieFondo"				        NVARCHAR(100),
		"Fechaingreso"       				    NVARCHAR(10),
		"MontoTraspasoUF"			      		DECIMAL(26,8),
		"MontoOriginalUF"                       DECIMAL(26,8),
		"FechaIngresoCartera" 		            NVARCHAR(10),
		"ProcentajeInvAccionesFondo"			DECIMAL(26,8),		  
		"ProcentajeInvAccionesFondoRescate"		DECIMAL(26,8),	  
		"RunFondoMutuo"			                NVARCHAR(10),
		"NombreFondoMutuo"		                NVARCHAR(100),
		"RutSociedadAdministradora"	            NVARCHAR(10),
		"NombreSociedadAdministradora"	        NVARCHAR(100),
		"DocumentoIdentidadEntidadRecaudadora" 	NVARCHAR(100),		  
		"IdCliente"                             BIGINT,
		"RazonSocial"                           NVARCHAR(100),
		"Direccion"                             NVARCHAR(300),
		"DocumentoIdentidad"                    NVARCHAR(10),
		"YearComercial"							NVARCHAR(4),
		"NumeroCertificado" 					BIGINT,
		"LogoCorredora"                         BLOB,
		"FirmaCorredora"                        BLOB
		
	
	)
	
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_IdSolicitudAporteRescate BIGINT;
		DECLARE v_Logo  TABLE("LogoCorredora" BLOB);
		DECLARE v_Firma TABLE("FirmaCorredora" BLOB);


		v_Logo =
			SELECT TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_Firma =
			SELECT TOP 1 "Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
	
		v_DatosCorredor = 
			SELECT 
			  PER."RazonSocial" 
			 ,EMP."IdEmpresa" 
			 ,CONCAT(DIR."Calle" ,CONCAT(' ',CONCAT(DIR."Numero",CONCAT(' , ',CIU."NombreCiudad")))) AS "Direccion"
			 ,DOC."DocumentoIdentidad" 
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona" 
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON DIR."IdPersona" = PER."IdPersona" AND DIR."Predeterminado" ='S'
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
			INNER JOIN  "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON CIU."IdCiudad" = DIR."IdCiudad" 
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;
		
		
		v_SolicitudesAporteRescate = SELECT ORI."IdSolicitudAporteRescate"  
									 FROM  "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" AS ORI
									 INNER JOIN :p_Traspaso  AS TRA ON TRA."IdTraspaso" = ORI."IdTraspaso";
									
		v_DatosAporte = 	SELECT  
							 CAR."EGRIdOperacion" AS "IdOperacionRescate" 
						    ,CAR."Nemotecnico"
						    ,SUM(CAR."EGRCantidad") AS "EGRCantidad"
						    ,APO."FechaCertificacion"
						    ,APO."IdSerieFondo"
						    ,APO."ValorCuota"
						    ,APO."FechaValorCuota" 
						    ,SUM(CAR."EGRCantidad"*APO."ValorCuota") AS "MontoValorCuotaAporte"
						FROM  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS RES
						INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."EGRIdOperacion" = RES."IdSolicitudAporteRescate" 
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS APO ON APO."IdSolicitudAporteRescate" = CAR."IdOperacion" 
						INNER JOIN :v_SolicitudesAporteRescate AS SOL ON SOL."IdSolicitudAporteRescate"=RES."IdSolicitudAporteRescate"
						GROUP BY CAR."EGRIdOperacion"
							,CAR."Nemotecnico"
							,APO."FechaCertificacion",APO."FechaCertificacion"
						    ,APO."IdSerieFondo"
						    ,APO."ValorCuota"
						    ,APO."FechaValorCuota";	
					
		v_DatosIdOperecionResFecha = SELECT   
										CAR."EGRIdOperacion" AS "IdOperacionRescate"
										,MAX(APO."FechaValorCuota") AS "fecha"
										,RES."FechaValorCuota" 
									FROM  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS RES
									INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."EGRIdOperacion" = RES."IdSolicitudAporteRescate" 
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS APO ON APO."IdSolicitudAporteRescate" = CAR."IdOperacion" 
									INNER JOIN :v_SolicitudesAporteRescate AS SOL ON SOL."IdSolicitudAporteRescate"=RES."IdSolicitudAporteRescate"
									GROUP BY  CAR."EGRIdOperacion",RES."FechaValorCuota" ;
									
		v_ResultadoDatosAporte = SELECT 
										APO."IdOperacionRescate"
										,APO."Nemotecnico"
										,APO."EGRCantidad"
										,APO."FechaCertificacion"
										,APO."IdSerieFondo"
										,APO."ValorCuota"
										,APO."FechaValorCuota"
										,APO."MontoValorCuotaAporte"	
								FROM :v_DatosAporte AS APO 
								INNER JOIN :v_DatosIdOperecionResFecha AS DAF ON DAF."IdOperacionRescate" = APO."IdOperacionRescate"
								WHERE DAF."fecha" = APO."FechaValorCuota";
					
					
		v_ValorUF = 
					SELECT DISTINCT
						PAR."Valor",PAR."AgnoMes",PAR."Fecha"
					FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS PAR 
					INNER JOIN :v_DatosIdOperecionResFecha  AS RES ON RES."FechaValorCuota" = PAR."Fecha"
					WHERE PAR."IdParametroFinanciero" = 'UF';
					
					
		v_ValorPrimerAporte = 	SELECT 
									MIN(APO."FechaValorCuota") AS "FechaValorCuota"
									, APO."IdOperacionRescate"
								FROM :v_DatosAporte AS APO
								GROUP BY APO."IdOperacionRescate";

		v_DatosDetalle =SELECT 
							  TRA."FolioTraspaso"
							 ,TRA."IdTraspaso" 
							 ,APO."MontoValorCuotaAporte"
							 ,PAR."Valor"
							 ,TO_VARCHAR(TRA."FechaHoraSuscripcion" ,'YYYY-MM-DD')  AS "FechaEncabezado"
							 ,COALESCE (DIV."NombreDivisionPais" ,'')AS "Lugar"
							 ,TRA."NombreCompleto"
							 ,DOC."DocumentoIdentidad" AS "RutCliente"
							 ,ORI."Cantidad"  AS "NumeroCuotas"
							 ,RES."ValorCuota" 
							 ,RES."IdSerieFondo"
							 ,COALESCE (FON."CodigoSVS",'')  AS "RutFondo"  --1  ORIGEN
							 ,ORI."NombreSerieFondo"  AS "NombreSerieFondo"				  --2	
							 ,TO_VARCHAR(TRA."FechaIngreso" ,'YYYY-MM-DD')  AS "Fechaingreso"       				  --3
							 ,RES."MontoMonedaSistema"/PAR."Valor" AS "MontoTraspasoUF"			      		  --4
							 ,RES."CostoUF" 	AS "MontoOriginalUF"  --5 
							 ,TO_VARCHAR(VPA."FechaValorCuota",'YYYY-MM-DD') AS "FechaIngresoCartera" 		  --6		
							 ,0.0 AS "ProcentajeInvAccionesFondo"						  --7
							 ,0.0 AS "ProcentajeInvAccionesFondoRescate"				  --8 
							 ,COALESCE (IND."RunFFMM",'') AS "RunFondoMutuo"			  --9	DESTINO
							 ,COALESCE (IND."NombreFFMM",'') AS "NombreFondoMutuo"		  --10	
							 ,COALESCE (IND."RutSociedadAdmFFMM",'') AS "RutSociedadAdministradora"	
							 ,COALESCE (IND."SociedadAdmFFMM",'') AS "NombreSociedadAdministradora"	
							 ,TRA."DocumentoIdentidadEntidadRecaudadora" 
							 ,'' AS "FirmaAdministradora"								  
							 ,TRA."IdCliente" 
							 ,YEAR(TRA."FechaIngreso") AS "YearComercial"
						FROM "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoUI" AS TRA 
						INNER JOIN   :p_Traspaso AS ENT ON ENT."IdTraspaso"=TRA."IdTraspaso"	
						INNER JOIN  "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleOrigenUI" AS ORI ON TRA."IdTraspaso" = ORI."IdTraspaso" 
						LEFT JOIN  "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino"  AS DES ON TRA."IdTraspaso" = DES."IdTraspaso" 
						INNER JOIN  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS RES ON RES."IdSolicitudAporteRescate" = ORI."IdSolicitudAporteRescate" 
						INNER JOIN :v_ValorPrimerAporte AS VPA ON VPA."IdOperacionRescate" = RES."IdSolicitudAporteRescate"
						INNER JOIN  "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" =ORI."IdSerieFondo" 
						INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS FON ON FON."IdFondo" = SER."IdFondo"
						LEFT JOIN  "BSCL.Plataforma.BD::REG.DivisionPais" AS DIV ON DIV."IdDivisionPais" = TRA."IdDivisionPais"
						LEFT  JOIN  "BSCL.FondoMutuo.BD::FMM.FondosIndustria" AS IND ON IND."RunFFMM" =DES."RunFFMM"
						INNER JOIN  "BSCL.SocioNegocio.BD::CLI.Cliente" AS  CLI ON CLI."IdCliente" = TRA."IdCliente" 
						INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona" 
						INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
						INNER JOIN  :v_ResultadoDatosAporte AS APO ON APO."IdOperacionRescate" = ORI."IdSolicitudAporteRescate"
						LEFT JOIN   :v_ValorUF PAR ON PAR."Fecha" = RES."FechaValorCuota"
						WHERE TRA."IdEstado" = 'VIG';
						
						
						
		p_Salida = SELECT
						   "Detalle"."FolioTraspaso" 
						 , "Detalle"."IdTraspaso"
						 , "Detalle"."FechaEncabezado"
						 , "Detalle"."Lugar"
						 , "Detalle"."NombreCompleto"
						 , "Detalle"."RutCliente"
						 , "Detalle"."NumeroCuotas" 
						 , "Detalle"."ValorCuota" 
						 , "Detalle"."IdSerieFondo"
						 , "Detalle"."RutFondo" 
						 , "Detalle"."NombreSerieFondo"				
						 , "Detalle"."Fechaingreso"       				  
						 , "Detalle"."MontoTraspasoUF"			      		  
						 , "Detalle"."MontoOriginalUF"  
						 , "Detalle"."FechaIngresoCartera" 		  
						 , "Detalle"."ProcentajeInvAccionesFondo"						  
						 , "Detalle"."ProcentajeInvAccionesFondoRescate"				  
						 , "Detalle"."RunFondoMutuo"			  
						 , "Detalle"."NombreFondoMutuo"		  
						 , "Detalle"."RutSociedadAdministradora"	
						 , "Detalle"."NombreSociedadAdministradora"	
						 , "Detalle"."DocumentoIdentidadEntidadRecaudadora" 				  
						 , "Detalle"."IdCliente"						 
						 , "Corredor"."RazonSocial"
						 , "Corredor"."Direccion"
						 , "Corredor"."DocumentoIdentidad"
						 , "Detalle"."YearComercial"
						 , 0 AS "NumeroCertificado"
						 , "Logo"."LogoCorredora"
						 , "Firma"."FirmaCorredora"
					 FROM :v_DatosDetalle AS "Detalle"
					 LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
			         LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
					 LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL;
		

END;
