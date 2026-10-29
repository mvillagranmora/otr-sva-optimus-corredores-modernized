SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::NCG380.ConsultarAutorizados";
GO

CREATE PROCEDURE
--
--  Proposito: Servicio para consultar información de Autorizados Relacionados para la Circular NCG380.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 08/09/2017

 "BSCL.Normativa.BD.Servicios::NCG380.ConsultarAutorizados" (
 	
 	--VARIABLES DE ENTRADA OBLIGATORIAS
 	IN p_IdEmpresa			BIGINT,
 	
 	--VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde				NVARCHAR(10) 	DEFAULT '',
	IN p_FechaOperacionHasta				NVARCHAR(10) 	DEFAULT '',
	IN p_IdCliente 							BIGINT 			DEFAULT '0',
	IN p_IdAutorizado 						BIGINT 			DEFAULT '0',
	IN p_IdTipoRelacion						NVARCHAR(6)	 	DEFAULT '',
	
	IN p_IdTrabajoParametros				BIGINT DEFAULT 0, --Se asumen valores por defecto en los parametros
	
	-- VARIABLES DE SALIDA
	OUT p_out_IdInfoTran					BIGINT 

 )
 
 	LANGUAGE SQLSCRIPT
 	SQL SECURITY INVOKER
 	DEFAULT SCHEMA "BSCL"
 AS
 	BEGIN
 		--------------------
		--Declare Variables
		--------------------
 		DECLARE v_IdNormativa			NVARCHAR(15):= 'NCG380';
 		DECLARE v_IdTipoDocumento   	NVARCHAR(10):= 'NCG380AUT';
 		DECLARE v_FechaOperacionDesde	NVARCHAR(10):= '';
 		DECLARE v_FechaOperacionHasta 	NVARCHAR(10):= '';
 		DECLARE v_IdPersonaCtaPropia	BIGINT:= 0;
 		DECLARE v_Count,v_IdInfoTran					 BIGINT:= 0;
 		DECLARE v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS,v_TODOS_AUT, v_TODOS_REL  	 NVARCHAR(40):= '';
 		DECLARE	v_IdClienteCtaPropia 						 BIGINT:=0;
 		DECLARE v_CodigoCtaPropia 							 NVARCHAR(3):= '';
 		DECLARE v_DocumentoIdentidadCtaPropia  				 NVARCHAR(40) DEFAULT '';
 		DECLARE v_RazonSocialCtaPropia  				 	 NVARCHAR(100) DEFAULT '';
 		DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
 		DECLARE v_out_error 								 NVARCHAR(500);
 		
 		v_FechaOperacionDesde := p_FechaOperacionDesde;
  		v_FechaOperacionHasta := p_FechaOperacionHasta;  	
  		
	---INICIO: DEFINICION GENERAL PARAMETROS MASIVOS  		
  		--PARAMETROS MASIVOS TABLA GLOBAL
		v_ParamClientes			=SELECT	0 	AS "IdCliente"		FROM DUMMY WHERE 1=2;
		v_ParamNemos			=SELECT	'' 	AS "Nemo" 			FROM DUMMY WHERE 1=2;
		v_ParamClasificacion	=SELECT	'' 	AS "Clasificacion"	FROM DUMMY WHERE 1=2;
		v_ParamTipoRelacion		=SELECT	'' 	AS "IdTipoRelacion"	FROM DUMMY WHERE 1=2;
		
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
				, CASE WHEN SOL."TipoParametro"='TODOS_AUT'		THEN "Valor" ELSE 'S' END AS "TODOS_AUT"
				, CASE WHEN SOL."TipoParametro"='TODOS_REL'		THEN "Valor" ELSE 'S' END AS "TODOS_REL"
			FROM :v_GBLParametros SOL WHERE  SOL."TipoParametro" IN ('TODOS_CLI','TODOS_NEMO','TODOS_CLAINS','TODOS_AUT','TODOS_REL');
		 	
			v_DatosAgrupado =SELECT  
					  MAX(SOL."TODOS_CLI")		AS "TODOS_CLI"
					, MAX(SOL."TODOS_NEMO")		AS "TODOS_NEMO"
					, MAX(SOL."TODOS_CLAINS")	AS "TODOS_CLAINS"	
					, MAX(SOL."TODOS_AUT")		AS "TODOS_AUT"
					, MAX(SOL."TODOS_REL")		AS "TODOS_REL"
					FROM :v_Datos SOL;
			SELECT TOP 1 "TODOS_CLI","TODOS_NEMO","TODOS_CLAINS","TODOS_AUT","TODOS_REL" INTO v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS, v_TODOS_AUT, v_TODOS_REL  FROM :v_DatosAgrupado;
			
			--Masivos
			v_ParamClientes= SELECT CLI."IdCliente" FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
									  INNER JOIN (SELECT DISTINCT "Valor" AS "RUT" FROM :v_GBLParametros
									  			WHERE "TipoParametro"='RUT_MAS'
									  		     ) AS PAR
									  ON (RTRIM(PAR."RUT") = CLI."DocumentoIdentidad")
									  WHERE CLI."TipoDocumentoIdentidad"='R' AND CLI."IdEstado"='ACT';
		
			v_ParamAutorizados= SELECT PER."IdPersona" AS "IdAutorizado" FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
						  INNER JOIN (SELECT DISTINCT "Valor" AS "RUT" FROM :v_GBLParametros
						  			WHERE "TipoParametro"='RUTAUT_MAS'
						  		     ) AS PAR
						  ON (RTRIM(PAR."RUT") = PER."DocumentoIdentidad")
						  WHERE PER."TipoDocumentoIdentidad"='R' AND PER."IdEstado"='ACT';						  
												  
									  
			--v_ParamNemos 			=  SELECT "Valor" AS "Nemo" 		  FROM :v_GBLParametros	WHERE "TipoParametro"='NEMO_MAS';
			--v_ParamClasificacion 	=  SELECT "Valor" AS "Clasificacion"  FROM :v_GBLParametros	WHERE "TipoParametro"='CLAINS_MAS';
			v_ParamTipoRelacion		=  SELECT	NULL  AS "IdTipoRelacion" FROM  DUMMY;
			
			
		
		ELSE
			---valores por defecto
			v_ParamClientes			=SELECT	NULL AS "IdCliente"		 FROM  DUMMY;
			v_ParamAutorizados		=SELECT	NULL AS "IdAutorizado"	 FROM  DUMMY;
			--v_ParamNemos			=SELECT	NULL AS "Nemo"	 		 FROM  DUMMY;
			--v_ParamClasificacion	=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
			v_ParamTipoRelacion		=SELECT	NULL AS "IdTipoRelacion" FROM  DUMMY;
		
		END IF;
---FIN PARAMETROS MASIVOS TABLA GLOBAL

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
			
			IF p_IdAutorizado > 0 THEN ---AUTORIZADO
				v_ParamAutorizados=SELECT	p_IdAutorizado AS "IdAutorizado"
							FROM  DUMMY
							UNION ALL SELECT "IdAutorizado" FROM :v_ParamAutorizados WHERE "IdAutorizado" IS NOT NULL;
			ELSE
				IF v_TODOS_AUT	='S' THEN
					v_ParamAutorizados=SELECT	NULL AS "IdAutorizado"	 FROM  DUMMY;
				END IF;		
			END IF ;
			IF p_IdTipoRelacion <>'' THEN --
					v_ParamTipoRelacion=SELECT	p_IdTipoRelacion AS "IdTipoRelacion"
							FROM  DUMMY
							UNION ALL SELECT "IdTipoRelacion" FROM :v_ParamTipoRelacion WHERE "IdTipoRelacion" IS NOT NULL;
			ELSE				
					IF v_TODOS_REL	='S' THEN
							v_ParamTipoRelacion=SELECT	NULL AS "IdTipoRelacion"	 FROM  DUMMY;
					END IF;			
			END IF;
---FIN DEFINIR: TABLA DE BUSQUEDA

---TERMINO: DEFINICION GENERAL PARAMETROS MASIVOS  		

		v_OrigenTablaAutorizados = 
			SELECT CLI."IdPersona", AUT."IdPersona" AS "IdPersonaDestino",TAUT."IdTipoRelacion", MIN(AUT."FechaIngreso") AS  "FechaInicioRelacion", 'AUTORIZADO' AS "Administracion", '0001-01-01 00:00:00' AS "FechaFinRelacion",
			AUT."IdEstado", AUT."IdTipoAutorizado" 
			FROM "BSCL.Portafolio.BD::POR.Autorizado" AS AUT
			LEFT JOIN "BSCL.Portafolio.BD::POR.TipoAutorizado" AS TAUT ON TAUT."IdTipoAutorizado" = AUT."IdTipoAutorizado"
			LEFT JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS TREL ON TREL."IdTipoRelacion"  = TAUT."IdTipoRelacion" 
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = AUT."IdPortafolio"
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona" 
			WHERE TREL."Administracion" = 'AUTORIZADO' AND AUT."IdEstado" = 'ACT' AND CLI."IdCliente" > 0
			GROUP BY  CLI."IdPersona", AUT."IdPersona",	TAUT."IdTipoRelacion", AUT."IdEstado", AUT."IdTipoAutorizado";
			
	 	--Obtener  (negocio) universo de relacionados de clientes con relacion NCG380
	 	v_SalidaConsultarOperaciones =
	 		SELECT
	 			 SOC."IdCliente" AS "IdClienteOrigen"
	 			,REL."IdPersonaOrigen"
		 		,REL."IdTipoRelacion"
		 		,REL."NombreTipoRelacion"
		 		,REL."FechaInicioRelacion"
		 		,REL."FechaFinRelacion"
		 		,REL."IdPersonaDestino"
		 		,REL."DocumentoIdentidadDefecto"
		 		,REL."IdTipoDocumentoIdentidadDefecto"
		 		,REL."NombreCompleto"
		 		,REL."RazonSocial"
		 		,REL."Nombres"
		 		,REL."ApellidoPaterno"
		 		,REL."ApellidoMaterno"
		 		,REL."FechaNacimiento"
		 		,REL."IdPaisResidencia"
		 		,REL."IdCliente"
		 		,COALESCE(IND."NombreValorIndicador",'') AS "NombreValorIndicador"
		 		,COALESCE(IND."CodigoValorIndicador",'') AS "CodigoValorIndicador"
		 	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" REL
		 	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TIPREL
				ON (REL."IdTipoRelacion" = TIPREL."IdTipoRelacion" AND
					REL."IdEmpresa" = TIPREL."IdEmpresa" AND
					TIPREL."IdEstado" = 'ACT')
		 	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
		 		ON (SOC."IdPersona" = REL."IdPersonaOrigen" AND SOC."Borrado"='N' AND SOC."IdCliente">0)
		 	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
		 		ON (IND."IdPersona" = REL."IdPersonaDestino" AND IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='NCG380' AND IND."CodigoValorIndicador" <>'')
		 	WHERE (TIPREL."EsIndefinida" = 'S' OR 
		 			/*(TO_DATE(REL."FechaInicioRelacion")  BETWEEN TO_DATE(:p_FechaOperacionDesde) AND TO_DATE(:p_FechaOperacionHasta)) AND
		 			(TO_DATE(REL."FechaFinRelacion")     BETWEEN TO_DATE(:p_FechaOperacionDesde) AND TO_DATE(:p_FechaOperacionHasta))*/
		 			(TO_DATE(REL."FechaInicioRelacion") <= TO_DATE(:p_FechaOperacionHasta) AND TO_DATE(REL."FechaFinRelacion") >= TO_DATE(:p_FechaOperacionDesde)))
		 			AND REL."IdEstado" = 'ACT' AND REL."Administracion" NOT LIKE '%_V' ESCAPE '_'
		 			AND (REL."Administracion" = CASE WHEN REL."IdTipoRelacion" IN ('REP','PEMDOR','CY') THEN 'RELACIONADOS'	ELSE REL."Administracion" END)
		 			AND REL."Administracion" != 'AUTORIZADO' AND REL."IdTipoRelacion" != 'AUTZDO'
		 	UNION ALL
		 	SELECT
		 		SOC."IdCliente" AS "IdClienteOrigen"
				,REL."IdPersona" AS "IdPersonaOrigen"
		 		,REL."IdTipoRelacion"
		 		,TIPREL."NombreTipoRelacion"
		 		,REL."FechaInicioRelacion"
		 		,REL."FechaFinRelacion"
		 		,REL."IdPersonaDestino"
		 		,IDENT."DocumentoIdentidad" AS "DocumentoIdentidadDefecto"
		 		,IDENT."IdTipoDocumentoIdentidad"  AS "IdTipoDocumentoIdentidadDefecto"
		 		,CASE "PER"."Tipo" 
					WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"
					ELSE "PER"."RazonSocial" END AS "NombreCompleto"
				,PER."RazonSocial"
				,PER."Nombres"
				,PER."ApellidoPaterno"
				,PER."ApellidoMaterno"
				,PER."FechaNacimiento"
				,PER."IdPaisResidencia"
		 		,SOCDEST."IdCliente"
				,COALESCE(IND."NombreValorIndicador",'') AS "NombreValorIndicador"
		 		,COALESCE(IND."CodigoValorIndicador",'') AS "CodigoValorIndicador"
			FROM :v_OrigenTablaAutorizados AS REL
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER 
				ON PER."IdPersona" = REL."IdPersonaDestino"
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
				ON (SOC."IdPersona" = REL."IdPersona" AND SOC."Borrado"='N' AND SOC."IdCliente">0)
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TIPREL
				ON (REL."IdTipoRelacion" = TIPREL."IdTipoRelacion" AND SOC."IdEmpresa" = TIPREL."IdEmpresa" AND TIPREL."IdEstado" = 'ACT')
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT
				ON REL."IdPersonaDestino"  = IDENT."IdPersona" AND IDENT."Predeterminado" = 'S'
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
				ON (IND."IdPersona" = REL."IdPersonaDestino" AND IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='NCG380' AND IND."CodigoValorIndicador" <>'')
			LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOCDEST
				ON (SOCDEST."IdPersona" = REL."IdPersonaDestino" AND SOCDEST."Borrado"='N' AND SOCDEST."IdCliente">0)
			WHERE (TIPREL."EsIndefinida" = 'S' OR 
		 			(TO_DATE(REL."FechaInicioRelacion") <= TO_DATE(:p_FechaOperacionHasta) AND TO_DATE(REL."FechaFinRelacion") >= TO_DATE(:p_FechaOperacionDesde)))
		 			AND REL."IdEstado" = 'ACT' AND REL."Administracion" NOT LIKE '%_V' ESCAPE '_'
		 			AND (REL."IdTipoAutorizado" = CASE WHEN REL."IdTipoRelacion" = 'AUTZDO' THEN 'ORDEN-CREAR' ELSE REL."IdTipoAutorizado" END)
		 	; 	

		v_tablaOperaciones =
			SELECT OPE.* FROM :v_SalidaConsultarOperaciones OPE
			INNER JOIN :v_ParamClientes 		CLI ON (OPE."IdClienteOrigen"=CLI."IdCliente" OR CLI."IdCliente" IS NULL)
			INNER JOIN :v_ParamAutorizados    	AUT ON (OPE."IdPersonaDestino"=AUT."IdAutorizado" 	OR AUT."IdAutorizado" IS NULL)
			INNER JOIN :v_ParamTipoRelacion     REL ON (OPE."IdTipoRelacion"=REL."IdTipoRelacion" OR REL."IdTipoRelacion" IS NULL);
	
			
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN	
					SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdNCG380AUT" .NEXTVAL INTO v_IdInfoTran FROM DUMMY;
					
					--p_out_TablaSalida = 
					INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_NCG380AUT" ("IdInterno", "NumeroFolioCliente","RutCLI","DigCLI","NumeroPersona","Rut","Dig","Pasaporte","Glei","ApellidoPaterno","ApellidoMaterno"
						,"PrimerNombre","SegundoNombre","RazonSocial","Telefono","Email","DireccionTipo","DireccionNombre"
						,"DireccionNumero","DireccionDepto","DireccionComuna","DireccionCiudad","DireccionEstado","DireccionPais"
						,"DireccionCodigoPostal","Nacionalidad","RelacionIntermediario","TipoRelacion")
					SELECT 
						v_IdInfoTran 					AS "IdInterno",
						LTRIM(RTRIM(SUBSTRING(PEROR."DocumentoIdentidad",1,LENGTH(PEROR."DocumentoIdentidad")-2))) 			AS "NumeroFolioCliente",
						(CASE WHEN PEROR."TipoDocumentoIdentidad" = 'R' THEN (LTRIM(RTRIM(SUBSTRING(PEROR."DocumentoIdentidad",1,LENGTH(PEROR."DocumentoIdentidad")-2)))) 
							ELSE '' END) 				AS "RutCLI",
						(CASE WHEN PEROR."TipoDocumentoIdentidad" = 'R' THEN	(LTRIM(RTRIM(SUBSTRING(PEROR."DocumentoIdentidad",LENGTH(PEROR."DocumentoIdentidad"),LENGTH(PEROR."DocumentoIdentidad")))))
							ELSE '' END) 				AS "DigCLI",
						OPE."IdPersonaDestino"		 	AS "NumeroPersona", ---AUTORIZADOS
						(CASE WHEN OPE."IdTipoDocumentoIdentidadDefecto" = 'R' THEN (LTRIM(RTRIM(SUBSTRING(OPE."DocumentoIdentidadDefecto",1,LENGTH(OPE."DocumentoIdentidadDefecto")-2)))) 
							ELSE '' END) 				AS "Rut",
						(CASE WHEN OPE."IdTipoDocumentoIdentidadDefecto" = 'R' THEN	(LTRIM(RTRIM(SUBSTRING(OPE."DocumentoIdentidadDefecto",LENGTH(OPE."DocumentoIdentidadDefecto"),LENGTH(OPE."DocumentoIdentidadDefecto")))))
							ELSE '' END) 				AS "Dig",
						(CASE WHEN OPE."IdTipoDocumentoIdentidadDefecto" = 'P' THEN	OPE."DocumentoIdentidadDefecto"                                                                                                                          
							ELSE '' END) 				AS "Pasaporte",
						PER."GlobalLegalEntity"  AS "Glei",
						OPE."ApellidoPaterno" AS "ApellidoPaterno",
						OPE."ApellidoMaterno" AS "ApellidoMaterno",
						CASE WHEN SUBSTR_BEFORE (OPE."Nombres",' ')='' THEN OPE."Nombres" ELSE SUBSTR_BEFORE (OPE."Nombres",' ') END  AS "PrimerNombre",
						SUBSTR_AFTER  (OPE."Nombres",' ') AS "SegundoNombre",
						(CASE WHEN PER."Tipo" = 'J' THEN OPE."RazonSocial"
								ELSE '' END 			) AS "RazonSocial",
						COALESCE(TEL."Numero",'')		  AS "Telefono",
						COALESCE(DIRE."Direccion",'')	  AS "Email",
						COALESCE(DIRFIS."DescripcionProposito",'')	  	AS "DireccionTipo",
						COALESCE(DIRFIS."Calle",'') 				  	AS "DireccionNombre",
						COALESCE(DIRFIS."Numero",'') 					AS "DireccionNumero",
						COALESCE(DIRFIS."Departamento",'') 				AS "DireccionDepto",
						COALESCE(DIRFIS."NombreDivisionPais",'') 		AS "DireccionComuna",
						COALESCE(DIRFIS."NombreCiudad",'') 				AS "DireccionCiudad",
						'EstadoProvincia' 								AS "DireccionEstado",
						COALESCE(PAISDIR."CodigoPais",'') 				AS "DireccionPais",
						COALESCE(DIRFIS."DescripcionProposito",'') 		AS "DireccionCodigoPostal",
						COALESCE(PAIS."NombrePais",'') 					AS "Nacionalidad",
						case 
							when OPE."CodigoValorIndicador" = 'SOCIO' 		then 'SO'
							when OPE."CodigoValorIndicador" = 'MIEMBRO' 	then 'MG'
							when OPE."CodigoValorIndicador" = 'OTRA' 		then 'XX'
							when OPE."CodigoValorIndicador" = 'DIRECTOR' 	then 'DI'
							WHEN OPE."CodigoValorIndicador" = 'TRABAJADOR' 	then 'TR'
							else 'SR'  
						end		AS "RelacionIntermediario",
						OPE."NombreTipoRelacion" AS "TipoRelacion"
						FROM :v_tablaOperaciones AS OPE
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 	PER 	ON (PER."IdPersona"=OPE."IdPersonaDestino" ) 
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 	PEROR 	ON (PEROR."IdPersona"=OPE."IdPersonaOrigen" )
						LEFT  JOIN "BSCL.SocioNegocio.BD::PER.Telefono" 				TEL  	ON (TEL."IdTelefono" = PER."IdTelefonoPrincipal") 
			     		LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" 	DIRE 	ON (DIRE."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal" AND DIRE."Borrado"='N')
			     		LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRFIS ON (DIRFIS."IdDireccionFisica" = PER."IdDireccionFisicaPrincipal")
			     		LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais"  						PAIS 	ON (PAIS."IdPais" = PER."IdPaisNacionalidadPrincipal")
			     		LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais"  						PAISDIR	ON (DIRFIS."IdPais" = PAISDIR."IdPais");
			     		
		
		
		END IF;						
		
		p_out_IdInfoTran =v_IdInfoTran;
		
			 
		select * from "BSCL.Normativa.BD::GBL.SALIDA_NCG380AUT" WHERE "IdInterno"=v_IdInfoTran;

 	END
