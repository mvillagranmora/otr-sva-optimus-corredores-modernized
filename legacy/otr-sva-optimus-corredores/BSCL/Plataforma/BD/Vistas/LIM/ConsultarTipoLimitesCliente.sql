SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimitesCliente"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimitesCliente"  
AS
SELECT  "IdTipoLimite" 
	   ,"Nombre"
	   ,"IdEstado"
  FROM "BSCL.Plataforma.BD::LIM.TipoLimite"
WHERE "IdGrupoLimite" = 'CLIENTE';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimitesCliente" TO BCS_DESARROLLADOR WITH GRANT OPTION;