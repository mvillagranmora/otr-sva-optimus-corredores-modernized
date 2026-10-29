SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.INFOTRANOFIC";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 19/01/2016
"BSCL.Normativa.BD.Servicios::NOR.INFOTRANOFIC" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
 	IN p_IdTipoDocumento   		 	  NVARCHAR(10) DEFAULT '',
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdInfoTranOfic INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	
	DECLARE v_ParamAnulacionPorNC 			 NVARCHAR(2):='N';
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
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , p_IdTipoDocumento AS "Valor"
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
	
	
	CALL "BSCL.Normativa.BD.Servicios::TRA.ConsultarTransacciones"
	(
		p_IdEmpresa					=> v_IdEmpresa,
		p_FechaOperacionDesde		=> v_FechaOperacionDesde,
		p_FechaOperacionHasta		=> v_FechaOperacionHasta,
		p_IdCliente 				=> v_IdCliente,
	    p_Nemotecnico 				=> v_NEMO,
		p_IdTrabajoParametros		=> :v_IdTrabajoParametros,
		p_paramAnulacionPorNC		=> v_ParamAnulacionPorNC,
		p_out_IdInfoTranOfic		=> v_out_IdInfoTranOfic
		--p_out_TablaSalida			=> v_out_TablaSalida
	);
	
	p_out_TablaSalida = SELECT
		"IdInterno"	
		,LPAD(LTRIM(LEFT ( LTRIM("RutIntermediario"),(LOCATE (LTRIM("RutIntermediario"),'-') - 1) )), 9, '0') AS "RutIntermediario"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutIntermediario") ,1)), 1, ' ') 	 									   AS "DVIntermediario"
		,"FechaTransaccionSistema"
        ,TO_CHAR(UTCTOLOCAL ("FechaTransaccionSistema", 'America/Santiago','platform'), 'YYYYMMDD') AS "FechaTransaccion"
    	,TO_CHAR(UTCTOLOCAL("FechaTransaccionSistema", 'America/Santiago','platform'), 'HH24MISS') AS "HoraTransaccion" 
		,LPAD("Mercado", 1, ' ') 					 AS "Mercado"
		,LPAD("SistemaTransaccion", 4, ' ')			 AS "SistemaTransaccion"
		,LPAD("Bolsa", 2, '0') 						 AS "Bolsa"
		,LPAD(LTRIM("Folio"),12, '0') 				 AS "Folio"
		,LPAD(LTRIM(LEFT ( LTRIM("RutContraparte"),(LOCATE (LTRIM("RutContraparte"),'-') - 1) )), 9, '0') AS "RutContraparte"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutContraparte") ,1)), 1, ' ') 	 									   AS "DVContraparte"
		,LPAD("NumeroOrden", 8, '0') 				 AS "NumeroOrden"
		,RPAD("Nemotecnico", 20, ' ') 				 AS "Nemotecnico"
		,"FechaVencimientoSistema"
		,LPAD(TO_NVARCHAR("FechaVencimientoSistema", 'YYYYMMDD'),8, '0') AS "FechaVencimiento"
		,LPAD("IdTipoInstrumento", 2, ' ') 			 AS "IdTipoInstrumento"
		,"HIdTipoInstrumento"
		,LPAD("IdTipoOperacion", 2, ' ') 			 AS "IdTipoOperacion"
		,"HIdTipoOperacion"
		,LPAD("Persona", 1, ' ') 					 AS "Persona"
		,(CASE WHEN LTRIM("Plazo") < 0 THEN ('00000') ELSE LPAD(LTRIM("Plazo"), 5, '0') END)	 AS "Plazo"
		,LPAD("IdCondicionLiquidacion", 2, ' ') 	 AS "IdCondicionLiquidacion"
		,LPAD("TipoCliente", 1, ' ') 				 AS "TipoCliente"
		,"UnidadesTransadasSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("UnidadesTransadasSistema", 15, 4)),'.'),15,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("UnidadesTransadasSistema", 15, 4)),'.'),1,4)  AS "UnidadesTransadas"
		,"PrecioTransaccionSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("PrecioTransaccionSistema", 11, 4)),'.'),11,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("PrecioTransaccionSistema", 11, 4)),'.'),1,4)  AS "PrecioTransaccion"
		,"TotalTransadoSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("TotalTransadoSistema", 17, 4)),'.'),17,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("TotalTransadoSistema", 17, 4)),'.'),1,4) 		   AS "TotalTransado"
		,LPAD("TipoUnidad", 2, ' ') 				 AS "TipoUnidad"
		,"ValorParSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),3,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("ValorParSistema", 3, 2)),'.'),1,2)  		   		   	   AS "ValorPar"
		,"TIRSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("TIRSistema", 3, 2)),'.'),3,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("TIRSistema", 3, 2)),'.'),1,2)  						 	   AS "TIR"
		,"RentabilidadSistema"
		,LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL("RentabilidadSistema", 3, 2)),'.'),3,'0')||','||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL("RentabilidadSistema", 3, 2)),'.'),1,2) 			   AS "Rentabilidad"
		,LPAD("NacionalidadCliente", 1, 'N') 		 AS "NacionalidadCliente"
		,LPAD(LTRIM(LEFT ( LTRIM("RutCliente"),(LOCATE (LTRIM("RutCliente"),'-') - 1) )), 9, '0')	 		 AS "RutCliente"
		,LPAD(LTRIM(RIGHT ( LTRIM("RutCliente") ,1)), 1, ' ')  	 											 AS "DVCliente"
		,RPAD("NombreCliente", 30, ' ') 			 AS "NombreCliente"
		,LPAD("NumeroFactura", 12, '0') 			 AS "NumeroFactura"
		,LTRIM(LEFT ( LTRIM("RutAutorizado"),(LOCATE (LTRIM("RutAutorizado"),'-') - 1) )) AS "RutAutorizado"
		,LTRIM(RIGHT ( LTRIM("RutAutorizado") ,1)) AS "DVAutorizado"
		,RPAD("NombreAutorizado", 30, ' ') AS "NombreAutorizado"
		,"IdClasificacionTipoInstrumento"
		,LPAD("CodigoOperador", 3, '0') AS "CodigoOperador"
	from "BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC" WHERE "IdInterno"=v_out_IdInfoTranOfic ORDER BY "Nemotecnico" ASC, "FechaTransaccionSistema" ASC;
	
	SELECT * FROM :p_out_TablaSalida;
	
	
--SELECT * FROM "BSCL.Normativa.BD::GBL.Parametros";	

END
