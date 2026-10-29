SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CIR1064SVS";
GO 
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 10/08/2017
"BSCL.Normativa.BD.Servicios::NOR.CIR1064SVS" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064SVS"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdInfoTran INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'CIR1064';
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
		"IdInterno"	
		,"EsCtaPropia"
		,"Uso"
		,LPAD(LTRIM(LEFT ( LTRIM("RutIntermediario"),(LOCATE (LTRIM("RutIntermediario"),'-') - 1) )), 9, '0') AS "RutIntermediario"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutIntermediario") ,1)), 1, ' ') 	 									   AS "DVIntermediario"
		,LPAD("CodigoIntermediario", 5, '0')			 AS "CodigoIntermediario"
		,"FechaTransaccionSistema"
		,TO_NVARCHAR("FechaTransaccionSistema", 'YYYYMMDD') AS "FechaTransaccion"
		,TO_NVARCHAR("FechaTransaccionSistema", 'HH24MISS') AS "HoraTransaccion"
		,RPAD("Mercado", 1, ' ') 					 AS "Mercado"
		,RPAD("SistemaTransaccion", 4, ' ')			 AS "SistemaTransaccion"
		,LPAD("Bolsa", 2, '0') 						 AS "Bolsa"
		,LPAD("Folio", 7, '0') 					 	 AS "Folio"
		,LPAD("Secuencia", 3, '0') 					 AS "Secuencia"
		,LPAD(LTRIM(LEFT ( LTRIM("RutContraparte"),(LOCATE (LTRIM("RutContraparte"),'-') - 1) )), 12, '0') AS "RutContraparte"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutContraparte") ,1)), 1, ' ') 	 									   AS "DVContraparte"
		,LPAD("CodigoContraparte", 5, '0')			 AS "CodigoContraparte"
		,LPAD("NumeroOrden", 8, '0') 				 AS "NumeroOrden"
		,CASE WHEN "Mercado"='R' THEN LPAD(LTRIM(RIGHT( LTRIM("NumeroOrden") ,4)), 4, 'X') ELSE 'XXXX' END 	 AS "UltimosDigitosNumeroOrden"
		,RPAD("Nemotecnico", 20, ' ') 				 AS "Nemotecnico"
		,"FechaVencimientoSistema"
		,TO_NVARCHAR("FechaVencimientoSistema", 'YYYYMMDD') AS "FechaVencimiento"
		,"IdTipoInstrumento" 			 AS "IdTipoInstrumento"
		,"HIdTipoInstrumento"
		,"IdTipoOperacion"			 AS "IdTipoOperacion"
		,"HIdTipoOperacion"
		,LPAD(LTRIM(RTRIM("HIdTipoOperacion")), 2, ' ') || 
		(CASE WHEN "IdTipoInstrumento"='SPT' THEN (CASE WHEN "IdMoneda"='USD' THEN LPAD(LTRIM(RTRIM("HIdTipoInstrumento")), 2, ' ') ELSE 'MO' END)
		 ELSE LPAD(LTRIM(RTRIM("HIdTipoInstrumento")), 2, ' ') 
		END) 
		|| LPAD(LTRIM(RTRIM("Uso")), 2, ' ') || (CASE WHEN "EsCtaPropia" ='S' THEN 'P' ELSE 'T' END) AS "TipoOperacion1064"
		,LPAD("Persona", 1, ' ') 					 AS "Persona"
		,(CASE WHEN LTRIM("Plazo") < 0 THEN ('00000') ELSE LPAD(LTRIM("Plazo"), 5, '0') END)	 AS "Plazo"
		,LPAD("IdCondicionLiquidacion", 2, ' ') 	 AS "IdCondicionLiquidacion"
		,RPAD("TipoCliente", 2, ' ') 				 AS "TipoCliente"
		,"UnidadesTransadasSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("UnidadesTransadasSistema", 10, 4)),'.'),10,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("UnidadesTransadasSistema", 10, 4)),'.'),1,4)  AS "UnidadesTransadas"
		,"PrecioTransaccionSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("PrecioTransaccionSistema", 11, 4)),'.'),11,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("PrecioTransaccionSistema", 11, 4)),'.'),1,4)  AS "PrecioTransaccion"
		,"TotalTransadoSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("TotalTransadoSistema", 17, 4)),'.'),17,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("TotalTransadoSistema", 17, 4)),'.'),1,4) 		   AS "TotalTransado"
		,RPAD("TipoUnidad", 2, ' ') 				 	AS "TipoUnidad"
		,RPAD('XXXX', 4, 'X') 				 			AS "TipoDolar"
		,LPAD("TipoUnidad", 2, ' ') 				 	AS "UnidadReajusteTransaccion"
		,LPAD('0', 7, '0') 				 				AS "NumeroContratoFuturo"
		,"ValorParSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),1,2)  		   		   	   AS "ValorPar"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),1,2)  		   		   	   AS "PorcentajeValorPar"
		,"TIRSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("TIRSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("TIRSistema", 3, 2)),'.'),1,2)  						 	   AS "TIR"
		,"RentabilidadSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("RentabilidadSistema", 3, 2)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("RentabilidadSistema", 3, 2)),'.'),1,2) 			   AS "Rentabilidad"
		,(CASE WHEN  "Uso"='PA' THEN LPAD("TipoUnidad", 2, ' ') ELSE 'XX' END) AS "TipoReajustePacto"
		,LPAD('0', 2, '0') AS "CodObservacion"
		,RPAD('X', 2, 'X') AS "CodModificacion"
		,LPAD('0', 12, '0') AS "FolioModificacion"
		,RPAD(' ', 18, ' ') AS "EspaciosSVS"
		,LPAD("NacionalidadCliente", 1, 'N') 		 AS "NacionalidadCliente"
		,LPAD(LTRIM(LEFT ( LTRIM("RutCliente"),(LOCATE (LTRIM("RutCliente"),'-') - 1) )), 9, '0')	 		 AS "RutCliente"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutCliente") ,1)), 1, ' ')  	 											 AS "DVCliente"
		,LPAD("NombreCliente", 30, ' ') 			 AS "NombreCliente"
		,LPAD("NumeroFactura", 12, '0') 			 AS "NumeroFactura"
		,LPAD(LTRIM(LEFT ( LTRIM("RutAutorizado"),(LOCATE (LTRIM("RutAutorizado"),'-') - 1) )), 9, '0')  	 AS "RutAutorizado"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutAutorizado") ,1)), 1, ' ') 	 									  	 AS "DVAutorizado"
		,LPAD("NombreAutorizado", 30, ' ') 			 AS "NombreAutorizado"
		,"IdClasificacionTipoInstrumento"
		,LPAD("CodigoOperador", 4, '0') AS "CodigoOperador"
		,"IdMoneda"
	from "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064" WHERE "IdInterno"=v_out_IdInfoTran ORDER BY "NumeroOrden";
	
	SELECT * FROM :p_out_TablaSalida;


END
