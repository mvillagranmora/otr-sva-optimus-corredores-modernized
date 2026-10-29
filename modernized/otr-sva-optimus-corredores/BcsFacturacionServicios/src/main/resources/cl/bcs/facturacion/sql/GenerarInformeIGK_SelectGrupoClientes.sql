SELECT DISTINCT "DocumentoIdentidad" AS "Rut"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CM
			WHERE CM."CargoGananciaCap"='C' AND "IdEmpresa"=:IdEmpresa
			AND TO_VARCHAR ( CM."FechaComprobante", 'YYYY-MM')=TO_VARCHAR (:FechaConsulta, 'YYYY-MM');