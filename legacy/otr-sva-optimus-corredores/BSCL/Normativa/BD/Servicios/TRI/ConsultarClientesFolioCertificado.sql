SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ConsultarClientesFolioCertificado";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ConsultarClientesFolioCertificado"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
  IN p_Version								  NVARCHAR(10),
  IN p_Certificado							NVARCHAR(100)
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN
		
		BEGIN
			
		--21
			IF :p_Certificado = 'GenerarCertificado21' THEN
			
				--v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21";
			  	v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT21' AND "Version"=:p_Version	
  				);    				
								
				
				SELECT DISTINCT "IdCliente","IdPortafolio", 0 as "Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
								  FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								  INNER JOIN  :v_detalles DETALLE ON DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente";

			END IF;
		--31
			IF :p_Certificado = 'GenerarCertificado31' THEN
				
				--v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31";
			  	v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT31' AND "Version"=:p_Version	
  				);    				
				
				SELECT DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
								  FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								  INNER JOIN  :v_detalles DETALLE ON DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente";
								  
			END IF;
		--7
			IF :p_Certificado = 'GenerarCertificado7' THEN
				
				--v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890";
			  	v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT7' AND "Version"=:p_Version	
  				);    
            
				
				SELECT DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
								  FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								  INNER JOIN  :v_detalles DETALLE ON DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente";
								  
			END IF;
		--43
			IF :p_Certificado = 'GenerarCertificado43' THEN
				
				--v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43";
				v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT43' AND "Version"=:p_Version	
  				);
  				
				
				SELECT DISTINCT "IdCliente","IdPortafolio",0 as "Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio"
								  FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								  INNER JOIN  :v_detalles DETALLE ON DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente";
								  
			END IF;
		--9
            IF (:p_Certificado = 'GenerarCertificado9') THEN
                
               -- v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9";
               	v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT9' AND "Version"=:p_Version	
  				);    
            
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--24
            IF (:p_Certificado = 'GenerarCertificado24') THEN
                
                --v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24";
                v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT24' AND "Version"=:p_Version	
  				);    
                           
                
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--30
            IF (:p_Certificado = 'GenerarCertificado30') THEN
                
                --v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30";
                v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT30' AND "Version"=:p_Version	
  				);    
                                      
                
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--36
            IF (:p_Certificado = 'GenerarCertificado36') THEN
                
                --v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36";
                
                v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT36' AND "Version"=:p_Version	
  				);    
                                      
                            
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--71
            IF (:p_Certificado = 'GenerarCertificado71') THEN
                
                --v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71";
                 v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT71' AND "Version"=:p_Version	
  				);    
                                  
                
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--17
            IF (:p_Certificado = 'GenerarCertificado17') THEN
                
                --v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17";
                v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT17' AND "Version"=:p_Version	
  				);    
                      
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		--8
            IF (:p_Certificado = 'GenerarCertificado8') THEN
                
             --   v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N8";
                v_detalles = SELECT DISTINCT "IdCertificadoCliente" FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N8" WHERE "IdCertificadoCliente"  IN (
  					SELECT "IdCertificadoCliente"   FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "IdTipoCertificado"='CERT8' AND "Version"=:p_Version	
  				);                 
                
                SELECT
                    DISTINCT "IdCliente","IdPortafolio","Folio",COALESCE("RutCliente",'') AS "RutCliente" ,COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio" 
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
                INNER JOIN :v_detalles DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
                                  
            END IF;
		END;
	END;								
END;