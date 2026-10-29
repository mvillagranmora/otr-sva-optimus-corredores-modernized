SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Asig";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Asig"
( 
    -- Parametros de Entrada
    IN p_IdSolicitudCircular BIGINT DEFAULT 0, 
    --VARIABLES DE SALIDA 
    OUT p_out_TablaSalida "BSCL.Normativa.BD::NOR.SALIDA_NCG380ASIG"
)   
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL" 
AS
BEGIN
    DECLARE v_FechaInicio,v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_IdEmpresa INT:=0;
    DECLARE v_Version,v_FechaVersion  NVARCHAR(10) DEFAULT '';          

    --Se obtiene la fecha de inicio y fecha termino
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX("FechaInicio"),'0001-01-01') AS DATE),
        CAST(MAX("FechaTermino") AS DATE)
        INTO v_IdEmpresa,v_FechaInicio,v_FechaTermino
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino"
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER'))
    );
        
    IF (:v_FechaInicio = '' OR :v_FechaInicio='0001-01-01') THEN
        v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
    ELSE --Se obtiene segun fecha 
        v_FechaVersion :=TO_VARCHAR (:v_FechaInicio, 'DD-MM-YYYY');
    END IF; 
    
    SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('NCG380ASIG',:v_FechaVersion);
    
    	
        
    p_out_TablaSalida =
    SELECT
    	c."DocumentoIdentidad" 					AS "Rut",
		c."CodigoPortafolio"					AS "Cuenta",
		c."NombreCompleto" 						AS "Nombre",
		c."IdBolsa" 						AS "Bolsa",
		c."FolioTransaccion" 					AS "Folio",
		c."Instrumento" 						AS "Instrumento",
		LEFT(IFNULL(c."IdTipoOperacion",''),1) 	AS "TipoOperacion",
		IFNULL(c."IdOrden",0)  					AS "NumeroOrden",
		c."FolioTransaccion" 					AS "NumeroOperacion" , --Folio del Palo
		CASE 
			WHEN c."ValorRescateAsignado" > 0 THEN c."ValorRescateAsignado"
			ELSE IFNULL(c."CantidadAsignada",0)
		END	AS "UnidadesAsignadas",
		IFNULL(c."MontoAsignado",0)				AS "MontoTotal",
		CASE WHEN c."IdClasificacionTipoInstrumento" = 'IF' THEN c."Tasa" ELSE IFNULL(c."PrecioAsignacion",0) END AS "Precio",
		CASE
            WHEN SUBSTRING(c."IdMonedaOperacion", 1, 3) = 'CLP' THEN '$$' || COALESCE(SUBSTRING(c."IdMonedaOperacion", 4), '')
            WHEN SUBSTRING(c."IdMonedaOperacion", 1, 3) = 'AUD' THEN 'DA' || COALESCE(SUBSTRING(c."IdMonedaOperacion", 4), '')
            ELSE COALESCE(c."IdMonedaOperacion",'')
        END AS "Moneda",
		IFNULL(c."FechaAsignacion",'') 			AS "FechaAsignacion",
		TO_CHAR(UTCTOLOCAL(c."FechaAsignacion", 'America/Santiago','platform'), 'HH24:MI:SS') AS "HoraAsignacion" ,
		IFNULL(c."TipoOperacionDetalle",'') AS "TipoOperacionDetalle",
		COALESCE(c."FolioOrden",'0')  		AS "FolioOrden",
		c."InstrumentoNegociacion" AS "InstrumentoNegociacion" 
    FROM (
        SELECT
			c."IdOrden", c."IdTransaccion",c."DocumentoIdentidad", CASE WHEN c."IdTipoInstrumento" = 'SIM' THEN c."InstrumentoSubyacente" WHEN c."IdTipoInstrumento" = 'PAG' THEN ord."NemotecnicoSVS" ELSE c."Instrumento" END AS "Instrumento", c."IdAsignacion",c."FechaAsignacion",c."IdCliente",c."IdInstrumento",c."IdClasificacionTipoInstrumento"
		   ,c."IdTipoOperacion",c."CantidadAsignada", c."MontoAsignado", c."IdMonedaOperacion",c."CodigoPortafolio",c."NombreCompleto",c."IdBolsa",c."FolioTransaccion",c."PrecioAsignacion", CASE WHEN c."IdTipoInstrumento" = 'SIM' THEN c."Instrumento" ELSE '' END AS "TipoOperacionDetalle"
           ,c."ValorRescateAsignado",c."FolioOrden", c."InstrumentoNegociacion", c."Tasa"
        FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" AS c
        LEFT JOIN "BSCL.Orden.BD::ORD.CobroAsignacion" AS COB ON (COB."IdAsignacion" = c."IdAsignacion")   
        INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'RUT') AS r ON (r."Valor" IS NULL OR r."IdSistema" = c."IdCliente")
        INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'NEMO') AS n ON (n."Valor" IS NULL OR n."IdSistema" = c."IdInstrumento")
        INNER JOIN (SELECT "Valor" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'CLAINS') AS m ON (m."Valor" IS NULL OR m."Valor" = c."IdClasificacionTipoInstrumento")
    	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = c."IdCliente"
    	LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ord ON (ord."IdOrden" = c."IdOrden" AND c."IdTipoInstrumento" = 'PAG')
    	WHERE CLIENTE."TipoEnrolamiento" = 'FULL' AND c."IdEstadoAsignacion" != 'ANU'
GROUP BY 
 c."IdOrden", c."IdTransaccion",c."DocumentoIdentidad", c."IdTipoInstrumento",c."InstrumentoSubyacente", c."Instrumento", c."IdAsignacion",c."FechaAsignacion",c."IdCliente",c."IdInstrumento",c."IdClasificacionTipoInstrumento"
,c."IdTipoOperacion",c."CantidadAsignada", c."CodigoTipoOperacionAccion", c."MontoAsignado"
,c."IdMonedaOperacion",c."CodigoPortafolio",c."NombreCompleto",c."IdBolsa",c."FolioTransaccion",c."PrecioAsignacion",c."Instrumento"  
,c."ValorRescateAsignado",c."FolioOrden", c."InstrumentoNegociacion", c."Tasa", ord."NemotecnicoSVS"
    ) AS c
    --homologacion
    WHERE (c."FechaAsignacion"  BETWEEN :v_FechaInicio AND  :v_FechaTermino)    
    ORDER BY c."FechaAsignacion", c."IdOrden" ASC;       
END;