
-- Actualizacion de estado y otros a DepositoDetalle cuando entidad es 'TES.DepositoDetalle'

UPDATE 	DEP

SET	DEP."IdEstado" 		= :TipoEjecucion,
	DEP."IdConcepto"	= :IdConcepto,
	DEP."Descripcion"	= :Descripcion
	
FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DEP
WHERE DEP."IdDepositoDetalle" = :Id
AND :Entidad = 'TES.DepositoDetalle';
			 