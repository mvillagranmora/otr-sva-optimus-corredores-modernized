SELECT 
Declaracion."IdDeclaracionOrigenFondos"
,Declaracion."IdTipoOperacionOrigenFondos"
,CAST(Declaracion."FechaDeclaracion" AS VARCHAR(10)) AS "FechaDeclaracion"
,Declaracion."Comentario"
,Declaracion."OperacionInscripcion"
,Declaracion."IdEstado"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos" AS Declaracion
WHERE Declaracion."IdEmpresa" = :p_IdEmpresa AND Declaracion."IdCliente" = :p_IdCliente 
