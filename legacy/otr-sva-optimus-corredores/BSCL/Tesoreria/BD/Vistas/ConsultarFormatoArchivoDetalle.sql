SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle" AS

SELECT 
	 FA."IdFormatoArchivo"
	,FA."CodigoFormatoArchivo"
	,FA."IdBanco"
	,BAN."NombreBanco"
 	,FA."IdMoneda"
	,FA."IdMedioDePago"
	,FA."FormatoSalida"
	,FA."Descripcion" AS "DescripcionFormato"
	,FA."IdEmpresa"
	,FA."TipoGlosa"	
	,FA."Servicio"	
	,FA."IdEstado"
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
	,FA."SeparadorCampos" -- Separador entre campos Ej. | (Solo para archivos TXT)	


FROM "BSCL.Tesoreria.BD::TES.FormatoArchivo" AS FA
INNER JOIN "BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" AS FAD ON (FA."IdFormatoArchivo" = FAD."IdFormatoArchivo")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BAN ON (FA."IdBanco" = BAN."IdBanco")

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;