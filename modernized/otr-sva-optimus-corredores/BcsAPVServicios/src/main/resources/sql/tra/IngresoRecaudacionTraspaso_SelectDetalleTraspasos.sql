SELECT
"IdTraspaso"
,"IdDetalleTraspaso"
,"IdContratoSAAPV"
,"IdRegimenTributario"
,"Codigo" AS "Regimen"
,"MontoPeso"
,"MontoUF"
,"Porcentaje"
,"IdCliente"
FROM "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV"
WHERE "IdTraspaso" IN (:IdTraspaso) AND "IdEstado" = 'ING';