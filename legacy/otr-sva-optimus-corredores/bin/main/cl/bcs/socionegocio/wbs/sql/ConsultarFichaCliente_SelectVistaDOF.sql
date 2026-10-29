SELECT
CLIENTE."IdPersona"
,COALESCE( Declaracion."IdCliente",0) AS "IdCliente"
,COALESCE( Declaracion."IdDeclaracionOrigenFondos",0) AS "IdDeclaracionOrigenFondos"
,COALESCE( Declaracion."IdTipoOperacionOrigenFondos",'') AS "IdTipoOperacionOrigenFondos"
,CAST(Declaracion."FechaDeclaracion" AS VARCHAR(10)) AS "FechaDeclaracion"
,COALESCE( Declaracion."Comentario",'') AS "Comentario"
,COALESCE( Declaracion."OperacionInscripcion",'') AS "OperacionInscripcion"
,COALESCE( Declaracion."IdEstado",'') AS "IdEstado"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos" AS Declaracion
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" Cliente ON Cliente."IdCliente" = Declaracion."IdCliente"
WHERE Declaracion."IdEmpresa" = :p_IdEmpresa AND CLIENTE."IdCliente" IN (:p_IdClientes)