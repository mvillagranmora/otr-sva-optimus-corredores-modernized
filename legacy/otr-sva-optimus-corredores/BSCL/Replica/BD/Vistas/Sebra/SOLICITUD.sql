SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD"; 
--select * from "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" WHERE PRO_ID = 'INOS';
CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" AS

SELECT
		ORD."IdOrden",ORD."IdEstadoOrden",ORD."IdOrden"  AS "SOL_SEC", -- TIPO :numeric
		0  AS "SOL_SEC_PADRE", -- TIPO :numeric
		coalesce (CAST(
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN POR."IdCliente"
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT),-1)  AS "PER_ID", -- TIPO :int
		CAST(COALESCE (POR."CodigoPortafolio" , '0' ) as Varchar)   AS "CUENTA", -- TIPO :int
		0  AS "FIL_ID", -- TIPO :int
		coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), '')  AS "RUT_CLI", -- TIPO :VARCHAR
		CASE ORD."IdTipoOperacion"
	WHEN 'COMACN' THEN 'ACCO'
	WHEN 'COMACX' THEN 'AOCO'
	WHEN 'COMCFI' THEN 'CFCO'
	WHEN 'COMETN' THEN 'ENCO'
	WHEN 'COMIIF' THEN 'IFCO'
	WHEN 'COMMON' THEN 'MOCO'
	WHEN 'COMMIL' THEN 'STCO'
	WHEN 'COMOSA' THEN 'COMO'
	WHEN 'COMRF' THEN 'RFCO'
	WHEN 'COMSIM' THEN 'ACCS'
	WHEN 'VENACN' THEN 'ACVE'
	WHEN 'VENACX' THEN 'AOVE'
	WHEN 'VENCFI' THEN 'CFVE'
	WHEN 'VENETN' THEN 'ENVE'
	WHEN 'VENIIF' THEN 'IFVE'
	WHEN 'VENMON' THEN 'MOVE'
	WHEN 'VENMIL' THEN 'STVE'
	WHEN 'VENRF' THEN 'RFVE'
	WHEN 'VENSIM' THEN 'ACVS' 
	WHEN 'VENOSA' THEN 'VENO' 
	ELSE 'XXXX'
	END AS OPE_ID,
    	cast(COALESCE(
			case when TPRO."IdTipoProducto"  = 'INSI' then 'INTS'
			when TPRO."IdTipoProducto"  = 'INCI' then 'INCF'
			when TPRO."IdTipoProducto"  = 'INAX' then 'INAO'
			when TPRO."IdTipoProducto"  = 'INAC' then 'INAC'
			when TPRO."IdTipoProducto"  = 'INCX' then 'INCO'
				when TPRO."IdTipoProducto"  = 'INFU' then 'INFU'
				when ORD."IdProducto"  = 'INEN' then 'INEN'
				when ORD."IdProducto"  = 'INEX' then 'INET'
				else  TPRO."IdTipoProducto"  end ,'') as varchar)   AS "PRO_ID", -- TIPO :char


		COALESCE(LEFT(CASE WHEN ORD."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN "IdClasificacionTipoInstrumento"
            			WHEN ORD."IdTipoInstrumento" IN ('OSA','SIM') THEN 'AC'
            			WHEN ORD."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN ORD."IdTipoInstrumento" = 'FWD' THEN 'FU'
            			WHEN ORD."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			ELSE COALESCE( hpr."ValorOrigen", ORD."IdTipoInstrumento")  END ,2),'')	 AS "MERCADO", -- TIPO :char 
		''	  AS "SOL_COD_GRUPO", -- TIPO :VARCHAR
		'CORR' AS "CDI_ID", -- TIPO :VARCHAR
		 CASE WHEN ORD."IdTipoCanalIngreso" = 'INT' then 'INT' else 'AGE' end    AS "CCA_CODIGO", -- TIPO :VARCHAR
		TO_VARCHAR (CASE WHEN ORD."FechaOperacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaOperacion" END ,'YYYY-MM-DD HH:MI:SS') AS "SOL_FECHA", -- TIPO :datetime
		
		COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "SOL_USERNAME_AGE_DUE", -- TIPO :VARCHAR
		
		COALESCE ( COD_EMPDUE_TRUN4."CodigoEmpleado" ,  '0000')  AS "SOL_COD_AGE_DUE", -- TIPO :VARCHAR
		
		COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "SOL_USERNAME_AGE_ATEN", -- TIPO :VARCHAR
		COALESCE ( COD_EMPATE_TRUN4."CodigoEmpleado" ,  '0000')    AS "SOL_COD_AGE_ATE", -- TIPO :VARCHAR
		
		COALESCE ( '',  '')  AS "SOL_USERNAME_SOLI", -- TIPO :VARCHAR
		
		COALESCE (0 , 0    )  AS "SOL_USU_SOLI", -- TIPO :int
		COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "SOL_USERNAME_AUT", -- TIPO :VARCHAR
		COALESCE (0 , 0    )  AS "SOL_USU_AUT", -- TIPO :int
		
		
		CAST(CASE WHEN trim(ORD."IdEstadoOrden")   = 'ANU'    THEN    'CA' 
                    WHEN trim(ORD."IdEstadoOrden") = 'OFE'  THEN 'CC'
                    WHEN trim(ORD."IdEstadoOrden") = 'ASC'  THEN  'EJ' 
                    WHEN trim(ORD."IdEstadoOrden") = 'VCD'  THEN 'EX'
                    WHEN trim(ORD."IdEstadoOrden") = 'EJE'  THEN 'EJ'
                    WHEN trim(ORD."IdEstadoOrden") = 'ENR'  THEN 'PE' 
                    WHEN trim(ORD."IdEstadoOrden") = 'RZD'  THEN  'VT'
                    ELSE ''
                END    AS Varchar) AS "SOL_ESTADO", -- TIPO :VARCHAR
		
		
		COALESCE (0 , 0    )  AS "SOL_FOL_SUBSIDIARIO", -- TIPO :numeric
		'' 					  AS "SOL_EST_SUBSIDIARIO", -- TIPO :VARCHAR
		CAST(CASE WHEN ORD."IdTipoVigenciaOrden" != 'SH' THEN 'P' ELSE 'D' END  AS Varchar)   AS "SOL_IND_PERMANENCIA", -- TIPO :VARCHAR
		
		TO_VARCHAR (CASE WHEN ORD."FechaVigencia" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaVigencia" end ,'YYYY-MM-DD')  AS "SOL_FEC_PERMANENCIA", -- TIPO :datetime
		
		COALESCE(ORD."Glosa" ,'') AS "SOL_GLOSA", -- TIPO :VARCHAR
		'N'	  AS "SOL_IND_MANCOMUNADA", -- TIPO :VARCHAR
		CAST(substr(TOPE."AccionOperacion",1,1)	AS varchar) AS "SOL_TIPO_OPERAC", -- TIPO :VARCHAR
		
		CAST(CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 then 'TS' when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'VC'
			ELSE '' END as Varchar) AS    "SOL_IND_SIMUL", -- TIPO :VARCHAR
		
		CAST(CASE when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'S'
		ELSE 'N' END as Varchar) 		AS "SOL_IND_VTA_CORTA", -- TIPO :VARCHAR
		
		ORD."IdOrden" 	 AS "SOL_SEC_ORDEN", -- TIPO :numeric
		''				  AS "SOL_TIPO_COMPROM", -- TIPO :VARCHAR
		
		CAST(substr(TOPE."AccionOperacion",1,2)	AS varchar)  AS "SOL_TIPO_OPER", -- TIPO :VARCHAR
		
		0	  AS "SOL_FPA", -- TIPO :int
		CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END   AS "SOL_IND_LIQUID", -- TIPO :VARCHAR
		
		COALESCE ("IdValorCobroComision" , 0    )  AS "SOL_VAL_COMISION", -- TIPO :numeric
		COALESCE ("IdValorCobroDerecho" , 0    )  AS "SOL_VAL_DERECHOS", -- TIPO :numeric
		COALESCE ("IdValorCobroGasto" , 0    )  AS "SOL_VAL_GASTOS", -- TIPO :numeric
		
		COALESCE ("IdCobroGasto",'GAEX'   )  AS "SOL_TIPO_GASTO", -- TIPO :VARCHAR
		COALESCE ("IdCobroComision",'COEX'   )  AS "SOL_TIPO_COMISION", -- TIPO :VARCHAR
		COALESCE ("IdCobroDerecho",'DEEX'   )  AS "SOL_TIPO_DERECHO", -- TIPO :VARCHAR
		
		CASE WHEN ORD."IdTipoCanalDistribucion" != 'MAN' then 'S'ELSE 'N' END    AS "SOL_IND_PREF_RUTEO", -- TIPO :VARCHAR
		
		
		CAST("IndicadorFacturacionInmediata" AS VARCHAR(1))  AS "SOL_IND_DESPACHO_FACTURA", -- TIPO :VARCHAR
		CASE WHEN "IdFormaCustodia" = 'CUS' THEN 'C' ELSE 'N' END   AS "SOL_IND_TIT_CUS", -- TIPO :VARCHAR
		'M'  AS "SOL_IND_TIPO_ASIGNAC", -- TIPO :VARCHAR
		'N'  AS "SOL_RUTEO_AGE", -- TIPO :VARCHAR
		'N'  AS "SOL_RUTEO_CLI", -- TIPO :VARCHAR
		'N'  AS "SOL_VALIDA_SAL_CLI", -- TIPO :VARCHAR
		'N'    	 AS "SOL_VALIDA_CUS_CLI", -- TIPO :VARCHAR
		'N'   	 AS "SOL_IND_PREOFER", -- TIPO :VARCHAR
		0 		 AS "SOL_USER_RESERVA", -- TIPO :decimal
		
		CAST(ORD."OrdenVigente" as Varchar)  AS "SOL_IND_VIGENTE", -- TIPO :VARCHAR
		'N'  AS "SOL_IND_CARTA", -- TIPO :VARCHAR
		
		CASE when "IdTipoCanalIngreso" = 'TRA' THEN 'S' ELSE 'N' end  AS "SOL_IND_POSTRERA", -- TIPO :VARCHAR
		
		'SU01'  AS "SOL_COD_SUC_AGE_DUE", -- TIPO :VARCHAR
		'SU01'  AS "SOL_COD_SUC_AGE_ATE", -- TIPO :VARCHAR
		
		''	 AS "SOL_COD_MOVTO", -- TIPO :VARCHAR
		0  AS "SOL_MTO_LINEA_DISP", -- TIPO :numeric
		'N'	  AS "SOL_IND_PREPAGO", -- TIPO :VARCHAR
		ORD."IdBolsaDistribucion"  	AS "SOL_BOLSA_DESTINO", -- TIPO :VARCHAR
		'N' 	 AS "SOL_IND_FIRMA_CLIENTE", -- TIPO :VARCHAR
		'N'    	 AS "SOL_IND_TRANSF", -- TIPO :VARCHAR
		0  		 AS "SOL_SEC_TRANSF", -- TIPO :numeric
		''  	 AS "SOL_IND_CUS_INSTRUMENTO", -- TIPO :VARCHAR
		''		 AS "SOL_USERNAME_RESERVA", -- TIPO :VARCHAR
		
		0		 AS "SOL_MIN_COMISION", -- TIPO :numeric
		0 		 AS "SOL_MIN_DERECHO", -- TIPO :numeric
		0 		 AS "SOL_COM_PLANTILLA", -- TIPO :numeric
		
		
		TO_VARCHAR (CASE WHEN ORD."FechaIngresoRegistro" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaIngresoRegistro" END ,'YYYY-MM-DD HH:MI:SS')   AS "SOL_FECHA_DIGITACION", -- TIPO :datetime
		
		0		 AS "SOL_VAL_IMPUESTO", -- TIPO :numeric
		
		ORD."EsConfirmada"	  AS "SOL_IND_CONFIRMADA", -- TIPO :VARCHAR
		'0'					  AS "CODCATEGORIAIFRS", -- TIPO :tinyint
		
		ORD."IdMonedaOrden"   AS "MON_ID", -- TIPO :VARCHAR
		
		'C'  				  AS "IND_TIT_CCTE", -- TIPO :VARCHAR
		0	 				  AS "SOL_CORRELATIVO_EVE", -- TIPO :int
		
		'1900-01-01' 		  AS "SOL_FEC_ULT_NOTIFICACION", -- TIPO :VARCHAR
		
		
		
		TO_VARCHAR (CASE WHEN ORD."FechaAceptacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaAceptacion" END ,'YYYY-MM-DD HH:MI:SS')    AS "SOL_FECHA_RECEPCION", -- TIPO :datetime
		
		''  AS "SOL_CATEGORIA_INVERSIONISTA", -- TIPO :VARCHAR
		
		TO_VARCHAR (CASE WHEN ORD."FechaAnulacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaAnulacion" END ,'YYYY-MM-DD HH:MI:SS')      AS "SOL_FECHA_ANULACION", -- TIPO :datetime
		
		COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "SOL_USERNAME_ANULACION", -- TIPO :VARCHAR
		
		ORD."IdTipoCanalDistribucion"  AS "SOL_CANAL", -- TIPO :VARCHAR
		
		''	  AS "ORDER_ID_HT", -- TIPO :VARCHAR
		'' 	  AS "SOL_TIPO_INGRESO", -- TIPO :VARCHAR
		0	  AS "SOL_MONTO_ESTIMADO" -- TIPO :numeric
					
		FROM "BSCL.Orden.BD::ORD.Orden" AS ORD --WHERE "IdOrden" = 317526
	
			LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
			                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
			                                                              AND hpr."ValorDestino" = ORD."IdTipoInstrumento"      AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET') )
			                                                              
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio" )
			--LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (ORD."DocumentoIdentidad" = DOC."DocumentoIdentidad")
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (ORD."IdCliente" = CLI."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (CLI."IdPersona" = PER."IdPersona")
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = ORD."IdTipoOperacion") 
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TPRO ON (TOPE."IdTipoProducto" = TPRO."IdTipoProducto")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = ORD."IdCondicionDeLiquidacion")											               
			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EmplDue ON (ORD."IdEmpleadoPortafolio"  = EmplDue."IdEmpleado")
			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EmplAte ON (ORD."IdEmpleadoDigitador"  = EmplAte."IdEmpleado")
			LEFT JOIN (
				SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
					SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
				) WHERE RANK < 10
			) AS COD_EMPDUE_TRUN4 ON COD_EMPDUE_TRUN4."IdEmpleado" = EmplDue."IdEmpleado"
			LEFT JOIN (
				SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
					SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
				) WHERE RANK < 10
			) AS COD_EMPATE_TRUN4 ON COD_EMPATE_TRUN4."IdEmpleado" = EmplAte."IdEmpleado"

			LEFT JOIN (
					SELECT  CO_OR."IdOrden"  , max( case when CO_CLA."CodigoCobroClasificacion" = 'COMISION' then CO_OR."IdCobro"  else '' end  ) as  "IdCobroComision" , 
											   max( case when CO_CLA."CodigoCobroClasificacion" = 'GASTO'    then CO_OR."IdCobro" else '' end  ) as  "IdCobroGasto" , 
											   max( case when CO_CLA."CodigoCobroClasificacion" = 'DERECHO'  then CO_OR."IdCobro" else '' end  ) as  "IdCobroDerecho", 

											   max( case when CO_CLA."CodigoCobroClasificacion" = 'COMISION' then "ValorCobroPerfil"  else 0 end  ) as  "IdValorCobroComision" , 
											   max( case when CO_CLA."CodigoCobroClasificacion" = 'GASTO'    then "ValorCobroPerfil" else 0 end  ) as  "IdValorCobroGasto" , 
											   max( case when CO_CLA."CodigoCobroClasificacion" = 'DERECHO'  then "ValorCobroPerfil" else 0 end  ) as  "IdValorCobroDerecho"											   
 
					FROM "BSCL.Orden.BD::ORD.CobroOrden"  as CO_OR
							JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CO ON (CO_OR."IdCobro" = CO."IdCobro")
							JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CO_CLA ON (CO."IdCobroClasificacion" = CO_CLA."IdCobroClasificacion")
					group by CO_OR."IdOrden"
				)
				AS COB_OR ON (ORD."IdOrden" = COB_OR."IdOrden")			
		WHERE TOPE."IdTipoOperacion" != 'VENSIM'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
