SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ExportarCircular";

CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03/08/2016

"BSCL.Normativa.BD.Servicios.NOR::ExportarCircular" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
 	IN p_IdTipoDocumento   		 	  NVARCHAR(10),
 	IN p_IdProceso					  BIGINT DEFAULT 0
    --VARIABLES DE SALIDA 
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_Count INT:=0;
		DECLARE v_Esquema NVARCHAR(10):='';
		DECLARE v_sp NVARCHAR(500):='';

		v_TablaParametros=SELECT '' AS "TipoParametro",'' AS "Valor" FROM DUMMY WHERE 1=2;	
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE  "IdTipoDocumento" =p_IdTipoDocumento;	

		IF v_Count>0 THEN
			
			SELECT RTRIM("Esquema") INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE  "IdTipoDocumento" =p_IdTipoDocumento;

			---------------------------------------------------------------
			---- EXPORTACION DE DATOS
			---------------------------------------------------------------

			IF 	v_Esquema='ART171Car1' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::ART171Car1" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida1);
				SELECT * FROM :v_out_TablaSalida1;
				
			END IF;
			IF 	v_Esquema='ART171Car2' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::ART171Car2" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida2);
				SELECT * FROM :v_out_TablaSalida2;
			END IF;
			IF 	v_Esquema='CI63Nomina' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::CI63Nomina" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida3);
				SELECT * FROM :v_out_TablaSalida3;
			END IF;
			
			IF 	v_Esquema='CI63Transa' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::CI63Transa" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida4);
				SELECT * FROM :v_out_TablaSalida4;
			END IF;	

			IF 	v_Esquema='NCG380Asig' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Asig"  (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida5);
				SELECT * FROM :v_out_TablaSalida5;
			END IF;
	
			IF 	v_Esquema='NCG380Aut' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.NCG380Aut" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida6);
				SELECT * FROM :v_out_TablaSalida6;
			END IF;
			
			IF 	v_Esquema='NCG380Cli' THEN 
				--* MSH : 2018-04			
				--* CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Cli" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida7);			
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cli" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida7);
				SELECT * FROM :v_out_TablaSalida7;
			END IF;
	
			IF 	v_Esquema='NCG380Cus' THEN 
				--* MSH : 2018-04
				--* CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Cus"(p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida8);
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cus"(p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida8);
				SELECT * FROM :v_out_TablaSalida8;
			END IF;

			IF 	v_Esquema='NCG380Fac' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Fac"  (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida9);
				SELECT * FROM :v_out_TablaSalida9;
			END IF;

			IF 	v_Esquema='NCG380Ope' THEN 
				--* MSH : 2018-04
				--* CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Ope" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida10);
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ope" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaNCG380Ope=>v_out_TablaSalida10);
				
				SELECT * FROM :v_out_TablaSalida10;
			END IF;

			IF 	v_Esquema='NCG380Ord' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Ord" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida11);
				SELECT * FROM :v_out_TablaSalida11;
			END IF;

			IF 	v_Esquema='NCG380Tra' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Tra" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_out_TablaSalida=>v_out_TablaSalida12);
				SELECT * FROM :v_out_TablaSalida12;
			END IF;	
			
			IF 	v_Esquema='CIR1064' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.CIR1064SVS"(p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida13);
				SELECT * FROM :v_out_TablaSalida13;
			END IF;	
			
			IF 	v_Esquema='CIR1064R' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.CIR1064Rueda"(p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida14);
				SELECT * FROM :v_out_TablaSalida14;
			END IF;	
			
			IF 	v_Esquema='CIR1064FR' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.CIR1064FueraRueda"(p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida15);
				SELECT * FROM :v_out_TablaSalida15;
			END IF;	

			IF 	v_Esquema='INFTRASEIL' OR v_Esquema='INFTRABCS'  THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.INFOTRANOFIC" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_IdTipoDocumento=>p_IdTipoDocumento,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida16);
				SELECT * FROM :v_out_TablaSalida16;
			END IF;
			
			IF 	v_Esquema='UAFROE' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::UAFROE" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida17);
				SELECT * FROM :v_out_TablaSalida17;
			END IF;
			
			IF 	v_Esquema='UAFDOF' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::UAFDOF" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida18);
				SELECT * FROM :v_out_TablaSalida18;
			END IF;

			IF 	v_Esquema='FATCAR' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.FatcaResumen" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida19);
				SELECT * FROM :v_out_TablaSalida19;
			END IF;
	
			IF 	v_Esquema='FATCAD' THEN 
				CALL "BSCL.Normativa.BD.Servicios::NOR.FatcaDetalle" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida20);
				SELECT * FROM :v_out_TablaSalida20;
			END IF;

			IF 	v_Esquema='INFDERECH' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::INFDERECH" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida21);
				SELECT * FROM :v_out_TablaSalida21;
			END IF;		
	
			IF 	v_Esquema='NCG380Ins' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Instrucciones" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida22);
				SELECT * FROM :v_out_TablaSalida22;
			END IF;	
			
			IF 	v_Esquema='NCG380Rep' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Repactaciones" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida23);
				SELECT * FROM :v_out_TablaSalida23;
			END IF;	
		
			IF 	v_Esquema='NCG380Deu' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380Deudores" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida24);
				SELECT * FROM :v_out_TablaSalida24;
			END IF;
			
			IF 	v_Esquema='NCG380IntB' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::NCG380OperacionesBur" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida25);
				SELECT * FROM :v_out_TablaSalida25;
			END IF;
			
			IF 	v_Esquema='CIR1518' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::CIR1518" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_IdTipoDocumento=>p_IdTipoDocumento,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida26);
				SELECT * FROM :v_out_TablaSalida26;
			END IF;
				
			IF 	v_Esquema='CIR1720' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::CIR1720" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_IdTipoDocumento=>p_IdTipoDocumento,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida27);
				SELECT * FROM :v_out_TablaSalida27;
			END IF;
			IF 	v_Esquema='CIRCMF' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::CIRCMF" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_IdTipoDocumento=>p_IdTipoDocumento,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida28);
				SELECT * FROM :v_out_TablaSalida28;
			END IF;
			IF 	v_Esquema='PACVIGEBOX' THEN 
				CALL "BSCL.Normativa.BD.Servicios.NOR::ArchivoPactosVigentesEbox" (p_IdSolicitudCircular=>p_IdSolicitudCircular,p_IdTipoDocumento=>p_IdTipoDocumento,p_TablaParametros=>:v_TablaParametros,p_out_TablaSalida=>v_out_TablaSalida29);
				SELECT * FROM :v_out_TablaSalida29;
			END IF;


		END IF;
	
END; 
