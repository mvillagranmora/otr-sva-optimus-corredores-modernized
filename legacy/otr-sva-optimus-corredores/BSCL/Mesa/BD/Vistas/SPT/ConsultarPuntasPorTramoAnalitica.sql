SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramoAnalitica";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramoAnalitica"
AS 
SELECT 
PPT."IdPuntasPorTramo" 
, COALESCE(PPT."MontoPorTramo",0) AS "Monto" 
, COALESCE(PPT."MontoCentavos",0) AS "AperturaPesos"
, COALESCE(PPT."ValorCompra",0) AS "MesaCompra"
, COALESCE(PPT."ValorVenta",0) AS "MesaVenta"
, COALESCE(US."CodigoUsuario",'') AS "UsuarioModifica" 
, COALESCE(PPT."FechaModificacion",'0001-01-01 00:00:00') AS "FechaModificacion"
, COALESCE(PPT."Canal",'') AS "Canal" 
, COALESCE(PTA."IdMonedaPunta",'') AS "IdMonedaPunta" 
FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS PPT 
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" AS PTA ON PTA."IdPuntas" = PPT."IdPuntas" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS US ON US."IdUsuario" = CAST(PPT."IdUsuario" AS BIGINT)
WHERE PPT."Estado" ='VIG';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramoAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;
