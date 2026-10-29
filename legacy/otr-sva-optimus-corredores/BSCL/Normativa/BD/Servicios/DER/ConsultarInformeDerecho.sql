SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::DER.ConsultarInformeDerecho";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::DER.ConsultarInformeDerecho"
( 
--VARIABLES DE ENTRADA OBLIGATORIAS
 	IN p_IdEmpresa			BIGINT,
 	
 	--VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde			NVARCHAR(10) 	DEFAULT '',
	IN p_FechaOperacionHasta			NVARCHAR(10) 	DEFAULT '',
	IN p_IdCliente 						BIGINT 			DEFAULT '0',
	IN p_Nemotecnico 					NVARCHAR(40)	DEFAULT '',
	IN p_IdTipoDocumento   				NVARCHAR(10)	DEFAULT 'INFDERECH',
	
	IN p_IdTrabajoParametros			BIGINT DEFAULT 0, --Se asumen valores por defecto en los parametros
	
	-- VARIABLES DE SALIDA
	OUT p_out_IdInfoTran				BIGINT 
 )
 
 	LANGUAGE SQLSCRIPT
 	SQL SECURITY INVOKER
 	DEFAULT SCHEMA "BSCL"
 AS
 	BEGIN
 		--------------------
		--Declare Variables
		--------------------
 		DECLARE v_IdNormativa			NVARCHAR(15):= 'INFDERECH';
 		DECLARE v_FechaOperacionDesde	NVARCHAR(10):= '';
 		DECLARE v_FechaOperacionHasta 	NVARCHAR(10):= '';
 		DECLARE v_IdPersonaCtaPropia	BIGINT:= 0;
 		DECLARE v_Count,v_IdInfoTran					 BIGINT:= 0;
 		DECLARE v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  	 NVARCHAR(40):= '';
 		DECLARE	v_IdClienteCtaPropia 						 BIGINT:=0;
 		DECLARE v_CodigoCtaPropia 							 NVARCHAR(3):= '';
 		DECLARE v_DocumentoIdentidadCtaPropia  				 NVARCHAR(40) DEFAULT '';
 		DECLARE v_RazonSocialCtaPropia  				 	 NVARCHAR(100) DEFAULT '';
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
				  CASE WHEN SOL."TipoParametro"='TODOS_CLI'		THEN "Valor" ELSE 'S' END AS "TODOS_CLI"
				, CASE WHEN SOL."TipoParametro"='TODOS_NEMO'	THEN "Valor" ELSE 'S' END AS "TODOS_NEMO"
				, CASE WHEN SOL."TipoParametro"='TODOS_CLAINS'	THEN "Valor" ELSE 'S' END AS "TODOS_CLAINS"
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
				v_ParamClasificacion=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
				--para este tipo documento se acota el todos solo para algunos mercados
				
				v_ParamClasificacion=  SELECT	'RV' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'RF' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'IF' AS "Clasificacion"	 FROM  DUMMY							
							UNION ALL  SELECT	'SP' AS "Clasificacion"	 FROM  DUMMY;
																		
			END IF;	
			
			
		---FIN DEFINIR: TABLA DE BUSQUEDA

		--TERMINO: DEFINICION GENERAL PARAMETROS MASIVOS  		


		---- Transacciones desde servicionNormativo de Operaciones
		---- v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
		
 			 v_TablaModulo = Select 'MRF' as "IdModulo" from dummy UNION ALL
							 Select 'FAC' as "IdModulo" from dummy UNION ALL
							 Select 'MFW' as "IdModulo" from dummy ;
							 
		
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY
					UNION ALL SELECT 'CLASEFECHA' AS "Codigo", 'TRANSACCION' AS "Valor" FROM DUMMY;
		
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_TablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
			
			
		v_tablaOperaciones=SELECT OPE.* FROM :v_SalidaConsultarOperaciones OPE;
		--INNER JOIN :v_ParamClientes 		CLI ON (OPE."IdCliente"=CLI."IdCliente" OR CLI."IdCliente" IS NULL)
		--INNER JOIN :v_ParamNemos    		NEM ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
		--INNER JOIN :v_ParamClasificacion    CLA ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL);

	
			---Valores de la cuenta propia	
		SELECT TOP 1 CLI."IdCliente", CLI."DocumentoIdentidad",COR."CodigoCorredora",  CLI."RazonSocial" INTO v_IdClienteCtaPropia,v_DocumentoIdentidadCtaPropia,v_CodigoCtaPropia, v_RazonSocialCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
		--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COD ON COD."IdPersona" =SOC."IdPersona"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" COR ON COD."IdCorredora" =COR."IdCorredora"
		WHERE EMP."IdEmpresa" = p_IdEmpresa
		AND CLI."IdEstado"='ACT';
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = p_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaOperacionDesde='' OR p_FechaOperacionDesde='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaOperacionDesde, 'DD-MM-YYYY');
		END IF;	
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		
		
		
			
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN	
					SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdCircular1064".NEXTVAL INTO v_IdInfoTran FROM DUMMY;
					
					--p_out_TablaSalida = 
					INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064" ("IdInterno","EsCtaPropia","Uso","RutIntermediario","CodigoIntermediario", "RazonSocialIntermediario" ,"FechaTransaccionSistema","Mercado","SistemaTransaccion","Bolsa","Folio","FolioFR","Secuencia","RutContraparte"
					,"CodigoContraparte","NumeroOrden","Nemotecnico","FechaVencimientoSistema"	,"IdTipoInstrumento","HIdTipoInstrumento","IdTipoOperacion","HIdTipoOperacion","Persona","Plazo","IdCondicionLiquidacion","TipoCliente","UnidadesTransadasSistema"
					,"PrecioTransaccionSistema","TotalTransadoSistema","TipoUnidad"	,"ValorParSistema"	,"TIRSistema","RentabilidadSistema","NacionalidadCliente","RutCliente"
					,"NombreCliente","NumeroFactura","RutAutorizado","NombreAutorizado","IdClasificacionTipoInstrumento","CodigoOperador","IdMoneda","EsEnRueda","IdCliente","IdPortafolio","AccionOperacion","IdEmisor")
					Select 
						v_IdInfoTran AS "IdInterno",
						CASE WHEN OPE."IdCliente"=v_IdClienteCtaPropia THEN 'S' ELSE 'N' END AS "EsCtaPropia",
						OPE."Uso",
						LTRIM(v_DocumentoIdentidadCtaPropia) 	AS "RutIntermediario",
						LTRIM(v_CodigoCtaPropia) 				AS "CodigoIntermediario",
						LTRIM(v_RazonSocialCtaPropia) 			AS "RazonSocialIntermediario",
						OPE."FechaMovimiento" AS "FechaTransaccionSistema",
						(CASE OPE."EsEnRueda" WHEN 'S' THEN 'R'    ELSE'F'	 END) AS "Mercado", 
						--TRA."IdSistemaBursatil"	 AS  "SistemaTransaccion",	
						''	 AS  "SistemaTransaccion",
						COALESCE(OPE."IdBolsa",'BCS') 	 AS  "Bolsa",			
						OPE."FolioTransaccion" AS "Folio",
						'0' AS "FolioFR",
						
						'0' AS "Secuencia",
						COALESCE(CORRE."DocumentoIdentidad",:v_DocumentoIdentidadCtaPropia) 		AS "RutContraparte", --Intermediario Contraparte
						COALESCE(CORRE."CodigoCorredora",:v_CodigoCtaPropia)    					AS "CodigoContraparte",
						OPE."IdOrden" 			AS "NumeroOrden",
						OPE."Nemotecnico" 				AS "Nemotecnico", 
						--case when OPE."IdClasificacionTipoInstrumento" = 'IF' then OPE."NemoSVS" else OPE."Nemotecnico" end AS "Nemotecnico",
						OPE."FechaVencimiento" 			AS "FechaVencimientoSistema",
						OPE."IdTipoInstrumento" 		AS "IdTipoInstrumento",
						OPE."HIdTipoInstrumento" 		AS "HIdTipoInstrumento",
						OPE."IdTipoOperacion" 			AS "IdTipoOperacion",
						OPE."HIdTipoOperacion" 			AS "HIdTipoOperacion",
						(CASE WHEN OPE."IdCliente"=v_IdClienteCtaPropia THEN 'P' ELSE 'T' END) AS "Persona",
						(CASE  WHEN DAYS_BETWEEN( OPE."FechaMovimiento",OPE."FechaVencimiento") >0 THEN DAYS_BETWEEN( OPE."FechaMovimiento",OPE."FechaVencimiento") ELSE 0 END) AS "Plazo",
						COALESCE(OPE."IdCondicionLiquidacion",'PH')	AS "IdCondicionLiquidacion",
						CASE WHEN COALESCE(RELNOR."CodigoValorIndicador",'NOVINCULADO') = 'NOVINCULADO' THEN 'N' ELSE 'S' END AS "TipoCliente",
						OPE."Cantidad" 					AS "UnidadesTransadasSistema",
						OPE."Precio" 					AS "PrecioTransaccionSistema",
						CASE LEFT(OPE."IdTipoOperacion",1)        
				 			WHEN 'C' THEN OPE."MontoCompra" 
				 			WHEN 'V' THEN OPE."MontoVenta"
							ELSE 0
						END AS "TotalTransadoSistema",
						COALESCE(OPE."IdMoneda",'  ') AS "TipoUnidad",
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
						'' AS "RutAutorizado",
						'' AS "NombreAutorizado",
						OPE."IdClasificacionTipoInstrumento",
						COALESCE(TRA."CodigoOperadorCorredor",'00') AS "CodigoOperador",
						OPE."IdMoneda",
						OPE."EsEnRueda",
						OPE."IdCliente",
						OPE."IdPortafolio",
						OPE."AccionOperacion",
						OPE."IdEmisor"
					FROM :v_tablaOperaciones as OPE
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdCliente" = OPE."IdCliente")
					---CONTRAPARTE CORREDOR
					LEFT OUTER JOIN (
						SELECT CNC."DocumentoIdentidad",COR."CodigoCorredora",CNC."IdCorredora",COR."IdBolsa" FROM 
						"BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"		  CNC 
						INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" COR ON (CNC."IdCorredora" =COR."IdCorredora")
					) CORRE ON  (CORRE."IdCorredora"=OPE."IdContraparte" AND OPE."IdBolsa"=CORRE."IdBolsa")
					--INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" CNCC ON (CNCC."IdCorredora"=CNC."IdCorredora")
					LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion"		   TRA ON (TRA."IdTransaccion"=OPE."IdTransaccion")
					LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS ON (PAIS."IdPais"=CLI."IdPaisNacionalidadPrincipal")
		    		--TIPO UNIDAD
    	 			LEFT OUTER JOIN ---RELACIONADO
		     			(SELECT REL."IdCliente",rtrim(ltrim(REL."CodigoValorIndicador")) as "CodigoValorIndicador"
			     			 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
							 WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
			     		) RELNOR ON (RELNOR."IdCliente" =OPE."IdCliente")
			     	WHERE 	COALESCE (RELNOR."CodigoValorIndicador", '') <>  'NOINFORMAR';

				
				
		
		
		END IF;						
		
		p_out_IdInfoTran =v_IdInfoTran;
		
			 
		select * from "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064" WHERE "IdInterno"=v_IdInfoTran;

 	END





