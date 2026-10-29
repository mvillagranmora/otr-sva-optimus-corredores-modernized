
-- Actualiza estado a DepositoDetalle cuando entidad es TES.Deposito

UPDATE 	DEP
SET	DEP."IdEstado" = CASE
						WHEN :TipoEjecucion = 'EDP' AND DEP."IdEstado" = 'ING' THEN 'EDP'
   						WHEN :TipoEjecucion = 'LIQ' AND DEP."IdEstado" = 'EDP' THEN 'LIQ'
   						WHEN :TipoEjecucion = 'ANU' THEN :TipoEjecucion
   						ELSE :TipoEjecucion
   					END
FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DEP
WHERE DEP."IdDeposito" = :Id
AND   :Entidad         = 'TES.Deposito';
