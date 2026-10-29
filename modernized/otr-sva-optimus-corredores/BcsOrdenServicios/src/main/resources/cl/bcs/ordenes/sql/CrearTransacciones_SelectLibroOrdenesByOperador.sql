SELECT
"IdEmpresa",
"IdLibroOrdenes",
"CodigoOperador",
"IdEstado"
FROM "BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador"
where "IdEmpresa" = :p_IdEmpresa