SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCodigoIFRS"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCodigoIFRS"
AS

	SELECT
		  "Codigo"
		  ,"Descripcion"
		  ,"Codigo" || ' - ' || "Descripcion" AS "CodigoDescripcion"
		  ,"CodigoValorizacion"
		  ,"DescripcionValorizacion"
	FROM "BSCL.Plataforma.BD::PLA.CodigoIFRS";

GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCodigoIFRS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO