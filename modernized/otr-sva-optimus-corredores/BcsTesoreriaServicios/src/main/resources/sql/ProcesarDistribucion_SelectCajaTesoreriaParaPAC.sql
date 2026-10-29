
SELECT "IdCaja", "Codigo", "Nombre" 
FROM "BSCL.Tesoreria.BD::TES.Caja" 
WHERE "Codigo" = 'PAC'  -- Caja para para movimientos PAC
AND "IdEstado" = 'ABT'  -- Abierta
AND "IdEmpresa" = :IdEmpresa;
		