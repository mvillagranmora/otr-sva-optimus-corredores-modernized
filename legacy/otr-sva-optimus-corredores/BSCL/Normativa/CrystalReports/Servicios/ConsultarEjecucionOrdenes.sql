SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::ConsultarEjecucionOrdenes"; 

 
CREATE PROCEDURE
--
--  Proposito: 
--  Autor:
--  Fecha Creacion: 
--
--call "BSCL.Normativa.CrystalReports.Servicios::ConsultarEjecucionOrdenes"(1,116323,'2015-02-21','2018-02-21') 
 
"BSCL.Normativa.CrystalReports.Servicios::ConsultarEjecucionOrdenes" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdCliente				BIGINT DEFAULT 0,
	IN p_FechaDesde				NVARCHAR(10) DEFAULT '0001-01-01',
	IN p_FechaHasta				NVARCHAR(10) DEFAULT '2099-01-01',
	IN p_IdPortafolio 			BIGINT DEFAULT 0
	
	
	
--	OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_CONSULTAR_ORDENES"
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_Count	 BIGINT:=0;
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_MensajeError		NVARCHAR(100) := '';
		DECLARE v_IdTipoDocumento   	NVARCHAR(10):=  'EJECORD';
		DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
		
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaDesde='' OR p_FechaDesde='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaDesde, 'DD-MM-YYYY');
		END IF;	
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
			
		
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
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
		
		
	v_Datos_Origen =SELECT "CEO".* 
						FROM "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes" AS "CEO"	
						WHERE TO_DATE(COALESCE("CEO"."FechaReferencia", "CEO"."FechaEjecucion")) BETWEEN TO_DATE(p_FechaDesde) AND TO_DATE(p_FechaHasta)
							AND "CEO"."IdCliente"    = (CASE WHEN p_IdCliente    = 0 THEN "CEO"."IdCliente"    ELSE p_IdCliente    END)
							AND "CEO"."IdPortafolio" = (CASE WHEN p_IdPortafolio = 0 THEN "CEO"."IdPortafolio" ELSE p_IdPortafolio END);
	
	
	
		
	SELECT COUNT(1) INTO v_Count FROM :v_Datos_Origen;

		IF v_Count > 0 THEN
				--VALORES
				--------------------
			v_tablaSalida=		
				SELECT 
				 "CEO"."IdEjecucion"						
				,"CEO"."IdDocumento" 
				,"CC"."NombreCompleto"					
				,COALESCE("CPDF"."NombreLargo",'-')			AS "DireccionFisica"
				,"CC"."DocumentoIdentidad" 					AS "RutCliente"
				,"CPT"."Numero" 							AS "Telefono"
				,"CPDE"."Direccion" 						AS "DireccionElectronica"
				,"CEO"."FolioGeneracion"					
				,"CEO"."IdOrden"							
				,"CEO"."Folio" 							
				,"CEO"."IdCliente" 						
				,"CEO"."IdPortafolio"
				,coalesce(POR."CodigoPortafolio",'') AS "CodigoPortafolio"
				,"CEO"."IdClasificacionTipoInstrumento" 	
				,"CEO"."IdTipoInstrumento"				
				,"CEO"."IdTipoOperacion"					
				,"CEO"."IdInstrumento" 					
				,"CEO"."Cantidad" 						
				,"CEO"."Monto" 							
				,"CEO"."MontoValorizado"					
				,"CEO"."Precio" 							
				,"CEO"."IdMoneda" 						
				,"CEO"."IdCondicionDeLiquidacion" 		
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaRecepcion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19), 'DD/MM/YYYY HH24:MI:SS')  			as  "FechaRecepcion"			
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaAceptacion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19),'DD/MM/YYYY HH24:MI:SS')			    as  "FechaAceptacion"	
				,"CEO"."TipoEjecucion"					
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaEjecucion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),10), 'DD-MM-YYYY')  	       			as "FechaEjecucion"				
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaEjecucion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19), 'DD-MM-YYYY HH24:MI:SS')  		    as "FechaHoraEjecucion"
				,"CEO"."IdBolsa" 
				,"CEO"."EsOD"
				,CASE TRIM("CEO"."EsOD") WHEN  'S' THEN 'OD' ELSE 'OS' END   	"Modalidad"						
				,"CEO"."PrecioEjecucion"					
				,"CEO"."MontoTransado" 					
				,"CEO"."CantidadTransada"				
				,"CEO"."ComisionTransada"		
				--OTRAS BOLSAS 		
				,CASE WHEN COALESCE("CEO"."MejorPrecioOtraBolsa",0) = 0 THEN '-/---'
				ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."MejorPrecioOtraBolsa",0),'d',2,'CL') || '/BEC'
				END AS "MejorPrecioOtraBolsa"			
				,"CEO"."CantidadMejorPrecioOtraBolsa" 	
				----MERCADO
				,CASE WHEN COALESCE("CEO"."HoraPrecioMinimoMercado",'')='' OR COALESCE("CEO"."PrecioMinimoMercado",0) = 0 THEN '-/--:--/---'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."PrecioMinimoMercado",0),'d',2,'CL')||'/'|| CASE WHEN COALESCE("CEO"."HoraPrecioMinimoMercado",'') ='' THEN '00:00' ELSE TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMinimoMercado"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI')  END ||'/'|| COALESCE("CEO"."IdBolsaPrecioMinimoMercado",'BCS')
				 END AS "PrecioMinHoraBolsaMcdo"
				
				,CASE WHEN COALESCE("CEO"."HoraPrecioMaximoMercado",'')='' OR COALESCE("CEO"."PrecioMaximoMercado",0) = 0 THEN '-/--:--/---'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."PrecioMaximoMercado",0),'d',2,'CL')||'/'|| CASE WHEN COALESCE("CEO"."HoraPrecioMaximoMercado",'') ='' THEN '00:00' ELSE TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMaximoMercado"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI')  END ||'/'|| COALESCE("CEO"."IdBolsaPrecioMaximoMercado",'BCS') 
				 END AS  "PrecioMaxHoraBolsaMcdo"
				 
				,"CEO"."MejorPrecioMercado"				
				,"CEO"."PrecioMinimoMercado"				
				,"CEO"."HoraPrecioMinimoMercado"			
				,"CEO"."IdBolsaPrecioMinimoMercado" 		
				,"CEO"."PrecioMaximoMercado"				
				,"CEO"."HoraPrecioMaximoMercado"			
				,"CEO"."IdBolsaPrecioMaximoMercado" 	
				---CORREDOR
				,CASE WHEN COALESCE("CEO"."HoraPrecioMenorCorredor",'')='' OR COALESCE("CEO"."MenorPrecioOperacionCorredor",0) = 0 THEN '-/--:--'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."MenorPrecioOperacionCorredor",0),'d',2,'CL') ||'/'||COALESCE(TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMenorCorredor"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI'),'00:00') 
				 END AS  "PrecioMinHoraBolsaCorr"
				 
				,CASE WHEN COALESCE("CEO"."HoraPrecioMayorCorredor",'')='' OR COALESCE("CEO"."MayorPrecioOperacionCorredor",0) = 0 THEN '-/--:--'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."MayorPrecioOperacionCorredor",0),'d',2,'CL') ||'/'||COALESCE(TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMayorCorredor"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI'),'00:00') 
				 END AS  "PrecioMaxHoraBolsaCorr"
				,"CEO"."MayorPrecioOperacionCorredor"	
				,"CEO"."HoraPrecioMayorCorredor"			
				,"CEO"."MenorPrecioOperacionCorredor"	
				,"CEO"."HoraPrecioMenorCorredor"			
				,"CEO"."IdEmpleadoDigitador"				
				,"CEO"."FolioComprobanteFacturacion"     
				,"CEO"."FechaGeneracion" 				
				,"CEO"."FolioExterno" 					
				,"CEO"."IdEstadoNotificacion"		 	
				,"CEO"."IdEstadoCarta"		 			
				,"CEO"."FechaIngreso"      				
				,"CEO"."FechaCreacion"     				
				,"CEO"."IdUsuarioCreador" 
				,EMP."NombreCompleto" AS "NombreAgente"
				,TIPOOPERACION."Accion" 						AS "AccionOperacion"
				,TERE."Numero" || '  '||TERE."Anexo" AS "TelefonoAgente"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI:SS')		AS "HoraSistemaLOCAL"
				,v_MensajeError	AS "MensajeError"
				,INS."Nemotecnico" as "Nemotecnico"
				,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("BEC"."PrecioMenor",0),'d',2,'CL')||'/'|| CASE WHEN COALESCE("BEC"."HoraPrecioMinimo",'') ='' THEN '00:00' ELSE TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"BEC"."HoraPrecioMinimo"),'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),'HH24:MI')   END ||'/'|| 'BEC'--COALESCE("CEO"."IdBolsaPrecioMinimoMercado",'BCS')
				AS "PrecioMinHoraBolsaBEC"
				,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("BEC"."PrecioMayor",0),'d',2,'CL')||'/'|| CASE WHEN COALESCE("BEC"."HoraPrecioMaximo",'') ='' THEN '00:00' ELSE TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"BEC"."HoraPrecioMaximo"),'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),'HH24:MI')  END ||'/'|| 'BEC'--COALESCE("CEO"."IdBolsaPrecioMaximoMercado",'BCS') 
				AS  "PrecioMaxHoraBolsaBEC"
				,COALESCE(RELNOR."RELACION",'SREL') AS "RelacionCorredor"
				,COALESCE("ORD"."Folio",'0') AS "FolioOrden"
				,"OrdenesUI"."InstrumentoNegociacion"	 
				FROM :v_Datos_Origen AS "CEO"	
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CC" ON ("CC"."IdCliente" = "CEO"."IdCliente")
				LEFT OUTER JOIN ---RELACIONADO
     			(SELECT COALESCE(RELHOM."ValorDestino" ,CAST (REL."CodigoValorIndicador" AS  nvarchar(100))) AS "RELACION",REL."IdCliente"
	     			 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
					 LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" RELHOM
		     			ON ((CAST (REL."CodigoValorIndicador" AS  nvarchar(100))= RELHOM."ValorOrigen" AND RELHOM."Tipo"='RELACION' AND RELHOM."Esquema"=v_Esquema AND RELHOM."Version"=v_Version))
				 	 WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
	     		) RELNOR ON (RELNOR."IdCliente" ="CEO"."IdCliente")
				
				LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP ON ("CC"."IdEmpleadoEjecutivo" =EMP."IdEmpleado")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TERE	ON (EMP."IdTelefonoPrincipal"=TERE."IdTelefono")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON ("CEO"."IdPortafolio"=POR."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "CPDF" ON ("CPDF"."IdDireccionFisica" = "CC"."IdDireccionFisicaPrincipal")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS "CPDE" ON ("CPDE"."IdDireccionElectronica" = "CC"."IdDireccionElectronicaPrincipal")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS "CPT" ON ("CPT"."IdTelefono" = "CC"."IdTelefonoPrincipal")
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = "CEO"."IdTipoOperacion"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON "CEO"."IdInstrumento" = INS."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" AS BEC ON "CEO"."IdInstrumento" = "BEC"."IdInstrumento" AND "BEC"."IdBolsa" = 'BEC' AND   TO_DATE("CEO"."FechaEjecucion") >=  "BEC"."Fecha" AND TO_DATE("CEO"."FechaEjecucion") <=  "BEC"."Fecha"
				LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "ORD" ON "CEO"."IdOrden" = "ORD"."IdOrden"
				LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" AS "OrdenesUI" ON "OrdenesUI"."IdOrden" = ORD."IdOrden";
	ELSE
	v_MensajeError := 'NO_EXISTEN_OPERACIONES';
	
		v_tablaSalida=
				SELECT 
				 0 AS  "IdEjecucion"						
				,0 AS "IdDocumento" 
				,'' AS "NombreCompleto"					
				,'' AS "DireccionFisica"
				,'' AS "RutCliente"
				,'' AS "Telefono"
				,'' AS "DireccionElectronica"
				,0 AS "FolioGeneracion"					
				,0 AS "IdOrden"							
				,'' AS "Folio" 							
				,0 AS "IdCliente" 						
				,0 AS "IdPortafolio"
				,'' AS "CodigoPortafolio"
				,'' AS "IdClasificacionTipoInstrumento" 	
				,'' AS "IdTipoInstrumento"				
				,'' AS "IdTipoOperacion"					
				,'' AS "IdInstrumento" 					
				,0 AS "Cantidad" 						
				,0 AS "Monto" 							
				,0 AS "MontoValorizado"					
				,0 AS "Precio" 							
				,'' AS "IdMoneda" 						
				,'' AS "IdCondicionDeLiquidacion" 		
				,'' AS "FechaRecepcion"			
				,'' AS "FechaAceptacion"	
				,'' AS "TipoEjecucion"					
				,'' AS "FechaEjecucion"		
				,'' AS "FechaHoraEjecucion"
				,'' AS "IdBolsa" 						
				,'' AS "EsOD"			
				,'' AS "Modalidad"
				,0 AS "PrecioEjecucion"					
				,0 AS "MontoTransado" 					
				,0 AS "CantidadTransada"				
				,0 AS "ComisionTransada"		
				,'' AS "MejorPrecioOtraBolsa"			
				,0 AS "CantidadMejorPrecioOtraBolsa" 	
				,'' AS "PrecioMinHoraBolsaMcdo"
				,'' AS  "PrecioMaxHoraBolsaMcdo"
				,0 AS "MejorPrecioMercado"				
				,0 AS "PrecioMinimoMercado"				
				,'' AS "HoraPrecioMinimoMercado"			
				,'' AS "IdBolsaPrecioMinimoMercado" 		
				,0 AS "PrecioMaximoMercado"				
				,'' AS "HoraPrecioMaximoMercado"			
				,'' AS "IdBolsaPrecioMaximoMercado" 	
				,'' AS "PrecioMinHoraBolsaCorr"
				,'' AS "PrecioMaxHoraBolsaCorr"
				,0 AS "MayorPrecioOperacionCorredor"	
				,0 AS "HoraPrecioMayorCorredor"			
				,0 AS "MenorPrecioOperacionCorredor"	
				,'' AS "HoraPrecioMenorCorredor"			
				,0 AS "IdEmpleadoDigitador"				
				,'' AS "FolioComprobanteFacturacion"     
				,'' AS "FechaGeneracion" 				
				,'' AS "FolioExterno" 					
				,'' AS "IdEstadoNotificacion"		 	
				,'' AS "IdEstadoCarta"		 			
				,'' AS "FechaIngreso"      				
				,'' AS "FechaCreacion"     				
				,0 AS "IdUsuarioCreador" 
				,'' AS "NombreAgente"
				,'' AS "AccionOperacion"
				,'' AS "TelefonoAgente"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI:SS')		AS "HoraSistemaLOCAL"
				,v_MensajeError	AS "MensajeError"
				,'' AS "Nemotecnico"
				,'' AS "PrecioMinHoraBolsaBEC"
				,'' AS "PrecioMaxHoraBolsaBEC"
				,'' AS "RelacionCorredor"
				,'' AS "FolioOrden"
				,'' AS "InstrumentoNegociacion"
			FROM DUMMY;	
		
		END IF;
		
SELECT * FROM :v_tablaSalida
	CROSS JOIN :v_datosGenerales
	CROSS JOIN :v_logo ORDER BY "IdOrden";	


END;


