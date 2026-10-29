SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::GenerarDatosFatca";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.FATCA::GenerarDatosFatca"
(
	IN p_IdEmpresa	BIGINT,
	IN p_SimularOperacion NVARCHAR(1),
	OUT p_out_Resultado TABLE(
							"IdPersona" 				BIGINT
							,"IdCliente" 				BIGINT
							,"DocumentoIdentidad" 		NVARCHAR(100)
							,"TipoPersona" 				NVARCHAR(3)
							,"IndicadorUSPerson"		NVARCHAR(1)
							,"ClasificacionFatca" 		NVARCHAR(100)
							,"ClasificacionFatcaFinal" 	NVARCHAR(100)
							,"IndicadorFatca" 			NVARCHAR(1)
							,"AutorizacionFatca" 		NVARCHAR(1)
							,"PatrimonioPeso" 			DECIMAL(26,8)
							,"PatrimonioDolar" 			DECIMAL(35,8)
							,"FechaEnrolamiento" 		NVARCHAR(10)
							,"FechaModificacion" 		NVARCHAR(10)
							,"FechaModificacionFatca" 	NVARCHAR(10)
							,"FechaNuevosIndicios" 		NVARCHAR(10)
							,"FlagNuevosIndicios" 		NVARCHAR(1)
							,"IndicadorResidencia" 		NVARCHAR(1)
							,"IndicadorNacimiento" 		NVARCHAR(1)
							,"IndicadorNacionalidad" 	NVARCHAR(1)
							,"IndicadorDomicilio" 		NVARCHAR(1)
							,"IndicadorTelefono" 		NVARCHAR(1)
							,"IndicadorOrdenes" 		NVARCHAR(1)
							,"IndicadorFirma" 			NVARCHAR(1)
							,"IndicadorBeneficiario" 	NVARCHAR(1)
						)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	DECLARE v_IdPaisUS BIGINT;
	DECLARE v_FechaProceso NVARCHAR(10);
	DECLARE v_Encendido NVARCHAR(10);
	DECLARE v_MontoPatrimonioPersonaNatural DECIMAL(26,8);
	DECLARE v_MontoPatrimonioPersonaJuridica DECIMAL(26,8);
	DECLARE v_MontoPatrimonioPersonaGlobal DECIMAL(26,8);
	DECLARE v_FechaEnrolamiento NVARCHAR(10);
	DECLARE v_BusquedaClientesNuevos NVARCHAR(10);
	DECLARE v_PorcentajeParticipacion DECIMAL(26,8);
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			DROP TABLE #CLIENTES_FATCA;
			DROP TABLE #CLIENTES_FATCA_RUT;
			RESIGNAL;
		END;
	BEGIN
		
		CREATE LOCAL TEMPORARY TABLE #CLIENTES_FATCA (
			"IdPersona" 					BIGINT
			,"IdCliente" 					BIGINT
			,"DocumentoIdentidad" 			NVARCHAR(100)
			,"TipoPersona" 					NVARCHAR(3)
			,"IndicadorUSPerson" 			NVARCHAR(1) DEFAULT '0'
			,"ClasificacionFatca" 			NVARCHAR(100)
			,"ClasificacionFatcaFinal" 		NVARCHAR(100)
			,"IndicadorFatca" 				NVARCHAR(1)
			,"AutorizacionFatca" 			NVARCHAR(1)
			,"PatrimonioPeso" 				DECIMAL(26,8) DEFAULT 0
			,"PatrimonioDolar" 				DECIMAL(35,8) DEFAULT 0
			,"FechaEnrolamiento" 			NVARCHAR(10)
			,"FechaModificacion"			NVARCHAR(10)
			,"FechaModificacionFatca" 		NVARCHAR(10)
			,"FechaNuevosIndicios" 			NVARCHAR(10)
			,"FlagNuevosIndicios" 			NVARCHAR(1)
			,"IndicadorResidencia" 			NVARCHAR(1) DEFAULT '0'
			,"IndicadorNacimiento" 			NVARCHAR(1) DEFAULT '0'
			,"IndicadorNacionalidad" 		NVARCHAR(1) DEFAULT '0'
			,"IndicadorDomicilio" 			NVARCHAR(1) DEFAULT '0'
			,"IndicadorTelefono" 			NVARCHAR(1) DEFAULT '0'
			,"IndicadorOrdenes" 			NVARCHAR(1) DEFAULT '0'
			,"IndicadorFirma" 				NVARCHAR(1) DEFAULT '0'
			,"IndicadorBeneficiario" 		NVARCHAR(1)
		);
		
		CREATE LOCAL TEMPORARY TABLE #CLIENTES_FATCA_RUT (
			"IdPersona" BIGINT
		);
		
		select Top 1 "Valor" into v_FechaProceso from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema' and "IdEmpresa" = p_IdEmpresa;
		
		select top 1 "IdPais" into v_IdPaisUS from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'US';
		
		/*
		 * SE BUSCAN LOS PARAMETROS FATCA NECESARIOS PARA EL PROCESO
		 */
		v_ParametosFatca = SELECT 
							CASE "CodigoParametro" WHEN 'FATCA.PatrimonioPersonaNatural' THEN "Valor" ELSE NULL END AS "PatrimonioPersonaNatural"
							,CASE "CodigoParametro" WHEN 'FATCA.PatrimonioPersonaJuridica' THEN "Valor" ELSE NULL END AS "PatrimonioPersonaJuridica"
							,CASE "CodigoParametro" WHEN 'FATCA.PatrimonioGlobal' THEN "Valor" ELSE NULL END AS "PatrimonioGlobal"
							,CASE "CodigoParametro" WHEN 'FATCA.FechaEnrolamiento' THEN "Valor" ELSE NULL END AS "FechaEnrolamiento"
							,CASE "CodigoParametro" WHEN 'FATCA.BusquedaClientesNuevos' THEN "Valor" ELSE NULL END AS "BusquedaClientesNuevos"
							,CASE "CodigoParametro" WHEN 'FATCA.PorcentajeBeneficiario' THEN "Valor" ELSE NULL END AS "PorcentajeBeneficiario"
							FROM "BSCL.Normativa.BD::NOR.Configuracion"
							WHERE "CodigoParametro" IN ('FATCA.PatrimonioPersonaNatural','FATCA.PatrimonioPersonaJuridica','FATCA.PatrimonioGlobal','FATCA.FechaEnrolamiento','FATCA.BusquedaClientesNuevos');
		
		v_ParametrosFatcaAgrupados = SELECT 
											 MAX("PatrimonioPersonaNatural") AS "PatrimonioPersonaNatural"
											,MAX("PatrimonioPersonaJuridica") AS "PatrimonioPersonaJuridica"
											,MAX("PatrimonioGlobal") AS "PatrimonioGlobal"
											,MAX("FechaEnrolamiento") AS "FechaEnrolamiento"
											,MAX("BusquedaClientesNuevos") AS "BusquedaClientesNuevos"
											,MAX("PorcentajeBeneficiario") AS "PorcentajeBeneficiario"
									FROM :v_ParametosFatca;
		
		SELECT 
			   TO_DECIMAL(COALESCE("PatrimonioPersonaNatural",0),26,8)
			   ,TO_DECIMAL(COALESCE("PatrimonioPersonaJuridica",0),26,8)
			   ,TO_DECIMAL(COALESCE("PatrimonioGlobal",0),26,8)
			   ,COALESCE("FechaEnrolamiento",'0001-01-01')
			   ,COALESCE("BusquedaClientesNuevos",'N')
			   ,TO_DECIMAL(COALESCE("PorcentajeBeneficiario",0),26,8)
		INTO v_MontoPatrimonioPersonaNatural,v_MontoPatrimonioPersonaJuridica,v_MontoPatrimonioPersonaGlobal,v_FechaEnrolamiento,v_BusquedaClientesNuevos,v_PorcentajeParticipacion
		FROM :v_ParametrosFatcaAgrupados;
		
		
		/*
		 * OBTENEMOS LOS CLIENTES
		 */
		
		v_clientesPortafolio = SELECT "IdCliente" ,"IdPortafolio" FROM "BSCL.Portafolio.BD::POR.Portafolio";
							  
		
		v_clientesFatca = SELECT CLI."IdPersona"
								 ,CLI."IdCliente"
								 ,IDENT."DocumentoIdentidad"
								 ,TPE."Descripcion" --NATURAL O JURIDICA viene de tipopersona 
								 ,FATCA."IdClasificacionFatca" --CLASIFICACION FATCA INICIAL
								 ,FATCA."IdClasificacionFatca" AS "IdClasificacionFatcaFinal" --CLASIFICACION FATCA FINAL
								 ,FATCA."AutorizacionInformarOrganismo" --AUTORIZACION INFORMAR ORGANISMO FATCA
								 ,TO_CHAR(CLI."FechaIngreso",'YYYY-MM-DD') AS "FechaIngresoCliente" --FECHA_INGRESO 
								 ,TO_CHAR(CLI."FechaModificacion",'YYYY-MM-DD') AS "FechaModificacionCliente" --FECHA MODIFICACION
								 ,TO_CHAR(FATCA."FechaModificacion",'YYYY-MM-DD') AS "FechaModificacionFatca" --FECHA MODIFICACION FATCA
								 -------------------------------------------
						  FROM "BSCL.SocioNegocio.BD::PER.Fatca" AS FATCA 
						  		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON FATCA."IdPersona" = CLI."IdPersona"
						  		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
						  		INNER JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" AS TPE ON PER."Tipo" = TPE."IdTipoPersona"
						  		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad";
						  
						
						 	

		/*
		 * SE CALCULA PATRIMONIO
		 */
		
		CALL "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
			p_IdModulo				=> 'NOR',
			p_IdUsuario				=> 0,
			p_IdEmpresa				=> :p_IdEmpresa,
			p_FechaDesde			=> :v_FechaProceso,
			p_FechaHasta			=> :v_FechaProceso,
			p_IdTrabajo				=> 1,
			p_Datos					=> :v_clientesPortafolio,
			p_out_ResumenInversion	=> v_ResumenInversion
		);
		
		v_TotalInversion =  SELECT "IdCliente","IdMonedaSistema",SUM("MontoMonedaSistema") as "total"
							FROM :v_ResumenInversion
							WHERE "Grupo" <> 'opl'
							GROUP BY "IdCliente","IdMonedaSistema";
				
				
		INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
		SELECT 2, "IdCliente", :v_FechaProceso, "IdMonedaSistema", 'USD', 0, "total",'CARTOLA' FROM :v_TotalInversion;
				
		CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
			p_IdEmpresa => :p_IdEmpresa,
			p_IdTrabajo => 2
		);
	


			
		v_ClientesFatcaPatrimonio = SELECT FATCA.*
										 ,TOTAL."total" AS "PatrimonioMonedaSistema"
										 ,TOTAL_USD."MontoDestino" AS "PatrimonioDolar"
									FROM :v_clientesFatca AS FATCA
									INNER JOIN :v_TotalInversion AS TOTAL ON FATCA."IdCliente" = TOTAL."IdCliente"
									INNER JOIN  "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS TOTAL_USD ON (FATCA."IdCliente" = TOTAL_USD."IdInterno" AND TOTAL_USD."IdTrabajo" = 2);
		
								
								
								
	v_TblIndicadorDomicilioFisico =
    SELECT        COUNT(1) AS "NroDomicilios", DIR."IdPersona", DIR."IdPais"
    FROM        "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR, :v_ClientesFatcaPatrimonio AS CLI
    WHERE        CLI."IdPersona" = DIR."IdPersona" AND  v_IdPaisUS =DIR."IdPais" 
    GROUP BY    DIR."IdPersona", DIR."IdPais";
   
    v_TblIndicadorTelefono =
    SELECT        COUNT(1) AS "NroTelefonos", TLF."IdPersona", TLF."IdPais"
    FROM        "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS TLF, :v_ClientesFatcaPatrimonio AS CLI
    WHERE        CLI."IdPersona" = TLF."IdPersona" AND TLF."IdPais" = v_IdPaisUS
    GROUP BY    TLF."IdPersona", TLF."IdPais";

	v_TblConsultarRelacion = 
	SELECT		COUNT(1) AS "Contador", CR."IdPersonaOrigen"
	FROM		"BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS CR , :v_ClientesFatcaPatrimonio AS CLI
	WHERE		CR."IdPersonaOrigen" = CLI."IdPersona" AND CR."IdTipoRelacion" = 'BENEMP' 
	AND	 		CR."PorcentajeParticipacion" > v_PorcentajeParticipacion
	GROUP BY	CR."IdPersonaOrigen";

	v_TblPersonaCtaBancaria = 
	SELECT		COUNT(1) AS "NroDeCuentas", CB."IdPersona" , CB."IdPaisOperacion" 
	FROM		"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CB, :v_ClientesFatcaPatrimonio AS CLI 
	WHERE		 CLI."IdPersona" = CB."IdPersona"	
	GROUP BY	 CB."IdPersona",  CB."IdPaisOperacion";


 V_tblFirma =  
	SELECT 	COUNT(1) AS "NroFirmas", CR."IdPersonaOrigen"
	FROM 	:v_ClientesFatcaPatrimonio AS CLI
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS CR ON CR."IdPersonaOrigen" = CLI."IdPersona"   
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" AS CRF ON (CR."IdPersonaDestino" = CRF."IdPersona")
			INNER JOIN "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS FPais	ON (FPais."IdPersona" = CRF."IdPersona" AND FPais."IdPaisResidencia" = v_IdPaisUS)
			INNER JOIN :v_TblIndicadorTelefono AS TLF ON CLI."IdPersona" = TLF."IdPersona" 
			INNER JOIN :v_TblIndicadorDomicilioFisico AS DIR ON  CLI."IdPersona" = DIR."IdPersona" 
			INNER JOIN :v_TblPersonaCtaBancaria AS CB ON  CLI."IdPersona" = CB."IdPersona" AND CB."IdPaisOperacion" = v_IdPaisUS
	WHERE 	(CRF."IdPaisResidencia" = v_IdPaisUS AND CRF."ExcepcionResidente" = 'N')
			OR (CRF."IdPaisNacimiento" = v_IdPaisUS AND CRF."ExcepcionNacimiento" = 'N')
			OR (CRF."ExcepcionDomicilio" = 'N' AND (DIR."NroDomicilios") > 1)--preguntar por como usar mas de 1
			OR (CRF."ExcepcionOrdenPermanente" = 'N' AND (CB."NroDeCuentas") > 1)
			OR (CRF."ExcepcionTelefono" = 'N' AND (TLF."NroTelefonos") > 1)
			--OR (v_IdPaisUS IN (CRF."IdPaisNacionalidad1",CRF."IdPaisNacionalidad2",CRF."IdPaisNacionalidad3",CRF."IdPaisNacionalidad4",CRF."IdPaisNacionalidad5"))
	GROUP BY CR."IdPersonaOrigen";
		
--------------------------------

		v_ClientesFatcaIndicios = SELECT DISTINCT CLI.*
										 ,COALESCE(IND."ValorIndicador",'N') AS "IndicadorFatca"
										 ,CASE WHEN FATCA."IdPaisResidencia" = v_IdPaisUS AND FATCA."ExcepcionResidente" = 'N' THEN '1'
										 	   WHEN FATCA."IdPaisResidencia" = v_IdPaisUS AND FATCA."ExcepcionResidente" = 'S' THEN 'E'
										 	   ELSE '0'
										  END AS "IndicadorResidencia"
										 ,CASE WHEN FATCA."IdPaisNacimiento" = v_IdPaisUS AND FATCA."ExcepcionNacimiento" = 'N' THEN '1'
										       WHEN FATCA."IdPaisNacimiento" = v_IdPaisUS AND FATCA."ExcepcionNacimiento" = 'S' THEN 'E'
										        ELSE '0'
										  END AS "IndicadorNacimiento"
										 /*,CASE WHEN v_IdPaisUS IN (FATCA."IdPaisNacionalidad1",FATCA."IdPaisNacionalidad2"
										 						  ,FATCA."IdPaisNacionalidad3",FATCA."IdPaisNacionalidad4"
										 						  ,FATCA."IdPaisNacionalidad5") THEN '1'
										  ELSE '0'
										  END AS "IndicadorNacionalidad"*/
										  , CASE WHEN COALESCE(FPais."IdPaisResidencia",1) > 1 THEN '1'
										  	ELSE '0'
										  	END AS "IndicadorNacionalidad"
										  ,CASE WHEN FATCA."ExcepcionDomicilio" = 'N' AND (DIR."NroDomicilios") > 0 THEN '1'
										       WHEN FATCA."ExcepcionDomicilio" = 'S' AND (DIR."NroDomicilios") > 0 THEN 'E'
										       ELSE '0'
										  END AS "IndicadorDomicilio"
										  ,CASE WHEN FATCA."ExcepcionTelefono" = 'N' AND (TLF."NroTelefonos") > 0 THEN '1'
										       WHEN FATCA."ExcepcionTelefono" = 'S' AND (TLF."NroTelefonos") > 0 THEN 'E'
										       ELSE '0'
										  END AS "IndicadorTelefono"
										  ,CASE WHEN FATCA."ExcepcionOrdenPermanente" = 'N' AND CB."NroDeCuentas" > 0 THEN '1'
										        WHEN FATCA."ExcepcionOrdenPermanente" = 'S' AND CB."NroDeCuentas" > 0 THEN 'E'
										       ELSE '0'
										  END AS "IndicadorOrdenes"
										  ,CASE WHEN FATCA."ExcepcionFirma" = 'N' AND  Firmas."NroFirmas" > 1 THEN '1'
										  		WHEN FATCA."ExcepcionFirma" = 'S' AND Firmas."NroFirmas" > 1 THEN 'E'
										 	   ELSE '0'
										 END AS "IndicadorFirma"
									
										 
										 ,CASE WHEN (CR."Contador") > 0 THEN '1'
										  ELSE '0'
										  END AS "IndicadorBeneficiario"
										  
										  
								  FROM :v_ClientesFatcaPatrimonio AS CLI
								  INNER JOIN :v_TblPersonaCtaBancaria AS CB ON  CLI."IdPersona" = CB."IdPersona" AND CB."IdPaisOperacion" = v_IdPaisUS
								  INNER JOIN :v_TblIndicadorTelefono AS TLF ON CLI."IdPersona" = TLF."IdPersona" 
								  INNER JOIN :v_TblIndicadorDomicilioFisico DIR ON  CLI."IdPersona" = DIR."IdPersona" 
								  INNER JOIN :v_TblConsultarRelacion CR ON CR."IdPersonaOrigen" = CLI."IdPersona" 
								  INNER JOIN :V_tblFirma Firmas ON CR."IdPersonaOrigen" = CLI."IdPersona"
					 			  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" FATCA ON (CLI."IdPersona" = FATCA."IdPersona")
					 			  LEFT  JOIN "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS FPais ON (FPais."IdPersona" = FATCA."IdPersona" AND FPais."IdPaisResidencia" = v_IdPaisUS)
					  			  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IND ON (CLI."IdCliente" = IND."IdCliente" AND IND."CodigoTipoIndicador" = 'DOC_FFATCA');
				
			v_UltimosDatosFatca = SELECT ROW_NUMBER() OVER (ORDER BY "FechaCargaInformacion" DESC) AS "ROWNUM"
										 ,"IdPersona"
										 ,"ind_nacimiento"
										 ,"ind_residencia"
										 ,"ind_nacionalidad"
										 ,"ind_direccion"
										 ,"ind_telefono"
										 ,"ind_ordenes"
										 ,"ind_firma"
										 ,"ind_benef_empresa"
										 ,"FechaCargaInformacion"
								  FROM "BSCL.Normativa.BD::NOR.DatosFatca";
			
			/*TIP: EN DATOS_FATCA
        	 *FECHA_ULT_MODIFICACION_FATCA -> FECHA ULTIMA MODIFICACION CLASIFICACION FATCA
        	 *FECHA_ULT_MODIFICACION -> FECHA ULTIMA MODIFICACION DE LOS INDICIOS
            */
			v_clientesFatcaFinal = SELECT CLIENTE.*
										  ,CASE WHEN '1' IN (CLIENTE."IndicadorNacimiento",CLIENTE."IndicadorResidencia",CLIENTE."IndicadorNacionalidad",CLIENTE."IndicadorDomicilio"
										  					,CLIENTE."IndicadorTelefono",CLIENTE."IndicadorOrdenes",CLIENTE."IndicadorFirma",CLIENTE."IndicadorBeneficiario") THEN '1'
										  		ELSE '0'
										   END AS "IndicadorUSPerson"
									 	  ,CASE WHEN (COALESCE(ULT."ind_nacimiento",'0') != CLIENTE."IndicadorNacimiento" AND CLIENTE."IndicadorNacimiento" = '1')
									 	             OR (COALESCE(ULT."ind_residencia",'0') != CLIENTE."IndicadorResidencia" AND CLIENTE."IndicadorResidencia" = '1')
									 	             OR (COALESCE(ULT."ind_nacionalidad",'0') != CLIENTE."IndicadorNacionalidad" AND CLIENTE."IndicadorNacionalidad" = '1')
									 	             OR (COALESCE(ULT."ind_direccion",'0') != CLIENTE."IndicadorDomicilio" AND CLIENTE."IndicadorDomicilio" = '1')
									 	             OR (COALESCE(ULT."ind_telefono",'0') != CLIENTE."IndicadorTelefono" AND CLIENTE."IndicadorTelefono" = '1')
									 	             OR (COALESCE(ULT."ind_ordenes",'0') != CLIENTE."IndicadorOrdenes" AND CLIENTE."IndicadorOrdenes" = '1')
									 	             OR (COALESCE(ULT."ind_firma",'0') != CLIENTE."IndicadorFirma" AND CLIENTE."IndicadorFirma" = '1')
									 	             OR (COALESCE(ULT."ind_benef_empresa",'0') != CLIENTE."IndicadorBeneficiario" AND CLIENTE."IndicadorBeneficiario" = '1')
									 	   THEN v_FechaProceso 
									 	   ELSE COALESCE(ULT."FechaCargaInformacion",'0001-01-01') END AS "FechaNuevosIndicios"
									 	   ,CASE WHEN (COALESCE(ULT."ind_nacimiento",'0') != CLIENTE."IndicadorNacimiento" AND CLIENTE."IndicadorNacimiento" = '1')
									 	             OR (COALESCE(ULT."ind_residencia",'0') != CLIENTE."IndicadorResidencia" AND CLIENTE."IndicadorResidencia" = '1')
									 	             OR (COALESCE(ULT."ind_nacionalidad",'0') != CLIENTE."IndicadorNacionalidad" AND CLIENTE."IndicadorNacionalidad" = '1')
									 	             OR (COALESCE(ULT."ind_direccion",'0') != CLIENTE."IndicadorDomicilio" AND CLIENTE."IndicadorDomicilio" = '1')
									 	             OR (COALESCE(ULT."ind_telefono",'0') != CLIENTE."IndicadorTelefono" AND CLIENTE."IndicadorTelefono" = '1')
									 	             OR (COALESCE(ULT."ind_ordenes",'0') != CLIENTE."IndicadorOrdenes" AND CLIENTE."IndicadorOrdenes" = '1')
									 	             OR (COALESCE(ULT."ind_firma",'0') != CLIENTE."IndicadorFirma" AND CLIENTE."IndicadorFirma" = '1')
									 	             OR (COALESCE(ULT."ind_benef_empresa",'0') != CLIENTE."IndicadorBeneficiario" AND CLIENTE."IndicadorBeneficiario" = '1')
									 	   THEN '1'
									 	   ELSE '0' END AS "FlagIndiciosNuevos"
								   FROM :v_ClientesFatcaIndicios CLIENTE
								   LEFT JOIN :v_UltimosDatosFatca ULT ON (CLIENTE."IdPersona" = ULT."IdPersona" AND ULT."ROWNUM" = 1);
		   
			INSERT INTO #CLIENTES_FATCA
			("IdPersona","IdCliente","DocumentoIdentidad","TipoPersona","IndicadorUSPerson","ClasificacionFatca","ClasificacionFatcaFinal","IndicadorFatca","AutorizacionFatca",
			"PatrimonioPeso","PatrimonioDolar","FechaEnrolamiento","FechaModificacion","FechaModificacionFatca","FechaNuevosIndicios","FlagNuevosIndicios",
			"IndicadorResidencia","IndicadorNacimiento","IndicadorNacionalidad","IndicadorDomicilio","IndicadorTelefono","IndicadorOrdenes","IndicadorFirma",
			"IndicadorBeneficiario")
			SELECT "IdPersona","IdCliente","DocumentoIdentidad","Descripcion","IndicadorUSPerson","IdClasificacionFatca","IdClasificacionFatcaFinal","IndicadorFatca","AutorizacionInformarOrganismo",
			"PatrimonioMonedaSistema","PatrimonioDolar","FechaIngresoCliente","FechaModificacionCliente","FechaModificacionFatca","FechaNuevosIndicios","FlagIndiciosNuevos",
			"IndicadorResidencia","IndicadorNacimiento","IndicadorNacionalidad","IndicadorDomicilio","IndicadorTelefono","IndicadorOrdenes","IndicadorFirma",
			"IndicadorBeneficiario"
			FROM :v_clientesFatcaFinal;

			--1 INDICADOR FATCA = S
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE CF."IndicadorFatca" = 'S'
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			-- 2 (CLIENTE NUEVO) Y (TIPO = US PERSON o US ENTITY o RECALCITRANTE)
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE "FechaEnrolamiento" > v_FechaEnrolamiento
			AND CF."ClasificacionFatca" IN ('RECALCITR','USENTITY','US_PERSON')
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			-- 3 (CLIENTE ANTIGUO) Y (TIPO US PERSON o US ENTITY o RECALCITRANTE o PENDIENTE)
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE (("FechaEnrolamiento" < v_FechaEnrolamiento) OR (v_BusquedaClientesNuevos = 'S' AND "FechaEnrolamiento" >= v_FechaEnrolamiento))
			AND CF."ClasificacionFatca" IN ('RECALCITR','USENTITY','US_PERSON','PENDIENTE')
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			
			/* 4 (CLIENTE ANTIGUO) Y (TIPO = NON US) Y (PATRIMONIO GLOBAL) Y (NUNCA HA SIDO CLASIFICADO)
			 * PRIMERO SE MARCAN COMO PENDIENTES
			 */
			
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE "FechaEnrolamiento" < v_FechaEnrolamiento
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaGlobal
			AND COALESCE("FechaModificacionFatca",'0001-01-01') = '0001-01-01'
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			UPDATE CLIENTES_FATCA
			SET "ClasificacionFatcaFinal" = 'PENDIENTE'
			FROM #CLIENTES_FATCA CLIENTES_FATCA
			WHERE "FechaEnrolamiento" < v_FechaEnrolamiento
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaGlobal
			AND COALESCE("FechaModificacionFatca",'0001-01-01') = '0001-01-01';
						
			/* 5 (CLIENTE ANTIGUO) Y (TIPO = NON US) Y (PATRIMONIO POR TIPO DE PERSONA) Y (FECHA CAMBIOS INDICIOS > FECHA ULTIMA CLASIFICACION FATCA)
			*/
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE "FechaEnrolamiento" < v_FechaEnrolamiento
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND (("TipoPersona" = 'N' AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaNatural) OR ("TipoPersona" = 'J' AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaJuridica))
			AND "FlagNuevosIndicios" = '1'
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			UPDATE CLIENTES_FATCA
			SET "ClasificacionFatcaFinal" = 'PENDIENTE'
			FROM #CLIENTES_FATCA CLIENTES_FATCA
			WHERE "FechaEnrolamiento" < v_FechaEnrolamiento
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND (("TipoPersona" = 'N' AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaNatural) OR ("TipoPersona" = 'J' AND "PatrimonioDolar" >= v_MontoPatrimonioPersonaJuridica))
			AND "FlagNuevosIndicios" = '1';
			
			
			/* 6 (PARAMETRO ENCENDIDO) Y (CLIENTE NUEVO) Y (TIPO = NON US) Y (FECHA CAMBIOS INDICIOS > FECHA ULTIMA CLASIFICACION FATCA)
			*/
			
			INSERT INTO #CLIENTES_FATCA_RUT
			SELECT "IdPersona"
			FROM #CLIENTES_FATCA CF
			WHERE (v_BusquedaClientesNuevos = 'S' AND "FechaEnrolamiento" >= v_FechaEnrolamiento)
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND "FlagNuevosIndicios" = '1'
			AND NOT EXISTS(SELECT 1 FROM #CLIENTES_FATCA_RUT CFR WHERE CFR."IdPersona" = CF."IdPersona");
			
			UPDATE CLIENTES_FATCA
			SET "ClasificacionFatcaFinal" = 'PENDIENTE'
			FROM #CLIENTES_FATCA CLIENTES_FATCA
			WHERE (v_BusquedaClientesNuevos = 'S' AND "FechaEnrolamiento" >= v_FechaEnrolamiento)
			AND "ClasificacionFatca" = 'NOUSENTITY'
			AND "FlagNuevosIndicios" = '1';
			
			/*
			 * SE INSERTAN LOS REGISTROS
			 */
			IF(p_SimularOperacion = 'N') THEN
				INSERT INTO "BSCL.Normativa.BD::NOR.DatosFatca"
				("IdDatosFatca","IdPersona","IdCliente"	,"rut_cli","tipo_persona","ind_us_person","clasificacion_fatca","indicador_fatca","auth_fatca"                    
				 ,"patrimonio_peso","patrimonio_dolar","fecha_enrolamiento","fecha_ult_modificacion","fecha_ult_modificacion_fatca","ind_residencia"                
				 ,"ind_nacimiento","ind_nacionalidad","ind_direccion","ind_telefono","ind_ordenes","ind_firma","ind_benef_empresa","fecha_nuevos_indicios"
				 ,"Estado","concatenados","FechaCargaInformacion")
				SELECT "BSCL.Normativa.BD.Secuencias::DatosFatca".NEXTVAL,CF."IdPersona",CF."IdCliente",CF."DocumentoIdentidad",CF."TipoPersona",
				       CF."IndicadorUSPerson",CF."ClasificacionFatcaFinal",CF."IndicadorFatca",CF."AutorizacionFatca",CF."PatrimonioPeso",CF."PatrimonioDolar",
				       CF."FechaEnrolamiento",CF."FechaModificacion",CF."FechaModificacionFatca",CF."IndicadorResidencia",CF."IndicadorNacimiento",CF."IndicadorNacionalidad",
				       CF."IndicadorDomicilio",CF."IndicadorTelefono",CF."IndicadorOrdenes",CF."IndicadorFirma",CF."IndicadorBeneficiario",CF."FechaNuevosIndicios",'V'
				       ,CF."IndicadorResidencia"||CF."IndicadorNacimiento"||CF."IndicadorNacionalidad"||CF."IndicadorDomicilio"||CF."IndicadorTelefono"||CF."IndicadorOrdenes"||CF."IndicadorFirma"||CF."IndicadorBeneficiario"
				       ,v_FechaProceso
				FROM #CLIENTES_FATCA_RUT CFR
				INNER JOIN #CLIENTES_FATCA CF ON (CF."IdPersona" = CFR."IdPersona");
				
				UPDATE FATCA
				SET "IdClasificacionFatca" = CF."ClasificacionFatcaFinal"
				FROM "BSCL.SocioNegocio.BD::PER.Fatca" FATCA
				INNER JOIN #CLIENTES_FATCA_RUT CFR ON (CFR."IdPersona" = FATCA."IdPersona")
				INNER JOIN #CLIENTES_FATCA CF ON (CF."IdPersona" = CFR."IdPersona")
				WHERE CF."ClasificacionFatcaFinal" = 'PENDIENTE';
			END IF;
			
			p_out_Resultado = SELECT CF."IdPersona"
									 ,CF."IdCliente"
									 ,CF."DocumentoIdentidad"
									 ,CF."TipoPersona"
									 ,CF."IndicadorUSPerson"
									 ,CF."ClasificacionFatca"
									 ,CF."ClasificacionFatcaFinal"
									 ,CF."IndicadorFatca"
									 ,CF."AutorizacionFatca"
									 ,CF."PatrimonioPeso"
									 ,CF."PatrimonioDolar"
									 ,CF."FechaEnrolamiento"
									 ,CF."FechaModificacion"
									 ,CF."FechaModificacionFatca"
									 ,CF."FechaNuevosIndicios"
									 ,CF."FlagNuevosIndicios"
									 ,CF."IndicadorResidencia"
									 ,CF."IndicadorNacimiento"
									 ,CF."IndicadorNacionalidad"
									 ,CF."IndicadorDomicilio"
									 ,CF."IndicadorTelefono"
									 ,CF."IndicadorOrdenes"
									 ,CF."IndicadorFirma"
									 ,CF."IndicadorBeneficiario"
							FROM #CLIENTES_FATCA_RUT CFR
							INNER JOIN #CLIENTES_FATCA CF ON (CF."IdPersona" = CFR."IdPersona");
			
			DROP TABLE #CLIENTES_FATCA;
			DROP TABLE #CLIENTES_FATCA_RUT;
	END;
	
END;
