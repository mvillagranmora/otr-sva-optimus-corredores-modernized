SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle";   
GO
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle" AS

SELECT 
	 FA."IdFormatoArchivo"
	,FA."FormatoSalida"	
	,FA."CodigoFormatoArchivo"
	,FA."SeparadorCampos"
	,FA."Descripcion" AS "DescripcionFormato"
	,FA."IdEmpresa"
	,FA."Servicio"	
	,FA."IdEstado"
	,FA."NombreArchivoSalida"
    ,FAD."IdFormatoArchivoDetalle"
	,FAD."TipoRegistro"
	,FAD."Campo"
	,FAD."TipoDato"
	,FAD."Largo"
	,FAD."Decimales"
	,FAD."ValorDefecto"
	,FAD."Orden"
	,FAD."Descripcion" AS  "DescripcionFormatoDetalle"
	,FAD."Formato"
	
FROM "BSCL.CuentaInversion.BD::CTA.FormatoArchivo" AS FA
INNER JOIN "BSCL.CuentaInversion.BD::CTA.FormatoArchivoDetalle" AS FAD ON (FA."IdFormatoArchivo" = FAD."IdFormatoArchivo")
ORDER BY FAD."IdFormatoArchivo", FAD."Orden"

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

