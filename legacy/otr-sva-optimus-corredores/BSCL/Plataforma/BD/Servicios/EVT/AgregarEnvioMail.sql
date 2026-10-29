SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarEnvioMail";
 
CREATE PROCEDURE
-- 
--  Proposito: Agrega un regsitro a la tabla de Mail-Destinatario-ArchivoAdjunto que es leido por el job
--  Autor: - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarEnvioMail"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                in p_Mails table("IdEnvio" BIGINT,"MensajeCuerpo" NCLOB,"Remitente" NVARCHAR(200),"Asunto" NVARCHAR(540)),
   									IN p_Destinatarios	table ("IdEnvio" BIGINT,"TipoDestinatario" NVARCHAR(5), "Destinatario" NVARCHAR(200)),
  									IN p_ArchivoAdjunto table ("IdEnvio" BIGINT, "Archivo" BLOB,"Nombre" NVARCHAR(200)),	
  								 
  								 	
 									OUT p_out_IdArchivo	table("IdArchivoAdjunto" BIGINT, "Archivo" BLOB, "Nombre" NVARCHAR(200),"IdEmail" BIGINT),
 								 	OUT p_out_MensajeSalida			NVARCHAR(520)

	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

 		DECLARE v_FechaCreacion SECONDDATE;
 		DECLARE v_contador BIGINT;
 		DECLARE v_contadorMail BIGINT;
 		DECLARE v_cc BIGINT;
 		
 		
 		select CURRENT_UTCTIMESTAMP INTO v_FechaCreacion FROM DUMMY;
 		v_IdMail = select "IdEnvio","BSCL.Plataforma.BD.Secuencias::EVT.Mail".nextval as "IdMail", "MensajeCuerpo","Remitente","Asunto" from :p_Mails;
   
		
		auxMail = select distinct "IdMail","Remitente", "Asunto",to_char("MensajeCuerpo") as "MensajeCuerpo" from :p_Destinatarios p INNER JOIN :v_IdMail v ON v."IdEnvio" = p."IdEnvio";
		
		SELECT COUNT(1) into v_contador FROM :p_Destinatarios;
		SELECT COUNT(1) INTO v_contadorMail from :v_IdMail;
		
		IF :v_contador > 0 and v_contadorMail > 0 THEN
		
		INSERT INTO "BSCL.Plataforma.BD::EVT.Mail" 
		(  "IdEmail", "FechaCreacion","Remitente",                        
 		   "Asunto","MensajeCuerpo")
 		SELECT  
 			v."IdMail",
 			v_FechaCreacion,
 			"Remitente",
 			"Asunto",
 			"MensajeCuerpo"

 			from :auxMail v;

  		
 		INSERT INTO "BSCL.Plataforma.BD::EVT.Destinatario" 
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::EVT.Destinatarios".nextval,
				PD."IdMail",
				"TipoDestinatario",
				"Destinatario"
			FROM :v_IdMail pd
			INNER JOIN :p_Destinatarios  vi
			ON pd."IdEnvio" = vi."IdEnvio" 
			where "Destinatario" is not null; 
		
		INSERT INTO "BSCL.Plataforma.BD::EVT.ArchivoAdjunto"
			SELECT  
				"BSCL.Plataforma.BD.Secuencias::EVT.ArchivoAdjunto".nextval,
				"IdMail",
				"Nombre",
				"Archivo"
			FROM :p_ArchivoAdjunto 	aa
			INNER JOIN :v_IdMail  vi
			ON aa."IdEnvio" = vi."IdEnvio";
		
		select count(1)	INTO v_cc from :v_IdMail;
		
 		p_out_MensajeSalida := ''|| v_cc;
 		p_out_IdArchivo = 
			 		SELECT 
							"IdArchivoAdjunto",
 							"Archivo",
							"Nombre",
							aa."IdEmail"
						FROM "BSCL.Plataforma.BD::EVT.ArchivoAdjunto" 	aa
						LEFT JOIN :v_IdMail  vi
						ON aa."IdEmail" = vi."IdMail" 
						LEFT JOIN "BSCL.Plataforma.BD::EVT.Mail" mail
						ON mail."IdEmail" = aa."IdEmail"
						WHERE "EstadoEnviado" = 'N';
 			

 		
 		ELSE
 		
 		END IF;
 		
 		
 			 
 			
 		   
		   

	

END;
