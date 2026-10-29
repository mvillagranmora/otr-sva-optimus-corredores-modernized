SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo";                                     
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo" AS

SELECT
	 FA."IdFormatoArchivo"
	,FA."FormatoSalida"    	-- TXT | CSV
	,FA."Descripcion"
	,FA."IdEmpresa"
	,FA."Servicio"         	-- Serviocio que entyrega la data para generar el archivo
	,FA."IdEstado"
	,FA."CodigoFormatoArchivo"
	,FA."SeparadorCampos"   -- Separador entre campos Ej. | (Solo para archivos TXT)	
	,FA."SeparadorDecimales" -- Caracter para separar decimales (Ej. Punto: 999.99   Coma: 999,99    Vacio: 99999)	
	,FA."RellenarCampo"		-- S/N	Indica si se rellena el campo.  Para NUM 0 a la izquierda (00099)  Para CHAR espacios a la derecha ('XX   ')
	
	,FA."NombreArchivoSalida"                    -- El JOB le concatenará la fecha, Hora y extensión  Ej: <NombreArchivoSalida>_20171025_143256.TXT
	,COALESCE(FAD."CantidadDeTiposDeRegistro",0) AS "CantidadDeTiposDeRegistro"  -- cantidad de formatos o tipos de línea para un archivo
	
	-- Rutas para almacenar y descargar archivo
	,COALESCE(CON."Valor",'') AS "RutaBase"      -- Ej: /mnt/optimus/des/empresa1/   (se usa para descargar los archivos creados) 
	,COALESCE(CCTA."Valor",'') AS "RutaModulo"    -- Ej: cta/	
	,(CASE
		WHEN (COALESCE(CON."Valor",'') <> '' AND  COALESCE(CCTA."Valor",'') <> '') THEN COALESCE(CON."Valor",'') || COALESCE(CCTA."Valor",'')
		ELSE ''
	END) AS "RutaCompleta"  -- Ej: /mnt/optimus/des/empresa1/cta/     (se usa para descargar los archivos creados)
	,FA."AgregoNombreColumna"
FROM "BSCL.CuentaInversion.BD::CTA.FormatoArchivo" AS FA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" AS CON ON (UCASE(CON."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND CON."IdEmpresa" = FA."IdEmpresa")
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" AS CCTA ON (UCASE(CCTA."CodigoParametro") = 'RUTAARCHIVOSCTA' AND CCTA."IdEmpresa" = FA."IdEmpresa")
LEFT JOIN (SELECT "IdFormatoArchivo", COUNT("TipoRegistro") AS "CantidadDeTiposDeRegistro" 
		   FROM (SELECT DISTINCT "IdFormatoArchivo", "TipoRegistro" FROM "BSCL.CuentaInversion.BD::CTA.FormatoArchivoDetalle")
		   GROUP BY "IdFormatoArchivo") AS FAD ON (FAD."IdFormatoArchivo" = FA."IdFormatoArchivo")
WITH READ ONLY;

GO 
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

