SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::TRA.ConsultarTransacciones";

CREATE PROCEDURE
--
--  Proposito: Servicio para consultar información de transacciones por Oficio para SVS(SEIL) o BCS.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 19/01/2017

 "BSCL.Normativa.BD.Servicios::TRA.ConsultarTransacciones" (
 	
 	--VARIABLES DE ENTRADA OBLIGATORIAS
 	IN p_IdEmpresa			BIGINT,
 	
 	--VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde			NVARCHAR(10) 	DEFAULT '',
	IN p_FechaOperacionHasta			NVARCHAR(10) 	DEFAULT '',
	IN p_IdCliente 						BIGINT 			DEFAULT '0',
	IN p_Nemotecnico 					NVARCHAR(40)	DEFAULT '',
	
	IN p_IdTrabajoParametros			BIGINT DEFAULT 0, --Se asumen valores por defecto en los parametros
	IN p_paramAnulacionPorNC			NVARCHAR(2)	DEFAULT '',

	-- VARIABLES DE SALIDA
	OUT p_out_IdInfoTranOfic				BIGINT 
--	OUT p_out_TablaSalida				"BSCL.Normativa.BD::NOR.SALIDA_INFOTRANOFIC"
 )
 
 	LANGUAGE SQLSCRIPT
 	SQL SECURITY INVOKER
 	DEFAULT SCHEMA "BSCL"
 AS
 	BEGIN
 		--------------------
		--Declare Variables
		--------------------
 		DECLARE v_IdNormativa			NVARCHAR(15):= 'INFOTRANOFIC';
 		DECLARE v_IdTipoDocumento   	NVARCHAR(10):=  'INFTRASEIL';
 		DECLARE v_FechaOperacionDesde	NVARCHAR(10):= '';
 		DECLARE v_FechaOperacionHasta 	NVARCHAR(10):= '';
 		DECLARE v_IdPersonaCtaPropia	BIGINT:= 0;
 		DECLARE v_Count,v_IdInfoTranOfic					 BIGINT:= 0;
 		DECLARE v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  	 NVARCHAR(40):= '';
 		DECLARE	v_IdClienteCtaPropia 						 BIGINT:=0;
 		DECLARE v_DocumentoIdentidadCtaPropia  				 NVARCHAR(40) DEFAULT '';
 		DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
 		DECLARE v_out_error 								 NVARCHAR(500);
 		
 		v_FechaOperacionDesde := p_FechaOperacionDesde;
  		v_FechaOperacionHasta := p_FechaOperacionHasta;  	
  		
		--INICIO: DEFINICION GENERAL PARAMETROS MASIVOS  		
  		--PARAMETROS MASIVOS TABLA GLOBAL
		v_ParamClientes			=SELECT	0 	AS "IdCliente"		FROM DUMMY WHERE 1=2;
		v_ParamNemos			=SELECT	'' 	AS "Nemo" 			FROM DUMMY WHERE 1=2;
		v_ParamClasificacion	=SELECT	'' 	AS "Clasificacion"	FROM DUMMY WHERE 1=2;
		
		----Obtener parametros Tabla Global
		SELECT  COUNt(1) INTO v_Count FROM "BSCL.Normativa.BD::GBL.Parametros" 
		WHERE   "IdInterno"=p_IdTrabajoParametros;
		
		IF v_Count > 0 THEN
		
			v_GBLParametros=SELECT "IdInterno","Tipo","NombreParametro" AS "TipoParametro","Valor"   FROM "BSCL.Normativa.BD::GBL.Parametros" WHERE  "IdInterno"=p_IdTrabajoParametros;
			
			--Opcion Todos
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='TODOS_CLI'		THEN "Valor" ELSE NULL END AS "TODOS_CLI"
				, CASE WHEN SOL."TipoParametro"='TODOS_NEMO'	THEN "Valor" ELSE NULL END AS "TODOS_NEMO"
				, CASE WHEN SOL."TipoParametro"='TODOS_CLAINS'	THEN "Valor" ELSE NULL END AS "TODOS_CLAINS"
			FROM :v_GBLParametros SOL WHERE  SOL."TipoParametro" IN ('TODOS_CLI','TODOS_NEMO','TODOS_CLAINS');
		 	
			v_DatosAgrupado =SELECT  
					  MAX(SOL."TODOS_CLI")		AS "TODOS_CLI"
					, MAX(SOL."TODOS_NEMO")		AS "TODOS_NEMO"
					, MAX(SOL."TODOS_CLAINS")	AS "TODOS_CLAINS"															
					FROM :v_Datos SOL;
			SELECT TOP 1 "TODOS_CLI","TODOS_NEMO","TODOS_CLAINS" INTO v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  FROM :v_DatosAgrupado;
			
			--Masivos
			v_ParamClientes= SELECT CLI."IdCliente" FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
									  INNER JOIN (SELECT DISTINCT "Valor" AS "RUT" FROM :v_GBLParametros
									  			WHERE "TipoParametro"='RUT_MAS'
									  		     ) AS PAR
									  ON (RTRIM(PAR."RUT") = CLI."DocumentoIdentidad")
									  WHERE CLI."TipoDocumentoIdentidad"='R' AND CLI."IdEstado"='ACT';
		
			v_ParamNemos 			=  SELECT "Valor" AS "Nemo" 		 FROM :v_GBLParametros	WHERE "TipoParametro"='NEMO_MAS';
			v_ParamClasificacion 	=  SELECT "Valor" AS "Clasificacion" FROM :v_GBLParametros	WHERE "TipoParametro"='CLAINS_MAS';
			
			
			
		
		ELSE
			---valores por defecto
			v_ParamClientes			=SELECT	NULL AS "IdCliente"		 FROM  DUMMY;
			v_ParamNemos			=SELECT	NULL AS "Nemo"	 		 FROM  DUMMY;
			v_ParamClasificacion	=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
		
		END IF;
		--FIN PARAMETROS MASIVOS TABLA GLOBAL

		---DEFINIR: TABLA DE BUSQUEDA
			IF p_IdCliente > 0 THEN ---CLIENTE
				v_ParamClientes=SELECT	p_IdCliente AS "IdCliente"
							FROM  DUMMY
							UNION ALL SELECT "IdCliente" FROM :v_ParamClientes WHERE "IdCliente" IS NOT NULL;
			ELSE
				IF v_TODOS_CLI	='S' THEN
					v_ParamClientes=SELECT	NULL AS "IdCliente"	 FROM  DUMMY;
				END IF;		
			END IF ;
			
			IF p_Nemotecnico <>'' THEN --
					v_ParamNemos=SELECT	p_Nemotecnico AS "Nemo"
							FROM  DUMMY
							UNION ALL SELECT "Nemo" FROM :v_ParamNemos WHERE "Nemo" IS NOT NULL;
			ELSE				
					IF v_TODOS_NEMO	='S' THEN
							v_ParamNemos=SELECT	NULL AS "Nemo"	 FROM  DUMMY;
					END IF;			
			END IF;
			
			IF v_TODOS_CLAINS	='S' THEN
				--v_ParamClasificacion=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
				--para este tipo documento se acota el todos solo para algunos mercados
				v_ParamClasificacion=  SELECT	'RV' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'RF' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'IF' AS "Clasificacion"	 FROM  DUMMY							
							UNION ALL  SELECT	'SP' AS "Clasificacion"	 FROM  DUMMY;														
			END IF;	
			
			
		---FIN DEFINIR: TABLA DE BUSQUEDA

		--TERMINO: DEFINICION GENERAL PARAMETROS MASIVOS  		


		---- Transacciones desde servicionNormativo de Operaciones
		v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
		
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;
		
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_TablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,
				p_paramAnulacionNC  => p_paramAnulacionPorNC,
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);

		v_tablaComprobantes = SELECT "IdComprobanteOperacion","IdComprobanteReferencia","IdTipoOperacion" FROM :v_SalidaConsultarOperaciones;

		v_tablaOperacionesFiltradas  = SELECT tOperaciones.*, 1 AS "nroOperacion" FROM :v_SalidaConsultarOperaciones tOperaciones
							  LEFT JOIN (
									SELECT tFac."IdComprobanteOperacion" AS "IdComprobanteFact"
									FROM :v_SalidaConsultarOperaciones AS tFac
									INNER JOIN (
										SELECT tCompr."IdComprobanteReferencia"
										FROM :v_tablaComprobantes AS tCompr
										INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP ON COMP."IdComprobante" = tCompr."IdComprobanteReferencia" AND COMP."IdEstadoComprobante" <> 'ANU'
										WHERE (NOT tCompr."IdComprobanteReferencia" = 0) AND (tCompr."IdComprobanteReferencia" IS NOT NULL)
									) tCompbte ON tFac."IdComprobanteOperacion" = tCompbte."IdComprobanteReferencia"
							  ) tRefF ON tOperaciones."IdComprobanteOperacion" = tRefF."IdComprobanteFact"
							  LEFT JOIN (
									SELECT tNC."IdComprobanteOperacion"
									FROM :v_tablaComprobantes AS tNC
									INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP ON COMP."IdComprobante" = tNC."IdComprobanteReferencia" AND COMP."IdEstadoComprobante" <> 'ANU'
									WHERE (NOT tNC."IdComprobanteOperacion" = 0) AND (NOT tNC."IdComprobanteReferencia" = 0) AND (tNC."IdComprobanteReferencia" IS NOT NULL)
							  ) tRefNC ON tOperaciones."IdComprobanteOperacion" = tRefNC."IdComprobanteOperacion"
							  WHERE(
										tRefNC."IdComprobanteOperacion" IS NULL AND tRefF."IdComprobanteFact" IS NULL
							  );

		v_tablaSimultaneasAnticipadas = SELECT  tOperaciones.*,
							ROW_NUMBER() OVER (PARTITION BY tOperaciones."IdComprobanteOperacion" ORDER BY tOperaciones."IdComprobanteOperacion" DESC) AS "nroOperacion" 
							FROM :v_SalidaConsultarOperaciones tOperaciones 
							INNER JOIN (
									SELECT relSim."IdComprobanteOperacion", 'S' AS "ConcideraSimultanea"
									FROM :v_tablaComprobantes AS relSim
									INNER JOIN :v_tablaComprobantes AS  comprSim
										ON (
											(comprSim."IdTipoOperacion" = 'ANSIMC' AND relSim."IdTipoOperacion" = 'COMSIM' )
											OR
											(comprSim."IdTipoOperacion" = 'ANSIMV' AND relSim."IdTipoOperacion" = 'VENSIM')
										)
										AND comprSim."IdComprobanteReferencia" = relSim."IdComprobanteOperacion" 
							) tSimul ON tOperaciones."IdComprobanteOperacion" = tSimul."IdComprobanteOperacion"
							WHERE (
									tSimul."ConcideraSimultanea" = 'S'
									AND tOperaciones."IdTipoOperacion" IN ('COMSIM', 'VENSIM') 
							)
							UNION ALL
							SELECT  tOperaciones.*,
							ROW_NUMBER() OVER (PARTITION BY tOperaciones."IdComprobanteOperacion" ORDER BY tOperaciones."IdComprobanteOperacion" DESC) AS "nroOperacion"
							FROM :v_SalidaConsultarOperaciones tOperaciones 
							INNER JOIN ( SELECT relSim2."IdComprobanteOperacion", 'S' AS "ConcideraSimultanea" FROM :v_tablaComprobantes AS relSim2
									LEFT JOIN :v_tablaComprobantes AS  comprSim ON comprSim."IdComprobanteOperacion" = relSim2."IdComprobanteReferencia"
									INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS  comprSim2 
									    ON (
											(comprSim2."IdTipoOperacion" = 'ANSIMC' AND relSim2."IdTipoOperacion" = 'COMSIM' )
											OR
											(comprSim2."IdTipoOperacion" = 'ANSIMV' AND relSim2."IdTipoOperacion" = 'VENSIM')
										)
										AND comprSim2."IdComprobante" = CASE WHEN comprSim."IdComprobanteOperacion" IS NULL THEN relSim2."IdComprobanteReferencia" ELSE -0 END
										AND comprSim2."IdEstadoComprobante" <> 'ANU'   
							) tSimul2 ON tOperaciones."IdComprobanteOperacion" = tSimul2."IdComprobanteOperacion"
							WHERE (
									tOperaciones."IdTipoOperacion" IN ('COMSIM', 'VENSIM') 
							);

		v_tablaUnion = SELECT * FROM :v_tablaOperacionesFiltradas
					   UNION ALL
					   SELECT * FROM :v_tablaSimultaneasAnticipadas WHERE "nroOperacion" = 1; -- Simultaneas anticipadas Multiples veces
						
		v_tablaOperaciones = SELECT OPE.* FROM :v_tablaUnion OPE
		INNER JOIN :v_ParamClientes 		CLI ON (OPE."IdCliente"=CLI."IdCliente" OR CLI."IdCliente" IS NULL)
		INNER JOIN :v_ParamNemos    		NEM ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
		INNER JOIN :v_ParamClasificacion    CLA ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL);

	
			---Valores de la cuenta propia	
		SELECT TOP 1 CLI."IdCliente", CLI."DocumentoIdentidad" INTO v_IdClienteCtaPropia,v_DocumentoIdentidadCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
		--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = p_IdEmpresa
		AND CLI."IdEstado"='ACT';
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaOperacionDesde='' OR p_FechaOperacionDesde='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaOperacionDesde, 'DD-MM-YYYY');
		END IF;	
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		
		
		
			
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN	
					SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdInfoTranOfic FROM DUMMY;
					
					--p_out_TablaSalida = 
					INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC" ("IdInterno","RutIntermediario" ,"FechaTransaccionSistema","Mercado","SistemaTransaccion","Bolsa","Folio","RutContraparte"
					,"NumeroOrden","Nemotecnico","FechaVencimientoSistema"	,"IdTipoInstrumento","HIdTipoInstrumento","IdTipoOperacion","HIdTipoOperacion","Persona","Plazo","IdCondicionLiquidacion","TipoCliente","UnidadesTransadasSistema"
					,"PrecioTransaccionSistema","TotalTransadoSistema","TipoUnidad"	,"ValorParSistema"	,"TIRSistema","RentabilidadSistema","NacionalidadCliente","RutCliente"
					,"NombreCliente","NumeroFactura","RutAutorizado","NombreAutorizado","IdClasificacionTipoInstrumento", "CodigoOperador")
					Select 
						v_IdInfoTranOfic AS "IdInterno",
						LTRIM(v_DocumentoIdentidadCtaPropia) as "RutIntermediario",
						--LEFT ( LTRIM(v_DocumentoIdentidadCtaPropia),(LOCATE (LTRIM(v_DocumentoIdentidadCtaPropia),'-') - 1) ) as "RutIntermediario",
						--RIGHT ( LTRIM(v_DocumentoIdentidadCtaPropia) ,1) as "DVIntermediario",
						OPE."FechaTransaccion" AS "FechaTransaccionSistema",
						(CASE OPE."EsEnRueda" WHEN 'S' THEN 'R'    ELSE'F'	 END) AS "Mercado", 
						COALESCE(SISTE."ValorDestino",'FRUE')	 AS  "SistemaTransaccion",	--Homologacion
						COALESCE(BOLSA."ValorDestino",'00') 	 AS  "Bolsa",				--Homologacion
						OPE."FolioTransaccion" AS "Folio",
						COALESCE(CNC."DocumentoIdentidad",'') 		AS "RutContraparte", --Intermediario Contraparte
						OPE."IdOrden" 			AS "NumeroOrden",
		                CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'IF' THEN TRA."NemotecnicoSVS" ELSE OPE."Nemotecnico" END AS "Nemotecnico",
						CASE WHEN OPE."FechaVencimiento" <= '0001-01-01 00:00:00' THEN '0000-00-00'
							WHEN OPE."Custodia" = 'NOM' AND OPE."HIdTipoProducto" = 'INAC' AND OPE."HIdTipoInstrumento" = 'AC' AND OPE."HIdTipoOperacion" = 'VE' THEN '0000-00-00'
							ELSE OPE."FechaVencimiento"
						END AS "FechaVencimientoSistema",
						OPE."IdTipoInstrumento" 		AS "IdTipoInstrumento",
						OPE."HIdTipoInstrumento" 		AS "HIdTipoInstrumento",
						OPE."IdTipoOperacion" 			AS "IdTipoOperacion",
						OPE."HIdTipoOperacion" 			AS "HIdTipoOperacion",
						(CASE WHEN OPE."IdCliente"=v_IdClienteCtaPropia THEN 'P' ELSE 'T' END) AS "Persona",
						CASE WHEN OPE."FechaVencimiento" <= '0001-01-01 00:00:00' THEN 0
							 WHEN OPE."Custodia" = 'NOM' AND OPE."HIdTipoProducto" = 'INAC' AND OPE."HIdTipoInstrumento" = 'AC' AND OPE."HIdTipoOperacion" = 'VE' THEN 0
							 ELSE DAYS_BETWEEN( OPE."FechaMovimiento",OPE."FechaVencimiento")
						END  AS "Plazo",
						COALESCE(CONDI."ValorDestino",'XX')	AS "IdCondicionLiquidacion",
						COALESCE(RELNOR."RELACION",'N') AS "TipoCliente",
						OPE."Cantidad" 					AS "UnidadesTransadasSistema",
						OPE."Precio" 					AS "PrecioTransaccionSistema",
						CASE LEFT(OPE."IdTipoOperacion",1)        
				 			WHEN 'C' THEN OPE."MontoCompra" 
				 			WHEN 'V' THEN OPE."MontoVenta"
							ELSE 0
						END AS "TotalTransadoSistema",
						COALESCE(TIPUN."ValorDestino",'OT') AS "TipoUnidad",
						CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'RF' AND OPE."TipoOperacionSecundaria" NOT IN ('RETCOM', 'RETVEN') 
							THEN  OPE."Precio"
							ELSE 0
						END AS "ValorParSistema",
						(CASE WHEN OPE."IdClasificacionTipoInstrumento"  IN ('RF','IF')  THEN OPE."Tasa" ELSE 0 END)  AS "TIR",
						0 AS "RentabilidadSistema",
						( CASE WHEN COALESCE(PAIS."CodigoPais",'CL')='CL' THEN 'N' ELSE 'E' END ) AS "NacionalidadCliente",
						COALESCE(CLI."DocumentoIdentidad",'') AS "RutCliente",
						CLI."NombreCompleto" AS "NombreCliente",
						OPE."FolioComprobante" AS "NumeroFactura",
						IDENT_AUT."DocumentoIdentidad" AS "RutAutorizado",
						PER_AUT."Nombres" ||' '|| PER_AUT."ApellidoPaterno" || ' ' ||PER_AUT."ApellidoMaterno" AS "NombreAutorizado",
						OPE."IdClasificacionTipoInstrumento",
						COALESCE(OPE."CodigoOperadorCorredor", '') AS "CodigoOperador"
					FROM :v_tablaOperaciones as OPE
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdCliente" = OPE."IdCliente")
					---CONTRAPARTE CORREDOR
					INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" CNC ON (CNC."IdCorredora"=OPE."IdContraparte")
					INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion"		   TRA ON (TRA."IdTransaccion"=OPE."IdTransaccion")
					LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS ON (PAIS."IdPais"=CLI."IdPaisNacionalidadPrincipal")
					--HOMOLOGACION
	     			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" BOLSA ---BOLSA
	     			ON (CAST (OPE."IdBolsa" AS  nvarchar(100))= BOLSA."ValorOrigen" AND BOLSA."Tipo"='BOLSA' AND BOLSA."Esquema"=v_Esquema AND BOLSA."Version"=v_Version)
	     			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" SISTE ---SISTEMABURSATIL
	     			ON (CAST (TRA."IdSistemaBursatil" AS  nvarchar(100))= SISTE."ValorOrigen" AND SISTE."Tipo"='SISTEMABURSATIL' AND SISTE."Esquema"=v_Esquema AND BOLSA."Version"=v_Version)
		    		--CONDICION LIQUIDACION
    	 			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" CONDI ---CONDICIONLIQUIDACION
	     			ON (CAST (OPE."IdCondicionLiquidacion" AS  nvarchar(100))= CONDI."ValorOrigen" AND CONDI."Tipo"='CONDICIONLIQUIDACION' AND CONDI."Esquema"=v_Esquema AND BOLSA."Version"=v_Version)
		    		--TIPO UNIDAD
    	 			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" TIPUN ---TIPOUNIDAD
	     			ON (CAST (OPE."IdMoneda" AS  nvarchar(100))= TIPUN."ValorOrigen" AND TIPUN."Tipo"='TIPOUNIDAD' AND TIPUN."Esquema"=v_Esquema AND BOLSA."Version"=v_Version)
					-- NOMBRE AUTORIZADO Y RUT AUTORIZADO
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER_AUT ON  PER_AUT."IdPersona" = (SELECT DISTINCT "IdPersona" FROM ( SELECT A."IdPortafolio", A."IdPersona" , RANK() OVER (PARTITION BY A."IdPortafolio" ORDER BY A."IdPersona" DESC) AS RANK FROM "BSCL.Portafolio.BD::POR.Autorizado" A WHERE "IdPortafolio" = OPE."IdPortafolio" AND "IdTipoAutorizado"='ORDEN-CREAR') WHERE RANK = 1)
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT_AUT ON IDENT_AUT."IdDocumentoIdentidad" = PER_AUT."IdDocumentoIdentidad"

	     			LEFT OUTER JOIN ---RELACIONADO
		     			(SELECT COALESCE(RELHOM."ValorDestino" ,'N') AS "RELACION",REL."IdCliente"
			     			 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
							 LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" RELHOM
				     			ON ((CAST (REL."CodigoValorIndicador" AS  nvarchar(100))= RELHOM."ValorOrigen" AND RELHOM."Tipo"='RELACION' AND RELHOM."Esquema"=v_Esquema AND RELHOM."Version"=v_Version))
						 	 WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
--									AND REL."IdCliente" =OPE."IdCliente")	
			     		) RELNOR ON (RELNOR."IdCliente" =OPE."IdCliente")
					ORDER BY OPE."Nemotecnico" ASC;
		ELSE
				p_out_TablaSalida=SELECT
					'' AS "RutIntermediario",
					'' AS "FechaTransaccionSistema",
					'' AS "Mercado",
					'' AS "SistemaTransaccion",
					'' AS "Bolsa",
					'' AS "Folio",
					'' AS "RutContraparte",
					0  AS "NumeroOrden",
					'' AS "Nemotecnico",
					'' AS "FechaVencimiento",
					'' AS "IdTipoInstrumento",
					'' AS "HIdTipoInstrumento",
					'' AS "IdTipoOperacion",
					'' AS "HIdTipoOperacion",
					'' AS "Persona",
					0  AS "Plazo",
					'' AS "IdCondicionLiquidacion",
					'' AS "TipoCliente",
					0  AS "UnidadesTransadasSistema",
					0  AS "PrecioTransaccionSistema",
					0  AS "TotalTransadoSistema",
					'' AS "TipoUnidad",
					0  AS "ValorParSistema",
					0  AS "TIRSistema",
					0  AS "RentabilidadSistema",
					'' AS "NacionalidadCliente",
					'' AS "RutCliente",
					'' AS "NombreCliente",
					0  AS "NumeroFactura",
					'' AS "RutAutorizado",
					'' AS "NombreAutorizado",
					'' AS "IdClasificacionTipoInstrumento",
					'' AS "CodigoOperador"
					FROM DUMMY WHERE 1=2;	
		
		END IF;						
		
		p_out_IdInfoTranOfic =v_IdInfoTranOfic;
		
			 
		select * from "BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC" WHERE "IdInterno"=v_IdInfoTranOfic;

 	END
	