SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.ConsultarResumen";
 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información Resumen de Circularización.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 11/10/2016

"BSCL.Normativa.BD.Servicios::CIR.ConsultarResumen" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    IN p_Periodo			NVARCHAR(6),
    IN p_Seccion			NVARCHAR(3) DEFAULT ''


    --VARIABLES DEL PROCESO

    --VARIABLES DE SALIDA OBLIGATORIAS

	--OUT p_out_TablaPEP	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
  		DECLARE v_Fecha NVARCHAR(10):='';
  		DECLARE v_Tipo NVARCHAR(14):='';
		DECLARE v_Count 							 BIGINT:=0;
		DECLARE	v_IdClienteCtaPropia 			 BIGINT:=0;
		DECLARE v_totCliconCust, v_porCliconCust, v_totComconCust, v_porComconCust, v_totInsconCust, v_porInsconCust, v_totInsconCustCTA_RV, v_totInsconCustCTP	 BIGINT:=0;
		DECLARE v_monCliconCust, v_monComconCust, v_monInsconCust, v_monInsconCustCTA_RV, v_monInsconCustCTP	 				 DECIMAL(26,8):=0;
		
		DECLARE v_totCliNoCirc, v_porCliNoCirc,v_PorMonCliNoCirc,v_totClicirManual,v_porClicirManual,v_PorMonClicirManual,v_totClicirInternet,v_porClicirInternet BIGINT:=0;
		DECLARE v_PorMonClicirInternet,v_totClicirInternetCON, v_porClicirInternetCON, v_PorMonClicirInternetCON, v_totClicirInternetDIS, v_porClicirInternetDIS, v_PorMonClicirInternetDIS  BIGINT:=0;
		DECLARE v_totClicirInternetPEN, v_porClicirInternetPEN, v_PorMonClicirInternetPEN  BIGINT:=0;
		DECLARE v_monCliNoCirc,v_monClicirManual,v_monClicirInternet,v_monClicirInternetCON, v_monClicirInternetDIS, v_monClicirInternetPEN	 DECIMAL(26,8):=0;
		
		DECLARE v_totComCon, v_porComCon, v_PorMonComCon BIGINT:=0;
		DECLARE v_monComcon  DECIMAL(26,8):=0;

		DECLARE v_totComDIS, v_porComDIS, v_PorMonComDIS BIGINT:=0;
		DECLARE v_monComDIS  DECIMAL(26,8):=0;
		
		DECLARE v_totComPEN, v_porComPEN, v_PorMonComPEN BIGINT:=0;
		DECLARE v_monComPEN  DECIMAL(26,8):=0;
		
		DECLARE v_totINSCon, v_porINSCon, v_PorMonINSCon BIGINT:=0;		
		DECLARE v_monINSCon  DECIMAL(26,8):=0;		
		
		DECLARE v_totINSDIS, v_porINSDIS, v_PorMonINSDIS BIGINT:=0;		
		DECLARE v_monINSDIS  DECIMAL(26,8):=0;	
			
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora" 
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		---Valores de la cuenta propia	
		SELECT CLI."IdCliente" INTO v_IdClienteCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
		--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = p_IdEmpresa
		AND CLI."IdEstado"='ACT';
		---Parametros

		v_TabSalida=
		SELECT 0 AS "Id",'' AS "Tipo",'' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto",'' AS "NombreCorredor",'' AS "NombreAuditor" FROM DUMMY
		WHERE 2=1;
		
			
		IF p_Seccion='AUD' OR p_Seccion='' THEN
			v_Tipo:='AUDITOR';
			v_AUDITOR= SELECT 10 AS "Id",v_Tipo AS "Tipo",'% de la Custodia circularizado directamente por el Auditor Externo' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto",'CCC' AS "NombreCorredor",'JUAN SOTO' AS "NombreAuditor" FROM DUMMY;
			
			v_AUDITOR= SELECT 20 AS "Id",v_Tipo AS "Tipo",'% de la Custodia circularizado por el Corredor y supervisado por el Auditor Externo' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto",'' AS "NombreCorredor",'' AS "NombreAuditor" FROM DUMMY
			UNION ALL SELECT * FROM :v_AUDITOR;
			
			v_TabSalida= SELECT "Id", "Tipo","Titulo", "Cantidad", "PorcentajeCantidad","Monto", "PorcentajeMonto","NombreCorredor", "NombreAuditor"
 							FROM :v_AUDITOR
							UNION ALL SELECT * FROM :v_TabSalida;
			
		END IF;
			
		IF p_Seccion='CLI' OR p_Seccion='' THEN
			v_Tipo:='CLIENTES';

		---------------------------------------------------------------------------	
		------------			CALCULOS DE RESUMEN
		---------------------------------------------------------------------------				
			
			SELECT COUNT(1) INTO v_Count
				FROM 
				(		
					SELECT "IdCliente"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente"
					WHERE "Periodo"=p_Periodo
					AND "IdCliente" <> v_IdClienteCtaPropia
					GROUP BY "IdCliente"				
				);	
					
			IF v_Count>0 THEN
			
---Total de clientes con custodia
				SELECT COUNT(1) INTO v_totCliconCust
				FROM 
				(		
					SELECT "IdCliente"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente"
					WHERE "Periodo"=p_Periodo AND "ValorPresente" >0
					AND "IdCliente" <> v_IdClienteCtaPropia
					GROUP BY "IdCliente"				
				);	
				
				v_porCliconCust:= (v_totCliconCust * 100)/ v_Count;
				
				SELECT SUM("ValorPresente") INTO v_monCliconCust FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente"
				WHERE "Periodo"=p_Periodo AND "ValorPresente" >0 AND "IdCliente" <> v_IdClienteCtaPropia;
				


--Total de clientes notificados manualmente
				 v_tabCircularizaManual= SELECT NOM."IdCliente", NOM."IdPortafolio"	
										 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
											WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
											AND  NOM."FormaEnvio" ='MANUAL'
											AND  NOM."IdCliente" <> v_IdClienteCtaPropia;
											
				SELECT COUNT(1) INTO v_totClicirManual
				FROM 
				(		
					SELECT "IdCliente"FROM :v_tabCircularizaManual
					GROUP BY "IdCliente"				
				);	
				v_porClicirManual := (v_totClicirManual * 100)/ v_Count;
				
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monClicirManual 
				FROM :v_tabCircularizaManual NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
				
				v_PorMonClicirManual:= (v_monClicirManual * 100)/ v_monCliconCust;
				
--Total de clientes notificados internet	

				v_tabCircularizaInternet= SELECT NOM."IdCliente", NOM."IdPortafolio"	
										 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
											WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
											AND  NOM."FormaEnvio" ='INTERNET'
											AND  NOM."IdCliente" <> v_IdClienteCtaPropia;
											
				SELECT COUNT(1) INTO v_totClicirInternet
				FROM 
				(		
					SELECT "IdCliente"FROM :v_tabCircularizaInternet
					GROUP BY "IdCliente"				
				);	
				v_porClicirInternet := (v_totClicirInternet * 100)/ v_Count;
				
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monClicirInternet 
				FROM :v_tabCircularizaInternet NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
				
				v_PorMonClicirInternet:= (v_monClicirInternet * 100)/ v_monCliconCust;

--'  -Total de respuestas conformes internet'

				v_tabCircularizaInternetCON= SELECT NOM."IdCliente", NOM."IdPortafolio"	
										 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
											WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
											AND  NOM."FormaEnvio" ='INTERNET'
											AND NOM."IdEstadoRespuesta"='APD'
											AND  NOM."IdCliente" <> v_IdClienteCtaPropia;
											
				SELECT COUNT(1) INTO v_totClicirInternetCON
				FROM 
				(		
					SELECT "IdCliente"FROM :v_tabCircularizaInternetCON
					GROUP BY "IdCliente"				
				);	
				
				
				v_porClicirInternetCON := (v_totClicirInternetCON * 100)/ v_totClicirInternet;--v_Count;
			
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monClicirInternetCON 
				FROM :v_tabCircularizaInternetCON NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
			
				v_PorMonClicirInternetCON:= (v_monClicirInternetCON * 100)/ v_monCliconCust;
		
		

--'  -Total de respuestas con discrepancia internet (2)'

				v_tabCircularizaInternetDIS= SELECT NOM."IdCliente", NOM."IdPortafolio"	
										 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
											WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
											AND  NOM."FormaEnvio" ='INTERNET'
											AND NOM."IdEstadoRespuesta"='RZD'
											AND  NOM."IdCliente" <> v_IdClienteCtaPropia;
											
				SELECT COUNT(1) INTO v_totClicirInternetDIS
				FROM 
				(		
					SELECT "IdCliente"FROM :v_tabCircularizaInternetDIS
					GROUP BY "IdCliente"				
				);	
				
				
				v_porClicirInternetDIS := (v_totClicirInternetDIS * 100)/ v_totClicirInternet;--v_Count;
			
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monClicirInternetDIS 
				FROM :v_tabCircularizaInternetDIS NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
			
				v_PorMonClicirInternetDIS:= (v_monClicirInternetDIS * 100)/ v_monCliconCust;
		
--'  -Total de clientes sin respuesta internet'

				v_tabCircularizaInternetPEN= SELECT NOM."IdCliente", NOM."IdPortafolio"	
										 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
											WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
											AND  NOM."FormaEnvio" ='INTERNET'
											AND (NOM."IdEstadoRespuesta"='' OR NOM."IdEstadoRespuesta"='PEN')
											AND  NOM."IdCliente" <> v_IdClienteCtaPropia;
											
				SELECT COUNT(1) INTO v_totClicirInternetPEN
				FROM 
				(		
					SELECT "IdCliente"FROM :v_tabCircularizaInternetPEN
					GROUP BY "IdCliente"				
				);	
				
				
				v_porClicirInternetPEN := (v_totClicirInternetPEN * 100)/ v_totClicirInternet;--v_Count;
			
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monClicirInternetPEN 
				FROM :v_tabCircularizaInternetPEN NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
			
				v_PorMonClicirInternetPEN:= (v_monClicirInternetPEN * 100)/ v_monCliconCust;

--Total clientes no notificados	

			 v_tabNoCirculariza= SELECT NOM."IdCliente", NOM."IdPortafolio"
			  FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					WHERE NOM."Periodo"=p_Periodo AND NOM."Notificar" ='NO'
					AND NOM."IdCliente" <> v_IdClienteCtaPropia
					AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" N
									WHERE N."Periodo"=p_Periodo AND N."IdCliente"=NOM."IdCliente"AND  N."Notificar" ='SI' )
					GROUP BY NOM."IdCliente", NOM."IdPortafolio";	
					

				SELECT COUNT(1) INTO v_totCliNoCirc 
				FROM 
				(		
					SELECT "IdCliente" FROM :v_tabNoCirculariza
					GROUP BY "IdCliente"				
				);	
				
				v_porCliNoCirc:= (v_totCliNoCirc * 100)/ v_Count;
				
				SELECT coalesce(SUM(RES."ValorPresente"),0) INTO v_monCliNoCirc 
				FROM :v_tabNoCirculariza NOM
				INNER JOIN "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenCliente" RES
				ON (RES."IdCliente"=NOM."IdCliente" AND NOM."IdPortafolio"=RES."IdPortafolio" AND RES."Periodo"=p_Periodo )
				WHERE RES."Periodo"=p_Periodo  AND NOM."IdCliente" <> v_IdClienteCtaPropia;
				
				v_PorMonCliNoCirc:= (v_monCliNoCirc * 100)/ v_monCliconCust;		
				
		
		
			END IF;
	
		---------------------------------------------------------------------------	
		------------			SECCION REPORTE
		---------------------------------------------------------------------------				
			v_CLIENTES= SELECT 1010 AS "Id",v_Tipo AS "Tipo",'Total de clientes con custodia' AS "Titulo", v_totCliconCust AS "Cantidad", v_porCliconCust AS "PorcentajeCantidad", v_monCliconCust AS "Monto", (CASE  WHEN  v_totCliconCust> 0 THEN 100 ELSE 0 END ) AS "PorcentajeMonto" FROM DUMMY;				
			
				
			v_CLIENTES= SELECT 1020 AS "Id",v_Tipo AS "Tipo",'Total de clientes notificados manualmente' AS "Titulo", v_totClicirManual AS "Cantidad", v_porClicirManual AS "PorcentajeCantidad", v_monClicirManual AS "Monto", v_PorMonClicirManual AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			
			v_CLIENTES= SELECT 1030 AS "Id",v_Tipo AS "Tipo",'Total de clientes notificados internet' AS "Titulo", v_totClicirInternet AS "Cantidad", v_porClicirInternet AS "PorcentajeCantidad", v_monClicirInternet AS "Monto", v_PorMonClicirInternet AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			
			v_CLIENTES= SELECT 1031 AS "Id",v_Tipo AS "Tipo", '  -Total de respuestas conformes internet' AS "Titulo", v_totClicirInternetCON AS "Cantidad", v_porClicirInternetCON AS "PorcentajeCantidad", v_monClicirInternetCON AS "Monto", v_PorMonClicirInternetCON AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			v_CLIENTES= SELECT 1032 AS "Id",v_Tipo AS "Tipo", '  -Total de respuestas con discrepancia internet (2)' AS "Titulo", v_totClicirInternetDIS AS "Cantidad", v_porClicirInternetDIS AS "PorcentajeCantidad", v_monClicirInternetDIS AS "Monto", v_PorMonClicirInternetDIS AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			v_CLIENTES= SELECT 1033 AS "Id",v_Tipo AS "Tipo", '  -Total de clientes sin respuesta internet' AS "Titulo", v_totClicirInternetPEN AS "Cantidad", v_porClicirInternetPEN AS "PorcentajeCantidad", v_monClicirInternetPEN AS "Monto", v_PorMonClicirInternetPEN AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			

			v_CLIENTES= SELECT 1040 AS "Id",v_Tipo AS "Tipo",'Total clientes no notificados' AS "Titulo", v_totCliNoCirc AS "Cantidad", v_porCliNoCirc AS "PorcentajeCantidad", v_monCliNoCirc AS "Monto", v_PorMonCliNoCirc AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;	
	
			v_CLIENTES= SELECT 1041 AS "Id",v_Tipo AS "Tipo", '  -Cambio de domicilio de clientes' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			v_CLIENTES= SELECT 1042 AS "Id",v_Tipo AS "Tipo", '  -Error en los datos del domicilio' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;
			v_CLIENTES= SELECT 1043 AS "Id",v_Tipo AS "Tipo", '  -Error en los datos de contacto electrónico del cliente' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;	
			v_CLIENTES= SELECT 1044 AS "Id",v_Tipo AS "Tipo", '  -Cliente falleció' AS "Titulo", 0 AS "Cantidad", 0 AS "PorcentajeCantidad", 0 AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CLIENTES;	
			
			v_TabSalida= SELECT "Id", "Tipo","Titulo", "Cantidad", "PorcentajeCantidad","Monto", "PorcentajeMonto",'' AS "NombreCorredor",'' AS  "NombreAuditor"
 							FROM :v_CLIENTES
							UNION ALL SELECT * FROM :v_TabSalida;
							
							
		END IF;
		
		IF p_Seccion='COM' OR p_Seccion='' THEN
			v_Tipo:='COMPAÑIAS'; ---> RV

		---------------------------------------------------------------------------	
		------------			CALCULOS DE RESUMEN
		---------------------------------------------------------------------------		



			SELECT COUNT(1) INTO v_Count
				FROM 
				(		
					SELECT "IdEmisor"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo
					AND "EsCtaPropia" <> 'S'
					AND "IdClasificacionTipoInstrumento"='RV'
					GROUP BY "IdEmisor"				
				);	
					
			IF v_Count>0 THEN
			
			v_tablCompanias_NoCtaPropia= SELECT*
					FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo
					AND "EsCtaPropia" <> 'S'
					AND "IdClasificacionTipoInstrumento"='RV';
					
			
--'Total de compañias'
			
				SELECT COUNT(1) INTO v_totComconCust
				FROM 
				(		
					SELECT "IdEmisor"FROM :v_tablCompanias_NoCtaPropia
					--WHERE  "TotalValorPresente" >0
					GROUP BY "IdEmisor"				
				);	
				
				v_porComconCust:= (v_totComconCust * 100)/ v_Count;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monComconCust FROM :v_tablCompanias_NoCtaPropia WHERE  "TotalValorPresente" >0;
				
				
				
				
--'Total notificados conformes'				
				v_tabCOMConforme =SELECT COM.*
					 FROM :v_tablCompanias_NoCtaPropia COM
					INNER JOIN  "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					ON (NOM."IdCliente"=COM."IdCliente" AND NOM."IdPortafolio"	=COM."IdPortafolio"	)
					WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
					AND NOM."IdEstadoRespuesta"='APD';			
				
				
				SELECT COUNT(1) INTO v_totComCon
				FROM 
				(		
					SELECT COM."IdEmisor"FROM :v_tabCOMConforme COM
					GROUP BY COM."IdEmisor"				
				);	
				
				v_porComCon:= (v_totComCon * 100)/ v_totComconCust;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monComCon FROM :v_tabCOMConforme WHERE  "TotalValorPresente" >0;
						
				v_PorMonComCon:= (v_monComCon * 100)/ v_monComconCust;
				
--'Total notificados con discrepancias (7)'			
				v_tabCOMDiscrepa =SELECT COM.*
					 FROM :v_tablCompanias_NoCtaPropia COM
					INNER JOIN  "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					ON (NOM."IdCliente"=COM."IdCliente" AND NOM."IdPortafolio"	=COM."IdPortafolio"	)
					WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
					AND NOM."IdEstadoRespuesta"='RZD';			
				
				
				SELECT COUNT(1) INTO v_totComDIS
				FROM 
				(		
					SELECT COM."IdEmisor"FROM :v_tabCOMDiscrepa COM
					GROUP BY COM."IdEmisor"				
				);	
				
				v_porComDIS:= (v_totComDIS * 100)/ v_totComconCust;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monComDIS FROM :v_tabCOMDiscrepa WHERE  "TotalValorPresente" >0;
						
				v_PorMonComDIS:= (v_monComDIS * 100)/ v_monComconCust;
								
	--'Total de compañias sin respuesta'		
				v_tabCOMPendientes =SELECT COM.*
					 FROM :v_tablCompanias_NoCtaPropia COM
					INNER JOIN  "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					ON (NOM."IdCliente"=COM."IdCliente" AND NOM."IdPortafolio"	=COM."IdPortafolio"	)
					WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
					AND NOM."IdEstadoRespuesta"='PEN' OR NOM."IdEstadoRespuesta"='';			
				
				
				SELECT COUNT(1) INTO v_totComPEN
				FROM 
				(		
					SELECT COM."IdEmisor"FROM :v_tabCOMPendientes COM
					GROUP BY COM."IdEmisor"				
				);	
				
				v_porComPEN:= (v_totComPEN * 100)/ v_totComconCust;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monComPEN FROM :v_tabCOMPendientes WHERE  "TotalValorPresente" >0;
						
				v_PorMonComPEN:= (v_monComPEN * 100)/ v_monComconCust;
								
							
				
				
			END IF;

		---------------------------------------------------------------------------	
		------------			SECCION REPORTE
		---------------------------------------------------------------------------			
			v_COMPA= SELECT 2010 AS "Id",v_Tipo AS "Tipo",'Total de compañias' AS "Titulo", v_totComconCust AS "Cantidad", v_porComconCust AS "PorcentajeCantidad", v_monComconCust AS "Monto", (CASE  WHEN  v_totComconCust> 0 THEN 100 ELSE 0 END ) AS "PorcentajeMonto" FROM DUMMY;
			
			v_COMPA= SELECT 2020 AS "Id",v_Tipo AS "Tipo",'Total notificados conformes' AS "Titulo", v_totComCon AS "Cantidad", v_porComcon AS "PorcentajeCantidad", v_monComCon AS "Monto", v_PorMonComCon AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_COMPA;
			
			v_COMPA= SELECT 2030 AS "Id",v_Tipo AS "Tipo",'Total notificados con discrepancias (7)' AS "Titulo", v_totComDIS AS "Cantidad", v_porComDIS AS "PorcentajeCantidad", v_monComDIS AS "Monto", v_PorMonComDIS AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_COMPA;
					
			v_COMPA= SELECT 2040 AS "Id",v_Tipo AS "Tipo",'Total de compañias sin respuesta' AS "Titulo", v_totComPEN AS "Cantidad", v_porComPEN AS "PorcentajeCantidad", v_monComPEN AS "Monto", v_PorMonComPEN AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_COMPA;
			
			v_TabSalida= SELECT "Id", "Tipo","Titulo", "Cantidad", "PorcentajeCantidad","Monto", "PorcentajeMonto",'' AS "NombreCorredor",'' AS  "NombreAuditor"
 						FROM :v_COMPA
						UNION ALL SELECT * FROM :v_TabSalida;
			
		END IF;
		
		IF p_Seccion='INS' OR p_Seccion='' THEN
			v_Tipo:='INSTRUMENTOS'; ---NO RV
		---------------------------------------------------------------------------	
		------------			CALCULOS DE RESUMEN
		---------------------------------------------------------------------------		
			SELECT COUNT(1) INTO v_Count
				FROM 
				(		
					SELECT "IdInstrumento"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo
					AND "EsCtaPropia" <> 'S'
					AND "IdClasificacionTipoInstrumento"<>'RV'
					GROUP BY "IdInstrumento"				
				);	
					
			IF v_Count>0 THEN
			
			  v_tablInstrumentos_NoCtaPropia= SELECT *
			  FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo 
					AND "EsCtaPropia" <> 'S'
					AND "IdClasificacionTipoInstrumento"<>'RV';
					
--'Cantidad total de instrumentos'		
				SELECT COUNT(1) INTO v_totInsconCust
				FROM 
				(		
					SELECT "IdInstrumento"FROM :v_tablInstrumentos_NoCtaPropia
					--WHERE  "TotalValorPresente" >0
					GROUP BY "IdInstrumento"				
				);	
				
				v_porInsconCust:= (v_totInsconCust * 100)/ v_Count;
				
				SELECT SUM("TotalValorPresente") INTO v_monInsconCust FROM :v_tablInstrumentos_NoCtaPropia
				WHERE "TotalValorPresente" >0 ;
				
--'Cantidad total de instrumentos cuadrados consformes'

				v_tabINSConforme =SELECT COM.*
					 FROM :v_tablInstrumentos_NoCtaPropia COM
					INNER JOIN  "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					ON (NOM."IdCliente"=COM."IdCliente" AND NOM."IdPortafolio"	=COM."IdPortafolio"	)
					WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
					AND NOM."IdEstadoRespuesta"='APD';			
				
				
				SELECT COUNT(1) INTO v_totINSCon
				FROM 
				(		
					SELECT COM."IdInstrumento"FROM :v_tabINSConforme COM
					GROUP BY COM."IdInstrumento"				
				);	
				
				v_porINSCon:= (v_totINSCon * 100)/ v_totInsconCust;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monINSCon FROM :v_tabINSConforme WHERE  "TotalValorPresente" >0;
						
				v_PorMonINSCon:= (v_monINSCon * 100)/ v_monInsconCust;		
				
--'Cantidad total de instrumentos con discrepancias' 		

				v_tabINSDiscrepa =SELECT COM.*
					 FROM :v_tablInstrumentos_NoCtaPropia COM
					INNER JOIN  "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
					ON (NOM."IdCliente"=COM."IdCliente" AND NOM."IdPortafolio"	=COM."IdPortafolio"	)
					WHERE  NOM."Periodo"=p_Periodo AND NOM."Notificar" ='SI'
					AND NOM."IdEstadoRespuesta"='RZD';			
				
				
				SELECT COUNT(1) INTO v_totINSDIS
				FROM 
				(		
					SELECT COM."IdInstrumento"FROM :v_tabINSDiscrepa COM
					GROUP BY COM."IdInstrumento"				
				);	
				
				v_porINSDIS:= (v_totINSDIS * 100)/ v_totInsconCust;
				
				SELECT COALESCE(SUM("TotalValorPresente"),0) INTO v_monINSDIS FROM :v_tabINSDiscrepa WHERE  "TotalValorPresente" >0;
						
				v_PorMonINSDIS:= (v_monINSDIS * 100)/ v_monInsconCust;			
		
		
		
		
				
				
			END IF;

		---------------------------------------------------------------------------	
		------------			SECCION REPORTE
		---------------------------------------------------------------------------					
			v_INSTRUMENTOS= SELECT 3010 AS "Id",v_Tipo AS "Tipo",'Cantidad total de instrumentos' AS "Titulo", v_totInsconCust AS "Cantidad", v_porInsconCust AS "PorcentajeCantidad", v_monInsconCust AS "Monto", (CASE  WHEN v_totInsconCust  > 0 THEN 100 ELSE 0 END ) AS "PorcentajeMonto" FROM DUMMY;

			v_INSTRUMENTOS= SELECT 3020 AS "Id",v_Tipo AS "Tipo",'Cantidad total de instrumentos cuadrados consformes' AS "Titulo", v_totINSCon AS "Cantidad", v_porINSCon AS "PorcentajeCantidad", v_monINSCon AS "Monto", v_PorMonINSCon AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_INSTRUMENTOS;
			
			v_INSTRUMENTOS= SELECT 3030 AS "Id",v_Tipo AS "Tipo",'Cantidad total de instrumentos con discrepancias' AS "Titulo", v_totINSDIS AS "Cantidad", v_porINSDIS AS "PorcentajeCantidad", v_monINSDIS AS "Monto", v_PorMonINSDIS AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_INSTRUMENTOS;

			v_TabSalida= SELECT "Id", "Tipo","Titulo", "Cantidad", "PorcentajeCantidad","Monto", "PorcentajeMonto",'' AS "NombreCorredor",'' AS  "NombreAuditor"
 						FROM :v_INSTRUMENTOS
						UNION ALL SELECT * FROM :v_TabSalida;			
			
		END IF;	
		
		IF p_Seccion='CTP' OR p_Seccion='' THEN
			v_Tipo:='CTAPROPIA';
			
		---------------------------------------------------------------------------	
		------------			CALCULOS DE RESUMEN
		---------------------------------------------------------------------------		
			SELECT COUNT(1) INTO v_Count ---RV
				FROM 
				(		
					SELECT "IdInstrumento"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo
					AND "EsCtaPropia" = 'S'
					AND "IdClasificacionTipoInstrumento"='RV'
					GROUP BY "IdInstrumento"				
				);	
					
			IF v_Count>0 THEN
			
				SELECT COUNT(1) INTO v_totInsconCustCTA_RV
				FROM 
				(		
					SELECT "IdInstrumento"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo AND "TotalValorPresente" >0
					AND "EsCtaPropia" = 'S'
					AND "IdClasificacionTipoInstrumento"='RV'
					GROUP BY "IdInstrumento"				
				);	
				
				--v_porInsconCust:= (v_totInsconCust * 100)/ v_Count;
				
				SELECT SUM("TotalValorPresente") INTO v_monInsconCustCTA_RV FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
				WHERE "Periodo"=p_Periodo AND "TotalValorPresente" >0 AND "EsCtaPropia" = 'S' AND "IdClasificacionTipoInstrumento"='RV';
			END IF;

			SELECT COUNT(1) INTO v_Count ---RV
				FROM 
				(		
					SELECT "IdInstrumento"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo
					AND "EsCtaPropia" = 'S'
					AND "IdClasificacionTipoInstrumento"<>'RV'
					GROUP BY "IdInstrumento"				
				);	
					
			IF v_Count>0 THEN
			
				SELECT COUNT(1) INTO v_totInsconCustCTP
				FROM 
				(		
					SELECT "IdInstrumento"FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
					WHERE "Periodo"=p_Periodo AND "TotalValorPresente" >0
					AND "EsCtaPropia" = 'S'
					AND "IdClasificacionTipoInstrumento"<>'RV'
					GROUP BY "IdInstrumento"				
				);	
				
				--v_porInsconCust:= (v_totInsconCust * 100)/ v_Count;
				
				SELECT SUM("TotalValorPresente") INTO v_monInsconCustCTP FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarResumenInstrumento"
				WHERE "Periodo"=p_Periodo AND "TotalValorPresente" >0 AND "EsCtaPropia" = 'S' AND "IdClasificacionTipoInstrumento"<>'RV';
			END IF;

		---------------------------------------------------------------------------	
		------------			SECCION REPORTE
		---------------------------------------------------------------------------				
			

			v_CTAPROPIA= SELECT 4010 AS "Id",v_Tipo AS "Tipo",'Instrumentos de Renta Variable' AS "Titulo", v_totInsconCustCTA_RV AS "Cantidad", 0 AS "PorcentajeCantidad", v_monInsconCustCTA_RV AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY;
			
			v_CTAPROPIA= SELECT 4020 AS "Id",v_Tipo AS "Tipo",'Instrumentos de Renta Fija, Intermediación Financiera y Monetarios' AS "Titulo", v_totInsconCustCTP AS "Cantidad", 0 AS "PorcentajeCantidad", v_monInsconCustCTP AS "Monto", 0 AS "PorcentajeMonto" FROM DUMMY
			UNION ALL SELECT * FROM :v_CTAPROPIA;
			
			v_TabSalida= SELECT "Id", "Tipo","Titulo", "Cantidad", "PorcentajeCantidad","Monto", "PorcentajeMonto",'' AS "NombreCorredor",'' AS  "NombreAuditor"
 						FROM :v_CTAPROPIA
						UNION ALL SELECT * FROM :v_TabSalida;				
			
			
		END IF;	
		
			
		SELECT * FROM :v_TabSalida
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo 
		ORDER BY "Id" ASC;
		
END; 