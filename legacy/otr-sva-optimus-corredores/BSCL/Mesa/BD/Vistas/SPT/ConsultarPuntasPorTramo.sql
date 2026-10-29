SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramo";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramo"
AS 
SELECT
  PPT."IdPuntasPorTramo" 
, PPT."IdEmpresa"  
, PPT."IdPuntas"  
, PPT."MontoPorTramo"  
, PPT."PorcentajeTramo" 
, PPT."MontoCentavos"   
, PPT."ValorCompra"   
, PPT."ValorVenta"   
, PPT."Estado"   
, PPT."IdUsuario"
, US."CodigoUsuario" as "Usuario"  
, PPT."FechaModificacion"
, PPT."Canal"
, PTA."IdMonedaPunta"   
FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS PPT
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" AS PTA
 ON PTA."IdPuntas" = PPT."IdPuntas"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS US
 ON US."IdUsuario" = CAST(PPT."IdUsuario" AS BIGINT);

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramo" TO BCS_DESARROLLADOR WITH GRANT OPTION;
