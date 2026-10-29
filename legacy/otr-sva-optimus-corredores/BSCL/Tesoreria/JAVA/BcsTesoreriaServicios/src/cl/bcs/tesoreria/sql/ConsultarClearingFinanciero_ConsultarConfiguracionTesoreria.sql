SELECT "IdEmpresa", "CodigoParametro", "Descripcion", "Valor"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria"
WHERE "CodigoParametro" IN ('CLEARINGINCLUYERFPM','CLEARINGINCLUYEIFPM','CLEARINGINCLUYERFPH','CLEARINGINCLUYEIFPH','CAMBIOSCCLV')
AND   "IdEmpresa" = :p_IdEmpresa;