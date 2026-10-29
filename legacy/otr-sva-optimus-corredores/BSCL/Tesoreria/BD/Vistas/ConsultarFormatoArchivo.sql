SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo";
GO 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" AS

SELECT
	 FA."IdFormatoArchivo"
	,FA."CodigoFormatoArchivo"
	,FA."IdBanco"
	,BAN."NombreBanco"
 	,FA."IdMoneda"
	,FA."IdMedioDePago"
	,FA."FormatoSalida"
	,FA."Descripcion"
	,FA."IdEmpresa"
	,FA."TipoGlosa"
	,FA."Servicio"
	,FA."IdEstado"	
	,FA."SeparadorCampos"     -- Separador entre campos Ej. | (Solo para archivos TXT)	
	,FA."SeparadorDecimales"  -- Caracter para separar decimales (Ej. Punto: 999.99   Coma: 999,99    Vacio: 99999)
	,FA."RellenarCampo"		  -- S/N	Indica si se rellena el campo.  Para NUM 0 a la izquierda (00099)  Para CHAR espacios a la derecha ('XX   ')
	,FA."TipoSaltoLinea"	  -- L/W	Indica si el salto de linea es en formato Windows (CRLF) o Linux (LF).
				
	-- Rutas para guardar y descargar archivo
	,COALESCE(CP."Valor",'') AS "RutaBase"       -- Ej: /mnt/optimus/des/empresa1/   (se usa para descargar el archivo creado) 
	,COALESCE(CT."Valor",'') AS "RutaTesoreria"  -- Ej: tes/ 
	,(CASE
		WHEN (COALESCE(CP."Valor",'') <> '' AND  COALESCE(CT."Valor",'') <> '') THEN COALESCE(CP."Valor",'') || COALESCE(CT."Valor",'')
		ELSE ''
	END) AS "RutaCompleta"  -- Ej: /mnt/optimus/des/empresa1/tes/    (se usa para crear el archivo)
	
	,FA."NombreArchivoSalida"                    -- El JOB le agregará la fecha, Hora y extensión Ej: Scotia_TeleBanking_20171025_143256.TXT
	,COALESCE(FAD."CantidadDeTiposDeRegistro",0) AS "CantidadDeTiposDeRegistro"  -- cantidad de formatos o tipos de línea para un archivo

FROM "BSCL.Tesoreria.BD::TES.FormatoArchivo" AS FA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BAN ON (FA."IdBanco" = BAN."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" AS CP ON (UCASE(CP."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND CP."IdEmpresa" = FA."IdEmpresa")
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria" AS CT ON (UCASE(CT."CodigoParametro") = 'RUTAARCHIVOTRANSFERENCIAS' AND CT."IdEmpresa" = FA."IdEmpresa")

LEFT JOIN (
		SELECT "IdFormatoArchivo", COUNT("TipoRegistro") AS "CantidadDeTiposDeRegistro" 
		FROM (SELECT DISTINCT "IdFormatoArchivo", "TipoRegistro" FROM "BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle")
		GROUP BY "IdFormatoArchivo") AS FAD ON (FAD."IdFormatoArchivo" = FA."IdFormatoArchivo")

WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

