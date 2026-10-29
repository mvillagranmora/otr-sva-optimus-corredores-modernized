SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado21";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado21"(
	-- ID empresa del login.
    IN p_IdEmpresa 	BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 	BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo	NVARCHAR(3)	 ,
    
    IN p_Version	NVARCHAR(10) ,
    
    IN p_Todos		NVARCHAR(1) DEFAULT 'S',
    
    IN p_Clientes	TABLE (
    					"IdCliente"	BIGINT
    				),
    
    OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_FechaSistema		NVARCHAR(10);
			DECLARE v_count_folio		BIGINT;
			DECLARE v_count_cli		    BIGINT=0;
			DECLARE V_ultimo_folio		BIGINT;
			DECLARE v_max_folio			BIGINT;
			DECLARE v_count_certificado BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			/*rut cliente*/
       		SELECT COUNT(1)  INTO v_count_cli  FROM :p_Clientes;      
       	     IF :p_Todos = 'N' AND v_count_cli>0 THEN
       	     	v_cliente = SELECT cl."DocumentoIdentidad" AS "RutCliente", cl."IdCliente"   FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl 
						               INNER JOIN :p_Clientes parcli ON (parcli."IdCliente" = cl."IdCliente");
       	     ELSE
       	     
       	    	 v_cliente = 
       	    	 SELECT DISTINCT DAT."RutCliente",max(COALESCE(cl."IdCliente",0)) AS "IdCliente"
       	    	 FROM
       	    	 (
       	    			 SELECT trim(UPPER(dj."RutParticipe")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DatosDJ1894" AS dj
				                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE d."Version" = :p_Version   AND  d."Esquema"='DJ1894'
						 UNION ALL
						 SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" AS dj
				                --INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE dj."Version" = :p_Version  
							        
				) DAT  
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl ON (trim(DAT."RutCliente") = trim(cl."DocumentoIdentidad"))
				GROUP BY DAT."RutCliente" ;	              
						              
						              
       	     END IF; 

			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_21';
			
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('DJ1894',:p_Version,0,'CERTIFICADO_21','Certificado 21','GenerarCertificado21');
			END IF;
			
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_21';

			v_datos = 
				SELECT 
					COALESCE(c."IdCliente",0) AS "IdCliente"
					--,dj."IdPortafolio"
					,0  AS  "IdPortafolio" 
					,dj."MontoNominalRescate"
					,dj."MontoHistorico"
					,dj."MayorValorActualizado"
					,dj."MenorValorActualizado"
					--,COALESCE(dj."IdFondo",dj."Nemo") AS "IdFondo"
					,dj."Nemo"
					,dj."FechaRescate"
					,MONTH (dj."FechaRescate") AS "DescMes"
					,TO_CHAR(dj."FechaRescate",'YYYY-MM') AS "AgnoMesRescate"
					,d."IdTipoDocumento"
					,:p_IdEmpresa AS "IdEmpresa"
					,:p_Version AS "Version"
					,COALESCE(dj."IdEmisor",'') AS "IdEmisor"
					,TRIM(UPPER(dj."RutParticipe")) AS "RutCliente"
                    ,'0' AS "CodigoPortafolio"
				FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" dj
				INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" d ON (dj."IdDocumento" = d."IdDocumento" AND  d."Esquema"='DJ1894')
				LEFT OUTER JOIN :v_cliente c ON (c."RutCliente" = TRIM(UPPER(dj."RutParticipe"))) 
				WHERE d."Version" = :p_Version 
					--AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(c."IdCliente",0) != 0))
					AND dj."TipoOperacion" IN (4,5,6,7);

			v_datos_maestro = 
				SELECT
					dj."IdCliente" AS "IdCliente"
					,dj."IdPortafolio"
					,dj."MontoNominalRescate"
					,dj."MontoHistorico"
					,dj."MayorValorActualizado"
					,dj."MenorValorActualizado"
					,round((IPCACT."Valor"/IPCINV."Valor"),3) AS 	"Factor"
					--,dj."IdFondo"
					,dj."Nemo"
					,dj."FechaRescate"
					,dj."DescMes"
					,dj."AgnoMesRescate"
					,dj."IdTipoDocumento"
					,dj."IdEmpresa"
					,dj."Version"
					,dj."IdEmisor"
					,dj."RutCliente"
                    ,dj."CodigoPortafolio"
				FROM :v_datos dj
			    LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (dj."FechaRescate", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  AND IPCINV."IdParametroFinanciero" = 'IPC'
				LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(dj."FechaRescate",0,4) || '-11' = IPCACT."AgnoMes")  AND IPCACT."IdParametroFinanciero" = 'IPC';


			v_datos_agrupados = 
				SELECT 
					MAX("IdCliente") AS "IdCliente"
					,"IdPortafolio"
					,SUM("MontoNominalRescate") AS "MontoRescate"
					,SUM("MontoHistorico") AS "MontoHistorico"
					,SUM("MayorValorActualizado") AS "MayorValor"
					,SUM("MenorValorActualizado") AS "MenorValor"
					--,SUM("MayorValor") * "Factor" AS "MayorValorActualizado"
					--,SUM("MenorValor") * "Factor" AS "MenorValorActualizado"
					,"Factor"
					--,"IdFondo"
					,"Nemo"
					,MAX("FechaRescate") AS "FechaRescate"
					,"DescMes"
					,"AgnoMesRescate"
					,"IdTipoDocumento"
					,"IdEmpresa"
					,"Version"
					,"IdEmisor"
					,"RutCliente"
                    ,"CodigoPortafolio"
				FROM :v_datos_maestro
				--GROUP BY "IdPortafolio","Factor","IdFondo","DescMes","AgnoMesRescate","IdTipoDocumento","IdEmpresa","Version","IdEmisor","RutCliente","CodigoPortafolio";
				GROUP BY "IdPortafolio","Factor","Nemo","DescMes","AgnoMesRescate","IdTipoDocumento","IdEmpresa","Version","IdEmisor","RutCliente","CodigoPortafolio";
				
			v_datos_actualizados = 
				SELECT 	
					"IdCliente"
					,"IdPortafolio"
					,"MontoRescate"
					,"MontoHistorico"
					,"MayorValor" / "Factor" AS "MayorValor"
					,"MenorValor" / "Factor" AS "MenorValor"
					,"MayorValor"  AS "MayorValorActualizado"
					,"MenorValor"  AS "MenorValorActualizado"
					,"Factor"
					--,"IdFondo"
					,"Nemo"
					,"FechaRescate"
					,"DescMes"
					,"AgnoMesRescate"
					,"IdTipoDocumento"
					,"IdEmpresa"
					,"Version"
					,"IdEmisor"
					,"RutCliente"
					,"CodigoPortafolio"
				FROM :v_datos_agrupados;

			v_datos_certificado = 
				SELECT DISTINCT "IdPortafolio","IdTipoDocumento","IdEmpresa","Version","Nemo","RutCliente","CodigoPortafolio" FROM :v_datos_actualizados;
									
			v_datos_sin_certificado = 
				SELECT 
					"BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente"
					,:v_ultimo_folio + "Numero" AS "Folio"
                    ,"RutCliente"
					,"CodigoPortafolio"
					,"IdPortafolio"
					,"IdTipoDocumento"
					,"IdEmpresa"
					,"Version"
					,"Nemo"
				FROM (
					SELECT 
						ROW_NUMBER() OVER(ORDER BY d."RutCliente",d."CodigoPortafolio") AS "Numero"
						,d."RutCliente"
						,d."CodigoPortafolio"
						,d."IdPortafolio"
						,d."IdTipoDocumento"
						,d."IdEmpresa"
						,d."Version"
						,d."Nemo"
					FROM :v_datos_certificado d
					WHERE NOT EXISTS (
						SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c 
						INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" det ON (det."IdCertificadoCliente" = c."IdCertificadoCliente"  AND det."IdFondo"=c."Nemo") 
							WHERE c."RutCliente" = d."RutCliente"
								AND c."CodigoPortafolio"  = d."CodigoPortafolio"
								AND c."Esquema" 		  = 'DJ1894'
								AND c."IdTipoCertificado" = 'CERT21'
								AND c."Version" 		  = d."Version"
								AND c."IdEmpresa" 		  = d."IdEmpresa"
								AND c."Nemo" 		      = d."Nemo"
					));
			
			
			v_detalle_sin_certificado = 
				SELECT  
					"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1894N21".NEXTVAL AS "IdDetalleCertificadoClienteDJ1894N21"
					,c."IdCertificadoCliente"
					,d."MontoRescate"
					,d."MontoHistorico"
					,d."MayorValor"
					,d."MenorValor"
					,d."MayorValorActualizado"
					,d."MenorValorActualizado"
					,d."DescMes"
					,d."Nemo"
					,d."Factor"
					,d."Version"
					,d."IdEmisor"
					,d."RutCliente"
                    ,d."CodigoPortafolio"
				FROM :v_datos_actualizados d
				INNER JOIN :v_datos_sin_certificado c 	
						ON (c."RutCliente" = d."RutCliente"
                        AND c."CodigoPortafolio" = d."CodigoPortafolio"
						AND d."Nemo" 		    = c."Nemo"
						AND d."IdEmpresa" 		= c."IdEmpresa"
						AND d."Version" 		= c."Version"
						AND d."IdTipoDocumento" = c."IdTipoDocumento"
						);
			
			v_datos_con_certificado = 
				SELECT 
					c."IdCertificadoCliente"
					,c."Folio"
                    ,d."RutCliente"
					,d."CodigoPortafolio"
					,d."IdPortafolio"
					,d."IdTipoDocumento"
					,d."IdEmpresa"
					,d."Version"
					,d."Nemo"
				FROM :v_datos_certificado d
				INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" c ON (c."RutCliente" = d."RutCliente"
																		AND c."CodigoPortafolio" = d."CodigoPortafolio" 
																		AND c."Nemo" 			 = d."Nemo"   ----aqui
																		AND c."Esquema" 		 = 'DJ1894'
																		AND c."Version" 	 	 = d."Version"
																		AND c."IdEmpresa" 		 = d."IdEmpresa"
																		AND c."IdTipoCertificado" = 'CERT21');
				--WHERE EXISTS ( SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" de WHERE de."IdFondo" = d."IdFondo" AND c."IdCertificadoCliente" = de."IdCertificadoCliente");
			
			v_detalle_con_certificado = 
				SELECT 	
					"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1894N21".NEXTVAL AS "IdDetalleCertificadoClienteDJ1894N21"
					,c."IdCertificadoCliente"
					,d."MontoRescate"
					,d."MontoHistorico"
					,d."MayorValor"
					,d."MenorValor"
					,d."MayorValorActualizado"
					,d."MenorValorActualizado"
					,d."DescMes"
					,d."Nemo"
					,d."Version"
					,d."Factor"
					,d."IdEmisor"
					,d."RutCliente"
                    ,d."CodigoPortafolio"
				FROM :v_datos_actualizados d
				INNER JOIN :v_datos_con_certificado c  	ON (c."RutCliente" = d."RutCliente"
                                                       	AND c."CodigoPortafolio" = d."CodigoPortafolio"
														AND d."Nemo" = c."Nemo"
														AND d."IdEmpresa" = c."IdEmpresa"
														AND d."Version" = c."Version"
														AND d."IdTipoDocumento" = c."IdTipoDocumento"
														);
						/*
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" de 
					ON (c."IdCertificadoCliente" = de."IdCertificadoCliente" 
					AND d."IdFondo" = de."IdFondo" 
					AND d."DescMes" = de."DescMes");
					*/
							
            INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"(
                "IdCertificadoCliente", "IdTipoDocumento", "IdEmpresa", 
                "Esquema", "Version", "Anio", "Periodo", 
                "IdCliente", "IdPortafolio", "Folio", 
                "IdTipoCertificado", "RutCliente", "CodigoPortafolio","Nemo")
            SELECT 
                "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",
                'DJ1894',"Version","Version","Version",
                0 AS "IdCliente",0 AS "IdPortafolio","Folio",
                'CERT21',TRIM(UPPER("RutCliente")),"CodigoPortafolio","Nemo"
            FROM :v_datos_sin_certificado; 

			--Se actualizan los folios en la tabla de datos en la dj
			/*
			UPDATE Datos
				SET "NumeroCertificado" = Cert."Folio"
				FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" Datos
				INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" Cert
				ON Cert."Esquema" = 'DJ1894'
				--si es por carga externa usara el RutCliente
			AND Cert."Version" 	 = Datos."Version"	
			AND Cert."IdCliente" = Datos."IdCliente" OR (Datos."RutParticipe" = Cert."RutCliente" AND Datos."TipoOperacion" IN (4,5,6,7));
            */

            UPDATE Datos
				SET "NumeroCertificado" = Cert."Folio"
				FROM "BSCL.Normativa.BD::TRI.DatosDJ1894" Datos
				INNER JOIN "BSCL.Normativa.BD::DOC.Documento" 	Doc
				ON (Doc."Esquema"='DJ1894' AND Doc."IdDocumento"= Datos."IdDocumento")
				INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" Cert
				ON (Cert."Esquema" = 'DJ1894' and  Cert."IdTipoCertificado"='CERT21'
				--si es por carga externa usara el RutCliente
				AND Cert."Version" 	 = p_Version	
				AND Cert."RutCliente" = Datos."RutParticipe"
				AND Cert."Nemo" = Datos."Nemo"
				AND Datos."TipoOperacion" IN (4,5,6,7)
				OR (Cert."Esquema" = 'DJ1894' AND TRIM(UPPER(Datos."RutParticipe")) = Cert."RutCliente" AND Cert."Version" = p_Version AND Datos."FuenteDatos" = 'EXTERNA' AND Cert."Nemo" = Datos."Nemo"))
				WHERE Doc."Version"=:p_Version;
			    
            
            
			DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" d 
			WHERE d."Version"= :p_Version
			AND TRIM(UPPER(d."RutCliente")) IN ( SELECT "RutCliente" FROM :v_cliente);
			

			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21"
			(
				 "IdDetalleCertificadoClienteDJ1894N21", "IdCertificadoCliente", "IdFondo",
				 "Factor", "MontoRescate", "MontoHistorico", "MayorValor", "MenorValor", 
				 "MayorValorActualizado", "MenorValorActualizado", "DescMes", "IdEmisor", "RutCliente","Version"
			)
			SELECT 
				 "IdDetalleCertificadoClienteDJ1894N21", "IdCertificadoCliente", "Nemo",
				 "Factor", "MontoRescate", "MontoHistorico", "MayorValor", "MenorValor", 
				 "MayorValorActualizado", "MenorValorActualizado", "DescMes", "IdEmisor", TRIM(UPPER("RutCliente")),"Version"
			FROM :v_detalle_sin_certificado;

			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21"
			(
				 "IdDetalleCertificadoClienteDJ1894N21", "IdCertificadoCliente", "IdFondo",
				 "Factor", "MontoRescate", "MontoHistorico", "MayorValor", "MenorValor", 
				 "MayorValorActualizado", "MenorValorActualizado", "DescMes", "IdEmisor", "RutCliente","Version"
			)
			SELECT 
				 "IdDetalleCertificadoClienteDJ1894N21", "IdCertificadoCliente", "Nemo",
				 "Factor", "MontoRescate", "MontoHistorico", "MayorValor", "MenorValor", 
				 "MayorValorActualizado", "MenorValorActualizado", "DescMes", "IdEmisor", TRIM(UPPER("RutCliente")),"Version"
			FROM :v_detalle_con_certificado;

			SELECT COUNT(1) into v_count_certificado FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
			WHERE "Esquema" = 'DJ1894' AND CERTIFICADO."Version" = :p_Version AND CERTIFICADO."IdTipoCertificado" ='CERT21';
			
			IF(v_count_certificado > 0) THEN
				SELECT MAX("Folio") into v_max_folio  
				FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
				WHERE "Esquema" = 'DJ1894' AND CERTIFICADO."Version" = :p_Version AND CERTIFICADO."IdTipoCertificado" ='CERT21';
				
				UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				SET "UltimoFolio" = v_max_folio
				WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_21';
			END IF;
			
			UPDATE "BSCL.Normativa.BD::TRI.ControlProceso" 
			SET "Carga" = 'S', 
				"Validacion" = 'S', 
			    "PreDeclaracion" = 'S', 
			    "Certificacion" = 'S', 
				"EstadoCertificado" = 'GEN' 
			WHERE "Esquema"= 'DJ1894' AND "Version" = :p_Version;
			
			
			
			UPDATE C SET C."IdEstado"='ACT'
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
			WHERE C."IdTipoCertificado"='CERT21' AND C."Version"=:p_Version
			AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente");
			
			v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0 AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'CERTIFICADO_21' AS "IdTipoRegistro"
						,:p_Version AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM DUMMY;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
