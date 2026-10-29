SELECT COUNT(CASE WHEN "IdEstado" = 'VIG' THEN 1 ELSE NULL END) AS "Cantidad","IdClasificacionTipoInstrumento" FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
GROUP BY "IdClasificacionTipoInstrumento";