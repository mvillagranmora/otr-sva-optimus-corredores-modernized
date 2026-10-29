SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_PuntasPorTramo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMesa_PuntasPorTramo" AS

SELECT 
	WST."IdStack" AS "IDSTACK", 
	"IdPuntasPorTramo" AS TABLE_KEY, 
	COALESCE("Action",'') AS ACTION, 
	"IdPuntasPorTramo" AS "IDPUNTASPORTRAMO", 		 --BIGINT, 
	COALESCE("Monto",0) AS "MONTO", 				 --DECIMAL, 
	COALESCE("AperturaPesos",0) AS "APERTURAPESOS", --DECIMAL, 
	COALESCE("MesaCompra",0) AS "MESACOMPRA", 		 --DECIMAL, 
	COALESCE("MesaVenta",0) AS "MESAVENTA", 		 --DECIMAL, 
	COALESCE("UsuarioModifica",'') AS "USUARIOMODIFICA", --NVARCHAR(100), 
	COALESCE("FechaModificacion",'1900-01-01 00:00:00') AS "FECHAMODIFICACION", --SECONDDATE, 
	COALESCE("Canal",'') AS "CANAL", 				 --NVARCHAR(100), 
	COALESCE("IdMonedaPunta",'') AS "IDMONEDAPUNTA" --NVARCHAR(3), 
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramoAnalitica"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Mesa_PuntasPorTramo' AND WST."Key" = "IdPuntasPorTramo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMesa_PuntasPorTramo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
