SELECT
"Tipo",
"Descripcion",
"ValorOrigen",
"ValorDestino"
FROM
"BSCL.Custodia.BD.Vistas::CUS.ConsultarHomologacionCMF"
WHERE
"IdEstado" = 'ACT' 
AND "Tipo" = :Tipo
AND ("ValorOrigen" = :ValorOrigen OR :ValorOrigen = '')
AND ("ValorDestino" = :ValorDestino OR :ValorDestino = '');
