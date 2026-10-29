SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarEventoCambioPlanActivoSAAPV";
GO
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarEventoCambioPlanActivoSAAPV"  AS 

SELECT  
	"IdEvento"
	,"IdCliente"
	,"IdEstadoAutorizacion"
	,"FechaHoraRegistro"
	,"EstadoAutorizacion"
	,"Rank"
FROM (
	SELECT  
	EVT."IdEvento"
	,EVT."IdCliente"
	,EVT."IdEstado" AS "IdEstadoAutorizacion"
	,EVT."FechaHoraRegistro"
	,EST."Nombre" AS "EstadoAutorizacion"
	,RANK() OVER (PARTITION BY EVT."IdCliente" ORDER BY EVT."FechaHoraRegistro" DESC) AS "Rank"
	FROM "BSCL.Plataforma.BD::AUT.Evento" AS EVT
	INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST
		ON EVT."IdEstado" = EST."IdEstado"
	WHERE EVT."IdTipoEvento" = 'MOPLAC'
	AND EVT."IdEstado" <> 'AUT'
) 
WHERE "Rank" = 1;
GO
GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarEventoCambioPlanActivoSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO