SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.ORDEN"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.ORDEN" AS

SELECT
	ORD."IdOrden", 
	COALESCE (ORD."IdOrden" , 0)  AS "ORD_SEC", -- TIPO :numeric
	COALESCE (ORD."IdOrden" , 0    )  AS "SOL_SEC", -- TIPO :numeric
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
	--FALTA ABRIR VENSIM POR ETF CFI
	--FALTA VENTA CORTA
	
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
			 
			 
	LEFT(COALESCE( hpr."ValorOrigen", 
				case 	
						when TPRO."IdTipoProducto"  = 'INRF' then 'RF'
						when TPRO."IdTipoProducto"  = 'INSI' then 'AC'
						when TPRO."IdTipoProducto"  = 'INFU' then 'FU'
						when ORD."IdProducto"  = 'INEN' then 'EN'
						when ORD."IdProducto"  = 'INEX' then 'ET'
					 else  TPRO."IdTipoProducto"  END)
				, 2) AS "MERCADO", -- TIPO :char
	
	coalesce (CAST(
		CASE 
			WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN POR."IdCliente"
			ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT),-1)  AS "PER_ID", -- TIPO :int

	CAST(COALESCE (POR."CodigoPortafolio" , '-1' ) as Varchar)  AS "CUENTA", -- TIPO :int CAMBIAR DATO DESTINO 
	CAST(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( (DOC."DocumentoIdentidad") AS VARCHAR(11))), '') AS Varchar)  AS "RUT_CLI", -- TIPO :varchar
	0  AS "FIL_ID", -- TIPO :int
	CAST(CASE WHEN ORD."IdTipoCanalDistribucion" = 'MAN' THEN
	 														   
	 														   CASE WHEN trim(ORD."IdEstadoOrden")   = 'ANU'    THEN    'CA' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'OFE'  THEN 'CC'
									                                WHEN trim(ORD."IdEstadoOrden") = 'ASC'  THEN  'EJ' 
													WHEN trim(ORD."IdEstadoOrden") = 'EJE'  THEN  'EJ' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'VCD'  THEN 'EX'
									                                WHEN trim(ORD."IdEstadoOrden") = 'ENR'  THEN 'PE' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'RZD'  THEN  'VT'
									                                ELSE ''
									                            END
	 												        ELSE '' END 
	 														    AS Varchar)   AS "ORD_ESTADO", -- TIPO :varchar //Falta Homologar
	 CAST(CASE WHEN ORD."IdTipoCanalDistribucion" != 'MAN' THEN 
	                                                            CASE WHEN trim(ORD."IdEstadoOrden")    = 'ANU'    THEN    'CA' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'OFE'  THEN 'CC'
									                                WHEN trim(ORD."IdEstadoOrden") = 'ASC'  THEN  'EJ' 
													WHEN trim(ORD."IdEstadoOrden") = 'EJE'  THEN  'EJ' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'VCD'  THEN 'EX'
									                                WHEN trim(ORD."IdEstadoOrden") = 'ENR'  THEN 'PE' 
									                                WHEN trim(ORD."IdEstadoOrden") = 'RZD'  THEN  'VT'
									                                ELSE ''
									                            END
	 														  ELSE '' END AS Varchar) AS "ORD_ESTADO_RUTEO", -- TIPO :varchar
	
	CASE WHEN COALESCE(ASIG."CantidadAsignada",0) = ORD."Cantidad" THEN 'AC' 
		WHEN COALESCE(ASIG."CantidadAsignada",0) < ORD."Cantidad" THEN 'PA' ELSE 'NA' END AS "ORD_ESTADO_ASIGNACION", -- TIPO :varchar REVISAR  
    CASE WHEN ORD."IdTipoCanalIngreso" = 'CLI' then 'CLI' else 'AGE' end  AS "CCA_CODIGO",
	'CORR' AS "CDI_ID", -- TIPO :char IDTIPOCANALDEDISTRIBUCION
	TO_VARCHAR (CASE WHEN ORD."FechaOperacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaOperacion" END ,'YYYY-MM-DD HH:MI:SS')  AS "ORD_FEC",	
	case ORD."Folio" when '' then '0' else COALESCE(ORD."Folio" , '0') end as ORD_FOLIO,
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORD_IND_IMPRESION", -- TIPO :char X
	CAST(CASE WHEN ORD."IdTipoVigenciaOrden" != 'SH' THEN 'P' ELSE 'D' END  AS Varchar) AS "ORD_IND_PERMANENCIA", -- TIPO :char 
	TO_VARCHAR (CASE WHEN ORD."FechaVigencia" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaVigencia" end ,'YYYY-MM-DD') AS "ORD_FEC_PERMANENCIA",	
	CAST(substr(TOPE."AccionOperacion",1,1) AS Varchar)  AS "ORD_TIPO_OPERAC", -- TIPO :char  

	CAST(CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 then 'TS' when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'VC'
		ELSE '' END as Varchar) AS "ORD_IND_SIMUL", -- TIPO :char
		
	CAST(CASE when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'S'
		ELSE 'N' END as Varchar) AS "ORD_IND_VTA_CORTA", -- TIPO :char
	CAST(ORD."OrdenVigente" as Varchar)  AS "ORD_IND_VIGENTE", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "COD_TRADER", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "CANAL_DIST", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORD_EXTERNAL_FOL", -- TIPO :varchar
	COALESCE ('1900-01-01','1900-01-01'     )  AS "ORD_EXTERNAL_FEC" -- TIPO :datetime

	
FROM "BSCL.Orden.BD::ORD.Orden" AS ORD 
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = ORD."IdTipoInstrumento"   AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET')   )
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (POR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = ORD."IdTipoOperacion") 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TPRO ON (TOPE."IdTipoProducto" = TPRO."IdTipoProducto")                   							  
LEFT JOIN (
		  SELECT  ASI."IdOrden", SUM( ASI."CantidadAsignada") AS  "CantidadAsignada"
		  FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
		  WHERE ASI."IdEstadoAsignacion" != 'ANU' AND ASI."EsAPlazo" = 'N'
		  GROUP BY ASI."IdOrden"
		  )
          as ASIG ON (ORD."IdOrden" = ASIG."IdOrden")
WHERE TOPE."IdTipoOperacion" IS NOT NULL
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.ORDEN" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
