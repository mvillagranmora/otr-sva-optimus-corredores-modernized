SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
	,"DatosPersonaRut"							
	,"DatosPersonaDV"							
	,"DatosPersonaIndentificadorExtranjero"		
	,"DatosPersonaPaisOtorganteIdentificador"	
	,"DatosPersonaPaisOtorganteIdentificadorDescripcion"--
	,"DatosPersonaNombreRazonSocial"				
	,"TipoTransaccionDJ"		
	,"TipoTransaccionDJDescripcion"
	,"ValoresTransados"			
	,"ValoresTransadosDescripcion"
	,"FechaTransaccionDia"						
	,"FechaTransaccionMes"						
	,"FechaTransaccionAnio"						
	,"DocumentoTipoDocumento"	
	,"DocumentoTipoDocumentoDescripcion"
	,"DocumentoNumero"							
	,"CodigoMonedaTransada"			
	,"CodigoMonedaTransadaDescripcion"			
	,"MontoCompras"								
	,"MontoVentas"								
	,"TipoCambio"																				
	,"IdCliente"
	,"IdPortafolio"
	,"FechaDocumento"
	,"IndNacionalidad" -- 'N'
	,"Paridad"
	,"LineaFactura"


) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
    ,a."FuenteDatos"							
	,a."DatosPersonaRut"							
	,a."DatosPersonaDV"							
	,a."DatosPersonaIndentificadorExtranjero"		
	,a."DatosPersonaPaisOtorganteIdentificador"	
	,COALESCE(d."Descripcion",'')			 as "DatosPersonaPaisOtorganteIdentificadorDescripcion"
	,a."DatosPersonaNombreRazonSocial"				
	,a."TipoTransaccionDJ"		   AS "TipoTransaccionDJ"
	--,COALESCE(b."ValorDestino",'X') AS "TipoTransaccionDJ"							
	,COALESCE(b."Descripcion",'') AS "TipoTransaccionDJDescripcion" -- 
	,a."ValoresTransados"		
	,COALESCE(e."Descripcion",'') AS "ValoresTransadosDescripcion"	
	,CAST (a."FechaTransaccionDia" AS VARCHAR)  AS "FechaTransaccionDia"	
	,CAST (a."FechaTransaccionMes" AS VARCHAR)  AS "FechaTransaccionMes"
	,CAST (a."FechaTransaccionAnio" AS VARCHAR) AS "FechaTransaccionAnio"
	--,a."DocumentoTipoDocumento"	 AS "DocumentoTipoDocumento"				
	,c."ValorDestino" AS "DocumentoTipoDocumento"
	,COALESCE(c."Descripcion",'') AS "DocumentoTipoDocumentoDescripcion"
	,a."DocumentoNumero"							
	,a."CodigoMonedaTransada"		
	,COALESCE(MON."Descripcion",'') AS "CodigoMonedaTransadaDescripcion"				
	,a."MontoCompras"	 AS "MontoCompras"							
	,a."MontoVentas"	AS "MontoVentas"						
	,ROUND(a."TipoCambio",2)																												
	,COALESCE(a."IdCliente",0)
	,COALESCE(a."IdPortafolio",0)
	--,a."TipoTransaccion"
--	,COALESCE(c."Descripcion",'X') AS"TipoTransaccion"
	,a."FechaDocumento"
	,COALESCE(a."IndNacionalidad",'N') -- 'N'
	,a."Paridad"
	,a."LineaFactura"


FROM "BSCL.Normativa.BD::TRI.DatosDJ1870" a

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" b 
ON (a."TipoTransaccionDJ" = b."ValorOrigen" AND b."Esquema"='DJ1870' AND b."Tipo" = 'TIPOTRANSACCION' AND a."FechaDocumento" BETWEEN b."FechaDatosDesde" AND b."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" c
ON (a."DocumentoTipoDocumento" = c."ValorOrigen" AND c."Esquema"='DJ1870' AND c."Tipo" = 'TIPODOCUMENTO' AND a."FechaDocumento" BETWEEN c."FechaDatosDesde" AND c."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" d
ON (a."DatosPersonaPaisOtorganteIdentificador" = d."ValorOrigen" AND d."Esquema"='DJ1870' AND d."Tipo" = 'PAISOTORGANTE' AND a."FechaDocumento" BETWEEN d."FechaDatosDesde" AND d."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" e
ON (a."ValoresTransados" = e."ValorOrigen" AND e."Esquema"='DJ1870' AND e."Tipo" = 'VALORESTRANSADOS' AND a."FechaDocumento" BETWEEN e."FechaDatosDesde" AND e."FechaDatosHasta")

LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" MON ON (MON."Tipo"='DJ1870MONEDAS' and MON."Codigo"=a."CodigoMonedaTransada")

LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1870" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"

WHERE "FuenteDatos" IN ('INICIAL','MANTENEDOR')



UNION ALL

SELECT DISTINCT 
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
    ,a."FuenteDatos"
	,a."DatosPersonaRut"							
	,a."DatosPersonaDV"							
	,a."DatosPersonaIndentificadorExtranjero"		
	,a."DatosPersonaPaisOtorganteIdentificador"	
	,COALESCE(d."Descripcion",'')			 as "DatosPersonaPaisOtorganteIdentificadorDescripcion"
	,a."DatosPersonaNombreRazonSocial"				
	,a."TipoTransaccionDJ"   AS "TipoTransaccionDJ"
	--,COALESCE(a."TipoTransaccionDJ",'X') AS "TipoTransaccionDJ"							
	,COALESCE(b."Descripcion",'') AS "TipoTransaccionDJDescripcion" -- 
	,a."ValoresTransados"							
	,COALESCE(e."Descripcion",'') AS "ValoresTransadosDescripcion"		
	,CAST (a."FechaTransaccionDia" AS VARCHAR) AS "FechaTransaccionDia"
	,CAST (a."FechaTransaccionMes" AS VARCHAR) AS "FechaTransaccionMes"
	,CAST (a."FechaTransaccionAnio" AS VARCHAR) AS "FechaTransaccionAnio"					
	,a."DocumentoTipoDocumento" AS "DocumentoTipoDocumento"
	,COALESCE(c."Descripcion",'') AS "DocumentoTipoDocumentoDescripcion"
	,a."DocumentoNumero"							
	,a."CodigoMonedaTransada"	
	,COALESCE(MON."Descripcion",'') AS "CodigoMonedaTransadaDescripcion"					
	,a."MontoCompras"	 AS "MontoCompras"							
	,a."MontoVentas"	 AS "MontoVentas"						
	,ROUND(a."TipoCambio",2)																												
	,a."IdCliente"
	,a."IdPortafolio"
--	,a."TipoTransaccion"
--	,COALESCE(c."Descripcion",'X') AS"TipoTransaccion"
	,a."FechaDocumento"
	,a."IndNacionalidad" -- 'N'
	,a."Paridad"
	,a."LineaFactura"
 
FROM "BSCL.Normativa.BD::TRI.DatosDJ1870" a

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" b 
ON (a."TipoTransaccionDJ" = b."ValorOrigen" AND b."Esquema"='DJ1870' AND b."Tipo" = 'TIPOTRANSACCION' AND a."FechaDocumento" BETWEEN b."FechaDatosDesde" AND b."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" c
ON (a."DocumentoTipoDocumento" = c."ValorOrigen" AND c."Esquema"='DJ1870' AND c."Tipo" = 'TIPODOCUMENTO' AND a."FechaDocumento" BETWEEN c."FechaDatosDesde" AND c."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" d
ON (a."DatosPersonaPaisOtorganteIdentificador" = d."ValorOrigen" AND d."Esquema"='DJ1870' AND d."Tipo" = 'PAISOTORGANTE' AND a."FechaDocumento" BETWEEN d."FechaDatosDesde" AND d."FechaDatosHasta")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" e
ON (a."ValoresTransados" = e."ValorOrigen" AND e."Esquema"='DJ1870' AND e."Tipo" = 'VALORESTRANSADOS' AND a."FechaDocumento" BETWEEN e."FechaDatosDesde" AND e."FechaDatosHasta")

LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" MON ON (MON."Tipo"='DJ1870MONEDAS' and MON."Codigo"=a."CodigoMonedaTransada")

LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1870" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
		
WHERE "FuenteDatos" = 'EXTERNA'


WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Datos" TO bcs_desarrollador WITH GRANT OPTION;
