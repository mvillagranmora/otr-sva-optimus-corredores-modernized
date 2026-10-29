SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa" as
SELECT 
E."IdEtapa",
E."IdTipoEvento", 
E."Codigo",
E."Nombre",
E."Orden",
E."TipoAutorizacion",
CASE WHEN E."TipoAutorizacion" = 'T' THEN 'TODOS' 
	ELSE CAST(E."MinimoAutorizan" AS NVARCHAR) 
END AS "MinimoAutorizan",
E."IdEstado",
E."IdEmpresa",
EA."Descripcion" AS "Estado"
FROM "BSCL.Plataforma.BD::AUT.Etapa" E
LEFT JOIN "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" EA ON E."IdEstado" = EA."IdEstado" AND EA."NombreEntidad" = 'AUT.ETAPA';
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 