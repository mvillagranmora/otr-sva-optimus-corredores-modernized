SELECT COALESCE("IdEstadoPeriodo",'') as "estadoPeriodo" 
FROM "BSCL.Facturacion.BD::FAC.PeriodoCierre"
Where "Periodo" in (:fechaPeriodo) AND "IdEstado" <> 'ANU'