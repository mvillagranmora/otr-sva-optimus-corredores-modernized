SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::INFDERECH";
--procedimiento que es llamado desde el job manager
--para crear el informe de derecho
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::INFDERECH"
( 
    -- Parametros de Entrada
 	 IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0 
	,IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))  
    
    -- VARIABLES DE SALIDA -- debo revisar el servicio 
	,OUT p_out_TablaSalida	 		 "BSCL.Normativo.BD::NOR.SALIDA_INFORMEDERECHO"  --Se debe llamar siemte p_out_TablaSalida
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdInfoTran INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'INFDERECH';
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

	
	CALL "BSCL.Normativa.BD.Servicios::DER.ConsultarInformeDerecho"
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

  	datos =
    SELECT
    D."TipoRegistro" , D."Folio" , D."Mercado" , D."CorredorCompra" , D."CorredorVenta", D."FechaVencimiento" , D."Nemotecnico", D."Precio", D."Tasa"
    , D."SignoTasa" , D."DocumentoIdentidad" , D."Cantidad" , D."TipoOperacion" , D."TipoReajuste" , D."Monto" , D."Emisor"
    
    ,D."TipoRegistro" || D."Folio" || D."Mercado" || D."CorredorCompra" || D."CorredorVenta"|| D."FechaVencimiento" || D."Nemotecnico"|| D."Precio"|| D."Tasa"
    || D."SignoTasa" || D."DocumentoIdentidad" || D."Cantidad" || D."TipoOperacion" || D."TipoReajuste" || D."Monto" || D."Emisor" AS  "RegistroConcatenado"
    FROM (
    	select  
    	                --- BCS 
    					--- 2,3,7			(Simultaneas) 
    					--- 4 cta propia
    					--- 5 clientes convenio
    					--- 6 relacionados 
    					---	BEC Y BCV			
    					--- c , d , g , h  	(Simultaneas)
    					--- a cta propia BEC
    					--- b cta propia BCV
    					--- e relacionados BEC
    					--- f relacionados BCV
    					-- fin BCS     :  no VINCULADO sin convenio  (no sim) quedan en 0 no se informa
    					-- fin BEC/BCV :  no VINCULADO sin convenio  quedan en 0 no se informa
    					
	                     case         
	                     --BCS 
	                       when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='N') then '2'
                 		   when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'S') 							  then '3'
		                   when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then '7'
	                       when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'S')							  then '4' 
		                   when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'N' and IFNULL(CONVENIO."ValorIndicador",'N') <> 'N') then '5'
		                   when (OPE."Bolsa" = 'BCS' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then '6'
						 --BEC Y BCV
			               when (OPE."Bolsa" = 'BEC' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'S') 							  then 'c'        
				           when (OPE."Bolsa" = 'BCV' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'S') 							  then 'd'        
				           when (OPE."Bolsa" = 'BEC' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then 'g'        
			               when (OPE."Bolsa" = 'BCV' and OPE."IdTipoInstrumento" =  'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then 'h'        
			               when (OPE."Bolsa" = 'BEC' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'S') 							  then 'a'        
			               when (OPE."Bolsa" = 'BCV' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'S') 							  then 'b' 
			               when (OPE."Bolsa" = 'BEC' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then 'e'        
			               when (OPE."Bolsa" = 'BCV' and OPE."IdTipoInstrumento" <> 'SIM' and OPE."EsCtaPropia" = 'N' and OPE."TipoCliente" ='S') then 'f'        
		                   else '0' -- no se informan
				          end as "TipoRegistro"  
				         ,LPAD(COALESCE(OPE."Folio",'0'),6,0) as "Folio"
			             ,case  when coalesce(TIPOINS."ValorDestino",OPE."HIdTipoInstrumento") ='ET' then 'AO'
			                    when coalesce(TIPOINS."ValorDestino",OPE."HIdTipoInstrumento") ='CM' then 'CF'
			                    else coalesce(TIPOINS."ValorDestino",OPE."HIdTipoInstrumento") 
			                    end
			              as "Mercado"
			             ,case when OPE."HIdTipoOperacion" = 'C' then LPAD(OPE."CodigoIntermediario",3,'0') 
			                                                         else LPAD(OPE."CodigoContraparte",3,'0')
			                                                        end as "CorredorCompra"
			             ,case when OPE."HIdTipoOperacion" = 'V' then LPAD(OPE."CodigoIntermediario",3,'0') 
			                                                         else LPAD(OPE."CodigoContraparte",3,'0') 
			                                                        end as "CorredorVenta"
			             ,case when  OPE."IdTipoInstrumento" = 'SIM' then  CASE WHEN ltrim(rtrim(coalesce(OPE."FechaVencimiento",'0001-01-01'))) ='' THEN '0001-01-01' ELSE  ltrim(rtrim(coalesce(OPE."FechaVencimiento",'0001-01-01'))) END
			              else '0001-01-01' end as "FechaVencimiento"
			             ,RPAD(COALESCE(OPE."Nemotecnico",OPE."Nemotecnico"),10,' ') as "Nemotecnico"
			             ,CASE WHEN OPE."IdTipoInstrumento" <> 'SIM' THEN LPAD('',17,'0')
			              	--ELSE LPAD(SUBSTR_BEFORE (TO_CHAR(OPE."PrecioTransaccionSistema"),'.')||SUBSTRING(SUBSTR_AFTER(TO_CHAR(OPE."PrecioTransaccionSistema"),'.'),1,4),17,'0') 
			                  ELSE LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."PrecioTransaccionSistema",26, 8)),'.'),13,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."PrecioTransaccionSistema",26, 8)),'.'),1,4)
			              END as "Precio"
			             ,CASE WHEN OPE."IdTipoInstrumento" <> 'SIM' THEN LPAD('',7,'0')
			             	--ELSE LPAD(SUBSTR_BEFORE (TO_CHAR(OPE."TIR"),'.')||SUBSTRING(SUBSTR_AFTER(TO_CHAR(OPE."TIR"),'.'),1,4),7,'0') 
			              WHEN TRIM(COALESCE (OPE."TIR",'')) = '' THEN LPAD('',7,'0')
			              ELSE LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."TIR",26, 8)),'.'),3,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."TIR",26, 8)),'.'),1,4)
			              END AS "Tasa"
			             ,CASE WHEN OPE."IdTipoInstrumento" = 'SIM' THEN 
			                         CASE WHEN LTRIM(RTRIM(OPE."TIR"))<>'' THEN
			                           CASE WHEN   CAST (COALESCE(REPLACE(OPE."TIR",',','.'),'0') AS DECIMAL)  >=0 THEN '+' ELSE '-' END 
			                         ELSE  LPAD('',1,' ')  END
			                   ELSE LPAD('',1,' ')
			              END as "SignoTasa"
			              
			             -- ,''as "SignoTasa"
			             ,LPAD(OPE."RutCliente",11,'0') as "DocumentoIdentidad"      
			             ,CASE WHEN (IFNULL(CONVENIO."IdCliente",'0') <> '0' and OPE."IdTipoInstrumento" <> 'SIM') 
			               THEN 
			                  -- LPAD(SUBSTR_BEFORE (TO_CHAR(OPE."TotalTransadoSistema"),'.')||SUBSTRING(SUBSTR_AFTER(TO_CHAR(OPE."TotalTransadoSistema"),'.'),1,2),17,'0')
			                  LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."TotalTransadoSistema",26, 8)),'.'),15,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."TotalTransadoSistema",26, 8)),'.'),1,2) 
			               ELSE
			                  --LPAD(SUBSTR_BEFORE (TO_CHAR(OPE."UnidadesTransadasSistema"),'.'),17,'0')
			                  LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."UnidadesTransadasSistema",26, 8)),'.'),15,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."UnidadesTransadasSistema",26, 8)),'.'),1,2)
			              END as "Cantidad" 
			             ,CASE WHEN OPE."AccionOperacion"='COMPRA' 	THEN 'C'
						  WHEN OPE."AccionOperacion"='VENTA'  	THEN 'V'
						  WHEN OPE."AccionOperacion"='INGRESO'  THEN 'C'
						  WHEN OPE."AccionOperacion"='RETIRO'  	THEN 'V'
						  ELSE ' ' END AS "TipoOperacion"
			             ,case when OPE."HIdTipoInstrumento" IN ('RF','IF') then coalesce(LPAD(HOMOLOGACION_TIPO_REAJUSTE."ValorDestino",2,' '),LPAD('',2,' ')) else LPAD('',2,' ') end  as "TipoReajuste"
			             ,case when OPE."HIdTipoInstrumento" IN ('RF','IF') then 
			             --LPAD(SUBSTR_BEFORE (ROUND(OPE."TotalTransadoSistema",0),'.'),17,'0')
			             LPAD(SUBSTR_BEFORE(TO_CHAR(TO_DECIMAL(OPE."TotalTransadoSistema",26, 8)),'.'),15,'0')||''||SUBSTRING(SUBSTR_AFTER(TO_CHAR(TO_DECIMAL(OPE."TotalTransadoSistema",26, 8)),'.'),1,2)
			             else LPAD('',17,'0') end as "Monto"
			             ,case when OPE."HIdTipoInstrumento" IN ('RF','IF') then RPAD(OPE."IdEmisor",10,' ') else RPAD('',10,' ') end as "Emisor"
    		FROM "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064" OPE
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS CONVENIO 	ON (CONVENIO."IdCliente" = OPE."IdCliente"	AND CONVENIO."CodigoTipoIndicador" = 'DRCHCNV')
			--HOMOLOGACION
			LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" TIPOINS ---TIPOINS
			ON (CAST (OPE."IdTipoInstrumento" AS  nvarchar(100))= TIPOINS."ValorOrigen" AND TIPOINS."Tipo"='TipoInstrumento' AND TIPOINS."Esquema"='INFDERECH' AND TIPOINS."Version"=v_Version)
			LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMOLOGACION_TIPO_REAJUSTE
			ON (CAST (OPE."TipoUnidad"   AS  nvarchar(100))= HOMOLOGACION_TIPO_REAJUSTE."ValorOrigen" AND  HOMOLOGACION_TIPO_REAJUSTE."Tipo" ='TipoReajuste' AND HOMOLOGACION_TIPO_REAJUSTE."Esquema" = 'INFDERECH'
	     	AND HOMOLOGACION_TIPO_REAJUSTE."Version" = v_Version)
			WHERE "IdInterno"=v_out_IdInfoTran
			AND "EsEnRueda"='S' AND COALESCE(OPE."Folio",'0')<>'0'
    ) as D;
    
   
	--1er registro
	p_out_TablaSalida=
	select TOP 1 
	  '' as "TipoRegistro" , '' as "Folio" , '' as "Mercado" , '' as "CorredorCompra" , '' as "CorredorVenta", '' as "FechaVencimiento" , '' as "Nemotecnico", '' as "Precio", '' as "Tasa"
    , '' as "SignoTasa" , '' as "DocumentoIdentidad" ,'' as "Cantidad" , '' as "TipoOperacion" , '' as "TipoReajuste" , '' as "Monto" , '' as "Emisor",
	 RPAD('1'
           ||LPAD(CORREDORA."CodigoCorredoraFix",3,'0')       
           ||v_FechaOperacionDesde
           --||RPAD(CORREDORA."Nombre",10,' ')
           --||LPAD(DOC."DocumentoIdentidad",11,'0')
           ,89,' ') as "RegistroConcatenado"    
    from "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS CORREDORA
    inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS DOC
       ON DOC."IdPersona" = CORREDORA."IdPersona" WHERE  CORREDORA."IdBolsa"='BCS' AND CORREDORA."IdEmpresa"= v_IdEmpresa
    UNION ALL 
    select *
    from :datos where "TipoRegistro" <>'0'
    union all 
	select 
	  '' as "TipoRegistro" , '' as "Folio" , '' as "Mercado" , '' as "CorredorCompra" , '' as "CorredorVenta", '' as "FechaVencimiento" , '' as "Nemotecnico", '' as "Precio", '' as "Tasa"
    , '' as "SignoTasa" , '' as "DocumentoIdentidad" ,'' as "Cantidad" , '' as "TipoOperacion" , '' as "TipoReajuste" , '' as "Monto" , '' as "Emisor",

     RPAD('9'
     			--to_varchar((select sum(TO_BIGINT("Cantidad")) from :datos))
                ||LPAD(to_varchar((select count(1) from :datos)),6,'0')
                ,89,' ') as "RegistroConcatenado" 
     from dummy;      
    
     SELECT * FROM :p_out_TablaSalida;

END;