SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CIR1064Rueda";
GO 
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 21/08/2017
"BSCL.Normativa.BD.Servicios::NOR.CIR1064Rueda" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064RUEDA"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdInfoTran INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'CIR1064R';
	DECLARE v_TODOS_CLI,v_TODOS_NEMO		 NVARCHAR(2):='N';
	DECLARE v_NEMO 							 NVARCHAR(40):='';
	DECLARE v_IDCLI				 			 NVARCHAR(40):='0';
	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	DECLARE v_ParametrosSolicitudNemo    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	
	
	IF p_IdSolicitudCircular>0 THEN
		
		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
	
		 				
		
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"
			FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
		 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		 	
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" PAR 
						WHERE PAR."IdSolicitudCircular" = p_IdSolicitudCircular;	
			
	END IF;
	
	SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
	if v_Count>0 THEN
		SELECT TOP 1  "Valor" INTO v_IdEmpresa
			FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
			
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"			
			FROM :p_TablaParametros SOL;
			
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM :p_TablaParametros PAR UNION ALL
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , v_IdTipoDocumento AS "Valor"
						 FROM DUMMY;		
			
	END IF;


	v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."FINI")) 	AS "FINI"
					, MAX(TRIM(SOL."FTER"))		AS "FTER"
					, MAX(TRIM(SOL."IDCLI"))	AS "IDCLI"
					, MAX(TRIM(SOL."NEMO"))		AS "NEMO"
					FROM :v_Datos SOL;
					

	SELECT TOP 1 "FINI","FTER", "IDCLI","NEMO" INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_IDCLI, v_NEMO FROM :v_DatosAgrupado;
	
	v_ParametrosSolicitudRut 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	v_ParametrosSolicitudNemo 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	
	
	
		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		v_FechaVersion :=TO_VARCHAR (v_FechaOperacionDesde, 'DD-MM-YYYY');
		
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		
		
	
	
	
	
	SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdTrabajoParametros FROM DUMMY;
			
	INSERT INTO "BSCL.Normativa.BD::GBL.Parametros" ("IdInterno","Tipo","NombreParametro","Valor")
	SELECT :v_IdTrabajoParametros AS "IdInterno",'SOLICITUD', PAR."TipoParametro", PAR."Valor"
		FROM :v_DatosParametros PAR;
			
	v_IdCliente = CAST (TRIM(v_IDCLI) AS BIGINT); 	
	
	
	CALL "BSCL.Normativa.BD.Servicios::CIR1064.ConsultarTransacciones"
	(
		p_IdEmpresa					=> v_IdEmpresa,
		p_FechaOperacionDesde		=> v_FechaOperacionDesde,
		p_FechaOperacionHasta		=> v_FechaOperacionHasta,
		p_IdCliente 				=> v_IdCliente,
	    p_Nemotecnico 				=> v_NEMO,
	    p_IdTipoDocumento			=> v_IdTipoDocumento,
		p_IdTrabajoParametros		=> :v_IdTrabajoParametros,
		p_out_IdInfoTran			=> v_out_IdInfoTran
		--p_out_TablaSalida			=> v_out_TablaSalida
	);
	
	p_out_TablaSalida = SELECT
		OPE."IdInterno"	
		,OPE."EsCtaPropia"
		,OPE."Uso"
		--,LPAD(LTRIM(LEFT ( LTRIM("RutIntermediario"),(LOCATE (LTRIM("RutIntermediario"),'-') - 1) )), 9, '0') AS "RutIntermediario"
		--,LPAD(LTRIM(RIGHT ( LTRIM("RutIntermediario") ,1)), 1, ' ') 	 									   AS "DVIntermediario"
		,LPAD(LTRIM(OPE."RutIntermediario"), 11, ' ')			 AS "RutIntermediario"
		,LPAD(OPE."CodigoIntermediario", 3, '0')			 	 AS "CodigoIntermediario"
		,RPAD(OPE."RazonSocialIntermediario", 10, ' ')			 AS "RazonSocialIntermediario"
		,OPE."FechaTransaccionSistema"
		,TO_NVARCHAR(OPE."FechaTransaccionSistema", 'YYYY-MM-DD') AS "FechaTransaccion"
		,TO_NVARCHAR(OPE."FechaTransaccionSistema", 'HH24MISS') AS "HoraTransaccion"
		,LPAD(OPE."Mercado", 1, ' ') 					 AS "Mercado"
		,LPAD(OPE."SistemaTransaccion", 4, ' ')			 AS "SistemaTransaccion"
		,LPAD(OPE."Bolsa", 2, '0') 						 AS "Bolsa"
		,LPAD(OPE."Folio", 8, '0') 					 	 AS "Folio"
		,LPAD(OPE."Secuencia", 3, '0') 					 AS "Secuencia"
		,LPAD(LTRIM(LEFT ( LTRIM(OPE."RutContraparte"),(LOCATE (LTRIM(OPE."RutContraparte"),'-') - 1) )), 12, '0') AS "RutContraparte"
		,LPAD(LTRIM(RIGHT ( LTRIM(OPE."RutContraparte") ,1)), 1, ' ') 	 									   AS "DVContraparte"
		,LPAD(OPE."CodigoContraparte", 5, '0')			 AS "CodigoContraparte"
		,LPAD(OPE."NumeroOrden", 8, '0') 				 AS "NumeroOrden"
		,CASE WHEN OPE."Mercado"='R' THEN LPAD(LTRIM(RIGHT( LTRIM(OPE."NumeroOrden") ,4)), 4, 'X') ELSE 'XXXX' END 	 AS "UltimosDigitosNumeroOrden"
		,RPAD(OPE."Nemotecnico", 10, ' ') 				 AS "Nemotecnico"
		,OPE."FechaVencimientoSistema"
		,TO_NVARCHAR(OPE."FechaVencimientoSistema", 'YYYYMMDD') AS "FechaVencimiento"
		,OPE."IdTipoInstrumento" 			 AS "IdTipoInstrumento"
		,OPE."HIdTipoInstrumento" 			 AS "HIdTipoInstrumento"
	 	,COALESCE(TIPOINS."ValorDestino",'00')  AS "IdTipoInstrumentoRueda" 
		,OPE."IdTipoOperacion"			 AS "IdTipoOperacion"
		,OPE."HIdTipoOperacion"
		,LPAD(OPE."Persona", 1, ' ') 					 AS "Persona"
		,(CASE WHEN LTRIM(OPE."Plazo") < 0 THEN ('00000') ELSE LPAD(LTRIM(OPE."Plazo"), 5, '0') END)	 AS "Plazo"
		,LPAD(OPE."IdCondicionLiquidacion", 2, ' ') 	 AS "IdCondicionLiquidacion"
		,(CASE WHEN LEFT(OPE."TipoCliente",1) ='X' THEN 'N' ELSE  LPAD(OPE."TipoCliente", 1, ' ') END)				 AS "TipoCliente"
		,OPE."UnidadesTransadasSistema"
		,' '||LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."UnidadesTransadasSistema", 13, 2)),'.'),13,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."UnidadesTransadasSistema", 13, 2)),'.'),1,2)  AS "UnidadesTransadas"
		,OPE."PrecioTransaccionSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."PrecioTransaccionSistema", 11, 4)),'.'),11,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."PrecioTransaccionSistema", 11, 4)),'.'),1,4)  AS "PrecioTransaccion"
		,OPE."TotalTransadoSistema"
		,' '||LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."TotalTransadoSistema", 15, 4)),'.'),15,'0') 		   AS "TotalTransado"
		,LPAD(OPE."TipoUnidad", 2, ' ') 				 	AS "TipoUnidad"
		,LPAD('XXXX', 4, 'X') 				 			AS "TipoDolar"
		,LPAD(OPE."TipoUnidad", 2, ' ') 				 	AS "UnidadReajusteTransaccion"
		,LPAD('0', 7, '0') 				 				AS "NumeroContratoFuturo"
		,OPE."ValorParSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."ValorParSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."ValorParSistema", 3, 2)),'.'),1,2)  		   		   	   AS "ValorPar"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."ValorParSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."ValorParSistema", 3, 2)),'.'),1,2)  		   		   	   AS "PorcentajeValorPar"
		,OPE."TIRSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."TIRSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."TIRSistema", 3, 2)),'.'),1,2)  						 	   AS "TIR"
		,OPE."RentabilidadSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."RentabilidadSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."RentabilidadSistema", 3, 2)),'.'),1,2) 			   AS "Rentabilidad"
		,(CASE WHEN  OPE."Uso"='PA' THEN LPAD(OPE."TipoUnidad", 2, ' ') ELSE 'XX' END) AS "TipoReajustePacto"
		,LPAD('0', 2, '0') AS "CodObservacion"
		,LPAD('X', 2, 'X') AS "CodModificacion"
		,LPAD('0', 12, '0') AS "FolioModificacion"
		,LPAD(' ', 18, ' ') AS "EspaciosSVS"
		,LPAD(OPE."NacionalidadCliente", 1, 'N') 		 AS "NacionalidadCliente"
		--,LPAD(LTRIM(LEFT ( LTRIM(OPE."RutCliente"),(LOCATE (LTRIM(OPE."RutCliente"),'-') - 1) )), 9, '0')	 		 AS "RutCliente"
		--,LPAD(LTRIM(RIGHT ( LTRIM(OPE."RutCliente") ,1)), 1, ' ')  	 											 AS "DVCliente"
		,LPAD(LTRIM(OPE."RutCliente"), 11, ' ')																	 AS "RutCliente"
		,LPAD(LTRIM(RIGHT ( LTRIM(POR."CodigoPortafolio") ,2)), 2, '0') 										 AS "CodigoPortafolio"
		,LPAD(OPE."NombreCliente", 30, ' ') 			 AS "NombreCliente"
		,LPAD(OPE."NumeroFactura", 12, '0') 			 AS "NumeroFactura"
		,LPAD(LTRIM(LEFT ( LTRIM(OPE."RutAutorizado"),(LOCATE (LTRIM(OPE."RutAutorizado"),'-') - 1) )), 9, '0')  	 AS "RutAutorizado"
		,LPAD(LTRIM(RIGHT ( LTRIM(OPE."RutAutorizado") ,1)), 1, ' ') 	 									  	 AS "DVAutorizado"
		,LPAD(OPE."NombreAutorizado", 30, ' ') 			 AS "NombreAutorizado"
		,OPE."IdClasificacionTipoInstrumento"
		,LPAD(OPE."CodigoOperador", 4, '0') AS "CodigoOperador"
		,OPE."IdMoneda"
		,LPAD(' ', 1, ' ') AS "Rueda"
		,CASE WHEN OPE."AccionOperacion"='COMPRA' 	THEN 'C '
			  WHEN OPE."AccionOperacion"='VENTA'  	THEN 'V '
			  WHEN OPE."AccionOperacion"='INGRESO'  THEN 'C '
			  WHEN OPE."AccionOperacion"='RETIRO'  	THEN 'V '
			  ELSE '  ' END AS "TipoOperacionRueda" 
	FROM "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064" OPE
	--PORTAFOLIO
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (OPE."IdPortafolio" = POR."IdPortafolio")
	--HOMOLOGACION
	LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" TIPOINS ---TIPOINS
	ON (CAST (OPE."HIdTipoInstrumento" AS  nvarchar(100))= TIPOINS."ValorOrigen" AND TIPOINS."Tipo"='TIPOINSTRUMENTORUEDA' AND TIPOINS."Esquema"=v_Esquema AND TIPOINS."Version"=v_Version)

	
	WHERE "IdInterno"=v_out_IdInfoTran
	AND "EsEnRueda"='S'
	ORDER BY LPAD(LTRIM(OPE."RutCliente"), 11, ' ')	,OPE."NumeroOrden";
	
	SELECT * FROM :p_out_TablaSalida;


END
