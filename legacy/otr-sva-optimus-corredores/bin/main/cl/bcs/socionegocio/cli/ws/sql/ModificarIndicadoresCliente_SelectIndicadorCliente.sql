SELECT 
"IdCliente"
,"IdIndicador"
,"IdTipoIndicador"
,"ValorIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
WHERE "IdCliente" = :IdCliente AND "CodigoTipoIndicador" = :CodigoTipoIndicador;