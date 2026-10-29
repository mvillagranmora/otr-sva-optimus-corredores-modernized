SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarDatatec";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarDatatec" AS
SELECT
	Op.*,
	Estado."Nombre" as "NombreEstado",
	TipoOp."Nombre" as "NombreTipoOperacion",
	TO_NVARCHAR(Op."FechaOperacion",'YYYY-MM-DD HH24:MI:SS') as "FechaOperacionFormato",
	TO_NVARCHAR(Op."FechaCreacion",'YYYY-MM-DD HH24:MI:SS') as "FechaCreacionFormato",
	COALESCE(USU."CodigoUsuario",'') AS "UsuarioCreador",
	POR."CodigoPortafolio"||' '||POR."Proposito" AS "CodigoPropositoPortafolio"
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" Op
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" Estado
	ON Op."IdEstadoRegistro" = Estado."IdEstado"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOp
	ON TipoOp."IdTipoOperacion" = Op."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
	ON Op."IdUsuarioCreador" = USU."IdUsuario"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
	ON Op."IdPortafolio" = POR."IdPortafolio"
WHERE Op."IdEstadoRegistro" IN ('PEN', 'ERR')
  AND ((Op."IdOperacionSpot" = 0 AND Op."ClaseMensaje" = '5') OR (Op."ClaseMensaje" = '50'))
;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarDatatec" TO BCS_DESARROLLADOR WITH GRANT OPTION;