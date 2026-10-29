
-- Actualiza estado y datos de liquidacion a Deposito cuando entidad es TES.Deposito
UPDATE DEP

SET DEP."IdEstado" 					= 	:TipoEjecucion,
    DEP."IdConcepto"				= 	:IdConcepto,
    DEP."Descripcion"				= 	:Descripcion,      
    DEP."IdUsuarioLiquidador" 		= 	CASE WHEN :TipoEjecucion = 'LIQ' THEN :IdUsuario ELSE DEP."IdUsuarioLiquidador" END,   
    DEP."FechaHoraLiquidacion" 		= 	CASE WHEN :TipoEjecucion = 'LIQ' 
   								   			THEN TO_DATE(:FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7') 
   								   			ELSE DEP."FechaHoraLiquidacion" 
	   						  			END

FROM "BSCL.Tesoreria.BD::TES.Deposito" AS DEP
WHERE DEP."IdDeposito" = :Id
AND  :Entidad = 'TES.Deposito';